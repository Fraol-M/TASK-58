import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import ReceivingDetailPage from '../pages/ReceivingDetailPage.vue'

const mockReceipt = ref<any>(null)
const mockDiscrepancies = ref<any[]>([])
const mockPutawayTasks = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockTransition = vi.fn()
const mockInspect = vi.fn()
const mockRefresh = vi.fn()

vi.mock('../composables/useReceiptDetail', () => ({
  useReceiptDetail: () => ({
    receipt: mockReceipt,
    discrepancies: mockDiscrepancies,
    putawayTasks: mockPutawayTasks,
    loading: mockLoading,
    error: mockError,
    transition: mockTransition,
    inspect: mockInspect,
    putaway: vi.fn(),
    refresh: mockRefresh,
  }),
}))

vi.mock('../composables/useWorkflow', () => ({
  useWorkflow: () => ({
    getAvailableTransitions: (status: string) => {
      const map: Record<string, any[]> = {
        DRAFT: [{ targetState: 'RECEIVING', reason: 'Begin receiving' }, { targetState: 'REJECTED', reason: 'Reject receipt' }],
        RECEIVING: [{ targetState: 'INSPECTION', reason: 'Send to inspection' }, { targetState: 'REJECTED', reason: 'Reject receipt' }],
        INSPECTION: [{ targetState: 'PUTAWAY', reason: 'Approve for putaway' }, { targetState: 'REJECTED', reason: 'Reject after inspection' }],
        PUTAWAY: [{ targetState: 'COMPLETED', reason: 'Complete receipt' }],
        COMPLETED: [],
        REJECTED: [],
      }
      return map[status.toUpperCase()] ?? []
    },
    getStatusColor: () => '#6b7280',
    getStepIndex: () => 0,
    allSteps: ['DRAFT', 'RECEIVING', 'INSPECTION', 'PUTAWAY', 'COMPLETED'],
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
  useRoute: () => ({ params: { receiptId: '1' }, query: {} }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(ReceivingDetailPage, {
    global: {
      plugins: [pinia],
      stubs: {
        AppCard: { template: '<div class="app-card"><slot /></div>', props: ['title', 'padding'] },
        AppButton: { template: '<button @click="$emit(\'click\')" :class="variant"><slot /></button>', props: ['variant', 'size'] },
        ConfirmDialog: { template: '<div v-if="open" data-testid="confirm-dialog"><slot /></div>', props: ['open', 'title', 'message', 'variant'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        WorkflowProgressIndicator: { template: '<div data-testid="workflow-progress">Workflow</div>', props: ['currentStatus'] },
        StatusBadge: { template: '<span data-testid="status-badge">{{ status }}</span>', props: ['status'] },
        ReceivingLineItems: { template: '<div data-testid="line-items">Line Items</div>', props: ['items', 'editable'] },
        InspectionForm: { template: '<div data-testid="inspection-form">Inspection</div>', props: ['items'], emits: ['submit'] },
        DiscrepancyCard: { template: '<div data-testid="discrepancy-card">Discrepancy</div>', props: ['discrepancy'] },
        PutawayItem: { template: '<div data-testid="putaway-item">Putaway</div>', props: ['task'], emits: ['confirm'] },
      },
    },
  })
  return { wrapper }
}

describe('ReceivingDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockReceipt.value = null
    mockDiscrepancies.value = []
    mockPutawayTasks.value = []
    mockLoading.value = false
    mockError.value = null
  })

  it('loads receipt detail', async () => {
    mockReceipt.value = {
      id: 1,
      receiptNumber: 'REC-001',
      supplierName: 'ACME Corp',
      status: 'DRAFT',
      lines: [{ id: 1, receiptId: 1, itemCode: 'WDG-001', itemName: 'Widget', expectedQty: 100, receivedQty: 0 }],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.text()).toContain('REC-001')
    expect(wrapper.text()).toContain('ACME Corp')
  })

  it('shows workflow progress indicator', async () => {
    mockReceipt.value = {
      id: 1,
      receiptNumber: 'REC-001',
      supplierName: 'ACME',
      status: 'RECEIVING',
      lines: [],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="workflow-progress"]').exists()).toBe(true)
  })

  it('renders line items table', async () => {
    mockReceipt.value = {
      id: 1,
      receiptNumber: 'REC-001',
      supplierName: 'ACME',
      status: 'RECEIVING',
      lines: [
        { id: 1, receiptId: 1, itemCode: 'WA-001', itemName: 'Widget A', expectedQty: 100, receivedQty: 95 },
        { id: 2, receiptId: 1, itemCode: 'WB-001', itemName: 'Widget B', expectedQty: 50, receivedQty: 50 },
      ],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="line-items"]').exists()).toBe(true)
  })

  it('shows transition actions based on state', async () => {
    mockReceipt.value = {
      id: 1,
      receiptNumber: 'REC-001',
      supplierName: 'ACME',
      status: 'DRAFT',
      lines: [],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    // DRAFT has 2 transitions: Begin receiving and Reject receipt
    const buttons = wrapper.findAll('.receipt-detail__actions button')
    expect(buttons.length).toBe(2)
    expect(buttons[0].text()).toContain('Begin receiving')
    expect(buttons[1].text()).toContain('Reject receipt')
  })

  it('shows inspection form when in INSPECTION state', async () => {
    mockReceipt.value = {
      id: 1,
      receiptNumber: 'REC-001',
      supplierName: 'ACME',
      status: 'INSPECTION',
      lines: [{ id: 1, receiptId: 1, itemCode: 'WDG-001', itemName: 'Widget', expectedQty: 100, receivedQty: 100 }],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="inspection-form"]').exists()).toBe(true)
  })
})
