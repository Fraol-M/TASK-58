import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ToastMessage, ToastType } from '@/types/ui'

let toastId = 0

export const useUiStore = defineStore('ui', () => {
  const sidebarCollapsed = ref(false)
  const globalLoading = ref(false)
  const toasts = ref<ToastMessage[]>([])

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setGlobalLoading(loading: boolean) {
    globalLoading.value = loading
  }

  function addToast(message: string, type: ToastType = 'info', duration: number = 4000) {
    const id = `toast-${++toastId}`
    const toast: ToastMessage = { id, type, message, duration }
    toasts.value.push(toast)

    if (duration > 0) {
      setTimeout(() => removeToast(id), duration)
    }

    return id
  }

  function removeToast(id: string) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  return {
    sidebarCollapsed,
    globalLoading,
    toasts,
    toggleSidebar,
    setGlobalLoading,
    addToast,
    removeToast,
  }
})
