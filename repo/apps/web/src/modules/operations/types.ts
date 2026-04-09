export type ReceiptType = 'PURCHASE' | 'TRANSFER' | 'RETURN'
export type ReceiptStatus = 'DRAFT' | 'RECEIVING' | 'INSPECTION' | 'PUTAWAY' | 'COMPLETED' | 'REJECTED' | 'POSTED' | 'UNPOSTED'
export type ReasonCode = 'DAMAGED' | 'SHORT_SHIP' | 'OVER_SHIP' | 'WRONG_ITEM' | 'QUALITY_FAIL' | 'OTHER'

export interface InboundReceipt {
  id: number
  receiptNumber: string
  receiptType: ReceiptType
  referenceNumber?: string
  supplierName: string
  status: ReceiptStatus
  expectedDate?: string
  receivedDate?: string
  lines: InboundLine[]
  createdAt: string
}

export interface InboundLine {
  id: number
  itemCode: string
  itemName: string
  expectedQty: number
  receivedQty: number
  inspectedQty: number
  inspectionResult?: 'PASS' | 'FAIL'
  inspectionNotes?: string
}

export interface Discrepancy {
  id: number
  lineId: number
  type: string
  expectedValue: string
  actualValue: string
  variancePercent: number
  reasonCode?: ReasonCode
  supervisorRequired: boolean
  resolvedBy?: string
  notes?: string
}

export interface PutawayTask {
  id: number
  lineId: number
  suggestedLocation: string
  actualLocation?: string
  status: 'pending' | 'in_progress' | 'completed'
}

export interface StateTransition {
  targetState: ReceiptStatus
  reason?: string
}
