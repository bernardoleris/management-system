import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../../api/http'
import type { Client, Page, PaymentMethod, Product, Sale } from '../../../api/contracts'
import { useAuth } from '../../../auth/AuthContext'
import { Dialog, DialogFooter, DialogHeader } from '../../../shared/ui/Dialog'
import styles from './NewSaleDialog.module.css'

interface CartLine { product: Product; quantity: number }
interface Props { open: boolean; onClose: () => void; onCompleted: () => void }

const paymentLabels: Record<PaymentMethod, string> = { CASH: 'Dinheiro', PIX: 'Pix', CREDIT_CARD: 'Crédito', DEBIT_CARD: 'Débito' }
const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export function NewSaleDialog({ open, onClose, onCompleted }: Props) {
  const { request } = useAuth()
  const [products, setProducts] = useState<Product[]>([])
  const [clients, setClients] = useState<Client[]>([])
  const [query, setQuery] = useState('')
  const [cart, setCart] = useState<CartLine[]>([])
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('PIX')
  const [clientId, setClientId] = useState('')
  const [discount, setDiscount] = useState('0')
  const [notes, setNotes] = useState('')
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [confirmComplete, setConfirmComplete] = useState(false)

  useEffect(() => {
    if (!open) return
    setError('')
    setSuccess('')
    setConfirmComplete(false)
    setLoading(true)
    Promise.all([
      request<Page<Product>>('/products/active?page=0&size=100&sort=name,asc'),
      request<Page<Client>>('/clients?page=0&size=100&sort=id,desc').catch(() => ({
        content: [], totalElements: 0, totalPages: 0, number: 0, size: 100, first: true, last: true,
      })),
    ]).then(([productPage, clientPage]) => {
      setProducts(productPage.content)
      setClients(clientPage.content)
    }).catch((reason: unknown) => {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar produtos e clientes.')
    }).finally(() => setLoading(false))
  }, [open, request])

  const filteredProducts = useMemo(() => {
    const normalized = query.trim().toLocaleLowerCase('pt-BR')
    if (!normalized) return products.slice(0, 8)
    return products.filter((product) => `${product.name} ${product.barcode ?? ''}`.toLocaleLowerCase('pt-BR').includes(normalized)).slice(0, 8)
  }, [products, query])

  const subtotal = cart.reduce((sum, line) => sum + line.product.salePrice * line.quantity, 0)
  const discountValue = Math.min(Math.max(Number(discount) || 0, 0), subtotal)
  const total = Math.max(0, subtotal - discountValue)

  function addProduct(product: Product) {
    setError('')
    if (product.quantity < 1) { setError(`${product.name} está sem estoque disponível.`); return }
    const existing = cart.find((line) => line.product.id === product.id)
    if (existing && existing.quantity >= product.quantity) { setError(`Estoque disponível de ${product.name}: ${product.quantity}.`); return }
    setCart((current) => {
      if (existing) return current.map((line) => line.product.id === product.id ? { ...line, quantity: Math.min(product.quantity, line.quantity + 1) } : line)
      return [...current, { product, quantity: 1 }]
    })
  }

  async function lookupBarcode(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const barcode = query.trim()
    if (!barcode) return
    try {
      const product = await request<Product>(`/products/barcode/${encodeURIComponent(barcode)}`)
      addProduct(product)
      setQuery('')
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Produto não encontrado pelo código de barras.')
    }
  }

  function updateQuantity(productId: number, quantity: number) {
    if (quantity < 1) { setCart((current) => current.filter((line) => line.product.id !== productId)); return }
    setCart((current) => current.map((line) => line.product.id === productId ? { ...line, quantity: Math.min(line.product.quantity, quantity) } : line))
  }

  async function submit(status: 'PENDING' | 'COMPLETED') {
    if (!cart.length) { setError('Adicione pelo menos um produto para continuar.'); return }
    setSaving(true)
    setError('')
    try {
      const sale = await request<Sale>('/sales', {
        method: 'POST',
        body: jsonBody({
          status, paymentMethod, discount: discountValue, notes: notes.trim() || undefined,
          clientId: clientId ? Number(clientId) : undefined,
          items: cart.map(({ product, quantity }) => ({ productId: product.id, quantity })),
        }),
      })
      if (status === 'COMPLETED') {
        setSuccess(`Venda #${sale.id} concluída com sucesso.`)
        window.setTimeout(onCompleted, 650)
      } else {
        setSuccess(`Venda #${sale.id} salva como pendente.`)
        window.setTimeout(onClose, 900)
      }
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível registrar a venda. Seu carrinho foi preservado.')
      setConfirmComplete(false)
    } finally { setSaving(false) }
  }

  if (!open) return null

  return (
      <div className={styles.featureScope}>
      <Dialog labelledBy="sale-dialog-title" className="sale-dialog" onClose={onClose} closeOnBackdrop={!saving} closeOnEscape={!saving && !confirmComplete}>
        <DialogHeader className="dialog-header"><div><span className="dialog-kicker">VENDA RÁPIDA</span><h2 id="sale-dialog-title">Nova venda</h2><p>Registre uma venda sem sair do que você estava fazendo.</p></div><button className="icon-button close-dialog" aria-label="Fechar venda" disabled={saving} onClick={onClose}>×</button></DialogHeader>
        <div className="sale-dialog-body">
          <div className="sale-entry-column">
            <form className="product-search" onSubmit={(event) => void lookupBarcode(event)}><label htmlFor="product-search">Adicionar produto</label><div className="search-field"><span aria-hidden="true">⌕</span><input id="product-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Busque pelo nome ou leia o código" autoComplete="off" autoFocus /><button type="submit" className="sr-only">Buscar código de barras</button></div></form>
            {error && <div className="form-alert" role="alert">{error}</div>}
            {success && <div className="form-success" role="status">{success}</div>}
            <div className="product-results" aria-live="polite">
              {loading ? <div className="inline-loading">Carregando catálogo…</div> : filteredProducts.length ? filteredProducts.map((product) => <button className="product-result" key={product.id} onClick={() => addProduct(product)} disabled={product.quantity < 1}><span className="product-glyph">{product.name.slice(0, 1).toUpperCase()}</span><span className="product-result-info"><strong>{product.name}</strong><small>{product.quantity} em estoque{product.barcode ? ` · ${product.barcode}` : ''}</small></span><span className="product-result-price">{money.format(product.salePrice)}</span><span className="add-product" aria-hidden="true">＋</span></button>) : <div className="empty-products">{products.length ? 'Nenhum produto corresponde à busca.' : 'Nenhum produto ativo disponível.'}</div>}
            </div>
          </div>
          <div className="sale-summary-column">
            <div className="cart-heading"><h3>Itens da venda</h3><span>{cart.reduce((count, line) => count + line.quantity, 0)} itens</span></div>
            <div className="cart-lines">{cart.length === 0 ? <div className="cart-empty"><span aria-hidden="true">▱</span><strong>Seu carrinho está vazio</strong><small>Escolha um produto para começar.</small></div> : cart.map(({ product, quantity }) => <div className="cart-line" key={product.id}><div className="cart-line-main"><strong>{product.name}</strong><small>{money.format(product.salePrice)} cada</small></div><div className="quantity-control"><button aria-label={`Diminuir ${product.name}`} onClick={() => updateQuantity(product.id, quantity - 1)}>−</button><span>{quantity}</span><button aria-label={`Aumentar ${product.name}`} disabled={quantity >= product.quantity} onClick={() => updateQuantity(product.id, quantity + 1)}>＋</button></div><strong className="line-total">{money.format(product.salePrice * quantity)}</strong></div>)}</div>
            <div className="sale-fields"><label>Cliente <select value={clientId} onChange={(event) => setClientId(event.target.value)}><option value="">Consumidor não identificado</option>{clients.map((client) => <option key={client.id} value={client.id}>{client.user.firstName} {client.user.lastName} · {client.documentNumber}</option>)}</select></label><label>Pagamento <select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value as PaymentMethod)}>{Object.entries(paymentLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label><label>Desconto (R$) <input type="number" min="0" max={subtotal} step="0.01" value={discount} onChange={(event) => setDiscount(event.target.value)} /></label><label>Observação <input maxLength={200} value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="Opcional" /></label></div>
            <div className="sale-totals"><div><span>Subtotal</span><span>{money.format(subtotal)}</span></div><div><span>Desconto</span><span>− {money.format(discountValue)}</span></div><div className="grand-total"><strong>Total</strong><strong>{money.format(total)}</strong></div></div>
          </div>
        </div>
        <DialogFooter className="dialog-footer"><button className="button-secondary" disabled={saving || !cart.length} onClick={() => void submit('PENDING')}>Salvar pendente</button><button className="button-primary" disabled={saving || !cart.length} onClick={() => setConfirmComplete(true)}>{saving ? 'Registrando…' : 'Concluir venda'}</button></DialogFooter>
        {confirmComplete && <Dialog labelledBy="confirm-title" role="alertdialog" variant="confirmation" className="confirm-card" onClose={() => setConfirmComplete(false)} closeOnBackdrop={false} closeOnEscape={!saving}><h3 id="confirm-title">Concluir venda?</h3><p>O estoque será atualizado e a venda ficará registrada como concluída.</p><div><button className="button-secondary" disabled={saving} onClick={() => setConfirmComplete(false)}>Voltar</button><button className="button-primary" disabled={saving} onClick={() => void submit('COMPLETED')}>{saving ? 'Concluindo…' : 'Confirmar venda'}</button></div></Dialog>}
      </Dialog>
      </div>
  )
}
