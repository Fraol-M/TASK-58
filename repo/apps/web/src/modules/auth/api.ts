import { getAdapter } from '@/services/adapters/adapter-factory'
import type { SignInPayload, SignUpPayload } from './types'

export function authApi() {
  const adapter = getAdapter()

  return {
    signIn(payload: SignInPayload) {
      return adapter.signIn({ username: payload.username, password: payload.password })
    },

    signUp(payload: SignUpPayload) {
      return adapter.signUp({
        username: payload.username,
        password: payload.password,
        email: payload.email,
      })
    },

    signOut() {
      return adapter.signOut()
    },

    getMe() {
      return adapter.getMe()
    },
  }
}
