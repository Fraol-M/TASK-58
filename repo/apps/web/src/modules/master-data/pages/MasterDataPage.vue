<script setup lang="ts">
import { ref } from 'vue'
import { useMasterData } from '../composables/useMasterData'
import { useToast } from '@/composables/useToast'
import { getAdapter } from '@/services/adapters/adapter-factory'
import EntityTable from '../components/EntityTable.vue'
import EntityForm from '../components/EntityForm.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import FormInput from '@/components/forms/FormInput.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'

const toast = useToast()
const adapter = getAdapter()
const { items, loading, error, entityType, searchQuery, setEntityType, refresh } = useMasterData()

const showModal = ref(false)
const saving = ref(false)

const tabs = [
  { key: 'term', label: 'Terms' },
  { key: 'school', label: 'Schools' },
  { key: 'major', label: 'Majors' },
  { key: 'class', label: 'Classes' },
  { key: 'course', label: 'Courses' },
]

const editItem = ref<any>(null)

async function handleEdit(item: any) {
  editItem.value = item
  showModal.value = true
}

async function handleDelete(item: any) {
  if (!confirm(`Are you sure you want to delete "${item.name}"?`)) return
  try {
    await adapter.deleteItem(entityType.value, item.id)
    toast.show(`${entityType.value} deleted`, 'success')
    refresh()
  } catch {
    toast.show('Failed to delete item', 'error')
  }
}

async function handleCreate(data: Record<string, any>) {
  saving.value = true
  try {
    if (editItem.value) {
      await adapter.updateItem(entityType.value, editItem.value.id, data)
      toast.show(`${entityType.value} updated successfully`, 'success')
    } else {
      await adapter.createItem(entityType.value, data)
      toast.show(`${entityType.value} created successfully`, 'success')
    }
    showModal.value = false
    editItem.value = null
    refresh()
  } catch {
    toast.show('Failed to save item', 'error')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="master-data-page">
    <div class="master-data-page__header">
      <h1 class="page-title">Master Data</h1>
      <AppButton @click="editItem = null; showModal = true">Add {{ entityType }}</AppButton>
    </div>

    <!-- Tabs -->
    <div class="master-data-page__tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { 'tab-btn--active': entityType === tab.key }]"
        @click="setEntityType(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Search -->
    <div class="master-data-page__search">
      <FormInput
        v-model="searchQuery"
        placeholder="Search by code or name..."
      />
    </div>

    <LoadingState v-if="loading" text="Loading data..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />
    <EntityTable
      v-else
      :items="items"
      :entity-type="entityType"
      @edit="handleEdit"
      @delete="handleDelete"
    />

    <AppModal :open="showModal" :title="editItem ? `Edit ${entityType}` : `Add ${entityType}`" @close="showModal = false; editItem = null">
      <EntityForm
        :key="editItem?.id ?? 'new'"
        :entity-type="entityType"
        :initial-data="editItem ?? undefined"
        :loading="saving"
        @submit="handleCreate"
      />
    </AppModal>
  </div>
</template>

<style scoped>
.master-data-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.master-data-page__tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.tab-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  font-family: inherit;
  color: #6b7280;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}

.tab-btn:hover {
  color: #374151;
}

.tab-btn--active {
  color: #4f46e5;
  border-bottom-color: #4f46e5;
}

.master-data-page__search {
  margin-bottom: 16px;
  max-width: 320px;
}
</style>
