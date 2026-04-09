import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useFitnessStore } from '../store'

export function useCheckIns(goalId: number) {
  const store = useFitnessStore()
  const { currentGoal, checkIns, loading, error } = storeToRefs(store)
  const submitting = ref(false)

  onMounted(() => {
    store.fetchGoal(goalId)
  })

  async function createCheckIn(value: number, notes: string) {
    submitting.value = true
    try {
      await store.createCheckIn({ goalId, value, notes })
    } finally {
      submitting.value = false
    }
  }

  function refresh() {
    store.fetchGoal(goalId)
  }

  return {
    currentGoal,
    checkIns,
    loading,
    error,
    submitting,
    createCheckIn,
    refresh,
  }
}
