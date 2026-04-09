import { storeToRefs } from 'pinia'
import { useExportsStore } from '../store'

export function useExports() {
  const store = useExportsStore()
  const { exportResult, loading, error } = storeToRefs(store)

  async function requestExport(dataType: string, password?: string) {
    await store.requestExport(dataType, password)
  }

  async function deleteAccount(password: string) {
    await store.deleteAccount(password)
  }

  return {
    exportResult,
    loading,
    error,
    requestExport,
    deleteAccount,
  }
}
