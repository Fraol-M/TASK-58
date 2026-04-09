import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '../store'

export function useDashboardData() {
  const store = useDashboardStore()
  const { stats, loading, error } = storeToRefs(store)

  onMounted(() => {
    if (!stats.value) {
      store.fetchStats()
    }
  })

  return {
    stats,
    loading,
    error,
    refresh: store.fetchStats,
  }
}
