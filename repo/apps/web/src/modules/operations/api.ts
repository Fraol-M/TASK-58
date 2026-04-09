import { getAdapter } from '@/services/adapters/adapter-factory'
import type {
  ReceiptType,
  TransitionRequest,
  AddLineRequest,
  ReceiveLineRequest,
  InspectionRequest,
  PutawayRequest,
  SupervisorReviewRequest,
} from '@/services/adapters/api-adapter.interface'

export function operationsApi() {
  const adapter = getAdapter()

  return {
    getReceipts(params?: { page: number; size: number }) {
      return adapter.getReceipts(params)
    },

    getReceipt(id: number) {
      return adapter.getReceipt(id)
    },

    transitionReceipt(id: number, data: TransitionRequest) {
      return adapter.transitionReceipt(id, data)
    },

    addLine(receiptId: number, data: AddLineRequest) {
      return adapter.addLine(receiptId, data)
    },

    receiveLine(receiptId: number, data: ReceiveLineRequest) {
      return adapter.receiveLine(receiptId, data)
    },

    createInspection(data: InspectionRequest) {
      return adapter.createInspection(data)
    },

    createPutaway(data: PutawayRequest) {
      return adapter.createPutaway(data)
    },

    getDiscrepancies(receiptId: number) {
      return adapter.getDiscrepancies(receiptId)
    },

    getPutawayTasks(receiptId: number) {
      return adapter.getPutawayTasks(receiptId)
    },

    createReceipt(data: { receiptType: ReceiptType; referenceNumber?: string; supplierName?: string; expectedDate?: string }) {
      return adapter.createReceipt(data)
    },

    supervisorReview(data: SupervisorReviewRequest) {
      return adapter.supervisorReview(data)
    },

    postReceipt(id: number) {
      return adapter.postReceipt(id)
    },

    unpostReceipt(id: number) {
      return adapter.unpostReceipt(id)
    },
  }
}
