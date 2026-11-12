import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../../api/http'
import type { Page, Sale } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import './overview.css'

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function OverviewPage() {
  const { request } = useAuth()
  const [sales, setSales] = useState<Page<Sale> | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const load = useCallback(() => {
    setLoading(true)
    setError('')
    request<Page<Sale>>('/sales?page=0&size=5&sort=date,desc').then(setSales).catch((reason: unknown) => {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os dados da operação.')
    }).finally(() => setLoading(false))
  }, [request])

  useEffect(() => {
    load()
    window.addEventListener('estocai:sale-created', load)
    return () => window.removeEventListener('estocai:sale-created', load)
  }, [load])

  function openSale() { window.dispatchEvent(new Event('estocai:open-sale')) }

  const completed = sales?.content.filter((sale) => sale.status === 'COMPLETED') ?? []
  const total = completed.reduce((sum, sale) => sum + sale.totalValue, 0)

  return <section className="page-section overview-page">
    <header className="page-heading overview-heading"><div><span className="eyebrow">SEU NEGÓCIO, EM FOCO</span><h1>Visão geral</h1><p>Um resumo recente da operação da sua empresa.</p></div><button className="button-primary" type="button" onClick={openSale}><span aria-hidden="true">＋</span> Nova venda</button></header>
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={load}>Tentar novamente</button></div>}
    <div className="overview-grid">
      <article className="metric-card"><span className="metric-icon metric-icon-green" aria-hidden="true">↗</span><span className="metric-label">Vendas concluídas recentes</span><strong>{loading ? '—' : completed.length}</strong><small>Na página mais recente</small></article>
      <article className="metric-card"><span className="metric-icon metric-icon-orange" aria-hidden="true">R$</span><span className="metric-label">Valor dessas vendas</span><strong>{loading ? '—' : money.format(total)}</strong><small>Soma dos registros carregados</small></article>
      <article className="metric-card metric-guidance"><span className="metric-icon metric-icon-purple" aria-hidden="true">✳</span><div className="metric-guidance-copy"><span className="metric-label">Próximo passo</span><strong>Registre uma venda</strong><small>O estoque acompanha a venda concluída.</small></div></article>
    </div>
    <div className="data-card overview-recent"><div className="data-card-header"><div><h2>Atividade recente</h2><p>Últimos registros de venda</p></div><button className="button-secondary" type="button" onClick={load} disabled={loading}><span aria-hidden="true">↻</span> Atualizar</button></div>
      {loading ? <div className="table-state" role="status">Carregando atividade…</div> : sales?.content.length ? <div className="recent-list">{sales.content.map((sale) => <div className="recent-row" key={sale.id}><span className="recent-icon" aria-hidden="true">↗</span><div className="recent-copy"><strong>Venda #{sale.id} · {sale.status === 'COMPLETED' ? 'Concluída' : sale.status === 'PENDING' ? 'Pendente' : 'Cancelada'}</strong><small>{sale.adminLogin} · {dateTime.format(new Date(sale.date))}</small></div><strong className="recent-value">{money.format(sale.totalValue)}</strong></div>)}</div> : <div className="table-state empty-state"><h3>A operação começa com a primeira venda</h3><p>Use “Nova venda” para registrar e acompanhar a movimentação do estoque.</p><button className="button-primary" type="button" onClick={openSale}>Criar venda</button></div>}
    </div>
  </section>
}
