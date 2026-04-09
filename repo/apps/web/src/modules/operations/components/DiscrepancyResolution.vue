<script setup lang="ts">
import { ref } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import FormTextarea from '@/components/forms/FormTextarea.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'

interface Props {
  supervisorRequired?: boolean
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  supervisorRequired: false,
  loading: false,
})

const emit = defineEmits<{
  resolve: [data: { reasonCode: string; notes: string }]
}>()

const reasonCodeOptions = [
  { value: 'DAMAGED', label: 'Damaged' },
  { value: 'SHORT_SHIP', label: 'Short Shipment' },
  { value: 'OVER_SHIP', label: 'Over Shipment' },
  { value: 'WRONG_ITEM', label: 'Wrong Item' },
  { value: 'QUALITY_FAIL', label: 'Quality Failure' },
  { value: 'OTHER', label: 'Other' },
]

const reasonCode = ref('')
const notes = ref('')

function handleSubmit() {
  if (!reasonCode.value) return
  emit('resolve', {
    reasonCode: reasonCode.value,
    notes: notes.value,
  })
}
</script>

<template>
  <form class="resolution-form" @submit.prevent="handleSubmit">
    <div v-if="supervisorRequired" class="resolution-form__warning">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
        <path d="M12 9v4m0 4h.01" />
      </svg>
      <span>This discrepancy requires supervisor approval before resolution.</span>
    </div>

    <FormField label="Reason Code" required>
      <FormSelect
        v-model="reasonCode"
        :options="reasonCodeOptions"
        placeholder="Select reason"
      />
    </FormField>

    <FormField label="Notes">
      <FormTextarea
        v-model="notes"
        placeholder="Additional details..."
        :rows="3"
      />
    </FormField>

    <SubmitButton
      :loading="loading"
      text="Resolve Discrepancy"
      loading-text="Resolving..."
      :disabled="!reasonCode"
    />
  </form>
</template>

<style scoped>
.resolution-form__warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 6px;
  font-size: 13px;
  margin-bottom: 16px;
}

.resolution-form__warning svg {
  flex-shrink: 0;
  color: #d97706;
}
</style>
