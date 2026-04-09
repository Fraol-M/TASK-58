<script setup lang="ts">
import type { DashboardData } from '@/services/adapters/api-adapter.interface'
import StatCard from './StatCard.vue'
import AppCard from '@/components/common/AppCard.vue'

interface Props {
  data: DashboardData
}

const props = defineProps<Props>()
</script>

<template>
  <div class="admin-dashboard">
    <div class="admin-dashboard__stats">
      <StatCard
        label="Total Users"
        :value="data.summary.totalUsers ?? 0"
        color="#4f46e5"
        subtitle="Registered accounts"
      />
      <StatCard
        label="Active Plans"
        :value="data.summary.activePlans ?? 0"
        color="#059669"
        subtitle="Study + Fitness"
      />
      <StatCard
        label="Import Jobs"
        :value="data.summary.importJobs ?? 0"
        color="#d97706"
        subtitle="Processed today"
      />
      <StatCard
        label="Avg Response"
        :value="`${data.summary.avgResponseTime ?? 0}ms`"
        color="#7c3aed"
        subtitle="API latency"
      />
    </div>

    <div class="admin-dashboard__content">
      <AppCard title="System Summary">
        <div
          v-for="(item, i) in data.recentActivity"
          :key="i"
          class="summary-row"
        >
          <span class="summary-row__label">{{ item.label }}</span>
          <span class="summary-row__value">{{ item.value }}</span>
        </div>
        <div v-if="data.recentActivity.length === 0" class="admin-dashboard__empty">
          No system activity data available.
        </div>
      </AppCard>

      <AppCard title="Performance" v-if="data.charts.performance">
        <div class="perf-grid">
          <div
            v-for="(val, i) in data.charts.performance"
            :key="i"
            class="perf-bar-wrapper"
          >
            <div
              class="perf-bar"
              :style="{ height: `${Math.min(val, 100)}%` }"
            ></div>
            <span class="perf-bar__label">{{ i + 1 }}</span>
          </div>
        </div>
      </AppCard>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard__stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.admin-dashboard__content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 768px) {
  .admin-dashboard__content {
    grid-template-columns: 1fr;
  }
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-row__label {
  font-size: 14px;
  color: #6b7280;
}

.summary-row__value {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.admin-dashboard__empty {
  font-size: 14px;
  color: #9ca3af;
  text-align: center;
  padding: 20px 0;
}

.perf-grid {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 120px;
  padding-top: 8px;
}

.perf-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
}

.perf-bar {
  width: 100%;
  max-width: 32px;
  background: #4f46e5;
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.3s ease;
}

.perf-bar__label {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
