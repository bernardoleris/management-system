import './App.css'
import { useEffect } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { AppShell } from './components/AppShell'
import { LoginPage } from './pages/LoginPage'
import { OverviewPage } from './pages/OverviewPage'
import { SalesPage } from './pages/SalesPage'

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

function AppRoutes() {
  const { session } = useAuth()

  useEffect(() => {
    if (session) document.title = `Estocaí · ${session.companyName}`
    else document.title = 'Estocaí · Gestão de estoque'
  }, [session])

  return (
    <Routes>
      <Route path="/login" element={<LoginRoute />} />
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/overview" element={<RequireAuth><OverviewRoute /></RequireAuth>} />
      <Route path="/sales" element={<RequireAuth><AppShell><SalesPage /></AppShell></RequireAuth>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function App() {
  return <BrowserRouter><AuthProvider><AppRoutes /></AuthProvider></BrowserRouter>
}

export default App
