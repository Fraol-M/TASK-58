import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ReceivingReceipt, PutawayTask, ReceiptType, ReceiptStatus, Discrepancy } from '@/services/adapters/api-adapter.interface'
import type { PaginatedResponse } from '@/types/api'
import { operationsApi } from './api'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export const useOperationsStore = defineStore('operations', () => {
  const receipts = ref<ReceivingReceipt[]>([])
  const currentReceipt = ref<ReceivingReceipt | null>(null)
  const putawayTasks = ref<PutawayTask[]>([])
  const discrepancies = ref<Discrepancy[]>([])
  const loading = ref(false)
  const error = ref<ApiError | null>(null)
  const totalReceipts = ref(0)

  const api = operationsApi()

  async function fetchReceipts(page = 0, size = 25) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getReceipts({ page, size })
      const payload = res.data as ReceivingReceipt[] | PaginatedResponse<ReceivingReceipt>
      if (Array.isArray(payload)) {
        receipts.value = payload
        totalReceipts.value = payload.length
      } else if (payload && Array.isArray(payload.content)) {
        receipts.value = payload.content
        totalReceipts.value = payload.totalElements ?? payload.content.length
      } else {
        receipts.value = []
        totalReceipts.value = 0
      }
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function fetchReceipt(id: number) {
    loading.value = true
    error.value = null
    try {
      const res = await api.getReceipt(id)
      currentReceipt.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function fetchDiscrepancies(receiptId: number) {
    try {
      const res = await api.getDiscrepancies(receiptId)
      const payload = res.data
      if (Array.isArray(payload)) {
        discrepancies.value = payload
      } else {
        discrepancies.value = []
      }
    } catch (e) {
      discrepancies.value = []
    }
  }

  async function fetchAllDiscrepancies() {
    loading.value = true
    try {
      if (receipts.value.length === 0) {
        await fetchReceipts(0, 100)
      }
      const allDisc: Discrepancy[] = []
      for (const r of receipts.value) {
        try {
          const res = await api.getDiscrepancies(r.id)
          if (Array.isArray(res.data)) {
            allDisc.push(...res.data)
          }
        } catch { /* skip receipt if discrepancies fail */ }
      }
      discrepancies.value = allDisc
    } catch {
      discrepancies.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchPutawayTasks() {
    loading.value = true
    try {
      if (receipts.value.length === 0) {
        await fetchReceipts(0, 100)
      }
      const allTasks: PutawayTask[] = []
      const putawayReceipts = receipts.value.filter(r => r.status === 'PUTAWAY')
      for (const r of putawayReceipts) {
        try {
          const res = await api.getPutawayTasks(r.id)
          if (Array.isArray(res.data)) {
            allTasks.push(...res.data)
          }
        } catch { /* skip receipt */ }
      }
      putawayTasks.value = allTasks
    } catch {
      putawayTasks.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchPutawayTasksForReceipt(receiptId: number) {
    try {
      const res = await api.getPutawayTasks(receiptId)
      if (Array.isArray(res.data)) {
        putawayTasks.value = res.data
      }
    } catch {
      putawayTasks.value = []
    }
  }

  async function addLine(receiptId: number, data: { itemCode: string; itemName: string; expectedQty: number; unitCost?: number }) {
    loading.value = true
    error.value = null
    try {
      await api.addLine(receiptId, data)
      await fetchReceipt(receiptId)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function receiveLine(receiptId: number, lineId: number, receivedQty: number) {
    error.value = null
    try {
      await api.receiveLine(receiptId, { lineId, receivedQty })
      await fetchReceipt(receiptId)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    }
  }

  async function createReceipt(data: { receiptType: ReceiptType; referenceNumber?: string; supplierName?: string; expectedDate?: string }) {
    loading.value = true
    error.value = null
    try {
      const res = await api.createReceipt(data)
      return res.data
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function transitionReceipt(id: number, targetState: ReceiptStatus, reason?: string) {
    loading.value = true
    error.value = null
    try {
      await api.transitionReceipt(id, { targetState, reason })
      await fetchReceipt(id)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createInspection(receiptId: number, lineId: number, inspectedQty: number, result: 'PASS' | 'FAIL', notes?: string) {
    loading.value = true
    error.value = null
    try {
      await api.createInspection({ receiptId, lineId, inspectedQty, result, notes })
      await fetchReceipt(receiptId)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createPutaway(receiptId: number, taskId: number, actualLocation?: string) {
    loading.value = true
    error.value = null
    try {
      const res = await api.createPutaway({ receiptId, taskId, actualLocation })
      putawayTasks.value = res.data
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function postReceipt(receiptId: number) {
    loading.value = true
    error.value = null
    try {
      await api.postReceipt(receiptId)
      await fetchReceipt(receiptId)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function unpostReceipt(receiptId: number) {
    loading.value = true
    error.value = null
    try {
      await api.unpostReceipt(receiptId)
      await fetchReceipt(receiptId)
    } catch (e) {
      error.value = normalizeError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    receipts,
    currentReceipt,
    putawayTasks,
    discrepancies,
    loading,
    error,
    totalReceipts,
    fetchReceipts,
    fetchReceipt,
    fetchDiscrepancies,
    fetchAllDiscrepancies,
    fetchPutawayTasks,
    fetchPutawayTasksForReceipt,
    addLine,
    receiveLine,
    createReceipt,
    transitionReceipt,
    postReceipt,
    unpostReceipt,
    createInspection,
    createPutaway,
  }
})
