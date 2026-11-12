import { useEffect, useState, type PropsWithChildren } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/http'
import type { AvailableCompany } from '../api/contracts'
import { useAuth } from '../auth/AuthContext'
import { NewSaleDialog } from '../features/sales/components/NewSaleDialog'
import { Dialog, DialogConfirmationIcon, DialogFooter } from '../shared/ui/Dialog'
import './notifications.css'

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
  const [unreadAlerts, setUnreadAlerts] = useState(0)

  useEffect(() => {
    let active = true
    loadCompanies().then((items) => { if (active) setCompanies(items) }).catch(() => undefined)
    return () => { active = false }
  }, [loadCompanies])

  useEffect(() => {
    let active = true
    const refreshAlerts = () => request<unknown[]>('/alerts/unread').then((items) => { if (active) setUnreadAlerts(items.length) }).catch(() => { if (active) setUnreadAlerts(0) })
    void refreshAlerts()
    const onAlertsChanged = () => { void refreshAlerts() }
    window.addEventListener('estocai:alerts-changed', onAlertsChanged)
    return () => { active = false; window.removeEventListener('estocai:alerts-changed', onAlertsChanged) }
  }, [request, session?.companyId])

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
        <header className="topbar"><div className="mobile-brand"><span className="brand-mark">e</span> estocaí</div><div className="topbar-company">{session?.companyName}</div><div className="notification-anchor"><button className="notification-button" aria-label={`Alertas${unreadAlerts ? `, ${unreadAlerts} não lidos` : ''}`} aria-expanded={notificationsOpen} onClick={() => setNotificationsOpen((value) => !value)}><span aria-hidden="true">♧</span>{unreadAlerts > 0 && <i aria-label={`${unreadAlerts} alertas não lidos`} />}</button>{notificationsOpen && <section className="notification-popover" aria-label="Central de alertas"><div><strong>Alertas de estoque</strong><button className="icon-button" aria-label="Fechar alertas" onClick={() => setNotificationsOpen(false)}>×</button></div><p>{unreadAlerts ? `Você tem ${unreadAlerts} ${unreadAlerts === 1 ? 'alerta não lido' : 'alertas não lidos'}.` : 'Não há alertas não lidos.'}</p><button className="notification-link" onClick={() => { setNotificationsOpen(false); navigate('/alerts') }}>Abrir alertas</button></section>}</div></header>
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
          <button className="button-primary" type="button" disabled={loggingOut} onClick={() => void onLogout()}>{loggingOut ? 'Saindo…' : 'Sair da conta'}</button>
        </DialogFooter>
      </Dialog>}
    </div>
  )
}
