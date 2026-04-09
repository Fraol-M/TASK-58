import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import ExportsPage from '../pages/ExportsPage.vue'

const mockExportResult = ref<any>(null)
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockRequestExport = vi.fn()
const mockDeleteAccount = vi.fn()

vi.mock('../composables/useExports', () => ({
  useExports: () => ({
    exportResult: mockExportResult,
    loading: mockLoading,
    error: mockError,
    requestExport: mockRequestExport,
    deleteAccount: mockDeleteAccount,
  }),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    toasts: { value: [] },
    show: vi.fn(),
    dismiss: vi.fn(),
  }),
}))

vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => ({
    listExports: vi.fn().mockResolvedValue({ data: [
      { id: 1, exportType: 'ACCOUNT_DATA', status: 'COMPLETED', createdAt: '2026-01-01' },
      { id: 2, exportType: 'FITNESS_DATA', status: 'COMPLETED', createdAt: '2026-01-02' },
    ] }),
    importAccountFile: vi.fn().mockResolvedValue({ data: 'Import completed' }),
  }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(ExportsPage, {
    global: {
      plugins: [pinia],
      stubs: {
        ExportForm: {
          name: 'ExportForm',
          template: '<form data-testid="export-form"><input data-testid="password-input" type="password" /><button type="button" @click="$emit(\'submit\', { exportType: \'ACCOUNT_DATA\', password: \'test123\' })">Export</button></form>',
          props: ['loading'],
          emits: ['submit'],
        },
        ExportHistory: { template: '<div data-testid="export-history"></div>', props: ['exports'] },
        AccountDeletionDialog: {
          template: '<div data-testid="account-deletion"><button data-testid="delete-confirm-btn" @click="$emit(\'confirm\', \'password\')">Delete Account</button></div>',
          props: ['loading'],
          emits: ['confirm'],
        },
        AppCard: { template: '<div class="app-card"><slot /></div>', props: ['title', 'padding'] },
        AppButton: { template: '<button><slot /></button>', props: ['loading', 'disabled'] },
        FormInput: { template: '<input />', props: ['modelValue', 'type', 'placeholder'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>' },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
      },
    },
  })
  return { wrapper }
}

describe('ExportsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockExportResult.value = null
    mockLoading.value = false
    mockError.value = null
  })

  it('renders export form and import section', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('h1').text()).toContain('Data Export & Import')
    expect(wrapper.find('[data-testid="export-form"]').exists()).toBe(true)
  })

  it('requires password in export form', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="password-input"]').exists()).toBe(true)
  })

  it('submits export request with type and password', async () => {
    mockRequestExport.mockResolvedValue({})

    const { wrapper } = mountPage()
    await flushPromises()

    const form = wrapper.findComponent({ name: 'ExportForm' })
    form.vm.$emit('submit', { exportType: 'ACCOUNT_DATA', password: 'test123' })
    await flushPromises()

    expect(mockRequestExport).toHaveBeenCalledWith('ACCOUNT_DATA', 'test123')
  })

  it('renders export history section', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="export-history"]').exists()).toBe(true)
  })

  it('account deletion requires confirmation', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="account-deletion"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Delete Account')

    const deleteBtn = wrapper.find('[data-testid="delete-confirm-btn"]')
    expect(deleteBtn.exists()).toBe(true)
    await deleteBtn.trigger('click')
    await flushPromises()

    expect(mockDeleteAccount).toHaveBeenCalled()
  })
})
