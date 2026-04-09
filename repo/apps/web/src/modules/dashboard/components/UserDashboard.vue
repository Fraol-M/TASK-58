<script setup lang="ts">
import type { DashboardData } from '@/services/adapters/api-adapter.interface'
import StatCard from './StatCard.vue'
import ProgressBar from '@/components/data-display/ProgressBar.vue'
import AppCard from '@/components/common/AppCard.vue'

interface Props {
  data: DashboardData
}

const props = defineProps<Props>()
</script>

<template>
  <div class="user-dashboard">
    <div class="user-dashboard__stats">
      <StatCard
        label="Fitness Goals"
        :value="data.summary.fitnessGoals ?? 0"
        color="#4f46e5"
        subtitle="Active goals"
      />
      <StatCard
        label="Study Streak"
        :value="`${data.summary.studyStreak ?? 0} days`"
        color="#059669"
        trend="up"
        subtitle="Keep it going!"
      />
      <StatCard
        label="Check-Ins"
        :value="data.summary.recentCheckIns ?? 0"
        color="#d97706"
        subtitle="This week"
      />
      <StatCard
        label="Notifications"
        :value="data.summary.pendingNotifications ?? 0"
        color="#7c3aed"
        subtitle="Unread"
      />
    </div>

    <div class="user-dashboard__content">
      <AppCard title="Goal Progress" v-if="data.charts.goalProgress">
        <div
          v-for="(progress, i) in data.charts.goalProgress"
          :key="i"
          class="goal-row"
        >
          <span class="goal-row__label">Goal {{ i + 1 }}</span>
          <ProgressBar
            :value="progress"
            :variant="progress >= 100 ? 'success' : progress >= 50 ? 'primary' : 'warning'"
          />
        </div>
        <div v-if="data.charts.goalProgress.length === 0" class="user-dashboard__empty">
          No active goals yet.
        </div>
      </AppCard>

      <AppCard title="Recent Activity">
        <div
          v-for="(item, i) in data.recentActivity"
          :key="i"
          class="activity-row"
        >
          <div class="activity-row__label">{{ item.label }}</div>
          <div class="activity-row__value">{{ item.value }}</div>
        </div>
        <div v-if="data.recentActivity.length === 0" class="user-dashboard__empty">
          No recent activity.
        </div>
      </AppCard>
    </div>
  </div>
</template>

<style scoped>
.user-dashboard__stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.user-dashboard__content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 768px) {
  .user-dashboard__content {
    grid-template-columns: 1fr;
  }
}

.goal-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.goal-row__label {
  font-size: 14px;
  color: #374151;
  min-width: 60px;
}

.activity-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.activity-row:last-child {
  border-bottom: none;
}

.activity-row__label {
  font-size: 14px;
  color: #374151;
}

.activity-row__value {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.user-dashboard__empty {
  font-size: 14px;
  color: #9ca3af;
  text-align: center;
  padding: 20px 0;
}
</style>
