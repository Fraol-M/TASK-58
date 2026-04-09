<script setup lang="ts">
import type { InboundLine } from '@/services/adapters/api-adapter.interface'

interface Props {
  items: InboundLine[]
  editable?: boolean
}

withDefaults(defineProps<Props>(), {
  editable: false,
})

const emit = defineEmits<{
  updateQty: [index: number, qty: number]
}>()

function hasDiscrepancy(item: InboundLine): boolean {
  return (item.receivedQty ?? 0) !== item.expectedQty && (item.receivedQty ?? 0) > 0
}

function onQtyChange(index: number, event: Event) {
  const val = Number((event.target as HTMLInputElement).value)
  emit('updateQty', index, val)
}
</script>

<template>
  <div class="line-items">
    <table class="line-items__table">
      <thead>
        <tr>
          <th>Code</th>
          <th>Name</th>
          <th class="text-right">Expected</th>
          <th class="text-right">Received</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(item, i) in items"
          :key="item.id"
          :class="{ 'line-items__row--discrepancy': hasDiscrepancy(item) }"
        >
          <td class="line-items__code">{{ item.itemCode }}</td>
          <td>{{ item.itemName }}</td>
          <td class="text-right">{{ item.expectedQty }}</td>
          <td class="text-right">
            <input
              v-if="editable"
              type="number"
              class="line-items__input"
              :value="item.receivedQty"
              min="0"
              @change="onQtyChange(i, $event)"
            />
            <span v-else>{{ item.receivedQty ?? '-' }}</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.line-items__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.line-items__table th {
  text-align: left;
  padding: 10px 12px;
  font-weight: 600;
  color: #6b7280;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #e5e7eb;
  background: #f9fafb;
}

.line-items__table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
  color: #111827;
}

.text-right {
  text-align: right;
}

.line-items__row--discrepancy {
  background: #fef2f2;
}

.line-items__row--discrepancy td {
  border-bottom-color: #fecaca;
}

.line-items__code {
  font-family: monospace;
  font-size: 13px;
  color: #6b7280;
}

.line-items__input {
  width: 80px;
  padding: 4px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  text-align: right;
  font-family: inherit;
}

.line-items__input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}
</style>
