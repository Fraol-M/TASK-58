import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import DiscrepanciesPage from '../pages/DiscrepanciesPage.vue'

const mockDiscrepancies = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockFilterResolved = ref('all')
const mockFilterSupervisor = ref(false)

// Auth store user — mutated per-test to control role
const mockUser = ref<any>(null)

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    toasts: { value: [] },
    show: vi.fn(),
    dismiss: vi.fn(),
  }),
}))

vi.mock('../api', () => ({
  operationsApi: () => ({
    supervisorReview: vi.fn().mockResolvedValue({}),
  }),
}))

vi.mock('../composables/useDiscrepancies', () => ({
  useDiscrepancies: () => ({
    discrepancies: mockDiscrepancies,
    loading: mockLoading,
    error: mockError,
    filterResolved: mockFilterResolved,
    filterSupervisor: mockFilterSupervisor,
  }),
}))

vi.mock('@/modules/auth/store', () => ({
  useAuthStore: () => ({
    get user() { return mockUser.value },
  }),
}))

const UNRESOLVED_DISC = {
  id: 1, receiptId: 1, lineId: 1, discrepancyType: 'QUANTITY',
  supervisorRequired: false, resolved: false, expectedValue: 100, actualValue: 95, variancePercent: 5,
}

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(DiscrepanciesPage, {
    global: {
      plugins: [pinia],
      stubs: {
        DiscrepancyCard: { template: '<div class="discrepancy-card" data-testid="discrepancy-card">{{ discrepancy.discrepancyType }} <span v-if="discrepancy.supervisorRequired" data-testid="supervisor-badge">Supervisor</span></div>', props: ['discrepancy'] },
        DiscrepancyResolution: { template: '<div data-testid="resolution-form">Resolution Form</div>', props: ['supervisorRequired'], emits: ['resolve'] },
        AppCard: { template: '<div class="app-card"><slot /></div>' },
        AppButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        EmptyState: { template: '<div data-testid="empty">No discrepancies</div>', props: ['title', 'message'] },
      },
    },
  })
  return { wrapper }
}

describe('DiscrepanciesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockDiscrepancies.value = []
    mockLoading.value = false
    mockError.value = null
    mockFilterResolved.value = 'all'
    mockFilterSupervisor.value = false
    mockUser.value = null
  })

  it('renders discrepancy cards', async () => {
    mockUser.value = { roles: ['ADMIN'] }
    mockDiscrepancies.value = [
      { id: 1, receiptId: 1, lineId: 1, discrepancyType: 'QUANTITY', supervisorRequired: false, resolved: false, expectedValue: 100, actualValue: 95, variancePercent: 5 },
      { id: 2, receiptId: 1, lineId: 2, discrepancyType: 'QUALITY', supervisorRequired: true, resolved: false, expectedValue: 50, actualValue: 45, variancePercent: 10 },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    const cards = wrapper.findAll('[data-testid="discrepancy-card"]')
    expect(cards.length).toBe(2)
  })

  it('shows supervisor badge when required', async () => {
    mockUser.value = { roles: ['ADMIN'] }
    mockDiscrepancies.value = [
      { id: 1, receiptId: 1, lineId: 2, discrepancyType: 'QUALITY', supervisorRequired: true, resolved: false, expectedValue: 50, actualValue: 45, variancePercent: 10 },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="supervisor-badge"]').exists()).toBe(true)
  })

  it('admin sees Resolve button on unresolved discrepancies', async () => {
    mockUser.value = { roles: ['ADMIN'] }
    mockDiscrepancies.value = [UNRESOLVED_DISC]

    const { wrapper } = mountPage()
    await flushPromises()

    const resolveBtn = wrapper.findAll('button').find(b => b.text() === 'Resolve')
    expect(resolveBtn).toBeDefined()
  })

  it('non-admin does not see Resolve button', async () => {
    mockUser.value = { roles: ['OPERATIONS_STAFF'] }
    mockDiscrepancies.value = [UNRESOLVED_DISC]

    const { wrapper } = mountPage()
    await flushPromises()

    const resolveBtn = wrapper.findAll('button').find(b => b.text() === 'Resolve')
    expect(resolveBtn).toBeUndefined()
  })

  it('unauthenticated user does not see Resolve button', async () => {
    mockUser.value = null
    mockDiscrepancies.value = [UNRESOLVED_DISC]

    const { wrapper } = mountPage()
    await flushPromises()

    const resolveBtn = wrapper.findAll('button').find(b => b.text() === 'Resolve')
    expect(resolveBtn).toBeUndefined()
  })

  it('opens resolution form when admin clicks Resolve', async () => {
    mockUser.value = { roles: ['ADMIN'] }
    mockDiscrepancies.value = [UNRESOLVED_DISC]

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="resolution-form"]').exists()).toBe(false)

    const resolveBtn = wrapper.findAll('button').find(b => b.text() === 'Resolve')
    expect(resolveBtn).toBeDefined()
    await resolveBtn!.trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-testid="resolution-form"]').exists()).toBe(true)
  })

  it('resolved discrepancies never show Resolve button', async () => {
    mockUser.value = { roles: ['ADMIN'] }
    mockDiscrepancies.value = [{ ...UNRESOLVED_DISC, resolved: true }]

    const { wrapper } = mountPage()
    await flushPromises()

    const resolveBtn = wrapper.findAll('button').find(b => b.text() === 'Resolve')
    expect(resolveBtn).toBeUndefined()
  })
})
