<template>
  <!-- 浮动按钮 -->
  <el-button
    type="primary"
    circle
    size="large"
    style="position:fixed;bottom:30px;right:30px;z-index:999"
    @click="openWidget"
  >
    <el-icon :size="20"><ChatDotRound /></el-icon>
  </el-button>

  <!-- 对话抽屉 -->
  <el-drawer
    v-model="visible"
    size="420px"
    direction="rtl"
    :close-on-press-escape="true"
  >
    <template #header>
      <span class="widget-drawer-title">AI 助手</span>
      <div class="widget-header-actions">
        <el-button type="primary" size="small" @click="handleNewChat">新对话</el-button>
        <el-button text size="small" :icon="FullScreen" @click="goFullPage" style="margin-left:4px;font-size:18px" />
      </div>
    </template>
    <div class="widget-body">
      <div class="widget-messages" ref="msgRef">
        <div
          v-for="(msg, i) in messages"
          :key="i"
          class="msg-row"
          :class="msg.role"
        >
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>

        <div v-if="waiting" class="msg-row assistant">
          <div class="msg-bubble thinking">
            <el-icon class="is-loading"><Loading /></el-icon> 思考中...
          </div>
        </div>
      </div>

      <div class="widget-input">
        <el-input
          ref="inputRef"
          v-model="question"
          placeholder="输入你的问题..."
          :disabled="waiting"
          @keyup.enter="sendMessage"
        >
          <template #append>
            <el-button
              type="primary"
              style="background:#409EFF;color:#fff;font-weight:600"
              :disabled="!question.trim() || waiting"
              @click="sendMessage"
            >
              发送
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ChatDotRound, Loading, FullScreen } from '@element-plus/icons-vue'
import { chat } from '@/api/ai'
import { useRouter } from 'vue-router'

const router = useRouter()

const visible = ref(false)
const inputRef = ref(null)
const question = ref('')
const waiting = ref(false)
const msgRef = ref(null)
const sessionId = ref('')

const messages = ref([
  { role: 'assistant', content: '你好！我是AI助手，有什么可以帮助你的？' }
])

function newSessionId() {
  return crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36) + Math.random().toString(36).slice(2)
}

function openWidget() {
  if (!sessionId.value) {
    sessionId.value = newSessionId()
  }
  visible.value = true
}

function handleNewChat() {
  sessionId.value = newSessionId()
  messages.value = [
    { role: 'assistant', content: '你好！我是AI助手，有什么可以帮助你的？' }
  ]
}

function goFullPage() {
  visible.value = false
  router.push('/ai')
}

function scrollToBottom() {
  nextTick(() => {
    if (msgRef.value) {
      msgRef.value.scrollTop = msgRef.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const q = question.value.trim()
  if (!q || waiting.value) return

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  waiting.value = true
  scrollToBottom()

  try {
    const res = await chat({ question: q, sessionId: sessionId.value })
    messages.value.push({ role: 'assistant', content: res.answer })
  } catch {
    messages.value.push({ role: 'assistant', content: 'AI服务暂时不可用，请稍后再试。' })
  } finally {
    waiting.value = false
    scrollToBottom()
    nextTick(() => inputRef.value?.focus())
  }
}
</script>

<style scoped>
.widget-body {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
}
.widget-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
}
.msg-row {
  display: flex;
  margin-bottom: 12px;
}
.msg-row.user {
  justify-content: flex-end;
}
.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}
.msg-row.assistant .msg-bubble {
  background: #f0f2f5;
  color: #303133;
  border-bottom-left-radius: 4px;
}
.msg-row.user .msg-bubble {
  background: #409EFF;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.thinking {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399 !important;
}
.widget-input {
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.widget-header-actions {
  display: flex;
  align-items: center;
}

.widget-drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
