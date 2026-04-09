export function required(value: unknown): string | null {
  if (value === null || value === undefined || value === '') {
    return 'This field is required'
  }
  if (typeof value === 'string' && value.trim().length === 0) {
    return 'This field is required'
  }
  return null
}

export function minLength(min: number) {
  return (value: unknown): string | null => {
    if (!value || typeof value !== 'string') return null
    if (value.length < min) {
      return `Must be at least ${min} characters`
    }
    return null
  }
}

export function maxLength(max: number) {
  return (value: unknown): string | null => {
    if (!value || typeof value !== 'string') return null
    if (value.length > max) {
      return `Must be at most ${max} characters`
    }
    return null
  }
}

export function email(value: unknown): string | null {
  if (!value || typeof value !== 'string') return null
  const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!pattern.test(value)) {
    return 'Please enter a valid email address'
  }
  return null
}

export function passwordStrength(value: unknown): string | null {
  if (!value || typeof value !== 'string') return null
  if (value.length < 8) {
    return 'Password must be at least 8 characters'
  }
  if (!/[a-z]/.test(value)) {
    return 'Password must contain a lowercase letter'
  }
  if (!/[A-Z]/.test(value)) {
    return 'Password must contain an uppercase letter'
  }
  if (!/[0-9]/.test(value)) {
    return 'Password must contain a number'
  }
  return null
}
