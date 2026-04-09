import { ref, computed, type Ref } from 'vue'

type ValidationRule = (value: unknown) => string | null
type RulesMap<T> = Partial<Record<keyof T, ValidationRule[]>>

export interface FormState<T extends Record<string, unknown>> {
  values: Ref<T>
  errors: Ref<Partial<Record<keyof T, string>>>
  touched: Ref<Partial<Record<keyof T, boolean>>>
  isSubmitting: Ref<boolean>
  isDirty: Ref<boolean>
  setField: <K extends keyof T>(name: K, value: T[K]) => void
  setError: (name: keyof T, msg: string) => void
  validate: (rules: RulesMap<T>) => boolean
  submit: (fn: () => Promise<void>) => Promise<void>
  reset: () => void
}

export function useFormState<T extends Record<string, unknown>>(
  initialValues: T,
): FormState<T> {
  const values = ref<T>({ ...initialValues }) as Ref<T>
  const errors = ref<Partial<Record<keyof T, string>>>({})
  const touched = ref<Partial<Record<keyof T, boolean>>>({})
  const isSubmitting = ref(false)

  const originalValues = { ...initialValues }

  const isDirty = computed(() => {
    const keys = Object.keys(originalValues) as (keyof T)[]
    return keys.some((key) => values.value[key] !== originalValues[key])
  })

  function setField<K extends keyof T>(name: K, value: T[K]) {
    values.value[name] = value
    touched.value = { ...touched.value, [name]: true }
    // Clear error when field changes
    if (errors.value[name]) {
      const next = { ...errors.value }
      delete next[name]
      errors.value = next
    }
  }

  function setError(name: keyof T, msg: string) {
    errors.value = { ...errors.value, [name]: msg }
  }

  function validate(rules: RulesMap<T>): boolean {
    const newErrors: Partial<Record<keyof T, string>> = {}
    let valid = true

    for (const [field, fieldRules] of Object.entries(rules)) {
      if (!fieldRules) continue
      const value = values.value[field as keyof T]
      for (const rule of fieldRules as ValidationRule[]) {
        const errorMsg = rule(value)
        if (errorMsg) {
          newErrors[field as keyof T] = errorMsg
          valid = false
          break
        }
      }
    }

    errors.value = newErrors
    return valid
  }

  async function submit(fn: () => Promise<void>) {
    isSubmitting.value = true
    try {
      await fn()
    } finally {
      isSubmitting.value = false
    }
  }

  function reset() {
    values.value = { ...initialValues } as T
    errors.value = {}
    touched.value = {}
    isSubmitting.value = false
  }

  return {
    values,
    errors: errors as Ref<Partial<Record<keyof T, string>>>,
    touched: touched as Ref<Partial<Record<keyof T, boolean>>>,
    isSubmitting,
    isDirty: isDirty as unknown as Ref<boolean>,
    setField,
    setError,
    validate,
    submit,
    reset,
  }
}
