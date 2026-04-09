<script setup lang="ts">
import type { Discrepancy } from '@/services/adapters/api-adapter.interface'
import AppBadge from '@/components/common/AppBadge.vue'

interface Props {
  discrepancy: Discrepancy
}

defineProps<Props>()

function typeLabel(type: string): string {
  return type.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
}
</script>

<template>
  <div :class="['discrepancy-card', { 'discrepancy-card--resolved': discrepancy.resolved }]">
    <div class="discrepancy-card__header">
      <AppBadge
        :label="typeLabel(discrepancy.discrepancyType)"
        :variant="discrepancy.resolved ? 'success' : 'danger'"
      />
      <AppBadge
        v-if="!discrepancy.resolved && discrepancy.supervisorRequired"
        label="Requires Supervisor"
        variant="warning"
      />
      <AppBadge
        v-if="discrepancy.resolved"
        label="Resolved"
        variant="success"
      />
    </div>

    <div class="discrepancy-card__details">
      <div class="detail-row">
        <span class="detail-row__label">Expected / Actual</span>
        <span class="detail-row__value">{{ discrepancy.expectedValue }} / {{ discrepancy.actualValue }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-row__label">Variance</span>
        <span class="detail-row__value">{{ discrepancy.variancePercent }}%</span>
      </div>
      <div v-if="discrepancy.notes" class="detail-row">
        <span class="detail-row__label">Notes</span>
        <span class="detail-row__value">{{ discrepancy.notes }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.discrepancy-card {
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.discrepancy-card--resolved {
  border-color: #d1fae5;
  background: #f0fdf4;
}

.discrepancy-card__header {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.discrepancy-card__details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.detail-row__label {
  color: #6b7280;
}

.detail-row__value {
  color: #111827;
  font-weight: 500;
}
</style>
