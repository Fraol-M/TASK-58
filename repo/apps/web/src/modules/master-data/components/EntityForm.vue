<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import FormDatePicker from '@/components/forms/FormDatePicker.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'
import { getAdapter } from '@/services/adapters/adapter-factory'

interface Props {
  entityType: string
  initialData?: Record<string, any>
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  submit: [data: Record<string, any>]
}>()

const adapter = getAdapter()

// Common fields
const code = ref(props.initialData?.code ?? '')
const name = ref(props.initialData?.name ?? '')
const effectiveFrom = ref(props.initialData?.effectiveFrom ?? '')
const effectiveTo = ref(props.initialData?.effectiveTo ?? '')

// Term-specific
const startDate = ref(props.initialData?.startDate ?? '')
const endDate = ref(props.initialData?.endDate ?? '')

// Major-specific
const schoolId = ref(props.initialData?.schoolId ? String(props.initialData.schoolId) : '')

// Class-specific
const majorId = ref(props.initialData?.majorId ? String(props.initialData.majorId) : '')
const year = ref(props.initialData?.year ? String(props.initialData.year) : '')

// Course-specific
const classId = ref(props.initialData?.classId ? String(props.initialData.classId) : '')
const termId = ref(props.initialData?.termId ? String(props.initialData.termId) : '')
const credits = ref(props.initialData?.credits ? String(props.initialData.credits) : '')

// Options for hierarchy selectors
const schoolOptions = ref<{ value: string; label: string }[]>([])
const majorOptions = ref<{ value: string; label: string }[]>([])
const classOptions = ref<{ value: string; label: string }[]>([])
const termOptions = ref<{ value: string; label: string }[]>([])

const errors = ref<Record<string, string>>({})

async function loadOptions() {
  try {
    if (props.entityType === 'major') {
      const res = await adapter.getItems('school')
      schoolOptions.value = (res.data || []).map((s: any) => ({ value: String(s.id), label: `${s.code} - ${s.name}` }))
    } else if (props.entityType === 'class') {
      const res = await adapter.getItems('major')
      majorOptions.value = (res.data || []).map((m: any) => ({ value: String(m.id), label: `${m.code} - ${m.name}` }))
    } else if (props.entityType === 'course') {
      const [classRes, termRes] = await Promise.all([
        adapter.getItems('class'),
        adapter.getItems('term'),
      ])
      classOptions.value = (classRes.data || []).map((c: any) => ({ value: String(c.id), label: `${c.code} - ${c.name}` }))
      termOptions.value = (termRes.data || []).map((t: any) => ({ value: String(t.id), label: `${t.code} - ${t.name}` }))
    }
  } catch {
    // Options loading failed silently
  }
}

onMounted(loadOptions)
watch(() => props.entityType, loadOptions)

const isValid = computed(() => {
  if (!code.value.trim() || !name.value.trim()) return false
  if (!effectiveFrom.value) return false
  if (props.entityType === 'term' && (!startDate.value || !endDate.value)) return false
  if (props.entityType === 'major' && !schoolId.value) return false
  if (props.entityType === 'class' && (!majorId.value || !year.value)) return false
  if (props.entityType === 'course' && (!classId.value || !termId.value)) return false
  return true
})

function validate(): boolean {
  errors.value = {}
  if (!code.value.trim()) errors.value.code = 'Code is required'
  if (!name.value.trim()) errors.value.name = 'Name is required'

  if (props.entityType === 'term') {
    if (!startDate.value) errors.value.startDate = 'Start date is required'
    if (!endDate.value) errors.value.endDate = 'End date is required'
    if (startDate.value && endDate.value && endDate.value < startDate.value) {
      errors.value.endDate = 'End date must be after start date'
    }
  }

  if (props.entityType === 'major' && !schoolId.value) {
    errors.value.schoolId = 'School is required'
  }

  if (props.entityType === 'class') {
    if (!majorId.value) errors.value.majorId = 'Major is required'
    if (!year.value) errors.value.year = 'Year is required'
  }

  if (props.entityType === 'course') {
    if (!classId.value) errors.value.classId = 'Class is required'
    if (!termId.value) errors.value.termId = 'Term is required'
  }

  if (!effectiveFrom.value) {
    errors.value.effectiveFrom = 'Effective from date is required'
  }

  if (effectiveFrom.value && effectiveTo.value && effectiveTo.value < effectiveFrom.value) {
    errors.value.effectiveTo = 'Effective To must be after Effective From'
  }

  return Object.keys(errors.value).length === 0
}

