import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import MasterDataPage from '../pages/MasterDataPage.vue'

const mockItems = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockEntityType = ref('term')
const mockSearchQuery = ref('')
const mockSetEntityType = vi.fn()
const mockRefresh = vi.fn()

vi.mock('../composables/useMasterData', () => ({
  useMasterData: () => ({
    items: mockItems,
    loading: mockLoading,
    error: mockError,
    entityType: mockEntityType,
    searchQuery: mockSearchQuery,
    setEntityType: mockSetEntityType,
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

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(MasterDataPage, {
    global: {
      plugins: [pinia],
      stubs: {
        EntityTable: { template: '<div data-testid="entity-table">Table</div>', props: ['items', 'entityType'], emits: ['edit', 'delete'] },
        EntityForm: { template: '<div data-testid="entity-form">Entity Form</div>', props: ['entityType', 'loading'], emits: ['submit'] },
        AppButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        AppModal: { template: '<div v-if="open" data-testid="modal"><slot /></div>', props: ['open', 'title'] },
        FormInput: { template: '<input data-testid="search-input" />', props: ['modelValue', 'placeholder'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
      },
    },
  })
  return { wrapper }
}

describe('MasterDataPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockItems.value = []
    mockLoading.value = false
    mockError.value = null
    mockEntityType.value = 'term'
    mockSearchQuery.value = ''
  })

  it('renders tabs for entity types', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    const tabs = wrapper.findAll('.tab-btn')
    expect(tabs.length).toBe(5)
    expect(tabs[0].text()).toBe('Terms')
    expect(tabs[1].text()).toBe('Schools')
    expect(tabs[2].text()).toBe('Majors')
    expect(tabs[3].text()).toBe('Classes')
    expect(tabs[4].text()).toBe('Courses')
  })

  it('shows entity table', async () => {
    mockItems.value = [
      { id: 1, code: 'T001', name: 'Fall 2026', type: 'term', active: true },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="entity-table"]').exists()).toBe(true)
  })

  it('switches tab', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    const schoolsTab = wrapper.findAll('.tab-btn')[1]
    await schoolsTab.trigger('click')
    await flushPromises()

    expect(mockSetEntityType).toHaveBeenCalledWith('school')
  })

  it('opens entity form', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(false)
    const addBtn = wrapper.findAll('button').find(b => b.text().includes('Add'))
    await addBtn!.trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(true)
  })
})
