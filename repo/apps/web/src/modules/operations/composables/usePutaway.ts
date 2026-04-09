import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useOperationsStore } from '../store'

export function usePutaway() {
  const store = useOperationsStore()
  const { putawayTasks, loading, error } = storeToRefs(store)

  onMounted(() => {
    store.fetchPutawayTasks()
  })

  const pendingTasks = computed(() =>
    putawayTasks.value.filter(t => t.status === 'PENDING')
  )

  const completedTasks = computed(() =>
    putawayTasks.value.filter(t => t.status === 'COMPLETED')
  )

  return {
    putawayTasks,
    pendingTasks,
    completedTasks,
    loading,
    error,
  }
}
