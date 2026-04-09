import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import ReceivingListPage from '../pages/ReceivingListPage.vue'

const mockReceipts = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockStatusFilter = ref('all')
const mockSetFilter = vi.fn()
const mockRefresh = vi.fn()

vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => ({
    getReceipts: vi.fn().mockResolvedValue({ data: [] }),
    createReceipt: vi.fn().mockResolvedValue({ data: {} }),
  }),
}))

vi.mock('../composables/useReceiving', () => ({
  useReceiving: () => ({
    receipts: mockReceipts,
    loading: mockLoading,
    error: mockError,
    statusFilter: mockStatusFilter,
    setFilter: mockSetFilter,
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

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
  useRoute: () => ({ params: {}, query: {} }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(ReceivingListPage, {
    global: {
      plugins: [pinia],
      stubs: {
        ReceivingTable: { name: 'ReceivingTable', template: '<table data-testid="receiving-table"><tbody><tr v-for="r in receipts" :key="r.id" @click="$emit(\'row-click\', r.id)"><td>{{ r.receiptNumber }}</td></tr></tbody></table>', props: ['receipts'], emits: ['row-click'] },
        ReceiptForm: { template: '<div data-testid="receipt-form">Receipt Form</div>', props: ['loading'], emits: ['submit'] },
        AppButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        AppModal: { template: '<div v-if="open" data-testid="modal"><slot /></div>', props: ['open', 'title'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        EmptyState: { template: '<div data-testid="empty">No receipts</div>', props: ['title', 'message', 'actionText'] },
      },
    },
  })
  return { wrapper }
}

describe('ReceivingListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockReceipts.value = []
    mockLoading.value = false
    mockError.value = null
    mockStatusFilter.value = 'all'
  })

  it('renders receiving table', async () => {
    mockReceipts.value = [
      { id: 1, receiptNumber: 'REC-001', supplierName: 'ACME', status: 'DRAFT', lines: [] },
      { id: 2, receiptNumber: 'REC-002', supplierName: 'Globex', status: 'RECEIVING', lines: [] },
    ]
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="receiving-table"]').exists()).toBe(true)
  })

  it('shows status filter tabs', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    const tabs = wrapper.findAll('.tab-btn')
    expect(tabs.length).toBe(7)
    expect(tabs[0].text()).toBe('All')
    expect(tabs[1].text()).toBe('Draft')
    expect(tabs[2].text()).toBe('Receiving')
  })

  it('opens receipt creation form', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(false)
    const newBtn = wrapper.findAll('button').find(b => b.text().includes('New Receipt'))
    await newBtn!.trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid="modal"]').exists()).toBe(true)
  })

  it('navigates to detail on click', async () => {
    mockReceipts.value = [
      { id: 42, receiptNumber: 'REC-042', supplierName: 'ACME', status: 'DRAFT', lines: [] },
    ]
    const { wrapper } = mountPage()
    await flushPromises()

    const table = wrapper.findComponent({ name: 'ReceivingTable' })
    table.vm.$emit('row-click', 42)
    await flushPromises()

    expect(mockPush).toHaveBeenCalledWith({ name: 'opsReceiptDetail', params: { receiptId: '42' } })
  })
})
