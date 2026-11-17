import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../api/http'
import type { Client, ClientAddress, ClientRequest, Page } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import { Dialog, DialogConfirmationIcon, DialogFooter, DialogHeader } from '../../shared/ui/Dialog'
import styles from './ClientsPage.module.css'

const PAGE_SIZE = 20
function formatDocument(type: Client['documentType'], value: string) {
  const digits = value.replace(/\D/g, '')
  if (type === 'CPF' && digits.length === 11) return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
  if (type === 'CNPJ' && digits.length === 14) return digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
  return value
}

function formatPhone(value: string | null) {
  if (!value) return '—'
  return value
}

export function ClientsPage() {
  const { request, session } = useAuth()
  const [pageNumber, setPageNumber] = useState(0)
  const [page, setPage] = useState<Page<Client> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [reload, setReload] = useState(0)
  const [editingClient, setEditingClient] = useState<Client | null | undefined>(undefined)
  const [statusClient, setStatusClient] = useState<Client | null>(null)
  const [savingStatus, setSavingStatus] = useState(false)

  const loadClients = useCallback(async (active: () => boolean) => {
    setLoading(true)
    setError('')
    try {
      const result = await request<Page<Client>>(`/clients?page=${pageNumber}&size=${PAGE_SIZE}&sort=id,desc`)
      if (active()) setPage(result)
    } catch (reason) {
      if (active()) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os clientes.')
    } finally {
      if (active()) setLoading(false)
    }
  }, [pageNumber, request])

  useEffect(() => {
    let active = true
    setPage(null)
    void loadClients(() => active)
    return () => { active = false }
  }, [loadClients, reload, session?.companyId])

  async function toggleStatus() {
    if (!statusClient) return
    setSavingStatus(true)
    setError('')
    try {
      await request<void>(`/clients/${statusClient.id}/toggle-active`, { method: 'PATCH' })
      setNotice(`Cliente ${statusClient.user.active ? 'desativado' : 'ativado'} com sucesso.`)
      setStatusClient(null)
      setReload((value) => value + 1)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível atualizar a situação do cliente.')
      setStatusClient(null)
    } finally {
      setSavingStatus(false)
    }
  }

  const totalPages = page?.totalPages ?? 0

  return <section className={styles.page}>
    <header className={`page-heading ${styles.heading}`}>
      <div>
        <span className="eyebrow">RELACIONAMENTO</span>
        <h1>Clientes</h1>
        <p>Contatos que podem ser associados às vendas de {session?.companyName}.</p>
      </div>
      <button className={`button-primary ${styles.newButton}`} type="button" onClick={() => setEditingClient(null)}>
        <span aria-hidden="true">＋</span> Novo cliente
      </button>
    </header>

    {notice && <div className={styles.notice} role="status">{notice}<button type="button" onClick={() => setNotice('')} aria-label="Fechar aviso">×</button></div>}
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div>}

    <section className="data-card" aria-labelledby="clients-list-title">
      <div className="data-card-header">
        <div><h2 id="clients-list-title">Clientes da empresa</h2><p>{loading ? 'Carregando…' : `${page?.totalElements ?? 0} ${page?.totalElements === 1 ? 'cliente' : 'clientes'}`}</p></div>
        <button className="button-secondary" type="button" disabled={loading} onClick={() => setReload((value) => value + 1)}><span aria-hidden="true">↻</span> Atualizar</button>
      </div>

      {loading ? <div className={styles.state} role="status">Carregando clientes…</div> : error ? <div className={styles.state}><h3>Não foi possível carregar a lista</h3><p>Verifique sua conexão e tente novamente.</p><button className="button-secondary" type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div> : page?.content.length ? <>
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead><tr><th>Cliente</th><th>Documento</th><th>E-mail</th><th>Telefone</th><th>Situação</th><th><span className="sr-only">Ações</span></th></tr></thead>
            <tbody>{page.content.map((client) => <tr key={client.id}>
              <td><strong>{client.user.firstName} {client.user.lastName}</strong></td>
              <td>{client.documentType} · {formatDocument(client.documentType, client.documentNumber)}</td>
              <td>{client.user.email}</td>
              <td>{formatPhone(client.user.phone)}</td>
              <td><span className={`${styles.status} ${client.user.active ? styles.active : styles.inactive}`}>{client.user.active ? 'Ativo' : 'Inativo'}</span></td>
              <td><div className={styles.actions}><button className="text-action" type="button" onClick={() => setEditingClient(client)}>Editar</button><button className={`text-action ${client.user.active ? 'text-action-danger' : ''}`} type="button" onClick={() => setStatusClient(client)}>{client.user.active ? 'Desativar' : 'Ativar'}</button></div></td>
            </tr>)}</tbody>
          </table>
        </div>
        <div className={styles.mobileList}>{page.content.map((client) => <article className={styles.mobileRow} key={client.id}>
          <div className={styles.mobileIdentity}><strong>{client.user.firstName} {client.user.lastName}</strong><span>{client.documentType} · {formatDocument(client.documentType, client.documentNumber)}</span><a href={`mailto:${client.user.email}`}>{client.user.email}</a><span>{formatPhone(client.user.phone)}</span></div>
          <span className={`${styles.status} ${client.user.active ? styles.active : styles.inactive}`}>{client.user.active ? 'Ativo' : 'Inativo'}</span>
          <div className={styles.actions}><button className="text-action" type="button" onClick={() => setEditingClient(client)}>Editar</button><button className={`text-action ${client.user.active ? 'text-action-danger' : ''}`} type="button" onClick={() => setStatusClient(client)}>{client.user.active ? 'Desativar' : 'Ativar'}</button></div>
        </article>)}</div>
        <div className={styles.pagination}>
          <span>{page?.totalElements ? `Página ${pageNumber + 1} de ${totalPages}` : 'Nenhum cliente'}</span>
          <div><button className="button-secondary" type="button" disabled={loading || pageNumber === 0} onClick={() => setPageNumber((value) => Math.max(0, value - 1))}>Anterior</button><button className="button-secondary" type="button" disabled={loading || !page || page.last || pageNumber + 1 >= totalPages} onClick={() => setPageNumber((value) => value + 1)}>Próxima</button></div>
        </div>
      </> : <div className={styles.state}><span className={styles.emptyIcon} aria-hidden="true">♙</span><h3>Nenhum cliente cadastrado</h3><p>Cadastre seus clientes para associá-los às vendas e manter os contatos organizados.</p><button className="button-secondary" type="button" onClick={() => setEditingClient(null)}>Cadastrar primeiro cliente</button></div>}
    </section>

    {editingClient !== undefined && <ClientForm client={editingClient} onClose={() => setEditingClient(undefined)} onSaved={(created) => {
      setEditingClient(undefined)
      setNotice(created ? 'Cliente cadastrado com sucesso.' : 'Dados do cliente atualizados com sucesso.')
      if (created) setPageNumber(0)
      setReload((value) => value + 1)
    }} />}

    {statusClient && <Dialog labelledBy="client-status-title" describedBy="client-status-description" role="alertdialog" variant="confirmation" closeOnBackdrop={!savingStatus} closeOnEscape={!savingStatus} onClose={() => setStatusClient(null)}>
      <DialogConfirmationIcon>{statusClient.user.active ? '!' : '✓'}</DialogConfirmationIcon>
      <h2 id="client-status-title">{statusClient.user.active ? 'Desativar cliente?' : 'Ativar cliente?'}</h2>
      <p id="client-status-description"><strong>{statusClient.user.firstName} {statusClient.user.lastName}</strong> {statusClient.user.active ? 'não poderá ser selecionado em novas vendas.' : 'voltará a ficar disponível para novas vendas.'}</p>
      <DialogFooter><button className="button-secondary" type="button" disabled={savingStatus} onClick={() => setStatusClient(null)}>Cancelar</button><button className={`button-primary ${statusClient.user.active ? 'button-danger' : ''}`} type="button" disabled={savingStatus} onClick={() => void toggleStatus()}>{savingStatus ? 'Salvando…' : statusClient.user.active ? 'Desativar cliente' : 'Ativar cliente'}</button></DialogFooter>
    </Dialog>}
  </section>
}

