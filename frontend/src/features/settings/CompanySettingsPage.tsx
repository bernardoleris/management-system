import { useEffect, useState, type FormEvent } from 'react'
import { ApiError, jsonBody } from '../../api/http'
import type { CompanySettings, CompanySettingsRequest } from '../../api/contracts'
import { useAuth } from '../../auth/AuthContext'
import styles from './CompanySettingsPage.module.css'

const timezones = [
  { value: 'America/Noronha', label: '(UTC−02:00) Fernando de Noronha' },
  { value: 'America/Sao_Paulo', label: '(UTC−03:00) Brasília' },
  { value: 'America/Manaus', label: '(UTC−04:00) Manaus' },
  { value: 'America/Rio_Branco', label: '(UTC−05:00) Rio Branco' },
]

interface SettingsForm {
  legalName: string
  tradeName: string
  documentNumber: string
  email: string
  phone: string
  timezone: string
}

function toForm(company: CompanySettings): SettingsForm {
  return {
    legalName: company.legalName,
    tradeName: company.tradeName,
    documentNumber: formatDocument(company.documentNumber ?? ''),
    email: company.email ?? '',
    phone: company.phone ?? '',
    timezone: company.timezone,
  }
}

function formatDocument(value: string) {
  const digits = value.replace(/\D/g, '').slice(0, 14)
  if (digits.length > 11) return digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{0,2})/, (_match, a, b, c, d, e) => [a, b, c, d].filter(Boolean).join('.') + (e ? `-${e}` : ''))
  return digits.replace(/(\d{3})(\d{0,3})(\d{0,3})(\d{0,2})/, (_match, a, b, c, d) => `${a}${b ? `.${b}` : ''}${c ? `.${c}` : ''}${d ? `-${d}` : ''}`)
}

function isSameForm(first: SettingsForm, second: SettingsForm) {
  return Object.keys(first).every((key) => first[key as keyof SettingsForm] === second[key as keyof SettingsForm])
}

