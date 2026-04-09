import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useFitnessStore } from '../store'

export function useGoals() {
  const store = useFitnessStore()
  const { goals, loading, error, totalGoals } = storeToRefs(store)

  onMounted(() => {
    if (goals.value.length === 0) {
      store.fetchGoals()
    }
  })

  async function createGoal(data: { goalType: string; description?: string; targetValue: number; unit: string; startDate: string; targetDate: string }) {
    await store.createGoal(data)
  }

  function refresh() {
    store.fetchGoals()
  }

  return {
    goals,
    loading,
    error,
    totalGoals,
    createGoal,
    refresh,
  }
}
