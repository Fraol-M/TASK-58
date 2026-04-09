<script setup lang="ts">
import { ref, computed } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'

interface Props {
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  submit: [data: { itemCode: string; itemName: string; expectedQty: number; unitCost?: number }]
}>()

const itemCode = ref('')
const itemName = ref('')
const expectedQty = ref('')
const unitCost = ref('')

const errors = ref<Record<string, string>>({})

const isValid = computed(() => {
  return itemCode.value.trim().length > 0 &&
    itemName.value.trim().length > 0 &&
    Number(expectedQty.value) > 0
})

function validate(): boolean {
  errors.value = {}
  if (!itemCode.value.trim()) errors.value.itemCode = 'Item code is required'
  if (!itemName.value.trim()) errors.value.itemName = 'Item name is required'
  if (!expectedQty.value || Number(expectedQty.value) <= 0) errors.value.expectedQty = 'Expected quantity must be positive'
  return Object.keys(errors.value).length === 0
}

function handleSubmit() {
  if (!validate()) return
  emit('submit', {
    itemCode: itemCode.value,
    itemName: itemName.value,
    expectedQty: Number(expectedQty.value),
    unitCost: unitCost.value ? Number(unitCost.value) : undefined,
  })
  itemCode.value = ''
  itemName.value = ''
  expectedQty.value = ''
  unitCost.value = ''
}
</script>

<template>
  <form class="add-line-form" @submit.prevent="handleSubmit">
    <div class="add-line-form__row">
      <FormField label="Item Code" required :error="errors.itemCode">
        <FormInput v-model="itemCode" placeholder="e.g., ITM-001" />
      </FormField>
      <FormField label="Item Name" required :error="errors.itemName">
        <FormInput v-model="itemName" placeholder="Enter item name" />
      </FormField>
      <FormField label="Expected Qty" required :error="errors.expectedQty">
        <FormInput v-model="expectedQty" type="number" placeholder="0" />
      </FormField>
      <FormField label="Unit Cost">
        <FormInput v-model="unitCost" type="number" placeholder="0.00" />
      </FormField>
    </div>
    <SubmitButton
      :loading="loading"
      text="Add Line"
      loading-text="Adding..."
      :disabled="!isValid"
    />
  </form>
</template>

<style scoped>
.add-line-form__row {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .add-line-form__row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
