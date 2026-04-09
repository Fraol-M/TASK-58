<script setup lang="ts">
import { ref, computed } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import FormCheckbox from '@/components/forms/FormCheckbox.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'

interface Props {
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  confirm: [password: string]
}>()

const understood = ref(false)
const password = ref('')

const canDelete = computed(() => understood.value && password.value.length >= 6)

function handleSubmit() {
  if (!canDelete.value) return
  emit('confirm', password.value)
}
</script>

<template>
  <div class="deletion-dialog">
    <div class="deletion-dialog__warning">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
        <path d="M12 9v4m0 4h.01" />
      </svg>
      <div>
        <h3 class="deletion-dialog__warning-title">Danger Zone</h3>
        <p class="deletion-dialog__warning-text">
          This action is permanent and cannot be undone. All your data including
          fitness assessments, study plans, and account settings will be permanently deleted.
        </p>
      </div>
    </div>

    <form @submit.prevent="handleSubmit">
      <FormCheckbox
        v-model="understood"
        label="I understand this action is permanent and all my data will be deleted"
      />

      <FormField label="Confirm Password" required class="deletion-dialog__password">
        <FormInput
          v-model="password"
          type="password"
          placeholder="Enter your password to confirm"
        />
      </FormField>

      <SubmitButton
        :loading="loading"
        text="Delete My Account"
        loading-text="Deleting..."
        :disabled="!canDelete"
      />
    </form>
  </div>
</template>

<style scoped>
.deletion-dialog__warning {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  margin-bottom: 20px;
}

.deletion-dialog__warning svg {
  flex-shrink: 0;
  color: #dc2626;
  margin-top: 2px;
}

.deletion-dialog__warning-title {
  font-size: 15px;
  font-weight: 600;
  color: #991b1b;
  margin: 0 0 4px;
}

.deletion-dialog__warning-text {
  font-size: 14px;
  color: #7f1d1d;
  margin: 0;
  line-height: 1.5;
}

.deletion-dialog__password {
  margin-top: 16px;
}

:deep(.submit-btn) {
  background-color: #dc2626;
  margin-top: 8px;
}

:deep(.submit-btn:hover:not(:disabled)) {
  background-color: #b91c1c;
}
</style>
