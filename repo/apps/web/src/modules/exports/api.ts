import { getAdapter } from '@/services/adapters/adapter-factory'
import type { ExportRequest } from '@/services/adapters/api-adapter.interface'

export function exportsApi() {
  const adapter = getAdapter()

  return {
    requestExport(data: ExportRequest) {
      return adapter.requestExport(data)
    },

    deleteAccount(password: string) {
      return adapter.deleteAccount(password)
    },
  }
}
