<script setup lang="ts">
import SidebarNav from '@/components/navigation/SidebarNav.vue'
import TopBar from '@/components/navigation/TopBar.vue'
import { useUiStore } from '@/stores/ui.store'

const uiStore = useUiStore()
</script>

<template>
  <div class="default-layout">
    <SidebarNav />
    <div class="default-layout__main" :class="{ 'default-layout__main--expanded': uiStore.sidebarCollapsed }">
      <TopBar />
      <main class="default-layout__content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.default-layout {
  display: flex;
  min-height: 100vh;
  background: #f9fafb;
}

.default-layout__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.default-layout__content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .default-layout__content {
    padding: 16px;
  }
}
</style>
