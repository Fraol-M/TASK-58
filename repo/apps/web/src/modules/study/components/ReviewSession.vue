<script setup lang="ts">
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'

interface Props {
  topic: string
  description?: string
  currentIndex: number
  totalCount: number
  submitting?: boolean
}

withDefaults(defineProps<Props>(), {
  submitting: false,
})

const emit = defineEmits<{
  rate: [quality: number]
}>()

const qualityLabels = [
  'Complete blackout',
  'Incorrect, but remembered on seeing',
  'Incorrect, but easy to recall',
  'Correct with serious difficulty',
  'Correct with some hesitation',
  'Perfect recall',
]
</script>

<template>
  <div class="review-session">
    <div class="review-session__progress">
      <span class="review-session__counter">
        {{ currentIndex + 1 }} of {{ totalCount }}
      </span>
      <div class="review-session__bar">
        <div
          class="review-session__bar-fill"
          :style="{ width: `${((currentIndex) / totalCount) * 100}%` }"
        ></div>
      </div>
    </div>

    <AppCard class="review-session__card">
      <div class="review-session__topic">
        <h3 class="review-session__topic-title">{{ topic }}</h3>
        <p v-if="description" class="review-session__topic-desc">{{ description }}</p>
      </div>

      <div class="review-session__divider"></div>

      <div class="review-session__rating">
        <p class="review-session__rating-label">How well did you recall this?</p>
        <div class="review-session__buttons">
          <button
            v-for="q in 6"
            :key="q - 1"
            :class="['quality-btn', `quality-btn--${q - 1}`]"
            :disabled="submitting"
            @click="emit('rate', q - 1)"
          >
            <span class="quality-btn__number">{{ q - 1 }}</span>
            <span class="quality-btn__label">{{ qualityLabels[q - 1] }}</span>
          </button>
        </div>
      </div>
    </AppCard>
  </div>
</template>

<style scoped>
.review-session__progress {
  margin-bottom: 20px;
}

.review-session__counter {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
  display: block;
}

.review-session__bar {
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.review-session__bar-fill {
  height: 100%;
  background: #4f46e5;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.review-session__topic {
  text-align: center;
  padding: 24px 0;
}

.review-session__topic-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px;
}

.review-session__topic-desc {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.review-session__divider {
  height: 1px;
  background: #e5e7eb;
  margin: 0 -20px;
}

.review-session__rating {
  padding-top: 20px;
}

.review-session__rating-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin: 0 0 12px;
  text-align: center;
}

.review-session__buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

@media (max-width: 480px) {
  .review-session__buttons {
    grid-template-columns: repeat(2, 1fr);
  }
}

.quality-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}

.quality-btn:hover:not(:disabled) {
  border-color: #4f46e5;
  background: #eef2ff;
}

.quality-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quality-btn__number {
  font-size: 18px;
  font-weight: 700;
}

.quality-btn--0 .quality-btn__number { color: #dc2626; }
.quality-btn--1 .quality-btn__number { color: #ea580c; }
.quality-btn--2 .quality-btn__number { color: #d97706; }
.quality-btn--3 .quality-btn__number { color: #ca8a04; }
.quality-btn--4 .quality-btn__number { color: #65a30d; }
.quality-btn--5 .quality-btn__number { color: #059669; }

.quality-btn__label {
  font-size: 11px;
  color: #6b7280;
  text-align: center;
  line-height: 1.3;
}
</style>
