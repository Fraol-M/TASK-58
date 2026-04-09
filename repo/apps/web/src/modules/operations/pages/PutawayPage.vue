<script setup lang="ts">
import { usePutaway } from '../composables/usePutaway'
import { useOperationsStore } from '../store'
import { useToast } from '@/composables/useToast'
import PutawayItem from '../components/PutawayItem.vue'
import AppButton from '@/components/common/AppButton.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const toast = useToast()
const store = useOperationsStore()
const { pendingTasks, completedTasks, loading, error } = usePutaway()

async function handleConfirm(taskId: number, location: string) {
  const task = pendingTasks.value.find(t => t.id === taskId)
  if (!task) return
  try {
    await store.createPutaway(task.receiptId, taskId, location)
    toast.show(`Task ${taskId} confirmed at ${location}`, 'success')
  } catch {
    toast.show('Putaway failed', 'error')
  }
}

async function bulkComplete() {
  for (const task of pendingTasks.value) {
    try {
      await store.createPutaway(task.receiptId, task.id, task.suggestedLocation)
    } catch {
      toast.show(`Failed to complete task ${task.id}`, 'error')
      return
    }
  }
  toast.show('All tasks completed', 'success')
}
</script>

<template>
  <div class="putaway-page">
    <div class="putaway-page__header">
      <h1 class="page-title">Putaway Queue</h1>
      <AppButton
        v-if="pendingTasks.length > 1"
        variant="secondary"
        @click="bulkComplete"
      >
        Bulk Complete
      </AppButton>
    </div>

    <LoadingState v-if="loading" text="Loading putaway tasks..." />
    <ErrorState v-else-if="error" :message="error.message" />
    <EmptyState
      v-else-if="pendingTasks.length === 0 && completedTasks.length === 0"
      title="No putaway tasks"
      message="There are no putaway tasks in the queue."
    />

    <template v-else>
      <section v-if="pendingTasks.length > 0" class="putaway-page__section">
        <h2 class="putaway-page__section-title">
          Pending ({{ pendingTasks.length }})
        </h2>
        <div class="putaway-page__grid">
          <PutawayItem
            v-for="task in pendingTasks"
            :key="task.id"
            :task="task"
            @confirm="handleConfirm"
          />
        </div>
      </section>

      <section v-if="completedTasks.length > 0" class="putaway-page__section">
        <h2 class="putaway-page__section-title">
          Completed ({{ completedTasks.length }})
        </h2>
        <div class="putaway-page__grid">
          <PutawayItem
            v-for="task in completedTasks"
            :key="task.id"
            :task="task"
            @confirm="handleConfirm"
          />
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.putaway-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.putaway-page__section {
  margin-bottom: 32px;
}

.putaway-page__section-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 16px;
}

.putaway-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
</style>
