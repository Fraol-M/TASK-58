<script setup lang="ts">
interface Props {
  variant?: 'spinner' | 'skeleton'
  text?: string
}

withDefaults(defineProps<Props>(), {
  variant: 'spinner',
  text: 'Loading...',
})
</script>

<template>
  <div class="loading-state">
    <template v-if="variant === 'spinner'">
      <div class="loading-state__spinner"></div>
      <p v-if="text" class="loading-state__text">{{ text }}</p>
    </template>
    <template v-else>
      <div class="loading-state__skeleton">
        <div class="skeleton-line skeleton-line--wide"></div>
        <div class="skeleton-line skeleton-line--medium"></div>
        <div class="skeleton-line skeleton-line--narrow"></div>
        <div class="skeleton-line skeleton-line--wide"></div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-state__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e5e7eb;
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: loading-spin 0.7s linear infinite;
}

.loading-state__text {
  margin-top: 12px;
  font-size: 14px;
  color: #6b7280;
}

.loading-state__skeleton {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-line {
  height: 14px;
  background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: skeleton-shimmer 1.5s infinite;
}

.skeleton-line--wide {
  width: 100%;
}
.skeleton-line--medium {
  width: 75%;
}
.skeleton-line--narrow {
  width: 50%;
}

@keyframes loading-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes skeleton-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
