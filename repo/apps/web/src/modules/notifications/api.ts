import { getAdapter } from '@/services/adapters/adapter-factory'

export function notificationsApi() {
  const adapter = getAdapter()

  return {
    getNotifications(page = 0, size = 25) {
      return adapter.getNotifications({ page, size })
    },

    markRead(id: number) {
      return adapter.markRead(id)
    },

    getDeliveryStatus(notificationId: number) {
      return adapter.getDeliveryStatus(notificationId)
    },
  }
}
