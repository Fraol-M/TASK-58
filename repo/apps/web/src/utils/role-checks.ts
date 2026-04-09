import type { User, UserRole } from '@/types/user'

export function isAdmin(user: User | null): boolean {
  return user !== null && user.roles.includes('ADMIN')
}

export function isOpsStaff(user: User | null): boolean {
  return user !== null && user.roles.includes('OPERATIONS_STAFF')
}

export function isRegularUser(user: User | null): boolean {
  return user !== null && user.roles.includes('REGULAR_USER')
}

export function hasRole(user: User | null, role: UserRole): boolean {
  return user !== null && user.roles.includes(role)
}
