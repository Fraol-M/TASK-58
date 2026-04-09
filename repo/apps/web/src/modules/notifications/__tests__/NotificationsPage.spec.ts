import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref, computed } from 'vue'
import NotificationsPage from '../pages/NotificationsPage.vue'

const mockNotifications = ref<any[]>([])
const mockLoading = ref(false)
const mockError = ref<any>(null)
const mockMarkAsRead = vi.fn()
const mockMarkAllRead = vi.fn()
const mockRefresh = vi.fn()

vi.mock('../composables/useNotifications', () => ({
  useNotifications: () => ({
    notifications: mockNotifications,
    unreadCount: computed(() => mockNotifications.value.filter((n: any) => !n.read).length),
    loading: mockLoading,
    error: mockError,
    markAsRead: mockMarkAsRead,
    markAllRead: mockMarkAllRead,
    refresh: mockRefresh,
  }),
}))

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)

  const wrapper = mount(NotificationsPage, {
    global: {
      plugins: [pinia],
      stubs: {
        NotificationItem: {
          name: 'NotificationItem',
          template: '<div class="notification-item" :data-testid="`notif-${notification.id}`" :class="{ \'unread\': !notification.read }" @click="$emit(\'click\', notification.id)">{{ notification.title }}</div>',
          props: ['notification'],
          emits: ['click'],
        },
        NotificationFilters: { template: '<div data-testid="filters">Filters</div>', props: ['typeFilter', 'readFilter'] },
        AppButton: { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['variant', 'size'] },
        AppCard: { template: '<div class="app-card"><slot /></div>', props: ['padding'] },
        LoadingState: { template: '<div data-testid="loading">Loading...</div>', props: ['text'] },
        ErrorState: { template: '<div data-testid="error">Error</div>', props: ['message'] },
        EmptyState: { template: '<div data-testid="empty">No notifications</div>', props: ['title', 'message'] },
      },
    },
  })
  return { wrapper }
}

describe('NotificationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockNotifications.value = []
    mockLoading.value = false
    mockError.value = null
  })

  it('renders notification list', async () => {
    mockNotifications.value = [
      { id: 1, title: 'Goal completed', message: 'You hit your target!', read: false, type: 'fitness', createdAt: '2026-04-07' },
      { id: 2, title: 'New receipt', message: 'Receipt REC-001 created', read: true, type: 'operations', createdAt: '2026-04-06' },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    const items = wrapper.findAll('.notification-item')
    expect(items.length).toBe(2)
  })

  it('shows unread indicator', async () => {
    mockNotifications.value = [
      { id: 1, title: 'Unread', message: 'msg', read: false, type: 'info', createdAt: '2026-04-07' },
      { id: 2, title: 'Read', message: 'msg', read: true, type: 'info', createdAt: '2026-04-06' },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    // The badge shows unread count
    expect(wrapper.find('.page-title__badge').exists()).toBe(true)
    expect(wrapper.find('.page-title__badge').text()).toBe('1')
  })

  it('marks notification as read', async () => {
    mockNotifications.value = [
      { id: 1, title: 'New update', message: 'msg', read: false, type: 'info', createdAt: '2026-04-07' },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    const item = wrapper.findComponent({ name: 'NotificationItem' })
    item.vm.$emit('click', 1)
    await flushPromises()

    expect(mockMarkAsRead).toHaveBeenCalledWith(1)
  })

  it('mark all read works', async () => {
    mockNotifications.value = [
      { id: 1, title: 'N1', message: 'msg', read: false, type: 'info', createdAt: '2026-04-07' },
      { id: 2, title: 'N2', message: 'msg', read: false, type: 'info', createdAt: '2026-04-06' },
    ]

    const { wrapper } = mountPage()
    await flushPromises()

    const markAllBtn = wrapper.findAll('button').find(b => b.text().includes('Mark All Read'))
    expect(markAllBtn).toBeDefined()
    await markAllBtn!.trigger('click')
    await flushPromises()

    expect(mockMarkAllRead).toHaveBeenCalled()
  })

  it('shows empty state', async () => {
    mockNotifications.value = []

    const { wrapper } = mountPage()
    await flushPromises()

    expect(wrapper.find('[data-testid="empty"]').exists()).toBe(true)
  })
})
