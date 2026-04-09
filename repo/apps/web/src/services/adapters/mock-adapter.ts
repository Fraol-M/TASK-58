import type { ApiResponse, PaginatedResponse, PaginationParams } from '@/types/api'
import type { User } from '@/types/user'
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
  InboundLine,
  TransitionRequest,
  InspectionRequest,
  PutawayRequest,
  PutawayTask,
  SupervisorReviewRequest,
  Discrepancy,
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
import type { LoginResponse } from '@/modules/auth/types'
import { simulateDelay } from '@/services/mock/delay'
import { findMockUser, mockUsers } from '@/services/mock/data/users'
import { mockAssessment, mockGoals, mockCheckIns } from '@/services/mock/data/fitness'
import { mockStudyPlans, mockDailyCompletions, mockForgettingPoints } from '@/services/mock/data/study'
import { mockReceipts, mockPutawayTasks } from '@/services/mock/data/operations'
import {
  mockMasterDataItems,
  mockImportResult,
  mockMergeCandidates,
  mockChangeHistory,
} from '@/services/mock/data/master-data'
import { mockNotifications } from '@/services/mock/data/notifications'

function ok<T>(data: T): ApiResponse<T> {
  return { success: true, data }
}

function paginate<T>(items: T[], params?: PaginationParams): PaginatedResponse<T> {
  const page = params?.page ?? 0
  const size = params?.size ?? 10
  const start = page * size
  const content = items.slice(start, start + size)
  return {
    content,
    page,
    size,
    totalElements: items.length,
    totalPages: Math.ceil(items.length / size),
  }
}

let currentUserId: number | null = null
let nextGoalId = 100
let nextCheckInId = 100
let nextPlanId = 100
let nextCompletionId = 100
let nextForgettingPointId = 100
let nextReceiptId = 100
let nextLineId = 100
let nextNotificationId = 100
let nextExportId = 100

export class MockAdapter implements ApiAdapter {
  // Auth
  async signIn(payload: SignInRequest): Promise<ApiResponse<LoginResponse>> {
    await simulateDelay()
    const found = findMockUser(payload.username, payload.password)
    if (!found) {
      throw { isAxiosError: true, response: { status: 401, data: { message: 'Invalid username or password' } } }
    }
    currentUserId = found.id
    const { password: _, ...user } = found
    return ok({
      token: `mock-jwt-${user.id}`,
      user,
      expiresAt: '2026-04-08T00:00:00Z',
    })
  }

  async signUp(payload: SignUpRequest): Promise<ApiResponse<User>> {
    await simulateDelay()
    const exists = mockUsers.find((u) => u.username === payload.username)
    if (exists) {
      throw { isAxiosError: true, response: { status: 409, data: { message: 'Username already taken' } } }
    }
    const newUser: User = {
      id: mockUsers.length + 1,
      username: payload.username,
      email: payload.email ?? '',
      roles: ['REGULAR_USER'],
      status: 'ACTIVE',
    }
    return ok(newUser)
  }

  async signOut(): Promise<ApiResponse<void>> {
    await simulateDelay(100, 200)
    currentUserId = null
    return ok(undefined as unknown as void)
  }

  async getMe(): Promise<ApiResponse<User>> {
    await simulateDelay()
    if (!currentUserId) {
      throw { isAxiosError: true, response: { status: 401, data: { message: 'Not authenticated' } } }
    }
    const found = mockUsers.find((u) => u.id === currentUserId)!
    const { password: _, ...user } = found
    return ok(user)
  }

  // Fitness
  async getAssessment(): Promise<ApiResponse<FitnessAssessment>> {
    await simulateDelay()
    return ok(mockAssessment)
  }

  async saveAssessment(data: Partial<FitnessAssessment>): Promise<ApiResponse<FitnessAssessment>> {
    await simulateDelay()
    return ok({ ...mockAssessment, ...data })
  }

  async getGoals(_params?: PaginationParams): Promise<ApiResponse<FitnessGoal[]>> {
    await simulateDelay()
    return ok(mockGoals)
  }

