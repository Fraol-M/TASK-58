import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import SubmitButton from '../forms/SubmitButton.vue'

describe('SubmitButton', () => {
  it('renders button text', () => {
    const wrapper = mount(SubmitButton, {
      props: { text: 'Save Changes' },
    })
    expect(wrapper.text()).toBe('Save Changes')
  })

  it('shows spinner when loading', () => {
    const wrapper = mount(SubmitButton, {
      props: { loading: true, loadingText: 'Saving...' },
    })
    expect(wrapper.find('.submit-btn__spinner').exists()).toBe(true)
    expect(wrapper.text()).toBe('Saving...')
  })

  it('is disabled when loading', () => {
    const wrapper = mount(SubmitButton, {
      props: { loading: true },
    })
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('prevents duplicate clicks', async () => {
    const wrapper = mount(SubmitButton, {
      props: { loading: true, text: 'Submit' },
    })

    const button = wrapper.find('button')
    expect(button.attributes('disabled')).toBeDefined()

    // When not loading, button should be enabled
    await wrapper.setProps({ loading: false })
    expect(button.attributes('disabled')).toBeUndefined()

    // When disabled prop is set, button should be disabled
    await wrapper.setProps({ disabled: true })
    expect(button.attributes('disabled')).toBeDefined()
  })
})
