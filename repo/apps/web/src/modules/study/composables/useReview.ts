import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useStudyStore } from '../store'

export function useReview(planId?: number) {
  const store = useStudyStore()
  const { forgettingPoints, loading, error } = storeToRefs(store)

  const currentIndex = ref(0)
  const sessionComplete = ref(false)
  const submitting = ref(false)

  // Filter forgetting points that are due for review
  const duePoints = computed(() => {
    const now = new Date().toISOString().substring(0, 10)
    return forgettingPoints.value.filter(fp => fp.nextReviewDate <= now)
  })

  const currentPoint = computed(() => {
    if (currentIndex.value >= duePoints.value.length) return null
    return duePoints.value[currentIndex.value]
  })

  const totalDue = computed(() => duePoints.value.length)

  async function rateQuality(quality: number) {
    if (!currentPoint.value) return
    submitting.value = true
    try {
      await store.addForgettingPoint(currentPoint.value.planId, currentPoint.value.topic, currentPoint.value.description)
      currentIndex.value++
      if (currentIndex.value >= duePoints.value.length) {
        sessionComplete.value = true
      }
    } finally {
      submitting.value = false
    }
  }

  onMounted(() => {
    if (planId) {
      store.fetchForgettingPoints(planId)
    }
  })

  return {
    forgettingPoints,
    duePoints,
    currentPoint,
    currentIndex,
    totalDue,
    sessionComplete,
    loading,
    error,
    submitting,
    rateQuality,
  }
}
