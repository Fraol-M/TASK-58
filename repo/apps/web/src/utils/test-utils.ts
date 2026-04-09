import { createPinia, setActivePinia } from 'pinia'
import { mount, type ComponentMountingOptions, type VueWrapper } from '@vue/test-utils'
import { createRouter, createMemoryHistory, type RouteRecordRaw } from 'vue-router'
import type { Component } from 'vue'

const defaultRoutes: RouteRecordRaw[] = [
  { path: '/', component: { template: '<div>Home</div>' } },
  { path: '/sign-in', component: { template: '<div>Sign In</div>' } },
  { path: '/dashboard', component: { template: '<div>Dashboard</div>' } },
]

export interface RenderOptions {
  props?: Record<string, unknown>
  slots?: Record<string, unknown>
  routes?: RouteRecordRaw[]
  initialRoute?: string
  global?: ComponentMountingOptions<unknown>['global']
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
    slots: options.slots as Record<string, string>,
    global: {
      plugins: [pinia, router],
      ...options.global,
    },
  })

  return wrapper
}
