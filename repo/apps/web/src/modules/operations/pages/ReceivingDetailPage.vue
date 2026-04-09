<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useReceiptDetail } from '../composables/useReceiptDetail'
import { useWorkflow } from '../composables/useWorkflow'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/modules/auth/store'
import { isAdmin } from '@/utils/role-checks'
import type { ReceiptStatus } from '@/services/adapters/api-adapter.interface'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import ConfirmDialog from '@/components/feedback/ConfirmDialog.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import WorkflowProgressIndicator from '../components/WorkflowProgressIndicator.vue'
import StatusBadge from '../components/StatusBadge.vue'
import ReceivingLineItems from '../components/ReceivingLineItems.vue'
import AddLineForm from '../components/AddLineForm.vue'
import InspectionForm from '../components/InspectionForm.vue'
import DiscrepancyCard from '../components/DiscrepancyCard.vue'
import PutawayItem from '../components/PutawayItem.vue'

const route = useRoute()
const toast = useToast()
const receiptId = Number(route.params.receiptId)

const { receipt, discrepancies, putawayTasks, loading, error, transition, addLine, receiveLine, inspect, putaway, post, unpost, refresh } =
  useReceiptDetail(receiptId)

const { getAvailableTransitions } = useWorkflow()
const authStore = useAuthStore()
const canUnpost = computed(() => isAdmin(authStore.user))

const confirmOpen = ref(false)
const pendingTransition = ref<{ status: ReceiptStatus; reason: string } | null>(null)
const addingLine = ref(false)

const availableTransitions = computed(() => {
  if (!receipt.value) return []
  return getAvailableTransitions(receipt.value.status)
})

const isDraft = computed(() => {
  return receipt.value?.status === 'DRAFT'
})

const isReceiving = computed(() => {
  return receipt.value?.status === 'RECEIVING'
})

const isInspection = computed(() => {
  return receipt.value?.status === 'INSPECTION'
})

const isPutaway = computed(() => {
  return receipt.value?.status === 'PUTAWAY'
})

function requestTransition(targetState: ReceiptStatus, reason: string) {
  pendingTransition.value = { status: targetState, reason }
  confirmOpen.value = true
}

async function confirmTransition() {
  if (!pendingTransition.value) return
  confirmOpen.value = false
  try {
    await transition(pendingTransition.value.status)
    toast.show('Receipt status updated', 'success')
  } catch {
    toast.show('Failed to update status', 'error')
  }
  pendingTransition.value = null
}

async function handleAddLine(data: { itemCode: string; itemName: string; expectedQty: number; unitCost?: number }) {
  addingLine.value = true
  try {
    await addLine(data)
    toast.show('Line item added', 'success')
  } catch {
    toast.show('Failed to add line item', 'error')
  } finally {
    addingLine.value = false
  }
}

async function handleUpdateQty(index: number, qty: number) {
  if (!receipt.value) return
  const line = receipt.value.lines[index]
  if (!line) return
  try {
    await receiveLine(line.id, qty)
    toast.show(`Received qty updated for ${line.itemCode}`, 'success')
  } catch {
    toast.show('Failed to update received quantity', 'error')
  }
}

async function handleInspection(data: { lineId: number; inspectedQty: number; result: 'PASS' | 'FAIL'; notes?: string }) {
  try {
    await inspect(data.lineId, data.inspectedQty, data.result, data.notes)
    toast.show('Inspection completed', 'success')
  } catch {
    toast.show('Inspection failed', 'error')
  }
}

async function handlePutawayConfirm(taskId: number, location: string) {
  try {
    await putaway(taskId, location)
    toast.show(`Putaway confirmed: ${location}`, 'success')
  } catch {
    toast.show('Putaway failed', 'error')
  }
}

async function handlePost() {
  try {
    await post()
    toast.show('Receipt posted to inventory', 'success')
  } catch {
    toast.show('Failed to post receipt', 'error')
  }
}

async function handleUnpost() {
  try {
    await unpost()
    toast.show('Receipt unposted from inventory', 'success')
  } catch {
    toast.show('Failed to unpost receipt', 'error')
  }
}
</script>

