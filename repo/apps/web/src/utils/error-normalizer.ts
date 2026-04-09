import type { ApiError } from '@/types/api'
import type { AxiosError } from 'axios'

function isAxiosError(error: unknown): error is AxiosError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'isAxiosError' in error &&
    (error as AxiosError).isAxiosError === true
  )
}

/**
 * Normalizes any thrown value into a consistent ApiError shape.
 */
export function normalizeError(error: unknown): ApiError {
  if (isAxiosError(error)) {
    const response = error.response
    if (response) {
      const data = response.data as Record<string, unknown> | undefined
      return {
        status: response.status,
        message:
          (data?.message as string) ??
          (data?.error as string) ??
          `Request failed with status ${response.status}`,
        fieldErrors: (data?.fieldErrors as Record<string, string>) ?? {},
      }
    }
    // Network error (no response)
    return {
      status: 0,
      message: 'Network error. Please check your connection and try again.',
      fieldErrors: {},
    }
  }

  if (error instanceof Error) {
    return {
      status: 0,
      message: error.message,
      fieldErrors: {},
    }
  }

  return {
    status: 0,
    message: 'An unexpected error occurred.',
    fieldErrors: {},
  }
}
