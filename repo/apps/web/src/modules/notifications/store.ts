import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification as ApiNotification } from '@/services/adapters/api-adapter.interface'
import { notificationsApi } from './api'
import { normalizeError } from '@/utils/error-normalizer'
import type { ApiError } from '@/types/api'

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref<ApiNotification[]>([])
  const loading = ref(false)
  const error = ref<ApiError | null>(null)

  const unreadCount = computed(() =>
    notifications.value.filter(n => !n.read).length
  )

  const api = notificationsApi()

  async function fetchNotifications() {
    loading.value = true
    error.value = null
    try {
      const res = await api.getNotifications()
      notifications.value = res.data?.content ?? (Array.isArray(res.data) ? res.data : [])
    } catch (e) {
      error.value = normalizeError(e)
    } finally {
      loading.value = false
    }
  }

  async function markAsRead(id: number) {
    try {
      await api.markRead(id)
      const n = notifications.value.find(n => n.id === id)
      if (n) n.read = true
    } catch (e) {
      error.value = normalizeError(e)
    }
  }

  async function markAllRead() {
    for (const n of notifications.value.filter(n => !n.read)) {
      await markAsRead(n.id)
    }
  }

  return {
    notifications,
    unreadCount,
    loading,
    error,
    fetchNotifications,
    markAsRead,
    markAllRead,
  }
})
