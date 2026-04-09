import type { ApiResponse, PaginatedResponse, PaginationParams } from '@/types/api'
import type { User } from '@/types/user'
import type { LoginResponse } from '@/modules/auth/types'

/* ---- Auth ---- */
export interface SignInRequest {
  username: string
  password: string
}

export interface SignUpRequest {
  username: string
  password: string
  email?: string
}

/* ---- Fitness ---- */
export interface FitnessAssessment {
  id: number
  userId: number
  assessmentType: string
  heightFeet: number
  heightInches: number
  formattedHeight: string
  weightLbs: number
  bmi?: number
  bodyFatPercent?: number
  waistInches?: number
  chestInches?: number
  armInches?: number
  assessmentDate: string
  notes?: string
  createdAt: string
  updatedAt?: string
}

export interface FitnessGoal {
  id: number
  userId: number
  assessmentId?: number
  goalType: string
  description?: string
  targetValue: number
  startValue?: number
  currentValue?: number
  unit: string
  startDate: string
  targetDate: string
  status: 'ACTIVE' | 'ACHIEVED' | 'RECALCULATED' | 'ABANDONED'
  missedCheckIns: number
  progressPercentage: number
  milestones: FitnessMilestone[]
  createdAt: string
  updatedAt?: string
}

export interface FitnessMilestone {
  id: number
  description: string
  targetValue: number
  achievedDate?: string
  seq: number
}

export interface FitnessCheckIn {
  id: number
  goalId: number
  userId: number
  weekNumber: number
  value: number
  notes?: string
  createdAt: string
}

export interface CreateGoalRequest {
  goalType: string
  description?: string
  targetValue: number
  unit: string
  startDate: string
  targetDate: string
}

export interface CreateCheckInRequest {
  goalId: number
  value: number
  notes?: string
}

/* ---- Study ---- */
export interface StudyPlan {
  id: number
  userId: number
  termId?: number
  schoolId?: number
  majorId?: number
  classId?: number
  courseId?: number
  title: string
  description?: string
  status: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED'
  createdAt: string
  updatedAt?: string
}

export interface DailyCompletion {
  id: number
  planId: number
  itemId?: number
  completedDate: string
  completed: boolean
  notes?: string
  createdAt: string
}

export interface ForgettingPoint {
  id: number
  planId: number
  topic: string
  description?: string
  nextReviewDate: string
  easeFactor: number
  intervalDays: number
  repetitions: number
  createdAt: string
  updatedAt?: string
}

export interface CreatePlanRequest {
  title: string
  description?: string
  termId?: number
  schoolId?: number
  majorId?: number
  classId?: number
  courseId?: number
}

export interface CreateCompletionRequest {
  itemId?: number
  completedDate: string
  completed: boolean
  notes?: string
}

export interface CreateForgettingPointRequest {
  topic: string
  description?: string
}

export interface ReviewRequest {
  quality: number
}

/* ---- Operations ---- */
export type ReceiptType = 'PURCHASE' | 'TRANSFER' | 'RETURN'

export type ReceiptStatus =
  | 'DRAFT'
  | 'RECEIVING'
  | 'INSPECTION'
  | 'PUTAWAY'
  | 'COMPLETED'
  | 'REJECTED'
  | 'POSTED'
  | 'UNPOSTED'

export interface InboundLine {
  id: number
  receiptId: number
  itemCode: string
  itemName: string
  expectedQty: number
  receivedQty?: number
  inspectedQty?: number
  unitCost?: number
  inspectionResult?: string
  inspectionNotes?: string
}

export interface ReceivingReceipt {
  id: number
  receiptNumber: string
  receiptType: ReceiptType
  referenceNumber?: string
  supplierName?: string
  status: ReceiptStatus
  expectedDate?: string
  receivedDate?: string
  createdBy: number
  supervisorApprovalRequired: boolean
  supervisorApprovedBy?: number
  lines: InboundLine[]
  createdAt: string
  updatedAt?: string
}

export interface PutawayTask {
  id: number
  receiptId: number
  lineId: number
  suggestedLocation: string
  actualLocation?: string
  status: 'PENDING' | 'COMPLETED'
  completedBy?: number
  completedAt?: string
}

