import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { StudyPlan, DailyCompletion, ForgettingPoint } from '@/services/adapters/api-adapter.interface'
import { studyApi } from './api'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export const useStudyStore = defineStore('study', () => {
  const plans = ref<StudyPlan[]>([])
  const currentPlan = ref<StudyPlan | null>(null)
  const completions = ref<DailyCompletion[]>([])
  const forgettingPoints = ref<ForgettingPoint[]>([])
  const loading = ref(false)
  const error = ref<ApiError | null>(null)
  const totalPlans = ref(0)

  const api = studyApi()

  async function fetchPlans(page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getPlans({ page, size })
      const payload = res.data as any
      if (Array.isArray(payload)) {
        plans.value = payload
        totalPlans.value = payload.length
      } else if (payload && Array.isArray(payload.content)) {
        plans.value = payload.content
        totalPlans.value = payload.totalElements ?? payload.content.length
      } else {
        plans.value = []
        totalPlans.value = 0
      }
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function createPlan(data: { title: string; description?: string; termId?: number; schoolId?: number; majorId?: number; classId?: number; courseId?: number }) {
    loading.value = true
    error.value = null
    try {
      const res = await api.createPlan(data)
      plans.value.unshift(res.data)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchCompletions(planId: number) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getCompletions(planId)
      completions.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function fetchForgettingPoints(planId: number) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getForgettingPoints(planId)
      forgettingPoints.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function addForgettingPoint(planId: number, topic: string, description?: string) {
    try {
      const res = await api.addForgettingPoint(planId, { topic, description })
      forgettingPoints.value.unshift(res.data)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    }
  }

  return {
    plans,
    currentPlan,
    completions,
    forgettingPoints,
    loading,
    error,
    totalPlans,
    fetchPlans,
    createPlan,
    fetchCompletions,
    fetchForgettingPoints,
    addForgettingPoint,
  }
})
