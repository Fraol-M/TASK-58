<script setup lang="ts">
import type { FitnessCheckIn } from '@/services/adapters/api-adapter.interface'
import Timeline from '@/components/data-display/Timeline.vue'
import TimelineItem from '@/components/data-display/TimelineItem.vue'
import { formatDate } from '@/utils/format-date'

interface Props {
  checkIns: FitnessCheckIn[]
}

defineProps<Props>()
</script>

<template>
  <div class="checkin-timeline">
    <Timeline v-if="checkIns.length > 0">
      <TimelineItem
        v-for="ci in checkIns"
        :key="ci.id"
        :title="`Value: ${ci.value}`"
        :date="formatDate(ci.createdAt, 'MMM DD, YYYY')"
        variant="success"
      >
        <p v-if="ci.notes" class="checkin-timeline__notes">{{ ci.notes }}</p>
      </TimelineItem>
    </Timeline>
    <div v-else class="checkin-timeline__empty">
      No check-ins recorded yet.
    </div>
  </div>
</template>

<style scoped>
.checkin-timeline__notes {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.checkin-timeline__empty {
  text-align: center;
  color: #9ca3af;
  padding: 24px 0;
  font-size: 14px;
}
</style>
