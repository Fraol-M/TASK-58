import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '../store'

vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => mockAdapter,
}))

const mockAdapter = {
  signIn: vi.fn(),
  signUp: vi.fn(),
  signOut: vi.fn(),
  getMe: vi.fn(),
}

describe('useAuthStore', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('initial state is unauthenticated', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('login sets user and token', async () => {
    const mockUser = { id: 1, username: 'testuser', email: 'test@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' }
    mockAdapter.signIn.mockResolvedValue({
      data: {
        user: mockUser,
        token: 'abc123', expiresAt: '2026-12-31',
      },
    })

    const store = useAuthStore()
    await store.login({ username: 'testuser', password: 'password123' })

    expect(store.user).toEqual(mockUser)
    expect(store.token).toBe('abc123')
  })

  it('login persists token to localStorage', async () => {
    mockAdapter.signIn.mockResolvedValue({
      data: {
        user: { id: 1, username: 'testuser', email: 'test@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' },
        token: 'stored-token', expiresAt: '2026-12-31',
      },
    })

    const store = useAuthStore()
    await store.login({ username: 'testuser', password: 'password123' })

    const storedToken = localStorage.getItem('campusfit_token')
    expect(storedToken).not.toBeNull()
    expect(JSON.parse(storedToken!)).toBe('stored-token')
  })

  it('logout clears user, token, and localStorage', async () => {
    mockAdapter.signIn.mockResolvedValue({
      data: {
        user: { id: 1, username: 'testuser', email: 'test@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' },
        token: 'abc123', expiresAt: '2026-12-31',
      },
    })
    mockAdapter.signOut.mockResolvedValue({})

    const store = useAuthStore()
    await store.login({ username: 'testuser', password: 'password123' })
    expect(store.isAuthenticated).toBe(true)

    await store.logout()

    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('campusfit_token')).toBeNull()
    expect(localStorage.getItem('campusfit_user')).toBeNull()
  })

  it('isAuthenticated returns true when token exists', async () => {
    mockAdapter.signIn.mockResolvedValue({
      data: {
        user: { id: 1, username: 'testuser', email: 'test@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' },
        token: 'abc123', expiresAt: '2026-12-31',
      },
    })

    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)

    await store.login({ username: 'testuser', password: 'password123' })
    expect(store.isAuthenticated).toBe(true)
  })

  it('initFromStorage restores token from localStorage', () => {
    const savedUser = { id: 1, username: 'saved', email: 'saved@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' }
    localStorage.setItem('campusfit_token', JSON.stringify('saved-token'))
    localStorage.setItem('campusfit_user', JSON.stringify(savedUser))

    const store = useAuthStore()
    store.initFromStorage()

    expect(store.token).toBe('saved-token')
    expect(store.user).toEqual(savedUser)
    expect(store.isAuthenticated).toBe(true)
  })

  it('fetchCurrentUser updates user state', async () => {
    const updatedUser = { id: 1, username: 'updated', email: 'updated@campus.edu', roles: ['ADMIN'], status: 'ACTIVE' }
    mockAdapter.getMe.mockResolvedValue({ data: updatedUser })

    const store = useAuthStore()
    // Set initial state so the store is usable
    store.token = 'abc123'
    store.user = { id: 1, username: 'old', email: 'old@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' } as any

    await store.fetchCurrentUser()

    expect(store.user).toEqual(updatedUser)
    expect(mockAdapter.getMe).toHaveBeenCalled()
  })
})
