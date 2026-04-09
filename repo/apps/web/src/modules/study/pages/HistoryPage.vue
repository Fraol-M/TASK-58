<script setup lang="ts">
import { useHistory } from '../composables/useHistory'
import CompletionCalendar from '../components/CompletionCalendar.vue'
import AppCard from '@/components/common/AppCard.vue'
import FormField from '@/components/forms/FormField.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import FormDatePicker from '@/components/forms/FormDatePicker.vue'
import DataTable from '@/components/tables/DataTable.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import { formatDate } from '@/utils/format-date'
import { computed } from 'vue'

const {
  completions,
  plans,
  loading,
  error,
  selectedPlanId,
  dateFrom,
  dateTo,
  completionDays,
} = useHistory()

const planOptions = computed(() =>
  plans.value.map(p => ({ value: String(p.id), label: p.title }))
)

const tableColumns = [
  { key: 'date', label: 'Date' },
  { key: 'plan', label: 'Plan' },
  { key: 'completed', label: 'Completed' },
  { key: 'notes', label: 'Notes' },
]

const tableData = computed(() =>
  completions.value.map(c => ({
    date: formatDate(c.completedDate, 'MMM DD, YYYY'),
    plan: plans.value.find(p => p.id === c.planId)?.title ?? '-',
    completed: c.completed ? 'Yes' : 'No',
    notes: c.notes || '-',
  }))
)

function handlePlanFilter(val: string) {
  selectedPlanId.value = val ? Number(val) : null
}
</script>

<template>
  <div class="history-page">
    <h1 class="page-title">Study History</h1>

    <LoadingState v-if="loading" text="Loading history..." />
    <ErrorState v-else-if="error" :message="error.message" />

    <template v-else>
      <!-- Completion Calendar -->
      <AppCard title="Last 30 Days" class="history-page__calendar">
        <CompletionCalendar :days="completionDays" />
      </AppCard>

      <!-- Filters -->
      <div class="history-page__filters">
        <FormField label="Filter by Plan">
          <FormSelect
            :model-value="selectedPlanId ? String(selectedPlanId) : ''"
            :options="[{ value: '', label: 'All Plans' }, ...planOptions]"
            placeholder="All Plans"
            @update:model-value="handlePlanFilter"
          />
        </FormField>
        <FormField label="From">
          <FormDatePicker v-model="dateFrom" />
        </FormField>
        <FormField label="To">
          <FormDatePicker v-model="dateTo" :min="dateFrom" />
        </FormField>
      </div>

      <!-- Table -->
      <AppCard padding="none">
        <EmptyState
          v-if="tableData.length === 0"
          title="No history found"
          message="No review sessions match your filters."
        />
        <DataTable
          v-else
          :columns="tableColumns"
          :data="tableData"
        />
      </AppCard>
    </template>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
}

.history-page__calendar {
  margin-bottom: 24px;
}

.history-page__filters {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

@media (max-width: 640px) {
  .history-page__filters {
    grid-template-columns: 1fr;
  }
}
</style>
