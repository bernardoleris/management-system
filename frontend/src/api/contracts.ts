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
  purchasePrice: number
  salePrice: number
  quantity: number
  active: boolean
  createdAt: string
  categoryId: number
  categoryName: string
  brandId: number
  brandName: string
  supplierId: number | null
  supplierTradeName: string | null
}

export interface ProductRequest {
  name: string
  description: string | null
  barcode: string | null
  purchasePrice: number
  salePrice: number
  quantity: number
  categoryId: number
  brandId: number
  supplierId: number | null
}

export interface NamedOption {
  id: number
  name: string
}

export interface SupplierOption {
  id: number
  tradeName: string
  cnpj: string
  phone: string
  email: string
  active: boolean
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

export type MovementType = 'ENTRY' | 'EXIT' | 'ADJUSTMENT' | 'SALE'
export type ExitReason = 'DAMAGED' | 'EXPIRED' | 'DONATED' | 'LOST' | 'OTHER'

export interface StockMovementItem {
  id: number
  productId: number
  productName: string
  quantity: number
  quantityBefore: number | null
  quantityDifference: number | null
}

export interface StockMovement {
  id: number
  type: MovementType
  typeLabel: string
  exitReason: ExitReason | null
  exitReasonLabel: string | null
  reason: string | null
  observation: string | null
  date: string
  adminId: number | null
  adminLogin: string | null
  supplierId: number | null
  supplierTradeName: string | null
  saleId: number | null
  items: StockMovementItem[]
}

export interface StockMovementRequest {
  type: Exclude<MovementType, 'SALE'>
  exitReason: ExitReason | null
  reason: string | null
  observation: string | null
  supplierId: number | null
  items: Array<{ productId: number; quantity: number }>
}

export type AlertType = 'LOW_STOCK' | 'OUT_OF_STOCK'

export interface StockAlert {
  id: number
  productId: number
  productName: string
  adminId: number | null
  adminLogin: string | null
  type: AlertType
  minimumQuantity: number
  message: string
  createdAt: string
  readAt: string | null
  read: boolean
  active: boolean
}

export interface AlertRequest {
  productId: number
  type: AlertType
  minimumQuantity: number
  message: string
}

export interface ApiErrorBody {
  status?: number
  message?: string
  details?: string
}
