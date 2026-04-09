<script setup lang="ts">
import { useAuditHistory } from '../composables/useAuditHistory'
import AuditTimeline from '../components/AuditTimeline.vue'
import FormField from '@/components/forms/FormField.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import FormDatePicker from '@/components/forms/FormDatePicker.vue'
import AppCard from '@/components/common/AppCard.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const { history, loading, error, entityTypeFilter, dateFrom, dateTo, refresh } = useAuditHistory()

const typeOptions = [
  { value: '', label: 'All Types' },
  { value: 'term', label: 'Terms' },
  { value: 'school', label: 'Schools' },
  { value: 'major', label: 'Majors' },
  { value: 'class', label: 'Classes' },
  { value: 'course', label: 'Courses' },
]
</script>

<template>
  <div class="history-page">
    <h1 class="page-title">Change History</h1>

    <div class="history-page__filters">
      <FormField label="Entity Type">
        <FormSelect
          v-model="entityTypeFilter"
          :options="typeOptions"
          placeholder="All Types"
        />
      </FormField>
      <FormField label="From">
        <FormDatePicker v-model="dateFrom" />
      </FormField>
      <FormField label="To">
        <FormDatePicker v-model="dateTo" :min="dateFrom" />
      </FormField>
    </div>

    <LoadingState v-if="loading" text="Loading history..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />

    <AppCard v-else>
      <EmptyState
        v-if="history.length === 0"
        title="No history"
        message="No change history entries match your filters."
      />
      <AuditTimeline v-else :entries="history" />
    </AppCard>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
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
