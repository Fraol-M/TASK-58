<script setup lang="ts">
import type { ChangeHistoryEntry } from '@/services/adapters/api-adapter.interface'
import Timeline from '@/components/data-display/Timeline.vue'
import TimelineItem from '@/components/data-display/TimelineItem.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import { formatDate } from '@/utils/format-date'

interface Props {
  entries: ChangeHistoryEntry[]
}

defineProps<Props>()
</script>

<template>
  <div class="audit-timeline">
    <Timeline v-if="entries.length > 0">
      <TimelineItem
        v-for="(entry, i) in entries"
        :key="i"
        :title="entry.action ?? entry.fieldName"
        :date="formatDate(entry.changedAt, 'MMM DD, YYYY HH:mm')"
      >
        <div class="audit-entry">
          <AppBadge :label="entry.entityType" variant="info" />
          <span class="audit-entry__by">by {{ entry.changedBy }}</span>
        </div>
        <div class="audit-entry__changes">
          <div v-if="entry.changes" v-for="(change, field) in entry.changes" :key="field" class="change-item">
            <span class="change-item__field">{{ field }}:</span>
            <span class="change-item__old">{{ change.oldValue }}</span>
            <span class="change-item__arrow">&rarr;</span>
            <span class="change-item__new">{{ change.newValue }}</span>
          </div>
          <div v-else class="change-item">
            <span class="change-item__field">{{ entry.fieldName }}:</span>
            <span class="change-item__old">{{ entry.oldValue }}</span>
            <span class="change-item__arrow">&rarr;</span>
            <span class="change-item__new">{{ entry.newValue }}</span>
          </div>
        </div>
      </TimelineItem>
    </Timeline>
    <div v-else class="audit-timeline__empty">
      No change history available.
    </div>
  </div>
</template>

<style scoped>
.audit-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.audit-entry__by {
  font-size: 12px;
  color: #9ca3af;
}

.audit-entry__changes {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.change-item {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.change-item__field {
  color: #6b7280;
  font-weight: 500;
}

.change-item__old {
  color: #dc2626;
  text-decoration: line-through;
}

.change-item__arrow {
  color: #9ca3af;
}

.change-item__new {
  color: #059669;
  font-weight: 500;
}

.audit-timeline__empty {
  text-align: center;
  color: #9ca3af;
  padding: 24px 0;
  font-size: 14px;
}
</style>
