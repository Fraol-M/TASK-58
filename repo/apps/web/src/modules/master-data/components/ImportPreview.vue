<script setup lang="ts">
import type { ImportResult } from '@/services/adapters/api-adapter.interface'
import AppButton from '@/components/common/AppButton.vue'
import AppBadge from '@/components/common/AppBadge.vue'

interface Props {
  result: ImportResult
}

defineProps<Props>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <div class="import-preview">
    <div class="import-preview__summary">
      <div class="summary-stat">
        <span class="summary-stat__value">{{ result.totalRows }}</span>
        <span class="summary-stat__label">Total Rows</span>
      </div>
      <div class="summary-stat summary-stat--success">
        <span class="summary-stat__value">{{ result.successCount }}</span>
        <span class="summary-stat__label">Success</span>
      </div>
      <div class="summary-stat summary-stat--danger">
        <span class="summary-stat__value">{{ result.errorCount }}</span>
        <span class="summary-stat__label">Errors</span>
      </div>
    </div>

    <div v-if="result.errors.length > 0" class="import-preview__errors">
      <h4 class="import-preview__errors-title">Errors</h4>
      <div
        v-for="(err, i) in result.errors"
        :key="i"
        class="error-row"
      >
        <AppBadge :label="`Row ${err.rowNumber}`" variant="danger" />
        <span class="error-row__message">{{ err.message }}</span>
      </div>
    </div>

    <div class="import-preview__actions">
      <AppButton variant="secondary" @click="emit('cancel')">Cancel</AppButton>
      <AppButton @click="emit('confirm')">Confirm Import</AppButton>
    </div>
  </div>
</template>

<style scoped>
.import-preview__summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.summary-stat {
  text-align: center;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.summary-stat--success {
  background: #f0fdf4;
}

.summary-stat--danger {
  background: #fef2f2;
}

.summary-stat__value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  display: block;
}

.summary-stat--success .summary-stat__value {
  color: #059669;
}

.summary-stat--danger .summary-stat__value {
  color: #dc2626;
}

.summary-stat__label {
  font-size: 13px;
  color: #6b7280;
}

.import-preview__errors {
  margin-bottom: 20px;
}

.import-preview__errors-title {
  font-size: 14px;
  font-weight: 600;
  color: #991b1b;
  margin: 0 0 8px;
}

.error-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #fecaca;
}

.error-row__message {
  font-size: 13px;
  color: #374151;
}

.import-preview__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
