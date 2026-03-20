import { reactive } from 'vue'

const state = reactive({
  visible: false,
  message: '',
  confirmText: '확인',
  cancelText: '취소',
  type: 'default', // 'default' | 'danger'
  resolve: null
})

export function useDialog() {
  function confirm(message, options = {}) {
    state.message = message
    state.confirmText = options.confirmText || '확인'
    state.cancelText = options.cancelText || '취소'
    state.type = options.type || 'default'
    state.visible = true

    return new Promise(res => {
      state.resolve = res
    })
  }

  function _confirm() {
    state.visible = false
    state.resolve?.(true)
  }

  function _cancel() {
    state.visible = false
    state.resolve?.(false)
  }

  return { state, confirm, _confirm, _cancel }
}
