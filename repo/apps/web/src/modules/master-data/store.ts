import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  MasterDataItem,
  ImportResult,
  MergeCandidate,
  ChangeHistoryEntry,
} from '@/services/adapters/api-adapter.interface'
import type { PaginatedResponse } from '@/types/api'
import { masterDataApi } from './api'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export const useMasterDataStore = defineStore('master-data', () => {
  const items = ref<MasterDataItem[]>([])
  const totalItems = ref(0)
  const importResult = ref<ImportResult | null>(null)
  const mergeCandidates = ref<MergeCandidate[]>([])
  const changeHistory = ref<ChangeHistoryEntry[]>([])
  const loading = ref(false)
  const error = ref<ApiError | null>(null)

  const api = masterDataApi()

  async function fetchItems(type?: string, page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getItems(type, { page, size })
      const payload = res.data as MasterDataItem[] | PaginatedResponse<MasterDataItem>
      if (Array.isArray(payload)) {
        items.value = payload
        totalItems.value = payload.length
      } else if (payload && Array.isArray(payload.content)) {
        items.value = payload.content
        totalItems.value = payload.totalElements ?? payload.content.length
      } else {
        items.value = []
        totalItems.value = 0
      }
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function importFile(file: File, type: string) {
    loading.value = true
    error.value = null
    try {
      const res = await api.importFile(file, type)
      importResult.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchMergeCandidates(type: string) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getMergeCandidates(type)
      mergeCandidates.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function executeMerge(entityType: string, sourceId: number, targetId: number) {
    loading.value = true
    error.value = null
    try {
      await api.executeMerge(entityType, sourceId, targetId)
      // Remove merged candidate
      mergeCandidates.value = mergeCandidates.value.filter(
        c => !(c.sourceItem.id === sourceId && c.targetItem.id === targetId)
      )
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchHistory(entityType?: string, entityId?: number) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getHistory(entityType, entityId)
      changeHistory.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  return {
    items,
    totalItems,
    importResult,
    mergeCandidates,
    changeHistory,
    loading,
    error,
    fetchItems,
    importFile,
    fetchMergeCandidates,
    executeMerge,
    fetchHistory,
  }
})
