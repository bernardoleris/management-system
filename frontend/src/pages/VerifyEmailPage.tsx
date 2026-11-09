import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ApiError, apiRequest, jsonBody } from '../api/http'

type VerificationState = 'loading' | 'verified' | 'invalid'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const [state, setState] = useState<VerificationState>('loading')
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [feedback, setFeedback] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const verificationStarted = useRef(false)
  const token = searchParams.get('token')

  useEffect(() => {
    if (verificationStarted.current) return
    verificationStarted.current = true
    window.history.replaceState({}, document.title, `${window.location.pathname}${window.location.hash}`)
    if (!token) {
      setState('invalid')
      setMessage('O link não contém um código de confirmação. Peça um novo e-mail para continuar.')
      return
    }
    void apiRequest<void>('/auth/verify-email', { method: 'POST', body: jsonBody({ token }) })
      .then(() => setState('verified'))
      .catch((reason: unknown) => {
        setState('invalid')
        setMessage(reason instanceof ApiError ? reason.message : 'Não foi possível confirmar este e-mail.')
      })
  }, [token])

  async function resend(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setFeedback('')
    try {
      await apiRequest<void>('/auth/resend-verification', { method: 'POST', body: jsonBody({ email: email.trim() }) })
      setFeedback('Se houver um cadastro pendente para este e-mail, enviaremos um novo link.')
    } catch (reason) {
      setFeedback(reason instanceof ApiError ? reason.message : 'Não foi possível solicitar outro link.')
    } finally { setSubmitting(false) }
  }

  return (
    <main className="password-flow-layout">
      <section className="password-flow-card verification-flow-card">
        <Link className="password-brand" to="/login"><span className="brand-mark">e</span> estocaí</Link>
        {state === 'loading' ? <div className="password-flow-heading"><h1>Confirmando seu e-mail…</h1><p>Estamos ativando sua empresa.</p></div> : null}
        {state === 'verified' ? <div className="verification-pending" role="status"><span className="password-flow-icon" aria-hidden="true">✓</span><div className="password-flow-heading"><h1>E-mail confirmado</h1><p>Sua empresa está ativa. Entre com seu login ou e-mail e a senha que você criou.</p></div><Link className="password-primary-link login-submit" to="/login">Ir para o login</Link></div> : null}
        {state === 'invalid' ? <>
          <div className="password-flow-heading"><span className="eyebrow">CONFIRMAÇÃO DE E-MAIL</span><h1>Não foi possível confirmar</h1><p>{message}</p></div>
          <form className="password-form" onSubmit={(event) => void resend(event)}>
            <label htmlFor="verification-email">E-mail usado no cadastro</label>
            <input id="verification-email" type="email" autoComplete="email" maxLength={50} required value={email} onChange={(event) => setEmail(event.target.value)} />
            {feedback && <div className="verification-feedback" role="status">{feedback}</div>}
            <button className="login-submit" type="submit" disabled={submitting}>{submitting ? 'Enviando…' : 'Enviar novo link'}</button>
          </form>
          <Link className="password-back-link" to="/login">Voltar para o login</Link>
        </> : null}
      </section>
    </main>
  )
}
