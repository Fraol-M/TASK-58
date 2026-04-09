import { createPinia, setActivePinia } from 'pinia'
import { mount, type VueWrapper } from '@vue/test-utils'
import { createRouter, createMemoryHistory, type RouteRecordRaw } from 'vue-router'
import type { Component } from 'vue'

const defaultRoutes: RouteRecordRaw[] = [
  { path: '/', component: { template: '<div>Home</div>' } },
  { path: '/sign-in', component: { template: '<div>Sign In</div>' } },
  { path: '/sign-up', component: { template: '<div>Sign Up</div>' } },
  { path: '/dashboard', component: { template: '<div>Dashboard</div>' } },
]

export interface RenderOptions {
  props?: Record<string, unknown>
  slots?: Record<string, string>
  routes?: RouteRecordRaw[]
  initialRoute?: string
}

export async function renderWithProviders(
  component: Component,
  options: RenderOptions = {},
): Promise<VueWrapper> {
  const pinia = createPinia()
  setActivePinia(pinia)

  const router = createRouter({
    history: createMemoryHistory(),
    routes: options.routes ?? defaultRoutes,
  })

  if (options.initialRoute) {
    router.push(options.initialRoute)
    await router.isReady()
  }

  const wrapper = mount(component, {
    props: options.props,
    slots: options.slots,
    global: {
      plugins: [pinia, router],
    },
  })

  return wrapper
}
