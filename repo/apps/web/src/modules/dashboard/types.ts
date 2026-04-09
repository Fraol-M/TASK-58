export interface DashboardStats {
  fitnessGoals: number
  studyStreak: number
  activeReceipts: number
  pendingNotifications: number
  recentCheckIns: number
  completedGoals: number
  activePlans: number
  pendingDiscrepancies: number
  putawayQueueSize: number
  totalUsers: number
  importJobs: number
  avgResponseTime: number
}

export interface StatCardData {
  label: string
  value: string | number
  trend?: 'up' | 'down' | 'neutral'
  icon?: string
  subtitle?: string
  color?: string
}
