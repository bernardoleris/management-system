import { useEffect, useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../api/http'
import type { AlertRequest, AlertType, Page, Product, StockAlert } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import { Dialog, DialogConfirmationIcon, DialogFooter, DialogHeader } from '../../shared/ui/Dialog'
import styles from '../inventory/operations.module.css'

const createdDate = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
type AlertView = 'active' | 'unread' | 'all'

export function AlertsPage() {
  const { request, session } = useAuth()
  const [view, setView] = useState<AlertView>('active')
  const [alerts, setAlerts] = useState<StockAlert[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [reload, setReload] = useState(0)
  const [form, setForm] = useState<StockAlert | null | undefined>(undefined)
  const [deactivate, setDeactivate] = useState<StockAlert | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  useEffect(() => {
    let active = true
    setLoading(true); setError('')
    const path = view === 'unread' ? '/alerts/unread' : view === 'active' ? '/alerts/active' : '/alerts'
    request<StockAlert[]>(path).then((data) => { if (active) setAlerts(data) })
      .catch((reason: unknown) => { if (active) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os alertas.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [request, reload, session?.companyId, view])

  async function markRead(alert: StockAlert) {
    setBusyId(alert.id); setError('')
    try {
      await request<StockAlert>(`/alerts/${alert.id}/read`, { method: 'PATCH' })
      setNotice('Alerta marcado como lido.'); setReload((value) => value + 1); window.dispatchEvent(new Event('estocai:alerts-changed'))
    } catch (reason) { setError(reason instanceof ApiError ? reason.message : 'Não foi possível atualizar o alerta.') }
    finally { setBusyId(null) }
  }

  async function deactivateAlert() {
    if (!deactivate) return
    setBusyId(deactivate.id); setError('')
    try {
      await request<void>(`/alerts/${deactivate.id}`, { method: 'DELETE' })
      setNotice('Alerta desativado.'); setDeactivate(null); setReload((value) => value + 1); window.dispatchEvent(new Event('estocai:alerts-changed'))
    } catch (reason) { setError(reason instanceof ApiError ? reason.message : 'Não foi possível desativar o alerta.') }
    finally { setBusyId(null) }
  }

  return <section className={`page-section ${styles.featureScope}`}>
    <header className="page-heading operations-heading"><div><span className="eyebrow">ACOMPANHAMENTO</span><h1>Alertas de estoque</h1><p>Acompanhe alertas configurados para os produtos de {session?.companyName}.</p></div><button className="button-primary" onClick={() => setForm(null)}>＋ Novo alerta</button></header>
    {notice && <div className="operations-notice" role="status">{notice}<button onClick={() => setNotice('')} aria-label="Fechar aviso">×</button></div>}
    {error && <div className="form-alert" role="alert">{error}<button type="button" onClick={() => { setError(''); setReload((value) => value + 1) }}>Tentar novamente</button></div>}
    <section className="data-card"><div className="data-card-header operations-card-header"><div><h2>Alertas configurados</h2><p>Marque como lido ou desative alertas que não precisam mais aparecer.</p></div><div className="alert-view-tabs" role="tablist" aria-label="Filtrar alertas"><button role="tab" aria-selected={view === 'active'} className={view === 'active' ? 'selected' : ''} onClick={() => setView('active')}>Ativos</button><button role="tab" aria-selected={view === 'unread'} className={view === 'unread' ? 'selected' : ''} onClick={() => setView('unread')}>Não lidos</button><button role="tab" aria-selected={view === 'all'} className={view === 'all' ? 'selected' : ''} onClick={() => setView('all')}>Todos</button></div></div>
      {loading ? <div className="operations-state" role="status">Carregando alertas…</div> : alerts.length ? <div className="alert-list">{alerts.map((alert) => <article className={`alert-row ${alert.read ? 'alert-read' : ''} ${!alert.active ? 'alert-inactive' : ''}`} key={alert.id}><span className={`alert-symbol ${alert.type === 'OUT_OF_STOCK' ? 'alert-symbol-empty' : ''}`} aria-hidden="true">{alert.type === 'OUT_OF_STOCK' ? '!' : '↘'}</span><div className="alert-main"><div className="alert-title-line"><h3>{alert.productName}</h3><span className={`alert-kind ${alert.type === 'OUT_OF_STOCK' ? 'alert-kind-empty' : ''}`}>{alert.type === 'OUT_OF_STOCK' ? 'Fora de estoque' : 'Estoque baixo'}</span>{!alert.read && <span className="alert-unread-dot" aria-label="Não lido" />}{!alert.active && <span className="alert-disabled-tag">Desativado</span>}</div><p>{alert.message}</p><small>Criado em {createdDate.format(new Date(alert.createdAt))} · Limite de {alert.minimumQuantity} un.{alert.adminLogin ? ` · ${alert.adminLogin}` : ''}</small></div><div className="alert-actions">{!alert.read && <button className="text-action" disabled={busyId === alert.id} onClick={() => void markRead(alert)}>{busyId === alert.id ? 'Salvando…' : 'Marcar como lido'}</button>}{alert.active && <button className="text-action text-action-muted" onClick={() => setForm(alert)}>Editar</button>}{alert.active && <button className="text-action text-action-muted" onClick={() => setDeactivate(alert)}>Desativar</button>}</div></article>)}</div> : <div className="operations-state"><span className="operations-empty-icon">⌁</span><h3>{view === 'unread' ? 'Tudo em dia' : 'Nenhum alerta encontrado'}</h3><p>{view === 'unread' ? 'Você já leu todos os alertas ativos.' : 'Crie um alerta para acompanhar o nível de estoque de um produto.'}</p>{view !== 'unread' && <button className="button-secondary" onClick={() => setForm(null)}>Criar alerta</button>}</div>}
    </section>
    {form !== undefined && <AlertForm alert={form} onClose={() => setForm(undefined)} onSaved={(message) => { setForm(undefined); setNotice(message); setReload((value) => value + 1); window.dispatchEvent(new Event('estocai:alerts-changed')) }} />}
    {deactivate && <Dialog labelledBy="alert-deactivate-title" role="alertdialog" variant="confirmation" closeOnBackdrop={false} onClose={() => setDeactivate(null)}><DialogConfirmationIcon>!</DialogConfirmationIcon><h2 id="alert-deactivate-title">Desativar alerta?</h2><p>O alerta de <strong>{deactivate.productName}</strong> será ocultado da lista de ativos. Você ainda pode consultá-lo em “Todos”.</p><DialogFooter><button className="button-secondary" disabled={busyId === deactivate.id} onClick={() => setDeactivate(null)}>Cancelar</button><button className="button-primary movement-delete-button" disabled={busyId === deactivate.id} onClick={() => void deactivateAlert()}>{busyId === deactivate.id ? 'Desativando…' : 'Desativar alerta'}</button></DialogFooter></Dialog>}
  </section>
}

function AlertForm({ alert, onClose, onSaved }: { alert: StockAlert | null; onClose: () => void; onSaved: (message: string) => void }) {
  const { request, session } = useAuth()
  const [products, setProducts] = useState<Product[]>([])
  const [loadingProducts, setLoadingProducts] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [productId, setProductId] = useState(alert ? String(alert.productId) : '')
  const [type, setType] = useState<AlertType>(alert?.type ?? 'LOW_STOCK')
  const [minimumQuantity, setMinimumQuantity] = useState(String(alert?.minimumQuantity ?? 5))
  const [message, setMessage] = useState(alert?.message ?? '')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true
    request<Page<Product>>('/products/active?page=0&size=100&sort=name,asc').then(async (first) => {
      const pages = await Promise.all(Array.from({ length: Math.max(first.totalPages - 1, 0) }, (_, index) => request<Page<Product>>(`/products/active?page=${index + 1}&size=100&sort=name,asc`)))
      if (active) setProducts([...first.content, ...pages.flatMap((page) => page.content)])
    }).catch((reason: unknown) => { if (active) setLoadError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os produtos.') }).finally(() => { if (active) setLoadingProducts(false) })
    return () => { active = false }
  }, [request, session?.companyId])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError('')
    const payload: AlertRequest = { productId: Number(productId), type, minimumQuantity: Number(minimumQuantity), message: message.trim() }
    setSaving(true)
    try {
      await request<StockAlert>(alert ? `/alerts/${alert.id}` : '/alerts', { method: alert ? 'PUT' : 'POST', body: jsonBody(payload) })
      onSaved(alert ? 'Alerta atualizado.' : 'Alerta criado.')
    } catch (reason) { setError(reason instanceof ApiError ? reason.message : 'Não foi possível salvar o alerta.') }
    finally { setSaving(false) }
  }

  return <Dialog labelledBy="alert-form-title" className="movement-form-dialog" onClose={onClose} closeOnEscape={!saving} closeOnBackdrop={!saving}><DialogHeader><div><span className="eyebrow">ALERTAS DE ESTOQUE</span><h2 id="alert-form-title">{alert ? 'Editar alerta' : 'Novo alerta'}</h2><p>Defina quando a equipe deve receber este aviso.</p></div><button className="icon-button" aria-label="Fechar formulário" onClick={onClose}>×</button></DialogHeader><form className="product-form" onSubmit={(event) => void submit(event)}>{error && <div className="form-alert" role="alert">{error}</div>}{loadError && <div className="form-alert" role="alert">{loadError}</div>}{loadingProducts && <div className="product-form-message" role="status">Carregando produtos…</div>}<div className="product-form-grid"><label className="product-form-wide">Produto<select required value={productId} onChange={(event) => setProductId(event.target.value)} disabled={loadingProducts || (!products.length && !alert)}><option value="">Selecione um produto</option>{alert && !products.some((product) => product.id === alert.productId) && <option value={alert.productId}>{alert.productName} · produto inativo</option>}{products.map((product) => <option value={product.id} key={product.id}>{product.name} · {product.quantity} un. em estoque</option>)}</select></label><label>Tipo de alerta<select value={type} onChange={(event) => setType(event.target.value as AlertType)}><option value="LOW_STOCK">Estoque baixo</option><option value="OUT_OF_STOCK">Fora de estoque</option></select></label><label>Quantidade mínima<input type="number" min="1" step="1" required value={minimumQuantity} onChange={(event) => setMinimumQuantity(event.target.value)} /><small className="field-hint">O limite precisa ser maior que zero.</small></label><label className="product-form-wide">Mensagem<textarea required maxLength={255} rows={3} value={message} onChange={(event) => setMessage(event.target.value)} placeholder="Ex.: Repor antes do próximo pedido" /></label></div><DialogFooter><button className="button-secondary" type="button" onClick={onClose} disabled={saving}>Cancelar</button><button className="button-primary" type="submit" disabled={saving || loadingProducts || (!products.length && !alert)}>{saving ? 'Salvando…' : alert ? 'Salvar alterações' : 'Criar alerta'}</button></DialogFooter></form></Dialog>
}
