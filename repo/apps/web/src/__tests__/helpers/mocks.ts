import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory, type Router, type RouteRecordRaw } from 'vue-router'
import type { User } from '@/types/user'
import { useAuthStore } from '@/modules/auth/store'

export function createMockUser(overrides: Partial<User> = {}): User {
  return {
    id: 1,
    username: 'testuser',
    email: 'test@campus.edu',
    roles: ['REGULAR_USER'],
    status: 'ACTIVE',
    ...overrides,
  }
}

export function createMockAuthStore(user: User | null = createMockUser()) {
  const pinia = createPinia()
  setActivePinia(pinia)

  const store = useAuthStore()
  if (user) {
    store.user = user
    store.token = 'mock-token'
  }

  return store
}

const defaultRoutes: RouteRecordRaw[] = [
  { path: '/', component: { template: '<div>Home</div>' } },
  { path: '/sign-in', component: { template: '<div>Sign In</div>' } },
  { path: '/dashboard', component: { template: '<div>Dashboard</div>' } },
]

export function createMockRouter(routes?: RouteRecordRaw[]): Router {
  return createRouter({
    history: createMemoryHistory(),
    routes: routes ?? defaultRoutes,
  })
}
