import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserRole } from '@/types/user'

export const useMockModeStore = defineStore('mockMode', () => {
  const isMockMode = computed(() => import.meta.env.VITE_MOCK_MODE === 'true')

  const mockUserRole = ref<UserRole>('REGULAR_USER')

  function switchMockUser(role: UserRole) {
    mockUserRole.value = role
  }

  return {
    isMockMode,
    mockUserRole,
    switchMockUser,
  }
})
