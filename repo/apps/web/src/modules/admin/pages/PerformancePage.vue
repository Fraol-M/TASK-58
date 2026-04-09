<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAsyncState } from '@/composables/useAsyncState'
import { getAdapter } from '@/services/adapters/adapter-factory'
import type { PerformanceMetrics } from '@/services/adapters/api-adapter.interface'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import DataTable from '@/components/tables/DataTable.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'

const adapter = getAdapter()
const { data, loading, error, execute } = useAsyncState<PerformanceMetrics>()

async function load() {
  await execute(async () => {
    const res = await adapter.getPerformanceMetrics()
    return res.data
  })
}

onMounted(load)

/** Derive table columns dynamically from the first row of data */
const tableColumns = computed(() => {
  if (!data.value || data.value.length === 0) return []
  const keys = Object.keys(data.value[0])
  return keys.map(key => ({
    key,
    label: formatLabel(key),
  }))
})

/** Derive summary cards from rows that have a numeric value in a "value" field or similar pattern */
const summaryCards = computed(() => {
  if (!data.value) return []
  // If the data has rows with simple key/value pairs (2 fields), show as metric cards
  // Otherwise show all data in the table only
  const firstRow = data.value[0]
  if (!firstRow) return []
  const keys = Object.keys(firstRow)
  // Show cards when rows look like summary metrics (have a numeric-ish value)
  if (keys.length <= 3 && keys.some(k => typeof firstRow[k] === 'number')) {
    return data.value.map((row, idx) => ({
      id: idx,
      label: String(row[keys[0]] ?? ''),
      value: row[keys.find(k => typeof row[k] === 'number') ?? keys[1]] ?? '',
    }))
  }
  return []
})

function formatLabel(key: string): string {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/_/g, ' ')
    .replace(/^\w/, c => c.toUpperCase())
    .trim()
}

function formatCellValue(value: unknown): string {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'number') return value.toLocaleString()
  return String(value)
}
</script>

<template>
  <div class="performance-page">
    <div class="performance-page__header">
      <h1 class="page-title">System Performance</h1>
      <AppButton variant="secondary" @click="load" :loading="loading">
        Refresh
      </AppButton>
    </div>

    <LoadingState v-if="loading && !data" text="Loading metrics..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="load" />

    <template v-else-if="data && data.length > 0">
      <!-- Summary cards when data rows look like key-value metrics -->
      <div v-if="summaryCards.length > 0" class="performance-page__stats">
        <AppCard v-for="card in summaryCards" :key="card.id" class="metric-card">
          <div class="metric-card__value">{{ card.value }}</div>
          <div class="metric-card__label">{{ card.label }}</div>
        </AppCard>
      </div>

      <!-- Full data table -->
      <AppCard title="Performance Metrics" padding="none" class="performance-page__queries">
        <DataTable
          :columns="tableColumns"
          :data="data"
          empty-message="No metrics available"
        >
          <template v-for="col in tableColumns" :key="col.key" #[`cell-${col.key}`]="{ value }">
            <code v-if="col.key === 'query'" class="query-text">{{ formatCellValue(value) }}</code>
            <span
              v-else-if="col.key.toLowerCase().includes('duration') || col.key.toLowerCase().includes('time')"
              :class="['time-value', Number(value) > 250 ? 'time-value--slow' : '']"
            >
              {{ formatCellValue(value) }}{{ typeof value === 'number' ? 'ms' : '' }}
            </span>
            <span v-else>{{ formatCellValue(value) }}</span>
          </template>
        </DataTable>
      </AppCard>
    </template>

    <div v-else-if="data && data.length === 0" class="performance-page__empty">
      <p>No performance metrics available.</p>
    </div>
  </div>
</template>

<style scoped>
.performance-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.performance-page__stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.metric-card {
  text-align: center;
}

.metric-card__value {
  font-size: 32px;
  font-weight: 700;
  color: #4f46e5;
}

.metric-card__label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.performance-page__queries {
  margin-bottom: 24px;
}

.performance-page__empty {
  text-align: center;
  padding: 48px 16px;
  color: #6b7280;
}

.query-text {
  font-size: 12px;
  color: #374151;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.time-value {
  font-weight: 500;
  color: #059669;
}

.time-value--slow {
  color: #dc2626;
}
</style>
