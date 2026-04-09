<script setup lang="ts">
interface Props {
  title?: string
  padding?: 'none' | 'sm' | 'md' | 'lg'
}

withDefaults(defineProps<Props>(), {
  padding: 'md',
})
</script>

<template>
  <div :class="['app-card', `app-card--pad-${padding}`]">
    <div v-if="$slots.header || title" class="app-card__header">
      <slot name="header">
        <h3 class="app-card__title">{{ title }}</h3>
      </slot>
    </div>
    <div class="app-card__body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="app-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<style scoped>
.app-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.app-card__header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.app-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.app-card--pad-none .app-card__body {
  padding: 0;
}
.app-card--pad-sm .app-card__body {
  padding: 12px;
}
.app-card--pad-md .app-card__body {
  padding: 20px;
}
.app-card--pad-lg .app-card__body {
  padding: 32px;
}

.app-card__footer {
  padding: 12px 20px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 8px 8px;
}
</style>