  async createGoal(data: CreateGoalRequest): Promise<ApiResponse<FitnessGoal>> {
    await simulateDelay()
    const goal: FitnessGoal = {
      id: nextGoalId++,
      userId: currentUserId ?? 1,
      goalType: data.goalType,
      description: data.description,
      targetValue: data.targetValue,
      unit: data.unit,
      startDate: data.startDate,
      targetDate: data.targetDate,
      status: 'ACTIVE',
      missedCheckIns: 0,
      progressPercentage: 0,
      milestones: [],
      createdAt: new Date().toISOString(),
    }
    mockGoals.push(goal)
    return ok(goal)
  }

  async getGoal(id: number): Promise<ApiResponse<FitnessGoal>> {
    await simulateDelay()
    const goal = mockGoals.find((g) => g.id === id)
    if (!goal) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Goal not found' } } }
    }
    return ok(goal)
  }

  async createCheckIn(data: CreateCheckInRequest): Promise<ApiResponse<FitnessCheckIn>> {
    await simulateDelay()
    const checkIn: FitnessCheckIn = {
      id: nextCheckInId++,
      goalId: data.goalId,
      userId: currentUserId ?? 1,
      weekNumber: 1,
      value: data.value,
      notes: data.notes,
      createdAt: new Date().toISOString(),
    }
    mockCheckIns.push(checkIn)
    return ok(checkIn)
  }

  // Study
  async getPlans(_params?: PaginationParams): Promise<ApiResponse<StudyPlan[]>> {
    await simulateDelay()
    return ok(mockStudyPlans)
  }

  async createPlan(data: CreatePlanRequest): Promise<ApiResponse<StudyPlan>> {
    await simulateDelay()
    const plan: StudyPlan = {
      id: nextPlanId++,
      userId: currentUserId ?? 1,
      title: data.title,
      description: data.description,
      courseId: data.courseId,
      termId: data.termId,
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
    }
    mockStudyPlans.push(plan)
    return ok(plan)
  }

  async getCompletions(planId: number): Promise<ApiResponse<DailyCompletion[]>> {
    await simulateDelay()
    return ok(mockDailyCompletions.filter((c) => c.planId === planId))
  }

  async createCompletion(planId: number, data: CreateCompletionRequest): Promise<ApiResponse<DailyCompletion>> {
    await simulateDelay()
    const completion: DailyCompletion = {
      id: nextCompletionId++,
      planId,
      itemId: data.itemId,
      completedDate: data.completedDate,
      completed: data.completed,
      notes: data.notes,
      createdAt: new Date().toISOString(),
    }
    mockDailyCompletions.push(completion)
    return ok(completion)
  }

  async getForgettingPoints(planId: number): Promise<ApiResponse<ForgettingPoint[]>> {
    await simulateDelay()
    return ok(mockForgettingPoints.filter((fp) => fp.planId === planId))
  }

  async addForgettingPoint(planId: number, data: CreateForgettingPointRequest): Promise<ApiResponse<ForgettingPoint>> {
    await simulateDelay()
    const point: ForgettingPoint = {
      id: nextForgettingPointId++,
      planId,
      topic: data.topic,
      description: data.description,
      nextReviewDate: new Date(Date.now() + 86400000).toISOString().split('T')[0],
      easeFactor: 2.5,
      intervalDays: 1,
      repetitions: 0,
      createdAt: new Date().toISOString(),
    }
    mockForgettingPoints.push(point)
    return ok(point)
  }

  async reviewForgettingPoint(id: number, data: ReviewRequest): Promise<ApiResponse<ForgettingPoint>> {
    await simulateDelay()
    const point = mockForgettingPoints.find((fp) => fp.id === id)
    if (!point) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Forgetting point not found' } } }
    }
    // Simple SM-2 style update
    const quality = data.quality
    point.repetitions += 1
    point.easeFactor = Math.max(1.3, point.easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    point.intervalDays = Math.round(point.intervalDays * point.easeFactor)
    const next = new Date()
    next.setDate(next.getDate() + point.intervalDays)
    point.nextReviewDate = next.toISOString().split('T')[0]
    point.updatedAt = new Date().toISOString()
    return ok(point)
  }

  // Operations
  async getReceipts(_params?: PaginationParams): Promise<ApiResponse<ReceivingReceipt[]>> {
    await simulateDelay()
    return ok(mockReceipts)
  }

  async getReceipt(id: number): Promise<ApiResponse<ReceivingReceipt>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === id)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    return ok(receipt)
  }

  async createReceipt(data: { receiptType: ReceiptType; referenceNumber?: string; supplierName?: string; expectedDate?: string }): Promise<ApiResponse<ReceivingReceipt>> {
    await simulateDelay()
    const id = nextReceiptId++
    const receipt: ReceivingReceipt = {
      id,
      receiptNumber: `RCV-2026-${String(id).padStart(4, '0')}`,
      receiptType: data.receiptType,
      referenceNumber: data.referenceNumber,
      supplierName: data.supplierName,
      status: 'DRAFT',
      expectedDate: data.expectedDate,
      createdBy: currentUserId ?? 1,
      supervisorApprovalRequired: false,
      lines: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    mockReceipts.push(receipt)
    return ok(receipt)
  }

  async transitionReceipt(id: number, data: TransitionRequest): Promise<ApiResponse<void>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === id)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    receipt.status = data.targetState
    receipt.updatedAt = new Date().toISOString()
    return ok(undefined as unknown as void)
  }

  async postReceipt(id: number): Promise<ApiResponse<void>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === id)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    receipt.status = 'POSTED'
    receipt.updatedAt = new Date().toISOString()
    return ok(undefined as unknown as void)
  }

  async unpostReceipt(id: number): Promise<ApiResponse<void>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === id)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    receipt.status = 'UNPOSTED'
    receipt.updatedAt = new Date().toISOString()
    return ok(undefined as unknown as void)
  }

  async addLine(receiptId: number, data: { itemCode: string; itemName: string; expectedQty: number; unitCost?: number }): Promise<ApiResponse<InboundLine>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === receiptId)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    const line: InboundLine = {
      id: nextLineId++,
      receiptId,
      itemCode: data.itemCode,
      itemName: data.itemName,
      expectedQty: data.expectedQty,
      unitCost: data.unitCost,
    }
    receipt.lines.push(line)
    receipt.updatedAt = new Date().toISOString()
    return ok(line)
  }

  async receiveLine(receiptId: number, data: { lineId: number; receivedQty: number }): Promise<ApiResponse<InboundLine>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === receiptId)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    const line = receipt.lines.find((l) => l.id === data.lineId)
    if (!line) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Line not found' } } }
    }
    line.receivedQty = data.receivedQty
    receipt.updatedAt = new Date().toISOString()
    return ok(line)
  }

  async createInspection(data: InspectionRequest): Promise<ApiResponse<InboundLine>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === data.receiptId)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    const line = receipt.lines.find((l) => l.id === data.lineId)
    if (!line) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Line not found' } } }
    }
    line.inspectedQty = data.inspectedQty
    line.inspectionResult = data.result
    line.inspectionNotes = data.notes
    receipt.updatedAt = new Date().toISOString()
    return ok(line)
  }

  async createPutaway(data: PutawayRequest): Promise<ApiResponse<PutawayTask[]>> {
    await simulateDelay()
    const task = mockPutawayTasks.find((t) => t.id === data.taskId)
    if (task) {
      task.actualLocation = data.actualLocation ?? task.suggestedLocation
      task.status = 'COMPLETED'
      task.completedBy = currentUserId ?? 1
      task.completedAt = new Date().toISOString()
      return ok([task])
    }
    const newTask: PutawayTask = {
      id: data.taskId,
      receiptId: data.receiptId,
      lineId: 0,
      suggestedLocation: data.actualLocation ?? 'Unknown',
      actualLocation: data.actualLocation,
      status: 'COMPLETED',
      completedBy: currentUserId ?? 1,
      completedAt: new Date().toISOString(),
    }
    return ok([newTask])
  }

  async getDiscrepancies(receiptId: number): Promise<ApiResponse<Discrepancy[]>> {
    await simulateDelay()
    return ok([])
  }

  async supervisorReview(data: SupervisorReviewRequest): Promise<ApiResponse<void>> {
    await simulateDelay()
    const receipt = mockReceipts.find((r) => r.id === data.receiptId)
    if (!receipt) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Receipt not found' } } }
    }
    receipt.supervisorApprovedBy = currentUserId ?? 3
    receipt.updatedAt = new Date().toISOString()
    return ok(undefined as unknown as void)
  }

  async getPutawayTasks(_receiptId: number): Promise<ApiResponse<PutawayTask[]>> {
    await simulateDelay()
    return ok(mockPutawayTasks)
  }

  // Master Data
  async getItems(type?: string, _params?: PaginationParams): Promise<ApiResponse<MasterDataItem[]>> {
    await simulateDelay()
    const filtered = type ? mockMasterDataItems.filter((i) => i.code.startsWith(type.toUpperCase())) : mockMasterDataItems
    return ok(filtered)
  }

  async createItem(_type: string, data: Record<string, any>): Promise<ApiResponse<MasterDataItem>> {
    await simulateDelay()
    const item: MasterDataItem = {
      id: Date.now(),
      code: data.code,
      name: data.name,
      effectiveFrom: data.effectiveFrom,
      effectiveTo: data.effectiveTo,
      startDate: data.startDate,
      endDate: data.endDate,
      schoolId: data.schoolId,
      majorId: data.majorId,
      classId: data.classId,
      termId: data.termId,
      year: data.year,
      credits: data.credits,
      active: true,
      createdAt: new Date().toISOString(),
    }
    mockMasterDataItems.push(item)
    return ok(item)
  }

  async updateItem(_type: string, id: number, data: Record<string, any>): Promise<ApiResponse<MasterDataItem>> {
    await simulateDelay()
    const item = mockMasterDataItems.find(i => i.id === id)
    if (!item) throw { isAxiosError: true, response: { status: 404, data: { message: 'Item not found' } } }
    Object.assign(item, data, { updatedAt: new Date().toISOString() })
    return ok(item)
  }

  async deleteItem(_type: string, id: number): Promise<ApiResponse<void>> {
    await simulateDelay()
    const idx = mockMasterDataItems.findIndex(i => i.id === id)
    if (idx >= 0) mockMasterDataItems.splice(idx, 1)
    return ok(undefined as unknown as void)
  }

  async importFile(_file: File, _type: string): Promise<ApiResponse<ImportResult>> {
    await simulateDelay(500, 1000)
    return ok(mockImportResult)
  }

  async exportItems(_type: string): Promise<ApiResponse<Blob>> {
    await simulateDelay(300, 600)
    const blob = new Blob(['id,code,name\n1,CS,Computer Science\n'], { type: 'text/csv' })
    return ok(blob)
  }

  async getMergeCandidate(_type: string): Promise<ApiResponse<MergeCandidate[]>> {
    await simulateDelay()
    return ok(mockMergeCandidates)
  }

  async executeMerge(_entityType: string, _sourceId: number, targetId: number): Promise<ApiResponse<MasterDataItem>> {
    await simulateDelay()
    const target = mockMasterDataItems.find((i) => i.id === targetId)
    if (!target) {
      throw { isAxiosError: true, response: { status: 404, data: { message: 'Item not found' } } }
    }
    return ok(target)
  }

  async getHistory(_entityType?: string, _entityId?: number): Promise<ApiResponse<ChangeHistoryEntry[]>> {
    await simulateDelay()
    return ok(mockChangeHistory)
  }

  // Notifications
  async getNotifications(params?: PaginationParams): Promise<ApiResponse<PaginatedResponse<Notification>>> {
    await simulateDelay()
    return ok(paginate(mockNotifications, params))
  }

  async createNotification(data: { type: string; title: string; body?: string; targetUserIds: number[] }): Promise<ApiResponse<Notification>> {
    await simulateDelay()
    const id = nextNotificationId++
    const notification: Notification = {
      id,
      notificationId: 1000 + id,
      type: data.type,
      title: data.title,
      body: data.body,
      read: false,
      createdAt: new Date().toISOString(),
    }
    mockNotifications.push(notification)
    return ok(notification)
  }

  async markRead(id: number): Promise<ApiResponse<void>> {
    await simulateDelay(100, 200)
    const notif = mockNotifications.find((n) => n.id === id)
    if (notif) {
      notif.read = true
      notif.readAt = new Date().toISOString()
    }
    return ok(undefined as unknown as void)
  }

  async getDeliveryStatus(notificationId: number): Promise<ApiResponse<unknown>> {
    await simulateDelay()
    return ok({
      notificationId,
      delivered: true,
      deliveredAt: new Date().toISOString(),
      channel: 'in-app',
    })
  }

  // Exports
  async requestExport(data: ExportRequest): Promise<ApiResponse<ExportResult>> {
    await simulateDelay(300, 600)
    const id = nextExportId++
    return ok({
      id,
      userId: currentUserId ?? 1,
      exportType: data.exportType,
      status: 'COMPLETED',
      filePath: `/mock/export/campusfit-export-${id}.zip`,
      passwordProtected: data.passwordProtected,
      expiresAt: '2026-04-15T00:00:00Z',
      createdAt: new Date().toISOString(),
      completedAt: new Date().toISOString(),
    })
  }

  async listExports(): Promise<ApiResponse<ExportResult[]>> {
    await simulateDelay()
    return ok([
      {
        id: 1,
        userId: currentUserId ?? 1,
        exportType: 'ACCOUNT_DATA',
        status: 'COMPLETED',
        filePath: '/mock/export/campusfit-export-1.zip',
        passwordProtected: false,
        expiresAt: '2026-04-15T00:00:00Z',
        createdAt: '2026-04-01T10:00:00Z',
        completedAt: '2026-04-01T10:01:00Z',
      },
    ])
  }

  async downloadExport(_id: number): Promise<ApiResponse<Blob>> {
    await simulateDelay(200, 400)
    const blob = new Blob(['mock-export-data'], { type: 'application/zip' })
    return ok(blob)
  }

  async importAccount(_importData: unknown): Promise<ApiResponse<string>> {
    await simulateDelay(500, 1000)
    return ok('Import completed successfully. 42 records restored.')
  }

  async importAccountFile(_file: File, _password: string): Promise<ApiResponse<string>> {
    await simulateDelay(500, 1000)
    return ok('Import from file completed successfully. 42 records restored.')
  }

  async deleteAccount(_password: string): Promise<ApiResponse<void>> {
    await simulateDelay()
    return ok(undefined as unknown as void)
  }

  // Dashboard
  async getDashboard(): Promise<ApiResponse<DashboardData>> {
    await simulateDelay()
    return ok({
      userRole: 'REGULAR_USER',
      metrics: [
        { key: 'activeGoals', value: 2, dimension: 'fitness', periodType: 'current' },
        { key: 'completedGoals', value: 1, dimension: 'fitness', periodType: 'all-time' },
        { key: 'activeStudyPlans', value: 2, dimension: 'study', periodType: 'current' },
        { key: 'totalCheckIns', value: 4, dimension: 'fitness', periodType: 'all-time' },
        { key: 'streakDays', value: 12, dimension: 'engagement', periodType: 'current' },
      ],
      summary: {
        fitnessGoals: 2,
        studyStreak: 12,
        recentCheckIns: 4,
        pendingNotifications: 3,
        totalUsers: 1247,
        activePlans: 15,
        importJobs: 3,
        avgResponseTime: 42,
        activeReceipts: 5,
        pendingDiscrepancies: 2,
        putawayQueueSize: 8,
        operationsProcessed: 12,
      },
      recentActivity: [
        { label: 'Fitness check-in recorded', value: '5K in 27:15' },
        { label: 'Study review completed', value: 'Organic Chemistry II' },
        { label: 'Goal milestone reached', value: 'Run 5K under 30 min' },
      ],
      charts: {
        weeklyActivity: [3, 5, 2, 4, 6, 1, 3],
        goalProgress: [65, 40, 100],
        performance: [72, 89, 45, 91, 67, 83, 55],
        receiptsByStatus: [3, 5, 2, 1, 12],
      },
    })
  }

  // Admin
  async getPerformanceMetrics(): Promise<ApiResponse<PerformanceMetrics>> {
    await simulateDelay()
    return ok([
      { metric: 'activeUsers', value: 1247, unit: 'users' },
      { metric: 'totalAssessments', value: 3891, unit: 'assessments' },
      { metric: 'avgGoalCompletion', value: 0.68, unit: 'ratio' },
      { metric: 'studyPlanCount', value: 2156, unit: 'plans' },
      { metric: 'operationsProcessed', value: 892, unit: 'operations' },
      { metric: 'systemUptime', value: 99.97, unit: 'percent' },
    ])
  }
}
