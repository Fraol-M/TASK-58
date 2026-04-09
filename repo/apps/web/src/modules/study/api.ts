import { getAdapter } from '@/services/adapters/adapter-factory'
import type { CreatePlanRequest, CreateForgettingPointRequest, ReviewRequest } from '@/services/adapters/api-adapter.interface'

export function studyApi() {
  const adapter = getAdapter()

  return {
    getPlans(params?: { page: number; size: number }) {
      return adapter.getPlans(params)
    },

    createPlan(data: CreatePlanRequest) {
      return adapter.createPlan(data)
    },

    getCompletions(planId: number) {
      return adapter.getCompletions(planId)
    },

    getForgettingPoints(planId: number) {
      return adapter.getForgettingPoints(planId)
    },

    addForgettingPoint(planId: number, data: CreateForgettingPointRequest) {
      return adapter.addForgettingPoint(planId, data)
    },

    reviewForgettingPoint(id: number, data: ReviewRequest) {
      return adapter.reviewForgettingPoint(id, data)
    },
  }
}
