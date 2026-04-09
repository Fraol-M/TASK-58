export type ExportType = 'ACCOUNT_DATA' | 'STUDY_DATA' | 'FITNESS_DATA'
export type ExportStatus = 'pending' | 'processing' | 'completed' | 'failed'

export interface ExportJob {
  id: number
  exportType: ExportType
  status: ExportStatus
  createdAt: string
  completedAt?: string
  downloadUrl?: string
}
