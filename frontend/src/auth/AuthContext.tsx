import { createContext, useCallback, useContext, useRef, useState, type PropsWithChildren } from 'react'
import { ApiError, apiRequest, jsonBody } from '../api/http'
import type { AuthSession, AvailableCompany, CompanyRole, LoginResponse, SwitchCompanyResponse } from '../api/contracts'

const STORAGE_KEY = 'estocai.session:v1'
const STORAGE_VERSION = 1
interface StoredSession { version: number; session: AuthSession }
interface AuthContextValue {
  session: AuthSession | null
  login: (login: string, password: string) => Promise<void>
  logout: () => Promise<void>
  request: <T>(path: string, options?: RequestInit) => Promise<T>
  loadCompanies: () => Promise<AvailableCompany[]>
  switchCompany: (companyId: number) => Promise<void>
  changePassword: (currentPassword: string, newPassword: string) => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

function readSession(): AuthSession | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const stored = JSON.parse(raw) as StoredSession
    return stored.version === STORAGE_VERSION && stored.session?.accessToken ? stored.session : null
  } catch { return null }
}

function roleFromCompanies(companies: AvailableCompany[], companyId: number): CompanyRole {
  const company = companies.find((item) => item.companyId === companyId)
  if (!company) throw new ApiError('Sua conta não tem acesso ativo à empresa selecionada.', 403)
  return company.role
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<AuthSession | null>(readSession)
  const sessionRef = useRef(session)
  sessionRef.current = session

  const persistSession = useCallback((next: AuthSession | null) => {
    sessionRef.current = next
    setSession(next)
    try {
      if (next) sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ version: STORAGE_VERSION, session: next }))
      else sessionStorage.removeItem(STORAGE_KEY)
    } catch { /* A aba mantém a sessão em memória se o armazenamento estiver indisponível. */ }
  }, [])
  const clearSession = useCallback(() => persistSession(null), [persistSession])

  const request = useCallback(async <T,>(path: string, options: RequestInit = {}): Promise<T> => {
    const current = sessionRef.current
    if (!current) throw new ApiError('Sua sessão terminou. Entre novamente.', 401)
    try {
      return await apiRequest<T>(path, { ...options, token: current.accessToken })
    } catch (error) {
      if (!(error instanceof ApiError) || error.status !== 401) throw error
      try {
        const refreshed = await apiRequest<LoginResponse>('/auth/refresh-token', {
          method: 'POST', body: jsonBody({ refreshToken: current.refreshToken, companyId: current.companyId }),
        })
        const next: AuthSession = {
          ...current,
          accessToken: refreshed.accessToken,
          refreshToken: refreshed.refreshToken || current.refreshToken,
          expiration: refreshed.expiration,
          companyName: refreshed.companyName || current.companyName,
        }
        persistSession(next)
        return await apiRequest<T>(path, { ...options, token: next.accessToken })
      } catch (refreshError) {
        clearSession()
        throw refreshError instanceof ApiError ? refreshError : new ApiError('Sua sessão expirou. Entre novamente.', 401)
      }
    }
  }, [clearSession, persistSession])

  const loadCompanies = useCallback(() => request<AvailableCompany[]>('/auth/companies'), [request])

  const login = useCallback(async (loginName: string, password: string) => {
    const response = await apiRequest<LoginResponse>('/auth/login', {
      method: 'POST', body: jsonBody({ login: loginName.trim(), password }),
    })
    const companies = await apiRequest<AvailableCompany[]>('/auth/companies', { token: response.accessToken })
    persistSession({
      login: response.login,
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      companyId: response.companyId,
      companyName: response.companyName,
      role: roleFromCompanies(companies, response.companyId),
      expiration: response.expiration,
    })
  }, [persistSession])

  const switchCompany = useCallback(async (companyId: number) => {
    const current = sessionRef.current
    if (!current) throw new ApiError('Sua sessão terminou. Entre novamente.', 401)
    const switched = await request<SwitchCompanyResponse>('/auth/switch-company', {
      method: 'POST', body: jsonBody({ companyId }),
    })
    const companies = await apiRequest<AvailableCompany[]>('/auth/companies', { token: switched.accessToken })
    persistSession({
      ...current,
      accessToken: switched.accessToken,
      companyId: switched.companyId,
      companyName: switched.companyName,
      expiration: switched.expiration,
      role: roleFromCompanies(companies, switched.companyId),
    })
  }, [persistSession, request])

  const logout = useCallback(async () => {
    const current = sessionRef.current
    if (current) {
      try {
        await apiRequest<void>('/auth/logout', {
          method: 'POST', body: jsonBody({ refreshToken: current.refreshToken }), token: current.accessToken,
        })
      } catch { /* Encerrar localmente mesmo se o servidor estiver indisponível. */ }
    }
    clearSession()
  }, [clearSession])

  const changePassword = useCallback(async (currentPassword: string, newPassword: string) => {
    await request<void>('/auth/change-password', {
      method: 'POST', body: jsonBody({ currentPassword, newPassword }),
    })
    clearSession()
  }, [clearSession, request])

  return <AuthContext.Provider value={{ session, login, logout, request, loadCompanies, switchCompany, changePassword }}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth precisa estar dentro de AuthProvider.')
  return value
}