function ClientForm({ client, onClose, onSaved }: { client: Client | null; onClose: () => void; onSaved: (created: boolean) => void }) {
  const { request } = useAuth()
  const [firstName, setFirstName] = useState(client?.user.firstName ?? '')
  const [lastName, setLastName] = useState(client?.user.lastName ?? '')
  const [email, setEmail] = useState(client?.user.email ?? '')
  const [phone, setPhone] = useState(client?.user.phone ?? '')
  const [documentType, setDocumentType] = useState<Client['documentType']>(client?.documentType ?? 'CPF')
  const [documentNumber, setDocumentNumber] = useState(client?.documentNumber ?? '')
  const [birthDate, setBirthDate] = useState(client?.birthDate ?? '')
  const [addressEnabled, setAddressEnabled] = useState(Boolean(client?.address))
  const [street, setStreet] = useState(client?.address?.street ?? '')
  const [number, setNumber] = useState(client?.address?.number ?? '')
  const [complement, setComplement] = useState(client?.address?.complement ?? '')
  const [neighborhood, setNeighborhood] = useState(client?.address?.neighborhood ?? '')
  const [city, setCity] = useState(client?.address?.city ?? '')
  const [state, setState] = useState(client?.address?.state ?? '')
  const [zipCode, setZipCode] = useState(client?.address?.zipCode ?? '')
  const [error, setError] = useState('')
  const [documentError, setDocumentError] = useState('')
  const [saving, setSaving] = useState(false)

  function setDocumentTypeAndClear(next: Client['documentType']) {
    setDocumentType(next)
    setDocumentNumber('')
    setDocumentError('')
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    const digits = documentNumber.replace(/\D/g, '')
    const requiredDigits = documentType === 'CPF' ? 11 : 14
    if (digits.length !== requiredDigits) {
      setDocumentError(`${documentType} deve conter ${requiredDigits} números.`)
      return
    }
    const addressFields = [street, number, neighborhood, city, state, zipCode]
    if (addressEnabled && addressFields.some((value) => !value.trim())) {
      setError('Preencha todos os campos obrigatórios do endereço ou desmarque a opção de endereço.')
      return
    }
    if (addressEnabled && (state.trim().length !== 2 || zipCode.replace(/\D/g, '').length !== 8)) {
      setError('Informe a UF com 2 letras e o CEP com 8 números.')
      return
    }

    const address: Omit<ClientAddress, 'id'> | undefined = addressEnabled ? {
      street: street.trim(), number: number.trim(), complement: complement.trim() || null,
      neighborhood: neighborhood.trim(), city: city.trim(), state: state.trim().toUpperCase(), zipCode: zipCode.replace(/\D/g, ''),
    } : undefined
    const payload: ClientRequest = {
      firstName: firstName.trim(), lastName: lastName.trim(), email: email.trim(), phone: phone.trim() || null,
      documentType, documentNumber: digits, birthDate: birthDate || null,
      ...(address && !client ? { address } : {}),
    }

    setSaving(true)
    try {
      if (client) {
        await request<Client>(`/clients/${client.id}`, { method: 'PUT', body: jsonBody(payload) })
        if (address) {
          const method = client.address ? 'PUT' : 'POST'
          await request<ClientAddress>(`/clients/${client.id}/address`, { method, body: jsonBody(address) })
        } else if (client.address) {
          await request<void>(`/clients/${client.id}/address`, { method: 'DELETE' })
        }
      } else {
        await request<Client>('/clients', { method: 'POST', body: jsonBody(payload) })
      }
      onSaved(Boolean(client))
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível salvar o cliente. Confira os dados e tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  return <Dialog labelledBy="client-form-title" className={styles.formDialog} closeOnBackdrop={!saving} closeOnEscape={!saving} onClose={onClose}>
    <DialogHeader><div><span className="eyebrow">RELACIONAMENTO</span><h2 id="client-form-title">{client ? 'Editar cliente' : 'Novo cliente'}</h2><p>Os dados são usados para identificar o cliente nas vendas.</p></div><button className="icon-button" type="button" aria-label="Fechar formulário" disabled={saving} onClick={onClose}>×</button></DialogHeader>
    <form className={styles.form} onSubmit={(event) => void submit(event)}>
      {error && <div className="form-alert" role="alert">{error}</div>}
      <div className={styles.formGrid}>
        <label>Nome<input autoFocus required maxLength={80} value={firstName} onChange={(event) => setFirstName(event.target.value)} /></label>
        <label>Sobrenome<input required maxLength={80} value={lastName} onChange={(event) => setLastName(event.target.value)} /></label>
        <label className={styles.spanTwo}>E-mail<input type="email" required maxLength={50} value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>Telefone <span className={styles.optional}>opcional</span><input type="tel" maxLength={20} value={phone} onChange={(event) => setPhone(event.target.value)} /></label>
        <label>Data de nascimento <span className={styles.optional}>opcional</span><input type="date" max={new Date().toISOString().slice(0, 10)} value={birthDate} onChange={(event) => setBirthDate(event.target.value)} /></label>
        <label>Tipo de documento<select value={documentType} onChange={(event) => setDocumentTypeAndClear(event.target.value as Client['documentType'])}><option value="CPF">CPF</option><option value="CNPJ">CNPJ</option></select></label>
        <label className={styles.documentField}>Número do documento<input id="client-document-number" inputMode="numeric" autoComplete="off" required maxLength={documentType === 'CPF' ? 14 : 18} value={formatDocument(documentType, documentNumber)} aria-invalid={Boolean(documentError)} aria-describedby={documentError ? 'client-document-number-error' : undefined} onChange={(event) => { setDocumentNumber(event.target.value.replace(/\D/g, '').slice(0, documentType === 'CPF' ? 11 : 14)); setDocumentError('') }} />{documentError && <span id="client-document-number-error" className={styles.fieldError} role="alert">{documentError}</span>}</label>
      </div>
      <label className={styles.addressToggle}><input type="checkbox" checked={addressEnabled} onChange={(event) => setAddressEnabled(event.target.checked)} /> Incluir endereço <span className={styles.optional}>opcional</span></label>
      {addressEnabled && <fieldset className={styles.addressFields}><legend>Endereço</legend>
        <label className={styles.spanTwo}>Rua<input required maxLength={100} value={street} onChange={(event) => setStreet(event.target.value)} /></label>
        <label>Número<input required maxLength={10} value={number} onChange={(event) => setNumber(event.target.value)} /></label>
        <label>Complemento <span className={styles.optional}>opcional</span><input maxLength={50} value={complement} onChange={(event) => setComplement(event.target.value)} /></label>
        <label>Bairro<input required maxLength={100} value={neighborhood} onChange={(event) => setNeighborhood(event.target.value)} /></label>
        <label>Cidade<input required maxLength={100} value={city} onChange={(event) => setCity(event.target.value)} /></label>
        <label>UF<input required minLength={2} maxLength={2} value={state} onChange={(event) => setState(event.target.value.replace(/[^a-z]/gi, '').toUpperCase())} /></label>
        <label>CEP<input required inputMode="numeric" maxLength={9} value={zipCode} onChange={(event) => setZipCode(event.target.value.replace(/\D/g, '').slice(0, 8))} /></label>
      </fieldset>}
      <DialogFooter><button className="button-secondary" type="button" disabled={saving} onClick={onClose}>Cancelar</button><button className="button-primary" type="submit" disabled={saving}>{saving ? 'Salvando…' : client ? 'Salvar alterações' : 'Cadastrar cliente'}</button></DialogFooter>
    </form>
  </Dialog>
}
