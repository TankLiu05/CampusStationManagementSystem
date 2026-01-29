import { onMounted, onUnmounted, onActivated, onDeactivated } from 'vue'

/**
 * 页面自动刷新功能
 * @param loadDataFn 数据加载函数
 * @param interval 刷新间隔(毫秒),默认5秒
 */
export function useAutoRefresh(loadDataFn: () => void | Promise<void>, interval: number = 5000) {
  let refreshTimer: number | null = null

  const startRefresh = () => {
    // 先清除已存在的定时器
    if (refreshTimer) {
      clearInterval(refreshTimer)
    }
    // 设置新的定时器
    refreshTimer = window.setInterval(() => {
      loadDataFn()
    }, interval)
  }

  const stopRefresh = () => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }

  // 页面首次加载时启动
  onMounted(() => {
    loadDataFn()
    startRefresh()
  })

  // 页面被keep-alive激活时启动
  onActivated(() => {
    loadDataFn()
    startRefresh()
  })

  // 页面被keep-alive停用时停止
  onDeactivated(() => {
    stopRefresh()
  })

  // 页面卸载时停止
  onUnmounted(() => {
    stopRefresh()
  })

  return {
    startRefresh,
    stopRefresh
  }
}
