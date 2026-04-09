<script setup lang="ts">
import { computed } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'
import { useSignUp } from '../composables/useSignUp'

const { form, serverError, handleSubmit } = useSignUp()

const passwordStrengthLevel = computed(() => {
  const pw = form.values.value.password
  if (!pw) return 0
  let score = 0
  if (pw.length >= 8) score++
  if (/[a-z]/.test(pw)) score++
  if (/[A-Z]/.test(pw)) score++
  if (/[0-9]/.test(pw)) score++
  return score
})

const strengthLabel = computed(() => {
  switch (passwordStrengthLevel.value) {
    case 0: return ''
    case 1: return 'Weak'
    case 2: return 'Fair'
    case 3: return 'Good'
    case 4: return 'Strong'
    default: return ''
  }
})

const strengthColor = computed(() => {
  switch (passwordStrengthLevel.value) {
    case 1: return '#dc2626'
    case 2: return '#d97706'
    case 3: return '#2563eb'
    case 4: return '#059669'
    default: return '#e5e7eb'
  }
})
</script>

<template>
  <form class="sign-up-form" @submit.prevent="handleSubmit">
    <div v-if="serverError" class="sign-up-form__error-banner">
      {{ serverError }}
    </div>

    <FormField label="Username" :error="form.errors.value.username" required>
      <FormInput
        :model-value="form.values.value.username"
        placeholder="Choose a username"
        :error="form.errors.value.username"
        @update:model-value="form.setField('username', $event)"
      />
    </FormField>

    <FormField label="Email" :error="form.errors.value.email" hint="Optional">
      <FormInput
        :model-value="form.values.value.email"
        type="email"
        placeholder="your@email.com"
        :error="form.errors.value.email"
        @update:model-value="form.setField('email', $event)"
      />
    </FormField>

    <FormField label="Password" :error="form.errors.value.password" required>
      <FormInput
        :model-value="form.values.value.password"
        type="password"
        placeholder="Create a strong password"
        :error="form.errors.value.password"
        @update:model-value="form.setField('password', $event)"
      />
      <div v-if="form.values.value.password" class="sign-up-form__strength">
        <div class="sign-up-form__strength-bar">
          <div
            class="sign-up-form__strength-fill"
            :style="{ width: `${passwordStrengthLevel * 25}%`, backgroundColor: strengthColor }"
          ></div>
        </div>
        <span class="sign-up-form__strength-label" :style="{ color: strengthColor }">
          {{ strengthLabel }}
        </span>
      </div>
    </FormField>

    <FormField label="Confirm Password" :error="form.errors.value.confirmPassword" required>
      <FormInput
        :model-value="form.values.value.confirmPassword"
        type="password"
        placeholder="Confirm your password"
        :error="form.errors.value.confirmPassword"
        @update:model-value="form.setField('confirmPassword', $event)"
      />
    </FormField>

    <SubmitButton
      :loading="form.isSubmitting.value"
      text="Create Account"
      loading-text="Creating account..."
    />

    <p class="sign-up-form__link">
      Already have an account?
      <router-link to="/sign-in">Sign in</router-link>
    </p>
  </form>
</template>

<style scoped>
.sign-up-form__error-banner {
  padding: 10px 14px;
  margin-bottom: 16px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  color: #dc2626;
  font-size: 14px;
}

.sign-up-form__strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.sign-up-form__strength-bar {
  flex: 1;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.sign-up-form__strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s, background-color 0.3s;
}

.sign-up-form__strength-label {
  font-size: 12px;
  font-weight: 500;
  min-width: 42px;
}

.sign-up-form__link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #6b7280;
}

.sign-up-form__link a {
  color: #4f46e5;
  font-weight: 500;
  text-decoration: none;
}
.sign-up-form__link a:hover {
  text-decoration: underline;
}
</style>
