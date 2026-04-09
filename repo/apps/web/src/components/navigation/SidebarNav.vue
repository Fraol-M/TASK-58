<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/modules/auth/store'
import { useUiStore } from '@/stores/ui.store'
import { isAdmin, isOpsStaff, isRegularUser } from '@/utils/role-checks'
import SidebarNavItem from './SidebarNavItem.vue'
import type { SidebarNavItem as NavItem } from '@/types/ui'

const authStore = useAuthStore()
const uiStore = useUiStore()

const navItems = computed<NavItem[]>(() => {
  const user = authStore.user
  const items: NavItem[] = []

  items.push({ label: 'Dashboard', icon: '\u2302', to: '/dashboard' })

  if (isRegularUser(user)) {
    items.push(
      { label: 'Assessment', icon: '\u2661', to: '/fitness/assessment' },
      { label: 'Goals', icon: '\u2691', to: '/fitness/goals' },
      { label: 'Check-ins', icon: '\u2713', to: '/fitness/check-ins' },
      { label: 'Study Plans', icon: '\u2710', to: '/study/plans' },
      { label: 'Study Review', icon: '\u2606', to: '/study/review' },
      { label: 'Study History', icon: '\u29D6', to: '/study/history' },
    )
  }

  if (isOpsStaff(user)) {
    items.push(
      { label: 'Receiving', icon: '\u2709', to: '/operations/receiving' },
      { label: 'Discrepancies', icon: '\u26A0', to: '/operations/discrepancies' },
      { label: 'Putaway', icon: '\u2750', to: '/operations/putaway' },
    )
  }

  if (isAdmin(user)) {
    items.push(
      { label: 'Master Data', icon: '\u2630', to: '/admin/master-data' },
      { label: 'Performance', icon: '\u2197', to: '/admin/performance' },
    )
  }

  items.push(
    { label: 'Notifications', icon: '\u266A', to: '/notifications' },
    { label: 'Exports', icon: '\u21E3', to: '/exports' },
    { label: 'Profile', icon: '\u263A', to: '/profile' },
  )

  return items
})
</script>

<template>
  <nav :class="['sidebar', { 'sidebar--collapsed': uiStore.sidebarCollapsed }]">
    <div class="sidebar__header">
      <h1 class="sidebar__brand">CampusFit</h1>
      <button class="sidebar__toggle" @click="uiStore.toggleSidebar" aria-label="Toggle sidebar">
        {{ uiStore.sidebarCollapsed ? '\u2192' : '\u2190' }}
      </button>
    </div>
    <div class="sidebar__nav">
      <SidebarNavItem
        v-for="item in navItems"
        :key="item.to"
        :label="item.label"
        :to="item.to"
        :icon="item.icon"
        :badge="item.badge"
      />
    </div>
  </nav>
</template>

<style scoped>
.sidebar {
  width: 240px;
  min-height: 100vh;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  flex-shrink: 0;
}

.sidebar--collapsed {
  width: 60px;
  overflow: hidden;
}

.sidebar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 14px;
  border-bottom: 1px solid #e5e7eb;
}

.sidebar__brand {
  font-size: 18px;
  font-weight: 700;
  color: #4f46e5;
  margin: 0;
  white-space: nowrap;
}

.sidebar--collapsed .sidebar__brand {
  display: none;
}

.sidebar__toggle {
  background: none;
  border: none;
  font-size: 16px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}
.sidebar__toggle:hover {
  background: #f3f4f6;
}

.sidebar__nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.sidebar--collapsed .sidebar__nav :deep(.sidebar-item__label),
.sidebar--collapsed .sidebar__nav :deep(.sidebar-item__badge) {
  display: none;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 500;
    left: 0;
    top: 0;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
  }
  .sidebar--collapsed {
    width: 0;
    border: none;
  }
}
</style>
