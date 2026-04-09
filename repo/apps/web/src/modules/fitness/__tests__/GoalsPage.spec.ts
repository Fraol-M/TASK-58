import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import GoalsPage from '../pages/GoalsPage.vue'

const mockGoals = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockCreateGoal = vi.fn()
const mockRefresh = vi.fn()

vi.mock('../composables/useGoals', () => ({
  useGoals: () => ({
    goals: mockGoals,
    loading: mockLoading,
    error: mockError,
    createGoal: mockCreateGoal,
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
  useRouter: () => ({
    push: vi.fn(),
  }),
  useRoute: () => ({
    params: {},
    query: {},
  }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(GoalsPage, {
    global: {
      plugins: [pinia],
      stubs: {
        GoalCard: { template: '<div class="goal-card" data-testid="goal-card"><slot /></div>', props: ['goal'] },
        GoalForm: { template: '<div data-testid="goal-form">Goal Form</div>', props: ['loading'], emits: ['submit'] },
        AppButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        AppModal: { template: '<div v-if="open" data-testid="modal"><slot /></div>', props: ['open', 'title'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        EmptyState: { template: '<div data-testid="empty">No goals</div>', props: ['title', 'message', 'actionText'] },
      },
    },
  })
  return { wrapper }
}

describe('GoalsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockGoals.value = []
    mockLoading.value = false
    mockError.value = null
  })

  it('shows loading state', async () => {
    mockLoading.value = true
    const { wrapper } = mountPage()
    expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
  })

  it('renders list of goal cards', async () => {
    mockGoals.value = [
      { id: 1, goalType: 'ENDURANCE', description: 'Run 5k', targetValue: 5, unit: 'km', startDate: '2026-01-01', targetDate: '2026-06-01', progressPercentage: 60, status: 'ACTIVE', milestones: [] },
      { id: 2, goalType: 'WEIGHT_LOSS', description: 'Lose Weight', targetValue: 10, unit: 'kg', startDate: '2026-01-01', targetDate: '2026-08-01', progressPercentage: 30, status: 'ACTIVE', milestones: [] },
    ]
    const { wrapper } = mountPage()
    await flushPromises()
    const cards = wrapper.findAll('[data-testid="goal-card"]')
    expect(cards.length).toBe(2)
  })

  it('shows empty state when no goals', async () => {
    mockGoals.value = []
    const { wrapper } = mountPage()
    await flushPromises()
    expect(wrapper.find('[data-testid="empty"]').exists()).toBe(true)
  })

  it('opens goal creation modal', async () => {
    mockGoals.value = []
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(false)
    const createBtn = wrapper.findAll('button').find(b => b.text().includes('Create Goal'))
    expect(createBtn).toBeDefined()
    await createBtn!.trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(true)
  })

  it('shows progress on goal cards', async () => {
    mockGoals.value = [
      { id: 1, goalType: 'ENDURANCE', description: 'Run 5k', targetValue: 5, unit: 'km', startDate: '2026-01-01', targetDate: '2026-06-01', progressPercentage: 75, status: 'ACTIVE', milestones: [] },
    ]
    const { wrapper } = mountPage()
    await flushPromises()
    expect(wrapper.find('[data-testid="goal-card"]').exists()).toBe(true)
  })
})
