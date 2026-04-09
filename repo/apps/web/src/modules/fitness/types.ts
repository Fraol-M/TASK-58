export type GoalType = 'WEIGHT_LOSS' | 'WEIGHT_GAIN' | 'FLEXIBILITY' | 'ENDURANCE' | 'STRENGTH'
export type GoalStatus = 'active' | 'completed' | 'abandoned'

export interface Assessment {
  id: number
  heightFeet: number
  heightInches: number
  weightLbs: number
  bodyFatPercent?: number
  waist?: number
  chest?: number
  arm?: number
  assessmentDate: string
  notes?: string
}

export interface Goal {
  id: number
  goalType: GoalType
  description: string
  targetValue: number
  startValue: number
  currentValue: number
  unit: string
  startDate: string
  targetDate: string
  status: GoalStatus
  milestones: Milestone[]
  progressPercent: number
}

export interface Milestone {
  id: number
  description: string
  targetValue: number
  achievedDate?: string
  seq: number
}

export interface CheckIn {
  id: number
  weekNumber: number
  value: number
  notes?: string
  createdAt: string
}

export interface GoalAdjustment {
  previousTarget: number
  newTarget: number
  reason: string
  createdAt: string
}

export interface AssessmentFormData {
  heightFeet: number
  heightInches: number
  weightLbs: number
  bodyFatPercent: string
  waist: string
  chest: string
  arm: string
  notes: string
}
