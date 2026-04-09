<script setup lang="ts">
interface Props {
  variant?: 'info' | 'success' | 'warning' | 'danger' | 'neutral'
  label: string
}

withDefaults(defineProps<Props>(), {
  variant: 'neutral',
})
</script>

<template>
  <span :class="['app-badge', `app-badge--${variant}`]">{{ label }}</span>
</template>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 9999px;
  line-height: 1.6;
  white-space: nowrap;
}

.app-badge--info {
  background-color: #dbeafe;
  color: #1e40af;
}

.app-badge--success {
  background-color: #d1fae5;
  color: #065f46;
}

.app-badge--warning {
  background-color: #fef3c7;
  color: #92400e;
}

.app-badge--danger {
  background-color: #fee2e2;
  color: #991b1b;
}

.app-badge--neutral {
  background-color: #f3f4f6;
  color: #4b5563;
}
</style>
