import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError, apiRequest, jsonBody } from '../api/http'

export function RegisterPage() {
  const [companyName, setCompanyName] = useState('')
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [login, setLogin] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await apiRequest<void>('/auth/register', {
        method: 'POST',
        body: jsonBody({ companyName: companyName.trim(), firstName: firstName.trim(), lastName: lastName.trim(), email: email.trim(), login: login.trim(), password }),
      })
      setSubmitted(true)
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível concluir o cadastro.')
    } finally { setSubmitting(false) }
  }

  return (
    <main className="password-flow-layout">
      <section className="password-flow-card register-flow-card">
        <Link className="password-brand" to="/login"><span className="brand-mark">e</span> estocaí</Link>
        {submitted ? (
          <div className="verification-pending" role="status">
            <span className="password-flow-icon" aria-hidden="true">✉</span>
            <div className="password-flow-heading"><h1>Confira seu e-mail</h1><p>Enviamos um link de confirmação para <strong>{email}</strong>. Confirme o endereço para ativar sua empresa e entrar no Estocaí.</p></div>
            <p className="verification-note">O link vale por 24 horas. Se não encontrar a mensagem, confira também a caixa de spam.</p>
            <Link className="password-primary-link login-submit" to="/login">Voltar para o login</Link>
          </div>
        ) : (
          <>
            <div className="password-flow-heading"><span className="eyebrow">COMECE POR AQUI</span><h1>Crie sua empresa</h1><p>Cadastre sua empresa e a conta de proprietário. Você ativa o acesso confirmando seu e-mail.</p></div>
            <form className="password-form register-form" onSubmit={(event) => void handleSubmit(event)}>
              <label htmlFor="register-company">Nome da empresa</label><input id="register-company" autoComplete="organization" maxLength={150} required value={companyName} onChange={(event) => setCompanyName(event.target.value)} />
              <div className="register-name-fields">
                <div><label htmlFor="register-first-name">Nome</label><input id="register-first-name" autoComplete="given-name" maxLength={80} required value={firstName} onChange={(event) => setFirstName(event.target.value)} /></div>
                <div><label htmlFor="register-last-name">Sobrenome</label><input id="register-last-name" autoComplete="family-name" maxLength={80} required value={lastName} onChange={(event) => setLastName(event.target.value)} /></div>
              </div>
              <label htmlFor="register-email">E-mail</label><input id="register-email" type="email" autoComplete="email" maxLength={50} required value={email} onChange={(event) => setEmail(event.target.value)} />
              <label htmlFor="register-login">Login</label><input id="register-login" autoComplete="username" maxLength={50} required value={login} onChange={(event) => setLogin(event.target.value)} />
              <label htmlFor="register-password">Senha</label><input id="register-password" type="password" autoComplete="new-password" minLength={12} maxLength={72} required value={password} onChange={(event) => setPassword(event.target.value)} />
              <span className="password-hint">Use pelo menos 12 caracteres.</span>
              {error && <div className="form-alert" role="alert">{error}</div>}
              <button className="login-submit" type="submit" disabled={submitting}>{submitting ? 'Criando empresa…' : 'Criar empresa e conta'}</button>
            </form>
            <Link className="password-back-link" to="/login">← Voltar para o login</Link>
          </>
        )}
      </section>
    </main>
  )
}
