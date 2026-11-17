import { useState } from 'react'
import { useAuth } from '../../auth/AuthContext'
import { readPreferences, savePreferences as persistPreferences, type AccountPreferences, type TextScale, type ThemePreference } from './preferences'
import styles from './AccountPages.module.css'

const textScales: Array<{ value: TextScale; label: string }> = [
  { value: 'normal', label: 'Padrão' },
  { value: 'large', label: 'Maior' },
  { value: 'larger', label: 'Maior+' },
]

const themes: Array<{ value: ThemePreference; label: string; description: string }> = [
  { value: 'system', label: 'Seguir sistema', description: 'Acompanha o tema do dispositivo.' },
  { value: 'light', label: 'Claro', description: 'Mantém a interface clara.' },
  { value: 'dark', label: 'Escuro', description: 'Usa fundos escuros e texto claro.' },
]

export function AccessibilityPage() {
  const { session } = useAuth()
  const login = session?.login ?? ''
  const [preferences, setPreferences] = useState<AccountPreferences>(() => readPreferences(login))
  const [saveResult, setSaveResult] = useState<'saved' | 'temporary' | null>(null)

  function update<K extends keyof AccountPreferences>(key: K, value: AccountPreferences[K]) {
    setPreferences((current) => ({ ...current, [key]: value }))
    setSaveResult(null)
  }

  function savePreferences() {
    setSaveResult(persistPreferences(login, preferences) ? 'saved' : 'temporary')
  }

  return <div className={styles.contentColumn}>
    <section className={`data-card ${styles.card}`} aria-labelledby="accessibility-title">
      <header className={styles.cardHeading}>
        <h2 id="accessibility-title">Acessibilidade e aparência</h2>
        <p>Ajuste a interface para ficar mais confortável no seu dia a dia.</p>
      </header>

      <fieldset className={styles.textScaleGroup}>
        <legend>Tamanho do texto</legend>
        <p>A alteração é aplicada em todas as telas deste navegador.</p>
        <div className={styles.textScaleOptions}>
          {textScales.map((option) => <button
            key={option.value}
            className={`${styles.textScaleOption}${preferences.textScale === option.value ? ` ${styles.textScaleOptionSelected}` : ''}`}
            type="button"
            aria-pressed={preferences.textScale === option.value}
            onClick={() => update('textScale', option.value)}
          >
            <span className={styles.radioMark} aria-hidden="true" />
            <strong>{option.label}</strong>
            <span className={styles.textSample} data-size={option.value}>Aa</span>
          </button>)}
        </div>
      </fieldset>

      <fieldset className={styles.textScaleGroup}>
        <legend>Aparência</legend>
        <p>Escolha o tema desta interface neste navegador.</p>
        <div className={styles.themeOptions}>
          {themes.map((option) => <button
            key={option.value}
            className={`${styles.themeOption}${preferences.theme === option.value ? ` ${styles.themeOptionSelected}` : ''}`}
            type="button"
            aria-pressed={preferences.theme === option.value}
            onClick={() => update('theme', option.value)}
          >
            <strong>{option.label}</strong>
            <span>{option.description}</span>
          </button>)}
        </div>
      </fieldset>

      <div className={styles.preferenceRows}>
        <label className={styles.preferenceRow}>
          <span><strong>Contraste reforçado</strong><small>Aumenta o contraste de textos, contornos e controles.</small></span>
          <input type="checkbox" role="switch" checked={preferences.highContrast} onChange={(event) => update('highContrast', event.target.checked)} />
        </label>
        <label className={styles.preferenceRow}>
          <span><strong>Reduzir movimento</strong><small>Diminui animações e transições da interface.</small></span>
          <input type="checkbox" role="switch" checked={preferences.reducedMotion} onChange={(event) => update('reducedMotion', event.target.checked)} />
        </label>
      </div>

      <footer className={styles.cardFooter}>
        <p role="status" aria-live="polite">{saveResult === 'saved' ? 'Preferências salvas neste navegador.' : saveResult === 'temporary' ? 'Preferências aplicadas até esta aba ser fechada.' : 'As preferências são pessoais e seguem sua conta neste navegador.'}</p>
        <button className="button-primary" type="button" onClick={savePreferences}>Salvar preferências</button>
      </footer>
    </section>

    <aside className={styles.infoCard}>
      <strong>Preferências pessoais</strong>
      <p>Suas escolhas não alteram a experiência das outras pessoas da empresa.</p>
    </aside>
  </div>
}
