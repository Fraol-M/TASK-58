<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  topic: string
  nextReviewDate: string
  intervalDays: number
}

const props = defineProps<Props>()

const urgency = computed(() => {
  const now = new Date()
  const review = new Date(props.nextReviewDate)
  const diffDays = Math.ceil((review.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return 'overdue'
  if (diffDays <= 7) return 'soon'
  return 'safe'
})

const colorClass = computed(() => {
  if (urgency.value === 'overdue') return 'fp-tag--danger'
  if (urgency.value === 'soon') return 'fp-tag--warning'
  return 'fp-tag--success'
})
</script>

<template>
  <div :class="['fp-tag', colorClass]">
    <span class="fp-tag__topic">{{ topic }}</span>
    <span class="fp-tag__meta">
      Next: {{ nextReviewDate }} | {{ intervalDays }}d interval
    </span>
  </div>
</template>

<style scoped>
.fp-tag {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid;
}

.fp-tag--success {
  background: #f0fdf4;
  border-color: #059669;
}

.fp-tag--warning {
  background: #fffbeb;
  border-color: #d97706;
}

.fp-tag--danger {
  background: #fef2f2;
  border-color: #dc2626;
}

.fp-tag__topic {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.fp-tag__meta {
  font-size: 12px;
  color: #6b7280;
}
</style>
