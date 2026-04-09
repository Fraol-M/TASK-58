import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import DashboardPage from '../pages/DashboardPage.vue'
import { useAuthStore } from '@/modules/auth/store'
import { useDashboardStore } from '../store'

vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => mockAdapter,
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    toasts: { value: [] },
    show: vi.fn(),
    dismiss: vi.fn(),
  }),
}))

const mockAdapter = {
  signIn: vi.fn(),
  signUp: vi.fn(),
  signOut: vi.fn(),
  getMe: vi.fn(),
  getDashboard: vi.fn(),
}

function mountPage(roles: string[] = ['REGULAR_USER']) {
  const pinia = createPinia()
  setActivePinia(pinia)

  const authStore = useAuthStore()
  authStore.user = { id: 1, username: 'testuser', email: 'test@campus.edu', roles, status: 'ACTIVE' } as any
  authStore.token = 'abc123'

  const wrapper = mount(DashboardPage, {
    global: {
      plugins: [pinia],
      stubs: {
        UserDashboard: { template: '<div data-testid="user-dashboard">User Dashboard</div>' },
        OpsDashboard: { template: '<div data-testid="ops-dashboard">Ops Dashboard</div>' },
        AdminDashboard: { template: '<div data-testid="admin-dashboard">Admin Dashboard</div>' },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
      },
    },
  })
  return { wrapper, pinia }
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('shows loading state while fetching', async () => {
    mockAdapter.getDashboard.mockReturnValue(new Promise(() => {}))
    const { wrapper } = mountPage()
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
  })

  it('renders UserDashboard for regular role', async () => {
    const mockStats = { goals: 3, checkIns: 10 }
    mockAdapter.getDashboard.mockResolvedValue({ data: mockStats })

    const { wrapper, pinia } = mountPage(['REGULAR_USER'])
    const dashStore = useDashboardStore(pinia)
    dashStore.stats = mockStats as any
    dashStore.loading = false

    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="user-dashboard"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="ops-dashboard"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="admin-dashboard"]').exists()).toBe(false)
  })

  it('renders OpsDashboard for operations role', async () => {
    const mockStats = { receipts: 5 }
    mockAdapter.getDashboard.mockResolvedValue({ data: mockStats })

    const { wrapper, pinia } = mountPage(['OPERATIONS_STAFF'])
    const dashStore = useDashboardStore(pinia)
    dashStore.stats = mockStats as any
    dashStore.loading = false

    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="ops-dashboard"]').exists()).toBe(true)
  })

  it('renders AdminDashboard for admin role', async () => {
    const mockStats = { users: 100 }
    mockAdapter.getDashboard.mockResolvedValue({ data: mockStats })

    const { wrapper, pinia } = mountPage(['ADMIN'])
    const dashStore = useDashboardStore(pinia)
    dashStore.stats = mockStats as any
    dashStore.loading = false

    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="admin-dashboard"]').exists()).toBe(true)
  })

  it('shows error state on fetch failure', async () => {
    mockAdapter.getDashboard.mockRejectedValue(new Error('Network error'))

    const { wrapper, pinia } = mountPage()
    const dashStore = useDashboardStore(pinia)

    await dashStore.fetchStats()
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="error"]').exists()).toBe(true)
  })
})
