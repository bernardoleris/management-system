import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../api/http'
import type { Page, Sale } from '../api/contracts'
import { useAuth } from '../auth/AuthContext'

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium', timeStyle: 'short' })
const statusLabel = { PENDING: 'Pendente', COMPLETED: 'Concluída', CANCELED: 'Cancelada' }

export function SalesPage() {
  const { request } = useAuth()
  const [page, setPage] = useState<Page<Sale> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [pageNumber, setPageNumber] = useState(0)
  const load = useCallback(() => {
    setLoading(true); setError('')
    request<Page<Sale>>(`/sales?page=${pageNumber}&size=20&sort=date,desc`).then(setPage).catch((reason: unknown) => {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar as vendas.')
    }).finally(() => setLoading(false))
  }, [pageNumber, request])
  useEffect(() => {
    load()
    window.addEventListener('estocai:sale-created', load)
    return () => window.removeEventListener('estocai:sale-created', load)
  }, [load])

  return <section className="page-section"><div className="page-heading"><div><span className="eyebrow">OPERAÇÃO</span><h1>Vendas</h1><p>Acompanhe as vendas registradas pela sua equipe.</p></div><div className="sales-count">{page ? `${page.totalElements} registros` : ' '}</div></div>{error && <div className="form-alert page-alert" role="alert">{error}<button onClick={load}>Tentar novamente</button></div>}<div className="data-card"><div className="data-card-header"><div><h2>Histórico de vendas</h2><p>Registros mais recentes primeiro</p></div><button className="button-secondary refresh-button" onClick={load} disabled={loading}>↻ Atualizar</button></div>{loading ? <div className="table-state">Carregando vendas…</div> : page?.content.length ? <><div className="sales-table-wrap"><table className="sales-table"><thead><tr><th>Venda</th><th>Data</th><th>Cliente</th><th>Itens</th><th>Pagamento</th><th>Status</th><th className="amount-cell">Total</th></tr></thead><tbody>{page.content.map((sale) => <tr key={sale.id}><td className="sale-id">#{sale.id}</td><td>{dateTime.format(new Date(sale.date))}</td><td>{sale.clientDocumentNumber || 'Consumidor não identificado'}</td><td>{sale.items.reduce((sum, item) => sum + item.quantity, 0)}</td><td>{({ CASH: 'Dinheiro', PIX: 'Pix', CREDIT_CARD: 'Crédito', DEBIT_CARD: 'Débito' } as const)[sale.paymentMethod]}</td><td><span className={`status-pill status-${sale.status.toLowerCase()}`}>{statusLabel[sale.status]}</span></td><td className="amount-cell">{money.format(sale.totalValue)}</td></tr>)}</tbody></table></div><div className="table-pagination"><span>Página {page.number + 1} de {Math.max(page.totalPages, 1)}</span><div><button className="button-secondary" disabled={page.first || loading} onClick={() => setPageNumber((n) => Math.max(0, n - 1))}>Anterior</button><button className="button-secondary" disabled={page.last || loading} onClick={() => setPageNumber((n) => n + 1)}>Próxima</button></div></div></> : <div className="table-state empty-state"><span className="empty-icon">↗</span><h3>Nenhuma venda por aqui ainda</h3><p>Quando uma venda for registrada, ela aparecerá neste histórico.</p></div>}</div></section>
}
