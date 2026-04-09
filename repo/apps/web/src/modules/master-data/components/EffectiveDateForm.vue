<script setup lang="ts">
import FormField from '@/components/forms/FormField.vue'
import FormDatePicker from '@/components/forms/FormDatePicker.vue'
import { computed } from 'vue'

interface Props {
  effectiveFrom: string
  effectiveTo: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:effectiveFrom': [value: string]
  'update:effectiveTo': [value: string]
}>()

const dateError = computed(() => {
  if (props.effectiveFrom && props.effectiveTo && props.effectiveTo < props.effectiveFrom) {
    return 'Effective To must be after Effective From'
  }
  return ''
})
</script>

<template>
  <div class="effective-date-form">
    <FormField label="Effective From">
      <FormDatePicker
        :model-value="effectiveFrom"
        @update:model-value="emit('update:effectiveFrom', $event)"
      />
    </FormField>
    <FormField label="Effective To" :error="dateError">
      <FormDatePicker
        :model-value="effectiveTo"
        :min="effectiveFrom"
        @update:model-value="emit('update:effectiveTo', $event)"
      />
    </FormField>
  </div>
</template>

<style scoped>
.effective-date-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
</style>
