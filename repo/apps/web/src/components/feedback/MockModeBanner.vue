<script setup lang="ts">
import { ref } from 'vue'
import { useMockModeStore } from '@/stores/mock-mode.store'

const mockStore = useMockModeStore()
const dismissed = ref(false)
</script>

<template>
  <div
    v-if="mockStore.isMockMode && !dismissed"
    class="mock-banner"
  >
    <span class="mock-banner__text">
      Running in mock mode &mdash; data is local only, no backend connected
    </span>
    <button class="mock-banner__dismiss" @click="dismissed = true" aria-label="Dismiss">
      &times;
    </button>
  </div>
</template>

<style scoped>
.mock-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 16px;
  background-color: #fbbf24;
  color: #78350f;
  font-size: 13px;
  font-weight: 500;
}

.mock-banner__text {
  text-align: center;
}

.mock-banner__dismiss {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 18px;
  color: #78350f;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}
.mock-banner__dismiss:hover {
  color: #451a03;
}
</style>
