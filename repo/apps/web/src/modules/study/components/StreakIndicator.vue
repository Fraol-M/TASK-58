<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  currentStreak: number
  longestStreak: number
}

const props = defineProps<Props>()

const flameColor = computed(() => {
  if (props.currentStreak >= 30) return '#dc2626'
  if (props.currentStreak >= 14) return '#ea580c'
  if (props.currentStreak >= 7) return '#d97706'
  if (props.currentStreak >= 3) return '#f59e0b'
  return '#9ca3af'
})
</script>

<template>
  <div class="streak-indicator">
    <div class="streak-indicator__current">
      <svg class="streak-indicator__flame" :style="{ color: flameColor }" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 23c-4.97 0-9-3.58-9-8 0-4 2.5-7 5-9 .5 4 3 5 5 4-1 3 0 6 2 7 0-3 2-5 2-8 2 2 4 5 4 8 0 4.42-4.03 8-9 8z" />
      </svg>
      <span class="streak-indicator__count">{{ currentStreak }}</span>
    </div>
    <div class="streak-indicator__label">day streak</div>
    <div class="streak-indicator__longest">
      Best: {{ longestStreak }} days
    </div>
  </div>
</template>

<style scoped>
.streak-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.streak-indicator__current {
  display: flex;
  align-items: center;
  gap: 4px;
}

.streak-indicator__flame {
  flex-shrink: 0;
}

.streak-indicator__count {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.streak-indicator__label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.streak-indicator__longest {
  font-size: 11px;
  color: #9ca3af;
}
</style>
