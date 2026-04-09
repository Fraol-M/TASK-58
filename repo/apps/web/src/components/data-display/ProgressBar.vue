<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  value: number
  variant?: 'primary' | 'success' | 'warning' | 'danger'
  showLabel?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  showLabel: true,
})

const clamped = computed(() => Math.max(0, Math.min(100, props.value)))
</script>

<template>
  <div class="progress-bar">
    <div class="progress-bar__track">
      <div
        :class="['progress-bar__fill', `progress-bar__fill--${variant}`]"
        :style="{ width: `${clamped}%` }"
      ></div>
    </div>
    <span v-if="showLabel" class="progress-bar__label">{{ clamped }}%</span>
  </div>
</template>

<style scoped>
.progress-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar__track {
  flex: 1;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar__fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.4s ease;
}

.progress-bar__fill--primary {
  background-color: #4f46e5;
}
.progress-bar__fill--success {
  background-color: #059669;
}
.progress-bar__fill--warning {
  background-color: #d97706;
}
.progress-bar__fill--danger {
  background-color: #dc2626;
}

.progress-bar__label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  min-width: 36px;
  text-align: right;
}
</style>
