import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useOperationsStore } from '../store'

export function useDiscrepancies() {
  const store = useOperationsStore()
  const { discrepancies: allDiscrepancies, loading, error } = storeToRefs(store)

  onMounted(() => {
    store.fetchAllDiscrepancies()
  })

  const filterResolved = ref<'all' | 'resolved' | 'unresolved'>('all')
  const filterSupervisor = ref(false)

  const filteredDiscrepancies = computed(() => {
    let result = allDiscrepancies.value

    if (filterResolved.value === 'resolved') {
      result = result.filter(d => d.resolved)
    } else if (filterResolved.value === 'unresolved') {
      result = result.filter(d => !d.resolved)
    }

    if (filterSupervisor.value) {
      result = result.filter(d => d.supervisorRequired)
    }

    return result
  })

  return {
    discrepancies: filteredDiscrepancies,
    allDiscrepancies,
    loading,
    error,
    filterResolved,
    filterSupervisor,
  }
}
