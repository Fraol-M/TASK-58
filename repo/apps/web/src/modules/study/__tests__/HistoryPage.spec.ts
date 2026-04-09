import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import HistoryPage from '../pages/HistoryPage.vue'

const mockCompletions = ref<any[]>([])
const mockPlans = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockSelectedPlanId = ref<number | null>(null)
const mockDateFrom = ref('')
const mockDateTo = ref('')
const mockCompletionDays = ref<any[]>([])

vi.mock('../composables/useHistory', () => ({
  useHistory: () => ({
    completions: mockCompletions,
    plans: mockPlans,
    loading: mockLoading,
    error: mockError,
    selectedPlanId: mockSelectedPlanId,
    dateFrom: mockDateFrom,
    dateTo: mockDateTo,
    completionDays: mockCompletionDays,
  }),
}))

vi.mock('@/utils/format-date', () => ({
  formatDate: (date: string) => date,
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(HistoryPage, {
    global: {
      plugins: [pinia],
      stubs: {
        CompletionCalendar: { template: '<div data-testid="calendar">Calendar</div>', props: ['days'] },
        AppCard: { template: '<div class="app-card"><slot /></div>', props: ['title', 'padding'] },
        FormField: { template: '<div class="form-field"><slot /></div>', props: ['label'] },
        FormSelect: { template: '<select data-testid="select"><option v-for="o in options" :key="o.value" :value="o.value">{{ o.label }}</option></select>', props: ['modelValue', 'options', 'placeholder'] },
        FormDatePicker: { template: '<input type="date" data-testid="date-picker" />', props: ['modelValue', 'min'] },
        DataTable: { template: '<table data-testid="data-table"><tr v-for="(row, i) in data" :key="i"><td v-for="col in columns" :key="col.key">{{ row[col.key] }}</td></tr></table>', props: ['columns', 'data'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        EmptyState: { template: '<div data-testid="empty">Empty</div>', props: ['title', 'message'] },
      },
    },
  })
  return { wrapper }
}

describe('HistoryPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCompletions.value = []
    mockPlans.value = []
    mockLoading.value = false
    mockError.value = null
    mockSelectedPlanId.value = null
    mockDateFrom.value = ''
    mockDateTo.value = ''
    mockCompletionDays.value = []
  })

  it('renders completion calendar', async () => {
    mockCompletionDays.value = [
      { date: '2026-04-01', count: 3 },
      { date: '2026-04-02', count: 1 },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="calendar"]').exists()).toBe(true)
  })

  it('shows completions table', async () => {
    mockPlans.value = [{ id: 1, title: 'Math 101' }]
    mockCompletions.value = [
      { id: 1, planId: 1, completedDate: '2026-04-01', completed: true, notes: 'Good session', createdAt: '2026-04-01T10:00:00Z' },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="data-table"]').exists()).toBe(true)
  })

  it('filters by date range', async () => {
    mockCompletions.value = []
    const { wrapper } = mountPage()
    await flushPromises()

    const datePickers = wrapper.findAll('[data-testid="date-picker"]')
    expect(datePickers.length).toBe(2) // From and To
  })
})
