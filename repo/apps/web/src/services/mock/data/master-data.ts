import type {
  MasterDataItem,
  ImportResult,
  ChangeHistoryEntry,
  MergeCandidate,
} from '@/services/adapters/api-adapter.interface'

export const mockMasterDataItems: MasterDataItem[] = [
  { id: 1, code: 'F2026', name: 'Fall 2026', effectiveFrom: '2026-08-01', effectiveTo: '2026-12-15', active: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: 2, code: 'S2026', name: 'Spring 2026', effectiveFrom: '2026-01-15', effectiveTo: '2026-05-15', active: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: 3, code: 'SU2026', name: 'Summer 2026', effectiveFrom: '2026-06-01', effectiveTo: '2026-07-31', active: true, createdAt: '2026-01-01T00:00:00Z' },
  { id: 4, code: 'ENG', name: 'School of Engineering', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 5, code: 'SCI', name: 'School of Science', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 6, code: 'BUS', name: 'School of Business', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 7, code: 'CS', name: 'Computer Science', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 8, code: 'ME', name: 'Mechanical Engineering', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 9, code: 'BIO', name: 'Biology', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 10, code: 'CHEM', name: 'Chemistry', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 11, code: 'CS101', name: 'Introduction to Computer Science', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 12, code: 'CS201', name: 'Data Structures', effectiveFrom: '2020-01-01', active: true, createdAt: '2020-01-01T00:00:00Z' },
  { id: 13, code: 'CS101-F26-01', name: 'Intro to CS Section 01', effectiveFrom: '2026-08-01', effectiveTo: '2026-12-15', active: true, createdAt: '2026-03-01T00:00:00Z' },
  { id: 14, code: 'CS101-F26-02', name: 'Intro to CS Section 02', effectiveFrom: '2026-08-01', effectiveTo: '2026-12-15', active: true, createdAt: '2026-03-20T00:00:00Z' },
]

export const mockImportResult: ImportResult = {
  id: 1,
  fileName: 'master-data-import.csv',
  entityType: 'course',
  totalRows: 150,
  successCount: 147,
  errorCount: 3,
  status: 'COMPLETED',
  errors: [
    { rowNumber: 23, field: 'code', message: 'Duplicate code "CS101" found' },
    { rowNumber: 89, field: 'name', message: 'Missing required field "name"' },
    { rowNumber: 134, field: 'parentId', message: 'Invalid parent reference "INVALID_REF"', rawValue: 'INVALID_REF' },
  ],
  createdAt: '2026-04-03T14:00:00Z',
  completedAt: '2026-04-03T14:01:00Z',
}

export const mockMergeCandidates: MergeCandidate[] = [
  {
    id: 1,
    sourceItem: { id: 15, code: 'CSCI', name: 'Computer Sci', effectiveFrom: '2020-01-01', active: true },
    targetItem: { id: 7, code: 'CS', name: 'Computer Science', effectiveFrom: '2020-01-01', active: true },
    similarity: 0.92,
  },
  {
    id: 2,
    sourceItem: { id: 16, code: 'ENGR', name: 'Engineering School', effectiveFrom: '2020-01-01', active: true },
    targetItem: { id: 4, code: 'ENG', name: 'School of Engineering', effectiveFrom: '2020-01-01', active: true },
    similarity: 0.87,
  },
]

export const mockChangeHistory: ChangeHistoryEntry[] = [
  {
    id: 1,
    entityType: 'major',
    entityId: 7,
    fieldName: 'name',
    oldValue: 'Comp Science',
    newValue: 'Computer Science',
    changedBy: 3,
    changedAt: '2026-03-28T14:30:00Z',
  },
  {
    id: 2,
    entityType: 'term',
    entityId: 3,
    fieldName: 'name',
    oldValue: '',
    newValue: 'Summer 2026',
    changedBy: 3,
    changedAt: '2026-03-25T09:00:00Z',
  },
  {
    id: 3,
    entityType: 'course',
    entityId: 14,
    fieldName: 'name',
    oldValue: '',
    newValue: 'Intro to CS Section 02',
    changedBy: 3,
    changedAt: '2026-03-20T11:00:00Z',
  },
]
