import { getAdapter } from '@/services/adapters/adapter-factory'
import type { DashboardData } from '@/services/adapters/api-adapter.interface'

export function dashboardApi() {
  const adapter = getAdapter()

  return {
    getDashboardStats() {
      return adapter.getDashboard()
    },
  }
}
