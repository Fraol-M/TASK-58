import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

// Stub the adapter so option-loading calls don't fail
vi.mock('@/services/adapters/adapter-factory', () => ({
  getAdapter: () => ({
    getItems: vi.fn().mockResolvedValue({ data: [] }),
  }),
}))

import EntityForm from '../components/EntityForm.vue'

const stubs = {
  FormField: {
    template: '<div class="form-field" :data-error="error"><label v-if="required" data-testid="required-marker">*</label><slot /></div>',
    props: { label: String, required: Boolean, error: String },
  },
  FormInput: {
    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" data-testid="form-input" />',
    props: ['modelValue', 'type', 'placeholder'],
    emits: ['update:modelValue'],
  },
  FormDatePicker: {
    template: '<input type="date" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" :data-field="$attrs[\'data-field\']" />',
    props: ['modelValue', 'min'],
    emits: ['update:modelValue'],
  },
  FormSelect: {
    template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option value="">--</option><option v-for="o in options" :key="o.value" :value="o.value">{{ o.label }}</option></select>',
    props: ['modelValue', 'options', 'placeholder'],
    emits: ['update:modelValue'],
  },
  SubmitButton: {
    template: '<button type="submit" :disabled="disabled" data-testid="submit-btn"><slot /></button>',
    props: ['loading', 'text', 'loadingText', 'disabled'],
  },
}

function mountForm(entityType = 'school', initialData?: Record<string, any>) {
  return mount(EntityForm, {
    props: { entityType, initialData },
    global: { stubs },
  })
}

describe('EntityForm – effectiveFrom is required', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('submit button is disabled when effectiveFrom is empty', async () => {
    const wrapper = mountForm('school')
    await flushPromises()

    // Fill code and name but leave effectiveFrom empty
    const inputs = wrapper.findAll('input[data-testid="form-input"]')
    await inputs[0].setValue('SCH-001')
    await inputs[1].setValue('Engineering School')

    const btn = wrapper.find('[data-testid="submit-btn"]')
    expect((btn.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('submit button becomes enabled once effectiveFrom is filled', async () => {
    const wrapper = mountForm('school')
    await flushPromises()

    const inputs = wrapper.findAll('input[data-testid="form-input"]')
    await inputs[0].setValue('SCH-001')
    await inputs[1].setValue('Engineering School')

    // Fill effectiveFrom
    const datePickers = wrapper.findAll('input[type="date"]')
    await datePickers[0].setValue('2026-01-01')

    const btn = wrapper.find('[data-testid="submit-btn"]')
    expect((btn.element as HTMLButtonElement).disabled).toBe(false)
  })

  it('does not emit submit when effectiveFrom is missing', async () => {
    const wrapper = mountForm('school')
    await flushPromises()

    const inputs = wrapper.findAll('input[data-testid="form-input"]')
    await inputs[0].setValue('SCH-001')
    await inputs[1].setValue('Engineering School')
    // effectiveFrom left empty

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.emitted('submit')).toBeFalsy()
  })

  it('emits submit with effectiveFrom when all required fields are filled (school)', async () => {
    const wrapper = mountForm('school')
    await flushPromises()

    const inputs = wrapper.findAll('input[data-testid="form-input"]')
    await inputs[0].setValue('SCH-001')
    await inputs[1].setValue('Engineering School')

    const datePickers = wrapper.findAll('input[type="date"]')
    await datePickers[0].setValue('2026-01-01')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.emitted('submit')).toBeTruthy()
    const [payload] = wrapper.emitted('submit')![0] as [Record<string, any>]
    expect(payload.effectiveFrom).toBe('2026-01-01')
    expect(payload.code).toBe('SCH-001')
  })

  it('emits submit with effectiveFrom for term entity type', async () => {
    const wrapper = mountForm('term')
    await flushPromises()

    const inputs = wrapper.findAll('input[data-testid="form-input"]')
    await inputs[0].setValue('T-001')
    await inputs[1].setValue('Fall 2026')

    const datePickers = wrapper.findAll('input[type="date"]')
    // term: startDate[0], endDate[1], effectiveFrom[2]
    await datePickers[0].setValue('2026-09-01')
    await datePickers[1].setValue('2026-12-31')
    await datePickers[2].setValue('2026-01-01')

    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.emitted('submit')).toBeTruthy()
    const [payload] = wrapper.emitted('submit')![0] as [Record<string, any>]
    expect(payload.effectiveFrom).toBe('2026-01-01')
    expect(payload.startDate).toBe('2026-09-01')
    expect(payload.endDate).toBe('2026-12-31')
  })

  it('Effective From field is marked required in the template', async () => {
    const wrapper = mountForm('school')
    await flushPromises()

    // FormField with required prop will render the required-marker
    const requiredMarkers = wrapper.findAll('[data-testid="required-marker"]')
    // code, name, and effectiveFrom are all required
    expect(requiredMarkers.length).toBeGreaterThanOrEqual(3)
  })
})
