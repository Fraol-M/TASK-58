<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePlans } from '../composables/usePlans'
import { useToast } from '@/composables/useToast'
import PlanCard from '../components/PlanCard.vue'
import PlanForm from '../components/PlanForm.vue'
import StreakIndicator from '../components/StreakIndicator.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'

const router = useRouter()
const toast = useToast()
const { plans, loading, error, createPlan, refresh } = usePlans()

const showModal = ref(false)
const creating = ref(false)

async function handleCreate(data: { title: string; description?: string; courseId?: number; termId?: number }) {
  creating.value = true
  try {
    await createPlan(data)
    showModal.value = false
    toast.show('Study plan created!', 'success')
  } catch {
    toast.show('Failed to create plan', 'error')
  } finally {
    creating.value = false
  }
}

function navigateToPlan(id: number) {
  router.push({ name: 'study-review', query: { planId: String(id) } })
}
</script>

<template>
  <div class="plans-page">
    <div class="plans-page__header">
      <div class="plans-page__title-row">
        <h1 class="page-title">Study Plans</h1>
        <StreakIndicator :current-streak="plans.length" :longest-streak="plans.length" />
      </div>
      <AppButton @click="showModal = true">Create Plan</AppButton>
    </div>

    <LoadingState v-if="loading" text="Loading study plans..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />
    <EmptyState
      v-else-if="plans.length === 0"
      title="No study plans yet"
      message="Create a study plan to start tracking your learning progress."
      action-text="Create Plan"
      @action="showModal = true"
    />

    <div v-else class="plans-page__grid">
      <PlanCard
        v-for="plan in plans"
        :key="plan.id"
        :plan="plan"
        @click="navigateToPlan"
      />
    </div>

    <AppModal :open="showModal" title="Create Study Plan" size="lg" @close="showModal = false">
      <PlanForm :loading="creating" @submit="handleCreate" />
    </AppModal>
  </div>
</template>

<style scoped>
.plans-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}

.plans-page__title-row {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.plans-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
</style>
