import { ref, watch, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useMasterDataStore } from '../store'

export function useAuditHistory() {
  const store = useMasterDataStore()
  const { changeHistory, loading, error } = storeToRefs(store)

  const entityTypeFilter = ref('')
  const dateFrom = ref('')
  const dateTo = ref('')

  function fetch() {
    store.fetchHistory(entityTypeFilter.value || undefined)
  }

  onMounted(fetch)
  watch(entityTypeFilter, fetch)

  return {
    history: changeHistory,
    loading,
    error,
    entityTypeFilter,
    dateFrom,
    dateTo,
    refresh: fetch,
  }
}
