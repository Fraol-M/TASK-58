<script setup lang="ts">
interface Props {
  modelValue: boolean
  label?: string
  disabled?: boolean
}

withDefaults(defineProps<Props>(), {
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

function onChange(e: Event) {
  emit('update:modelValue', (e.target as HTMLInputElement).checked)
}
</script>

<template>
  <label class="form-checkbox" :class="{ 'form-checkbox--disabled': disabled }">
    <input
      type="checkbox"
      :checked="modelValue"
      :disabled="disabled"
      class="form-checkbox__input"
      @change="onChange"
    />
    <span v-if="label" class="form-checkbox__label">{{ label }}</span>
  </label>
</template>

<style scoped>
.form-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
}

.form-checkbox--disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.form-checkbox__input {
  width: 16px;
  height: 16px;
  accent-color: #4f46e5;
  cursor: inherit;
}

.form-checkbox__label {
  user-select: none;
}
</style>
