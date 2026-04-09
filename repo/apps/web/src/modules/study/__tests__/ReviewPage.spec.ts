import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref, computed } from 'vue'
import ReviewPage from '../pages/ReviewPage.vue'

const mockDuePoints = ref<any[]>([])
const mockCurrentPoint = ref<any>(null)
const mockCurrentIndex = ref(0)
const mockTotalDue = ref(0)
const mockSessionComplete = ref(false)
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockSubmitting = ref(false)
const mockRateQuality = vi.fn()

vi.mock('../composables/useReview', () => ({
  useReview: () => ({
    duePoints: mockDuePoints,
    currentPoint: mockCurrentPoint,
    currentIndex: mockCurrentIndex,
    totalDue: mockTotalDue,
    sessionComplete: mockSessionComplete,
    loading: mockLoading,
    error: mockError,
    submitting: mockSubmitting,
    rateQuality: mockRateQuality,
  }),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ params: {}, query: {} }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(ReviewPage, {
    global: {
      plugins: [pinia],
      stubs: {
        ReviewSession: {
          name: 'ReviewSession',
          template: '<div data-testid="review-session"><button v-for="q in [1,2,3,4,5]" :key="q" :data-quality="q" @click="$emit(\'rate\', q)">{{ q }}</button></div>',
          props: ['topic', 'description', 'currentIndex', 'totalCount', 'submitting'],
          emits: ['rate'],
        },
        AppCard: { template: '<div class="app-card"><slot /></div>' },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
      },
    },
  })
  return { wrapper }
}

describe('ReviewPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockDuePoints.value = []
    mockCurrentPoint.value = null
    mockCurrentIndex.value = 0
    mockTotalDue.value = 0
    mockSessionComplete.value = false
    mockLoading.value = false
    mockError.value = null
    mockSubmitting.value = false
  })

  it('shows due forgetting points', async () => {
    mockDuePoints.value = [
      { id: 1, planId: 1, topic: 'Math', description: 'Quadratic formula', nextReviewDate: '2026-04-01' },
      { id: 2, planId: 1, topic: 'Physics', description: 'Newton second law', nextReviewDate: '2026-04-01' },
    ]
    mockCurrentPoint.value = mockDuePoints.value[0]
    mockTotalDue.value = 2

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="review-session"]').exists()).toBe(true)
  })

  it('renders review session with quality buttons', async () => {
    mockDuePoints.value = [{ id: 1, planId: 1, topic: 'Math', description: 'Quadratic formula', nextReviewDate: '2026-04-01' }]
    mockCurrentPoint.value = mockDuePoints.value[0]
    mockTotalDue.value = 1

    const { wrapper } = mountPage()
    await flushPromises()

    const buttons = wrapper.findAll('[data-quality]')
    expect(buttons.length).toBe(5)
  })

  it('submits review and loads next', async () => {
    mockDuePoints.value = [
      { id: 1, planId: 1, topic: 'Math', description: 'Quadratic formula', nextReviewDate: '2026-04-01' },
      { id: 2, planId: 1, topic: 'Physics', description: 'Newton second law', nextReviewDate: '2026-04-01' },
    ]
    mockCurrentPoint.value = mockDuePoints.value[0]
    mockTotalDue.value = 2
    mockRateQuality.mockResolvedValue({})

    const { wrapper } = mountPage()
    await flushPromises()

    const session = wrapper.findComponent({ name: 'ReviewSession' })
    session.vm.$emit('rate', 4)
    await flushPromises()

    expect(mockRateQuality).toHaveBeenCalledWith(4)
  })

  it('shows "all caught up" when none due', async () => {
    mockDuePoints.value = []
    mockCurrentPoint.value = null
    mockTotalDue.value = 0

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.text()).toContain('All caught up')
  })
})
