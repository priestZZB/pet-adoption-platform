<template>
  <div class="chat-detail-page">
    <!-- 顶部：宠物信息精选 -->
    <div class="chat-header">
      <el-avatar :size="40" :src="petCover" class="header-avatar" style="border-radius:8px">{{ petName[0] }}</el-avatar>
      <div class="header-info">
        <div class="header-title">{{ petName }}</div>
        <div class="header-meta">{{ petCategory }} · {{ petAge }} · {{ petGender }}</div>
        <div class="header-sub">与 {{ otherName }} 的交流</div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div ref="msgContainer" class="msg-container">
      <div v-if="loading" class="loading-center"><el-icon class="is-loading" :size="24"><Loading /></el-icon></div>
      <template v-else>
        <div v-for="msg in messages" :key="msg.id" class="msg-group">
          <!-- 对方消息：左对齐 -->
          <div v-if="msg.senderId !== myId" class="msg-row msg-left">
            <el-avatar :size="32" :src="otherAvatar" class="msg-avatar">{{ otherName[0] }}</el-avatar>
            <div class="msg-body">
              <div class="msg-meta">
                <span class="msg-author">{{ otherName }}</span>
                <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div class="msg-bubble bubble-other">{{ msg.content }}</div>
            </div>
          </div>
          <!-- 我的消息：右对齐 -->
          <div v-else class="msg-row msg-right">
            <div class="msg-body msg-body-right">
              <div class="msg-meta msg-meta-right">
                <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
                <span class="msg-author">我</span>
              </div>
              <div class="msg-bubble bubble-self">{{ msg.content }}</div>
            </div>
            <el-avatar :size="32" :src="myAvatar" class="msg-avatar">{{ myName[0] }}</el-avatar>
          </div>
        </div>
      </template>
    </div>

    <!-- 输入 -->
    <div class="msg-input-bar">
      <el-input v-model="inputText" placeholder="输入消息..." :rows="2" type="textarea" resize="none" @keyup.enter.prevent="handleSend" />
      <el-button type="primary" :loading="sending" @click="handleSend" style="height:52px;border-radius:10px;flex-shrink:0;min-width:80px">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getConversation, sendMessage, markChatAsRead } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const myId = userStore.userInfo?.id
const myName = userStore.userInfo?.nickname || userStore.userInfo?.username || '我'
const myAvatar = userStore.userInfo?.avatar || ''

const petId = ref(route.query.petId)
const otherUserId = ref(parseInt(route.query.otherUserId))
const petName = ref(route.query.petName || '宠物')
const petCover = ref('')
const petCategory = ref('')
const petAge = ref('')
const petGender = ref('')
const otherName = ref(route.query.otherName || '用户')
const otherAvatar = ref('')
const messages = ref([])
const loading = ref(true)
const sending = ref(false)
const inputText = ref('')
const msgContainer = ref(null)

async function loadDetail() {
  loading.value = true
  try {
    // 加载对方信息
    try {
      const u = await request.get('/user/public/' + otherUserId.value)
      otherName.value = u?.nickname || u?.username || '用户'
      otherAvatar.value = u?.avatar || ''
    } catch {}
    // 加载宠物信息
    try {
      const p = await request.get('/pets/' + petId.value)
      petName.value = p?.name || petName.value
      petCover.value = p?.images?.[0] || p?.coverImage || ''
      petCategory.value = p?.categoryName || ''
      petAge.value = p?.age || ''
      petGender.value = (p?.gender === 'male' ? '公' : p?.gender === 'female' ? '母' : p?.gender) || ''
    } catch {}
    // 加载消息
    messages.value = await getConversation({ petId: petId.value, otherUserId: otherUserId.value })
    await nextTick()
    scrollToBottom()
  } catch { messages.value = [] }
  finally { loading.value = false }
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  sending.value = true
  try {
    await sendMessage({ receiverId: otherUserId.value, petId: petId.value, content: text })
    inputText.value = ''
    await loadDetail()
  } catch { ElMessage.error('发送失败') }
  finally { sending.value = false }
}

function scrollToBottom() {
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}

onMounted(async () => {
  await loadDetail()
  try { await markChatAsRead({ petId: petId.value, otherUserId: otherUserId.value }) } catch {}
})
</script>

<style scoped>
.chat-detail-page {
  max-width: 700px; margin: 0 auto;
  display: flex; flex-direction: column;
  height: calc(100vh - 120px);
}

/* 顶部宠物信息 */
.chat-header {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 0; flex-shrink: 0;
  border-bottom: 1px solid var(--yc-border);
  margin-bottom: 8px;
}
.header-back { flex-shrink: 0; }
.header-avatar { border-radius: 8px; flex-shrink: 0; }
.header-info { flex: 1; min-width: 0; }
.header-title { font-size: 16px; font-weight: 600; color: var(--yc-text-primary); }
.header-meta { font-size: 12px; color: var(--yc-text-secondary); margin-top: 1px; }
.header-sub { font-size: 11px; color: var(--yc-text-tertiary); margin-top: 1px; }

/* 消息列表 */
.msg-container {
  flex: 1; overflow-y: auto;
  padding: 8px 0; display: flex; flex-direction: column; gap: 16px;
}
.loading-center { display: flex; justify-content: center; padding: 40px 0; }

.msg-group { display: flex; flex-direction: column; }
.msg-row { display: flex; align-items: flex-end; gap: 8px; }
.msg-left { justify-content: flex-start; }
.msg-right { justify-content: flex-end; }

.msg-avatar { flex-shrink: 0; }
.msg-body { max-width: 65%; display: flex; flex-direction: column; gap: 2px; }
.msg-body-right { align-items: flex-end; }

.msg-meta { display: flex; align-items: center; gap: 6px; margin-bottom: 2px; }
.msg-meta-right { justify-content: flex-end; }
.msg-author { font-size: 11px; color: var(--yc-text-tertiary); font-weight: 500; }
.msg-time { font-size: 11px; color: var(--yc-text-tertiary); }

.msg-bubble {
  padding: 10px 14px; border-radius: 16px;
  font-size: 14px; line-height: 1.5; word-break: break-word;
}
.bubble-self {
  background: var(--yc-accent); color: #fff;
  border-bottom-right-radius: 4px;
}
.bubble-other {
  background: var(--yc-bg-card); color: var(--yc-text-primary);
  border: 1px solid var(--yc-border);
  border-bottom-left-radius: 4px;
}

/* 输入 */
.msg-input-bar {
  display: flex; gap: 10px; align-items: center;
  padding: 10px 0; flex-shrink: 0;
  border-top: 1px solid var(--yc-border);
  margin-top: 8px;
}
.msg-input-bar :deep(.el-textarea__inner) { border-radius: 10px; resize: none; }
</style>
