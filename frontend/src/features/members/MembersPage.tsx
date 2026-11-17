import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../api/http'
import type { CompanyMember, CompanyMemberCreateRequest, CompanyMemberRoleRequest, CompanyRole, ExistingCompanyMemberRequest } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import { Dialog, DialogConfirmationIcon, DialogFooter, DialogHeader } from '../../shared/ui/Dialog'
import { ThemedSelect } from '../../shared/ui/ThemedSelect'
import styles from './MembersPage.module.css'

type MemberStatus = 'all' | 'active' | 'inactive'
type DialogMode = 'create' | 'existing' | null
const roleLabels: Record<CompanyRole, string> = { COMPANY_OWNER: 'Proprietário', ADMIN: 'Administrador', OPERATOR: 'Operador' }

export function MembersPage() {
  const { request, session, updateCurrentRole } = useAuth()
  const [members, setMembers] = useState<CompanyMember[]>([])
  const [status, setStatus] = useState<MemberStatus>('active')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [reload, setReload] = useState(0)
  const [dialogMode, setDialogMode] = useState<DialogMode>(null)
  const [roleMember, setRoleMember] = useState<CompanyMember | null>(null)
  const [statusMember, setStatusMember] = useState<CompanyMember | null>(null)
  const [busy, setBusy] = useState(false)

  const loadMembers = useCallback(async (isActive: () => boolean) => {
    setLoading(true)
    setError('')
    try {
      const result = await request<CompanyMember[]>('/company/members')
      if (isActive()) setMembers(result)
    } catch (reason) {
      if (isActive()) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os membros da empresa.')
    } finally {
      if (isActive()) setLoading(false)
    }
  }, [request])

  useEffect(() => {
    let active = true
    setMembers([])
    void loadMembers(() => active)
    return () => { active = false }
  }, [loadMembers, reload, session?.companyId])

  async function changeRole(role: CompanyRole) {
    if (!roleMember) return
    setBusy(true)
    setError('')
    try {
      const payload: CompanyMemberRoleRequest = { role }
      await request<CompanyMember>(`/company/members/${roleMember.membershipId}/role`, { method: 'PUT', body: jsonBody(payload) })
      setRoleMember(null)
      if (roleMember.login === session?.login && role !== 'COMPANY_OWNER') {
        updateCurrentRole(role)
        return
      }
      setNotice(`Acesso de ${roleMember.firstName} atualizado para ${roleLabels[role]}.`)
      setReload((value) => value + 1)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível atualizar o papel do membro.')
      setRoleMember(null)
    } finally {
      setBusy(false)
    }
  }

  async function updateMembership() {
    if (!statusMember) return
    setBusy(true)
    setError('')
    try {
      if (statusMember.active) {
        await request<void>(`/company/members/${statusMember.membershipId}`, { method: 'DELETE' })
        setNotice(`Acesso de ${statusMember.firstName} removido desta empresa.`)
      } else {
        if (!statusMember.login) throw new Error('Esta conta não possui login para ser reativada por aqui.')
        const payload: ExistingCompanyMemberRequest = { login: statusMember.login, role: statusMember.role }
        await request<CompanyMember>('/company/members/existing', { method: 'POST', body: jsonBody(payload) })
        setNotice(`Acesso de ${statusMember.firstName} restaurado.`)
      }
      setStatusMember(null)
      setReload((value) => value + 1)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : reason instanceof Error ? reason.message : 'Não foi possível atualizar o acesso do membro.')
      setStatusMember(null)
    } finally {
      setBusy(false)
    }
  }

  const visibleMembers = members.filter((member) => status === 'all' || (status === 'active' ? member.active : !member.active))
  const activeOwnerCount = members.filter((member) => member.active && member.role === 'COMPANY_OWNER').length
  const isSoleActiveOwner = (member: CompanyMember) => member.active && member.role === 'COMPANY_OWNER' && activeOwnerCount === 1

  return <section className={styles.page}>
    <header className={`page-heading ${styles.heading}`}>
      <div><span className="eyebrow">EMPRESA</span><h1>Membros e acessos</h1><p>Gerencie quem pode acessar {session?.companyName} e o nível de permissão de cada pessoa.</p></div>
      <div className={styles.headerActions}><button className="button-secondary" type="button" onClick={() => setDialogMode('existing')}>Adicionar conta existente</button><button className={`button-primary ${styles.newButton}`} type="button" onClick={() => setDialogMode('create')}><span aria-hidden="true">＋</span> Criar usuário</button></div>
    </header>

    {notice && <div className={styles.notice} role="status">{notice}<button type="button" onClick={() => setNotice('')} aria-label="Fechar aviso">×</button></div>}
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={() => setReload((value) => value + 1)}>Atualizar lista</button></div>}

    <section className="data-card" aria-labelledby="members-list-title">
      <div className={`data-card-header ${styles.cardHeader}`}><div><h2 id="members-list-title">Pessoas com acesso</h2><p>{loading ? 'Carregando…' : `${visibleMembers.length} ${visibleMembers.length === 1 ? 'membro' : 'membros'}`}</p></div><div className={styles.controls}><label>Situação<ThemedSelect className={styles.filterSelect} label="Situação" value={status} onChange={(value) => setStatus(value as MemberStatus)} options={[{ value: 'active', label: 'Ativos' }, { value: 'inactive', label: 'Inativos' }, { value: 'all', label: 'Todos' }]} /></label><button className="button-secondary" type="button" disabled={loading} onClick={() => setReload((value) => value + 1)}>Atualizar</button></div></div>
      {loading ? <div className={styles.state} role="status">Carregando membros…</div> : error ? <div className={styles.state}><h3>Não foi possível carregar os membros</h3><p>Verifique sua conexão e tente novamente.</p><button className="button-secondary" type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div> : visibleMembers.length ? <>
        <div className={styles.tableWrap}><table className={styles.table}><thead><tr><th>Membro</th><th>Login</th><th>Papel</th><th>Situação</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>
          {visibleMembers.map((member) => <tr key={member.membershipId}><td><div className={styles.identity}><strong>{member.firstName} {member.lastName}</strong><span>{member.email}</span></div></td><td>{member.login || '—'}</td><td>{roleLabels[member.role]}</td><td><span className={`${styles.status} ${member.active ? styles.active : styles.inactive}`}>{member.active ? 'Ativo' : 'Inativo'}</span></td><td><MemberActions member={member} soleOwner={isSoleActiveOwner(member)} onChangeRole={() => setRoleMember(member)} onChangeStatus={() => setStatusMember(member)} /></td></tr>)}
        </tbody></table></div>
        <div className={styles.mobileList}>{visibleMembers.map((member) => <article className={styles.mobileRow} key={member.membershipId}><div className={styles.identity}><strong>{member.firstName} {member.lastName}</strong><span>{member.email}</span><span>Login: {member.login || '—'}</span><span>{roleLabels[member.role]}</span></div><span className={`${styles.status} ${member.active ? styles.active : styles.inactive}`}>{member.active ? 'Ativo' : 'Inativo'}</span><MemberActions member={member} soleOwner={isSoleActiveOwner(member)} onChangeRole={() => setRoleMember(member)} onChangeStatus={() => setStatusMember(member)} /></article>)}</div>
      </> : <div className={styles.state}><span className={styles.emptyIcon} aria-hidden="true">♙</span><h3>{status === 'inactive' ? 'Nenhum membro inativo' : 'Nenhum membro encontrado'}</h3><p>{status === 'inactive' ? 'Membros desativados aparecerão aqui.' : 'Crie um novo usuário ou adicione uma conta existente para compartilhar o acesso à empresa.'}</p>{status !== 'inactive' && <button className="button-secondary" type="button" onClick={() => setDialogMode('create')}>Criar primeiro usuário</button>}</div>}
    </section>

    {dialogMode && <MemberForm mode={dialogMode} onClose={() => setDialogMode(null)} onSaved={(name) => { setDialogMode(null); setNotice(`${name} agora tem acesso à empresa.`); setStatus('active'); setReload((value) => value + 1) }} />}

    {roleMember && <RoleDialog member={roleMember} busy={busy} onClose={() => setRoleMember(null)} onSave={(role) => void changeRole(role)} />}

    {statusMember && <Dialog labelledBy="member-status-title" describedBy="member-status-description" role="alertdialog" variant="confirmation" closeOnBackdrop={!busy} closeOnEscape={!busy} onClose={() => setStatusMember(null)}><DialogConfirmationIcon>{statusMember.active ? '!' : '✓'}</DialogConfirmationIcon><h2 id="member-status-title">{statusMember.active ? 'Remover acesso à empresa?' : 'Reativar acesso à empresa?'}</h2><p id="member-status-description"><strong>{statusMember.firstName} {statusMember.lastName}</strong> {statusMember.active ? 'perderá acesso aos dados desta empresa. A conta continuará existindo.' : 'voltará a acessar os dados desta empresa com o papel de ' + roleLabels[statusMember.role] + '.'}</p><DialogFooter><button className="button-secondary" type="button" disabled={busy} onClick={() => setStatusMember(null)}>Cancelar</button><button className={`button-primary ${statusMember.active ? 'button-danger' : ''}`} type="button" disabled={busy || (!statusMember.active && !statusMember.login)} onClick={() => void updateMembership()}>{busy ? 'Salvando…' : statusMember.active ? 'Remover acesso' : 'Reativar acesso'}</button></DialogFooter></Dialog>}
  </section>
}

