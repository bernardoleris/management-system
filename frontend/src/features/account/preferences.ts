export type TextScale = 'normal' | 'large' | 'larger'
export type ThemePreference = 'system' | 'light' | 'dark'

export interface AccountPreferences {
  textScale: TextScale
  highContrast: boolean
  reducedMotion: boolean
  theme: ThemePreference
}

const DEFAULT_PREFERENCES: AccountPreferences = {
  textScale: 'normal',
  highContrast: false,
  reducedMotion: false,
  theme: 'system',
}
const STORAGE_PREFIX = 'estocai.preferences:v1:'
let systemThemeQuery: MediaQueryList | null = null
let systemThemeListener: (() => void) | null = null

function storageKey(login: string) {
  return `${STORAGE_PREFIX}${encodeURIComponent(login.trim().toLowerCase())}`
}

export function readPreferences(login: string): AccountPreferences {
  try {
    const value = JSON.parse(localStorage.getItem(storageKey(login)) ?? 'null') as Partial<AccountPreferences> | null
    return {
      textScale: value?.textScale === 'large' || value?.textScale === 'larger' ? value.textScale : 'normal',
      highContrast: value?.highContrast === true,
      reducedMotion: value?.reducedMotion === true,
      theme: value?.theme === 'light' || value?.theme === 'dark' ? value.theme : 'system',
    }
  } catch {
    return DEFAULT_PREFERENCES
  }
}

export function savePreferences(login: string, preferences: AccountPreferences): boolean {
  let persisted = true
  try {
    localStorage.setItem(storageKey(login), JSON.stringify(preferences))
  } catch {
    persisted = false
  }
  applyPreferences(preferences)
  return persisted
}

export function applyPreferences(preferences: AccountPreferences) {
  const root = document.documentElement

  removeSystemThemeListener()
  root.dataset.textScale = preferences.textScale
  root.dataset.contrast = preferences.highContrast ? 'high' : 'normal'
  root.dataset.reducedMotion = String(preferences.reducedMotion)

  const applyResolvedTheme = () => {
    if (preferences.theme === 'system') {
      root.dataset.theme = systemThemeQuery?.matches ? 'dark' : 'light'
      return
    }
    root.dataset.theme = preferences.theme
  }

  if (preferences.theme === 'system') {
    systemThemeQuery = window.matchMedia('(prefers-color-scheme: dark)')
    systemThemeListener = applyResolvedTheme
    systemThemeQuery.addEventListener('change', applyResolvedTheme)
  }

  applyResolvedTheme()
}

export function clearAppliedPreferences() {
  removeSystemThemeListener()
  const root = document.documentElement
  delete root.dataset.textScale
  delete root.dataset.contrast
  delete root.dataset.reducedMotion
  delete root.dataset.theme
}

function removeSystemThemeListener() {
  if (systemThemeQuery && systemThemeListener) {
    systemThemeQuery.removeEventListener('change', systemThemeListener)
  }
  systemThemeQuery = null
  systemThemeListener = null
}
