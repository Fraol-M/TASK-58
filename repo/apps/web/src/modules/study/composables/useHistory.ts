import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useStudyStore } from '../store'

export function useHistory() {
  const store = useStudyStore()
  const { completions, plans, loading, error } = storeToRefs(store)

  const selectedPlanId = ref<number | null>(null)
  const dateFrom = ref('')
  const dateTo = ref('')

  const filteredCompletions = computed(() => {
    let result = completions.value

    if (selectedPlanId.value) {
      result = result.filter(c => c.planId === selectedPlanId.value)
    }

    if (dateFrom.value) {
      result = result.filter(c => c.createdAt >= dateFrom.value)
    }

    if (dateTo.value) {
      result = result.filter(c => c.createdAt <= dateTo.value)
    }

    return result
  })

  // Build a simple completion calendar for last 30 days
  const completionDays = computed(() => {
    const days: Array<{ date: string; completed: boolean; isToday: boolean }> = []
    const today = new Date()
    const completionDates = new Set(completions.value.map(c => c.completedDate.substring(0, 10)))

    for (let i = 29; i >= 0; i--) {
      const d = new Date(today)
      d.setDate(d.getDate() - i)
      const dateStr = d.toISOString().substring(0, 10)
      days.push({
        date: dateStr,
        completed: completionDates.has(dateStr),
        isToday: i === 0,
      })
    }
    return days
  })

  onMounted(() => {
    if (plans.value.length === 0) {
      store.fetchPlans()
    }
    // Fetch completions for all plans
    for (const plan of plans.value) {
      store.fetchCompletions(plan.id)
    }
  })

  return {
    completions: filteredCompletions,
    plans,
    loading,
    error,
    selectedPlanId,
    dateFrom,
    dateTo,
    completionDays,
  }
}
