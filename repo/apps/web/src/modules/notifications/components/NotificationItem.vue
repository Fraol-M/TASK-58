<script setup lang="ts">
import type { Notification } from '@/services/adapters/api-adapter.interface'
import { formatRelative } from '@/utils/format-date'

interface Props {
  notification: Notification
}

defineProps<Props>()

const emit = defineEmits<{
  click: [id: number]
}>()

function typeIcon(type: string): string {
  switch (type?.toLowerCase()) {
    case 'announcement': return 'megaphone'
    case 'reminder': return 'clock'
    case 'follow_up': return 'reply'
    default: return 'bell'
  }
}
</script>

<template>
  <div
    :class="['notification-item', { 'notification-item--unread': !notification.read }]"
    @click="emit('click', notification.id)"
  >
    <div class="notification-item__icon">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0" />
      </svg>
    </div>
    <div class="notification-item__content">
      <div class="notification-item__header">
        <span class="notification-item__title">{{ notification.title }}</span>
        <span v-if="!notification.read" class="notification-item__dot"></span>
      </div>
      <p class="notification-item__body">{{ notification.body }}</p>
      <span class="notification-item__time">{{ formatRelative(notification.createdAt) }}</span>
    </div>
  </div>
</template>

<style scoped>
.notification-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.15s;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item--unread {
  background: #eef2ff;
}

.notification-item--unread:hover {
  background: #e0e7ff;
}

.notification-item__icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.notification-item--unread .notification-item__icon {
  background: #c7d2fe;
  color: #4338ca;
}

.notification-item__content {
  flex: 1;
  min-width: 0;
}

.notification-item__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.notification-item__title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.notification-item--unread .notification-item__title {
  font-weight: 700;
}

.notification-item__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4f46e5;
  flex-shrink: 0;
}

.notification-item__body {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-item__time {
  font-size: 12px;
  color: #9ca3af;
}
</style>
