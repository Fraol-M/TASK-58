import httpClient from '@/services/http-client'
import type { ApiResponse, PaginatedResponse, PaginationParams } from '@/types/api'
import type { User } from '@/types/user'
import type { LoginResponse } from '@/modules/auth/types'
import type {
  ApiAdapter,
  SignInRequest,
  SignUpRequest,
  FitnessAssessment,
  FitnessGoal,
  FitnessCheckIn,
  CreateGoalRequest,
  CreateCheckInRequest,
  StudyPlan,
  DailyCompletion,
  ForgettingPoint,
  CreatePlanRequest,
  CreateCompletionRequest,
  CreateForgettingPointRequest,
  ReviewRequest,
  ReceivingReceipt,
  ReceiptType,
  TransitionRequest,
  AddLineRequest,
  ReceiveLineRequest,
  InspectionRequest,
  PutawayRequest,
  PutawayTask,
  SupervisorReviewRequest,
  Discrepancy,
  InboundLine,
  MasterDataItem,
  ImportResult,
  MergeCandidate,
  ChangeHistoryEntry,
  Notification,
  ExportRequest,
  ExportResult,
  DashboardData,
  PerformanceMetrics,
} from './api-adapter.interface'

function paginationQuery(params?: PaginationParams): Record<string, unknown> {
  if (!params) return {}
  return {
    page: params.page,
    size: params.size,
    ...(params.sort ? { sort: params.sort } : {}),
    ...(params.direction ? { direction: params.direction } : {}),
  }
}

export class HttpAdapter implements ApiAdapter {
  // Auth
  async signIn(payload: SignInRequest): Promise<ApiResponse<LoginResponse>> {
    const { data } = await httpClient.post('/auth/sign-in', payload)
    return data
  }

  async signUp(payload: SignUpRequest): Promise<ApiResponse<User>> {
    const { data } = await httpClient.post('/auth/sign-up', payload)
    return data
  }

  async signOut(): Promise<ApiResponse<void>> {
    const { data } = await httpClient.post('/auth/sign-out')
    return data
  }

  async getMe(): Promise<ApiResponse<User>> {
    const { data } = await httpClient.get('/me')
    return data
  }

  // Fitness
  async getAssessment(): Promise<ApiResponse<FitnessAssessment>> {
    const { data } = await httpClient.get('/fitness/assessment')
    return data
  }

  async saveAssessment(body: Partial<FitnessAssessment>): Promise<ApiResponse<FitnessAssessment>> {
    const { data } = await httpClient.put('/fitness/assessment', body)
    return data
  }

  async getGoals(params?: PaginationParams): Promise<ApiResponse<FitnessGoal[]>> {
    const { data } = await httpClient.get('/fitness/goals', { params: paginationQuery(params) })
    return data
  }

  async createGoal(body: CreateGoalRequest): Promise<ApiResponse<FitnessGoal>> {
    const { data } = await httpClient.post('/fitness/goals', body)
    return data
  }

  async getGoal(id: number): Promise<ApiResponse<FitnessGoal>> {
    const { data } = await httpClient.get(`/fitness/goals/${id}`)
    return data
  }

  async createCheckIn(body: CreateCheckInRequest): Promise<ApiResponse<FitnessCheckIn>> {
    const { data } = await httpClient.post(`/fitness/goals/${body.goalId}/check-ins`, body)
    return data
  }

  // Study
  async getPlans(params?: PaginationParams): Promise<ApiResponse<StudyPlan[]>> {
    const { data } = await httpClient.get('/study/plans', { params: paginationQuery(params) })
    return data
  }

  async createPlan(body: CreatePlanRequest): Promise<ApiResponse<StudyPlan>> {
    const { data } = await httpClient.post('/study/plans', body)
    return data
  }

  async getCompletions(planId: number): Promise<ApiResponse<DailyCompletion[]>> {
    const { data } = await httpClient.get(`/study/plans/${planId}/completions`)
    return data
  }

  async createCompletion(planId: number, body: CreateCompletionRequest): Promise<ApiResponse<DailyCompletion>> {
    const { data } = await httpClient.post(`/study/plans/${planId}/completions`, body)
    return data
  }

