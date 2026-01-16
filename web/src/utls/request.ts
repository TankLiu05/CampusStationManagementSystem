/**
 * 统一请求拦截器
 * 所有 API 请求都通过此拦截器处理
 */

// 默认通过 Vite 代理走相对路径，生产环境可设置 VITE_API_BASE_URL
const API_BASE = (import.meta as any).env?.VITE_API_BASE_URL ?? ''

export interface RequestConfig extends RequestInit {
  params?: Record<string, any> // URL 查询参数
  skipErrorHandler?: boolean // 是否跳过全局错误处理
}

/**
 * 统一请求方法
 * @param url 请求地址
 * @param config 请求配置
 */
export async function request<T = any>(url: string, config?: RequestConfig): Promise<T> {
  const { params, skipErrorHandler, ...fetchConfig } = config || {}

  // 构建完整 URL
  let fullUrl = url.startsWith('http') ? url : `${API_BASE}${url}`

  // 处理 URL 查询参数
  if (params) {
    const searchParams = new URLSearchParams()
    Object.keys(params).forEach((key) => {
      const value = params[key]
      if (value !== undefined && value !== null) {
        searchParams.append(key, String(value))
      }
    })
    const queryString = searchParams.toString()
    if (queryString) {
      fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString
    }
  }

  try {
    const response = await fetch(fullUrl, {
      credentials: 'include', // 重要：携带 Cookie（Session）
      headers: {
        'Content-Type': 'application/json',
        ...fetchConfig.headers,
      },
      ...fetchConfig,
    })

    // 处理响应状态码
    if (!response.ok) {
      const errorText = await response.text()
      const error = new Error(errorText || `HTTP ${response.status}`)
      ;(error as any).status = response.status
      ;(error as any).response = response

      // 全局错误处理
      if (!skipErrorHandler) {
        handleError(response.status, errorText)
      }

      throw error
    }

    // 处理响应内容
    const contentType = response.headers.get('content-type') || ''
    if (!contentType.includes('application/json')) {
      // 非 JSON 响应（如删除接口返回空）
      return undefined as unknown as T
    }

    return response.json() as Promise<T>
  } catch (error) {
    // 网络错误或其他异常
    if (!skipErrorHandler && error instanceof Error) {
      console.error('请求失败:', error.message)
      // 可以在这里添加全局提示，如 toast/notification
    }
    throw error
  }
}

/**
 * 全局错误处理
 * @param status HTTP 状态码
 * @param message 错误信息
 */
function handleError(status: number, message: string) {
  switch (status) {
    case 401:
      console.warn('未登录或登录已过期:', message)
      // 可以在这里跳转到登录页
      // window.location.href = '/login'
      break
    case 403:
      console.warn('无权限访问:', message)
      break
    case 404:
      console.warn('资源不存在:', message)
      break
    case 409:
      console.warn('数据冲突:', message)
      break
    case 500:
      console.error('服务器错误:', message)
      break
    default:
      console.error(`请求错误 [${status}]:`, message)
  }
}


