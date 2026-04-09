<script setup lang="ts">
import { ref, computed } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import FormInput from '@/components/forms/FormInput.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'

interface Props {
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  submit: [data: { exportType: string; password: string }]
}>()

const typeOptions = [
  { value: 'ACCOUNT_DATA', label: 'Account Data' },
  { value: 'STUDY_DATA', label: 'Study Data' },
  { value: 'FITNESS_DATA', label: 'Fitness Data' },
]

const exportType = ref('')
const password = ref('')
const confirmPassword = ref('')

const errors = ref<Record<string, string>>({})

const isValid = computed(() => {
  return exportType.value &&
    password.value.length >= 6 &&
    password.value === confirmPassword.value
})

function validate(): boolean {
  errors.value = {}
  if (!exportType.value) errors.value.exportType = 'Select an export type'
  if (!password.value) errors.value.password = 'Password is required'
  else if (password.value.length < 6) errors.value.password = 'Password must be at least 6 characters'
  if (password.value !== confirmPassword.value) {
    errors.value.confirmPassword = 'Passwords do not match'
  }
  return Object.keys(errors.value).length === 0
}

function handleSubmit() {
  if (!validate()) return
  emit('submit', {
    exportType: exportType.value,
    password: password.value,
  })
}
</script>

<template>
  <form class="export-form" @submit.prevent="handleSubmit">
    <FormField label="Export Type" required :error="errors.exportType">
      <FormSelect
        v-model="exportType"
        :options="typeOptions"
        placeholder="Select data to export"
      />
    </FormField>

    <FormField label="Password" required :error="errors.password" hint="Required for encryption">
      <FormInput
        v-model="password"
        type="password"
        placeholder="Enter password"
      />
    </FormField>

    <FormField label="Confirm Password" required :error="errors.confirmPassword">
      <FormInput
        v-model="confirmPassword"
        type="password"
        placeholder="Confirm password"
      />
    </FormField>

    <SubmitButton
      :loading="loading"
      text="Request Export"
      loading-text="Processing..."
      :disabled="!isValid"
    />
  </form>
</template>
