import { ref } from 'vue'

export function useModal() {
  const isOpen = ref(false)
  const modalData = ref(null)

  function open(data = null) {
    modalData.value = data
    isOpen.value = true
  }

  function close() {
    isOpen.value = false
    modalData.value = null
  }

  return { isOpen, modalData, open, close }
}
