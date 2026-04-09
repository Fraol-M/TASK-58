<script setup lang="ts">
import { useToast } from '@/composables/useToast'
import AppCard from '@/components/common/AppCard.vue'
import AssessmentForm from '../components/AssessmentForm.vue'

const toast = useToast()

function onSaved() {
  toast.show('Assessment saved successfully!', 'success')
}
</script>

<template>
  <div class="assessment-page">
    <h1 class="page-title">Fitness Assessment</h1>
    <p class="page-subtitle">
      Complete your fitness assessment to set a baseline for your goals.
    </p>
    <AppCard>
      <AssessmentForm @saved="onSaved" />
    </AppCard>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 4px;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 24px;
}
</style>
