import { getAdapter } from '@/services/adapters/adapter-factory'
import type { CreateGoalRequest, CreateCheckInRequest } from '@/services/adapters/api-adapter.interface'

export function fitnessApi() {
  const adapter = getAdapter()

  return {
    getAssessment() {
      return adapter.getAssessment()
    },

    saveAssessment(data: Record<string, unknown>) {
      return adapter.saveAssessment(data)
    },

    getGoals(params?: { page: number; size: number }) {
      return adapter.getGoals(params)
    },

    createGoal(data: CreateGoalRequest) {
      return adapter.createGoal(data)
    },

    getGoal(id: number) {
      return adapter.getGoal(id)
    },

    createCheckIn(data: CreateCheckInRequest) {
      return adapter.createCheckIn(data)
    },
  }
}
