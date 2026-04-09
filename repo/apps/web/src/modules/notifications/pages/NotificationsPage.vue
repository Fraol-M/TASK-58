<script setup lang="ts">
import { ref, computed } from 'vue'
import { useNotifications } from '../composables/useNotifications'
import { useAuthStore } from '@/modules/auth/store'
import { isAdmin } from '@/utils/role-checks'
import NotificationItem from '../components/NotificationItem.vue'
import NotificationFilters from '../components/NotificationFilters.vue'
import DeliveryStatusPanel from '../components/DeliveryStatusPanel.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppCard from '@/components/common/AppCard.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const authStore = useAuthStore()
const showAdmin = computed(() => isAdmin(authStore.user))

const { notifications, unreadCount, loading, error, markAsRead, markAllRead, refresh } =
  useNotifications()

const typeFilter = ref('')
const readFilter = ref('')
const selectedNotificationId = ref<number | null>(null)

const filteredNotifications = computed(() => {
  let result = notifications.value

  if (typeFilter.value) {
    result = result.filter(n => n.type === typeFilter.value)
  }

  if (readFilter.value === 'unread') {
    result = result.filter(n => !n.read)
  } else if (readFilter.value === 'read') {
    result = result.filter(n => n.read)
  }

  return result
})

function handleClick(id: number) {
  markAsRead(id)
  if (showAdmin.value) {
    // For admins, find the notification's notificationId and show delivery status
    const notification = notifications.value.find(n => n.id === id)
    selectedNotificationId.value = notification?.notificationId ?? null
  }
}
</script>

<template>
  <div class="notifications-page">
    <div class="notifications-page__header">
      <h1 class="page-title">
        Notifications
        <span v-if="unreadCount > 0" class="page-title__badge">{{ unreadCount }}</span>
      </h1>
      <AppButton
        v-if="unreadCount > 0"
        variant="secondary"
        size="sm"
        @click="markAllRead"
      >
        Mark All Read
      </AppButton>
    </div>

    <NotificationFilters
      v-model:type-filter="typeFilter"
      v-model:read-filter="readFilter"
    />

    <LoadingState v-if="loading" text="Loading notifications..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />

    <AppCard v-else padding="none" class="notifications-page__list">
      <EmptyState
        v-if="filteredNotifications.length === 0"
        title="No notifications"
        message="You're all caught up! No notifications to show."
      />
      <div v-else>
        <NotificationItem
          v-for="n in filteredNotifications"
          :key="n.id"
          :notification="n"
          @click="handleClick"
        />
      </div>
    </AppCard>

    <!-- Admin: Delivery & Read Receipt Tracking -->
    <DeliveryStatusPanel
      v-if="showAdmin"
      :notification-id="selectedNotificationId"
    />
  </div>
</template>

<style scoped>
.notifications-page__header {
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title__badge {
  font-size: 13px;
  font-weight: 600;
  background: #4f46e5;
  color: #fff;
  padding: 2px 8px;
  border-radius: 9999px;
}

.notifications-page__list {
  margin-top: 20px;
}
</style>
