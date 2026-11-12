import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError, apiRequest, jsonBody } from '../../../api/http'

export function ResetPasswordPage() {
  const [token] = useState(() => new URLSearchParams(window.location.search).get('token') ?? '')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState(() => token ? '' : 'Este link não é válido. Solicite uma nova recuperação de senha.')
  const [reset, setReset] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (token) window.history.replaceState(window.history.state, '', window.location.pathname)
  }, [token])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    if (!token) {
      setError('Este link não é válido. Solicite uma nova recuperação de senha.')
      return
    }
    if (newPassword !== confirmPassword) {
      setError('As senhas não coincidem.')
      return
    }
    setSubmitting(true)
    try {
      await apiRequest<void>('/auth/reset-password', {
        method: 'POST', body: jsonBody({ token, newPassword }),
      })
      setReset(true)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível redefinir a senha.')
    } finally { setSubmitting(false) }
  }

  return (
    <main className="password-flow-layout">
      <section className="password-flow-card">
        <Link className="password-brand" to="/login"><span className="brand-mark">e</span> estocaí</Link>
        {reset ? <div className="password-flow-heading" role="status"><span className="password-flow-icon">✓</span><span className="eyebrow">SENHA ATUALIZADA</span><h1>Pronto para voltar?</h1><p>Sua senha foi redefinida. Entre com a nova senha para continuar.</p><Link className="login-submit password-primary-link" to="/login">Ir para o login</Link></div> : <>
          <div className="password-flow-heading"><span className="eyebrow">NOVA SENHA</span><h1>Crie uma nova senha</h1><p>Escolha uma senha com pelo menos 12 caracteres.</p></div>
          {!token && <div className="form-alert" role="alert">{error}</div>}
          <form className="password-form" onSubmit={(event) => void handleSubmit(event)}>
            <label htmlFor="new-password">Nova senha</label>
            <input id="new-password" type="password" autoComplete="new-password" minLength={12} maxLength={72} required disabled={!token} value={newPassword} onChange={(event) => setNewPassword(event.target.value)} />
            <label htmlFor="confirm-password">Confirme a nova senha</label>
            <input id="confirm-password" type="password" autoComplete="new-password" minLength={12} maxLength={72} required disabled={!token} value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} />
            {error && token && <div className="form-alert" role="alert">{error}</div>}
            <button className="login-submit" type="submit" disabled={!token || submitting}>{submitting ? 'Salvando…' : 'Redefinir senha'}</button>
          </form>
          <Link className="password-back-link" to="/forgot-password">Solicitar outro link</Link>
        </>}
      </section>
    </main>
  )
}