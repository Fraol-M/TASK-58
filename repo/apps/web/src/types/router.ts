import 'vue-router'
import type { UserRole } from './user'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiredRole?: UserRole | UserRole[]
    title?: string
    layout?: 'default' | 'auth'
  }
}
