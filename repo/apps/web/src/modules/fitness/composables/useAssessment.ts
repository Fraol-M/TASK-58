import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useFitnessStore } from '../store'
import type { AssessmentFormData } from '../types'

export function useAssessment() {
  const store = useFitnessStore()
  const { assessment, loading, error } = storeToRefs(store)

  const currentStep = ref(1)
  const totalSteps = 3
  const submitting = ref(false)

  const form = ref<AssessmentFormData>({
    heightFeet: 5,
    heightInches: 6,
    weightLbs: 150,
    bodyFatPercent: '',
    waist: '',
    chest: '',
    arm: '',
    notes: '',
  })

  const stepErrors = ref<Record<string, string>>({})

  function validateStep1(): boolean {
    stepErrors.value = {}
    if (form.value.heightFeet < 3 || form.value.heightFeet > 8) {
      stepErrors.value.heightFeet = 'Height must be between 3 and 8 feet'
    }
    if (form.value.heightInches < 0 || form.value.heightInches > 11) {
      stepErrors.value.heightInches = 'Inches must be between 0 and 11'
    }
    if (form.value.weightLbs < 50 || form.value.weightLbs > 500) {
      stepErrors.value.weightLbs = 'Weight must be between 50 and 500 lbs'
    }
    return Object.keys(stepErrors.value).length === 0
  }

  function validateStep2(): boolean {
    stepErrors.value = {}
    const bf = form.value.bodyFatPercent
    if (bf && (Number(bf) < 1 || Number(bf) > 60)) {
      stepErrors.value.bodyFatPercent = 'Body fat must be between 1% and 60%'
    }
    return Object.keys(stepErrors.value).length === 0
  }

  const canGoNext = computed(() => {
    if (currentStep.value === 1) return true
    if (currentStep.value === 2) return true
    return false
  })

  function nextStep() {
    if (currentStep.value === 1 && !validateStep1()) return
    if (currentStep.value === 2 && !validateStep2()) return
    if (currentStep.value < totalSteps) {
      currentStep.value++
    }
  }

  function prevStep() {
    if (currentStep.value > 1) {
      currentStep.value--
      stepErrors.value = {}
    }
  }

  async function submit() {
    submitting.value = true
    try {
      await store.saveAssessment({
        heightFeet: form.value.heightFeet,
        heightInches: form.value.heightInches,
        weightLbs: form.value.weightLbs,
        bodyFatPercent: form.value.bodyFatPercent ? Number(form.value.bodyFatPercent) : undefined,
        waist: form.value.waist ? Number(form.value.waist) : undefined,
        chest: form.value.chest ? Number(form.value.chest) : undefined,
        arm: form.value.arm ? Number(form.value.arm) : undefined,
        notes: form.value.notes || undefined,
      })
    } finally {
      submitting.value = false
    }
  }

  function prefillFromExisting() {
    if (assessment.value) {
      const a = assessment.value
      form.value.heightFeet = a.heightFeet
      form.value.heightInches = a.heightInches
      form.value.weightLbs = a.weightLbs
      form.value.notes = ''
    }
  }

  onMounted(async () => {
    await store.fetchAssessment()
    prefillFromExisting()
  })

  return {
    form,
    currentStep,
    totalSteps,
    stepErrors,
    submitting,
    loading,
    error,
    assessment,
    canGoNext,
    nextStep,
    prevStep,
    submit,
  }
}
