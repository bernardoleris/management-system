import { useCallback, useEffect, useRef, useState, type PropsWithChildren } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/http'
import type { AvailableCompany, StockAlert } from '../api/contracts'
import { useAuth } from '../auth/AuthContext'
import { NewSaleDialog } from '../features/sales/components/NewSaleDialog'
import { Dialog, DialogConfirmationIcon, DialogFooter } from '../shared/ui/Dialog'
import './notifications.css'

const notificationDate = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function AppShell({ children }: PropsWithChildren) {
  const { session, logout, loadCompanies, switchCompany } = useAuth()
  const { request } = useAuth()
  const navigate = useNavigate()
  const [saleOpen, setSaleOpen] = useState(false)
  const [logoutConfirmOpen, setLogoutConfirmOpen] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)
  const [companies, setCompanies] = useState<AvailableCompany[]>([])
  const [companyError, setCompanyError] = useState('')
  const [switching, setSwitching] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [notificationsClosing, setNotificationsClosing] = useState(false)
  const [unreadAlerts, setUnreadAlerts] = useState(0)
  const [unreadAlertItems, setUnreadAlertItems] = useState<StockAlert[]>([])
  const [recentAlertIds, setRecentAlertIds] = useState<number[]>([])
  const [notificationsLoading, setNotificationsLoading] = useState(true)
  const [notificationError, setNotificationError] = useState('')
  const [busyAlertId, setBusyAlertId] = useState<number | null>(null)
  const notificationAnchorRef = useRef<HTMLDivElement>(null)
  const notificationButtonRef = useRef<HTMLButtonElement>(null)
  const notificationCloseTimeoutRef = useRef<number | null>(null)
  const alertHighlightTimeoutRef = useRef<number | null>(null)

  const openNotifications = useCallback(() => {
    if (notificationCloseTimeoutRef.current !== null) {
      window.clearTimeout(notificationCloseTimeoutRef.current)
      notificationCloseTimeoutRef.current = null
    }
    setNotificationsClosing(false)
    setNotificationsOpen(true)
  }, [])

  const closeNotifications = useCallback((restoreFocus = false) => {
    if (notificationCloseTimeoutRef.current !== null) window.clearTimeout(notificationCloseTimeoutRef.current)
    setNotificationsOpen(false)
    const exitDelay = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 150
    setNotificationsClosing(exitDelay > 0)
    notificationCloseTimeoutRef.current = window.setTimeout(() => {
      setNotificationsClosing(false)
      notificationCloseTimeoutRef.current = null
    }, exitDelay)
    if (restoreFocus) window.requestAnimationFrame(() => notificationButtonRef.current?.focus())
  }, [])

  const toggleNotifications = useCallback(() => {
    if (notificationsOpen) closeNotifications()
    else openNotifications()
  }, [closeNotifications, notificationsOpen, openNotifications])

  useEffect(() => () => {
    if (notificationCloseTimeoutRef.current !== null) window.clearTimeout(notificationCloseTimeoutRef.current)
    if (alertHighlightTimeoutRef.current !== null) window.clearTimeout(alertHighlightTimeoutRef.current)
  }, [])

  useEffect(() => {
    let active = true
    loadCompanies().then((items) => { if (active) setCompanies(items) }).catch(() => undefined)
    return () => { active = false }
  }, [loadCompanies])

  useEffect(() => {
    let active = true
    let knownAlertIds: Set<number> | null = null
    setNotificationsLoading(true)
    setUnreadAlertItems([])
    setUnreadAlerts(0)
    setNotificationError('')
    const refreshAlerts = async () => {
      try {
        const items = await request<StockAlert[]>('/alerts/unread')
        if (!active) return
        const newAlerts = knownAlertIds === null ? [] : items.filter((item) => !knownAlertIds?.has(item.id))
        const shouldOpen = knownAlertIds === null ? items.length > 0 : newAlerts.length > 0
        knownAlertIds = new Set(items.map((item) => item.id))
        setUnreadAlertItems([...items].sort((first, second) => new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime()))
        setUnreadAlerts(items.length)
        setNotificationError('')
        if (newAlerts.length) {
          setRecentAlertIds(newAlerts.map((item) => item.id))
          if (alertHighlightTimeoutRef.current !== null) window.clearTimeout(alertHighlightTimeoutRef.current)
          alertHighlightTimeoutRef.current = window.setTimeout(() => {
            setRecentAlertIds([])
            alertHighlightTimeoutRef.current = null
          }, 1800)
        }
        if (shouldOpen) openNotifications()
      } catch {
        if (active) {
          setNotificationError('Não foi possível carregar os alertas.')
          setUnreadAlertItems([])
          setUnreadAlerts(0)
        }
      } finally {
        if (active) setNotificationsLoading(false)
      }
    }
    void refreshAlerts()
    const intervalId = window.setInterval(() => { void refreshAlerts() }, 30_000)
    const onAlertsChanged = () => { void refreshAlerts() }
    window.addEventListener('estocai:alerts-changed', onAlertsChanged)
    return () => { active = false; window.clearInterval(intervalId); window.removeEventListener('estocai:alerts-changed', onAlertsChanged) }
  }, [openNotifications, request, session?.companyId])

  useEffect(() => {
    if (!notificationsOpen) return
    function onPointerDown(event: PointerEvent) {
      if (event.target instanceof Node && !notificationAnchorRef.current?.contains(event.target)) closeNotifications()
    }
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') closeNotifications(true)
    }
    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [closeNotifications, notificationsOpen])

  useEffect(() => {
    const openSale = () => setSaleOpen(true)
    window.addEventListener('estocai:open-sale', openSale)
    return () => window.removeEventListener('estocai:open-sale', openSale)
  }, [])

  async function onCompanyChange(companyId: number) {
    if (companyId === session?.companyId) return
    setCompanyError('')
    setSwitching(true)
    try {
      await switchCompany(companyId)
      navigate('/', { replace: true })
    } catch (error) {
      setCompanyError(error instanceof ApiError ? error.message : 'Não foi possível trocar de empresa.')
    } finally {
      setSwitching(false)
    }
  }

  async function onLogout() {
    setLoggingOut(true)
    await logout()
    setLoggingOut(false)
    setLogoutConfirmOpen(false)
    navigate('/login', { replace: true })
  }

  async function markAlertRead(alert: StockAlert) {
    setBusyAlertId(alert.id)
    setNotificationError('')
    try {
      await request<StockAlert>(`/alerts/${alert.id}/read`, { method: 'PATCH' })
      setUnreadAlertItems((items) => items.filter((item) => item.id !== alert.id))
      setUnreadAlerts((count) => Math.max(0, count - 1))
      window.dispatchEvent(new Event('estocai:alerts-changed'))
    } catch (error) {
      setNotificationError(error instanceof ApiError ? error.message : 'Não foi possível marcar o alerta como lido.')
    } finally {
      setBusyAlertId(null)
    }
  }

  return (
    <div className="app-frame">
      <aside className="sidebar">
        <NavLink className="brand" to="/" aria-label="Estocaí, início">
          <span className="brand-mark" aria-hidden="true">e</span><span>estocaí</span>
        </NavLink>
        <label className="company-switcher-label" htmlFor="company-switcher">Empresa ativa</label>
        <select id="company-switcher" className="company-switcher" value={session?.companyId ?? ''} disabled={switching || companies.length < 2} onChange={(event) => void onCompanyChange(Number(event.target.value))}>
          {companies.length === 0 && <option value={session?.companyId}>{session?.companyName}</option>}
          {companies.map((company) => <option value={company.companyId} key={company.companyId}>{company.companyName}</option>)}
        </select>
        {companyError && <p className="switch-error" role="alert">{companyError}</p>}
        <div className="nav-caption">OPERAÇÃO</div>
        {session?.role !== 'OPERATOR' && <NavLink className="nav-link" to="/overview"><span aria-hidden="true">◫</span> Visão geral</NavLink>}
        <NavLink className="nav-link" to="/products"><span aria-hidden="true">▦</span> Produtos</NavLink>
        {session?.role !== 'OPERATOR' && <NavLink className="nav-link" to="/catalog-data"><span aria-hidden="true">◇</span> Categorias e marcas</NavLink>}
        <NavLink className="nav-link" to="/sales"><span aria-hidden="true">↗</span> Vendas</NavLink>
        <NavLink className="nav-link" to="/stock-movements"><span aria-hidden="true">↕</span> Movimentações</NavLink>
        <NavLink className="nav-link" to="/alerts"><span aria-hidden="true">♧</span> Alertas</NavLink>
        <NavLink className="nav-link" to="/account/password"><span aria-hidden="true">♙</span> Minha senha</NavLink>
        <div className="sidebar-push" />
        <div className="sidebar-user"><div className="avatar">{session?.login.slice(0, 1).toUpperCase()}</div><div className="user-meta"><strong>{session?.login}</strong><span>{session?.role === 'COMPANY_OWNER' ? 'Proprietário' : session?.role === 'ADMIN' ? 'Administrador' : 'Operador'}</span></div><button className="icon-button logout-button" aria-label="Sair da conta" onClick={() => setLogoutConfirmOpen(true)}>↪</button></div>
      </aside>
      <main className="main-area">
        <header className="topbar">
          <div className="mobile-brand"><span className="brand-mark">e</span> estocaí</div>
          <div className="topbar-company">{session?.companyName}</div>
          <div className="notification-anchor" ref={notificationAnchorRef}>
            <button ref={notificationButtonRef} className={`notification-button${recentAlertIds.length ? ' notification-button-new' : ''}`} aria-label={`Alertas${unreadAlerts ? `, ${unreadAlerts} não lidos` : ''}`} aria-expanded={notificationsOpen} aria-controls="notification-popover" onClick={toggleNotifications}>
              <span aria-hidden="true">♧</span>
              {unreadAlerts > 0 && <i aria-label={`${unreadAlerts} alertas não lidos`} />}
            </button>
            {(notificationsOpen || notificationsClosing) && <section id="notification-popover" className={`notification-popover${notificationsClosing ? ' notification-popover-closing' : ''}`} aria-label="Central de alertas" aria-live="polite" aria-hidden={notificationsClosing}>
              <div className="notification-popover-header">
                <strong>Alertas de estoque</strong>
                <button className="icon-button" aria-label="Fechar alertas" onClick={() => closeNotifications(true)}>×</button>
              </div>
              {notificationError && <p className="notification-error" role="alert">{notificationError}</p>}
              {notificationsLoading ? <p role="status">Carregando alertas…</p> : unreadAlertItems.length ? <>
                <p className="notification-summary">{unreadAlerts} {unreadAlerts === 1 ? 'alerta não lido' : 'alertas não lidos'}</p>
                <ul className="notification-list">
                  {unreadAlertItems.slice(0, 5).map((alert) => <li className={recentAlertIds.includes(alert.id) ? 'notification-item-new' : undefined} key={alert.id}>
                    <div className="notification-item-heading"><strong>{alert.productName}</strong><span className={alert.type === 'OUT_OF_STOCK' ? 'notification-kind notification-kind-empty' : 'notification-kind'}>{alert.type === 'OUT_OF_STOCK' ? 'Sem estoque' : 'Estoque baixo'}</span></div>
                    <p>{alert.message}</p>
                    <small>{alert.minimumQuantity} un. · {notificationDate.format(new Date(alert.createdAt))}</small>
                    <button className="notification-mark-read" disabled={busyAlertId === alert.id} onClick={() => void markAlertRead(alert)}>{busyAlertId === alert.id ? 'Salvando…' : 'Marcar como lido'}</button>
                  </li>)}
                </ul>
                {unreadAlerts > 5 && <small className="notification-more">Mais {unreadAlerts - 5} alertas não lidos</small>}
              </> : <p>Não há alertas não lidos.</p>}
              <button className="notification-link" onClick={() => { closeNotifications(); navigate('/alerts') }}>Abrir central de alertas</button>
            </section>}
          </div>
        </header>
        <div className="page-content">{children}</div>
      </main>
      <button className="global-sale-button" type="button" onClick={() => setSaleOpen(true)}><span aria-hidden="true">＋</span> Nova venda</button>
      <NewSaleDialog open={saleOpen} onClose={() => setSaleOpen(false)} onCompleted={() => { setSaleOpen(false); window.dispatchEvent(new Event('estocai:sale-created')); if (location.pathname !== '/sales') navigate('/sales') }} />
      {logoutConfirmOpen && <Dialog labelledBy="logout-confirm-title" describedBy="logout-confirm-description" role="alertdialog" variant="confirmation" closeOnBackdrop={!loggingOut} closeOnEscape={!loggingOut} onClose={() => setLogoutConfirmOpen(false)}>
        <DialogConfirmationIcon>↪</DialogConfirmationIcon>
        <h2 id="logout-confirm-title">Sair da conta?</h2>
        <p id="logout-confirm-description">Sua sessão será encerrada. Você poderá entrar novamente quando quiser.</p>
        <DialogFooter>
          <button className="button-secondary" type="button" disabled={loggingOut} onClick={() => setLogoutConfirmOpen(false)}>Continuar conectado</button>
          <button className="button-primary logout-confirm-button" type="button" disabled={loggingOut} onClick={() => void onLogout()}>{loggingOut ? 'Saindo…' : 'Sair da conta'}</button>
        </DialogFooter>
      </Dialog>}
    </div>
  )
}
