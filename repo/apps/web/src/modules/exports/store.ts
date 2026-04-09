import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ExportResult } from '@/services/adapters/api-adapter.interface'
import { exportsApi } from './api'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export const useExportsStore = defineStore('exports', () => {
  const exportResult = ref<ExportResult | null>(null)
  const loading = ref(false)
  const error = ref<ApiError | null>(null)

  const api = exportsApi()

  async function requestExport(exportType: string, password?: string) {
    loading.value = true
    error.value = null
    try {
      const res = await api.requestExport({
        exportType: exportType as 'ACCOUNT_DATA' | 'STUDY_DATA' | 'FITNESS_DATA',
        passwordProtected: !!password,
        exportPassword: password,
      })
      exportResult.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteAccount(password: string) {
    loading.value = true
    error.value = null
    try {
      await api.deleteAccount(password)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    exportResult,
    loading,
    error,
    requestExport,
    deleteAccount,
  }
})
