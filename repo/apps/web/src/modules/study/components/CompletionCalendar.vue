<script setup lang="ts">
interface DayData {
  date: string
  completed: boolean
  isToday: boolean
}

interface Props {
  days: DayData[]
}

defineProps<Props>()

function dayLabel(dateStr: string): string {
  const d = new Date(dateStr)
  return String(d.getDate())
}
</script>

<template>
  <div class="completion-calendar">
    <div class="completion-calendar__grid">
      <div
        v-for="day in days"
        :key="day.date"
        :class="[
          'cal-day',
          {
            'cal-day--completed': day.completed,
            'cal-day--today': day.isToday,
          }
        ]"
        :title="day.date"
      >
        <span class="cal-day__number">{{ dayLabel(day.date) }}</span>
      </div>
    </div>
    <div class="completion-calendar__legend">
      <span class="legend-item">
        <span class="legend-dot legend-dot--completed"></span>
        Completed
      </span>
      <span class="legend-item">
        <span class="legend-dot legend-dot--missed"></span>
        Missed
      </span>
      <span class="legend-item">
        <span class="legend-dot legend-dot--today"></span>
        Today
      </span>
    </div>
  </div>
</template>

<style scoped>
.completion-calendar__grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.cal-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: #f3f4f6;
  position: relative;
}

.cal-day--completed {
  background: #d1fae5;
}

.cal-day--today {
  outline: 2px solid #4f46e5;
  outline-offset: -2px;
}

.cal-day__number {
  font-size: 12px;
  font-weight: 500;
  color: #374151;
}

.cal-day--completed .cal-day__number {
  color: #065f46;
}

.completion-calendar__legend {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 2px;
}

.legend-dot--completed {
  background: #d1fae5;
}

.legend-dot--missed {
  background: #f3f4f6;
}

.legend-dot--today {
  background: #fff;
  outline: 2px solid #4f46e5;
}
</style>
