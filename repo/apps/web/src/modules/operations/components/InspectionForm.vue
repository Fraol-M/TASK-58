<script setup lang="ts">
import { ref } from 'vue'
import type { InboundLine } from '@/services/adapters/api-adapter.interface'
import FormField from '@/components/forms/FormField.vue'
import FormTextarea from '@/components/forms/FormTextarea.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'
import AppButton from '@/components/common/AppButton.vue'

interface Props {
  items: InboundLine[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  submit: [data: { lineId: number; inspectedQty: number; result: 'PASS' | 'FAIL'; notes?: string }]
}>()

interface LineResult {
  itemId: number
  result: 'pass' | 'fail' | ''
  notes: string
}

const lineResults = ref<LineResult[]>(
  props.items.map(item => ({
    itemId: item.id,
    result: '',
    notes: '',
  }))
)

const overallNotes = ref('')

function passAll() {
  lineResults.value.forEach(lr => {
    lr.result = 'pass'
  })
}

function handleSubmit() {
  for (const lr of lineResults.value) {
    if (lr.result) {
      const item = props.items.find(it => it.id === lr.itemId)
      emit('submit', {
        lineId: lr.itemId,
        inspectedQty: item?.receivedQty ?? item?.expectedQty ?? 0,
        result: lr.result === 'pass' ? 'PASS' : 'FAIL',
        notes: lr.notes || overallNotes.value || undefined,
      })
    }
  }
}
</script>

<template>
  <div class="inspection-form">
    <div class="inspection-form__header">
      <h3 class="inspection-form__title">Inspection</h3>
      <AppButton variant="secondary" size="sm" @click="passAll">
        Pass All
      </AppButton>
    </div>

    <div class="inspection-form__lines">
      <div
        v-for="(lr, i) in lineResults"
        :key="lr.itemId"
        class="inspection-line"
      >
        <div class="inspection-line__info">
          <span class="inspection-line__name">{{ items[i]?.itemName ?? 'Item' }}</span>
          <span class="inspection-line__qty">Qty: {{ items[i]?.receivedQty ?? 0 }}</span>
        </div>
        <div class="inspection-line__actions">
          <label class="inspection-radio">
            <input
              type="radio"
              :name="`result-${lr.itemId}`"
              value="pass"
              v-model="lr.result"
            />
            <span class="inspection-radio__label inspection-radio__label--pass">Pass</span>
          </label>
          <label class="inspection-radio">
            <input
              type="radio"
              :name="`result-${lr.itemId}`"
              value="fail"
              v-model="lr.result"
            />
            <span class="inspection-radio__label inspection-radio__label--fail">Fail</span>
          </label>
        </div>
        <FormTextarea
          v-if="lr.result === 'fail'"
          v-model="lr.notes"
          placeholder="Describe the issue..."
          :rows="2"
        />
      </div>
    </div>

    <FormField label="Overall Notes">
      <FormTextarea
        v-model="overallNotes"
        placeholder="Additional inspection notes..."
        :rows="3"
      />
    </FormField>

    <SubmitButton
      :loading="loading"
      text="Complete Inspection"
      loading-text="Submitting..."
    />
  </div>
</template>

<style scoped>
.inspection-form__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.inspection-form__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.inspection-form__lines {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.inspection-line {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.inspection-line__info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.inspection-line__name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.inspection-line__qty {
  font-size: 13px;
  color: #6b7280;
}

.inspection-line__actions {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.inspection-radio {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.inspection-radio input {
  accent-color: #4f46e5;
}

.inspection-radio__label {
  font-size: 14px;
  font-weight: 500;
}

.inspection-radio__label--pass {
  color: #059669;
}

.inspection-radio__label--fail {
  color: #dc2626;
}
</style>
