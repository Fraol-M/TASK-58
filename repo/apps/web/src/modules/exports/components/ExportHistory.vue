<script setup lang="ts">
import DataTable from '@/components/tables/DataTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import { formatDate } from '@/utils/format-date'

interface ExportRow {
  type: string
  status: string
  createdAt: string
  downloadUrl?: string
}

interface Props {
  exports: ExportRow[]
}

defineProps<Props>()

const columns = [
  { key: 'type', label: 'Export Type' },
  { key: 'status', label: 'Status' },
  { key: 'date', label: 'Requested' },
  { key: 'download', label: '' },
]

function statusVariant(status: string): 'success' | 'warning' | 'danger' | 'neutral' {
  if (status === 'completed') return 'success'
  if (status === 'processing' || status === 'pending') return 'warning'
  if (status === 'failed') return 'danger'
  return 'neutral'
}
</script>

<template>
  <DataTable
    :columns="columns"
    :data="exports.map(e => ({
      type: e.type,
      status: e.status,
      date: formatDate(e.createdAt, 'MMM DD, YYYY'),
      downloadUrl: e.downloadUrl,
    }))"
    empty-message="No export history"
  >
    <template #cell-status="{ value }">
      <AppBadge :label="String(value)" :variant="statusVariant(String(value))" />
    </template>
    <template #cell-download="{ row }">
      <a
        v-if="row.downloadUrl"
        :href="String(row.downloadUrl)"
        class="download-link"
        target="_blank"
      >
        Download
      </a>
    </template>
  </DataTable>
</template>

<style scoped>
.download-link {
  color: #4f46e5;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
}

.download-link:hover {
  text-decoration: underline;
}
</style>
