<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  accept?: string
  maxSize?: number // bytes
  multiple?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  accept: '*',
  maxSize: 10 * 1024 * 1024, // 10 MB
  multiple: false,
})

const emit = defineEmits<{
  files: [files: File[]]
}>()

const isDragging = ref(false)
const errorMsg = ref('')
const selectedFiles = ref<File[]>([])

function validateAndEmit(fileList: FileList | null) {
  errorMsg.value = ''
  if (!fileList || fileList.length === 0) return

  const files = Array.from(fileList)
  const oversized = files.filter((f) => f.size > props.maxSize)

  if (oversized.length > 0) {
    const maxMB = (props.maxSize / (1024 * 1024)).toFixed(1)
    errorMsg.value = `File(s) exceed maximum size of ${maxMB} MB`
    return
  }

  if (props.accept !== '*') {
    const acceptedTypes = props.accept.split(',').map((t) => t.trim())
    const invalid = files.filter((f) => {
      return !acceptedTypes.some((type) => {
        if (type.startsWith('.')) return f.name.toLowerCase().endsWith(type.toLowerCase())
        if (type.endsWith('/*')) return f.type.startsWith(type.replace('/*', '/'))
        return f.type === type
      })
    })
    if (invalid.length > 0) {
      errorMsg.value = `Invalid file type. Accepted: ${props.accept}`
      return
    }
  }

  selectedFiles.value = files
  emit('files', files)
}

function onDrop(e: DragEvent) {
  isDragging.value = false
  validateAndEmit(e.dataTransfer?.files ?? null)
}

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  validateAndEmit(input.files)
}
</script>

<template>
  <div
    :class="['file-upload', { 'file-upload--dragging': isDragging }]"
    @dragover.prevent="isDragging = true"
    @dragleave="isDragging = false"
    @drop.prevent="onDrop"
  >
    <input
      type="file"
      :accept="accept"
      :multiple="multiple"
      class="file-upload__input"
      @change="onFileChange"
    />
    <div class="file-upload__content">
      <svg class="file-upload__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M12 16V4m0 0L8 8m4-4l4 4M4 17v2a2 2 0 002 2h12a2 2 0 002-2v-2" />
      </svg>
      <p v-if="selectedFiles.length === 0" class="file-upload__text">
        Drag and drop files here, or <span class="file-upload__link">browse</span>
      </p>
      <p v-else class="file-upload__text">
        {{ selectedFiles.map((f) => f.name).join(', ') }}
      </p>
    </div>
    <p v-if="errorMsg" class="file-upload__error">{{ errorMsg }}</p>
  </div>
</template>

<style scoped>
.file-upload {
  position: relative;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.15s, background-color 0.15s;
}

.file-upload:hover,
.file-upload--dragging {
  border-color: #4f46e5;
  background-color: #eef2ff;
}

.file-upload__input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.file-upload__content {
  pointer-events: none;
}

.file-upload__icon {
  width: 32px;
  height: 32px;
  color: #9ca3af;
  margin: 0 auto 8px;
  display: block;
}

.file-upload__text {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.file-upload__link {
  color: #4f46e5;
  font-weight: 500;
}

.file-upload__error {
  font-size: 13px;
  color: #dc2626;
  margin: 8px 0 0;
}
</style>
