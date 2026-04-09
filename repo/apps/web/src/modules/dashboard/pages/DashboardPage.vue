<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/modules/auth/store'
import { useDashboardData } from '../composables/useDashboardData'
import UserDashboard from '../components/UserDashboard.vue'
import OpsDashboard from '../components/OpsDashboard.vue'
import AdminDashboard from '../components/AdminDashboard.vue'
import LoadingState from '@/components/feedback/LoadingState.vue'
import ErrorState from '@/components/feedback/ErrorState.vue'

const authStore = useAuthStore()
const { stats, loading, error, refresh } = useDashboardData()

const userRoles = computed(() => authStore.user?.roles ?? [])

const isAdmin = computed(() => userRoles.value.includes('ADMIN'))
const isOps = computed(() => userRoles.value.includes('OPERATIONS_STAFF'))
</script>

<template>
  <div class="dashboard-page">
    <h1 class="dashboard-page__greeting">
      Welcome back, {{ authStore.user?.username }}
    </h1>

    <LoadingState v-if="loading" text="Loading dashboard..." />
    <ErrorState v-else-if="error" :message="error.message" @retry="refresh" />

    <template v-else-if="stats">
      <AdminDashboard v-if="isAdmin" :data="stats" />
      <OpsDashboard v-else-if="isOps" :data="stats" />
      <UserDashboard v-else :data="stats" />
    </template>
  </div>
</template>

<style scoped>
.dashboard-page__greeting {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
}
</style>
