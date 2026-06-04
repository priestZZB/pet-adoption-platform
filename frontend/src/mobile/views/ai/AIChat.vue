<template>
  <div class="page">
    <!-- 会话侧边栏（抽屉） -->
    <div v-if="sessionOpen" class="overlay" @click="sessionOpen = false" />
    <div class="session-drawer" :class="{ open: sessionOpen }">
      <div class="drawer-header">
        <span>对话记录</span>
        <button class="new-btn" @click="startNewSession">新对话</button>
      </div>
      <div class="session-list">
        <div v-for="s in sessions" :key="s.id" :class="['s-item', { active: s.id === currentSessionId }]" @click="selectSession(s); sessionOpen = false">
          <span class="s-title">{{ s.title || '新对话' }}</span>
          <span class="s-time">{{ s.updatedAt?.slice(0, 10) }}</span>
        </div>
      </div>
    </div>

    <!-- 消息区 -->
    <div class="msg-area" ref="msgRef">
      <div v-if="messages.length === 0" class="empty-chat">
        <i class="fas fa-robot"></i>
        <p>我是 AI 助手，有什么可以帮你的？</p>
      </div>
      <template v-for="(m, idx) in messages" :key="idx">
        <div class="msg-item" :class="{ user: m.role === 'user' }">
          <div class="msg-bubble">{{ m.content }}</div>
        </div>
        <div v-if="m.role === 'assistant' && idx < messages.length - 1" class="msg-divider"></div>
      </template>
      <div v-if="thinking" class="msg-item">
        <div class="msg-bubble thinking"><i class="fas fa-spinner fa-pulse"></i> 思考中...</div>
      </div>
    </div>

    <!-- 输入栏 -->
    <div class="input-bar">
      <button class="history-btn" @click="sessionOpen = true"><i class="fas fa-history"></i></button>
      <input v-model="input" class="msg-input" placeholder="问点什么..." @keyup.enter="handleSend" />
      <button class="send-btn" :disabled="!input.trim() || thinking" @click="handleSend">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chat, getSessions, getSessionMessages, deleteSession } from '@/api/ai'

const sessions = ref([])
const messages = ref([])
const input = ref('')
const thinking = ref(false)
const sessionOpen = ref(false)
const currentSessionId = ref(null)
const msgRef = ref(null)

onMounted(async () => {
  try { sessions.value = await getSessions() } catch {}
  if (sessions.value.length > 0) selectSession(sessions.value[0])
})

async function selectSession(s) {
  currentSessionId.value = s.id
  try { messages.value = await getSessionMessages(s.id) } catch {}
  scrollBottom()
}

function startNewSession() {
  messages.value = []
  currentSessionId.value = null
  sessionOpen.value = false
}

async function handleSend() {
  if (!input.value.trim() || thinking.value) return
  const content = input.value.trim()
  input.value = ''
  messages.value.push({ role: 'user', content })
  thinking.value = true
  scrollBottom()
  try {
    const res = await chat({ message: content, sessionId: currentSessionId.value })
    messages.value.push({ role: 'assistant', content: res.reply || res.content || res })
    if (!currentSessionId.value && res.sessionId) {
      currentSessionId.value = res.sessionId
      sessions.value = await getSessions()
    }
  } catch {} finally { thinking.value = false; scrollBottom() }
}

function scrollBottom() {
  nextTick(() => {
    if (msgRef.value) msgRef.value.scrollTop = msgRef.value.scrollHeight
  })
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: calc(100vh - 104px); height: calc(100dvh - 104px); background: #f7f1e5; }
/* 会话抽屉 */
.overlay { position: fixed; inset: 0; z-index: 1000; background: rgba(0,0,0,0.4); }
.session-drawer { position: fixed; top: 0; left: 0; bottom: 0; z-index: 1001; width: 280px; background: #fefaf5; transform: translateX(-100%); transition: transform 0.25s; display: flex; flex-direction: column; }
.session-drawer.open { transform: translateX(0); }
.drawer-header { display: flex; align-items: center; justify-content: space-between; padding: 16px; border-bottom: 1px solid #ece4d8; font-size: 15px; font-weight: 600; color: #5a4a42; }
.new-btn { height: 32px; padding: 0 12px; border: 1px solid #8ab8a0; border-radius: 8px; background: none; color: #8ab8a0; font-size: 13px; cursor: pointer; }
.session-list { flex: 1; overflow-y: auto; padding: 8px; }
.s-item { display: flex; justify-content: space-between; align-items: center; padding: 12px; border-radius: 10px; cursor: pointer; margin-bottom: 2px; }
.s-item.active { background: rgba(139,184,160,0.08); }
.s-title { font-size: 14px; color: #5a4a42; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.s-time { font-size: 11px; color: #b5a898; flex-shrink: 0; }

/* 消息区 */
.msg-area { flex: 1; overflow-y: auto; padding: 16px; }
.empty-chat { display: flex; flex-direction: column; align-items: center; padding: 48px 0; color: #a09080; }
.empty-chat i { font-size: 48px; margin-bottom: 12px; }
.empty-chat p { font-size: 14px; }
.msg-item { margin-bottom: 12px; }
.msg-item.user { display: flex; justify-content: flex-end; }
.msg-bubble { max-width: 80%; padding: 10px 14px; border-radius: 14px; font-size: 15px; line-height: 1.5; word-break: break-word; }
.msg-item:not(.user) .msg-bubble { background: #fefaf5; border: 1px solid #ece4d8; color: #5a4a42; }
.msg-item.user .msg-bubble { background: #8ab8a0; color: #fff; }
.msg-bubble.thinking { color: #a09080; font-size: 14px; }
.msg-divider { height: 1px; background: #f5f0e8; margin: 8px 0; }

/* 输入栏 */
.input-bar { display: flex; gap: 8px; padding: 10px 12px; padding-bottom: calc(10px + env(safe-area-inset-bottom, 0)); background: rgba(254,250,245,0.97); border-top: 1px solid #ece4d8; align-items: center; }
.history-btn { width: 40px; height: 40px; border: none; border-radius: 20px; background: none; color: #a09080; font-size: 18px; cursor: pointer; flex-shrink: 0; }
.msg-input { flex: 1; height: 40px; padding: 0 14px; border: 1px solid #d1e7dd; border-radius: 20px; background: #fefaf5; font-size: 15px; color: #5a4a42; outline: none; }
.msg-input:focus { border-color: #8ab8a0; }
.send-btn { height: 40px; padding: 0 18px; border: none; border-radius: 20px; background: #8ab8a0; color: #fff; font-size: 14px; font-weight: 500; cursor: pointer; flex-shrink: 0; }
.send-btn:disabled { opacity: 0.4; }
</style>
