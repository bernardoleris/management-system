import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/http'
import { useAuth } from '../../auth/AuthContext'
import styles from './AccountPages.module.css'

export function ChangePasswordPage() {
  const { changePassword } = useAuth()
  const navigate = useNavigate()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    if (newPassword !== confirmPassword) {
      setError('As senhas não coincidem.')
      return
    }
    setSubmitting(true)
    try {
      await changePassword(currentPassword, newPassword)
      navigate('/login', { replace: true, state: { passwordChanged: true } })
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'Não foi possível alterar a senha.')
    } finally { setSubmitting(false) }
  }

  return (
    <div className={styles.contentColumn}>
      <section className={`data-card ${styles.card}`} aria-labelledby="account-security-title">
        <header className={styles.cardHeading}><h2 id="account-security-title">Senha e segurança</h2><p>Use uma senha forte e exclusiva para proteger sua conta.</p></header>
        <form className={`password-form ${styles.passwordForm}`} onSubmit={(event) => void handleSubmit(event)}>
          <label htmlFor="current-password">Senha atual</label>
          <input id="current-password" type="password" autoComplete="current-password" required value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} />
          <label htmlFor="account-new-password">Nova senha</label>
          <input id="account-new-password" type="password" autoComplete="new-password" minLength={12} maxLength={72} required value={newPassword} onChange={(event) => setNewPassword(event.target.value)} />
          <span className="password-hint">Use pelo menos 12 caracteres.</span>
          <label htmlFor="account-confirm-password">Confirme a nova senha</label>
          <input id="account-confirm-password" type="password" autoComplete="new-password" minLength={12} maxLength={72} required value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} />
          {error && <div className="form-alert" role="alert">{error}</div>}
          <div className={styles.passwordInfo}><strong>Você precisará entrar novamente</strong><p>Depois da troca, sua sessão atual será encerrada para proteger sua conta.</p></div>
          <button className={`button-primary password-settings-submit ${styles.passwordSubmit}`} type="submit" disabled={submitting}>{submitting ? 'Salvando…' : 'Alterar senha'}</button>
        </form>
      </section>
    </div>
  )
}
