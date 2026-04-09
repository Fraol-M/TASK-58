<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  page: number
  pageSize: number
  totalElements: number
  totalPages: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  pageChange: [page: number]
  pageSizeChange: [size: number]
}>()

const startItem = computed(() => props.totalElements === 0 ? 0 : props.page * props.pageSize + 1)
const endItem = computed(() => Math.min((props.page + 1) * props.pageSize, props.totalElements))

function onSizeChange(e: Event) {
  emit('pageSizeChange', Number((e.target as HTMLSelectElement).value))
}
</script>

<template>
  <div class="table-pagination">
    <div class="table-pagination__info">
      Showing {{ startItem }} - {{ endItem }} of {{ totalElements }}
    </div>
    <div class="table-pagination__controls">
      <select :value="pageSize" class="table-pagination__size" @change="onSizeChange">
        <option :value="5">5 / page</option>
        <option :value="10">10 / page</option>
        <option :value="25">25 / page</option>
        <option :value="50">50 / page</option>
      </select>
      <button
        class="table-pagination__btn"
        :disabled="page <= 0"
        @click="emit('pageChange', page - 1)"
      >
        Previous
      </button>
      <span class="table-pagination__page">
        {{ page + 1 }} / {{ totalPages }}
      </span>
      <button
        class="table-pagination__btn"
        :disabled="page >= totalPages - 1"
        @click="emit('pageChange', page + 1)"
      >
        Next
      </button>
    </div>
  </div>
</template>

<style scoped>
.table-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  font-size: 14px;
  color: #6b7280;
  flex-wrap: wrap;
  gap: 12px;
}

.table-pagination__controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-pagination__size {
  padding: 4px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 13px;
  font-family: inherit;
  background: #fff;
  color: #374151;
}

.table-pagination__btn {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  background: #fff;
  color: #374151;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: background-color 0.15s;
}

.table-pagination__btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.table-pagination__btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.table-pagination__page {
  font-size: 13px;
  font-weight: 500;
  min-width: 60px;
  text-align: center;
}
</style>
