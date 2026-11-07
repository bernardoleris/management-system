export type CompanyRole = 'COMPANY_OWNER' | 'ADMIN' | 'OPERATOR'

export interface LoginResponse {
  login: string
  authenticated: boolean
  issuedAt: string
  expiration: string
  accessToken: string
  refreshToken: string
  companyId: number
  companyName: string
}

export interface AvailableCompany {
  companyId: number
  companyName: string
  role: CompanyRole
}

export interface SwitchCompanyResponse {
  companyId: number
  companyName: string
  issuedAt: string
  expiration: string
  accessToken: string
}

export interface AuthSession {
  login: string
  accessToken: string
  refreshToken: string
  companyId: number
  companyName: string
  role: CompanyRole
  expiration: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export interface Product {
  id: number
  name: string
  description: string | null
  barcode: string | null
  salePrice: number
  quantity: number
  active: boolean
  categoryName: string
  brandName: string
}

export interface Client {
  id: number
  documentType: 'CPF' | 'CNPJ'
  documentNumber: string
  user: {
    firstName: string
    lastName: string
    email: string
    phone: string | null
  }
}

export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'PIX'
export type SaleStatus = 'PENDING' | 'COMPLETED' | 'CANCELED'

export interface SaleItem {
  id: number
  productId: number
  productName: string
  quantity: number
  unitPrice: number
  subtotal: number
  discount: number
}

export interface Sale {
  id: number
  date: string
  status: SaleStatus
  paymentMethod: PaymentMethod
  discount: number
  totalValue: number
  notes: string | null
  adminLogin: string
  clientId: number | null
  clientDocumentNumber: string | null
  items: SaleItem[]
}

export interface ApiErrorBody {
  status?: number
  message?: string
  details?: string
}
