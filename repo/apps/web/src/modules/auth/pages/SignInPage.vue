<script setup lang="ts">
import SignInForm from '../components/SignInForm.vue'
</script>

<template>
  <div class="sign-in-page">
    <h2 class="sign-in-page__title">Sign In to CampusFit</h2>
    <SignInForm />
  </div>
</template>

<style scoped>
.sign-in-page__title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  text-align: center;
  margin: 0 0 24px;
}
</style>
