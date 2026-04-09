<script setup lang="ts">
import AppModal from '@/components/common/AppModal.vue'
import AppButton from '@/components/common/AppButton.vue'

interface Props {
  open: boolean
  title?: string
  message?: string
  confirmText?: string
  cancelText?: string
  variant?: 'primary' | 'danger'
}

withDefaults(defineProps<Props>(), {
  title: 'Confirm',
  message: 'Are you sure you want to proceed?',
  confirmText: 'Confirm',
  cancelText: 'Cancel',
  variant: 'primary',
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <AppModal :open="open" :title="title" size="sm" @close="emit('cancel')">
    <p class="confirm-dialog__message">{{ message }}</p>
    <template #footer>
      <AppButton variant="secondary" @click="emit('cancel')">
        {{ cancelText }}
      </AppButton>
      <AppButton :variant="variant === 'danger' ? 'danger' : 'primary'" @click="emit('confirm')">
        {{ confirmText }}
      </AppButton>
    </template>
  </AppModal>
</template>

<style scoped>
.confirm-dialog__message {
  font-size: 14px;
  color: #4b5563;
  margin: 0;
  line-height: 1.6;
}
</style>
