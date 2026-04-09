import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useNotificationsStore } from '../store'

export function useNotifications() {
  const store = useNotificationsStore()
  const { notifications, unreadCount, loading, error } = storeToRefs(store)

  onMounted(() => {
    store.fetchNotifications()
  })

  return {
    notifications,
    unreadCount,
    loading,
    error,
    markAsRead: store.markAsRead,
    markAllRead: store.markAllRead,
    refresh: store.fetchNotifications,
  }
}
