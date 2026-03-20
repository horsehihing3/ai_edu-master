import { ref } from 'vue'
import { useToast } from './useToast'

export function useApi() {
  const loading = ref(false)
  const error = ref(null)
  const { showToast } = useToast()

  async function request(apiFn, options = {}) {
    const {
      onSuccess,
      onError,
      successMessage,
      showError = true
    } = options

    loading.value = true
    error.value = null

    try {
      const result = await apiFn()
      if (successMessage) {
        showToast({ type: 'success', message: successMessage })
      }
      if (onSuccess) onSuccess(result)
      return result
    } catch (e) {
      error.value = e.message || '오류가 발생했습니다.'
      if (showError) {
        showToast({ type: 'error', message: error.value })
      }
      if (onError) onError(e)
      throw e
    } finally {
      loading.value = false
    }
  }

  return { loading, error, request }
}
