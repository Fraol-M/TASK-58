<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDiscrepancies } from '../composables/useDiscrepancies'
import { operationsApi } from '../api'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/modules/auth/store'
import { isAdmin } from '@/utils/role-checks'
import DiscrepancyCard from '../components/DiscrepancyCard.vue'
import DiscrepancyResolution from '../components/DiscrepancyResolution.vue'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const {
  discrepancies,
  loading,
  error,
  filterResolved,
  filterSupervisor,
} = useDiscrepancies()

const toast = useToast()
const api = operationsApi()
const authStore = useAuthStore()
const expandedId = ref<number | null>(null)
const canResolve = computed(() => isAdmin(authStore.user))

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

async function handleResolve(discrepancyId: number, data: { reasonCode: string; notes: string }) {
  const disc = discrepancies.value.find(d => d.id === discrepancyId)
  if (!disc) return
  try {
    await api.supervisorReview({
      receiptId: disc.receiptId,
      discrepancyId,
      reasonCode: data.reasonCode,
      notes: data.notes,
    })
    toast.show('Discrepancy resolved', 'success')
    expandedId.value = null
  } catch {
    toast.show('Failed to resolve discrepancy', 'error')
  }
}

const filterOptions: Array<{ key: 'all' | 'resolved' | 'unresolved'; label: string }> = [
  { key: 'all', label: 'All' },
  { key: 'unresolved', label: 'Unresolved' },
  { key: 'resolved', label: 'Resolved' },
]
</script>

<template>
  <div class="discrepancies-page">
    <h1 class="page-title">Discrepancies</h1>

    <div class="discrepancies-page__filters">
      <div class="filter-tabs">
        <button
          v-for="opt in filterOptions"
          :key="opt.key"
          :class="['filter-tab', { 'filter-tab--active': filterResolved === opt.key }]"
          @click="filterResolved = opt.key"
        >
          {{ opt.label }}
        </button>
      </div>
      <label class="supervisor-filter">
        <input type="checkbox" v-model="filterSupervisor" />
        <span>Supervisor required only</span>
      </label>
    </div>

    <LoadingState v-if="loading" text="Loading discrepancies..." />
    <ErrorState v-else-if="error" :message="error.message" />
    <EmptyState
      v-else-if="discrepancies.length === 0"
      title="No discrepancies"
      message="No discrepancies match your current filters."
    />

    <div v-else class="discrepancies-page__list">
      <div v-for="d in discrepancies" :key="d.id" class="discrepancy-wrapper">
        <DiscrepancyCard :discrepancy="d" />
        <div v-if="!d.resolved && canResolve" class="discrepancy-wrapper__expand">
          <AppButton
            variant="ghost"
            size="sm"
            @click="toggleExpand(d.id)"
          >
            {{ expandedId === d.id ? 'Close' : 'Resolve' }}
          </AppButton>
        </div>
        <div v-if="expandedId === d.id" class="discrepancy-wrapper__resolution">
          <DiscrepancyResolution
            :supervisor-required="d.supervisorRequired"
            @resolve="(resolveData: any) => handleResolve(d.id, resolveData)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 20px;
}

.discrepancies-page__filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-tabs {
  display: flex;
  gap: 4px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.filter-tab {
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  background: #fff;
  color: #6b7280;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.filter-tab:not(:last-child) {
  border-right: 1px solid #e5e7eb;
}

.filter-tab--active {
  background: #4f46e5;
  color: #fff;
}

.supervisor-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
}

.supervisor-filter input {
  accent-color: #4f46e5;
}

.discrepancies-page__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.discrepancy-wrapper__expand {
  margin-top: 8px;
  text-align: right;
}

.discrepancy-wrapper__resolution {
  margin-top: 12px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}
</style>
