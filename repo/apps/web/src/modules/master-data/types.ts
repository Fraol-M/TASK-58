export type MasterDataEntity = 'term' | 'school' | 'major' | 'class' | 'course'

export interface Term {
  id: number
  code: string
  name: string
  effectiveFrom: string
  effectiveTo: string
  active: boolean
}

export interface School {
  id: number
  code: string
  name: string
  effectiveFrom: string
  effectiveTo: string
  active: boolean
}

export interface Major {
  id: number
  code: string
  name: string
  schoolId: number
  effectiveFrom: string
  effectiveTo: string
  active: boolean
}

export interface AcademicClass {
  id: number
  code: string
  name: string
  majorId: number
  effectiveFrom: string
  effectiveTo: string
  active: boolean
}

export interface Course {
  id: number
  code: string
  name: string
  classId: number
  effectiveFrom: string
  effectiveTo: string
  active: boolean
}

export interface ImportJob {
  id: number
  fileName: string
  entityType: MasterDataEntity
  totalRows: number
  successCount: number
  errorCount: number
  status: 'pending' | 'processing' | 'completed' | 'failed'
  errors: ImportError[]
}

export interface ImportError {
  rowNumber: number
  field: string
  message: string
  rawValue?: string
}

export interface MergeCandidate {
  sourceId: number
  targetId: number
  entityType: MasterDataEntity
  similarity: number
}

export interface ChangeHistoryEntry {
  entityType: string
  entityId: number
  field: string
  oldValue: string
  newValue: string
  changedBy: string
  changedAt: string
}
