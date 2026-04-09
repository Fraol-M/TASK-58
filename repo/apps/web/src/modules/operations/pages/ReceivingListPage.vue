<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useReceiving } from '../composables/useReceiving'
import { useOperationsStore } from '../store'
import type { ReceiptType } from '@/services/adapters/api-adapter.interface'
import { useToast } from '@/composables/useToast'
import ReceivingTable from '../components/ReceivingTable.vue'
import ReceiptForm from '../components/ReceiptForm.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const router = useRouter()
const toast = useToast()
const { receipts, loading, error, statusFilter, setFilter, refresh } = useReceiving()

const showModal = ref(false)
const creating = ref(false)

const statusTabs = [
  { key: 'all', label: 'All' },
  { key: 'DRAFT', label: 'Draft' },
  { key: 'RECEIVING', label: 'Receiving' },
  { key: 'INSPECTION', label: 'Inspection' },
  { key: 'PUTAWAY', label: 'Putaway' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'REJECTED', label: 'Rejected' },
]

const store = useOperationsStore()

function handleRowClick(id: number) {
  router.push({ name: 'opsReceiptDetail', params: { receiptId: String(id) } })
}

async function handleCreate(data: { type: string; referenceNumber: string; supplier: string }) {
  creating.value = true
  try {
    await store.createReceipt({
      receiptType: data.type as ReceiptType,
      referenceNumber: data.referenceNumber,
      supplierName: data.supplier,
    })
    toast.show('Receipt created successfully!', 'success')
    showModal.value = false
    refresh()
  } catch {
    toast.show('Failed to create receipt', 'error')
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div class="receiving-list">
    <div class="receiving-list__header">
      <h1 class="page-title">Receiving Workspace</h1>
      <AppButton @click="showModal = true">New Receipt</AppButton>
    </div>

    <!-- Status filter tabs -->
    <div class="receiving-list__tabs">
      <button
        v-for="tab in statusTabs"
        :key="tab.key"
        :class="['tab-btn', { 'tab-btn--active': statusFilter === tab.key }]"
        @click="setFilter(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <LoadingState v-if="loading" text="Loading receipts..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />
    <EmptyState
      v-else-if="receipts.length === 0"
      title="No receipts found"
      :message="statusFilter === 'all' ? 'Create your first receiving receipt to get started.' : 'No receipts match the selected filter.'"
      :action-text="statusFilter === 'all' ? 'New Receipt' : undefined"
      @action="showModal = true"
    />
    <ReceivingTable
      v-else
      :receipts="receipts"
      @row-click="handleRowClick"
    />

    <AppModal :open="showModal" title="New Receipt" @close="showModal = false">
      <ReceiptForm :loading="creating" @submit="handleCreate" />
    </AppModal>
  </div>
</template>

<style scoped>
.receiving-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.receiving-list__tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 24px;
  overflow-x: auto;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0;
}

.tab-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  color: #6b7280;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.15s, border-color 0.15s;
}

.tab-btn:hover {
  color: #374151;
}

.tab-btn--active {
  color: #4f46e5;
  border-bottom-color: #4f46e5;
}
</style>
