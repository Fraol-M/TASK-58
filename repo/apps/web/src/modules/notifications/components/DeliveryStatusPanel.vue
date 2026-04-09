<script setup lang="ts">
import { ref, watch } from 'vue'
import { notificationsApi } from '../api'
import AppCard from '@/components/common/AppCard.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import { formatRelative } from '@/utils/format-date'

interface DeliveryStatus {
  targetId: number
  userId: number
  read: boolean
  readAt?: string
  deliveredAt?: string
  channel: string
}

interface Props {
  notificationId: number | null
}

const props = defineProps<Props>()

const statuses = ref<DeliveryStatus[]>([])
const loading = ref(false)
const api = notificationsApi()

watch(() => props.notificationId, async (id) => {
  if (!id) {
    statuses.value = []
    return
  }
  loading.value = true
  try {
    const res = await api.getDeliveryStatus(id)
    statuses.value = (res.data as unknown as DeliveryStatus[]) ?? []
  } catch {
    statuses.value = []
  } finally {
    loading.value = false
  }
}, { immediate: true })
</script>

<template>
  <AppCard v-if="notificationId" title="Delivery & Read Receipts" class="delivery-panel">
    <div v-if="loading" class="delivery-panel__loading">Loading status...</div>
    <div v-else-if="statuses.length === 0" class="delivery-panel__empty">
      No delivery records found.
    </div>
    <table v-else class="delivery-panel__table">
      <thead>
        <tr>
          <th>User</th>
          <th>Channel</th>
          <th>Delivered</th>
          <th>Read</th>
          <th>Read At</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in statuses" :key="s.targetId">
          <td>User #{{ s.userId }}</td>
          <td>{{ s.channel }}</td>
          <td>
            <AppBadge
              :label="s.deliveredAt ? 'Delivered' : 'Pending'"
              :variant="s.deliveredAt ? 'success' : 'neutral'"
            />
          </td>
          <td>
            <AppBadge
              :label="s.read ? 'Read' : 'Unread'"
              :variant="s.read ? 'success' : 'warning'"
            />
          </td>
          <td>{{ s.readAt ? formatRelative(s.readAt) : '-' }}</td>
        </tr>
      </tbody>
    </table>
  </AppCard>
</template>

<style scoped>
.delivery-panel {
  margin-top: 24px;
}

.delivery-panel__loading,
.delivery-panel__empty {
  text-align: center;
  color: #9ca3af;
  padding: 16px 0;
  font-size: 14px;
}

.delivery-panel__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.delivery-panel__table th {
  text-align: left;
  padding: 8px 12px;
  font-weight: 600;
  color: #6b7280;
  font-size: 12px;
  text-transform: uppercase;
  border-bottom: 2px solid #e5e7eb;
}

.delivery-panel__table td {
  padding: 8px 12px;
  border-bottom: 1px solid #e5e7eb;
  color: #111827;
}
</style>
