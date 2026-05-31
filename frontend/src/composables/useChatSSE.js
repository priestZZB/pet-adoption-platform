/**
 * 聊天 SSE 实时推送 hook
 * 管理 EventSource 连接，提供事件监听和清理
 */
import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'

const BASE_URL = ''

// SSE 重连最大次数和基础间隔
const MAX_RETRIES = 50
const BASE_DELAY = 3000

export function useChatSSE() {
  const userStore = useUserStore()
  const eventSource = ref(null)
  const connected = ref(false)
  let retryCount = 0
  let retryTimer = null

  /**
   * 建立 SSE 连接
   * @param {Object} handlers - 事件处理器
   * @param {Function} handlers.onNewMessage - 新消息回调 (msg)
   * @param {Function} handlers.onUnreadCount - 未读更新回调 (count)
   * @param {Function} handlers.onConversationUpdate - 会话更新回调
   * @param {Function|boolean} handlers.onReconnect - false 禁止重连，或函数回调
   */
  function connect(handlers = {}) {
    if (!userStore.isLogin || !userStore.token) return
    disconnect()

    // EventSource 不支持自定义 headers，用 query param 传递 token
    const url = `${BASE_URL}/api/chat/sse/subscribe?token=${encodeURIComponent(userStore.token)}`
    const esWithToken = new EventSource(url)

    esWithToken.onopen = () => {
      connected.value = true
      retryCount = 0  // 连接成功后重置重试计数
    }

    esWithToken.addEventListener('new-message', (e) => {
      try {
        const msg = JSON.parse(e.data)
        if (handlers.onNewMessage) handlers.onNewMessage(msg)
      } catch { /* ignore parse errors */ }
    })

    esWithToken.addEventListener('unread-count', (e) => {
      try {
        const data = JSON.parse(e.data)
        if (handlers.onUnreadCount) handlers.onUnreadCount(data.count)
      } catch { /* ignore */ }
    })

    esWithToken.addEventListener('conversation-update', () => {
      if (handlers.onConversationUpdate) handlers.onConversationUpdate()
    })

    esWithToken.addEventListener('new-notification', (e) => {
      try {
        const data = JSON.parse(e.data)
        if (handlers.onNewNotification) handlers.onNewNotification(data)
      } catch {
        if (handlers.onNewNotification) handlers.onNewNotification()
      }
    })

    esWithToken.addEventListener('online-status', (e) => {
      try {
        const data = JSON.parse(e.data)
        if (handlers.onOnlineStatus) handlers.onOnlineStatus(data.userId, data.online)
      } catch { /* ignore */ }
    })

    esWithToken.onerror = () => {
      connected.value = false
      retryCount++
      // 调用者可通过 onReconnect === false 禁用自动重连
      if (handlers.onReconnect === false) return
      // 超过最大重试次数后停止
      if (retryCount > MAX_RETRIES) return
      // 指数退避：3s → 4.5s → 6.75s → ... 最大60s
      const delay = Math.min(BASE_DELAY * Math.pow(1.5, Math.min(retryCount - 1, 10)), 60000)
      retryTimer = setTimeout(() => connect(handlers), delay)
    }

    eventSource.value = esWithToken
  }

  function disconnect() {
    if (retryTimer) {
      clearTimeout(retryTimer)
      retryTimer = null
    }
    retryCount = 0
    if (eventSource.value) {
      eventSource.value.close()
      eventSource.value = null
    }
    connected.value = false
  }

  // 组件卸载时自动断开
  onUnmounted(disconnect)

  return { connect, disconnect, connected }
}
