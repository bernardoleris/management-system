import type { ApiErrorBody } from './contracts'

const configuredBaseUrl = import.meta.env.VITE_API_URL?.trim()
export const API_BASE_URL = (configuredBaseUrl || 'http://localhost:8080').replace(/\/$/, '')

export class ApiError extends Error {
  readonly status: number
  readonly details?: string

  constructor(message: string, status: number, details?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

interface RequestOptions extends RequestInit { token?: string }

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { token, headers: inputHeaders, ...init } = options
  const headers = new Headers(inputHeaders)
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers })
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor. Tente novamente.', 0)
  }
  if (response.status === 204) return undefined as T

  const payload: unknown = await response.json().catch(() => undefined)
  if (!response.ok) {
    const body = payload as ApiErrorBody | undefined
    throw new ApiError(body?.message || 'Ocorreu um erro ao processar sua solicitação.', response.status, body?.details)
  }
  return payload as T
}

export function jsonBody(value: unknown): string { return JSON.stringify(value) }
