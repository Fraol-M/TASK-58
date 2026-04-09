<script setup lang="ts">
import { ref, computed } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'
import AppButton from '@/components/common/AppButton.vue'

interface Props {
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  submit: [data: { type: string; referenceNumber: string; supplier: string }]
}>()

const typeOptions = [
  { value: 'PURCHASE', label: 'Purchase Order' },
  { value: 'TRANSFER', label: 'Transfer' },
  { value: 'RETURN', label: 'Return' },
]

const receiptType = ref('')
const referenceNumber = ref('')
const supplier = ref('')

const errors = ref<Record<string, string>>({})

const isValid = computed(() => {
  return receiptType.value && supplier.value.trim().length > 0
})

function validate(): boolean {
  errors.value = {}
  if (!receiptType.value) errors.value.type = 'Receipt type is required'
  if (!supplier.value.trim()) errors.value.supplier = 'Supplier name is required'
  return Object.keys(errors.value).length === 0
}

function handleSubmit() {
  if (!validate()) return
  emit('submit', {
    type: receiptType.value,
    referenceNumber: referenceNumber.value,
    supplier: supplier.value,
  })
}
</script>

<template>
  <form class="receipt-form" @submit.prevent="handleSubmit">
    <FormField label="Receipt Type" required :error="errors.type">
      <FormSelect
        v-model="receiptType"
        :options="typeOptions"
        placeholder="Select type"
      />
    </FormField>

    <FormField label="Reference Number" hint="PO number, transfer ID, etc.">
      <div class="receipt-form__ref-row">
        <FormInput
          v-model="referenceNumber"
          placeholder="e.g., PO-2026-0042"
        />
        <AppButton variant="secondary" size="sm" :disabled="true">
          Scan
        </AppButton>
      </div>
    </FormField>

    <FormField label="Supplier Name" required :error="errors.supplier">
      <FormInput
        v-model="supplier"
        placeholder="Enter supplier name"
      />
    </FormField>

    <SubmitButton
      :loading="loading"
      text="Create Receipt"
      loading-text="Creating..."
      :disabled="!isValid"
    />
  </form>
</template>

<style scoped>
.receipt-form__ref-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
</style>
