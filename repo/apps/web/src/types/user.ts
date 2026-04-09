export type UserRole = 'REGULAR_USER' | 'OPERATIONS_STAFF' | 'ADMIN'

export type UserStatus = 'ACTIVE' | 'LOCKED' | 'DISABLED' | 'DELETED'

export interface User {
  id: number
  username: string
  email: string
  roles: UserRole[]
  status: UserStatus
}

export interface AuthTokens {
  token: string
  expiresAt: string
}
