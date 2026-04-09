import { describe, it, expect, vi, beforeEach } from 'vitest'
import { authGuard, roleGuard } from '../guards'

vi.mock('@/utils/storage', () => ({
  getToken: vi.fn(),
  setToken: vi.fn(),
  removeToken: vi.fn(),
  storageGet: vi.fn(),
  storageSet: vi.fn(),
  storageRemove: vi.fn(),
}))

vi.mock('@/utils/role-checks', () => ({
  hasRole: (user: any, role: string) => {
    return user && Array.isArray(user.roles) && user.roles.includes(role)
  },
}))

import { getToken } from '@/utils/storage'

describe('guards', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('redirects to sign-in when not authenticated', () => {
    vi.mocked(getToken).mockReturnValue(null)
    expect(authGuard()).toBe(false)
  })

  it('allows when authenticated', () => {
    vi.mocked(getToken).mockReturnValue('valid-token')
    expect(authGuard()).toBe(true)
  })

  it('allows with correct role', () => {
    const user = { id: 1, username: 'admin', roles: ['ADMIN'], status: 'ACTIVE' }
    localStorage.setItem('campusfit_user', JSON.stringify(user))

    expect(roleGuard('ADMIN')).toBe(true)
  })

  it('blocks with wrong role', () => {
    const user = { id: 1, username: 'regular', roles: ['REGULAR_USER'], status: 'ACTIVE' }
    localStorage.setItem('campusfit_user', JSON.stringify(user))

    expect(roleGuard('ADMIN')).toBe(false)
  })
})
