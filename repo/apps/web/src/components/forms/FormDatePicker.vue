<script setup lang="ts">
interface Props {
  modelValue: string
  placeholder?: string
  error?: string
  min?: string
  max?: string
}

withDefaults(defineProps<Props>(), {
  placeholder: 'MM/DD/YYYY',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

function onChange(e: Event) {
  emit('update:modelValue', (e.target as HTMLInputElement).value)
}
</script>

<template>
  <input
    type="date"
    :value="modelValue"
    :min="min"
    :max="max"
    :placeholder="placeholder"
    :class="['form-date', { 'form-date--error': error }]"
    @change="onChange"
  />
</template>

<style scoped>
.form-date {
  width: 100%;
  padding: 8px 12px;
  font-size: 14px;
  font-family: inherit;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #111827;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.form-date:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.form-date--error {
  border-color: #dc2626;
}
</style>
