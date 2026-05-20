/**
 * 聊天 WebSocket STOMP 实时推送 hook
 * 支持：实时消息、未读数更新、打字提示、在线状态
 */
import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'

export function useChatWebSocket() {
  const userStore = useUserStore()
  const client = ref(null)
  const connected = ref(false)

  async function connect(handlers = {}) {
    if (!userStore.isLogin || !userStore.token) return
    disconnect()

    try {
      const [{ Client }, { default: SockJS }] = await Promise.all([
        import('@stomp/stompjs'),
        import('sockjs-client')
      ])

      const stompClient = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        connectHeaders: { Authorization: `Bearer ${userStore.token}` },
        debug: () => {},
        reconnectDelay: 3000,
        onConnect: () => {
          connected.value = true

          // 订阅: 新消息
          stompClient.subscribe('/user/queue/chat', (message) => {
            try {
              const msg = JSON.parse(message.body)
              if (handlers.onNewMessage) handlers.onNewMessage(msg)
            } catch { /* ignore */ }
          })

          // 订阅: 未读数
          stompClient.subscribe('/user/queue/unread', (message) => {
            try {
              const data = JSON.parse(message.body)
              if (handlers.onUnreadCount) handlers.onUnreadCount(data.count)
            } catch { /* ignore */ }
          })

          // 订阅: 对方正在输入
          stompClient.subscribe('/user/queue/typing', (message) => {
            try {
              const data = JSON.parse(message.body)
              if (handlers.onTyping) {
                handlers.onTyping(data.senderId)
              }
            } catch { /* ignore */ }
          })

          // 订阅: 在线状态更新
          stompClient.subscribe('/user/queue/online', (message) => {
            try {
              const data = JSON.parse(message.body)
              if (handlers.onOnlineStatus) {
                handlers.onOnlineStatus(data.userId, data.online)
              }
            } catch { /* ignore */ }
          })

          // 发送上线通知
          stompClient.publish({
            destination: '/app/chat/online',
            body: JSON.stringify({ userId: userStore.userInfo?.id })
          })
        },
        onDisconnect: () => {
          connected.value = false
          // 发送下线通知
          if (stompClient && stompClient.connected) {
            try {
              stompClient.publish({
                destination: '/app/chat/offline',
                body: JSON.stringify({ userId: userStore.userInfo?.id })
              })
            } catch { /* ignore */ }
          }
        },
        onStompError: () => { connected.value = false }
      })

      stompClient.activate()
      client.value = stompClient
    } catch (e) {
      console.warn('WebSocket 连接失败:', e)
      connected.value = false
    }
  }

  /** 发送打字中事件 */
  function sendTyping(receiverId) {
    if (!client.value || !client.value.connected) return
    try {
      client.value.publish({
        destination: '/app/chat/typing',
        body: JSON.stringify({
          senderId: userStore.userInfo?.id,
          receiverId
        })
      })
    } catch { /* ignore */ }
  }

  /** 查询对方是否在线 */
  function checkOnline(userId) {
    if (!client.value || !client.value.connected) return
    try {
      client.value.publish({
        destination: '/app/chat/check-online',
        body: JSON.stringify({
          userId: userStore.userInfo?.id,
          checkUserId: userId
        })
      })
    } catch { /* ignore */ }
  }

  function disconnect() {
    if (client.value) {
      client.value.deactivate()
      client.value = null
    }
    connected.value = false
  }

  onUnmounted(disconnect)

  return { connect, disconnect, connected, sendTyping, checkOnline }
}
