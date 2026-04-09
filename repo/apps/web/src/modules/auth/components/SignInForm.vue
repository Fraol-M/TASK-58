<script setup lang="ts">
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'
import { useSignIn } from '../composables/useSignIn'

const { form, serverError, handleSubmit } = useSignIn()
</script>

<template>
  <form class="sign-in-form" @submit.prevent="handleSubmit">
    <div v-if="serverError" class="sign-in-form__error-banner">
      {{ serverError }}
    </div>

    <FormField label="Username" :error="form.errors.value.username" required>
      <FormInput
        :model-value="form.values.value.username"
        placeholder="Enter your username"
        :error="form.errors.value.username"
        @update:model-value="form.setField('username', $event)"
      />
    </FormField>

    <FormField label="Password" :error="form.errors.value.password" required>
      <FormInput
        :model-value="form.values.value.password"
        type="password"
        placeholder="Enter your password"
        :error="form.errors.value.password"
        @update:model-value="form.setField('password', $event)"
      />
    </FormField>

    <SubmitButton
      :loading="form.isSubmitting.value"
      text="Sign In"
      loading-text="Signing in..."
    />

    <p class="sign-in-form__link">
      Don't have an account?
      <router-link to="/sign-up">Create one</router-link>
    </p>
  </form>
</template>

<style scoped>
.sign-in-form__error-banner {
  padding: 10px 14px;
  margin-bottom: 16px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  color: #dc2626;
  font-size: 14px;
}

.sign-in-form__link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #6b7280;
}

.sign-in-form__link a {
  color: #4f46e5;
  font-weight: 500;
  text-decoration: none;
}
.sign-in-form__link a:hover {
  text-decoration: underline;
}
</style>
