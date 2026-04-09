<script setup lang="ts">
import { watch } from 'vue'
import { useMerge } from '../composables/useMerge'
import { useToast } from '@/composables/useToast'
import MergeComparison from '../components/MergeComparison.vue'
import FormField from '@/components/forms/FormField.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const toast = useToast()
const { mergeCandidates, loading, error, entityType, fetchCandidates, merge } = useMerge()

const typeOptions = [
  { value: 'term', label: 'Terms' },
  { value: 'school', label: 'Schools' },
  { value: 'major', label: 'Majors' },
  { value: 'class', label: 'Classes' },
  { value: 'course', label: 'Courses' },
]

watch(entityType, () => {
  fetchCandidates()
})

async function handleMerge(sourceId: number, targetId: number) {
  try {
    await merge(sourceId, targetId)
    toast.show('Records merged successfully', 'success')
  } catch {
    toast.show('Merge failed', 'error')
  }
}
</script>

<template>
  <div class="merge-page">
    <h1 class="page-title">Merge Duplicates</h1>

    <div class="merge-page__filter">
      <FormField label="Entity Type">
        <FormSelect
          v-model="entityType"
          :options="typeOptions"
          placeholder="Select type"
        />
      </FormField>
    </div>

    <LoadingState v-if="loading" text="Finding duplicates..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="fetchCandidates" />
    <EmptyState
      v-else-if="mergeCandidates.length === 0"
      title="No duplicates found"
      message="No potential duplicate records were found for this entity type."
    />

    <div v-else class="merge-page__list">
      <MergeComparison
        v-for="candidate in mergeCandidates"
        :key="`${candidate.sourceItem.id}-${candidate.targetItem.id}`"
        :candidate="candidate"
        @merge="handleMerge"
        @skip="() => {}"
      />
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
}

.merge-page__filter {
  max-width: 300px;
  margin-bottom: 24px;
}

.merge-page__list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
