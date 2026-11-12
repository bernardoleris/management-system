import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../api/http'
import type { NamedOption } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import { Dialog, DialogConfirmationIcon, DialogFooter, DialogHeader } from '../../shared/ui/Dialog'
import './catalog-data.css'

type ReferenceKind = 'categories' | 'brands'
const kindCopy: Record<ReferenceKind, { singular: string; plural: string; eyebrow: string; description: string }> = {
  categories: { singular: 'categoria', plural: 'categorias', eyebrow: 'ORGANIZAÇÃO DO CATÁLOGO', description: 'Agrupe produtos por tipo para facilitar o cadastro e a consulta.' },
  brands: { singular: 'marca', plural: 'marcas', eyebrow: 'IDENTIFICAÇÃO DE PRODUTOS', description: 'Mantenha as marcas usadas nos produtos da empresa ativa.' },
}

export function CatalogDataPage() {
  const { request, session } = useAuth()
  const [kind, setKind] = useState<ReferenceKind>('categories')
  const [entries, setEntries] = useState<NamedOption[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [reload, setReload] = useState(0)
  const [query, setQuery] = useState('')
  const [formEntry, setFormEntry] = useState<NamedOption | null | undefined>(undefined)
  const [deleteEntry, setDeleteEntry] = useState<NamedOption | null>(null)
  const [savingDelete, setSavingDelete] = useState(false)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    setEntries([])
    request<NamedOption[]>(`/${kind}`).then((result) => { if (active) setEntries(result) })
      .catch((reason: unknown) => { if (active) setError(reason instanceof ApiError ? reason.message : `Não foi possível carregar ${kindCopy[kind].plural}.`) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [kind, reload, request, session?.companyId])

  const filteredEntries = useMemo(() => {
    const normalized = query.trim().toLocaleLowerCase('pt-BR')
    return normalized ? entries.filter((entry) => entry.name.toLocaleLowerCase('pt-BR').includes(normalized)) : entries
  }, [entries, query])

  function changeKind(next: ReferenceKind) {
    setKind(next)
    setQuery('')
    setError('')
    setNotice('')
  }

  async function removeEntry() {
    if (!deleteEntry) return
    setSavingDelete(true)
    setError('')
    try {
      await request<void>(`/${kind}/${deleteEntry.id}`, { method: 'DELETE' })
      setNotice(`${kindCopy[kind].singular[0].toUpperCase()}${kindCopy[kind].singular.slice(1)} removida.`)
      setDeleteEntry(null)
      setReload((value) => value + 1)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : `Não foi possível remover a ${kindCopy[kind].singular}.`)
      setDeleteEntry(null)
    } finally { setSavingDelete(false) }
  }

  return <section className="page-section catalog-data-page">
    <header className="page-heading catalog-data-heading"><div><span className="eyebrow">{kindCopy[kind].eyebrow}</span><h1>Categorias e marcas</h1><p>Cadastre as referências necessárias para organizar os produtos de {session?.companyName}.</p></div><button className="button-primary" type="button" onClick={() => setFormEntry(null)}><span aria-hidden="true">＋</span> Nova {kindCopy[kind].singular}</button></header>
    {notice && <div className="operations-notice" role="status">{notice}<button type="button" onClick={() => setNotice('')} aria-label="Fechar aviso">×</button></div>}
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div>}
    <section className="data-card">
      <div className="data-card-header catalog-data-card-header"><div><h2>Referências do catálogo</h2><p>{loading ? 'Carregando…' : `${entries.length} ${kindCopy[kind].plural}`}</p></div><button className="button-secondary catalog-refresh" type="button" disabled={loading} onClick={() => setReload((value) => value + 1)}><span aria-hidden="true">↻</span> Atualizar</button></div>
      <div className="catalog-data-toolbar"><div className="catalog-data-tabs" role="tablist" aria-label="Tipo de referência"><button role="tab" aria-selected={kind === 'categories'} className={kind === 'categories' ? 'selected' : ''} onClick={() => changeKind('categories')}>Categorias</button><button role="tab" aria-selected={kind === 'brands'} className={kind === 'brands' ? 'selected' : ''} onClick={() => changeKind('brands')}>Marcas</button></div><label className="catalog-search">Buscar nesta lista<input type="search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder={`Buscar ${kindCopy[kind].plural}`} /></label></div>
      {loading ? <div className="operations-state" role="status">Carregando {kindCopy[kind].plural}…</div> : error ? <div className="operations-state"><h3>Não foi possível carregar a lista</h3><p>Verifique sua conexão e tente novamente.</p><button className="button-secondary" type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div> : filteredEntries.length ? <>
        <div className="catalog-data-table-wrap"><table className="catalog-data-table"><thead><tr><th>Nome</th><th>Uso</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>{filteredEntries.map((entry) => <tr key={entry.id}><td><strong>{entry.name}</strong></td><td className="catalog-reference-note">Disponível no cadastro de produtos</td><td><div className="catalog-data-actions"><button className="text-action" type="button" onClick={() => setFormEntry(entry)}>Editar</button><button className="text-action text-action-muted" type="button" onClick={() => setDeleteEntry(entry)}>Excluir</button></div></td></tr>)}</tbody></table></div>
        <div className="catalog-data-mobile-list">{filteredEntries.map((entry) => <article className="catalog-data-mobile-row" key={entry.id}><div><strong>{entry.name}</strong><small>Referência do catálogo de produtos</small></div><div className="catalog-data-actions"><button className="text-action" type="button" onClick={() => setFormEntry(entry)}>Editar</button><button className="text-action text-action-muted" type="button" onClick={() => setDeleteEntry(entry)}>Excluir</button></div></article>)}</div>
        <div className="catalog-data-count">{filteredEntries.length === entries.length ? `${entries.length} ${kindCopy[kind].plural}` : `${filteredEntries.length} de ${entries.length} ${kindCopy[kind].plural}`}</div>
      </> : <div className="operations-state"><span className="operations-empty-icon" aria-hidden="true">◇</span><h3>{query ? `Nenhuma ${kindCopy[kind].singular} encontrada` : `Nenhuma ${kindCopy[kind].singular} cadastrada`}</h3><p>{query ? 'Tente outro nome ou limpe a busca.' : `Cadastre uma ${kindCopy[kind].singular} para liberar o cadastro de produtos.`}</p>{!query && <button className="button-secondary" type="button" onClick={() => setFormEntry(null)}>Cadastrar {kindCopy[kind].singular}</button>}</div>}
    </section>
    {formEntry !== undefined && <ReferenceForm kind={kind} entry={formEntry} onClose={() => setFormEntry(undefined)} onSaved={(saved) => { setFormEntry(undefined); setNotice(`${kindCopy[kind].singular[0].toUpperCase()}${kindCopy[kind].singular.slice(1)} ${saved ? 'atualizada' : 'cadastrada'} com sucesso.`); setReload((value) => value + 1) }} />}
    {deleteEntry && <Dialog labelledBy="reference-delete-title" role="alertdialog" variant="confirmation" closeOnBackdrop={false} onClose={() => setDeleteEntry(null)}><DialogConfirmationIcon>!</DialogConfirmationIcon><h2 id="reference-delete-title">Excluir {kindCopy[kind].singular}?</h2><p><strong>{deleteEntry.name}</strong> será removida permanentemente. Se houver produtos vinculados, o servidor poderá recusar a exclusão.</p><DialogFooter><button className="button-secondary" type="button" disabled={savingDelete} onClick={() => setDeleteEntry(null)}>Cancelar</button><button className="button-primary reference-delete-button" type="button" disabled={savingDelete} onClick={() => void removeEntry()}>{savingDelete ? 'Excluindo…' : `Excluir ${kindCopy[kind].singular}`}</button></DialogFooter></Dialog>}
  </section>
}

