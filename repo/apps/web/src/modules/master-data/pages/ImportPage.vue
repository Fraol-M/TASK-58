<script setup lang="ts">
import { useImport } from '../composables/useImport'
import { useToast } from '@/composables/useToast'
import ImportUploader from '../components/ImportUploader.vue'
import ImportPreview from '../components/ImportPreview.vue'
import FormField from '@/components/forms/FormField.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import AppCard from '@/components/common/AppCard.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'

const toast = useToast()
const {
  selectedFile,
  entityType,
  hasPreview,
  importResult,
  loading,
  error,
  onFileSelected,
  submitImport,
  reset,
} = useImport()

const typeOptions = [
  { value: 'term', label: 'Terms' },
  { value: 'school', label: 'Schools' },
  { value: 'major', label: 'Majors' },
  { value: 'class', label: 'Classes' },
  { value: 'course', label: 'Courses' },
]

async function handleConfirm() {
  try {
    await submitImport()
    toast.show('Import completed successfully', 'success')
  } catch {
    toast.show('Import failed', 'error')
  }
}

function handleCancel() {
  reset()
}
</script>

<template>
  <div class="import-page">
    <h1 class="page-title">Import Data</h1>

    <AppCard class="import-page__card">
      <FormField label="Entity Type">
        <FormSelect
          v-model="entityType"
          :options="typeOptions"
          placeholder="Select entity type"
        />
      </FormField>

      <ImportUploader @files="onFileSelected" />

      <div v-if="selectedFile" class="import-page__file-info">
        Selected: <strong>{{ selectedFile.name }}</strong>
        ({{ Math.round(selectedFile.size / 1024) }} KB)
      </div>
    </AppCard>

    <LoadingState v-if="loading" text="Processing import..." />
    <ErrorState v-else-if="error" :message="error.message" />

    <AppCard v-if="importResult" title="Import Results" class="import-page__results">
      <ImportPreview
        :result="importResult"
        @confirm="handleConfirm"
        @cancel="handleCancel"
      />
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

.import-page__card {
  margin-bottom: 24px;
}

.import-page__file-info {
  margin-top: 12px;
  font-size: 14px;
  color: #374151;
}

.import-page__results {
  margin-top: 24px;
}
</style>
