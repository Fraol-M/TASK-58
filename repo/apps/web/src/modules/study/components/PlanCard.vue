<script setup lang="ts">
import type { StudyPlan } from '@/services/adapters/api-adapter.interface'
import AppBadge from '@/components/common/AppBadge.vue'
import { formatDate } from '@/utils/format-date'

interface Props {
  plan: StudyPlan
}

defineProps<Props>()

const emit = defineEmits<{
  click: [id: number]
}>()

function statusVariant(status: string): 'success' | 'warning' | 'neutral' {
  if (status === 'completed') return 'success'
  if (status === 'active') return 'warning'
  return 'neutral'
}
</script>

<template>
  <div class="plan-card" @click="emit('click', plan.id)">
    <div class="plan-card__header">
      <h3 class="plan-card__title">{{ plan.title }}</h3>
      <AppBadge :label="plan.status" :variant="statusVariant(plan.status)" />
    </div>
    <p v-if="plan.description" class="plan-card__schedule">{{ plan.description }}</p>
    <div class="plan-card__footer">
      <span class="plan-card__date">Created {{ formatDate(plan.createdAt, 'MMM DD, YYYY') }}</span>
    </div>
  </div>
</template>

<style scoped>
.plan-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: box-shadow 0.15s, border-color 0.15s;
}

.plan-card:hover {
  border-color: #4f46e5;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.1);
}

.plan-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.plan-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.plan-card__schedule {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px;
}

.plan-card__footer {
  font-size: 12px;
  color: #9ca3af;
}
</style>
