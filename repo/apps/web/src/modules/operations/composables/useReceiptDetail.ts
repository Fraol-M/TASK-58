import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useOperationsStore } from '../store'
import type { ReceiptStatus } from '@/services/adapters/api-adapter.interface'

export function useReceiptDetail(receiptId: number) {
  const store = useOperationsStore()
  const { currentReceipt, discrepancies, putawayTasks, loading, error } = storeToRefs(store)

  onMounted(async () => {
    await store.fetchReceipt(receiptId)
    store.fetchDiscrepancies(receiptId)
    if (store.currentReceipt?.status === 'PUTAWAY') {
      store.fetchPutawayTasksForReceipt(receiptId)
    }
  })

  async function transition(status: ReceiptStatus, notes?: string) {
    await store.transitionReceipt(receiptId, status, notes)
    store.fetchDiscrepancies(receiptId)
  }

  async function addLine(data: { itemCode: string; itemName: string; expectedQty: number; unitCost?: number }) {
    await store.addLine(receiptId, data)
  }

  async function receiveLine(lineId: number, receivedQty: number) {
    await store.receiveLine(receiptId, lineId, receivedQty)
  }

  async function inspect(lineId: number, inspectedQty: number, result: 'PASS' | 'FAIL', notes?: string) {
    await store.createInspection(receiptId, lineId, inspectedQty, result, notes)
    store.fetchDiscrepancies(receiptId)
  }

  async function putaway(taskId: number, actualLocation?: string) {
    await store.createPutaway(receiptId, taskId, actualLocation)
  }

  async function post() {
    await store.postReceipt(receiptId)
  }

  async function unpost() {
    await store.unpostReceipt(receiptId)
  }

  function refresh() {
    store.fetchReceipt(receiptId)
    store.fetchDiscrepancies(receiptId)
  }

  return {
    receipt: currentReceipt,
    discrepancies,
    putawayTasks,
    loading,
    error,
    transition,
    addLine,
    receiveLine,
    inspect,
    putaway,
    post,
    unpost,
    refresh,
  }
}