function MemberActions({ member, soleOwner, onChangeRole, onChangeStatus }: { member: CompanyMember; soleOwner: boolean; onChangeRole: () => void; onChangeStatus: () => void }) {
  const reason = 'Mantenha outro proprietário ativo antes de alterar ou remover este acesso.'
  return <div className={styles.actionCell}>
    <div className={styles.actions}>
      {member.active && <button className="text-action" type="button" disabled={soleOwner} aria-label={soleOwner ? `Alterar acesso indisponível. ${reason}` : undefined} onClick={onChangeRole}>Alterar acesso</button>}
      <button className={`text-action ${member.active ? 'text-action-danger' : ''}`} type="button" disabled={soleOwner} aria-label={soleOwner ? `Remover acesso indisponível. ${reason}` : undefined} onClick={onChangeStatus}>{member.active ? 'Remover acesso' : 'Reativar'}</button>
    </div>
    {soleOwner && <small className={styles.blockedNote}>{reason}</small>}
  </div>
}

function MemberForm({ mode, onClose, onSaved }: { mode: Exclude<DialogMode, null>; onClose: () => void; onSaved: (name: string) => void }) {
  const { request } = useAuth()
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [login, setLogin] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<CompanyRole>('OPERATOR')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const isNew = mode === 'create'

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSaving(true)
    try {
      if (isNew) {
        const payload: CompanyMemberCreateRequest = { firstName: firstName.trim(), lastName: lastName.trim(), email: email.trim(), phone: phone.trim() || null, login: login.trim(), password, role }
        const member = await request<CompanyMember>('/company/members', { method: 'POST', body: jsonBody(payload) })
        onSaved(`${member.firstName} ${member.lastName}`)
      } else {
        const payload: ExistingCompanyMemberRequest = { login: login.trim(), role }
        const member = await request<CompanyMember>('/company/members/existing', { method: 'POST', body: jsonBody(payload) })
        onSaved(`${member.firstName} ${member.lastName}`)
      }
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível salvar o acesso. Confira os dados e tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  return <Dialog labelledBy="member-form-title" className={styles.formDialog} closeOnBackdrop={!saving} closeOnEscape={!saving} onClose={onClose}>
    <DialogHeader><div><span className="eyebrow">ACESSO À EMPRESA</span><h2 id="member-form-title">{isNew ? 'Criar usuário' : 'Adicionar conta existente'}</h2><p>{isNew ? 'Crie a conta e defina o acesso inicial para esta empresa.' : 'Informe o login de uma conta já existente no sistema.'}</p></div><button className="icon-button" type="button" aria-label="Fechar formulário" disabled={saving} onClick={onClose}>×</button></DialogHeader>
    <form className={styles.form} onSubmit={(event) => void submit(event)}>
      {error && <div className="form-alert" role="alert">{error}</div>}
      {isNew ? <div className={styles.formGrid}><label>Nome<input autoFocus required maxLength={80} value={firstName} onChange={(event) => setFirstName(event.target.value)} /></label><label>Sobrenome<input required maxLength={80} value={lastName} onChange={(event) => setLastName(event.target.value)} /></label><label className={styles.wide}>E-mail<input type="email" required maxLength={50} value={email} onChange={(event) => setEmail(event.target.value)} /></label><label><span>Telefone <span className={styles.optional}>opcional</span></span><input type="tel" maxLength={20} value={phone} onChange={(event) => setPhone(event.target.value)} /></label><label>Login<input required maxLength={50} autoComplete="username" value={login} onChange={(event) => setLogin(event.target.value)} /></label><label className={styles.wide}>Senha inicial<input type="password" required minLength={12} maxLength={72} autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} /><small className={styles.hint}>Use de 12 a 72 caracteres. Compartilhe a senha inicial com a pessoa por um canal seguro.</small></label><RoleField className={styles.wide} role={role} onChange={setRole} /></div> : <div className={styles.formGrid}><label className={styles.wide}>Login da conta<input autoFocus required autoComplete="username" value={login} onChange={(event) => setLogin(event.target.value)} /><small className={styles.hint}>Se a conta não existir, a API retornará um aviso.</small></label><RoleField className={styles.wide} role={role} onChange={setRole} /></div>}
      <DialogFooter><button className="button-secondary" type="button" disabled={saving} onClick={onClose}>Cancelar</button><button className="button-primary" type="submit" disabled={saving}>{saving ? 'Salvando…' : isNew ? 'Criar usuário' : 'Adicionar à empresa'}</button></DialogFooter>
    </form>
  </Dialog>
}

