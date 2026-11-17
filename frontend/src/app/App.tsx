import '../styles/global.css'
import '../styles/actions.css'
import '../styles/password-flows.css'
import './shell.css'
import { useEffect } from 'react'
import { BrowserRouter, Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom'
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
import { ClientsPage } from '../features/clients/ClientsPage'
import { SuppliersPage } from '../features/suppliers/SuppliersPage'
import { MembersPage } from '../features/members/MembersPage'
import { AnalyticsPage } from '../features/analytics/AnalyticsPage'
import { CompanySettingsPage } from '../features/settings/CompanySettingsPage'
import { AccountLayout } from '../features/account/AccountLayout'
import { AccountPage } from '../features/account/AccountPage'
import { AccessibilityPage } from '../features/account/AccessibilityPage'

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
  return session?.role === 'OPERATOR' ? <Navigate to="/sales" replace /> : <OverviewPage />
}

function AnalyticsRoute() {
  const { session } = useAuth()
  return session?.role === 'OPERATOR' ? <Navigate to="/sales" replace /> : <AnalyticsPage />
}

function CatalogDataRoute() {
  const { session } = useAuth()
  return session?.role === 'OPERATOR' ? <Navigate to="/sales" replace /> : <CatalogDataPage />
}

function SuppliersRoute() {
  const { session } = useAuth()
  return session?.role === 'OPERATOR' ? <Navigate to="/sales" replace /> : <SuppliersPage />
}

function MembersRoute() {
  const { session } = useAuth()
  return session?.role === 'COMPANY_OWNER' ? <MembersPage /> : <Navigate to={session?.role === 'OPERATOR' ? '/sales' : '/overview'} replace />
}

function CompanySettingsRoute() {
  const { session } = useAuth()
  return session?.role === 'COMPANY_OWNER' ? <CompanySettingsPage /> : <Navigate to={session?.role === 'OPERATOR' ? '/sales' : '/overview'} replace />
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
      <Route path="/" element={<HomeRedirect />} />
      <Route element={<RequireAuth><AppShell><Outlet /></AppShell></RequireAuth>}>
        <Route path="account/password" element={<Navigate to="/account/security" replace />} />
        <Route path="account" element={<AccountLayout />}>
          <Route index element={<Navigate to="profile" replace />} />
          <Route path="profile" element={<AccountPage />} />
          <Route path="security" element={<ChangePasswordPage />} />
          <Route path="accessibility" element={<AccessibilityPage />} />
        </Route>
        <Route path="overview" element={<OverviewRoute />} />
        <Route path="/analytics" element={<AnalyticsRoute />} />
        <Route path="sales" element={<SalesPage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="stock-movements" element={<StockMovementsPage />} />
        <Route path="alerts" element={<AlertsPage />} />
        <Route path="catalog-data" element={<CatalogDataRoute />} />
        <Route path="suppliers" element={<SuppliersRoute />} />
        <Route path="members" element={<MembersRoute />} />
        <Route path="company-settings" element={<CompanySettingsRoute />} />
        <Route path="/clients" element={<ClientsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function App() {
  return <BrowserRouter><AuthProvider><AppRoutes /></AuthProvider></BrowserRouter>
}

export default App
