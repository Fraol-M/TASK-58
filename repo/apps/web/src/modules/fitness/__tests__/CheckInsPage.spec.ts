import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import CheckInsPage from '../pages/CheckInsPage.vue'

const mockCurrentGoal = ref<any>(null)
const mockCheckIns = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockSubmitting = ref(false)
const mockCreateCheckIn = vi.fn()
const mockRefresh = vi.fn()

vi.mock('../composables/useCheckIns', () => ({
  useCheckIns: () => ({
    currentGoal: mockCurrentGoal,
    checkIns: mockCheckIns,
    loading: mockLoading,
    error: mockError,
    submitting: mockSubmitting,
    createCheckIn: mockCreateCheckIn,
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
  useRoute: () => ({ params: { goalId: '1' }, query: {} }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(CheckInsPage, {
    global: {
      plugins: [pinia],
      stubs: {
        CheckInForm: { template: '<form data-testid="checkin-form"><button type="submit">Submit</button></form>', props: ['unitLabel', 'loading'], emits: ['submit'] },
        CheckInTimeline: { template: '<div data-testid="checkin-timeline">Timeline</div>', props: ['checkIns'] },
        MilestoneTimeline: { template: '<div data-testid="milestones">Milestones</div>', props: ['milestones', 'overallProgress'] },
        AppCard: { template: '<div class="app-card"><slot /></div>', props: ['title', 'padding'] },
        AppBadge: { template: '<span class="badge">{{ label }}</span>', props: ['label', 'variant'] },
        ProgressBar: { template: '<div class="progress-bar"></div>', props: ['value', 'variant'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
      },
    },
  })
  return { wrapper }
}

describe('CheckInsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCurrentGoal.value = null
    mockCheckIns.value = []
    mockLoading.value = false
    mockError.value = null
    mockSubmitting.value = false
  })

  it('loads check-ins for a goal', async () => {
    mockCurrentGoal.value = {
      id: 1,
      goalType: 'ENDURANCE',
      description: 'Run 5k',
      targetValue: 5,
      unit: 'km',
      startDate: '2026-01-01',
      targetDate: '2026-06-01',
      progressPercentage: 40,
      status: 'ACTIVE',
      milestones: [],
    }
    mockCheckIns.value = [
      { id: 1, goalId: 1, userId: 1, weekNumber: 1, value: 2.5, notes: 'Good run', createdAt: '2026-04-01' },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.text()).toContain('Run 5k')
    expect(wrapper.find('[data-testid="checkin-timeline"]').exists()).toBe(true)
  })

  it('renders check-in timeline', async () => {
    mockCurrentGoal.value = {
      id: 1,
      goalType: 'ENDURANCE',
      description: 'Run 5k',
      targetValue: 5,
      unit: 'km',
      startDate: '2026-01-01',
      targetDate: '2026-06-01',
      progressPercentage: 40,
      status: 'ACTIVE',
      milestones: [],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="checkin-timeline"]').exists()).toBe(true)
  })

  it('shows check-in form', async () => {
    mockCurrentGoal.value = {
      id: 1,
      goalType: 'ENDURANCE',
      description: 'Run 5k',
      targetValue: 5,
      unit: 'km',
      startDate: '2026-01-01',
      targetDate: '2026-06-01',
      progressPercentage: 40,
      status: 'ACTIVE',
      milestones: [],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="checkin-form"]').exists()).toBe(true)
  })

  it('submits check-in value', async () => {
    mockCurrentGoal.value = {
      id: 1,
      goalType: 'ENDURANCE',
      description: 'Run 5k',
      targetValue: 5,
      unit: 'km',
      startDate: '2026-01-01',
      targetDate: '2026-06-01',
      progressPercentage: 40,
      status: 'ACTIVE',
      milestones: [],
    }
    mockCreateCheckIn.mockResolvedValue({})

    const { wrapper } = mountPage()
    await flushPromises()

    const form = wrapper.findComponent({ name: 'CheckInForm' })
    expect(form.exists()).toBe(true)
    form.vm.$emit('submit', { value: 3.0, notes: 'Great run' })
    await flushPromises()

    expect(mockCreateCheckIn).toHaveBeenCalledWith(3.0, 'Great run')
  })
})
