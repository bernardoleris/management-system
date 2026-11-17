import { useAuth } from '../../auth/AuthContext'
import styles from './AccountPages.module.css'

const roleNames = {
  COMPANY_OWNER: 'Proprietário',
  ADMIN: 'Administrador',
  OPERATOR: 'Operador',
} as const

export function AccountPage() {
  const { session } = useAuth()

  return <div className={styles.contentColumn}>
    <section className={`data-card ${styles.card}`} aria-labelledby="account-profile-title">
      <header className={styles.cardHeading}>
        <h2 id="account-profile-title">Dados da conta</h2>
        <p>Informações usadas para identificar você no Estocaí.</p>
      </header>

      <div className={styles.accountFields}>
        <label className={styles.field} htmlFor="account-login">Login
          <input id="account-login" value={session?.login ?? ''} readOnly autoComplete="username" />
          <span>Usado para entrar na sua conta.</span>
        </label>
        <div className={styles.field}>
          <span>Acesso na empresa ativa</span>
          <div className={styles.readOnlyValue}>{session ? roleNames[session.role] : '—'}</div>
          <small>As permissões podem variar entre empresas.</small>
        </div>
      </div>

      <div className={styles.scopeNote}>
        <strong>Seu perfil pessoal</strong>
        <p>Esta conta pode acessar diferentes empresas. Nome, e-mail e telefone ainda não podem ser editados por esta tela.</p>
      </div>
    </section>

    <aside className={styles.infoCard}>
      <strong>Empresa ativa</strong>
      <p>{session?.companyName ?? 'Nenhuma empresa selecionada'} · os dados da empresa são gerenciados em Configurações da empresa.</p>
    </aside>
  </div>
}
