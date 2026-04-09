<script setup lang="ts">
import { ref, computed } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import FormTextarea from '@/components/forms/FormTextarea.vue'
import FormDatePicker from '@/components/forms/FormDatePicker.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'

interface Props {
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  submit: [data: {
    goalType: string
    description?: string
    targetValue: number
    unit: string
    startDate: string
    targetDate: string
  }]
}>()

const goalTypeOptions = [
  { value: 'WEIGHT_LOSS', label: 'Weight Loss' },
  { value: 'WEIGHT_GAIN', label: 'Weight Gain' },
  { value: 'FLEXIBILITY', label: 'Flexibility' },
  { value: 'ENDURANCE', label: 'Endurance' },
  { value: 'STRENGTH', label: 'Strength' },
]

const goalType = ref('')
const description = ref('')
const targetValue = ref('')
const unit = ref('')
const startDate = ref('')
const targetDate = ref('')

const errors = ref<Record<string, string>>({})

const isValid = computed(() => {
  return goalType.value && description.value && targetValue.value &&
    Number(targetValue.value) > 0 && startDate.value && targetDate.value &&
    targetDate.value > startDate.value
})

function validate(): boolean {
  errors.value = {}
  if (!goalType.value) errors.value.goalType = 'Goal type is required'
  if (!description.value) errors.value.description = 'Description is required'
  if (!targetValue.value || Number(targetValue.value) <= 0) {
    errors.value.targetValue = 'Target value must be greater than 0'
  }
  if (!startDate.value) errors.value.startDate = 'Start date is required'
  if (!targetDate.value) errors.value.targetDate = 'Target date is required'
  if (startDate.value && targetDate.value && targetDate.value <= startDate.value) {
    errors.value.targetDate = 'Target date must be after start date'
  }
  return Object.keys(errors.value).length === 0
}

function handleSubmit() {
  if (!validate()) return
  emit('submit', {
    goalType: goalType.value,
    description: description.value || undefined,
    targetValue: Number(targetValue.value),
    unit: unit.value,
    startDate: startDate.value,
    targetDate: targetDate.value,
  })
}
</script>

<template>
  <form class="goal-form" @submit.prevent="handleSubmit">
    <FormField label="Goal Type" required :error="errors.goalType">
      <FormSelect
        v-model="goalType"
        :options="goalTypeOptions"
        placeholder="Select goal type"
      />
    </FormField>

    <FormField label="Description" required :error="errors.description">
      <FormTextarea
        v-model="description"
        placeholder="Describe your goal..."
        :rows="3"
      />
    </FormField>

    <div class="goal-form__row">
      <FormField label="Target Value" required :error="errors.targetValue">
        <FormInput
          v-model="targetValue"
          type="number"
          placeholder="e.g., 180"
        />
      </FormField>
      <FormField label="Unit" hint="e.g., lbs, minutes, reps">
        <FormInput v-model="unit" placeholder="e.g., lbs" />
      </FormField>
    </div>

    <div class="goal-form__row">
      <FormField label="Start Date" required :error="errors.startDate">
        <FormDatePicker v-model="startDate" />
      </FormField>
      <FormField label="Target Date" required :error="errors.targetDate">
        <FormDatePicker v-model="targetDate" :min="startDate" />
      </FormField>
    </div>

    <SubmitButton
      :loading="loading"
      text="Create Goal"
      loading-text="Creating..."
      :disabled="!isValid"
    />
  </form>
</template>

<style scoped>
.goal-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
</style>
