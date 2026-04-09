export interface StudyPlan {
  id: number
  courseId?: number
  termId?: number
  title: string
  description?: string
  status: 'active' | 'completed' | 'paused'
  items: StudyPlanItem[]
  createdAt: string
}

export interface StudyPlanItem {
  id: number
  title: string
  description?: string
  dueDate?: string
  seq: number
}

export interface DailyCompletion {
  id: number
  completedDate: string
  completed: boolean
  notes?: string
}

export interface ForgettingPoint {
  id: number
  topic: string
  description: string
  nextReviewDate: string
  easeFactor: number
  intervalDays: number
  repetitions: number
}

export interface Streak {
  currentStreak: number
  longestStreak: number
  lastActiveDate: string
}

export interface AcademicHierarchy {
  terms: { value: string; label: string }[]
  schools: { value: string; label: string }[]
  majors: { value: string; label: string }[]
  classes: { value: string; label: string }[]
  courses: { value: string; label: string }[]
}
