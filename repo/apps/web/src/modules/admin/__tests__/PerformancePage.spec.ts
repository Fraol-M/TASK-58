import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import PerformancePage from '../pages/PerformancePage.vue'

const mockData = ref<any>(null)
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockExecute = vi.fn()

vi.mock('@/composables/useAsyncState', () => ({
  useAsyncState: () => ({
    data: mockData,
    loading: mockLoading,
    error: mockError,
    execute: mockExecute,
  }),
}))

vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => ({
    getPerformanceMetrics: vi.fn().mockResolvedValue({
      data: [
        { query: 'SELECT * FROM t_user', avgDuration: 120, callCount: 450 },
        { query: 'SELECT * FROM t_goal', avgDuration: 310, callCount: 80 },
      ],
    }),
  }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)
  return mount(PerformancePage, {
    global: {
      plugins: [pinia],
      stubs: {
        AppCard: { template: '<div class="app-card"><slot /><slot name="default" /></div>', props: ['title', 'padding'] },
        AppButton: { template: '<button @click="$emit(\'click\')" :disabled="loading"><slot /></button>', props: ['variant', 'loading'] },
        DataTable: { template: '<table data-testid="data-table"><slot /></table>', props: ['columns', 'data', 'emptyMessage'] },
        LoadingState: { template: '<div data-testid="loading-state">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error-state">{{ message }}</div>', props: ['message'] },
      },
    },
  })
}

describe('PerformancePage', () => {
  beforeEach(() => {
    mockData.value = null
    mockLoading.value = false
    mockError.value = null
    mockExecute.mockClear()
  })

  it('shows loading state while data is loading', async () => {
    mockLoading.value = true
    mockData.value = null

    const wrapper = mountPage()

    expect(wrapper.find('[data-testid="loading-state"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="data-table"]').exists()).toBe(false)
  })

  it('shows error state when load fails', async () => {
    mockLoading.value = false
    mockError.value = new Error('Network error')

    const wrapper = mountPage()

    expect(wrapper.find('[data-testid="error-state"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="error-state"]').text()).toContain('Network error')
  })

  it('renders data table when metrics are loaded', async () => {
    mockData.value = [
      { query: 'SELECT * FROM t_user', avgDuration: 120, callCount: 450 },
      { query: 'SELECT * FROM t_goal', avgDuration: 310, callCount: 80 },
    ]

    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="data-table"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="loading-state"]').exists()).toBe(false)
  })

  it('shows empty state when metrics array is empty', async () => {
    mockData.value = []

    const wrapper = mountPage()

    expect(wrapper.find('.performance-page__empty').exists()).toBe(true)
  })

  it('calls execute on mount', async () => {
    mountPage()
    await flushPromises()

    expect(mockExecute).toHaveBeenCalledTimes(1)
  })

  it('calls execute again when refresh button is clicked', async () => {
    mockData.value = [{ query: 'SELECT 1', avgDuration: 5, callCount: 1 }]
    const wrapper = mountPage()
    await flushPromises()

    mockExecute.mockClear()
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(mockExecute).toHaveBeenCalledTimes(1)
  })

  it('renders page title', () => {
    const wrapper = mountPage()

    expect(wrapper.find('h1').text()).toBe('System Performance')
  })
})
