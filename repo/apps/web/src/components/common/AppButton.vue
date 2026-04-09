<script setup lang="ts">
interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  type?: 'button' | 'submit' | 'reset'
}

withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  type: 'button',
})
</script>

<template>
  <button
    :type="type"
    :class="['app-btn', `app-btn--${variant}`, `app-btn--${size}`]"
    :disabled="disabled || loading"
  >
    <span v-if="loading" class="app-btn__spinner" aria-hidden="true"></span>
    <span :class="{ 'app-btn__content--hidden': loading }">
      <slot />
    </span>
  </button>
</template>

<style scoped>
.app-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.15s, border-color 0.15s, opacity 0.15s;
  position: relative;
  font-family: inherit;
  line-height: 1.4;
}

.app-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* Sizes */
.app-btn--sm {
  padding: 6px 12px;
  font-size: 13px;
}

.app-btn--md {
  padding: 8px 16px;
  font-size: 14px;
}

.app-btn--lg {
  padding: 12px 24px;
  font-size: 16px;
}

/* Variants */
.app-btn--primary {
  background-color: #4f46e5;
  color: #fff;
  border-color: #4f46e5;
}
.app-btn--primary:hover:not(:disabled) {
  background-color: #4338ca;
}

.app-btn--secondary {
  background-color: #fff;
  color: #374151;
  border-color: #d1d5db;
}
.app-btn--secondary:hover:not(:disabled) {
  background-color: #f9fafb;
  border-color: #9ca3af;
}

.app-btn--danger {
  background-color: #dc2626;
  color: #fff;
  border-color: #dc2626;
}
.app-btn--danger:hover:not(:disabled) {
  background-color: #b91c1c;
}

.app-btn--ghost {
  background-color: transparent;
  color: #4f46e5;
  border-color: transparent;
}
.app-btn--ghost:hover:not(:disabled) {
  background-color: #eef2ff;
}

/* Spinner */
.app-btn__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: btn-spin 0.6s linear infinite;
  position: absolute;
}

.app-btn__content--hidden {
  visibility: hidden;
}

@keyframes btn-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
