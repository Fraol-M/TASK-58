import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useFormState } from '@/composables/useFormState'
import { useAuthStore } from '../store'
import { required } from '@/utils/validation-rules'
import { normalizeError } from '@/utils/error-normalizer'

export function useSignIn() {
  const router = useRouter()
  const route = useRoute()
  const authStore = useAuthStore()
  const serverError = ref('')

  const form = useFormState({
    username: '',
    password: '',
  })

  const rules = {
    username: [required],
    password: [required],
  }

  async function handleSubmit() {
    serverError.value = ''
    if (!form.validate(rules)) return

    await form.submit(async () => {
      try {
        await authStore.login({
          username: form.values.value.username,
          password: form.values.value.password,
        })
        const redirect = (route.query.redirect as string) || '/dashboard'
        router.push(redirect)
      } catch (e) {
        const error = normalizeError(e)
        serverError.value = error.message
      }
    })
  }

  return {
    form,
    serverError,
    handleSubmit,
  }
}
