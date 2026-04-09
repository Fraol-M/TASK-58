<script setup lang="ts">
interface KeyValueItem {
  label: string
  value: string | number
}

interface Props {
  items: KeyValueItem[]
}

defineProps<Props>()
</script>

<template>
  <dl class="kv-list">
    <div v-for="(item, i) in items" :key="i" class="kv-list__item">
      <dt class="kv-list__label">{{ item.label }}</dt>
      <dd class="kv-list__value">{{ item.value }}</dd>
    </div>
  </dl>
</template>

<style scoped>
.kv-list {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.kv-list__item {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.kv-list__item:last-child {
  border-bottom: none;
}

.kv-list__label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 400;
}

.kv-list__value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
  margin: 0;
  text-align: right;
}
</style>