function ReferenceForm({ kind, entry, onClose, onSaved }: { kind: ReferenceKind; entry: NamedOption | null; onClose: () => void; onSaved: (updated: boolean) => void }) {
  const { request } = useAuth()
  const [name, setName] = useState(entry?.name ?? '')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const copy = kindCopy[kind]

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSaving(true)
    try {
      await request<NamedOption>(entry ? `/${kind}/${entry.id}` : `/${kind}`, { method: entry ? 'PUT' : 'POST', body: jsonBody({ name: name.trim() }) })
      onSaved(Boolean(entry))
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : `Não foi possível salvar a ${copy.singular}.`)
    } finally { setSaving(false) }
  }

  return <Dialog labelledBy="reference-form-title" className="reference-form-dialog" closeOnBackdrop={!saving} closeOnEscape={!saving} onClose={onClose}><DialogHeader><div><span className="eyebrow">{copy.eyebrow}</span><h2 id="reference-form-title">{entry ? `Editar ${copy.singular}` : `Nova ${copy.singular}`}</h2><p>Use um nome fácil de reconhecer no catálogo.</p></div><button className="icon-button" type="button" aria-label="Fechar formulário" disabled={saving} onClick={onClose}>×</button></DialogHeader><form className="reference-form" onSubmit={(event) => void submit(event)}>{error && <div className="form-alert" role="alert">{error}</div>}<label htmlFor="reference-name">Nome da {copy.singular}<input id="reference-name" autoFocus required maxLength={80} value={name} onChange={(event) => setName(event.target.value)} placeholder={kind === 'categories' ? 'Ex.: Bebidas' : 'Ex.: Serra Alta'} /><small>{name.length}/80 caracteres</small></label><DialogFooter><button className="button-secondary" type="button" disabled={saving} onClick={onClose}>Cancelar</button><button className="button-primary" type="submit" disabled={saving || !name.trim()}>{saving ? 'Salvando…' : entry ? 'Salvar alterações' : `Cadastrar ${copy.singular}`}</button></DialogFooter></form></Dialog>
}
