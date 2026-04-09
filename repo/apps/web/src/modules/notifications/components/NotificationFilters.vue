<script setup lang="ts">
interface Props {
  typeFilter: string
  readFilter: string
}

defineProps<Props>()

const emit = defineEmits<{
  'update:typeFilter': [value: string]
  'update:readFilter': [value: string]
}>()

const typeOptions = [
  { value: '', label: 'All Types' },
  { value: 'ANNOUNCEMENT', label: 'Announcements' },
  { value: 'REMINDER', label: 'Reminders' },
  { value: 'FOLLOW_UP', label: 'Follow-ups' },
]

const readOptions = [
  { value: '', label: 'All' },
  { value: 'unread', label: 'Unread' },
  { value: 'read', label: 'Read' },
]
</script>

<template>
  <div class="notification-filters">
    <div class="filter-group">
      <button
        v-for="opt in typeOptions"
        :key="opt.value"
        :class="['filter-btn', { 'filter-btn--active': typeFilter === opt.value }]"
        @click="emit('update:typeFilter', opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>
    <div class="filter-group">
      <button
        v-for="opt in readOptions"
        :key="opt.value"
        :class="['filter-btn', { 'filter-btn--active': readFilter === opt.value }]"
        @click="emit('update:readFilter', opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.notification-filters {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  gap: 4px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.filter-btn {
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  background: #fff;
  color: #6b7280;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.filter-btn:not(:last-child) {
  border-right: 1px solid #e5e7eb;
}

.filter-btn--active {
  background: #4f46e5;
  color: #fff;
}
</style>
