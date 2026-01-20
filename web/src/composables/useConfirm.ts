import { ref, readonly } from 'vue'

export interface ConfirmOptions {
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  type?: 'info' | 'warning' | 'danger'
}

interface ConfirmState {
  visible: boolean
  title: string
  message: string
  confirmText: string
  cancelText: string
  type: 'info' | 'warning' | 'danger'
  resolve: ((value: boolean) => void) | null
}

const state = ref<ConfirmState>({
  visible: false,
  title: '确认操作',
  message: '',
  confirmText: '确定',
  cancelText: '取消',
  type: 'info',
  resolve: null,
})

const show = (options: ConfirmOptions | string): Promise<boolean> => {
  return new Promise((resolve) => {
    const opts = typeof options === 'string' ? { message: options } : options

    state.value = {
      visible: true,
      title: opts.title || '确认操作',
      message: opts.message,
      confirmText: opts.confirmText || '确定',
      cancelText: opts.cancelText || '取消',
      type: opts.type || 'info',
      resolve,
    }
  })
}

const handleConfirm = () => {
  if (state.value.resolve) {
    state.value.resolve(true)
  }
  state.value.visible = false
  state.value.resolve = null
}

const handleCancel = () => {
  if (state.value.resolve) {
    state.value.resolve(false)
  }
  state.value.visible = false
  state.value.resolve = null
}

export function useConfirm() {
  return {
    state: readonly(state),
    show,
    confirm: show,
    handleConfirm,
    handleCancel,
  }
}

// 导出全局方法供非组件使用
export const confirm = show
