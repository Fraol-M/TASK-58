<script setup lang="ts">
import type { FitnessGoal } from '@/services/adapters/api-adapter.interface'
import AppBadge from '@/components/common/AppBadge.vue'
import ProgressBar from '@/components/data-display/ProgressBar.vue'
import { formatDate } from '@/utils/format-date'

interface Props {
  goal: FitnessGoal
}

defineProps<Props>()

const emit = defineEmits<{
  click: [id: number]
}>()

function statusVariant(status: string): 'success' | 'warning' | 'danger' | 'neutral' {
  if (status === 'ACHIEVED') return 'success'
  if (status === 'ACTIVE') return 'warning'
  if (status === 'ABANDONED') return 'danger'
  return 'neutral'
}
</script>

<template>
  <div class="goal-card" @click="emit('click', goal.id)">
    <div class="goal-card__header">
      <h3 class="goal-card__title">{{ goal.description || goal.goalType }}</h3>
      <AppBadge :label="goal.status" :variant="statusVariant(goal.status)" />
    </div>
    <p class="goal-card__target">Target: {{ goal.targetValue }} {{ goal.unit }}</p>
    <ProgressBar
      :value="goal.progressPercentage"
      :variant="goal.progressPercentage >= 100 ? 'success' : goal.progressPercentage >= 50 ? 'primary' : 'warning'"
    />
    <div class="goal-card__footer">
      <span class="goal-card__date">Target Date: {{ formatDate(goal.targetDate, 'MMM DD, YYYY') }}</span>
      <span class="goal-card__milestones">
        {{ goal.milestones.filter(m => m.achievedDate).length }}/{{ goal.milestones.length }} milestones
      </span>
    </div>
  </div>
</template>

<style scoped>
.goal-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: box-shadow 0.15s, border-color 0.15s;
}

.goal-card:hover {
  border-color: #4f46e5;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.1);
}

.goal-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.goal-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.goal-card__target {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px;
}

.goal-card__footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.goal-card__milestones {
  font-weight: 500;
}
</style>
