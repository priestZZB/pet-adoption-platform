<template>
  <div class="page">
    <div class="msg-list" ref="msgListRef">
      <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
      <div v-for="(m, idx) in messages" :key="idx" class="msg-item" :class="{ mine: isMine(m) }">
        <div class="msg-bubble">{{ m.content || m.message }}</div>
        <div class="msg-time">{{ m.createdAt?.slice(11, 16) || '' }}</div>
      </div>
      <div ref="bottomRef"></div>
    </div>

    <div class="input-bar">
      <input v-model="text" class="msg-input" placeholder="输入消息..." @keyup.enter="handleSend" />
      <button class="send-btn" :disabled="!text.trim()" @click="handleSend">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getConversation, sendMessage } from '@/api/chat'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const messages = ref([]); const text = ref(''); const loading = ref(true)
const bottomRef = ref(null)

const targetId = route.query.targetId
const currentUserId = ref(null)

function isMine(m) { return (m.senderId || m.fromUserId) === currentUserId.value }

async function loadMessages() {
  loading.value = true
  try {
    currentUserId.value = userStore.userInfo?.id
    const res = await getConversation({ targetId, page: 1, size: 50 })
    messages.value = (res.records || res || []).reverse()
  } catch {} finally { loading.value = false; scrollBottom() }
}

function scrollBottom() {
  nextTick(() => { bottomRef.value?.scrollIntoView({ behavior: 'smooth' }) })
}

async function handleSend() {
  if (!text.value.trim()) return
  try {
    await sendMessage({ targetId, message: text.value })
    messages.value.push({ content: text.value, senderId: currentUserId.value, createdAt: new Date().toISOString() })
    text.value = ''
    scrollBottom()
  } catch {}
}

onMounted(loadMessages)
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: calc(100vh - 104px); height: calc(100dvh - 104px); }
.msg-list { flex: 1; overflow-y: auto; padding: 12px 16px; }
.loading-wrap { display: flex; justify-content: center; padding: 40px 0; font-size: 20px; color: #d1e7dd; }
.msg-item { margin-bottom: 14px; display: flex; flex-direction: column; align-items: flex-start; }
.msg-item.mine { align-items: flex-end; }
.msg-bubble { max-width: 75%; padding: 10px 14px; border-radius: 16px; font-size: 15px; line-height: 1.5; word-break: break-word; }
.msg-item:not(.mine) .msg-bubble { background: #fefaf5; border: 1px solid #ece4d8; color: #5a4a42; border-bottom-left-radius: 4px; }
.msg-item.mine .msg-bubble { background: #8ab8a0; color: #fff; border-bottom-right-radius: 4px; }
.msg-time { font-size: 11px; color: #b5a898; margin-top: 2px; }
.input-bar { display: flex; gap: 8px; padding: 10px 16px; padding-bottom: calc(10px + env(safe-area-inset-bottom, 0)); border-top: 1px solid #ece4d8; background: rgba(254,250,245,0.97); }
.msg-input { flex: 1; height: 42px; padding: 0 14px; border: 1px solid #d1e7dd; border-radius: 21px; background: #fefaf5; font-size: 15px; color: #5a4a42; outline: none; }
.msg-input:focus { border-color: #8ab8a0; }
.send-btn { height: 42px; padding: 0 18px; border: none; border-radius: 21px; background: #8ab8a0; color: #fff; font-size: 14px; font-weight: 500; cursor: pointer; flex-shrink: 0; }
.send-btn:disabled { opacity: 0.4; }
</style>
