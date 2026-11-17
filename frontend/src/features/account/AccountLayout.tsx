import { NavLink, Outlet } from 'react-router-dom'
import styles from './AccountPages.module.css'

export function AccountLayout() {
  return <section className={`page-section ${styles.page}`}>
    <header className={`page-heading ${styles.heading}`}>
      <div>
        <span className="eyebrow">MINHA CONTA</span>
        <h1>Minha conta</h1>
        <p>Seus dados de acesso e preferências acompanham você ao trocar de empresa.</p>
      </div>
    </header>

    <div className={styles.layout}>
      <nav className={`data-card ${styles.navigation}`} aria-label="Seções da sua conta">
        <h2>Sua conta</h2>
        <NavLink to="/account/profile" className={({ isActive }) => `${styles.navigationLink}${isActive ? ` ${styles.navigationLinkActive}` : ''}`}>
          <strong>Perfil</strong><span>Dados da conta</span>
        </NavLink>
        <NavLink to="/account/security" className={({ isActive }) => `${styles.navigationLink}${isActive ? ` ${styles.navigationLinkActive}` : ''}`}>
          <strong>Segurança</strong><span>Senha e acesso</span>
        </NavLink>
        <NavLink to="/account/accessibility" className={({ isActive }) => `${styles.navigationLink}${isActive ? ` ${styles.navigationLinkActive}` : ''}`}>
          <strong>Acessibilidade</strong><span>Visual e movimento</span>
        </NavLink>
      </nav>
      <Outlet />
    </div>
  </section>
}