export function CompanySettingsPage() {
  const { request, session, updateCurrentCompanyName } = useAuth()
  const [company, setCompany] = useState<CompanySettings | null>(null)
  const [form, setForm] = useState<SettingsForm | null>(null)
  const [initialForm, setInitialForm] = useState<SettingsForm | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [documentError, setDocumentError] = useState('')
  const [notice, setNotice] = useState('')
  const [reload, setReload] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    setCompany(null)
    setForm(null)
    setInitialForm(null)
    request<CompanySettings>('/company/settings').then((result) => {
      if (!active) return
      const nextForm = toForm(result)
      setCompany(result)
      setForm(nextForm)
      setInitialForm(nextForm)
    }).catch((reason: unknown) => {
      if (active) setError(reason instanceof ApiError ? reason.message : 'Não foi possível carregar os dados da empresa.')
    }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [reload, request, session?.companyId])

  function change<K extends keyof SettingsForm>(key: K, value: SettingsForm[K]) {
    setForm((current) => current ? { ...current, [key]: value } : current)
    setNotice('')
    setError('')
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!form) return
    setError('')
    setNotice('')
    const documentDigits = form.documentNumber.replace(/\D/g, '')
    if (documentDigits && documentDigits.length !== 11 && documentDigits.length !== 14) {
      setDocumentError('Informe um CPF com 11 dígitos ou um CNPJ com 14.')
      return
    }
    setDocumentError('')
    setSaving(true)
    try {
      const payload: CompanySettingsRequest = {
        legalName: form.legalName.trim(),
        tradeName: form.tradeName.trim(),
        documentNumber: documentDigits || null,
        email: form.email.trim() || null,
        phone: form.phone.trim() || null,
        timezone: form.timezone,
      }
      const updated = await request<CompanySettings>('/company/settings', { method: 'PUT', body: jsonBody(payload) })
      const nextForm = toForm(updated)
      setCompany(updated)
      setForm(nextForm)
      setInitialForm(nextForm)
      updateCurrentCompanyName(updated.tradeName)
      setNotice('Configurações da empresa salvas.')
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível salvar as configurações da empresa.')
    } finally {
      setSaving(false)
    }
  }

  const changed = Boolean(form && initialForm && !isSameForm(form, initialForm))

  return <section className={`page-section ${styles.page}`}>
    <header className={`page-heading ${styles.heading}`}>
      <div><span className="eyebrow">DADOS DA EMPRESA</span><h1>Configurações da empresa</h1><p>Gerencie os dados e as preferências da empresa ativa.</p></div>
    </header>

    {notice && <div className={styles.notice} role="status">{notice}</div>}
    {error && <div className="form-alert page-alert" role="alert">{error}<button type="button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></div>}

    {loading ? <div className={styles.loading} role="status">Carregando dados da empresa…</div> : form && company ? <form onSubmit={(event) => void save(event)}>
      <div className={styles.columns}>
        <section className={`data-card ${styles.formCard}`} aria-labelledby="company-data-title">
          <div className={styles.cardHeading}><h2 id="company-data-title">Dados cadastrais</h2><p>Informações que identificam sua empresa nos registros.</p></div>
          <div className={styles.fields}>
            <label className={styles.field}>Nome fantasia<input required maxLength={150} value={form.tradeName} onChange={(event) => change('tradeName', event.target.value)} /></label>
            <label className={styles.field}>Razão social<input required maxLength={150} value={form.legalName} onChange={(event) => change('legalName', event.target.value)} /></label>
            <label className={styles.field}>CPF ou CNPJ<input inputMode="numeric" autoComplete="off" aria-invalid={Boolean(documentError)} aria-describedby={documentError ? 'company-document-error' : undefined} value={form.documentNumber} onChange={(event) => { setDocumentError(''); change('documentNumber', formatDocument(event.target.value)) }} /><small id="company-document-error" className={styles.fieldError} aria-live="polite">{documentError}</small></label>
            <label className={styles.field}>E-mail de contato<input type="email" maxLength={100} autoComplete="email" value={form.email} onChange={(event) => change('email', event.target.value)} /></label>
            <label className={styles.field}>Telefone<input type="tel" maxLength={20} autoComplete="tel" value={form.phone} onChange={(event) => change('phone', event.target.value)} /></label>
          </div>
          <div className={styles.formDivider} />
          <div className={styles.formFooter}><div><h3>Contato da empresa</h3><p>Use um e-mail acompanhado pela equipe responsável pela conta.</p></div><button className="button-primary" type="submit" disabled={saving || !changed}>{saving ? 'Salvando…' : 'Salvar alterações'}</button></div>
        </section>

        <aside className={styles.sideColumn}>
          <section className={`data-card ${styles.regionalCard}`} aria-labelledby="regional-title">
            <div className={styles.cardHeading}><h2 id="regional-title">Preferências regionais</h2><p>Afetam datas e valores exibidos no sistema.</p></div>
            <label className={styles.field}>Moeda<input value={company.currency === 'BRL' ? 'Real brasileiro (BRL)' : company.currency} readOnly /><span className={styles.fieldHint}>A moeda disponível atualmente é BRL.</span></label>
            <label className={styles.field}>Fuso horário<select value={form.timezone} onChange={(event) => change('timezone', event.target.value)}>{!timezones.some((zone) => zone.value === form.timezone) && <option value={form.timezone}>{form.timezone}</option>}{timezones.map((zone) => <option key={zone.value} value={zone.value}>{zone.label}</option>)}</select></label>
          </section>
          <section className={styles.statusCard} aria-label="Escopo das configurações">
            <strong><i aria-hidden="true" />{company.active ? 'Empresa ativa' : 'Empresa inativa'}</strong>
            <p>Você está editando os dados da empresa selecionada no menu lateral. As alterações não afetam outras empresas da sua conta.</p>
            <span>ESCOPO</span><b>Somente esta empresa</b>
          </section>
        </aside>
      </div>
      <div className={styles.scopeNote}><strong>Sobre as alterações</strong><p>As informações atualizadas ficam vinculadas à empresa ativa e são compartilhadas com os membros que têm acesso a ela.</p></div>
    </form> : !error ? <div className={styles.loading}>Não foi possível carregar os dados da empresa.</div> : null}
  </section>
}
