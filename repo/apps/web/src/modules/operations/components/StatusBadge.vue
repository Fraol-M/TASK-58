<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status: string
}

const props = defineProps<Props>()

const colorConfig = computed(() => {
  const s = props.status.toLowerCase()
  switch (s) {
    case 'draft':
    case 'pending':
      return { bg: '#f3f4f6', text: '#4b5563' }
    case 'receiving':
    case 'in_progress':
      return { bg: '#dbeafe', text: '#1e40af' }
    case 'inspection':
      return { bg: '#fef3c7', text: '#92400e' }
    case 'putaway':
      return { bg: '#ede9fe', text: '#5b21b6' }
    case 'completed':
      return { bg: '#d1fae5', text: '#065f46' }
    case 'rejected':
      return { bg: '#fee2e2', text: '#991b1b' }
    default:
      return { bg: '#f3f4f6', text: '#4b5563' }
  }
})

function displayStatus(status: string): string {
  return status.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
}
</script>

<template>
  <span
    class="status-badge"
    :style="{ backgroundColor: colorConfig.bg, color: colorConfig.text }"
  >
    {{ displayStatus(status) }}
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 9999px;
  white-space: nowrap;
}
</style>
