export interface ApiResponse<T> {
  success: boolean
  data: T
  error?: string
}

export interface ApiError {
  status: number
  message: string
  fieldErrors: Record<string, string>
}

export interface PaginatedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PaginationParams {
  page: number
  size: number
  sort?: string
  direction?: 'asc' | 'desc'
}
