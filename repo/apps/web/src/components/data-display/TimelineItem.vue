<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  date?: string
  title?: string
  variant?: 'default' | 'success' | 'warning' | 'danger'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
})

const dotColor = computed(() => {
  switch (props.variant) {
    case 'success': return '#059669'
    case 'warning': return '#d97706'
    case 'danger': return '#dc2626'
    default: return '#4f46e5'
  }
})
</script>

<template>
  <div class="timeline-item">
    <span
      class="timeline-item__dot"
      :style="{ backgroundColor: dotColor }"
    ></span>
    <div class="timeline-item__content">
      <div class="timeline-item__header">
        <span v-if="title" class="timeline-item__title">{{ title }}</span>
        <span v-if="date" class="timeline-item__date">{{ date }}</span>
      </div>
      <div class="timeline-item__body">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-item__dot {
  position: absolute;
  left: -21px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #e5e7eb;
}

.timeline-item__content {
  padding-left: 4px;
}

.timeline-item__header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 4px;
}

.timeline-item__title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.timeline-item__date {
  font-size: 12px;
  color: #9ca3af;
}

.timeline-item__body {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}
</style>
