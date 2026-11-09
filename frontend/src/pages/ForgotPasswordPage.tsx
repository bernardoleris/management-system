import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError, apiRequest, jsonBody } from '../api/http'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [sent, setSent] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await apiRequest<void>('/auth/forgot-password', {
        method: 'POST', body: jsonBody({ email: email.trim() }),
      })
      setSent(true)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível processar a solicitação.')
    } finally { setSubmitting(false) }
  }

  return (
    <main className="password-flow-layout">
      <section className="password-flow-card">
        <Link className="password-brand" to="/login"><span className="brand-mark">e</span> estocaí</Link>
        {!sent ? <>
          <div className="password-flow-heading"><span className="eyebrow">RECUPERAÇÃO DE ACESSO</span><h1>Esqueceu sua senha?</h1><p>Informe o e-mail associado ao seu usuário. Enviaremos um link para criar uma nova senha.</p></div>
          <form className="password-form" onSubmit={(event) => void handleSubmit(event)}>
            <label htmlFor="recovery-email">E-mail</label>
            <input id="recovery-email" type="email" autoComplete="email" maxLength={50} required value={email} onChange={(event) => setEmail(event.target.value)} placeholder="voce@empresa.com.br" />
            {error && <div className="form-alert" role="alert">{error}</div>}
            <button className="login-submit" type="submit" disabled={submitting}>{submitting ? 'Enviando…' : 'Enviar link de recuperação'}</button>
          </form>
        </> : <div className="password-flow-heading" role="status"><span className="password-flow-icon">✉</span><span className="eyebrow">SOLICITAÇÃO RECEBIDA</span><h1>Confira sua caixa de entrada</h1><p>Se houver uma conta ativa associada a esse e-mail, enviaremos um link de recuperação. Confira também a pasta de spam.</p></div>}
        <Link className="password-back-link" to="/login">← Voltar para o login</Link>
      </section>
    </main>
  )
}
