<script setup lang="ts">
import { computed } from 'vue'
import { useWorkflow } from '../composables/useWorkflow'

interface Props {
  currentStatus: string
}

const props = defineProps<Props>()

const { getStatusColor, getStepIndex, allSteps } = useWorkflow()

const currentIdx = computed(() => getStepIndex(props.currentStatus))
const isRejected = computed(() => props.currentStatus.toUpperCase() === 'REJECTED')

function stepLabel(step: string): string {
  return step.charAt(0) + step.slice(1).toLowerCase()
}
</script>

<template>
  <div class="workflow-progress">
    <div
      v-for="(step, i) in allSteps"
      :key="step"
      class="workflow-step"
    >
      <div
        :class="[
          'workflow-step__dot',
          {
            'workflow-step__dot--completed': i < currentIdx,
            'workflow-step__dot--active': i === currentIdx && !isRejected,
            'workflow-step__dot--rejected': isRejected && i === currentIdx,
          }
        ]"
      >
        <svg v-if="i < currentIdx" width="14" height="14" viewBox="0 0 16 16" fill="currentColor">
          <path d="M6.5 11.5L3 8l1-1 2.5 2.5L12 4l1 1-6.5 6.5z" />
        </svg>
        <span v-else>{{ i + 1 }}</span>
      </div>
      <span
        :class="[
          'workflow-step__label',
          { 'workflow-step__label--active': i === currentIdx }
        ]"
      >
        {{ stepLabel(step) }}
      </span>
      <div v-if="i < allSteps.length - 1" class="workflow-step__connector">
        <div
          :class="[
            'workflow-step__line',
            { 'workflow-step__line--filled': i < currentIdx }
          ]"
        ></div>
      </div>
    </div>

    <div v-if="isRejected" class="workflow-rejected">
      <div class="workflow-step__dot workflow-step__dot--rejected">
        <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor">
          <path d="M4.5 4.5l7 7M11.5 4.5l-7 7" />
        </svg>
      </div>
      <span class="workflow-step__label workflow-step__label--rejected">Rejected</span>
    </div>
  </div>
</template>

<style scoped>
.workflow-progress {
  display: flex;
  align-items: flex-start;
  gap: 0;
  overflow-x: auto;
  padding: 8px 0;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
  min-width: 80px;
}

.workflow-step__dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  background: #e5e7eb;
  color: #6b7280;
  position: relative;
  z-index: 1;
}

.workflow-step__dot--completed {
  background: #059669;
  color: #fff;
}

.workflow-step__dot--active {
  background: #4f46e5;
  color: #fff;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.2);
}

.workflow-step__dot--rejected {
  background: #dc2626;
  color: #fff;
}

.workflow-step__label {
  font-size: 11px;
  color: #6b7280;
  margin-top: 6px;
  text-align: center;
  font-weight: 500;
}

.workflow-step__label--active {
  color: #4f46e5;
  font-weight: 600;
}

.workflow-step__label--rejected {
  color: #dc2626;
  font-weight: 600;
}

.workflow-step__connector {
  position: absolute;
  top: 16px;
  left: 50%;
  width: 100%;
  height: 2px;
  z-index: 0;
}

.workflow-step__line {
  height: 100%;
  background: #e5e7eb;
}

.workflow-step__line--filled {
  background: #059669;
}

.workflow-rejected {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
}
</style>
