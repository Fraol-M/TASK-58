import { ref, computed } from 'vue'

type ValidationRule = (value: unknown) => string | null
type RulesMap = Record<string, ValidationRule[]>

export function useValidation(rules: RulesMap) {
  const errors = ref<Record<string, string>>({})

  function validate(values: Record<string, unknown>): boolean {
    const newErrors: Record<string, string> = {}
    let valid = true

    for (const [field, fieldRules] of Object.entries(rules)) {
      const value = values[field]
      for (const rule of fieldRules) {
        const errorMsg = rule(value)
        if (errorMsg) {
          newErrors[field] = errorMsg
          valid = false
          break
        }
      }
    }

    errors.value = newErrors
    return valid
  }

  const isValid = computed(() => Object.keys(errors.value).length === 0)

  return {
    errors,
    isValid,
    validate,
  }
}
