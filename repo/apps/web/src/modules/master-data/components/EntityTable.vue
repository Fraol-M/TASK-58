<script setup lang="ts">
import { computed } from 'vue'
import type { MasterDataItem } from '@/services/adapters/api-adapter.interface'
import DataTable from '@/components/tables/DataTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppButton from '@/components/common/AppButton.vue'

interface Props {
  items: MasterDataItem[]
  entityType: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  edit: [item: MasterDataItem]
  delete: [item: MasterDataItem]
}>()

const columns = computed(() => {
  const base = [
    { key: 'code', label: 'Code', sortable: true },
    { key: 'name', label: 'Name', sortable: true },
    { key: 'type', label: 'Type' },
    { key: 'active', label: 'Status' },
    { key: 'actions', label: '', width: '120px' },
  ]
  return base
})

const tableData = computed(() =>
  props.items.map(item => ({
    ...item,
    _raw: item,
  }))
)
</script>

<template>
  <DataTable
    :columns="columns"
    :data="tableData"
    :loading="loading"
    empty-message="No items found for this entity type."
  >
    <template #cell-type="{ row }">
      <AppBadge :label="String(row.type)" variant="info" />
    </template>
    <template #cell-active="{ row }">
      <AppBadge
        :label="row.active ? 'Active' : 'Inactive'"
        :variant="row.active ? 'success' : 'neutral'"
      />
    </template>
    <template #cell-actions="{ row }">
      <div class="entity-actions">
        <AppButton variant="ghost" size="sm" @click="emit('edit', row._raw as any)">
          Edit
        </AppButton>
        <AppButton
          variant="ghost"
          size="sm"
          :disabled="false"
          @click="emit('delete', row._raw as any)"
        >
          Delete
        </AppButton>
      </div>
    </template>
  </DataTable>
</template>

<style scoped>
.entity-actions {
  display: flex;
  gap: 4px;
}
</style>
