const TOKEN_KEY = 'campusfit_token'

export function storageGet<T>(key: string): T | null {
  try {
    const raw = localStorage.getItem(key)
    if (raw === null) return null
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function storageSet<T>(key: string, value: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch {
    // storage full or unavailable
  }
}

export function storageRemove(key: string): void {
  try {
    localStorage.removeItem(key)
  } catch {
    // storage unavailable
  }
}

export function getToken(): string | null {
  return storageGet<string>(TOKEN_KEY)
}

export function setToken(token: string): void {
  storageSet(TOKEN_KEY, token)
}

export function removeToken(): void {
  storageRemove(TOKEN_KEY)
}
