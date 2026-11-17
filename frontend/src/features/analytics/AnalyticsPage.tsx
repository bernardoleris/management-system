import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from '../../api/http'
import type { AnalyticsChart, AnalyticsPeriod, ProductWithoutSales, TopProductAnalytics } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import { ThemedSelect } from '../../shared/ui/ThemedSelect'
import './analytics.css'

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const integer = new Intl.NumberFormat('pt-BR')
const periods: Array<{ value: AnalyticsPeriod; label: string }> = [
  { value: 'LAST_7_DAYS', label: 'Últimos 7 dias' },
  { value: 'LAST_30_DAYS', label: 'Últimos 30 dias' },
  { value: 'LAST_6_MONTHS', label: 'Últimos 6 meses' },
  { value: 'LAST_12_MONTHS', label: 'Últimos 12 meses' },
  { value: 'CURRENT_MONTH', label: 'Este mês' },
  { value: 'CURRENT_YEAR', label: 'Este ano' },
]

interface AnalyticsData {
  sales: AnalyticsChart
  revenue: AnalyticsChart
  payments: AnalyticsChart
  topProducts: TopProductAnalytics[]
  productsWithoutSales: ProductWithoutSales[]
}

const emptyData: AnalyticsData = {
  sales: { labels: [], data: [] },
  revenue: { labels: [], data: [] },
  payments: { labels: [], data: [] },
  topProducts: [],
  productsWithoutSales: [],
}

function ChartCard({ title, description, chart, kind, loading }: { title: string; description: string; chart: AnalyticsChart; kind: 'sales' | 'revenue'; loading: boolean }) {
  const max = Math.max(...chart.data.map(Number), 0)
  const points = chart.data.length
  const step = Math.max(1, Math.ceil(points / 6))
  const chartWidth = Math.max(points * 30, 300)
  const columnWidth = chartWidth / Math.max(points, 1)
  const value = kind === 'revenue' ? (amount: number) => money.format(amount) : (amount: number) => `${integer.format(amount)} ${amount === 1 ? 'venda' : 'vendas'}`

  return <article className="data-card analytics-chart-card">
    <div className="data-card-header"><div><h2>{title}</h2><p>{description}</p></div><span className={`analytics-legend analytics-legend-${kind}`}><i /> {kind === 'sales' ? 'Vendas concluídas' : 'Faturamento'}</span></div>
    {loading ? <div className="analytics-empty-chart">Carregando gráfico…</div> : points ? <div className="analytics-chart-wrap">
      <div className="analytics-y-labels" aria-hidden="true"><span>{value(max)}</span><span>{value(max / 2)}</span><span>{value(0)}</span></div>
      <svg className="analytics-bar-chart" viewBox={`0 0 ${chartWidth} 190`} preserveAspectRatio="none" role="img" aria-label={`${title} por período`}>
        <line x1="0" y1="14" x2={chartWidth} y2="14" className="analytics-grid-line" />
        <line x1="0" y1="91" x2={chartWidth} y2="91" className="analytics-grid-line" />
        <line x1="0" y1="168" x2={chartWidth} y2="168" className="analytics-grid-line" />
        {chart.data.map((raw, index) => {
          const amount = Number(raw) || 0
          const width = Math.min(16, Math.max(5, columnWidth * 0.56))
          const x = index * columnWidth + (columnWidth - width) / 2
          const height = max > 0 ? amount / max * 145 : 0
          return <g key={`${chart.labels[index]}-${index}`}>
            <title>{chart.labels[index]}: {value(amount)}</title>
            <rect className={`analytics-bar analytics-bar-${kind}`} x={x} y={168 - height} width={width} height={height} rx="4" />
          </g>
        })}
      </svg>
      <div className="analytics-x-labels" aria-hidden="true">{chart.labels.map((label, index) => index % step === 0 || index === points - 1 ? <span key={`${label}-${index}`}>{label}</span> : <span key={`${label}-${index}`} />)}</div>
      <details className="analytics-chart-data"><summary>Ver dados do gráfico</summary><div className="analytics-chart-data-list">{chart.labels.map((label, index) => <span key={`${label}-${index}`}><strong>{label}</strong>{value(Number(chart.data[index]) || 0)}</span>)}</div></details>
    </div> : <div className="analytics-empty-chart">Nenhuma venda concluída neste período.</div>}
  </article>
}

function RankedBars({ title, description, items, kind, loading }: { title: string; description: string; items: Array<{ id: number; label: string; value: number }>; kind: 'product' | 'payment'; loading: boolean }) {
  const max = Math.max(...items.map((item) => item.value), 0)
  return <article className="data-card analytics-ranking-card">
    <div className="data-card-header"><div><h2>{title}</h2><p>{description}</p></div></div>
    {loading ? <div className="analytics-empty-list">Carregando dados…</div> : items.length ? <ol className="analytics-ranking-list">{items.map((item, index) => <li key={item.id}>
      <span className="analytics-rank">{String(index + 1).padStart(2, '0')}</span>
      <div className="analytics-ranking-main"><div className="analytics-ranking-label"><strong title={item.label}>{item.label}</strong><span>{integer.format(item.value)} {kind === 'product' ? 'un.' : item.value === 1 ? 'venda' : 'vendas'}</span></div><div className="analytics-track"><i className={`analytics-track-fill analytics-track-${kind}`} style={{ width: `${max ? item.value / max * 100 : 0}%` }} /></div></div>
    </li>)}</ol> : <div className="analytics-empty-list">Sem dados para este período.</div>}
  </article>
}

