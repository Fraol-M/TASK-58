import { ref, type Ref } from 'vue'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export interface AsyncState<T> {
  data: Ref<T | null>
  loading: Ref<boolean>
  error: Ref<ApiError | null>
  execute: (fn: () => Promise<T>) => Promise<T | null>
  reset: () => void
}

export function useAsyncState<T>(initialData: T | null = null): AsyncState<T> {
  const data = ref<T | null>(initialData) as Ref<T | null>
  const loading = ref(false)
  const error = ref<ApiError | null>(null)

  async function execute(fn: () => Promise<T>): Promise<T | null> {
    loading.value = true
    error.value = null
    try {
      const result = await fn()
      data.value = result
      return result
    } catch (e) {
      error.value = normalizeError(e)
      return null
    } finally {
      loading.value = false
    }
  }

  function reset() {
    data.value = initialData
    loading.value = false
    error.value = null
  }

  return { data, loading, error, execute, reset }
}
