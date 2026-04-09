<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useGoals } from '../composables/useGoals'
import { useToast } from '@/composables/useToast'
import GoalCard from '../components/GoalCard.vue'
import GoalForm from '../components/GoalForm.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const router = useRouter()
const toast = useToast()
const { goals, loading, error, createGoal, refresh } = useGoals()

const showModal = ref(false)
const creating = ref(false)

async function handleCreateGoal(data: { goalType: string; description?: string; targetValue: number; unit: string; startDate: string; targetDate: string }) {
  creating.value = true
  try {
    await createGoal(data)
    showModal.value = false
    toast.show('Goal created successfully!', 'success')
  } catch {
    toast.show('Failed to create goal', 'error')
  } finally {
    creating.value = false
  }
}

function navigateToGoal(id: number) {
  router.push({ name: 'fitness-checkins', params: { goalId: String(id) } })
}
</script>

<template>
  <div class="goals-page">
    <div class="goals-page__header">
      <h1 class="page-title">Fitness Goals</h1>
      <AppButton @click="showModal = true">Create Goal</AppButton>
    </div>

    <LoadingState v-if="loading" text="Loading goals..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />
    <EmptyState
      v-else-if="goals.length === 0"
      title="No fitness goals yet"
      message="Create your first fitness goal to start tracking your progress."
      action-text="Create Goal"
      @action="showModal = true"
    />

    <div v-else class="goals-page__grid">
      <GoalCard
        v-for="goal in goals"
        :key="goal.id"
        :goal="goal"
        @click="navigateToGoal"
      />
    </div>

    <AppModal :open="showModal" title="Create New Goal" @close="showModal = false">
      <GoalForm :loading="creating" @submit="handleCreateGoal" />
    </AppModal>
  </div>
</template>

<style scoped>
.goals-page__header {
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

.goals-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
</style>