export function AnalyticsPage() {
  const { request } = useAuth()
  const [period, setPeriod] = useState<AnalyticsPeriod>('LAST_12_MONTHS')
  const [data, setData] = useState<AnalyticsData>(emptyData)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const requestId = useRef(0)

  const load = useCallback(async () => {
    const currentRequest = ++requestId.current
    setLoading(true)
    setError('')
    const query = `period=${encodeURIComponent(period)}`
    try {
      const [sales, revenue, payments, topProducts, productsWithoutSales] = await Promise.all([
        request<AnalyticsChart>(`/analytics/sales-over-time?${query}`),
        request<AnalyticsChart>(`/analytics/revenue-over-time?${query}`),
        request<AnalyticsChart>(`/analytics/payment-methods?${query}`),
        request<TopProductAnalytics[]>(`/analytics/top-products?${query}&limit=5`),
        request<ProductWithoutSales[]>(`/analytics/products-without-sales?${query}`),
      ])
      if (currentRequest === requestId.current) setData({ sales, revenue, payments, topProducts, productsWithoutSales })
    } catch (reason) {
      if (currentRequest === requestId.current) {
        setData(emptyData)
        setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar as análises da empresa.')
      }
    } finally {
      if (currentRequest === requestId.current) setLoading(false)
    }
  }, [period, request])

  useEffect(() => {
    void load()
    window.addEventListener('estocai:sale-created', load)
    return () => { requestId.current += 1; window.removeEventListener('estocai:sale-created', load) }
  }, [load])

  const completedSales = data.sales.data.reduce((sum, value) => sum + Number(value || 0), 0)
  const revenue = data.revenue.data.reduce((sum, value) => sum + Number(value || 0), 0)
  const averageTicket = completedSales > 0 ? revenue / completedSales : 0

  return <section className="page-section analytics-page">
    <header className="page-heading analytics-heading"><div><span className="eyebrow">DESEMPENHO DO NEGÓCIO</span><h1>Análises</h1><p>Acompanhe vendas, faturamento e produtos da sua empresa.</p></div><div className="analytics-controls"><span className="analytics-period-label">Período</span><ThemedSelect label="Período" value={period} options={periods} onChange={(value) => setPeriod(value as AnalyticsPeriod)} /><button className="button-secondary refresh-button" type="button" onClick={() => void load()} disabled={loading}><span aria-hidden="true">↻</span> Atualizar</button></div></header>
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={() => void load()}>Tentar novamente</button></div>}
    <div className="analytics-kpis" aria-busy={loading}>
      <article className="metric-card"><span className="metric-icon metric-icon-green" aria-hidden="true">↗</span><span className="metric-label">Vendas concluídas</span><strong>{loading ? '—' : integer.format(completedSales)}</strong><small>No período selecionado</small></article>
      <article className="metric-card"><span className="metric-icon metric-icon-orange" aria-hidden="true">R$</span><span className="metric-label">Faturamento</span><strong>{loading ? '—' : money.format(revenue)}</strong><small>Vendas concluídas no período</small></article>
      <article className="metric-card"><span className="metric-icon metric-icon-purple" aria-hidden="true">◉</span><span className="metric-label">Ticket médio</span><strong>{loading ? '—' : money.format(averageTicket)}</strong><small>Faturamento ÷ vendas concluídas</small></article>
      <article className="metric-card"><span className="metric-icon metric-icon-orange" aria-hidden="true">!</span><span className="metric-label">Produtos sem venda</span><strong>{loading ? '—' : integer.format(data.productsWithoutSales.length)}</strong><small>Ativos sem venda no período</small></article>
    </div>
    {loading && <p className="analytics-loading" role="status">Carregando análises…</p>}
    <div className="analytics-chart-grid" aria-busy={loading}>
      <ChartCard title="Vendas ao longo do tempo" description="Quantidade de vendas concluídas" chart={data.sales} kind="sales" loading={loading} />
      <ChartCard title="Faturamento ao longo do tempo" description="Valor total de vendas concluídas" chart={data.revenue} kind="revenue" loading={loading} />
    </div>
    <div className="analytics-ranking-grid" aria-busy={loading}>
      <RankedBars title="Produtos mais vendidos" description="Unidades vendidas no período" kind="product" loading={loading} items={data.topProducts.map((item) => ({ id: item.productId, label: item.productName, value: item.totalSold }))} />
      <RankedBars title="Vendas por forma de pagamento" description="Vendas concluídas por meio de pagamento" kind="payment" loading={loading} items={data.payments.labels.map((label, index) => ({ id: index, label, value: Number(data.payments.data[index]) || 0 })).filter((item) => item.value > 0).sort((first, second) => second.value - first.value)} />
    </div>
    <article className="data-card analytics-unsold-card">
      <div className="data-card-header"><div><h2>Produtos sem venda no período</h2><p>Produtos ativos que não tiveram venda concluída no intervalo selecionado</p></div><span className="analytics-count">{loading ? '—' : `${integer.format(data.productsWithoutSales.length)} produtos`}</span></div>
      {loading ? <div className="table-state" role="status">Carregando produtos…</div> : data.productsWithoutSales.length ? <div className="table-scroll"><table className="data-table analytics-table"><thead><tr><th>Produto</th><th>Estoque atual</th><th>Última venda</th></tr></thead><tbody>{data.productsWithoutSales.map((product) => <tr key={product.productId}><td><strong>{product.productName}</strong></td><td>{integer.format(product.quantity)} un.</td><td>{product.daysSinceLastSale === null ? <span className="analytics-never-sold">Nunca vendido</span> : product.daysSinceLastSale === 0 ? 'Hoje' : `Há ${integer.format(product.daysSinceLastSale)} dias`}</td></tr>)}</tbody></table></div> : <div className="table-state empty-state"><h3>Todos os produtos tiveram venda</h3><p>Nenhum produto ativo ficou sem venda concluída neste período.</p></div>}
    </article>
  </section>
}
