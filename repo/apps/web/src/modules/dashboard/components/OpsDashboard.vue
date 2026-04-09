<script setup lang="ts">
import type { DashboardData } from '@/services/adapters/api-adapter.interface'
import StatCard from './StatCard.vue'
import StatusIndicator from '@/components/data-display/StatusIndicator.vue'
import AppCard from '@/components/common/AppCard.vue'

interface Props {
  data: DashboardData
}

const props = defineProps<Props>()
</script>

<template>
  <div class="ops-dashboard">
    <div class="ops-dashboard__stats">
      <StatCard
        label="Active Receipts"
        :value="data.summary.activeReceipts ?? 0"
        color="#2563eb"
        subtitle="In progress"
      />
      <StatCard
        label="Pending Discrepancies"
        :value="data.summary.pendingDiscrepancies ?? 0"
        color="#dc2626"
        :trend="Number(data.summary.pendingDiscrepancies ?? 0) > 0 ? 'down' : 'neutral'"
        subtitle="Needs resolution"
      />
      <StatCard
        label="Putaway Queue"
        :value="data.summary.putawayQueueSize ?? 0"
        color="#7c3aed"
        subtitle="Tasks pending"
      />
      <StatCard
        label="Completed Today"
        :value="data.summary.operationsProcessed ?? 0"
        color="#059669"
        trend="up"
        subtitle="Receipts closed"
      />
    </div>

    <AppCard title="Receipt Status Overview">
      <div class="ops-dashboard__status-list">
        <div class="status-row">
          <StatusIndicator status="pending" label="Draft" />
          <span class="status-row__count">{{ data.charts.receiptsByStatus?.[0] ?? 0 }}</span>
        </div>
        <div class="status-row">
          <StatusIndicator status="in_progress" label="Receiving" />
          <span class="status-row__count">{{ data.charts.receiptsByStatus?.[1] ?? 0 }}</span>
        </div>
        <div class="status-row">
          <StatusIndicator status="inspection" label="Inspection" />
          <span class="status-row__count">{{ data.charts.receiptsByStatus?.[2] ?? 0 }}</span>
        </div>
        <div class="status-row">
          <StatusIndicator status="putaway" label="Putaway" />
          <span class="status-row__count">{{ data.charts.receiptsByStatus?.[3] ?? 0 }}</span>
        </div>
        <div class="status-row">
          <StatusIndicator status="completed" label="Completed" />
          <span class="status-row__count">{{ data.charts.receiptsByStatus?.[4] ?? 0 }}</span>
        </div>
      </div>
    </AppCard>
  </div>
</template>

<style scoped>
.ops-dashboard__stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.ops-dashboard__status-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.status-row:last-child {
  border-bottom: none;
}

.status-row__count {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}
</style>
