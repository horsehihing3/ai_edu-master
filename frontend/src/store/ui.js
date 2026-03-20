import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const isLoading = ref(false)
  const toasts = ref([])
  const sidebarOpen = ref(false)
  let toastId = 0

  function showToast({ type = 'info', message, duration = 3000 }) {
    const id = ++toastId
    toasts.value.push({ id, type, message, duration })
    setTimeout(() => hideToast(id), duration)
    return id
  }

  function hideToast(id) {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  function setLoading(val) {
    isLoading.value = val
  }

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function closeSidebar() {
    sidebarOpen.value = false
  }

  return {
    isLoading,
    toasts,
    sidebarOpen,
    showToast,
    hideToast,
    setLoading,
    toggleSidebar,
    closeSidebar
  }
})
