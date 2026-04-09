<script setup lang="ts">
import type { UserRole } from '@/types/user'
import { usePermission } from '@/composables/usePermission'
import { computed } from 'vue'

interface Props {
  requiredRole: UserRole | UserRole[]
}

const props = defineProps<Props>()
const { hasPermission } = usePermission()

const allowed = computed(() => hasPermission(props.requiredRole))
</script>

<template>
  <slot v-if="allowed" />
  <div v-else class="permission-denied">
    <svg class="permission-denied__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0110 0v4" />
    </svg>
    <h3 class="permission-denied__title">Access Denied</h3>
    <p class="permission-denied__message">
      You do not have the required permissions to view this content.
    </p>
  </div>
</template>

<style scoped>
.permission-denied {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 48px 20px;
}

.permission-denied__icon {
  width: 48px;
  height: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.permission-denied__title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px;
}

.permission-denied__message {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  max-width: 320px;
}
</style>
