<script setup lang="ts">
import { useAssessment } from '../composables/useAssessment'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import FormTextarea from '@/components/forms/FormTextarea.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'
import AppButton from '@/components/common/AppButton.vue'

const {
  form,
  currentStep,
  totalSteps,
  stepErrors,
  submitting,
  nextStep,
  prevStep,
  submit,
} = useAssessment()

const emit = defineEmits<{
  saved: []
}>()

const feetOptions = Array.from({ length: 6 }, (_, i) => ({
  value: String(i + 3),
  label: `${i + 3} ft`,
}))

const inchesOptions = Array.from({ length: 12 }, (_, i) => ({
  value: String(i),
  label: `${i} in`,
}))

async function handleSubmit() {
  try {
    await submit()
    emit('saved')
  } catch {
    // error handled by store
  }
}
</script>

<template>
  <div class="assessment-form">
    <div class="assessment-form__steps">
      <div
        v-for="step in totalSteps"
        :key="step"
        :class="['step-dot', {
          'step-dot--active': step === currentStep,
          'step-dot--completed': step < currentStep
        }]"
      >
        <span class="step-dot__number">{{ step }}</span>
        <span class="step-dot__label">
          {{ step === 1 ? 'Height & Weight' : step === 2 ? 'Measurements' : 'Review' }}
        </span>
      </div>
    </div>

    <form @submit.prevent="handleSubmit">
      <!-- Step 1: Height & Weight -->
      <div v-if="currentStep === 1" class="assessment-form__section">
        <h3 class="assessment-form__section-title">Height & Weight</h3>
        <div class="assessment-form__row">
          <FormField label="Height (feet)" required :error="stepErrors.heightFeet">
            <FormSelect
              :model-value="String(form.heightFeet)"
              :options="feetOptions"
              @update:model-value="form.heightFeet = Number($event)"
            />
          </FormField>
          <FormField label="Height (inches)" required :error="stepErrors.heightInches">
            <FormSelect
              :model-value="String(form.heightInches)"
              :options="inchesOptions"
              @update:model-value="form.heightInches = Number($event)"
            />
          </FormField>
        </div>
        <FormField label="Weight (lbs)" required :error="stepErrors.weightLbs">
          <FormInput
            :model-value="String(form.weightLbs)"
            type="number"
            placeholder="Enter weight in pounds"
            @update:model-value="form.weightLbs = Number($event)"
          />
        </FormField>
      </div>

      <!-- Step 2: Body Measurements -->
      <div v-if="currentStep === 2" class="assessment-form__section">
        <h3 class="assessment-form__section-title">Body Measurements (Optional)</h3>
        <FormField label="Body Fat %" :error="stepErrors.bodyFatPercent" hint="Optional">
          <FormInput
            v-model="form.bodyFatPercent"
            type="number"
            placeholder="e.g., 18.5"
          />
        </FormField>
        <div class="assessment-form__row">
          <FormField label="Waist (inches)" hint="Optional">
            <FormInput v-model="form.waist" type="number" placeholder="e.g., 32" />
          </FormField>
          <FormField label="Chest (inches)" hint="Optional">
            <FormInput v-model="form.chest" type="number" placeholder="e.g., 38" />
          </FormField>
          <FormField label="Arm (inches)" hint="Optional">
            <FormInput v-model="form.arm" type="number" placeholder="e.g., 14" />
          </FormField>
        </div>
        <FormField label="Notes">
          <FormTextarea v-model="form.notes" placeholder="Any additional notes..." :rows="3" />
        </FormField>
      </div>

      <!-- Step 3: Review -->
      <div v-if="currentStep === 3" class="assessment-form__section">
        <h3 class="assessment-form__section-title">Review Your Assessment</h3>
        <div class="review-grid">
          <div class="review-item">
            <span class="review-item__label">Height</span>
            <span class="review-item__value">{{ form.heightFeet }}' {{ form.heightInches }}"</span>
          </div>
          <div class="review-item">
            <span class="review-item__label">Weight</span>
            <span class="review-item__value">{{ form.weightLbs }} lbs</span>
          </div>
          <div v-if="form.bodyFatPercent" class="review-item">
            <span class="review-item__label">Body Fat</span>
            <span class="review-item__value">{{ form.bodyFatPercent }}%</span>
          </div>
          <div v-if="form.waist" class="review-item">
            <span class="review-item__label">Waist</span>
            <span class="review-item__value">{{ form.waist }}"</span>
          </div>
          <div v-if="form.chest" class="review-item">
            <span class="review-item__label">Chest</span>
            <span class="review-item__value">{{ form.chest }}"</span>
          </div>
          <div v-if="form.arm" class="review-item">
            <span class="review-item__label">Arm</span>
            <span class="review-item__value">{{ form.arm }}"</span>
          </div>
          <div v-if="form.notes" class="review-item review-item--full">
            <span class="review-item__label">Notes</span>
            <span class="review-item__value">{{ form.notes }}</span>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <div class="assessment-form__nav">
        <AppButton v-if="currentStep > 1" variant="secondary" @click="prevStep">
          Back
        </AppButton>
        <span v-else></span>
        <AppButton v-if="currentStep < totalSteps" @click="nextStep">
          Next
        </AppButton>
        <SubmitButton
          v-else
          :loading="submitting"
          text="Save Assessment"
          loading-text="Saving..."
        />
      </div>
    </form>
  </div>
</template>

<style scoped>
.assessment-form__steps {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 32px;
}

.step-dot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-dot__number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  background: #e5e7eb;
  color: #6b7280;
  transition: all 0.2s;
}

.step-dot--active .step-dot__number {
  background: #4f46e5;
  color: #fff;
}

.step-dot--completed .step-dot__number {
  background: #059669;
  color: #fff;
}

.step-dot__label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.step-dot--active .step-dot__label {
  color: #4f46e5;
}

.assessment-form__section {
  margin-bottom: 24px;
}

.assessment-form__section-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px;
}

.assessment-form__row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.assessment-form__nav {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.review-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.review-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.review-item--full {
  grid-column: 1 / -1;
}

.review-item__label {
  font-size: 13px;
  color: #6b7280;
}

.review-item__value {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}
</style>
