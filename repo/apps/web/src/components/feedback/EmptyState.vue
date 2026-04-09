<script setup lang="ts">
interface Props {
  title?: string
  message?: string
  actionText?: string
}

withDefaults(defineProps<Props>(), {
  title: 'Nothing here yet',
  message: 'Get started by creating your first item.',
})

const emit = defineEmits<{
  action: []
}>()
</script>

<template>
  <div class="empty-state">
    <svg class="empty-state__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <path d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0l-4-4m-8 4l-4-4" />
    </svg>
    <h3 class="empty-state__title">{{ title }}</h3>
    <p class="empty-state__message">{{ message }}</p>
    <button
      v-if="actionText"
      class="empty-state__action"
      @click="emit('action')"
    >
      {{ actionText }}
    </button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 48px 20px;
}

.empty-state__icon {
  width: 48px;
  height: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-state__title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 4px;
}

.empty-state__message {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 20px;
  max-width: 300px;
}

.empty-state__action {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  color: #fff;
  background-color: #4f46e5;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s;
}
.empty-state__action:hover {
  background-color: #4338ca;
}
</style>
