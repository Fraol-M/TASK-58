import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useFormState } from '@/composables/useFormState'
import { useAuthStore } from '../store'
import { required, email as emailRule, passwordStrength } from '@/utils/validation-rules'
import { normalizeError } from '@/utils/error-normalizer'

export function useSignUp() {
  const router = useRouter()
  const authStore = useAuthStore()
  const serverError = ref('')

  const form = useFormState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  })

  function passwordMatch(value: unknown): string | null {
    if (value !== form.values.value.password) {
      return 'Passwords do not match'
    }
    return null
  }

  const rules = {
    username: [required],
    email: [emailRule],
    password: [required, passwordStrength],
    confirmPassword: [required, passwordMatch],
  }

  async function handleSubmit() {
    serverError.value = ''
    if (!form.validate(rules)) return

    await form.submit(async () => {
      try {
        await authStore.register({
          username: form.values.value.username,
          password: form.values.value.password,
          confirmPassword: form.values.value.confirmPassword,
          email: form.values.value.email || undefined,
        })
        router.push('/sign-in')
      } catch (e) {
        const error = normalizeError(e)
        serverError.value = error.message
        if (error.fieldErrors) {
          for (const [field, msg] of Object.entries(error.fieldErrors)) {
            form.setError(field as keyof typeof form.values.value, msg)
          }
        }
      }
    })
  }

  return {
    form,
    serverError,
    handleSubmit,
  }
}
