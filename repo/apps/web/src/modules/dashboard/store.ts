import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DashboardData } from '@/services/adapters/api-adapter.interface'
import { dashboardApi } from './api'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref<DashboardData | null>(null)
  const loading = ref(false)
  const error = ref<ApiError | null>(null)

  const api = dashboardApi()

  async function fetchStats() {
    loading.value = true
    error.value = null
    try {
      const res = await api.getDashboardStats()
      stats.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  return {
    stats,
    loading,
    error,
    fetchStats,
  }
})