export interface TransitionRequest {
  targetState: ReceiptStatus
  reason?: string
}

export interface InspectionRequest {
  receiptId: number
  lineId: number
  inspectedQty: number
  result: 'PASS' | 'FAIL'
  notes?: string
}

export interface PutawayRequest {
  receiptId: number
  taskId: number
  actualLocation?: string
}

export interface SupervisorReviewRequest {
  receiptId: number
  discrepancyId: number
  reasonCode: string
  notes?: string
}

export interface Discrepancy {
  id: number
  receiptId: number
  lineId: number
  discrepancyType: 'QUANTITY' | 'QUALITY' | 'WRONG_ITEM'
  expectedValue: number
  actualValue: number
  variancePercent: number
  reasonCode?: string
  supervisorRequired: boolean
  resolvedBy?: number
  resolvedAt?: string
  notes?: string
  createdAt: string
  resolved: boolean
}

/* ---- Master Data ---- */
export interface MasterDataItem {
  id: number
  code: string
  name: string
  effectiveFrom: string
  effectiveTo?: string
  startDate?: string
  endDate?: string
  schoolId?: number
  majorId?: number
  classId?: number
  termId?: number
  year?: number
  credits?: number
  active: boolean
  createdAt?: string
  updatedAt?: string
}

export interface AddLineRequest {
  itemCode: string
  itemName: string
  expectedQty: number
  unitCost?: number
}

export interface ReceiveLineRequest {
  lineId: number
  receivedQty: number
}

export interface ImportResult {
  id: number
  fileName: string
  entityType: string
  totalRows: number
  successCount: number
  errorCount: number
  status: string
  errors: { rowNumber: number; field: string; message: string; rawValue?: string }[]
  createdAt?: string
  completedAt?: string
}

export interface MergeCandidate {
  id: number
  sourceItem: MasterDataItem
  targetItem: MasterDataItem
  similarity: number
}

export interface ChangeHistoryEntry {
  id: number
  entityType: string
  entityId: number
  action?: string
  fieldName: string
  oldValue: string
  newValue: string
  changedBy: number
  changedAt: string
  changes?: Record<string, { oldValue: string; newValue: string }>
}

/* ---- Notifications ---- */
export interface Notification {
  id: number
  notificationId: number
  type: string
  title: string
  body?: string
  read: boolean
  readAt?: string
  createdAt: string
}

export interface NotificationStatus {
  total: number
  unread: number
}

/* ---- Exports ---- */
export interface ExportRequest {
  exportType: 'ACCOUNT_DATA' | 'STUDY_DATA' | 'FITNESS_DATA'
  passwordProtected: boolean
  exportPassword?: string
}

export interface ExportResult {
  id: number
  userId: number
  exportType: string
  status: string
  filePath?: string
  downloadUrl?: string
  passwordProtected: boolean
  expiresAt?: string
  createdAt?: string
  completedAt?: string
}

/* ---- Dashboard ---- */
export interface DashboardMetricEntry {
  key: string
  value: number
  dimension?: string
  periodType?: string
  periodValue?: string
}

export interface DashboardActivity {
  label: string
  value: string | number
}

export interface DashboardData {
  userRole: string
  metrics: DashboardMetricEntry[]
  summary: Record<string, string | number>
  recentActivity: DashboardActivity[]
  charts: Record<string, number[]>
}

/* ---- Admin ---- */
export type PerformanceMetrics = Record<string, string | number | null>[]

/* ---- Adapter Interface ---- */
export interface ApiAdapter {
  // Auth
  signIn(payload: SignInRequest): Promise<ApiResponse<LoginResponse>>
  signUp(payload: SignUpRequest): Promise<ApiResponse<User>>
  signOut(): Promise<ApiResponse<void>>
  getMe(): Promise<ApiResponse<User>>

  // Fitness
  getAssessment(): Promise<ApiResponse<FitnessAssessment>>
  saveAssessment(data: Partial<FitnessAssessment>): Promise<ApiResponse<FitnessAssessment>>
  getGoals(params?: PaginationParams): Promise<ApiResponse<FitnessGoal[]>>
  createGoal(data: CreateGoalRequest): Promise<ApiResponse<FitnessGoal>>
  getGoal(id: number): Promise<ApiResponse<FitnessGoal>>
  createCheckIn(data: CreateCheckInRequest): Promise<ApiResponse<FitnessCheckIn>>

