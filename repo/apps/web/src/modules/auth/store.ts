import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/user'
import type { SignInPayload, SignUpPayload } from './types'
import { authApi } from './api'
import { setToken, removeToken, getToken, storageGet, storageSet, storageRemove } from '@/utils/storage'
import { normalizeError } from '@/utils/error-normalizer'

const USER_STORAGE_KEY = 'campusfit_user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)

  const isAuthenticated = computed(() => token.value !== null && user.value !== null)

  const api = authApi()

  async function login(payload: SignInPayload) {
    const response = await api.signIn(payload)
    const loginData = response.data
    user.value = loginData.user
    token.value = loginData.token
    setToken(loginData.token)
    storageSet(USER_STORAGE_KEY, loginData.user)
  }

  async function register(payload: SignUpPayload) {
    await api.signUp(payload)
  }

  async function logout() {
    try {
      await api.signOut()
    } catch {
      // Ignore sign-out errors
    } finally {
      user.value = null
      token.value = null
      removeToken()
      storageRemove(USER_STORAGE_KEY)
    }
  }

  async function fetchCurrentUser() {
    try {
      const response = await api.getMe()
      user.value = response.data
      storageSet(USER_STORAGE_KEY, response.data)
    } catch (e) {
      const error = normalizeError(e)
      if (error.status === 401) {
        await logout()
      }
    }
  }

  function initFromStorage() {
    const savedToken = getToken()
    const savedUser = storageGet<User>(USER_STORAGE_KEY)
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = savedUser
    }
  }

  return {
    user,
    token,
    isAuthenticated,
    login,
    register,
    logout,
    fetchCurrentUser,
    initFromStorage,
  }
})
