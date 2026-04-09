<script setup lang="ts">
interface Props {
  label?: string
  error?: string
  required?: boolean
  hint?: string
}

defineProps<Props>()
</script>

<template>
  <div class="form-field">
    <label v-if="label" class="form-field__label">
      {{ label }}
      <span v-if="required" class="form-field__required">*</span>
    </label>
    <slot />
    <p v-if="error" class="form-field__error">{{ error }}</p>
    <p v-else-if="hint" class="form-field__hint">{{ hint }}</p>
  </div>
</template>

<style scoped>
.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.form-field__label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-field__required {
  color: #dc2626;
  margin-left: 2px;
}

.form-field__error {
  font-size: 13px;
  color: #dc2626;
  margin: 0;
}

.form-field__hint {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}
</style>
