<script setup lang="ts">
import { ref } from 'vue'
import type { MasterDataItem, MergeCandidate } from '@/services/adapters/api-adapter.interface'
import AppButton from '@/components/common/AppButton.vue'

interface Props {
  candidate: MergeCandidate
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  merge: [sourceId: number, targetId: number]
  skip: []
}>()

const selections = ref<Record<string, 'source' | 'target'>>({
  code: 'target',
  name: 'target',
})

function handleMerge(sourceId: number, targetId: number) {
  emit('merge', sourceId, targetId)
}
</script>

<template>
  <div class="merge-comparison">
    <div class="merge-comparison__header">
      <span class="merge-comparison__similarity">
        {{ Math.round(candidate.similarity * 100) }}% similar
      </span>
    </div>

    <div class="merge-comparison__grid">
      <div class="merge-comparison__col">
        <h4 class="merge-comparison__col-title">Source</h4>
        <div class="merge-field">
          <span class="merge-field__label">Code</span>
          <span class="merge-field__value">{{ candidate.sourceItem.code }}</span>
          <label class="merge-field__radio">
            <input type="radio" v-model="selections.code" value="source" />
            Keep
          </label>
        </div>
        <div class="merge-field">
          <span class="merge-field__label">Name</span>
          <span class="merge-field__value">{{ candidate.sourceItem.name }}</span>
          <label class="merge-field__radio">
            <input type="radio" v-model="selections.name" value="source" />
            Keep
          </label>
        </div>
      </div>

      <div class="merge-comparison__col">
        <h4 class="merge-comparison__col-title">Target</h4>
        <div class="merge-field">
          <span class="merge-field__label">Code</span>
          <span :class="['merge-field__value', { 'merge-field__value--diff': candidate.sourceItem.code !== candidate.targetItem.code }]">
            {{ candidate.targetItem.code }}
          </span>
          <label class="merge-field__radio">
            <input type="radio" v-model="selections.code" value="target" />
            Keep
          </label>
        </div>
        <div class="merge-field">
          <span class="merge-field__label">Name</span>
          <span :class="['merge-field__value', { 'merge-field__value--diff': candidate.sourceItem.name !== candidate.targetItem.name }]">
            {{ candidate.targetItem.name }}
          </span>
          <label class="merge-field__radio">
            <input type="radio" v-model="selections.name" value="target" />
            Keep
          </label>
        </div>
      </div>
    </div>

    <div class="merge-comparison__actions">
      <AppButton variant="secondary" @click="emit('skip')">Skip</AppButton>
      <AppButton
        :loading="loading"
        @click="handleMerge(candidate.sourceItem.id, candidate.targetItem.id)"
      >
        Merge
      </AppButton>
    </div>
  </div>
</template>

<style scoped>
.merge-comparison {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.merge-comparison__header {
  margin-bottom: 16px;
}

.merge-comparison__similarity {
  font-size: 13px;
  font-weight: 600;
  color: #d97706;
  background: #fef3c7;
  padding: 2px 10px;
  border-radius: 9999px;
}

.merge-comparison__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 16px;
}

.merge-comparison__col-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px;
}

.merge-field {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.merge-field__label {
  font-size: 11px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.merge-field__value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.merge-field__value--diff {
  background: #fef3c7;
  padding: 2px 4px;
  border-radius: 2px;
}

.merge-field__radio {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  margin-top: 4px;
}

.merge-field__radio input {
  accent-color: #4f46e5;
}

.merge-comparison__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
