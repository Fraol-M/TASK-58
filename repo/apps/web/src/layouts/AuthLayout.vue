<script setup lang="ts">
</script>

<template>
  <div class="auth-layout">
    <div class="auth-layout__container">
      <div class="auth-layout__brand">
        <h1 class="auth-layout__logo">CampusFit</h1>
        <p class="auth-layout__tagline">Your campus fitness and study companion</p>
      </div>
      <div class="auth-layout__card">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 50%, #c7d2fe 100%);
  padding: 20px;
}

.auth-layout__container {
  width: 100%;
  max-width: 420px;
}

.auth-layout__brand {
  text-align: center;
  margin-bottom: 32px;
}

.auth-layout__logo {
  font-size: 32px;
  font-weight: 800;
  color: #4f46e5;
  margin: 0 0 4px;
}

.auth-layout__tagline {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.auth-layout__card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}
</style>
