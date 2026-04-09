import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useMasterDataStore } from '../store'

export function useImport() {
  const store = useMasterDataStore()
  const { importResult, loading, error } = storeToRefs(store)

  const selectedFile = ref<File | null>(null)
  const entityType = ref('term')
  const previewRows = ref<Record<string, string>[]>([])
  const hasPreview = ref(false)

  function onFileSelected(files: File[]) {
    if (files.length > 0) {
      selectedFile.value = files[0]
      // Generate basic preview from file name
      hasPreview.value = true
    }
  }

  async function submitImport() {
    if (!selectedFile.value) return
    await store.importFile(selectedFile.value, entityType.value)
  }

  function reset() {
    selectedFile.value = null
    previewRows.value = []
    hasPreview.value = false
  }

  return {
    selectedFile,
    entityType,
    previewRows,
    hasPreview,
    importResult,
    loading,
    error,
    onFileSelected,
    submitImport,
    reset,
  }
}
