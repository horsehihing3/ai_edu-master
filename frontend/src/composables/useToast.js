import { useUiStore } from '@/store/ui'

export function useToast() {
  const uiStore = useUiStore()

  function showToast({ type = 'info', message, duration = 3000 }) {
    return uiStore.showToast({ type, message, duration })
  }

  function success(message, duration) {
    return showToast({ type: 'success', message, duration })
  }

  function error(message, duration) {
    return showToast({ type: 'error', message, duration })
  }

  function warning(message, duration) {
    return showToast({ type: 'warning', message, duration })
  }

  function info(message, duration) {
    return showToast({ type: 'info', message, duration })
  }

  return { showToast, success, error, warning, info }
}
