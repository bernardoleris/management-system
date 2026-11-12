import '../styles/global.css'
import '../styles/actions.css'
import '../styles/password-flows.css'
import './shell.css'
import { useEffect } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider, useAuth } from '../auth/AuthContext'
import { AppShell } from './AppShell'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { ForgotPasswordPage } from '../features/auth/pages/ForgotPasswordPage'
import { ResetPasswordPage } from '../features/auth/pages/ResetPasswordPage'
import { ChangePasswordPage } from '../features/account/ChangePasswordPage'
import { RegisterPage } from '../features/auth/pages/RegisterPage'
import { VerifyEmailPage } from '../features/auth/pages/VerifyEmailPage'
import { OverviewPage } from '../features/overview/OverviewPage'
import { SalesPage } from '../features/sales/SalesPage'
import { ProductsPage } from '../features/products/ProductsPage'
import { StockMovementsPage } from '../features/inventory/StockMovementsPage'
import { AlertsPage } from '../features/alerts/AlertsPage'
import { CatalogDataPage } from '../features/catalog-data/CatalogDataPage'

function HomeRedirect() {
  const { session } = useAuth()
  return <Navigate to={session?.role === 'OPERATOR' ? '/sales' : '/overview'} replace />
}

function RequireAuth({ children }: { children: ReactNode }) {
  const { session } = useAuth()
  const location = useLocation()
  return session ? children : <Navigate to="/login" state={{ from: location }} replace />
}

function LoginRoute() {
  const { session } = useAuth()
  return session ? <HomeRedirect /> : <LoginPage />
}

function OverviewRoute() {
  const { session } = useAuth()
  return session?.role === 'OPERATOR' ? <Navigate to="/sales" replace /> : <AppShell><OverviewPage /></AppShell>
}

function CatalogDataRoute() {
  const { session } = useAuth()
  return session?.role === 'OPERATOR' ? <Navigate to="/sales" replace /> : <AppShell><CatalogDataPage /></AppShell>
}

function AppRoutes() {
  const { session } = useAuth()

  useEffect(() => {
    if (session) document.title = `Estocaí · ${session.companyName}`
    else document.title = 'Estocaí · Gestão de estoque'
  }, [session])

  return (
    <Routes>
      <Route path="/login" element={<LoginRoute />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/account/password" element={<RequireAuth><AppShell><ChangePasswordPage /></AppShell></RequireAuth>} />
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/overview" element={<RequireAuth><OverviewRoute /></RequireAuth>} />
      <Route path="/sales" element={<RequireAuth><AppShell><SalesPage /></AppShell></RequireAuth>} />
      <Route path="/products" element={<RequireAuth><AppShell><ProductsPage /></AppShell></RequireAuth>} />
      <Route path="/stock-movements" element={<RequireAuth><AppShell><StockMovementsPage /></AppShell></RequireAuth>} />
      <Route path="/alerts" element={<RequireAuth><AppShell><AlertsPage /></AppShell></RequireAuth>} />
      <Route path="/catalog-data" element={<RequireAuth><CatalogDataRoute /></RequireAuth>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function App() {
  return <BrowserRouter><AuthProvider><AppRoutes /></AuthProvider></BrowserRouter>
}

export default App