function RoleField({ role, onChange, className }: { role: CompanyRole; onChange: (role: CompanyRole) => void; className?: string }) {
  return <label className={className}>Papel<select value={role} onChange={(event) => onChange(event.target.value as CompanyRole)}><option value="OPERATOR">Operador — rotina de vendas e estoque</option><option value="ADMIN">Administrador — gestão operacional</option><option value="COMPANY_OWNER">Proprietário — controle de acessos</option></select></label>
}

function RoleDialog({ member, busy, onClose, onSave }: { member: CompanyMember; busy: boolean; onClose: () => void; onSave: (role: CompanyRole) => void }) {
  const [role, setRole] = useState<CompanyRole>(member.role)
  return <Dialog labelledBy="member-role-title" className={styles.formDialog} closeOnBackdrop={!busy} closeOnEscape={!busy} onClose={onClose}>
    <DialogHeader><div><span className="eyebrow">PERMISSÕES</span><h2 id="member-role-title">Alterar acesso</h2><p>{member.firstName} {member.lastName} · {member.login || member.email}</p></div><button className="icon-button" type="button" aria-label="Fechar formulário" disabled={busy} onClick={onClose}>×</button></DialogHeader>
    <div className={styles.form}><RoleField role={role} onChange={setRole} /><DialogFooter><button className="button-secondary" type="button" disabled={busy} onClick={onClose}>Cancelar</button><button className="button-primary" type="button" disabled={busy || role === member.role} onClick={() => onSave(role)}>{busy ? 'Salvando…' : 'Salvar acesso'}</button></DialogFooter></div>
  </Dialog>
}
