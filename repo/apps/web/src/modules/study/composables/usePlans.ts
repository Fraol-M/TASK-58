import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useStudyStore } from '../store'

export function usePlans() {
  const store = useStudyStore()
  const { plans, loading, error, totalPlans } = storeToRefs(store)

  onMounted(() => {
    if (plans.value.length === 0) {
      store.fetchPlans()
    }
  })

  async function createPlan(data: { title: string; description?: string; courseId?: number; termId?: number }) {
    await store.createPlan(data)
  }

  function refresh() {
    store.fetchPlans()
  }

  return {
    plans,
    loading,
    error,
    totalPlans,
    createPlan,
    refresh,
  }
}
