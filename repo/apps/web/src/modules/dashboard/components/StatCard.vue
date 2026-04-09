<script setup lang="ts">
interface Props {
  label: string
  value: string | number
  subtitle?: string
  trend?: 'up' | 'down' | 'neutral'
  color?: string
}

withDefaults(defineProps<Props>(), {
  trend: 'neutral',
  color: '#4f46e5',
})
</script>

<template>
  <div class="stat-card">
    <div class="stat-card__header">
      <span class="stat-card__label">{{ label }}</span>
      <span
        v-if="trend !== 'neutral'"
        :class="['stat-card__trend', `stat-card__trend--${trend}`]"
      >
        <svg v-if="trend === 'up'" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <path d="M8 4l4 4H9v4H7V8H4l4-4z" />
        </svg>
        <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <path d="M8 12l-4-4h3V4h2v4h3l-4 4z" />
        </svg>
      </span>
    </div>
    <div class="stat-card__value" :style="{ color }">{{ value }}</div>
    <div v-if="subtitle" class="stat-card__subtitle">{{ subtitle }}</div>
  </div>
</template>

<style scoped>
.stat-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.stat-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.stat-card__label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.stat-card__trend {
  display: inline-flex;
  align-items: center;
}

.stat-card__trend--up {
  color: #059669;
}

.stat-card__trend--down {
  color: #dc2626;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-card__subtitle {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
