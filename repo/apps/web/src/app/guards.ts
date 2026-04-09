import type { UserRole } from '@/types/user'
import { getToken } from '@/utils/storage'
import { hasRole } from '@/utils/role-checks'

/**
 * Checks if user is authenticated by verifying a token exists in storage.
 * Returns true if authenticated.
 */
export function authGuard(): boolean {
  const token = getToken()
  return token !== null
}

/**
 * Checks if the authenticated user has the required role.
 * Reads user from localStorage since the store may not be accessible
 * outside of component context during navigation guard.
 */
export function roleGuard(requiredRole: UserRole | UserRole[]): boolean {
  try {
    const raw = localStorage.getItem('campusfit_user')
    if (!raw) return false
    const user = JSON.parse(raw)
    if (!user || !Array.isArray(user.roles)) return false

    if (Array.isArray(requiredRole)) {
      return requiredRole.some((r) => hasRole(user, r))
    }
    return hasRole(user, requiredRole)
  } catch {
    return false
  }
}
