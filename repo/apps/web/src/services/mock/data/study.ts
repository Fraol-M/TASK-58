import type {
  StudyPlan,
  DailyCompletion,
  ForgettingPoint,
} from '@/services/adapters/api-adapter.interface'

export const mockStudyPlans: StudyPlan[] = [
  {
    id: 1,
    userId: 1,
    courseId: 13,
    termId: 1,
    title: 'Organic Chemistry II',
    description: 'Mon/Wed/Fri 2-4 PM study sessions',
    status: 'ACTIVE',
    createdAt: '2026-03-01T10:00:00Z',
  },
  {
    id: 2,
    userId: 1,
    courseId: 12,
    termId: 1,
    title: 'Data Structures & Algorithms',
    description: 'Tue/Thu 10 AM - 12 PM study sessions',
    status: 'ACTIVE',
    createdAt: '2026-03-05T10:00:00Z',
  },
  {
    id: 3,
    userId: 1,
    title: 'Introduction to Psychology',
    description: 'Daily 8-9 AM review',
    status: 'COMPLETED',
    createdAt: '2026-01-15T10:00:00Z',
  },
]

export const mockDailyCompletions: DailyCompletion[] = [
  { id: 1, planId: 1, completedDate: '2026-03-15', completed: true, notes: 'Good progress on reaction mechanisms.', createdAt: '2026-03-15T14:00:00Z' },
  { id: 2, planId: 1, completedDate: '2026-04-01', completed: true, notes: 'Struggled with NMR spectroscopy.', createdAt: '2026-04-01T14:00:00Z' },
  { id: 3, planId: 2, completedDate: '2026-03-20', completed: true, notes: 'Nailed tree traversal algorithms.', createdAt: '2026-03-20T11:00:00Z' },
  { id: 4, planId: 2, completedDate: '2026-04-03', completed: true, notes: 'Graph algorithms practice.', createdAt: '2026-04-03T11:00:00Z' },
]

export const mockForgettingPoints: ForgettingPoint[] = [
  { id: 1, planId: 1, topic: 'SN1 vs SN2 conditions', description: 'Need to review substitution reaction conditions', nextReviewDate: '2026-04-10', easeFactor: 2.5, intervalDays: 3, repetitions: 2, createdAt: '2026-03-15T14:00:00Z' },
  { id: 2, planId: 1, topic: 'Stereoisomer naming conventions', nextReviewDate: '2026-04-12', easeFactor: 2.3, intervalDays: 5, repetitions: 1, createdAt: '2026-03-15T14:00:00Z' },
  { id: 3, planId: 1, topic: 'NMR chemical shifts', nextReviewDate: '2026-04-08', easeFactor: 2.1, intervalDays: 1, repetitions: 0, createdAt: '2026-04-01T14:00:00Z' },
  { id: 4, planId: 1, topic: 'Splitting patterns', nextReviewDate: '2026-04-09', easeFactor: 2.2, intervalDays: 2, repetitions: 1, createdAt: '2026-04-01T14:00:00Z' },
  { id: 5, planId: 2, topic: 'AVL rotation cases', description: 'Single and double rotations for AVL trees', nextReviewDate: '2026-04-11', easeFactor: 2.5, intervalDays: 4, repetitions: 2, createdAt: '2026-03-20T11:00:00Z' },
  { id: 6, planId: 2, topic: 'Dijkstra edge relaxation', nextReviewDate: '2026-04-07', easeFactor: 2.0, intervalDays: 1, repetitions: 0, createdAt: '2026-04-03T11:00:00Z' },
  { id: 7, planId: 2, topic: 'BFS vs DFS use cases', nextReviewDate: '2026-04-08', easeFactor: 2.3, intervalDays: 2, repetitions: 1, createdAt: '2026-04-03T11:00:00Z' },
]
