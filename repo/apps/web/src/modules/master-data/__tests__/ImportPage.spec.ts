import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import ImportPage from '../pages/ImportPage.vue'

const mockSelectedFile = ref<File | null>(null)
const mockEntityType = ref('term')
const mockHasPreview = ref(false)
const mockImportResult = ref<any>(null)
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockOnFileSelected = vi.fn()
const mockSubmitImport = vi.fn()
const mockReset = vi.fn()

vi.mock('../composables/useImport', () => ({
  useImport: () => ({
    selectedFile: mockSelectedFile,
    entityType: mockEntityType,
    hasPreview: mockHasPreview,
    importResult: mockImportResult,
    loading: mockLoading,
    error: mockError,
    onFileSelected: mockOnFileSelected,
    submitImport: mockSubmitImport,
    reset: mockReset,
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

  const wrapper = mount(ImportPage, {
    global: {
      plugins: [pinia],
      stubs: {
        ImportUploader: { template: '<div data-testid="file-upload" @click="$emit(\'files\', [])">Upload Area</div>', emits: ['files'] },
        ImportPreview: { template: '<div data-testid="import-preview"><span v-for="err in (result?.errors || [])" :key="err.row" class="error-row">Row {{ err.row }}: {{ err.message }}</span><button data-testid="confirm-btn" @click="$emit(\'confirm\')">Confirm</button><button @click="$emit(\'cancel\')">Cancel</button></div>', props: ['result'], emits: ['confirm', 'cancel'] },
        FormField: { template: '<div class="form-field"><slot /></div>', props: ['label'] },
        FormSelect: { template: '<select data-testid="type-select"><slot /></select>', props: ['modelValue', 'options', 'placeholder'] },
        AppCard: { template: '<div class="app-card"><slot /></div>', props: ['title'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
      },
    },
  })
  return { wrapper }
}

describe('ImportPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockSelectedFile.value = null
    mockEntityType.value = 'term'
    mockHasPreview.value = false
    mockImportResult.value = null
    mockLoading.value = false
    mockError.value = null
  })

  it('renders file upload area', async () => {
    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="file-upload"]').exists()).toBe(true)
    expect(wrapper.find('h1').text()).toContain('Import Data')
  })

  it('shows preview after upload', async () => {
    mockImportResult.value = {
      totalRows: 10,
      successCount: 8,
      errorCount: 2,
      errors: [
        { row: 3, message: 'Invalid code format' },
        { row: 7, message: 'Duplicate entry' },
      ],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="import-preview"]').exists()).toBe(true)
  })

  it('highlights error rows', async () => {
    mockImportResult.value = {
      totalRows: 10,
      successCount: 8,
      errorCount: 2,
      errors: [
        { row: 3, message: 'Invalid code format' },
        { row: 7, message: 'Duplicate entry' },
      ],
    }

    const { wrapper } = mountPage()
    await flushPromises()

    const errorRows = wrapper.findAll('.error-row')
    expect(errorRows.length).toBe(2)
    expect(errorRows[0].text()).toContain('Row 3')
    expect(errorRows[1].text()).toContain('Row 7')
  })

  it('confirm triggers import', async () => {
    mockImportResult.value = {
      totalRows: 5,
      successCount: 5,
      errorCount: 0,
      errors: [],
    }
    mockSubmitImport.mockResolvedValue({})

    const { wrapper } = mountPage()
    await flushPromises()

    const confirmBtn = wrapper.find('[data-testid="confirm-btn"]')
    await confirmBtn.trigger('click')
    await flushPromises()

    expect(mockSubmitImport).toHaveBeenCalled()
  })
})
