<script setup lang="ts">
import { ref } from 'vue'
import type { PutawayTask } from '@/services/adapters/api-adapter.interface'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppBadge from '@/components/common/AppBadge.vue'

interface Props {
  task: PutawayTask
}

const props = defineProps<Props>()

const emit = defineEmits<{
  confirm: [taskId: number, location: string]
}>()

const actualLocation = ref(props.task.suggestedLocation || '')

function handleConfirm() {
  if (actualLocation.value.trim()) {
    emit('confirm', props.task.id, actualLocation.value)
  }
}
</script>

<template>
  <div :class="['putaway-item', { 'putaway-item--completed': task.status === 'COMPLETED' }]">
    <div class="putaway-item__header">
      <span class="putaway-item__name">Line #{{ task.lineId }}</span>
      <AppBadge
        :label="task.status"
        :variant="task.status === 'COMPLETED' ? 'success' : 'neutral'"
      />
    </div>

    <div class="putaway-item__details">
      <div class="putaway-item__detail">
        <span class="putaway-item__label">Suggested Location</span>
        <span class="putaway-item__value">{{ task.suggestedLocation || 'N/A' }}</span>
      </div>
      <div v-if="task.actualLocation" class="putaway-item__detail">
        <span class="putaway-item__label">Actual Location</span>
        <span class="putaway-item__value">{{ task.actualLocation }}</span>
      </div>
    </div>

    <div v-if="task.status !== 'COMPLETED'" class="putaway-item__action">
      <FormField label="Actual Location">
        <FormInput
          v-model="actualLocation"
          placeholder="e.g., A-01-03"
        />
      </FormField>
      <AppButton
        size="sm"
        :disabled="!actualLocation.trim()"
        @click="handleConfirm"
      >
        Confirm Putaway
      </AppButton>
    </div>
  </div>
</template>

<style scoped>
.putaway-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.putaway-item--completed {
  background: #f0fdf4;
  border-color: #d1fae5;
}

.putaway-item__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.putaway-item__name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.putaway-item__details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 12px;
}

.putaway-item__detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.putaway-item__label {
  font-size: 12px;
  color: #6b7280;
}

.putaway-item__value {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.putaway-item__action {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.putaway-item__action > :first-child {
  flex: 1;
}
</style>
