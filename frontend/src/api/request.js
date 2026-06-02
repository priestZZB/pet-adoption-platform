import axios from 'axios'
import { getToken, removeToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
  paramsSerializer: {
    serialize(params) {
      const parts = []
      for (const [key, val] of Object.entries(params)) {
        if (Array.isArray(val)) {
          val.forEach(v => parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(v)))
        } else {
          parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(val))
        }
      }
      return parts.join('&')
    }
  }
})

// 请求拦截器 — 注入 Bearer Token
request.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 — 统一解析 + 错误处理
request.interceptors.response.use(
  response => {
    const { code, msg, data } = response.data
    if (code === 200) {
      return data   // 统一返回 data，业务代码直接取
    }
    if (code === 401 || code === 4031) {
      ElMessage.error(msg || '登录已过期')
      removeToken()
      const reason = encodeURIComponent(msg || '账号已被禁用')
      router.push('/login?reason=' + reason)
      return Promise.reject(new Error(msg))
    }
    // 管理员权限拦截（4032）：直接弹友好提示，不跳转
    if (code === 4032) {
      ElMessage.warning(msg || '管理员不能执行此操作')
      return Promise.reject(new Error(msg))
    }
    // 业务错误统一弹出提示
    //  - GET 请求（数据加载）不弹窗，避免后台刷新时干扰
    //  - 请求级配置 __silent: true 可强制不弹窗
    const method = (response.config?.method || '').toLowerCase()
    if (method !== 'get' && !response.config?.__silent) {
      ElMessage.error(msg || '操作失败')
    }
    return Promise.reject(new Error(msg))
  },
  error => {
    // HTTP 网络层错误处理
    const status = error.response?.status

    // 网络断连 / 超时 → 跳转网络错误页
    if (error.code === 'ERR_NETWORK' || error.message === 'Network Error' || error.code === 'ECONNABORTED') {
      router.push('/network-error')
      return Promise.reject(error)
    }

    // 403 无权限 → 跳转 403 页
    if (status === 403) {
      router.push('/403')
      return Promise.reject(error)
    }

    // 500 服务器错误 → 跳转 500 页
    if (status === 500) {
      router.push('/500')
      return Promise.reject(error)
    }

    // 503 维护中 → 跳转维护页
    if (status === 503) {
      router.push('/maintenance')
      return Promise.reject(error)
    }

    // 其他 HTTP 错误（404 等）不跳转，静默返回
    return Promise.reject(error)
  }
)

export default request
