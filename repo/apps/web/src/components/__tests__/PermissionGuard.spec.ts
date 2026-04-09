import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PermissionGuard from '../feedback/PermissionGuard.vue'
import { useAuthStore } from '@/modules/auth/store'

vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => ({
    signIn: vi.fn(),
    signUp: vi.fn(),
    signOut: vi.fn(),
    getMe: vi.fn(),
  }),
}))

function mountGuard(requiredRole: string | string[], userRoles: string[] = []) {
  const pinia = createPinia()
  setActivePinia(pinia)

  const authStore = useAuthStore(pinia)
  if (userRoles.length > 0) {
    authStore.user = { id: 1, username: 'test', email: 'test@campus.edu', roles: userRoles, status: 'ACTIVE' } as any
    authStore.token = 'abc123'
  }

  const wrapper = mount(PermissionGuard, {
    props: { requiredRole: requiredRole as any },
    slots: {
      default: '<div data-testid="protected-content">Protected Content</div>',
    },
    global: {
      plugins: [pinia],
    },
  })
  return { wrapper, authStore }
}

describe('PermissionGuard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders children with correct role', async () => {
    const { wrapper } = mountGuard('ADMIN', ['ADMIN'])
    await flushPromises()

    expect(wrapper.find('[data-testid="protected-content"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Protected Content')
    expect(wrapper.text()).not.toContain('Access Denied')
  })

  it('shows access denied without role', async () => {
    const { wrapper } = mountGuard('ADMIN', ['REGULAR_USER'])
    await flushPromises()

    expect(wrapper.find('[data-testid="protected-content"]').exists()).toBe(false)
    expect(wrapper.text()).toContain('Access Denied')
  })

  it('works with array of roles', async () => {
    const { wrapper } = mountGuard(['ADMIN', 'OPERATIONS_STAFF'], ['OPERATIONS_STAFF'])
    await flushPromises()

    expect(wrapper.find('[data-testid="protected-content"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Protected Content')
  })
})
