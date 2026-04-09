<script setup lang="ts">
import SignUpForm from '../components/SignUpForm.vue'
</script>

<template>
  <div class="sign-up-page">
    <h2 class="sign-up-page__title">Create Account</h2>
    <SignUpForm />
  </div>
</template>

<style scoped>
.sign-up-page__title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  text-align: center;
  margin: 0 0 24px;
}
</style>
