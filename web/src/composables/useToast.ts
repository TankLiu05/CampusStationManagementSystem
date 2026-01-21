import { ref, readonly } from 'vue'

// Toast 类型定义
export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id: number
  message: string
  type: ToastType
  duration: number
}

// 全局 toast 状态
const toasts = ref<Toast[]>([])
let toastId = 0

// 添加 toast
const addToast = (message: string, type: ToastType = 'info', duration: number = 4000) => {
  const id = ++toastId
  const toast: Toast = { id, message, type, duration }
  toasts.value.push(toast)

  // 自动移除
  if (duration > 0) {
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }

  return id
}

// 移除 toast
const removeToast = (id: number) => {
  const index = toasts.value.findIndex((t) => t.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

// 快捷方法
const success = (message: string, duration?: number) => addToast(message, 'success', duration)
const error = (message: string, duration?: number) => addToast(message, 'error', duration)
const warning = (message: string, duration?: number) => addToast(message, 'warning', duration)
const info = (message: string, duration?: number) => addToast(message, 'info', duration)

export function useToast() {
  return {
    toasts: readonly(toasts),
    addToast,
    removeToast,
    success,
    error,
    warning,
    info,
  }
}

// 导出全局方法供非组件使用
export const toast = {
  success,
  error,
  warning,
  info,
  add: addToast,
  remove: removeToast,
}
