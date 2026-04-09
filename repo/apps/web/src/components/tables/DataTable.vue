<script setup lang="ts">
interface Column {
  key: string
  label: string
  sortable?: boolean
  width?: string
}

interface Props {
  columns: Column[]
  data: Record<string, unknown>[]
  loading?: boolean
  emptyMessage?: string
}

withDefaults(defineProps<Props>(), {
  loading: false,
  emptyMessage: 'No data available',
})

const emit = defineEmits<{
  sort: [key: string]
}>()
</script>

<template>
  <div class="data-table-wrapper">
    <table class="data-table">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :style="col.width ? { width: col.width } : undefined"
            :class="{ 'data-table__sortable': col.sortable }"
            @click="col.sortable ? emit('sort', col.key) : undefined"
          >
            {{ col.label }}
            <span v-if="col.sortable" class="data-table__sort-icon">&#8597;</span>
          </th>
        </tr>
      </thead>
      <tbody>
        <!-- Loading state -->
        <tr v-if="loading">
          <td :colspan="columns.length" class="data-table__status">
            <div class="data-table__spinner"></div>
            Loading...
          </td>
        </tr>
        <!-- Empty state -->
        <tr v-else-if="data.length === 0">
          <td :colspan="columns.length" class="data-table__status">
            {{ emptyMessage }}
          </td>
        </tr>
        <!-- Data rows -->
        <tr v-else v-for="(row, index) in data" :key="index">
          <td v-for="col in columns" :key="col.key">
            <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
              {{ row[col.key] ?? '-' }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.data-table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table th {
  text-align: left;
  padding: 10px 12px;
  font-weight: 600;
  color: #6b7280;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #e5e7eb;
  white-space: nowrap;
  background: #f9fafb;
}

.data-table__sortable {
  cursor: pointer;
  user-select: none;
}
.data-table__sortable:hover {
  color: #374151;
}

.data-table__sort-icon {
  margin-left: 4px;
  font-size: 10px;
}

.data-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
  color: #111827;
}

.data-table tbody tr:hover {
  background: #f9fafb;
}

.data-table__status {
  text-align: center;
  padding: 32px 12px;
  color: #6b7280;
}

.data-table__spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e5e7eb;
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: table-spin 0.6s linear infinite;
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

@keyframes table-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
