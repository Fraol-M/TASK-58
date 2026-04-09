import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { authGuard, roleGuard } from './guards'

const routes: RouteRecordRaw[] = [
  // Auth routes
  {
    path: '/sign-in',
    name: 'signIn',
    component: () => import('@/modules/auth/pages/SignInPage.vue'),
    meta: { title: 'Sign In', layout: 'auth' },
  },
  {
    path: '/sign-up',
    name: 'signUp',
    component: () => import('@/modules/auth/pages/SignUpPage.vue'),
    meta: { title: 'Create Account', layout: 'auth' },
  },

  // Dashboard
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/modules/dashboard/pages/DashboardPage.vue'),
    meta: { title: 'Dashboard', requiresAuth: true },
  },

  // Notifications
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('@/modules/notifications/pages/NotificationsPage.vue'),
    meta: { title: 'Notifications', requiresAuth: true },
  },

  // Profile
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/modules/profile/pages/ProfilePage.vue'),
    meta: { title: 'Profile', requiresAuth: true },
  },

  // Exports
  {
    path: '/exports',
    name: 'exports',
    component: () => import('@/modules/exports/pages/ExportsPage.vue'),
    meta: { title: 'Exports', requiresAuth: true },
  },

  // Fitness (regular user)
  {
    path: '/fitness/assessment',
    name: 'fitnessAssessment',
    component: () => import('@/modules/fitness/pages/AssessmentPage.vue'),
    meta: { title: 'Fitness Assessment', requiresAuth: true, requiredRole: 'REGULAR_USER' },
  },
  {
    path: '/fitness/goals',
    name: 'fitnessGoals',
    component: () => import('@/modules/fitness/pages/GoalsPage.vue'),
    meta: { title: 'Fitness Goals', requiresAuth: true, requiredRole: 'REGULAR_USER' },
  },
  {
    path: '/fitness/check-ins',
    name: 'fitnessCheckIns',
    component: () => import('@/modules/fitness/pages/CheckInsPage.vue'),
    meta: { title: 'Check-ins', requiresAuth: true, requiredRole: 'REGULAR_USER' },
  },

  // Study (regular user)
  {
    path: '/study/plans',
    name: 'studyPlans',
    component: () => import('@/modules/study/pages/PlansPage.vue'),
    meta: { title: 'Study Plans', requiresAuth: true, requiredRole: 'REGULAR_USER' },
  },
  {
    path: '/study/review',
    name: 'studyReview',
    component: () => import('@/modules/study/pages/ReviewPage.vue'),
    meta: { title: 'Study Review', requiresAuth: true, requiredRole: 'REGULAR_USER' },
  },
  {
    path: '/study/history',
    name: 'studyHistory',
    component: () => import('@/modules/study/pages/HistoryPage.vue'),
    meta: { title: 'Study History', requiresAuth: true, requiredRole: 'REGULAR_USER' },
  },

  // Operations (accessible to both OPERATIONS_STAFF and ADMIN — admin needs
  // access for supervisor review of discrepancies)
  {
    path: '/operations/receiving',
    name: 'opsReceiving',
    component: () => import('@/modules/operations/pages/ReceivingListPage.vue'),
    meta: { title: 'Receiving', requiresAuth: true, requiredRole: ['OPERATIONS_STAFF', 'ADMIN'] },
  },
  {
    path: '/operations/receiving/:receiptId',
    name: 'opsReceiptDetail',
    component: () => import('@/modules/operations/pages/ReceivingDetailPage.vue'),
    meta: { title: 'Receipt Detail', requiresAuth: true, requiredRole: ['OPERATIONS_STAFF', 'ADMIN'] },
  },
  {
    path: '/operations/discrepancies',
    name: 'opsDiscrepancies',
    component: () => import('@/modules/operations/pages/DiscrepanciesPage.vue'),
    meta: { title: 'Discrepancies', requiresAuth: true, requiredRole: ['OPERATIONS_STAFF', 'ADMIN'] },
  },
  {
    path: '/operations/putaway',
    name: 'opsPutaway',
    component: () => import('@/modules/operations/pages/PutawayPage.vue'),
    meta: { title: 'Putaway', requiresAuth: true, requiredRole: ['OPERATIONS_STAFF', 'ADMIN'] },
  },

  // Admin
  {
    path: '/admin/master-data',
    name: 'adminMasterData',
    component: () => import('@/modules/master-data/pages/MasterDataPage.vue'),
    meta: { title: 'Master Data', requiresAuth: true, requiredRole: 'ADMIN' },
  },
  {
    path: '/admin/master-data/import',
    name: 'adminMasterDataImport',
    component: () => import('@/modules/master-data/pages/ImportPage.vue'),
    meta: { title: 'Import Data', requiresAuth: true, requiredRole: 'ADMIN' },
  },
  {
    path: '/admin/master-data/merge',
    name: 'adminMasterDataMerge',
    component: () => import('@/modules/master-data/pages/MergePage.vue'),
    meta: { title: 'Merge Data', requiresAuth: true, requiredRole: 'ADMIN' },
  },
  {
    path: '/admin/master-data/history',
    name: 'adminMasterDataHistory',
    component: () => import('@/modules/master-data/pages/HistoryPage.vue'),
    meta: { title: 'Change History', requiresAuth: true, requiredRole: 'ADMIN' },
  },
  {
    path: '/admin/performance',
    name: 'adminPerformance',
    component: () => import('@/modules/admin/pages/PerformancePage.vue'),
    meta: { title: 'Performance', requiresAuth: true, requiredRole: 'ADMIN' },
  },

  // Redirects
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  // Set document title
  document.title = to.meta.title ? `${to.meta.title} - CampusFit` : 'CampusFit'

  if (to.meta.requiresAuth) {
    const allowed = authGuard()
    if (!allowed) {
      return next({ path: '/sign-in', query: { redirect: to.fullPath } })
    }

    if (to.meta.requiredRole) {
      const hasRole = roleGuard(to.meta.requiredRole)
      if (!hasRole) {
        return next('/dashboard')
      }
    }
  }

  next()
})

export default router
