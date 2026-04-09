<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import FormField from '@/components/forms/FormField.vue'
import FormInput from '@/components/forms/FormInput.vue'
import FormTextarea from '@/components/forms/FormTextarea.vue'
import FormSelect from '@/components/forms/FormSelect.vue'
import SubmitButton from '@/components/forms/SubmitButton.vue'
import { getAdapter } from '@/services/adapters/adapter-factory'
import type { MasterDataItem } from '@/services/adapters/api-adapter.interface'

interface Props {
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<{
  submit: [data: {
    title: string
    description?: string
    termId?: number
    schoolId?: number
    majorId?: number
    classId?: number
    courseId?: number
  }]
}>()

const adapter = getAdapter()

const title = ref('')
const description = ref('')
const termId = ref('')
const schoolId = ref('')
const majorId = ref('')
const classId = ref('')
const courseId = ref('')

const terms = ref<MasterDataItem[]>([])
const schools = ref<MasterDataItem[]>([])
const majors = ref<MasterDataItem[]>([])
const classes = ref<MasterDataItem[]>([])
const courses = ref<MasterDataItem[]>([])

const errors = ref<Record<string, string>>({})

const isValid = computed(() => title.value.trim().length > 0)

onMounted(async () => {
  try {
    const [t, s, m, cl, co] = await Promise.all([
      adapter.getItems('term'),
      adapter.getItems('school'),
      adapter.getItems('major'),
      adapter.getItems('class'),
      adapter.getItems('course'),
    ])
    terms.value = Array.isArray(t.data) ? t.data : (t.data as any)?.content ?? []
    schools.value = Array.isArray(s.data) ? s.data : (s.data as any)?.content ?? []
    majors.value = Array.isArray(m.data) ? m.data : (m.data as any)?.content ?? []
    classes.value = Array.isArray(cl.data) ? cl.data : (cl.data as any)?.content ?? []
    courses.value = Array.isArray(co.data) ? co.data : (co.data as any)?.content ?? []
  } catch { /* hierarchy load is best-effort */ }
})

function toOptions(items: MasterDataItem[]) {
  return items.map(i => ({ value: String(i.id), label: `${i.code} - ${i.name}` }))
}

function validate(): boolean {
  errors.value = {}
  if (!title.value.trim()) errors.value.title = 'Title is required'
  return Object.keys(errors.value).length === 0
}

function handleSubmit() {
  if (!validate()) return
  emit('submit', {
    title: title.value,
    description: description.value || undefined,
    termId: termId.value ? Number(termId.value) : undefined,
    schoolId: schoolId.value ? Number(schoolId.value) : undefined,
    majorId: majorId.value ? Number(majorId.value) : undefined,
    classId: classId.value ? Number(classId.value) : undefined,
    courseId: courseId.value ? Number(courseId.value) : undefined,
  })
}
</script>

<template>
  <form class="plan-form" @submit.prevent="handleSubmit">
    <FormField label="Title" required :error="errors.title">
      <FormInput v-model="title" placeholder="e.g., Data Structures & Algorithms" />
    </FormField>

    <FormField label="Description">
      <FormTextarea v-model="description" placeholder="Describe the study plan..." :rows="3" />
    </FormField>

    <div class="plan-form__hierarchy">
      <FormField label="Term">
        <FormSelect v-model="termId" :options="toOptions(terms)" placeholder="Select term" />
      </FormField>

      <FormField label="School">
        <FormSelect v-model="schoolId" :options="toOptions(schools)" placeholder="Select school" />
      </FormField>

      <FormField label="Major">
        <FormSelect v-model="majorId" :options="toOptions(majors)" placeholder="Select major" />
      </FormField>

      <FormField label="Class">
        <FormSelect v-model="classId" :options="toOptions(classes)" placeholder="Select class" />
      </FormField>

      <FormField label="Course">
        <FormSelect v-model="courseId" :options="toOptions(courses)" placeholder="Select course" />
      </FormField>
    </div>

    <SubmitButton :loading="loading" text="Create Plan" loading-text="Creating..." :disabled="!isValid" />
  </form>
</template>

<style scoped>
.plan-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-form__hierarchy {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 640px) {
  .plan-form__hierarchy {
    grid-template-columns: 1fr;
  }
}
</style>
