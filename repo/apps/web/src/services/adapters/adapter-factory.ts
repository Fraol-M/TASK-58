import type { ApiAdapter } from './api-adapter.interface'
import { HttpAdapter } from './http-adapter'
import { MockAdapter } from './mock-adapter'

let instance: ApiAdapter | null = null

export function getAdapter(): ApiAdapter {
  if (instance) return instance

  if (import.meta.env.VITE_MOCK_MODE === 'true') {
    instance = new MockAdapter()
  } else {
    instance = new HttpAdapter()
  }

  return instance!
}
