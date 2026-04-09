<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MockModeBanner from '@/components/feedback/MockModeBanner.vue'
import ToastContainer from '@/components/feedback/ToastContainer.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const route = useRoute()

const layout = computed(() => {
  return route.meta.layout === 'auth' ? 'auth' : 'default'
})
</script>

<template>
  <MockModeBanner />
  <ToastContainer />
  <component :is="layout === 'auth' ? AuthLayout : DefaultLayout">
    <RouterView />
  </component>
</template>
