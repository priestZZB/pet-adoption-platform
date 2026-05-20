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
    ElMessage.error(msg || '操作失败')
    return Promise.reject(new Error(msg))
  },
  error => {
    ElMessage.error('网络错误，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
