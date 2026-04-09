<script setup lang="ts">
interface Props {
  loading?: boolean
  disabled?: boolean
  text?: string
  loadingText?: string
}

withDefaults(defineProps<Props>(), {
  loading: false,
  disabled: false,
  text: 'Submit',
  loadingText: 'Submitting...',
})
</script>

<template>
  <button
    type="submit"
    class="submit-btn"
    :disabled="disabled || loading"
  >
    <span v-if="loading" class="submit-btn__spinner" aria-hidden="true"></span>
    <span>{{ loading ? loadingText : text }}</span>
  </button>
</template>

<style scoped>
.submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 10px 20px;
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  color: #fff;
  background-color: #4f46e5;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s, opacity 0.15s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #4338ca;
}

.submit-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.submit-btn__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: submit-spin 0.6s linear infinite;
}

@keyframes submit-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