  async getForgettingPoints(planId: number): Promise<ApiResponse<ForgettingPoint[]>> {
    const { data } = await httpClient.get(`/study/plans/${planId}/forgetting-points`)
    return data
  }

  async addForgettingPoint(planId: number, body: CreateForgettingPointRequest): Promise<ApiResponse<ForgettingPoint>> {
    const { data } = await httpClient.post(`/study/plans/${planId}/forgetting-points`, body)
    return data
  }

  async reviewForgettingPoint(id: number, body: ReviewRequest): Promise<ApiResponse<ForgettingPoint>> {
    const { data } = await httpClient.post(`/study/forgetting-points/${id}/review`, body)
    return data
  }

  // Operations
  async getReceipts(params?: PaginationParams): Promise<ApiResponse<ReceivingReceipt[]>> {
    const { data } = await httpClient.get('/inbound/receipts', { params: paginationQuery(params) })
    return data
  }

  async getReceipt(id: number): Promise<ApiResponse<ReceivingReceipt>> {
    const { data } = await httpClient.get(`/inbound/receipts/${id}`)
    return data
  }

  async createReceipt(body: { receiptType: ReceiptType; referenceNumber?: string; supplierName?: string; expectedDate?: string }): Promise<ApiResponse<ReceivingReceipt>> {
    const { data } = await httpClient.post('/inbound/receipts', body)
    return data
  }

  async transitionReceipt(id: number, body: TransitionRequest): Promise<ApiResponse<void>> {
    const { data } = await httpClient.post(`/inbound/receipts/${id}/transition`, body)
    return data
  }

  async postReceipt(id: number): Promise<ApiResponse<void>> {
    const { data } = await httpClient.post(`/inbound/receipts/${id}/post`)
    return data
  }

  async unpostReceipt(id: number): Promise<ApiResponse<void>> {
    const { data } = await httpClient.post(`/inbound/receipts/${id}/unpost`)
    return data
  }

  async addLine(receiptId: number, body: { itemCode: string; itemName: string; expectedQty: number; unitCost?: number }): Promise<ApiResponse<InboundLine>> {
    const { data } = await httpClient.post(`/inbound/receipts/${receiptId}/lines`, body)
    return data
  }

  async receiveLine(receiptId: number, body: { lineId: number; receivedQty: number }): Promise<ApiResponse<InboundLine>> {
    const { data } = await httpClient.post(`/inbound/receipts/${receiptId}/receive`, body)
    return data
  }

  async createInspection(body: InspectionRequest): Promise<ApiResponse<InboundLine>> {
    const { data } = await httpClient.post(`/inbound/receipts/${body.receiptId}/inspection`, body)
    return data
  }

  async createPutaway(body: PutawayRequest): Promise<ApiResponse<PutawayTask[]>> {
    const { data } = await httpClient.post(`/inbound/receipts/${body.receiptId}/putaway`, body)
    return data
  }

  async getDiscrepancies(receiptId: number): Promise<ApiResponse<Discrepancy[]>> {
    const { data } = await httpClient.get(`/inbound/receipts/${receiptId}/discrepancies`)
    return data
  }

  async getPutawayTasks(receiptId: number): Promise<ApiResponse<PutawayTask[]>> {
    const { data } = await httpClient.get(`/inbound/receipts/${receiptId}/putaway`)
    return data
  }

  async supervisorReview(body: SupervisorReviewRequest): Promise<ApiResponse<void>> {
    const { data } = await httpClient.post(`/inbound/receipts/${body.receiptId}/supervisor-review`, {
      discrepancyId: body.discrepancyId,
      reasonCode: body.reasonCode,
      notes: body.notes,
    })
    return data
  }

  // Master Data
  private pluralize(type: string): string {
    if (type.endsWith('s')) return type
    if (type.endsWith('ss') || type.endsWith('x') || type.endsWith('ch') || type.endsWith('sh')) return `${type}es`
    return `${type}s`
  }

