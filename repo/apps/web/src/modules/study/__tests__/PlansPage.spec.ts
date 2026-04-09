import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import PlansPage from '../pages/PlansPage.vue'

const mockPlans = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockCreatePlan = vi.fn()
const mockRefresh = vi.fn()

vi.mock('../composables/usePlans', () => ({
  usePlans: () => ({
    plans: mockPlans,
    loading: mockLoading,
    error: mockError,
    createPlan: mockCreatePlan,
    refresh: mockRefresh,
  }),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    toasts: { value: [] },
    show: vi.fn(),
    dismiss: vi.fn(),
  }),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ params: {}, query: {} }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(PlansPage, {
    global: {
      plugins: [pinia],
      stubs: {
        PlanCard: { template: '<div class="plan-card" data-testid="plan-card">{{ plan.title }}</div>', props: ['plan'], emits: ['click'] },
        PlanForm: { name: 'PlanForm', template: '<div data-testid="plan-form">Plan Form</div>', props: ['loading'], emits: ['submit'] },
        StreakIndicator: { template: '<div data-testid="streak">Streak</div>', props: ['currentStreak', 'longestStreak'] },
        AppButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        AppModal: { template: '<div v-if="open" data-testid="modal"><slot /></div>', props: ['open', 'title', 'size'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        EmptyState: { template: '<div data-testid="empty">No plans</div>', props: ['title', 'message', 'actionText'] },
      },
    },
  })
  return { wrapper }
}

describe('PlansPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPlans.value = []
    mockLoading.value = false
    mockError.value = null
  })

  it('renders plan cards', async () => {
    mockPlans.value = [
      { id: 1, title: 'Math 101', description: 'MWF 9am', status: 'ACTIVE' },
      { id: 2, title: 'Physics 201', description: 'TTh 2pm', status: 'ACTIVE' },
    ]
    const { wrapper } = mountPage()
    await flushPromises()

    const cards = wrapper.findAll('[data-testid="plan-card"]')
    expect(cards.length).toBe(2)
  })

  it('shows empty state when no plans', async () => {
    mockPlans.value = []
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="empty"]').exists()).toBe(true)
  })

  it('opens plan creation form', async () => {
    mockPlans.value = []
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(false)
    const createBtn = wrapper.findAll('button').find(b => b.text().includes('Create Plan'))
    expect(createBtn).toBeDefined()
    await createBtn!.trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(true)
  })

  it('creates plan on submit', async () => {
    mockPlans.value = []
    mockCreatePlan.mockResolvedValue({})

    const { wrapper } = mountPage()
    await flushPromises()

    // Open modal first
    const createBtn = wrapper.findAll('button').find(b => b.text().includes('Create Plan'))
    await createBtn!.trigger('click')
    await flushPromises()

    const form = wrapper.findComponent({ name: 'PlanForm' })
    expect(form.exists()).toBe(true)

    const planData = { title: 'CS 301', description: 'MWF 10am' }
    form.vm.$emit('submit', planData)
    await flushPromises()

    expect(mockCreatePlan).toHaveBeenCalledWith(planData)
  })
})