function handleSubmit() {
  if (!validate()) return

  const data: Record<string, any> = {
    code: code.value,
    name: name.value,
    effectiveFrom: effectiveFrom.value,
    effectiveTo: effectiveTo.value || undefined,
  }

  if (props.entityType === 'term') {
    data.startDate = startDate.value
    data.endDate = endDate.value
  } else if (props.entityType === 'major') {
    data.schoolId = Number(schoolId.value)
  } else if (props.entityType === 'class') {
    data.majorId = Number(majorId.value)
    data.year = Number(year.value)
  } else if (props.entityType === 'course') {
    data.classId = Number(classId.value)
    data.termId = Number(termId.value)
    data.credits = credits.value ? Number(credits.value) : 0
  }

  emit('submit', data)
}

const entityLabel = computed(() => {
  return props.entityType.charAt(0).toUpperCase() + props.entityType.slice(1)
})
</script>

<template>
  <form class="entity-form" @submit.prevent="handleSubmit">
    <FormField :label="`${entityLabel} Code`" required :error="errors.code">
      <FormInput v-model="code" :placeholder="`e.g., ${entityType.toUpperCase()}-001`" />
    </FormField>

    <FormField :label="`${entityLabel} Name`" required :error="errors.name">
      <FormInput v-model="name" :placeholder="`Enter ${entityType} name`" />
    </FormField>

    <!-- Term-specific: start/end dates -->
    <template v-if="entityType === 'term'">
      <div class="entity-form__dates">
        <FormField label="Start Date" required :error="errors.startDate">
          <FormDatePicker v-model="startDate" />
        </FormField>
        <FormField label="End Date" required :error="errors.endDate">
          <FormDatePicker v-model="endDate" :min="startDate" />
        </FormField>
      </div>
    </template>

    <!-- Major-specific: school selector -->
    <template v-if="entityType === 'major'">
      <FormField label="School" required :error="errors.schoolId">
        <FormSelect
          v-model="schoolId"
          :options="schoolOptions"
          placeholder="Select a school"
        />
      </FormField>
    </template>

    <!-- Class-specific: major selector + year -->
    <template v-if="entityType === 'class'">
      <FormField label="Major" required :error="errors.majorId">
        <FormSelect
          v-model="majorId"
          :options="majorOptions"
          placeholder="Select a major"
        />
      </FormField>
      <FormField label="Year" required :error="errors.year">
        <FormInput v-model="year" type="number" placeholder="e.g., 2026" />
      </FormField>
    </template>

    <!-- Course-specific: class + term selectors + credits -->
    <template v-if="entityType === 'course'">
      <FormField label="Class" required :error="errors.classId">
        <FormSelect
          v-model="classId"
          :options="classOptions"
          placeholder="Select a class"
        />
      </FormField>
      <FormField label="Term" required :error="errors.termId">
        <FormSelect
          v-model="termId"
          :options="termOptions"
          placeholder="Select a term"
        />
      </FormField>
      <FormField label="Credits">
        <FormInput v-model="credits" type="number" placeholder="e.g., 3" />
      </FormField>
    </template>

    <div class="entity-form__dates">
      <FormField label="Effective From" required :error="errors.effectiveFrom">
        <FormDatePicker v-model="effectiveFrom" />
      </FormField>
      <FormField label="Effective To" :error="errors.effectiveTo">
        <FormDatePicker v-model="effectiveTo" :min="effectiveFrom" />
      </FormField>
    </div>

    <SubmitButton
      :loading="loading"
      :text="initialData ? `Update ${entityLabel}` : `Create ${entityLabel}`"
      :loading-text="`Saving...`"
      :disabled="!isValid"
    />
  </form>
</template>

<style scoped>
.entity-form__dates {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
</style>
