import { ref } from 'vue'
import type { ToastMessage, ToastType } from '@/types/ui'

const toasts = ref<ToastMessage[]>([])

let idCounter = 0

export function useToast() {
  function show(message: string, type: ToastType = 'info', duration: number = 4000) {
    const id = `toast-${++idCounter}`
    const toast: ToastMessage = { id, type, message, duration }
    toasts.value.push(toast)

    if (duration > 0) {
      setTimeout(() => dismiss(id), duration)
    }

    return id
  }

  function dismiss(id: string) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  return {
    toasts,
    show,
    dismiss,
  }
}
