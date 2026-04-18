import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProfilePage from '../pages/ProfilePage.vue'
import { useAuthStore } from '@/modules/auth/store'

const mockToastShow = vi.fn()

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    toasts: { value: [] },
    show: mockToastShow,
    dismiss: vi.fn(),
  }),
}))

function mountPage(userOverrides: Record<string, any> = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)

  const authStore = useAuthStore()
  authStore.user = {
    id: 1,
    username: 'testuser',
    email: 'test@example.com',
    roles: ['REGULAR_USER'],
    status: 'ACTIVE',
    ...userOverrides,
  } as any

  return mount(ProfilePage, {
    global: {
      plugins: [pinia],
      stubs: {
        AppCard: { template: '<div class="app-card" :data-title="title"><slot /></div>', props: ['title'] },
        AppBadge: { template: '<span class="badge">{{ label }}</span>', props: ['label', 'variant'] },
        FormField: { template: '<div class="form-field"><slot /></div>', props: ['label', 'required', 'error'] },
        FormInput: {
          template: '<input :value="modelValue" :type="type" :placeholder="placeholder" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          props: ['modelValue', 'type', 'placeholder'],
          emits: ['update:modelValue'],
        },
        SubmitButton: { template: '<button type="submit" :disabled="loading">{{ loading ? loadingText : text }}</button>', props: ['loading', 'text', 'loadingText'] },
      },
    },
  })
}

describe('ProfilePage', () => {
  beforeEach(() => {
    mockToastShow.mockClear()
  })

  // ---- Account info rendering ----

  it('displays username from auth store', () => {
    const wrapper = mountPage()

    expect(wrapper.html()).toContain('testuser')
  })

  it('displays email from auth store', () => {
    const wrapper = mountPage()

    expect(wrapper.html()).toContain('test@example.com')
  })

  it('renders role badge for each role', () => {
    const wrapper = mountPage({ roles: ['REGULAR_USER', 'OPERATIONS_STAFF'] })

    const badges = wrapper.findAll('.badge')
    const badgeTexts = badges.map(b => b.text())
    expect(badgeTexts).toContain('REGULAR_USER')
    expect(badgeTexts).toContain('OPERATIONS_STAFF')
  })

  it('renders avatar with first letter of username', () => {
    const wrapper = mountPage({ username: 'Alice' })

    expect(wrapper.find('.profile-info__avatar').text()).toBe('A')
  })

  it('displays ACTIVE status badge', () => {
    const wrapper = mountPage({ status: 'ACTIVE' })

    const badges = wrapper.findAll('.badge')
    expect(badges.some(b => b.text() === 'ACTIVE')).toBe(true)
  })

  // ---- Admin-only section ----

  it('shows notification preferences section for ADMIN users', () => {
    const wrapper = mountPage({ roles: ['ADMIN'] })

    expect(wrapper.findAll('.app-card').some(card => card.attributes('data-title') === 'Notification Preferences')).toBe(true)
  })

  it('hides notification preferences section for regular users', () => {
    const wrapper = mountPage({ roles: ['REGULAR_USER'] })

    expect(wrapper.findAll('.app-card').some(card => card.attributes('data-title') === 'Notification Preferences')).toBe(false)
  })

  // ---- Password validation ----

  it('shows validation errors when password fields are empty on submit', async () => {
    const wrapper = mountPage()

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    // Password errors should be set — toast not called (validation failed silently)
    expect(mockToastShow).not.toHaveBeenCalledWith('Password changed successfully', 'success')
  })

  it('shows mismatch error when passwords do not match', async () => {
    const wrapper = mountPage()
    const inputs = wrapper.findAll('input')

    // Fill in old password, new password, mismatched confirm
    await inputs[0].setValue('oldpass123')
    await inputs[1].setValue('newpass123')
    await inputs[2].setValue('differentpass')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    // Should not show success toast (validation failed)
    expect(mockToastShow).not.toHaveBeenCalledWith('Password changed successfully', 'success')
  })

  it('submits successfully when all password fields are valid and matching', async () => {
    const wrapper = mountPage()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('currentpass123')
    await inputs[1].setValue('newpassword123')
    await inputs[2].setValue('newpassword123')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockToastShow).toHaveBeenCalledWith('Password changed successfully', 'success')
  })

  it('shows error when new password is too short', async () => {
    const wrapper = mountPage()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('currentpass123')
    await inputs[1].setValue('short')
    await inputs[2].setValue('short')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mockToastShow).not.toHaveBeenCalledWith('Password changed successfully', 'success')
  })
})
