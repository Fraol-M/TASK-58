<script setup lang="ts">
import { RouterLink } from 'vue-router'

interface Props {
  label: string
  to: string
  icon?: string
  badge?: number
}

defineProps<Props>()
</script>

<template>
  <RouterLink :to="to" class="sidebar-item" active-class="sidebar-item--active">
    <span v-if="icon" class="sidebar-item__icon">{{ icon }}</span>
    <span class="sidebar-item__label">{{ label }}</span>
    <span v-if="badge && badge > 0" class="sidebar-item__badge">{{ badge > 99 ? '99+' : badge }}</span>
  </RouterLink>
</template>

<style scoped>
.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 14px;
  color: #6b7280;
  text-decoration: none;
  transition: background-color 0.15s, color 0.15s;
}

.sidebar-item:hover {
  background-color: #f3f4f6;
  color: #111827;
}

.sidebar-item--active {
  background-color: #eef2ff;
  color: #4f46e5;
  font-weight: 500;
}

.sidebar-item__icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}

.sidebar-item__label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-item__badge {
  background: #dc2626;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 9999px;
  min-width: 18px;
  text-align: center;
  line-height: 1.5;
}
</style>
