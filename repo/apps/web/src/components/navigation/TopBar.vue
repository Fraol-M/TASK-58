<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/modules/auth/store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const showUserMenu = ref(false)

const pageTitle = computed(() => (route.meta.title as string) || 'CampusFit')

const userInitial = computed(() => {
  const name = authStore.user?.username || 'U'
  return name.charAt(0).toUpperCase()
})

async function handleSignOut() {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/sign-in')
}

function toggleMenu() {
  showUserMenu.value = !showUserMenu.value
}

function closeMenu() {
  showUserMenu.value = false
}
</script>

<template>
  <header class="topbar">
    <div class="topbar__left">
      <h2 class="topbar__title">{{ pageTitle }}</h2>
    </div>
    <div class="topbar__right">
      <router-link to="/notifications" class="topbar__notif">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 01-3.46 0" />
        </svg>
      </router-link>

      <div class="topbar__user" @click="toggleMenu" v-click-outside="closeMenu">
        <div class="topbar__avatar">{{ userInitial }}</div>
        <span class="topbar__username">{{ authStore.user?.username }}</span>

        <div v-if="showUserMenu" class="topbar__dropdown">
          <router-link to="/profile" class="topbar__dropdown-item" @click="closeMenu">
            Profile
          </router-link>
          <button class="topbar__dropdown-item topbar__dropdown-item--btn" @click="handleSignOut">
            Sign Out
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script lang="ts">
// Global click-outside directive
export default {
  directives: {
    clickOutside: {
      mounted(el: HTMLElement, binding: { value: () => void }) {
        el._clickOutsideHandler = (e: MouseEvent) => {
          if (!el.contains(e.target as Node)) {
            binding.value()
          }
        }
        document.addEventListener('click', el._clickOutsideHandler)
      },
      unmounted(el: HTMLElement) {
        if (el._clickOutsideHandler) {
          document.removeEventListener('click', el._clickOutsideHandler)
        }
      },
    },
  },
}

declare global {
  interface HTMLElement {
    _clickOutsideHandler?: (e: MouseEvent) => void
  }
}
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.topbar__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.topbar__right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar__notif {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  color: #6b7280;
  text-decoration: none;
  transition: background-color 0.15s;
}
.topbar__notif:hover {
  background: #f3f4f6;
  color: #111827;
}

.topbar__user {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.15s;
}
.topbar__user:hover {
  background: #f3f4f6;
}

.topbar__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #4f46e5;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.topbar__username {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.topbar__dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  min-width: 160px;
  z-index: 100;
  overflow: hidden;
}

.topbar__dropdown-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  font-size: 14px;
  color: #374151;
  text-decoration: none;
  text-align: left;
  transition: background-color 0.1s;
}
.topbar__dropdown-item:hover {
  background: #f3f4f6;
}

.topbar__dropdown-item--btn {
  border: none;
  background: none;
  cursor: pointer;
  font-family: inherit;
  border-top: 1px solid #e5e7eb;
  color: #dc2626;
}

@media (max-width: 768px) {
  .topbar__username {
    display: none;
  }
}
</style>
