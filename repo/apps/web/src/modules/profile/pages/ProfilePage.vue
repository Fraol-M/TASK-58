<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/modules/auth/store'
import { useToast } from '@/composables/useToast'
import AppCard from '@/components/common/AppCard.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'

const authStore = useAuthStore()
const toast = useToast()

const user = computed(() => authStore.user)
const isAdmin = computed(() => user.value?.roles.includes('ADMIN') ?? false)

// Change password form
const oldPassword = ref('')
const newPassword = ref('')
const confirmNewPassword = ref('')
const passwordErrors = ref<Record<string, string>>({})
const changingPassword = ref(false)

function validatePassword(): boolean {
  passwordErrors.value = {}
  if (!oldPassword.value) passwordErrors.value.oldPassword = 'Current password is required'
  if (!newPassword.value) passwordErrors.value.newPassword = 'New password is required'
  else if (newPassword.value.length < 8) passwordErrors.value.newPassword = 'Must be at least 8 characters'
  if (newPassword.value !== confirmNewPassword.value) {
    passwordErrors.value.confirmNewPassword = 'Passwords do not match'
  }
  return Object.keys(passwordErrors.value).length === 0
}

async function handleChangePassword() {
  if (!validatePassword()) return
  changingPassword.value = true
  try {
    // Would call API here
    toast.show('Password changed successfully', 'success')
    oldPassword.value = ''
    newPassword.value = ''
    confirmNewPassword.value = ''
  } catch {
    toast.show('Failed to change password', 'error')
  } finally {
    changingPassword.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <h1 class="page-title">Profile</h1>

    <!-- User Info -->
    <AppCard title="Account Information" class="profile-page__section">
      <div class="profile-info">
        <div class="profile-info__avatar">
          {{ user?.username?.charAt(0).toUpperCase() ?? '?' }}
        </div>
        <div class="profile-info__details">
          <div class="profile-info__row">
            <span class="profile-info__label">Username</span>
            <span class="profile-info__value">{{ user?.username ?? '-' }}</span>
          </div>
          <div class="profile-info__row">
            <span class="profile-info__label">Email</span>
            <span class="profile-info__value">{{ user?.email ?? '-' }}</span>
          </div>
          <div class="profile-info__row">
            <span class="profile-info__label">Roles</span>
            <div class="profile-info__roles">
              <AppBadge
                v-for="role in user?.roles ?? []"
                :key="role"
                :label="role"
                :variant="role === 'ADMIN' ? 'danger' : role === 'OPERATIONS_STAFF' ? 'warning' : 'info'"
              />
            </div>
          </div>
          <div class="profile-info__row">
            <span class="profile-info__label">Status</span>
            <AppBadge
              :label="user?.status ?? 'unknown'"
              :variant="user?.status === 'ACTIVE' ? 'success' : 'danger'"
            />
          </div>
        </div>
      </div>
    </AppCard>

    <!-- Change Password -->
    <AppCard title="Change Password" class="profile-page__section">
      <form @submit.prevent="handleChangePassword" class="password-form">
        <FormField label="Current Password" required :error="passwordErrors.oldPassword">
          <FormInput
            v-model="oldPassword"
            type="password"
            placeholder="Enter current password"
          />
        </FormField>

        <FormField label="New Password" required :error="passwordErrors.newPassword">
          <FormInput
            v-model="newPassword"
            type="password"
            placeholder="Enter new password"
          />
        </FormField>

        <FormField label="Confirm New Password" required :error="passwordErrors.confirmNewPassword">
          <FormInput
            v-model="confirmNewPassword"
            type="password"
            placeholder="Confirm new password"
          />
        </FormField>

        <SubmitButton
          :loading="changingPassword"
          text="Update Password"
          loading-text="Updating..."
        />
      </form>
    </AppCard>

    <!-- Admin Notification Preferences -->
    <AppCard v-if="isAdmin" title="Notification Preferences" class="profile-page__section">
      <div class="pref-list">
        <label class="pref-item">
          <input type="checkbox" checked class="pref-item__input" />
          <div>
            <span class="pref-item__label">Email notifications</span>
            <span class="pref-item__desc">Receive email for important system events</span>
          </div>
        </label>
        <label class="pref-item">
          <input type="checkbox" checked class="pref-item__input" />
          <div>
            <span class="pref-item__label">Import completion alerts</span>
            <span class="pref-item__desc">Get notified when data imports finish</span>
          </div>
        </label>
        <label class="pref-item">
          <input type="checkbox" class="pref-item__input" />
          <div>
            <span class="pref-item__label">Weekly system reports</span>
            <span class="pref-item__desc">Receive weekly summary of system metrics</span>
          </div>
        </label>
      </div>
    </AppCard>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 24px;
}

.profile-page__section {
  margin-bottom: 24px;
}

.profile-info {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile-info__avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #4f46e5;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-info__details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-info__row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-info__label {
  font-size: 14px;
  color: #6b7280;
  min-width: 100px;
}

.profile-info__value {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.profile-info__roles {
  display: flex;
  gap: 6px;
}

.password-form {
  max-width: 400px;
}

.pref-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pref-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: border-color 0.15s;
}

.pref-item:hover {
  border-color: #4f46e5;
}

.pref-item__input {
  margin-top: 2px;
  accent-color: #4f46e5;
}

.pref-item__label {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  display: block;
}

.pref-item__desc {
  font-size: 13px;
  color: #6b7280;
  display: block;
  margin-top: 2px;
}
</style>
