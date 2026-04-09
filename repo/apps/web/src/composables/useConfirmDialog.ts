import { ref } from 'vue'
import type { ConfirmDialogOptions } from '@/types/ui'

const isOpen = ref(false)
const options = ref<ConfirmDialogOptions>({
  title: '',
  message: '',
  confirmText: 'Confirm',
  cancelText: 'Cancel',
})

let resolvePromise: ((value: boolean) => void) | null = null

export function useConfirmDialog() {
  function open(opts: ConfirmDialogOptions): Promise<boolean> {
    options.value = {
      confirmText: 'Confirm',
      cancelText: 'Cancel',
      ...opts,
    }
    isOpen.value = true
    return new Promise<boolean>((resolve) => {
      resolvePromise = resolve
    })
  }

  function confirm() {
    isOpen.value = false
    resolvePromise?.(true)
    resolvePromise = null
  }

  function close() {
    isOpen.value = false
    resolvePromise?.(false)
    resolvePromise = null
  }

  return {
    isOpen,
    options,
    open,
    confirm,
    close,
  }
}