  async getItems(type?: string, params?: PaginationParams): Promise<ApiResponse<MasterDataItem[]>> {
    const endpoint = type ? `/admin/${this.pluralize(type)}` : '/admin/terms'
    const { data } = await httpClient.get(endpoint, { params: paginationQuery(params) })
    return data
  }

  async createItem(type: string, body: Record<string, any>): Promise<ApiResponse<MasterDataItem>> {
    const { data } = await httpClient.post(`/admin/${this.pluralize(type)}`, body)
    return data
  }

  async updateItem(type: string, id: number, body: Record<string, any>): Promise<ApiResponse<MasterDataItem>> {
    const { data } = await httpClient.put(`/admin/${this.pluralize(type)}/${id}`, body)
    return data
  }

  async deleteItem(type: string, id: number): Promise<ApiResponse<void>> {
    const { data } = await httpClient.delete(`/admin/${this.pluralize(type)}/${id}`)
    return data
  }

  async importFile(file: File, type: string): Promise<ApiResponse<ImportResult>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('entityType', type)
    const { data } = await httpClient.post('/admin/master-data/imports', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  }

  async exportItems(type: string): Promise<ApiResponse<Blob>> {
    const { data } = await httpClient.get(`/admin/master-data/export`, {
      params: { entityType: type },
      responseType: 'blob',
    })
    return data
  }

  async getMergeCandidate(type: string): Promise<ApiResponse<MergeCandidate[]>> {
    const { data } = await httpClient.get('/admin/master-data/merge', { params: { entityType: type } })
    return data
  }

  async executeMerge(entityType: string, sourceId: number, targetId: number): Promise<ApiResponse<MasterDataItem>> {
    const { data } = await httpClient.post('/admin/master-data/merge', { entityType, sourceId, targetId })
    return data
  }

  async getHistory(entityType?: string, entityId?: number): Promise<ApiResponse<ChangeHistoryEntry[]>> {
    const { data } = await httpClient.get('/admin/master-data/history', {
      params: { ...(entityType ? { entityType } : {}), ...(entityId ? { entityId } : {}) },
    })
    return data
  }

  // Notifications
  async getNotifications(params?: PaginationParams): Promise<ApiResponse<PaginatedResponse<Notification>>> {
    const { data } = await httpClient.get('/notifications', { params: paginationQuery(params) })
    return data
  }

  async createNotification(body: { type: string; title: string; body?: string; targetUserIds: number[] }): Promise<ApiResponse<Notification>> {
    const { data } = await httpClient.post('/notifications', body)
    return data
  }

  async markRead(id: number): Promise<ApiResponse<void>> {
    const { data } = await httpClient.post(`/notifications/${id}/read`)
    return data
  }

  async getDeliveryStatus(notificationId: number): Promise<ApiResponse<unknown>> {
    const { data } = await httpClient.get(`/notifications/${notificationId}/status`)
    return data
  }

  // Exports
  async requestExport(body: ExportRequest): Promise<ApiResponse<ExportResult>> {
    const { data } = await httpClient.post('/exports/account', body)
    return data
  }

  async listExports(): Promise<ApiResponse<ExportResult[]>> {
    const { data } = await httpClient.get('/exports')
    return data
  }

  async downloadExport(id: number): Promise<ApiResponse<Blob>> {
    const { data } = await httpClient.get(`/exports/${id}/download`, { responseType: 'blob' })
    return data
  }

  async importAccount(importData: unknown): Promise<ApiResponse<string>> {
    const { data } = await httpClient.post('/imports/account', importData)
    return data
  }

  async importAccountFile(file: File, password: string): Promise<ApiResponse<string>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('password', password)
    const { data } = await httpClient.post('/imports/account/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  }

  async deleteAccount(password: string): Promise<ApiResponse<void>> {
    const { data } = await httpClient.delete('/account', { data: { password } })
    return data
  }

  // Dashboard
  async getDashboard(): Promise<ApiResponse<DashboardData>> {
    const { data } = await httpClient.get('/dashboard')
    return data
  }

  // Admin
  async getPerformanceMetrics(): Promise<ApiResponse<PerformanceMetrics>> {
    const { data } = await httpClient.get('/admin/performance')
    return data
  }
}
