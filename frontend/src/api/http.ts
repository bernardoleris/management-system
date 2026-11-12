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

type JsonObject = Record<string, unknown>

function isJsonObject(value: unknown): value is JsonObject {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function finiteNumber(...values: unknown[]): number | undefined {
  for (const value of values) {
    const number = typeof value === 'number' ? value : typeof value === 'string' && value.trim() ? Number(value) : NaN
    if (Number.isFinite(number)) return number
  }
  return undefined
}

function normalizePagePayload(payload: unknown): unknown {
  if (!isJsonObject(payload) || !Array.isArray(payload.content)) return payload

  const nestedPage = isJsonObject(payload.page) ? payload.page : undefined
  const pageable = isJsonObject(payload.pageable) ? payload.pageable : undefined
  const hasPageMetadata = ['number', 'totalPages', 'totalElements', 'pageable'].some((key) => key in payload) || nestedPage !== undefined
  if (!hasPageMetadata) return payload

  const content = payload.content
  const number = Math.max(0, finiteNumber(payload.number, pageable?.pageNumber, nestedPage?.number, nestedPage?.pageNumber) ?? 0)
  const size = Math.max(0, finiteNumber(payload.size, pageable?.pageSize, nestedPage?.size) ?? content.length)
  const totalElements = Math.max(0, finiteNumber(payload.totalElements, nestedPage?.totalElements) ?? content.length)
  const totalPages = Math.max(0, finiteNumber(payload.totalPages, nestedPage?.totalPages) ?? (size ? Math.ceil(totalElements / size) : 0))

  return {
    ...payload,
    content,
    number,
    size,
    totalElements,
    totalPages,
    first: typeof payload.first === 'boolean' ? payload.first : number === 0,
    last: typeof payload.last === 'boolean' ? payload.last : number + 1 >= totalPages,
  }
}

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
  return normalizePagePayload(payload) as T
}

export function jsonBody(value: unknown): string { return JSON.stringify(value) }
