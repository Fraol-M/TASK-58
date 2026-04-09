<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status: string
  label?: string
}

const props = defineProps<Props>()

const colorClass = computed(() => {
  const s = props.status.toLowerCase()
  if (['active', 'completed', 'success', 'approved'].includes(s)) return 'status--success'
  if (['pending', 'in_progress', 'processing'].includes(s)) return 'status--warning'
  if (['rejected', 'failed', 'error', 'locked', 'disabled', 'abandoned'].includes(s)) return 'status--danger'
  if (['inspection', 'putaway'].includes(s)) return 'status--info'
  return 'status--neutral'
})
</script>

<template>
  <span :class="['status-indicator', colorClass]">
    <span class="status-indicator__dot"></span>
    <span v-if="label" class="status-indicator__label">{{ label }}</span>
    <span v-else class="status-indicator__label">{{ status }}</span>
  </span>
</template>

<style scoped>
.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
}

.status-indicator__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status--success .status-indicator__dot {
  background-color: #059669;
}
.status--success .status-indicator__label {
  color: #059669;
}

.status--warning .status-indicator__dot {
  background-color: #d97706;
}
.status--warning .status-indicator__label {
  color: #d97706;
}

.status--danger .status-indicator__dot {
  background-color: #dc2626;
}
.status--danger .status-indicator__label {
  color: #dc2626;
}

.status--info .status-indicator__dot {
  background-color: #2563eb;
}
.status--info .status-indicator__label {
  color: #2563eb;
}

.status--neutral .status-indicator__dot {
  background-color: #9ca3af;
}
.status--neutral .status-indicator__label {
  color: #6b7280;
}
</style>
