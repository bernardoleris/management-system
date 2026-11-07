import { useEffect, useState, type PropsWithChildren } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/http'
import type { AvailableCompany } from '../api/contracts'
import { useAuth } from '../auth/AuthContext'
import { NewSaleDialog } from './NewSaleDialog'
import './notifications.css'

export function AppShell({ children }: PropsWithChildren) {
  const { session, logout, loadCompanies, switchCompany } = useAuth()
  const navigate = useNavigate()
  const [saleOpen, setSaleOpen] = useState(false)
  const [companies, setCompanies] = useState<AvailableCompany[]>([])
  const [companyError, setCompanyError] = useState('')
  const [switching, setSwitching] = useState(false)
  const [notificationsOpen, setNotificationsOpen] = useState(false)

  useEffect(() => {
    let active = true
    loadCompanies().then((items) => { if (active) setCompanies(items) }).catch(() => undefined)
    return () => { active = false }
  }, [loadCompanies])

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
    await logout()
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
        <NavLink className="nav-link" to="/sales"><span aria-hidden="true">↗</span> Vendas</NavLink>
        <div className="sidebar-push" />
        <div className="sidebar-user"><div className="avatar">{session?.login.slice(0, 1).toUpperCase()}</div><div className="user-meta"><strong>{session?.login}</strong><span>{session?.role === 'COMPANY_OWNER' ? 'Proprietário' : session?.role === 'ADMIN' ? 'Administrador' : 'Operador'}</span></div><button className="icon-button logout-button" aria-label="Sair da conta" onClick={() => void onLogout()}>↪</button></div>
      </aside>
      <main className="main-area">
        <header className="topbar"><div className="mobile-brand"><span className="brand-mark">e</span> estocaí</div><div className="topbar-company">{session?.companyName}</div><div className="notification-anchor"><button className="notification-button" aria-label="Notificações" aria-expanded={notificationsOpen} onClick={() => setNotificationsOpen((value) => !value)}><span aria-hidden="true">♧</span></button>{notificationsOpen && <section className="notification-popover" aria-label="Centro de notificações"><div><strong>Notificações</strong><button className="icon-button" aria-label="Fechar notificações" onClick={() => setNotificationsOpen(false)}>×</button></div><p>Os alertas da sua operação aparecerão aqui.</p><small>O centro será conectado ao backend na próxima etapa.</small></section>}</div></header>
        <div className="page-content">{children}</div>
      </main>
      <button className="global-sale-button" onClick={() => setSaleOpen(true)}><span aria-hidden="true">＋</span> Nova venda</button>
      <NewSaleDialog open={saleOpen} onClose={() => setSaleOpen(false)} onCompleted={() => { setSaleOpen(false); window.dispatchEvent(new Event('estocai:sale-created')); if (location.pathname !== '/sales') navigate('/sales') }} />
    </div>
  )
}
