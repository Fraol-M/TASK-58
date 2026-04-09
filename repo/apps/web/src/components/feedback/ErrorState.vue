<script setup lang="ts">
interface Props {
  message?: string
  retryText?: string
}

withDefaults(defineProps<Props>(), {
  message: 'Something went wrong. Please try again.',
  retryText: 'Retry',
})

const emit = defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="error-state">
    <svg class="error-state__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <circle cx="12" cy="12" r="10" />
      <path d="M12 8v4m0 4h.01" />
    </svg>
    <p class="error-state__message">{{ message }}</p>
    <button class="error-state__retry" @click="emit('retry')">
      {{ retryText }}
    </button>
  </div>
</template>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 20px;
}

.error-state__icon {
  width: 40px;
  height: 40px;
  color: #dc2626;
  margin-bottom: 12px;
}

.error-state__message {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px;
  max-width: 320px;
}

.error-state__retry {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  color: #dc2626;
  background: #fff;
  border: 1px solid #dc2626;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s;
}
.error-state__retry:hover {
  background: #fef2f2;
}
</style>
