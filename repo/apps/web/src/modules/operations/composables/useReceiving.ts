import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useOperationsStore } from '../store'

export function useReceiving() {
  const store = useOperationsStore()
  const { receipts, loading, error, totalReceipts } = storeToRefs(store)

  const statusFilter = ref('all')

  const filteredReceipts = computed(() => {
    if (statusFilter.value === 'all') return receipts.value
    return receipts.value.filter(r => r.status === statusFilter.value)
  })

  onMounted(() => {
    store.fetchReceipts()
  })

  function setFilter(status: string) {
    statusFilter.value = status
  }

  function refresh() {
    store.fetchReceipts()
  }

  return {
    receipts: filteredReceipts,
    allReceipts: receipts,
    loading,
    error,
    totalReceipts,
    statusFilter,
    setFilter,
    refresh,
  }
}
