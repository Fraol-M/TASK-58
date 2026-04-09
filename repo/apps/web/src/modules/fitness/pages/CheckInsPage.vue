<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useCheckIns } from '../composables/useCheckIns'
import { useToast } from '@/composables/useToast'
import AppCard from '@/components/common/AppCard.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import ProgressBar from '@/components/data-display/ProgressBar.vue'
import CheckInForm from '../components/CheckInForm.vue'
import CheckInTimeline from '../components/CheckInTimeline.vue'
import MilestoneTimeline from '../components/MilestoneTimeline.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'

const route = useRoute()
const toast = useToast()
const goalId = Number(route.params.goalId)

const { currentGoal, checkIns, loading, error, submitting, createCheckIn, refresh } =
  useCheckIns(goalId)

const goalUnit = computed(() => {
  if (!currentGoal.value) return ''
  return currentGoal.value.unit
})

async function handleCheckIn(data: { value: number; notes: string }) {
  try {
    await createCheckIn(data.value, data.notes)
    toast.show('Check-in recorded!', 'success')
  } catch {
    toast.show('Failed to save check-in', 'error')
  }
}
</script>

<template>
  <div class="checkins-page">
    <h1 class="page-title">Goal Check-Ins</h1>

    <LoadingState v-if="loading" text="Loading goal details..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />

    <template v-else-if="currentGoal">
      <!-- Goal summary -->
      <AppCard class="checkins-page__summary">
        <div class="goal-summary">
          <div class="goal-summary__info">
            <h2 class="goal-summary__title">{{ currentGoal.description || currentGoal.goalType }}</h2>
            <AppBadge :label="currentGoal.status" :variant="currentGoal.status === 'ACHIEVED' ? 'success' : currentGoal.status === 'ACTIVE' ? 'warning' : 'danger'" />
          </div>
          <p class="goal-summary__target">Target: {{ currentGoal.targetValue }} {{ currentGoal.unit }}</p>
          <ProgressBar
            :value="currentGoal.progressPercentage"
            :variant="currentGoal.progressPercentage >= 100 ? 'success' : 'primary'"
          />
        </div>
      </AppCard>

      <div class="checkins-page__content">
        <div class="checkins-page__main">
          <!-- Add Check-In -->
          <AppCard title="Log Check-In" class="checkins-page__form-card">
            <CheckInForm
              :unit-label="goalUnit"
              :loading="submitting"
              @submit="handleCheckIn"
            />
          </AppCard>

          <!-- Timeline -->
          <AppCard title="Check-In History">
            <CheckInTimeline :check-ins="checkIns" />
          </AppCard>
        </div>

        <!-- Sidebar: Milestones -->
        <div class="checkins-page__sidebar">
          <AppCard title="Milestones">
            <MilestoneTimeline
              :milestones="currentGoal.milestones"
              :overall-progress="currentGoal.progressPercentage"
            />
          </AppCard>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 20px;
}

.checkins-page__summary {
  margin-bottom: 24px;
}

.goal-summary__info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.goal-summary__title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.goal-summary__target {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px;
}

.checkins-page__content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

@media (max-width: 768px) {
  .checkins-page__content {
    grid-template-columns: 1fr;
  }
}

.checkins-page__main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
</style>
