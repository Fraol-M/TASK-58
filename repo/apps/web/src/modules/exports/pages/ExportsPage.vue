<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useExports } from '../composables/useExports'
import { useToast } from '@/composables/useToast'
import ExportForm from '../components/ExportForm.vue'
import ExportHistory from '../components/ExportHistory.vue'
import AccountDeletionDialog from '../components/AccountDeletionDialog.vue'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import FormInput from '@/components/forms/FormInput.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import { getAdapter } from '@/services/adapters/adapter-factory'

const toast = useToast()
const { exportResult, loading, error, requestExport, deleteAccount } = useExports()

const exporting = ref(false)
const deleting = ref(false)
const importing = ref(false)
const exportHistory = ref<any[]>([])
const historyLoading = ref(false)

// Import form state
const importFile = ref<File | null>(null)
const importPassword = ref('')

const adapter = getAdapter()

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await adapter.listExports()
    exportHistory.value = res.data ?? []
  } catch {
    exportHistory.value = []
  } finally {
    historyLoading.value = false
  }
}

onMounted(() => {
  loadHistory()
})

async function handleExport(data: { exportType: string; password: string }) {
  exporting.value = true
  try {
    await requestExport(data.exportType, data.password)
    toast.show('Export requested successfully!', 'success')
    await loadHistory()
  } catch {
    toast.show('Export request failed', 'error')
  } finally {
    exporting.value = false
  }
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  importFile.value = target.files?.[0] ?? null
}

async function handleImport() {
  if (!importFile.value || !importPassword.value) {
    toast.show('Please select a file and enter the export password', 'error')
    return
  }
  importing.value = true
  try {
    const res = await adapter.importAccountFile(importFile.value, importPassword.value)
    toast.show(res.data || 'Import completed successfully!', 'success')
    importFile.value = null
    importPassword.value = ''
  } catch {
    toast.show('Import failed. Check the file and password.', 'error')
  } finally {
    importing.value = false
  }
}

async function handleDelete(password: string) {
  deleting.value = true
  try {
    await deleteAccount(password)
    toast.show('Account deleted', 'success')
  } catch {
    toast.show('Account deletion failed', 'error')
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <div class="exports-page">
    <h1 class="page-title">Data Export & Import</h1>

    <ErrorState v-if="error" :message="error.message" />

    <AppCard title="Request Export" class="exports-page__section">
      <ExportForm :loading="exporting" @submit="handleExport" />
    </AppCard>

    <AppCard title="Import from Export File" class="exports-page__section">
      <p class="imports-desc">Upload a previously exported <code>.enc</code> file and enter its password to restore your data.</p>
      <div class="import-form">
        <div class="import-form__field">
          <label class="import-form__label">Export File</label>
          <input
            type="file"
            accept=".enc"
            class="import-form__file"
            @change="handleFileSelect"
          />
        </div>
        <div class="import-form__field">
          <label class="import-form__label">Export Password</label>
          <FormInput
            v-model="importPassword"
            type="password"
            placeholder="Enter the export password"
          />
        </div>
        <AppButton
          :loading="importing"
          :disabled="!importFile || !importPassword"
          @click="handleImport"
        >
          Import Data
        </AppButton>
      </div>
    </AppCard>

    <AppCard title="Export History" padding="none" class="exports-page__section">
      <ExportHistory :exports="exportHistory" />
    </AppCard>

    <div class="exports-page__danger-zone">
      <h2 class="exports-page__danger-title">Delete Account</h2>
      <AppCard>
        <AccountDeletionDialog :loading="deleting" @confirm="handleDelete" />
      </AppCard>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
}

.exports-page__section {
  margin-bottom: 32px;
}

.imports-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px;
}

.import-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 400px;
}

.import-form__field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.import-form__label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.import-form__file {
  font-size: 14px;
  font-family: inherit;
}

.exports-page__danger-zone {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 2px solid #fecaca;
}

.exports-page__danger-title {
  font-size: 18px;
  font-weight: 600;
  color: #991b1b;
  margin: 0 0 16px;
}
</style>
