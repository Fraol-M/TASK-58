import { ref, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useMasterDataStore } from '../store'

export function useMasterData(initialType?: string) {
  const store = useMasterDataStore()
  const { items, totalItems, loading, error } = storeToRefs(store)

  const entityType = ref(initialType || 'term')
  const searchQuery = ref('')
  const page = ref(0)
  const pageSize = ref(25)

  function fetch() {
    store.fetchItems(entityType.value, page.value, pageSize.value)
  }

  onMounted(fetch)

  watch([entityType, page, pageSize], fetch)

  function setEntityType(type: string) {
    entityType.value = type
    page.value = 0
  }

  return {
    items,
    totalItems,
    loading,
    error,
    entityType,
    searchQuery,
    page,
    pageSize,
    setEntityType,
    refresh: fetch,
  }
}