<template>
  <div class="receipt-detail">
    <LoadingState v-if="loading" text="Loading receipt..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />

    <template v-else-if="receipt">
      <!-- Header -->
      <div class="receipt-detail__header">
        <div>
          <h1 class="page-title">{{ receipt.receiptNumber }}</h1>
          <p class="receipt-detail__subtitle">
            {{ receipt.supplierName }} &middot; <StatusBadge :status="receipt.status" />
          </p>
        </div>
        <div class="receipt-detail__actions">
          <AppButton
            v-for="t in availableTransitions"
            :key="t.targetState"
            :variant="t.targetState === 'REJECTED' ? 'danger' : 'primary'"
            size="sm"
            @click="requestTransition(t.targetState, t.reason ?? '')"
          >
            {{ t.reason || t.targetState }}
          </AppButton>
        </div>
      </div>

      <!-- Workflow -->
      <AppCard class="receipt-detail__workflow">
        <WorkflowProgressIndicator :current-status="receipt.status" />
      </AppCard>

      <!-- Add Line (DRAFT only) -->
      <AppCard v-if="isDraft" title="Add Line Item" class="receipt-detail__section">
        <AddLineForm :loading="addingLine" @submit="handleAddLine" />
      </AppCard>

      <!-- Line Items -->
      <AppCard title="Line Items" padding="none" class="receipt-detail__section">
        <ReceivingLineItems
          :items="receipt.lines"
          :editable="isReceiving"
          @update-qty="handleUpdateQty"
        />
      </AppCard>

      <!-- Inspection Form -->
      <AppCard v-if="isInspection" title="Inspection" class="receipt-detail__section">
        <InspectionForm :items="receipt.lines" @submit="handleInspection" />
      </AppCard>

      <!-- Discrepancies -->
      <AppCard
        v-if="discrepancies.length > 0"
        title="Discrepancies"
        class="receipt-detail__section"
      >
        <div class="receipt-detail__discrepancies">
          <DiscrepancyCard
            v-for="d in discrepancies"
            :key="d.id"
            :discrepancy="d"
          />
        </div>
      </AppCard>

      <!-- Posting Controls -->
      <AppCard
        v-if="receipt.status === 'COMPLETED'"
        title="Post to Inventory"
        class="receipt-detail__section"
      >
        <p class="receipt-detail__post-hint">All putaway is complete. Post this receipt to finalize inventory records.</p>
        <AppButton variant="primary" @click="handlePost">Post Receipt</AppButton>
      </AppCard>

      <AppCard
        v-if="receipt.status === 'POSTED' && canUnpost"
        title="Unpost Receipt"
        class="receipt-detail__section"
      >
        <p class="receipt-detail__post-hint">This receipt is posted. Unposting reverses the inventory finalization (admin only).</p>
        <AppButton variant="danger" @click="handleUnpost">Unpost Receipt</AppButton>
      </AppCard>

      <!-- Putaway -->
      <AppCard v-if="isPutaway" title="Putaway Tasks" class="receipt-detail__section">
        <div class="receipt-detail__putaway">
          <PutawayItem
            v-for="t in putawayTasks"
            :key="t.id"
            :task="t"
            @confirm="handlePutawayConfirm"
          />
        </div>
        <div v-if="putawayTasks.length === 0" class="receipt-detail__empty">
          No putaway tasks assigned yet.
        </div>
      </AppCard>
    </template>

    <!-- Confirm Dialog -->
    <ConfirmDialog
      :open="confirmOpen"
      title="Confirm Transition"
      :message="`Are you sure you want to ${pendingTransition?.reason ?? 'update the status'}?`"
      :variant="pendingTransition?.status === 'REJECTED' ? 'danger' : 'primary'"
      @confirm="confirmTransition"
      @cancel="confirmOpen = false"
    />
  </div>
</template>

<style scoped>
.receipt-detail__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.receipt-detail__subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 4px 0 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.receipt-detail__actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.receipt-detail__workflow {
  margin-bottom: 24px;
}

.receipt-detail__section {
  margin-bottom: 24px;
}

.receipt-detail__discrepancies {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.receipt-detail__putaway {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}

.receipt-detail__empty {
  text-align: center;
  color: #9ca3af;
  padding: 24px 0;
  font-size: 14px;
}

.receipt-detail__post-hint {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px;
}
</style>
