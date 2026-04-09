<script setup lang="ts">
import type { ReceivingReceipt } from '@/services/adapters/api-adapter.interface'
import DataTable from '@/components/tables/DataTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import StatusIndicator from '@/components/data-display/StatusIndicator.vue'
import { formatDate } from '@/utils/format-date'

interface Props {
  receipts: ReceivingReceipt[]
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  rowClick: [id: number]
}>()

const columns = [
  { key: 'receiptNumber', label: 'Receipt #', sortable: true },
  { key: 'type', label: 'Type' },
  { key: 'supplier', label: 'Supplier', sortable: true },
  { key: 'status', label: 'Status' },
  { key: 'createdAt', label: 'Created', sortable: true },
  { key: 'actions', label: '', width: '80px' },
]

function toTableData(receipts: ReceivingReceipt[]) {
  return receipts.map(r => ({
    id: r.id,
    receiptNumber: r.receiptNumber,
    type: r.status,
    supplier: r.supplierName,
    status: r.status,
    createdAt: formatDate(r.createdAt, 'MMM DD, YYYY'),
  }))
}

function typeVariant(status: string): 'info' | 'warning' | 'neutral' {
  if (status === 'in_progress' || status === 'pending') return 'warning'
  return 'info'
}
</script>

<template>
  <DataTable
    :columns="columns"
    :data="toTableData(receipts)"
    :loading="loading"
    empty-message="No receipts found"
  >
    <template #cell-type="{ row }">
      <AppBadge :label="String(row.type)" :variant="typeVariant(String(row.type))" />
    </template>
    <template #cell-status="{ row }">
      <StatusIndicator :status="String(row.status)" />
    </template>
    <template #cell-actions="{ row }">
      <button class="table-link" @click="emit('rowClick', Number(row.id))">
        View
      </button>
    </template>
  </DataTable>
</template>

<style scoped>
.table-link {
  background: none;
  border: none;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: inherit;
}

.table-link:hover {
  background: #eef2ff;
}
</style>
