import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/http'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [loginName, setLoginName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(loginName, password)
      const destination = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname
      navigate(destination || '/', { replace: true })
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível entrar. Tente novamente.')
    } finally { setSubmitting(false) }
  }

  return (
    <main className="login-layout">
      <section className="login-story">
        <div className="login-story-content"><div className="login-brand"><span className="brand-mark">e</span><span>estocaí</span></div><div className="story-copy"><span className="eyebrow">MENOS COMPLEXIDADE. MAIS CONTROLE.</span><h1>Seu estoque em dia. Seu negócio em movimento.</h1><p>Vendas, produtos e alertas em um só lugar — para você decidir com segurança e seguir em frente.</p></div><div className="story-preview" aria-label="Prévia do fluxo de venda e estoque"><div className="preview-window"><div className="preview-window-top"><span /><span /><span /><small>Resumo da operação</small></div><div className="preview-total"><span>Vendas de hoje</span><strong>R$ 2.480,00</strong><small><i /> atualizado agora</small></div><div className="preview-bars"><span style={{ height: '38%' }} /><span style={{ height: '57%' }} /><span style={{ height: '44%' }} /><span style={{ height: '75%' }} /><span style={{ height: '60%' }} /><span style={{ height: '91%' }} /><span style={{ height: '70%' }} /></div></div><div className="preview-sale"><span className="preview-sale-icon">✓</span><div><strong>Venda registrada</strong><small>Estoque atualizado automaticamente</small></div><span className="preview-sale-value">+ R$ 189,90</span></div></div><div className="login-story-foot"><span>CONTROLE QUE ACOMPANHA O SEU RITMO</span><span>© 2026 Estocaí</span></div></div>
      </section>
      <section className="login-panel"><div className="login-form-wrap"><div className="login-heading"><span className="eyebrow">BEM-VINDO DE VOLTA</span><h2>Acesse sua conta</h2><p>Entre para acompanhar sua operação.</p></div><form className="login-form" onSubmit={(event) => void handleSubmit(event)}><label htmlFor="login">Usuário ou e-mail</label><input id="login" name="login" autoComplete="username" value={loginName} onChange={(event) => setLoginName(event.target.value)} required placeholder="Seu usuário ou e-mail" /><label htmlFor="password">Senha</label><input id="password" name="password" type="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} required placeholder="Sua senha" />{error && <div className="form-alert" role="alert">{error}</div>}<button className="login-submit" type="submit" disabled={submitting}>{submitting ? <><span className="spinner" /> Entrando…</> : <>Entrar <span aria-hidden="true">→</span></>}</button></form><div className="login-help"><span className="help-icon">i</span><p>Problemas para acessar? Fale com o administrador da sua empresa.</p></div></div><footer className="login-panel-foot"><span>Seguro para o seu negócio</span><span>Feito para o dia a dia de quem empreende</span></footer></section>
    </main>
  )
}
