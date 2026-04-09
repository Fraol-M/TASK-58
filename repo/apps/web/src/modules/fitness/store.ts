import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  FitnessAssessment,
  FitnessGoal,
  FitnessCheckIn,
  CreateGoalRequest,
  CreateCheckInRequest,
} from '@/services/adapters/api-adapter.interface'
import type { PaginatedResponse } from '@/types/api'
import { fitnessApi } from './api'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export const useFitnessStore = defineStore('fitness', () => {
  const assessment = ref<FitnessAssessment | null>(null)
  const goals = ref<FitnessGoal[]>([])
  const currentGoal = ref<FitnessGoal | null>(null)
  const checkIns = ref<FitnessCheckIn[]>([])
  const loading = ref(false)
  const error = ref<ApiError | null>(null)
  const totalGoals = ref(0)

  const api = fitnessApi()

  async function fetchAssessment() {
    loading.value = true
    error.value = null
    try {
      const res = await api.getAssessment()
      assessment.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function saveAssessment(data: Record<string, unknown>) {
    loading.value = true
    error.value = null
    try {
      const res = await api.saveAssessment(data)
      assessment.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchGoals(page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getGoals({ page, size })
      const payload = res.data as FitnessGoal[] | PaginatedResponse<FitnessGoal>
      if (Array.isArray(payload)) {
        goals.value = payload
        totalGoals.value = payload.length
      } else if (payload && Array.isArray(payload.content)) {
        goals.value = payload.content
        totalGoals.value = payload.totalElements ?? payload.content.length
      } else {
        goals.value = []
        totalGoals.value = 0
      }
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function fetchGoal(id: number) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getGoal(id)
      currentGoal.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function createGoal(data: CreateGoalRequest) {
    loading.value = true
    error.value = null
    try {
      const res = await api.createGoal(data)
      goals.value.unshift(res.data)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createCheckIn(data: CreateCheckInRequest) {
    loading.value = true
    error.value = null
    try {
      const res = await api.createCheckIn(data)
      checkIns.value.unshift(res.data)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    assessment,
    goals,
    currentGoal,
    checkIns,
    loading,
    error,
    totalGoals,
    fetchAssessment,
    saveAssessment,
    fetchGoals,
    fetchGoal,
    createGoal,
    createCheckIn,
  }
})
