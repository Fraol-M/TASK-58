import { ref, computed } from 'vue'

export function usePagination(initialPageSize: number = 10) {
  const page = ref(0)
  const pageSize = ref(initialPageSize)
  const totalElements = ref(0)

  const totalPages = computed(() =>
    Math.max(1, Math.ceil(totalElements.value / pageSize.value)),
  )

  function nextPage() {
    if (page.value < totalPages.value - 1) {
      page.value++
    }
  }

  function prevPage() {
    if (page.value > 0) {
      page.value--
    }
  }

  function goToPage(n: number) {
    if (n >= 0 && n < totalPages.value) {
      page.value = n
    }
  }

  function setTotal(n: number) {
    totalElements.value = n
  }

  return {
    page,
    pageSize,
    totalElements,
    totalPages,
    nextPage,
    prevPage,
    goToPage,
    setTotal,
  }
}
