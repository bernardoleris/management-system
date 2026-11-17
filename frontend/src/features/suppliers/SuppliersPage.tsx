import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../api/http'
import type { Page, SupplierOption, SupplierRequest } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import { Dialog, DialogConfirmationIcon, DialogFooter, DialogHeader } from '../../shared/ui/Dialog'
import { ThemedSelect } from '../../shared/ui/ThemedSelect'
import styles from './SuppliersPage.module.css'

const PAGE_SIZE = 20
type SupplierStatus = '' | 'active' | 'disabled'

function formatCnpj(value: string) {
  const digits = value.replace(/\D/g, '').slice(0, 14)
  if (digits.length <= 2) return digits
  if (digits.length <= 5) return `${digits.slice(0, 2)}.${digits.slice(2)}`
  if (digits.length <= 8) return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5)}`
  if (digits.length <= 12) return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8)}`
  return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8, 12)}-${digits.slice(12)}`
}

export function SuppliersPage() {
  const { request, session } = useAuth()
  const [page, setPage] = useState<Page<SupplierOption> | null>(null)
  const [pageNumber, setPageNumber] = useState(0)
  const [status, setStatus] = useState<SupplierStatus>('')
  const [reload, setReload] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [editing, setEditing] = useState<SupplierOption | null | undefined>(undefined)
  const [statusSupplier, setStatusSupplier] = useState<SupplierOption | null>(null)
  const [savingStatus, setSavingStatus] = useState(false)

  const loadSuppliers = useCallback(async (isActive: () => boolean) => {
    setLoading(true)
    setError('')
    const base = status === 'active' ? '/suppliers/active' : status === 'disabled' ? '/suppliers/disabled' : '/suppliers'
    try {
      const result = await request<Page<SupplierOption>>(`${base}?page=${pageNumber}&size=${PAGE_SIZE}&sort=tradeName,asc`)
      if (isActive()) setPage(result)
    } catch (reason) {
      if (isActive()) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os fornecedores.')
    } finally {
      if (isActive()) setLoading(false)
    }
  }, [pageNumber, request, status])

  useEffect(() => {
    let active = true
    setPage(null)
    void loadSuppliers(() => active)
    return () => { active = false }
  }, [loadSuppliers, reload, session?.companyId])

  function changeStatus(next: SupplierStatus) {
    setStatus(next)
    setPageNumber(0)
  }

  async function toggleStatus() {
    if (!statusSupplier) return
    setSavingStatus(true)
    setError('')
    try {
      await request<void>(`/suppliers/${statusSupplier.id}/toggle-active`, { method: 'PATCH' })
      setNotice(`Fornecedor ${statusSupplier.active ? 'desativado' : 'ativado'} com sucesso.`)
      setStatusSupplier(null)
      setReload((value) => value + 1)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível atualizar a situação do fornecedor.')
      setStatusSupplier(null)
    } finally {
      setSavingStatus(false)
    }
  }

  const totalPages = page?.totalPages ?? 0

  return <section className={styles.page}>
    <header className={`page-heading ${styles.heading}`}>
      <div><span className="eyebrow">RELACIONAMENTO</span><h1>Fornecedores</h1><p>Empresas que abastecem o estoque de {session?.companyName}.</p></div>
      <button className={`button-primary ${styles.newButton}`} type="button" onClick={() => setEditing(null)}><span aria-hidden="true">＋</span> Novo fornecedor</button>
    </header>

    {notice && <div className={styles.notice} role="status">{notice}<button type="button" onClick={() => setNotice('')} aria-label="Fechar aviso">×</button></div>}
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div>}

    <section className="data-card" aria-labelledby="suppliers-list-title">
      <div className={`data-card-header ${styles.cardHeader}`}>
        <div><h2 id="suppliers-list-title">Fornecedores da empresa</h2><p>{loading ? 'Carregando…' : `${page?.totalElements ?? 0} ${page?.totalElements === 1 ? 'fornecedor' : 'fornecedores'}`}</p></div>
        <div className={styles.listControls}>
          <label className={styles.statusFilter}>Situação<ThemedSelect className={styles.statusFilterSelect} label="Situação" value={status} onChange={(value) => changeStatus(value as SupplierStatus)} options={[{ value: '', label: 'Todos' }, { value: 'active', label: 'Ativos' }, { value: 'disabled', label: 'Inativos' }]} /></label>
          <button className="button-secondary" type="button" disabled={loading} onClick={() => setReload((value) => value + 1)}>Atualizar</button>
        </div>
      </div>

      {loading ? <div className={styles.state} role="status">Carregando fornecedores…</div> : error ? <div className={styles.state}><h3>Não foi possível carregar a lista</h3><p>Verifique sua conexão e tente novamente.</p><button className="button-secondary" type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div> : page?.content.length ? <>
        <div className={styles.tableWrap}><table className={styles.table}>
          <thead><tr><th>Fornecedor</th><th>CNPJ</th><th>E-mail</th><th>Telefone</th><th>Situação</th><th><span className="sr-only">Ações</span></th></tr></thead>
          <tbody>{page.content.map((supplier) => <tr key={supplier.id}>
            <td><strong>{supplier.tradeName}</strong></td><td>{formatCnpj(supplier.cnpj)}</td><td>{supplier.email}</td><td>{supplier.phone}</td>
            <td><span className={`${styles.status} ${supplier.active ? styles.active : styles.inactive}`}>{supplier.active ? 'Ativo' : 'Inativo'}</span></td>
            <td><div className={styles.actions}><button className="text-action" type="button" onClick={() => setEditing(supplier)}>Editar</button><button className={`text-action ${supplier.active ? 'text-action-danger' : ''}`} type="button" onClick={() => setStatusSupplier(supplier)}>{supplier.active ? 'Desativar' : 'Ativar'}</button></div></td>
          </tr>)}</tbody>
        </table></div>
        <div className={styles.mobileList}>{page.content.map((supplier) => <article className={styles.mobileRow} key={supplier.id}>
          <div className={styles.mobileIdentity}><strong>{supplier.tradeName}</strong><span>CNPJ {formatCnpj(supplier.cnpj)}</span><a href={`mailto:${supplier.email}`}>{supplier.email}</a><a href={`tel:${supplier.phone.replace(/[^\d+]/g, '')}`}>{supplier.phone}</a></div>
          <span className={`${styles.status} ${supplier.active ? styles.active : styles.inactive}`}>{supplier.active ? 'Ativo' : 'Inativo'}</span>
          <div className={styles.actions}><button className="text-action" type="button" onClick={() => setEditing(supplier)}>Editar</button><button className={`text-action ${supplier.active ? 'text-action-danger' : ''}`} type="button" onClick={() => setStatusSupplier(supplier)}>{supplier.active ? 'Desativar' : 'Ativar'}</button></div>
        </article>)}</div>
        <div className={styles.pagination}><span>{page.totalElements ? `Página ${pageNumber + 1} de ${totalPages}` : 'Nenhum fornecedor'}</span><div><button className="button-secondary" type="button" disabled={loading || pageNumber === 0} onClick={() => setPageNumber((value) => Math.max(0, value - 1))}>Anterior</button><button className="button-secondary" type="button" disabled={loading || page.last || pageNumber + 1 >= totalPages} onClick={() => setPageNumber((value) => value + 1)}>Próxima</button></div></div>
      </> : <div className={styles.state}><span className={styles.emptyIcon} aria-hidden="true">▱</span><h3>{status === 'disabled' ? 'Nenhum fornecedor inativo' : status === 'active' ? 'Nenhum fornecedor ativo' : 'Nenhum fornecedor cadastrado'}</h3><p>{status ? 'Altere o filtro de situação para consultar outros fornecedores.' : 'Cadastre seus fornecedores para associá-los aos produtos e organizar o abastecimento.'}</p>{!status && <button className="button-secondary" type="button" onClick={() => setEditing(null)}>Cadastrar primeiro fornecedor</button>}</div>}
    </section>

    {editing !== undefined && <SupplierForm supplier={editing} onClose={() => setEditing(undefined)} onSaved={(created) => {
      setEditing(undefined)
      setNotice(created ? 'Fornecedor cadastrado com sucesso.' : 'Dados do fornecedor atualizados com sucesso.')
      if (created) { setStatus(''); setPageNumber(0) }
      setReload((value) => value + 1)
    }} />}

    {statusSupplier && <Dialog labelledBy="supplier-status-title" describedBy="supplier-status-description" role="alertdialog" variant="confirmation" closeOnBackdrop={!savingStatus} closeOnEscape={!savingStatus} onClose={() => setStatusSupplier(null)}>
      <DialogConfirmationIcon>{statusSupplier.active ? '!' : '✓'}</DialogConfirmationIcon>
      <h2 id="supplier-status-title">{statusSupplier.active ? 'Desativar fornecedor?' : 'Ativar fornecedor?'}</h2>
      <p id="supplier-status-description"><strong>{statusSupplier.tradeName}</strong> {statusSupplier.active ? 'deixará de aparecer nas opções de fornecedor para novos produtos.' : 'voltará a aparecer nas opções de fornecedor para produtos.'}</p>
      <DialogFooter><button className="button-secondary" type="button" disabled={savingStatus} onClick={() => setStatusSupplier(null)}>Cancelar</button><button className={`button-primary ${statusSupplier.active ? 'button-danger' : ''}`} type="button" disabled={savingStatus} onClick={() => void toggleStatus()}>{savingStatus ? 'Salvando…' : statusSupplier.active ? 'Desativar fornecedor' : 'Ativar fornecedor'}</button></DialogFooter>
    </Dialog>}
  </section>
}

function SupplierForm({ supplier, onClose, onSaved }: { supplier: SupplierOption | null; onClose: () => void; onSaved: (created: boolean) => void }) {
  const { request } = useAuth()
  const [tradeName, setTradeName] = useState(supplier?.tradeName ?? '')
  const [cnpj, setCnpj] = useState(supplier?.cnpj ?? '')
  const [phone, setPhone] = useState(supplier?.phone ?? '')
  const [email, setEmail] = useState(supplier?.email ?? '')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    const digits = cnpj.replace(/\D/g, '')
    if (digits.length !== 14) { setError('O CNPJ deve conter 14 números.'); return }
    const payload: SupplierRequest = { tradeName: tradeName.trim(), cnpj: digits, phone: phone.trim(), email: email.trim() }
    setSaving(true)
    try {
      if (supplier) await request<SupplierOption>(`/suppliers/${supplier.id}`, { method: 'PUT', body: jsonBody(payload) })
      else await request<SupplierOption>('/suppliers', { method: 'POST', body: jsonBody(payload) })
      onSaved(!supplier)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível salvar o fornecedor. Confira os dados e tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  return <Dialog labelledBy="supplier-form-title" className={styles.formDialog} closeOnBackdrop={!saving} closeOnEscape={!saving} onClose={onClose}>
    <DialogHeader><div><span className="eyebrow">RELACIONAMENTO</span><h2 id="supplier-form-title">{supplier ? 'Editar fornecedor' : 'Novo fornecedor'}</h2><p>Dados usados no cadastro de produtos e no abastecimento do estoque.</p></div><button className="icon-button" type="button" aria-label="Fechar formulário" disabled={saving} onClick={onClose}>×</button></DialogHeader>
    <form className={styles.form} onSubmit={(event) => void submit(event)}>
      {error && <div className="form-alert" role="alert">{error}</div>}
      <div className={styles.formGrid}>
        <label className={styles.wide}>Nome fantasia<input autoFocus required maxLength={100} value={tradeName} onChange={(event) => setTradeName(event.target.value)} /></label>
        <label>CNPJ<input inputMode="numeric" autoComplete="off" required maxLength={18} value={formatCnpj(cnpj)} aria-describedby="supplier-cnpj-hint" onChange={(event) => setCnpj(event.target.value.replace(/\D/g, '').slice(0, 14))} /><small id="supplier-cnpj-hint" className={styles.hint}>14 números, sem pontos ou traços.</small></label>
        <label>Telefone<input type="tel" required maxLength={20} value={phone} onChange={(event) => setPhone(event.target.value)} /></label>
        <label className={styles.wide}>E-mail<input type="email" required maxLength={100} value={email} onChange={(event) => setEmail(event.target.value)} /></label>
      </div>
      <DialogFooter><button className="button-secondary" type="button" disabled={saving} onClick={onClose}>Cancelar</button><button className="button-primary" type="submit" disabled={saving}>{saving ? 'Salvando…' : supplier ? 'Salvar alterações' : 'Cadastrar fornecedor'}</button></DialogFooter>
    </form>
  </Dialog>
}
