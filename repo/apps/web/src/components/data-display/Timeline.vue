<script setup lang="ts">
</script>

<template>
  <div class="timeline">
    <slot />
  </div>
</template>

<style scoped>
.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: #e5e7eb;
}
</style>
