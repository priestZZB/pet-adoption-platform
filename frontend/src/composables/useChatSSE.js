/**
 * 聊天 SSE 实时推送 hook
 * 管理 EventSource 连接，提供事件监听和清理
 */
import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'

const BASE_URL = ''

export function useChatSSE() {
  const userStore = useUserStore()
  const eventSource = ref(null)
  const connected = ref(false)

  /**
   * 建立 SSE 连接
   * @param {Object} handlers - 事件处理器
   * @param {Function} handlers.onNewMessage - 新消息回调 (msg)
   * @param {Function} handlers.onUnreadCount - 未读更新回调 (count)
   * @param {Function} handlers.onConversationUpdate - 会话更新回调
   */
  function connect(handlers = {}) {
    if (!userStore.isLogin || !userStore.token) return
    disconnect()

    // EventSource 不支持自定义 headers，用 query param 传递 token
    const url = `${BASE_URL}/api/chat/sse/subscribe?token=${encodeURIComponent(userStore.token)}`
    const esWithToken = new EventSource(url)

    esWithToken.onopen = () => { connected.value = true }

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

    esWithToken.addEventListener('new-notification', () => {
      if (handlers.onNewNotification) handlers.onNewNotification()
    })

    esWithToken.addEventListener('online-status', (e) => {
      try {
        const data = JSON.parse(e.data)
        if (handlers.onOnlineStatus) handlers.onOnlineStatus(data.userId, data.online)
      } catch { /* ignore */ }
    })

    esWithToken.onerror = () => {
      connected.value = false
      // 自动重连
      setTimeout(() => {
        if (handlers.onReconnect !== false) connect(handlers)
      }, 3000)
    }

    eventSource.value = esWithToken
  }

  function disconnect() {
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
