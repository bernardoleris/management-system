import { useEffect, useState, type FormEvent } from 'react'
import { ApiError } from '../../api/http'
import type { MovementType, Page, Product, StockMovement, StockMovementRequest, ExitReason } from '../../api/contracts'
import { jsonBody } from '../../api/http'
import { useAuth } from '../../auth/AuthContext'
import { Dialog, DialogConfirmationIcon, DialogFooter, DialogHeader } from '../../shared/ui/Dialog'
import styles from './operations.module.css'

const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
const movementNames: Record<MovementType, string> = { ENTRY: 'Entrada', EXIT: 'Saída', ADJUSTMENT: 'Ajuste', SALE: 'Venda' }
const exitNames: Record<ExitReason, string> = { DAMAGED: 'Danificado', EXPIRED: 'Vencido', DONATED: 'Brinde ou doação', LOST: 'Perda ou furto', OTHER: 'Outro' }
export function StockMovementsPage() {
  const { request, session } = useAuth()
  const [page, setPage] = useState<Page<StockMovement> | null>(null)
  const [pageNumber, setPageNumber] = useState(0)
  const [filter, setFilter] = useState<MovementType | ''>('')
  const [reload, setReload] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [detail, setDetail] = useState<StockMovement | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<StockMovement | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let active = true
    setLoading(true); setError('')
    const base = filter ? `/stock-movements/type/${filter}` : '/stock-movements'
    request<Page<StockMovement>>(`${base}?page=${pageNumber}&size=20&sort=date,desc`)
      .then((result) => { if (active) setPage(result) })
      .catch((reason: unknown) => { if (active) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar as movimentações.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [filter, pageNumber, reload, request, session?.companyId])

  function changeFilter(value: MovementType | '') { setFilter(value); setPageNumber(0) }

  async function deleteMovement() {
    if (!deleteTarget) return
    setBusy(true); setError('')
    try {
      await request<void>(`/stock-movements/${deleteTarget.id}`, { method: 'DELETE' })
      setNotice('Movimentação removida e estoque atualizado.')
      setDeleteTarget(null); setReload((value) => value + 1)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível remover a movimentação.')
    } finally { setBusy(false) }
  }

  return <section className={`page-section ${styles.featureScope}`}>
    <header className="page-heading operations-heading">
      <div><span className="eyebrow">ESTOQUE</span><h1>Movimentações</h1><p>Entradas, saídas, ajustes e vendas registradas em {session?.companyName}.</p></div>
      <button className="button-primary" onClick={() => setFormOpen(true)}>＋ Registrar movimentação</button>
    </header>
    {notice && <div className="operations-notice" role="status">{notice}<button onClick={() => setNotice('')} aria-label="Fechar aviso">×</button></div>}
    {error && <div className="form-alert" role="alert">{error}<button type="button" onClick={() => { setError(''); setReload((value) => value + 1) }}>Tentar novamente</button></div>}
    <section className="data-card">
      <div className="data-card-header operations-card-header"><div><h2>Histórico de estoque</h2><p>Ordenado da movimentação mais recente para a mais antiga.</p></div><label className="operation-filter">Tipo<select value={filter} onChange={(event) => changeFilter(event.target.value as MovementType | '')}><option value="">Todos</option><option value="ENTRY">Entrada</option><option value="EXIT">Saída</option><option value="ADJUSTMENT">Ajuste</option><option value="SALE">Venda</option></select></label></div>
      {loading ? <div className="operations-state" role="status">Carregando movimentações…</div> : page?.content.length ? <>
        <div className="operations-table-wrap"><table className="operations-table"><thead><tr><th>Data</th><th>Tipo</th><th>Itens</th><th>Responsável</th><th>Referência</th><th></th></tr></thead><tbody>
          {page.content.map((movement) => <tr key={movement.id}><td>{dateTime.format(new Date(movement.date))}</td><td><span className={`operation-type operation-${movement.type.toLowerCase()}`}>{movement.typeLabel || movementNames[movement.type]}</span></td><td>{movement.items.reduce((total, item) => total + (movement.type === 'ADJUSTMENT' ? Math.abs(item.quantityDifference ?? 0) : item.quantity), 0)} un. <small>em {movement.items.length} {movement.items.length === 1 ? 'produto' : 'produtos'}</small></td><td>{movement.adminLogin || '—'}</td><td>{movement.supplierTradeName || (movement.saleId ? `Venda #${movement.saleId}` : '—')}</td><td className="operation-actions"><button className="text-action" onClick={() => setDetail(movement)}>Detalhes</button>{movement.type !== 'SALE' && <button className="text-action text-action-muted" onClick={() => setDeleteTarget(movement)}>Remover</button>}</td></tr>)}
        </tbody></table></div>
        <div className="movement-pagination"><span>{page.totalElements} movimentações · Página {page.number + 1} de {Math.max(page.totalPages, 1)}</span><div><button className="button-secondary" disabled={page.first || loading} onClick={() => setPageNumber((n) => Math.max(0, n - 1))}>Anterior</button><button className="button-secondary" disabled={page.last || loading} onClick={() => setPageNumber((n) => n + 1)}>Próxima</button></div></div>
      </> : <div className="operations-state"><span className="operations-empty-icon">↕</span><h3>Nenhuma movimentação encontrada</h3><p>Registre uma entrada, saída ou ajuste para atualizar o estoque.</p></div>}
    </section>

    {formOpen && <MovementForm onClose={() => setFormOpen(false)} onSaved={() => { setFormOpen(false); setPageNumber(0); setFilter(''); setNotice('Movimentação registrada e estoque atualizado.'); setReload((value) => value + 1); window.dispatchEvent(new Event('estocai:stock-changed')) }} />}
    {detail && <Dialog labelledBy="movement-detail-title" variant="details" onClose={() => setDetail(null)}><DialogHeader><div><span className="eyebrow">MOVIMENTAÇÃO #{detail.id}</span><h2 id="movement-detail-title">{detail.typeLabel || movementNames[detail.type]}</h2><p>{dateTime.format(new Date(detail.date))} · {detail.adminLogin || 'Responsável não informado'}</p></div><button className="icon-button" aria-label="Fechar detalhes" onClick={() => setDetail(null)}>×</button></DialogHeader><div className="movement-detail-content">{detail.reason && <p><strong>Motivo:</strong> {detail.reason}</p>}{detail.exitReasonLabel && <p><strong>Motivo da saída:</strong> {detail.exitReasonLabel}</p>}{detail.observation && <p><strong>Observação:</strong> {detail.observation}</p>}{detail.supplierTradeName && <p><strong>Fornecedor:</strong> {detail.supplierTradeName}</p>}{detail.saleId && <p><strong>Venda:</strong> #{detail.saleId}</p>}<h3>Produtos</h3><ul>{detail.items.map((item) => <li key={item.id}><span>{item.productName}</span><strong>{detail.type === 'ADJUSTMENT' ? `${item.quantityBefore ?? 0} → ${item.quantity} un. (${(item.quantityDifference ?? 0) > 0 ? '+' : ''}${item.quantityDifference ?? 0})` : `${item.quantity} un.`}</strong></li>)}</ul></div><DialogFooter><button className="button-secondary" onClick={() => setDetail(null)}>Fechar</button></DialogFooter></Dialog>}
    {deleteTarget && <Dialog labelledBy="movement-delete-title" role="alertdialog" variant="confirmation" closeOnBackdrop={false} onClose={() => setDeleteTarget(null)}><DialogConfirmationIcon>!</DialogConfirmationIcon><h2 id="movement-delete-title">Remover movimentação?</h2><p>A ação será desfeita no estoque e esta movimentação será excluída. Vendas não podem ser removidas por aqui.</p><DialogFooter><button className="button-secondary" disabled={busy} onClick={() => setDeleteTarget(null)}>Cancelar</button><button className="button-primary movement-delete-button" disabled={busy} onClick={() => void deleteMovement()}>{busy ? 'Removendo…' : 'Remover e reverter estoque'}</button></DialogFooter></Dialog>}
  </section>
}

function MovementForm({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
  const { request, session } = useAuth()
  const [products, setProducts] = useState<Product[]>([])
  const [loadingProducts, setLoadingProducts] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [type, setType] = useState<'ENTRY' | 'EXIT' | 'ADJUSTMENT'>('ENTRY')
  const [exitReason, setExitReason] = useState<ExitReason>('OTHER')
  const [reason, setReason] = useState('')
  const [observation, setObservation] = useState('')
  const [productId, setProductId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [items, setItems] = useState<Array<{ product: Product; quantity: number }>>([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true
    setLoadingProducts(true); setLoadError('')
    request<Page<Product>>('/products/active?page=0&size=100&sort=name,asc').then(async (first) => {
      const pages = await Promise.all(Array.from({ length: Math.max(first.totalPages - 1, 0) }, (_, index) => request<Page<Product>>(`/products/active?page=${index + 1}&size=100&sort=name,asc`)))
      if (active) setProducts([...first.content, ...pages.flatMap((page) => page.content)])
    }).catch((failure: unknown) => { if (active) setLoadError(failure instanceof ApiError ? failure.message : 'Não foi possível carregar os produtos.') }).finally(() => { if (active) setLoadingProducts(false) })
    return () => { active = false }
  }, [request, session?.companyId])

  function addItem() {
    const product = products.find((item) => item.id === Number(productId))
    const amount = Number(quantity)
    if (!product || !Number.isInteger(amount) || amount < 1) return
    setItems((current) => [...current.filter((item) => item.product.id !== product.id), { product, quantity: amount }])
    setProductId(''); setQuantity('1')
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError('')
    if (!items.length) { setError('Adicione ao menos um produto à movimentação.'); return }
    if (type === 'ADJUSTMENT' && !reason.trim()) { setError('Informe o motivo do ajuste.'); return }
    setSaving(true)
    const payload: StockMovementRequest = { type, exitReason: type === 'EXIT' ? exitReason : null, reason: reason.trim() || null, observation: observation.trim() || null, supplierId: null, items: items.map(({ product, quantity: amount }) => ({ productId: product.id, quantity: amount })) }
    try { await request<StockMovement>('/stock-movements', { method: 'POST', body: jsonBody(payload) }); onSaved() }
    catch (failure) { setError(failure instanceof ApiError ? failure.message : 'Não foi possível registrar a movimentação.') }
    finally { setSaving(false) }
  }

  return <Dialog labelledBy="movement-form-title" className="movement-form-dialog" onClose={onClose} closeOnEscape={!saving} closeOnBackdrop={!saving}><DialogHeader><div><span className="eyebrow">CONTROLE DE ESTOQUE</span><h2 id="movement-form-title">Registrar movimentação</h2><p>O estoque será atualizado assim que salvar.</p></div><button className="icon-button" aria-label="Fechar formulário" onClick={onClose}>×</button></DialogHeader><form className="product-form" onSubmit={(event) => void submit(event)}>
    {error && <div className="form-alert" role="alert">{error}</div>}{loadError && <div className="form-alert" role="alert">{loadError}</div>}{loadingProducts && <div className="product-form-message" role="status">Carregando produtos…</div>}
    <div className="product-form-grid"><label>Tipo de movimentação<select value={type} onChange={(event) => setType(event.target.value as typeof type)}><option value="ENTRY">Entrada</option><option value="EXIT">Saída</option><option value="ADJUSTMENT">Ajuste de saldo</option></select></label>{type === 'EXIT' && <label>Motivo da saída<select value={exitReason} onChange={(event) => setExitReason(event.target.value as ExitReason)}>{Object.entries(exitNames).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>}
      <label className="product-form-wide">Produto<select value={productId} onChange={(event) => setProductId(event.target.value)} disabled={loadingProducts || !products.length}><option value="">Selecione um produto</option>{products.map((product) => <option value={product.id} key={product.id}>{product.name} · {product.quantity} un. disponíveis</option>)}</select></label>
      <label>{type === 'ADJUSTMENT' ? 'Quantidade real após ajuste' : 'Quantidade'}<input type="number" min="1" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} /></label><div className="movement-add-wrap"><button className="button-secondary" type="button" disabled={!productId || !Number(quantity)} onClick={addItem}>Adicionar produto</button></div>
      {items.length > 0 && <div className="movement-cart product-form-wide"><strong>Itens desta movimentação</strong>{items.map(({ product, quantity: amount }) => <div key={product.id}><span>{product.name}<small>Estoque atual: {product.quantity} un.</small></span><strong>{type === 'ADJUSTMENT' ? `Novo saldo: ${amount} un.` : `${amount} un.`}</strong><button type="button" aria-label={`Remover ${product.name}`} onClick={() => setItems((current) => current.filter((item) => item.product.id !== product.id))}>Remover</button></div>)}</div>}
      {type === 'ADJUSTMENT' && <label className="product-form-wide">Motivo do ajuste<input required maxLength={255} value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Ex.: contagem física do estoque" /></label>}
      {type !== 'ADJUSTMENT' && <label className="product-form-wide">Motivo (opcional)<input maxLength={255} value={reason} onChange={(event) => setReason(event.target.value)} /></label>}
      <label className="product-form-wide">Observação (opcional)<textarea maxLength={255} rows={2} value={observation} onChange={(event) => setObservation(event.target.value)} /></label>
    </div><DialogFooter><button className="button-secondary" type="button" onClick={onClose} disabled={saving}>Cancelar</button><button className="button-primary" type="submit" disabled={saving || loadingProducts || !products.length}>{saving ? 'Salvando…' : 'Atualizar estoque'}</button></DialogFooter>
  </form></Dialog>
}
