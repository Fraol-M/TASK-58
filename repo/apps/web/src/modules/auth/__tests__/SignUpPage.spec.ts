import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import SignUpPage from '../pages/SignUpPage.vue'
import SignUpForm from '../components/SignUpForm.vue'
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
      { path: '/sign-in', name: 'sign-in', component: { template: '<div>Sign In</div>' } },
      { path: '/sign-up', name: 'sign-up', component: SignUpPage },
      { path: '/dashboard', name: 'dashboard', component: { template: '<div>Dashboard</div>' } },
    ],
  })
}

function mountPage() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createTestRouter()

  const wrapper = mount(SignUpPage, {
    global: {
      plugins: [pinia, router],
    },
  })
  return { wrapper, router, pinia }
}

describe('SignUpPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('renders sign-up form with all fields', () => {
    const { wrapper } = mountPage()
    expect(wrapper.find('h2').text()).toContain('Create Account')
    const form = wrapper.findComponent(SignUpForm)
    expect(form.exists()).toBe(true)
    expect(wrapper.find('input[placeholder="Choose a username"]').exists()).toBe(true)
    expect(wrapper.findAll('input[type="password"]').length).toBe(2)
  })

  it('shows validation error for weak password', async () => {
    const { wrapper } = mountPage()

    const usernameInput = wrapper.find('input[placeholder="Choose a username"]')
    await usernameInput.setValue('testuser')

    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('123')
    await passwordInputs[1].setValue('123')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Password must be at least 8 characters')
  })

  it('shows error when passwords do not match', async () => {
    const { wrapper } = mountPage()

    const usernameInput = wrapper.find('input[placeholder="Choose a username"]')
    await usernameInput.setValue('testuser')

    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('StrongPass1')
    await passwordInputs[1].setValue('DifferentPass1')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Passwords do not match')
  })

  it('calls signUp on valid submit', async () => {
    mockAdapter.signUp.mockResolvedValue({ success: true, data: {} })

    const { wrapper } = mountPage()

    const usernameInput = wrapper.find('input[placeholder="Choose a username"]')
    await usernameInput.setValue('newuser')

    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('StrongPass1')
    await passwordInputs[1].setValue('StrongPass1')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockAdapter.signUp).toHaveBeenCalledWith(
      expect.objectContaining({
        username: 'newuser',
        password: 'StrongPass1',
      }),
    )
  })

  it('navigates to sign-in on success', async () => {
    mockAdapter.signUp.mockResolvedValue({ success: true, data: {} })

    const { wrapper, router } = mountPage()
    const pushSpy = vi.spyOn(router, 'push')

    const usernameInput = wrapper.find('input[placeholder="Choose a username"]')
    await usernameInput.setValue('newuser')

    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('StrongPass1')
    await passwordInputs[1].setValue('StrongPass1')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(pushSpy).toHaveBeenCalledWith('/sign-in')
  })

  it('displays error on duplicate username', async () => {
    mockAdapter.signUp.mockRejectedValue({
      isAxiosError: true,
      response: {
        status: 409,
        data: { message: 'Username already taken' },
      },
    })

    const { wrapper } = mountPage()

    const usernameInput = wrapper.find('input[placeholder="Choose a username"]')
    await usernameInput.setValue('existinguser')

    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('StrongPass1')
    await passwordInputs[1].setValue('StrongPass1')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Username already taken')
  })
})
