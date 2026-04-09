import { getAdapter } from '@/services/adapters/adapter-factory'

export function masterDataApi() {
  const adapter = getAdapter()

  return {
    getItems(type?: string, params?: { page: number; size: number }) {
      return adapter.getItems(type, params)
    },

    importFile(file: File, type: string) {
      return adapter.importFile(file, type)
    },

    getMergeCandidates(type: string) {
      return adapter.getMergeCandidate(type)
    },

    executeMerge(entityType: string, sourceId: number, targetId: number) {
      return adapter.executeMerge(entityType, sourceId, targetId)
    },

    getHistory(entityType?: string, entityId?: number) {
      return adapter.getHistory(entityType, entityId)
    },
  }
}
