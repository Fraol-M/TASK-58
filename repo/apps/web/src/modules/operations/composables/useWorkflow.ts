import type { ReceiptStatus, StateTransition } from '../types'

const TRANSITIONS: Record<string, StateTransition[]> = {
  DRAFT: [
    { targetState: 'RECEIVING', reason: 'Begin receiving' },
    { targetState: 'REJECTED', reason: 'Reject receipt' },
  ],
  RECEIVING: [
    { targetState: 'INSPECTION', reason: 'Send to inspection' },
    { targetState: 'REJECTED', reason: 'Reject receipt' },
  ],
  INSPECTION: [
    { targetState: 'PUTAWAY', reason: 'Approve for putaway' },
    { targetState: 'REJECTED', reason: 'Reject after inspection' },
  ],
  PUTAWAY: [
    { targetState: 'COMPLETED', reason: 'Complete receipt' },
  ],
  COMPLETED: [],
  REJECTED: [],
  POSTED: [],
  UNPOSTED: [],
}

const STATUS_COLORS: Record<string, string> = {
  DRAFT: '#6b7280',
  RECEIVING: '#2563eb',
  INSPECTION: '#d97706',
  PUTAWAY: '#7c3aed',
  COMPLETED: '#059669',
  REJECTED: '#dc2626',
  POSTED: '#0891b2',
  UNPOSTED: '#92400e',
  // lowercase variants from API
  pending: '#6b7280',
  in_progress: '#2563eb',
  inspection: '#d97706',
  putaway: '#7c3aed',
  completed: '#059669',
  rejected: '#dc2626',
  posted: '#0891b2',
  unposted: '#92400e',
}

const ALL_STEPS: ReceiptStatus[] = ['DRAFT', 'RECEIVING', 'INSPECTION', 'PUTAWAY', 'COMPLETED']

export function useWorkflow() {
  function getAvailableTransitions(currentStatus: string): StateTransition[] {
    const key = currentStatus.toUpperCase()
    return TRANSITIONS[key] ?? []
  }

  function getStatusColor(status: string): string {
    return STATUS_COLORS[status] ?? STATUS_COLORS[status.toUpperCase()] ?? '#6b7280'
  }

  function getStepIndex(status: string): number {
    const upper = status.toUpperCase()
    if (upper === 'REJECTED') return -1
    return ALL_STEPS.indexOf(upper as ReceiptStatus)
  }

  return {
    getAvailableTransitions,
    getStatusColor,
    getStepIndex,
    allSteps: ALL_STEPS,
  }
}
