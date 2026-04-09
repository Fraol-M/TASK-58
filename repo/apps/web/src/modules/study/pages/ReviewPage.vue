<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useReview } from '../composables/useReview'
import ReviewSession from '../components/ReviewSession.vue'
import AppCard from '@/components/common/AppCard.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'

const route = useRoute()
const planId = route.query.planId ? Number(route.query.planId) : undefined

const {
  duePoints,
  currentPoint,
  currentIndex,
  totalDue,
  sessionComplete,
  loading,
  error,
  submitting,
  rateQuality,
} = useReview(planId)

const hasDuePoints = computed(() => duePoints.value.length > 0)
</script>

<template>
  <div class="review-page">
    <h1 class="page-title">Review Session</h1>

    <LoadingState v-if="loading" text="Loading review points..." />
    <ErrorState v-else-if="error" :message="error.message" />

    <template v-else>
      <!-- Session complete -->
      <AppCard v-if="sessionComplete || !hasDuePoints" class="review-page__complete">
        <div class="complete-message">
          <svg class="complete-message__icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#059669" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <path d="M8 12l2.5 2.5L16 9" />
          </svg>
          <h2 class="complete-message__title">
            {{ sessionComplete ? 'Session Complete!' : 'All caught up!' }}
          </h2>
          <p class="complete-message__text">
            {{ sessionComplete
              ? `You reviewed ${totalDue} point${totalDue === 1 ? '' : 's'}. Great work!`
              : 'No items due for review right now. Check back later.'
            }}
          </p>
        </div>
      </AppCard>

      <!-- Active review -->
      <ReviewSession
        v-else-if="currentPoint"
        :topic="currentPoint.topic"
        :description="currentPoint.description ?? ''"
        :current-index="currentIndex"
        :total-count="totalDue"
        :submitting="submitting"
        @rate="rateQuality"
      />
    </template>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
}

.review-page__complete {
  max-width: 480px;
  margin: 0 auto;
}

.complete-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 32px 0;
}

.complete-message__icon {
  margin-bottom: 16px;
}

.complete-message__title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px;
}

.complete-message__text {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}
</style>
