<script setup lang="ts">
import FormFileUpload from '@/components/forms/FormFileUpload.vue'

const emit = defineEmits<{
  files: [files: File[]]
}>()

function onFiles(files: File[]) {
  emit('files', files)
}
</script>

<template>
  <div class="import-uploader">
    <h3 class="import-uploader__title">Upload File</h3>
    <p class="import-uploader__desc">
      Upload an Excel (.xlsx) or CSV (.csv) file to import master data records.
    </p>
    <FormFileUpload
      accept=".xlsx,.xls,.csv"
      @files="onFiles"
    />
    <div class="import-uploader__formats">
      <span class="format-tag">XLSX</span>
      <span class="format-tag">XLS</span>
      <span class="format-tag">CSV</span>
    </div>
  </div>
</template>

<style scoped>
.import-uploader__title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px;
}

.import-uploader__desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px;
}

.import-uploader__formats {
  display: flex;
  gap: 6px;
  margin-top: 12px;
  justify-content: center;
}

.format-tag {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 4px;
}
</style>
