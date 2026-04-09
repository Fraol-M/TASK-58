<script setup lang="ts">
import { ref } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import FormTextarea from '@/components/forms/FormTextarea.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'

interface Props {
  unitLabel?: string
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  unitLabel: '',
  loading: false,
})

const emit = defineEmits<{
  submit: [data: { value: number; notes: string }]
}>()

const value = ref('')
const notes = ref('')
const error = ref('')

function handleSubmit() {
  error.value = ''
  if (!value.value || Number(value.value) <= 0) {
    error.value = 'Value is required and must be positive'
    return
  }
  emit('submit', {
    value: Number(value.value),
    notes: notes.value,
  })
  value.value = ''
  notes.value = ''
}
</script>

<template>
  <form class="checkin-form" @submit.prevent="handleSubmit">
    <FormField
      :label="`Value${unitLabel ? ` (${unitLabel})` : ''}`"
      required
      :error="error"
    >
      <FormInput
        v-model="value"
        type="number"
        :placeholder="`Enter value${unitLabel ? ` in ${unitLabel}` : ''}`"
      />
    </FormField>

    <FormField label="Notes">
      <FormTextarea
        v-model="notes"
        placeholder="Any notes about this check-in..."
        :rows="3"
      />
    </FormField>

    <SubmitButton
      :loading="loading"
      text="Log Check-In"
      loading-text="Saving..."
    />
  </form>
</template>

<style scoped>
.checkin-form {
  padding: 4px 0;
}
</style>
