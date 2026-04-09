import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useMasterDataStore } from '../store'

export function useMerge() {
  const store = useMasterDataStore()
  const { mergeCandidates, loading, error } = storeToRefs(store)

  const entityType = ref('term')

  function fetchCandidates() {
    store.fetchMergeCandidates(entityType.value)
  }

  async function merge(sourceId: number, targetId: number) {
    await store.executeMerge(entityType.value, sourceId, targetId)
  }

  onMounted(fetchCandidates)

  return {
    mergeCandidates,
    loading,
    error,
    entityType,
    fetchCandidates,
    merge,
  }
}
