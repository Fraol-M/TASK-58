import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import SignInPage from '../pages/SignInPage.vue'
import SignInForm from '../components/SignInForm.vue'
import { useAuthStore } from '../store'

vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => mockAdapter,
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    toasts: { value: [] },
    show: vi.fn(),
    dismiss: vi.fn(),
  }),
}))

const mockAdapter = {
  signIn: vi.fn(),
  signUp: vi.fn(),
  signOut: vi.fn(),
  getMe: vi.fn(),
}

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>Home</div>' } },
      { path: '/sign-in', name: 'sign-in', component: SignInPage },
      { path: '/sign-up', name: 'sign-up', component: { template: '<div>Sign Up</div>' } },
      { path: '/dashboard', name: 'dashboard', component: { template: '<div>Dashboard</div>' } },
    ],
  })
}

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createTestRouter()

  const wrapper = mount(SignInPage, {
    global: {
      plugins: [pinia, router],
    },
  })
  return { wrapper, router, pinia }
}

describe('SignInPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('renders sign-in form with username and password fields', () => {
    const { wrapper } = mountPage()
    expect(wrapper.find('h2').text()).toContain('Sign In')
    const form = wrapper.findComponent(SignInForm)
    expect(form.exists()).toBe(true)
    expect(wrapper.find('input[placeholder="Enter your username"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  it('shows validation errors when submitting empty form', async () => {
    const { wrapper } = mountPage()
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(wrapper.text()).toContain('required')
  })

  it('calls signIn with correct payload on valid submit', async () => {
    mockAdapter.signIn.mockResolvedValue({
      success: true,
      data: {
        user: { id: 1, username: 'testuser', email: 'test@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' },
        token: 'abc123', expiresAt: '2026-12-31',
      },
    })

    const { wrapper } = mountPage()

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('testuser')
    await inputs[1].setValue('password123')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockAdapter.signIn).toHaveBeenCalledWith({
      username: 'testuser',
      password: 'password123',
    })
  })

  it('displays error message on failed sign-in', async () => {
    mockAdapter.signIn.mockRejectedValue({
      isAxiosError: true,
      response: {
        status: 401,
        data: { message: 'Invalid username or password' },
      },
    })

    const { wrapper } = mountPage()

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('testuser')
    await inputs[1].setValue('wrongpassword')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Invalid username or password')
  })

  it('displays lockout message when account is locked', async () => {
    mockAdapter.signIn.mockRejectedValue({
      isAxiosError: true,
      response: {
        status: 423,
        data: { message: 'Account is locked' },
      },
    })

    const { wrapper } = mountPage()

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('lockeduser')
    await inputs[1].setValue('password123')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Account is locked')
  })

  it('navigates to dashboard on successful sign-in', async () => {
    mockAdapter.signIn.mockResolvedValue({
      success: true,
      data: {
        user: { id: 1, username: 'testuser', email: 'test@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' },
        token: 'abc123', expiresAt: '2026-12-31',
      },
    })

    const { wrapper, router } = mountPage()
    const pushSpy = vi.spyOn(router, 'push')

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('testuser')
    await inputs[1].setValue('password123')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(pushSpy).toHaveBeenCalledWith('/dashboard')
  })

  it('has link to sign-up page', () => {
    const { wrapper } = mountPage()
    const link = wrapper.find('a[href="/sign-up"]')
    expect(link.exists()).toBe(true)
    expect(wrapper.text()).toContain('Create one')
  })

  it('disables submit button while loading', async () => {
    let resolveSignIn: (v: unknown) => void
    mockAdapter.signIn.mockReturnValue(
      new Promise((resolve) => {
        resolveSignIn = resolve
      }),
    )

    const { wrapper } = mountPage()

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('testuser')
    await inputs[1].setValue('password123')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    const button = wrapper.find('button[type="submit"]')
    expect(button.attributes('disabled')).toBeDefined()

    resolveSignIn!({
      success: true,
      data: {
        user: { id: 1, username: 'testuser', email: 'test@campus.edu', roles: ['REGULAR_USER'], status: 'ACTIVE' },
        token: 'abc', expiresAt: '2026-12-31',
      },
    })
    await flushPromises()
  })
})