  // Study
  getPlans(params?: PaginationParams): Promise<ApiResponse<StudyPlan[]>>
  createPlan(data: CreatePlanRequest): Promise<ApiResponse<StudyPlan>>
  getCompletions(planId: number): Promise<ApiResponse<DailyCompletion[]>>
  createCompletion(planId: number, data: CreateCompletionRequest): Promise<ApiResponse<DailyCompletion>>
  getForgettingPoints(planId: number): Promise<ApiResponse<ForgettingPoint[]>>
  addForgettingPoint(planId: number, data: CreateForgettingPointRequest): Promise<ApiResponse<ForgettingPoint>>
  reviewForgettingPoint(id: number, data: ReviewRequest): Promise<ApiResponse<ForgettingPoint>>

  // Operations
  getReceipts(params?: PaginationParams): Promise<ApiResponse<ReceivingReceipt[]>>
  getReceipt(id: number): Promise<ApiResponse<ReceivingReceipt>>
  createReceipt(data: { receiptType: ReceiptType; referenceNumber?: string; supplierName?: string; expectedDate?: string }): Promise<ApiResponse<ReceivingReceipt>>
  transitionReceipt(id: number, data: TransitionRequest): Promise<ApiResponse<void>>
  postReceipt(id: number): Promise<ApiResponse<void>>
  unpostReceipt(id: number): Promise<ApiResponse<void>>
  addLine(receiptId: number, data: AddLineRequest): Promise<ApiResponse<InboundLine>>
  receiveLine(receiptId: number, data: ReceiveLineRequest): Promise<ApiResponse<InboundLine>>
  createInspection(data: InspectionRequest): Promise<ApiResponse<InboundLine>>
  createPutaway(data: PutawayRequest): Promise<ApiResponse<PutawayTask[]>>
  getDiscrepancies(receiptId: number): Promise<ApiResponse<Discrepancy[]>>
  getPutawayTasks(receiptId: number): Promise<ApiResponse<PutawayTask[]>>
  supervisorReview(data: SupervisorReviewRequest): Promise<ApiResponse<void>>

  // Master Data
  getItems(type?: string, params?: PaginationParams): Promise<ApiResponse<MasterDataItem[]>>
  createItem(type: string, data: Record<string, any>): Promise<ApiResponse<MasterDataItem>>
  updateItem(type: string, id: number, data: Record<string, any>): Promise<ApiResponse<MasterDataItem>>
  deleteItem(type: string, id: number): Promise<ApiResponse<void>>
  importFile(file: File, type: string): Promise<ApiResponse<ImportResult>>
  exportItems(type: string): Promise<ApiResponse<Blob>>
  getMergeCandidate(type: string): Promise<ApiResponse<MergeCandidate[]>>
  executeMerge(entityType: string, sourceId: number, targetId: number): Promise<ApiResponse<MasterDataItem>>
  getHistory(entityType?: string, entityId?: number): Promise<ApiResponse<ChangeHistoryEntry[]>>

  // Notifications
  getNotifications(params?: PaginationParams): Promise<ApiResponse<PaginatedResponse<Notification>>>
  createNotification(data: { type: string; title: string; body?: string; targetUserIds: number[] }): Promise<ApiResponse<Notification>>
  markRead(id: number): Promise<ApiResponse<void>>
  getDeliveryStatus(notificationId: number): Promise<ApiResponse<unknown>>

  // Exports
  requestExport(data: ExportRequest): Promise<ApiResponse<ExportResult>>
  listExports(): Promise<ApiResponse<ExportResult[]>>
  downloadExport(id: number): Promise<ApiResponse<Blob>>
  importAccount(importData: unknown): Promise<ApiResponse<string>>
  importAccountFile(file: File, password: string): Promise<ApiResponse<string>>
  deleteAccount(password: string): Promise<ApiResponse<void>>

  // Dashboard
  getDashboard(): Promise<ApiResponse<DashboardData>>

  // Admin
  getPerformanceMetrics(): Promise<ApiResponse<PerformanceMetrics>>
}
