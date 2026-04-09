import { computed } from 'vue'
import type { UserRole } from '@/types/user'
import { useAuthStore } from '@/modules/auth/store'
import { hasRole } from '@/utils/role-checks'

export function usePermission() {
  const authStore = useAuthStore()

  function hasPermission(requiredRole: UserRole | UserRole[]): boolean {
    const user = authStore.user
    if (!user) return false

    if (Array.isArray(requiredRole)) {
      return requiredRole.some((r) => hasRole(user, r))
    }
    return hasRole(user, requiredRole)
  }

  const isAuthorized = computed(() => authStore.isAuthenticated)

  return {
    hasPermission,
    isAuthorized,
  }
}
