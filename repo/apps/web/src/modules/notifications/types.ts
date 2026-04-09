export type NotificationType = 'ANNOUNCEMENT' | 'REMINDER' | 'FOLLOW_UP'

export interface Notification {
  id: number
  type: NotificationType
  title: string
  body: string
  readAt?: string
  createdAt: string
}
