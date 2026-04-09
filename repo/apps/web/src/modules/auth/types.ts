import type { User } from '@/types/user'

export interface SignInPayload {
  username: string
  password: string
}

export interface SignUpPayload {
  username: string
  password: string
  confirmPassword: string
  email?: string
}

export interface LoginResponse {
  token: string
  user: User
  expiresAt: string
}
