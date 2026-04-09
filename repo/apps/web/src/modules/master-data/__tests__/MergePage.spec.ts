import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import MergePage from '../pages/MergePage.vue'

const mockMergeCandidates = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockEntityType = ref('term')
const mockFetchCandidates = vi.fn()
const mockMerge = vi.fn()

vi.mock('../composables/useMerge', () => ({
  useMerge: () => ({
    mergeCandidates: mockMergeCandidates,
    loading: mockLoading,
    error: mockError,
    entityType: mockEntityType,
    fetchCandidates: mockFetchCandidates,
    merge: mockMerge,
  }),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    toasts: { value: [] },
    show: vi.fn(),
    dismiss: vi.fn(),
  }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(MergePage, {
    global: {
      plugins: [pinia],
      stubs: {
        MergeComparison: {
          template: '<div data-testid="merge-comparison"><span class="source">{{ candidate.sourceItem.name }}</span><span class="target">{{ candidate.targetItem.name }}</span><span v-for="field in candidate.differingFields || []" :key="field" class="diff-field">{{ field }}</span><button data-testid="merge-btn" @click="$emit(\'merge\', candidate.sourceItem.id, candidate.targetItem.id)">Merge</button></div>',
          props: ['candidate'],
          emits: ['merge', 'skip'],
        },
        FormField: { template: '<div class="form-field"><slot /></div>', props: ['label'] },
        FormSelect: { template: '<select data-testid="type-select"><slot /></select>', props: ['modelValue', 'options', 'placeholder'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        EmptyState: { template: '<div data-testid="empty">No duplicates</div>', props: ['title', 'message'] },
      },
    },
  })
  return { wrapper }
}

describe('MergePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockMergeCandidates.value = []
    mockLoading.value = false
    mockError.value = null
    mockEntityType.value = 'term'
  })

  it('renders merge comparison', async () => {
    mockMergeCandidates.value = [
      {
        id: 1,
        sourceItem: { id: 10, name: 'Fall 2025', code: 'F25', type: 'term' },
        targetItem: { id: 11, name: 'Fall 2025 (duplicate)', code: 'F25D', type: 'term' },
        similarity: 0.95,
        differingFields: ['code', 'name'],
      },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="merge-comparison"]').exists()).toBe(true)
    expect(wrapper.find('.source').text()).toBe('Fall 2025')
    expect(wrapper.find('.target').text()).toBe('Fall 2025 (duplicate)')
  })

  it('highlights differing fields', async () => {
    mockMergeCandidates.value = [
      {
        id: 1,
        sourceItem: { id: 10, name: 'Fall 2025', code: 'F25', type: 'term' },
        targetItem: { id: 11, name: 'Fall 2025 Dup', code: 'F25D', type: 'term' },
        similarity: 0.90,
        differingFields: ['code', 'name'],
      },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    const diffFields = wrapper.findAll('.diff-field')
    expect(diffFields.length).toBe(2)
    expect(diffFields[0].text()).toBe('code')
    expect(diffFields[1].text()).toBe('name')
  })

  it('triggers merge operation', async () => {
    mockMergeCandidates.value = [
      {
        id: 1,
        sourceItem: { id: 10, name: 'Fall 2025', code: 'F25', type: 'term' },
        targetItem: { id: 11, name: 'Fall 2025 Dup', code: 'F25D', type: 'term' },
        similarity: 0.95,
        differingFields: [],
      },
    ]
    mockMerge.mockResolvedValue({})

    const { wrapper } = mountPage()
    await flushPromises()

    const mergeBtn = wrapper.find('[data-testid="merge-btn"]')
    await mergeBtn.trigger('click')
    await flushPromises()

    expect(mockMerge).toHaveBeenCalledWith(10, 11)
  })
})
