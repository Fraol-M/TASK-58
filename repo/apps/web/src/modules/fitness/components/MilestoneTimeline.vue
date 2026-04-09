<script setup lang="ts">
import type { FitnessMilestone } from '@/services/adapters/api-adapter.interface'
import ProgressBar from '@/components/data-display/ProgressBar.vue'
import { formatDate } from '@/utils/format-date'

interface Props {
  milestones: FitnessMilestone[]
  overallProgress: number
}

defineProps<Props>()
</script>

<template>
  <div class="milestone-timeline">
    <div class="milestone-timeline__progress">
      <span class="milestone-timeline__label">Overall Progress</span>
      <ProgressBar
        :value="overallProgress"
        :variant="overallProgress >= 100 ? 'success' : 'primary'"
      />
    </div>

    <div class="milestone-timeline__list">
      <div
        v-for="(ms, i) in milestones"
        :key="ms.id"
        :class="['milestone-item', { 'milestone-item--completed': ms.achievedDate }]"
      >
        <div class="milestone-item__indicator">
          <span v-if="ms.achievedDate" class="milestone-item__check">&#10003;</span>
          <span v-else class="milestone-item__number">{{ ms.seq }}</span>
        </div>
        <div class="milestone-item__content">
          <span class="milestone-item__title">{{ ms.description }}</span>
          <span v-if="ms.achievedDate" class="milestone-item__date">
            Completed {{ formatDate(ms.achievedDate, 'MMM DD, YYYY') }}
          </span>
          <span v-else class="milestone-item__date">Pending</span>
        </div>
      </div>
    </div>

    <div v-if="milestones.length === 0" class="milestone-timeline__empty">
      No milestones defined.
    </div>
  </div>
</template>

<style scoped>
.milestone-timeline__progress {
  margin-bottom: 20px;
}

.milestone-timeline__label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 6px;
  display: block;
}

.milestone-timeline__list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.milestone-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.milestone-item:last-child {
  border-bottom: none;
}

.milestone-item__indicator {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  background: #e5e7eb;
  color: #6b7280;
}

.milestone-item--completed .milestone-item__indicator {
  background: #d1fae5;
  color: #059669;
}

.milestone-item__check {
  font-size: 14px;
}

.milestone-item__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.milestone-item__title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.milestone-item--completed .milestone-item__title {
  text-decoration: line-through;
  color: #6b7280;
}

.milestone-item__date {
  font-size: 12px;
  color: #9ca3af;
}

.milestone-timeline__empty {
  text-align: center;
  color: #9ca3af;
  padding: 16px 0;
  font-size: 14px;
}
</style>
