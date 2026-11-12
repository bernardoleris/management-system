import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../../api/http'
import type { Page, Sale } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import styles from './SalesPage.module.css'

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium', timeStyle: 'short' })
const statusLabel = { PENDING: 'Pendente', COMPLETED: 'Concluída', CANCELED: 'Cancelada' }
const paymentLabel = { CASH: 'Dinheiro', PIX: 'Pix', CREDIT_CARD: 'Crédito', DEBIT_CARD: 'Débito' }

export function SalesPage() {
  const { request } = useAuth()
  const [page, setPage] = useState<Page<Sale> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [pageNumber, setPageNumber] = useState(0)
  const load = useCallback(() => {
    setLoading(true)
    setError('')
    request<Page<Sale>>(`/sales?page=${pageNumber}&size=20&sort=date,desc`).then(setPage).catch((reason: unknown) => {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar as vendas.')
    }).finally(() => setLoading(false))
  }, [pageNumber, request])

  useEffect(() => {
    load()
    window.addEventListener('estocai:sale-created', load)
    return () => window.removeEventListener('estocai:sale-created', load)
  }, [load])

  function openSale() { window.dispatchEvent(new Event('estocai:open-sale')) }

  return <section className={`page-section sales-page ${styles.featureScope}`}>
    <header className="page-heading sales-heading"><div><span className="eyebrow">OPERAÇÃO</span><h1>Vendas</h1><p>Acompanhe as vendas registradas pela sua equipe.</p></div><div className="sales-heading-actions"><div className="sales-count">{page ? `${page.totalElements} registros` : ''}</div><button className="button-primary" type="button" onClick={openSale}><span aria-hidden="true">＋</span> Nova venda</button></div></header>
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={load}>Tentar novamente</button></div>}
    <div className="data-card"><div className="data-card-header"><div><h2>Histórico de vendas</h2><p>Registros mais recentes primeiro</p></div><button className="button-secondary refresh-button" type="button" onClick={load} disabled={loading}><span aria-hidden="true">↻</span> Atualizar</button></div>
      {loading ? <div className="table-state" role="status">Carregando vendas…</div> : page?.content.length ? <>
        <div className="sales-table-wrap"><table className="sales-table"><thead><tr><th>Venda</th><th>Data</th><th>Cliente</th><th>Itens</th><th>Pagamento</th><th>Status</th><th className="amount-cell">Total</th></tr></thead><tbody>{page.content.map((sale) => <tr key={sale.id}><td className="sale-id">#{sale.id}</td><td>{dateTime.format(new Date(sale.date))}</td><td>{sale.clientDocumentNumber || 'Consumidor não identificado'}</td><td>{sale.items.reduce((sum, item) => sum + item.quantity, 0)} un.</td><td>{paymentLabel[sale.paymentMethod]}</td><td><span className={`status-pill status-${sale.status.toLowerCase()}`}>{statusLabel[sale.status]}</span></td><td className="amount-cell">{money.format(sale.totalValue)}</td></tr>)}</tbody></table></div>
        <div className="table-pagination"><span>Página {page.number + 1} de {Math.max(page.totalPages, 1)}</span><div><button className="button-secondary" type="button" disabled={page.first || loading} onClick={() => setPageNumber((number) => Math.max(0, number - 1))}>Anterior</button><button className="button-secondary" type="button" disabled={page.last || loading} onClick={() => setPageNumber((number) => number + 1)}>Próxima</button></div></div>
      </> : <div className="table-state empty-state"><span className="empty-icon" aria-hidden="true">↗</span><h3>Nenhuma venda por aqui ainda</h3><p>Registre a primeira venda para acompanhar a atividade da sua equipe.</p><button className="button-primary" type="button" onClick={openSale}>Criar venda</button></div>}
    </div>
  </section>
}
