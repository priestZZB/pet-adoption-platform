<template>
  <!-- 可拖拽浮动按钮 -->
  <div
    class="float-wrapper"
    :style="floatStyle"
    @mousedown.prevent="startDrag"
    @touchstart.prevent="startDrag"
  >
    <el-button
      class="ai-float-btn"
      circle
      size="large"
      @click="openWidget"
    >
      <el-icon :size="20"><ChatDotRound /></el-icon>
    </el-button>
  </div>

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
          <el-avatar
            v-if="msg.role === 'assistant'"
            :size="28"
            src="/images/ai-avatar.jpg"
            class="widget-avatar"
          />
          <div class="msg-bubble" @mouseenter="hoveredMsg = i" @mouseleave="hoveredMsg = null">
            <div class="msg-content" v-html="renderMarkdown(msg.content)"></div>
            <button
              v-if="msg.role === 'assistant' && hoveredMsg === i"
              class="w-copy-btn"
              title="复制"
              @click="copyText(msg.content)"
            >
              <i class="fas fa-copy"></i>
            </button>
          </div>
          <el-avatar
            v-if="msg.role === 'user'"
            :size="28"
            :src="userStore.userInfo?.avatar || ''"
            class="widget-avatar"
          >
            {{ (userStore.userInfo?.nickname?.[0] || 'U').toUpperCase() }}
          </el-avatar>
        </div>

        <div v-if="waiting" class="msg-row assistant">
          <el-avatar :size="28" src="/images/ai-avatar.jpg" class="widget-avatar" />
          <div class="msg-bubble thinking">
            <el-icon class="is-loading"><Loading /></el-icon> 思考中...
          </div>
        </div>
      </div>

      <div class="widget-input">
        <div class="widget-input-row">
          <el-input
            ref="inputRef"
            v-model="question"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 3 }"
            placeholder="输入你的问题... (Enter发送)"
            :disabled="waiting"
            class="widget-input-field"
            @keydown="handleWidgetKeydown"
          />
          <el-button
            class="widget-send-btn"
            :disabled="!question.trim() || waiting"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ChatDotRound, Loading, FullScreen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { chat } from '@/api/ai'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { marked } from 'marked'

const router = useRouter()
const userStore = useUserStore()

// === 拖拽逻辑 ===
const BTN_SIZE = 40

const isDragging = ref(false)
const didDrag = ref(false)

const pos = reactive({
  x: window.innerWidth - 60,
  y: window.innerHeight - 90
})

const dragStart = reactive({ x: 0, y: 0, origX: 0, origY: 0 })

const floatStyle = computed(() => ({
  position: 'fixed',
  left: pos.x + 'px',
  top: pos.y + 'px',
  zIndex: 999,
  cursor: isDragging.value ? 'grabbing' : 'grab',
  transition: isDragging.value ? 'none' : 'left 0.25s ease'
}))

function startDrag(e) {
  isDragging.value = true
  didDrag.value = false
  const clientX = e.clientX ?? e.touches?.[0]?.clientX ?? 0
  const clientY = e.clientY ?? e.touches?.[0]?.clientY ?? 0
  dragStart.x = clientX
  dragStart.y = clientY
  dragStart.origX = pos.x
  dragStart.origY = pos.y
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', onDrag, { passive: false })
  document.addEventListener('touchend', endDrag)
}

function onDrag(e) {
  if (!isDragging.value) return
  e.preventDefault()
  const clientX = e.clientX ?? e.touches?.[0]?.clientX ?? 0
  const clientY = e.clientY ?? e.touches?.[0]?.clientY ?? 0
  const dx = clientX - dragStart.x
  const dy = clientY - dragStart.y
  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
    didDrag.value = true
  }
  pos.x = dragStart.origX + dx
  pos.y = dragStart.origY + dy
}

function endDrag() {
  if (!isDragging.value) return
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)

  // 松手后吸附到最近的左右边缘
  snapToEdge()
}

function snapToEdge() {
  const MARGIN = 20
  const right = window.innerWidth - pos.x - BTN_SIZE
  const left = pos.x
  if (right < left) {
    // 离右边近 → 吸附到右侧，距右边缘 MARGIN px
    pos.x = window.innerWidth - BTN_SIZE - MARGIN
  } else {
    // 离左边近 → 吸附到左侧，距左边缘 MARGIN px
    pos.x = MARGIN
  }
}

// === AI 对话逻辑 ===
const visible = ref(false)
const inputRef = ref(null)
const question = ref('')
const waiting = ref(false)
const msgRef = ref(null)
const sessionId = ref('')
const hoveredMsg = ref(null)

function renderMarkdown(text) {
  if (!text) return ''
  let t = text
    .replace(/\r\n/g, '\n').replace(/\r/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
  t = t.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  t = t.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  t = t.replace(/`([^`]+)`/g, '<code>$1</code>')
  t = t.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  t = t.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
  t = t.replace(/^&gt; (.+)$/gm, '<blockquote>$1</blockquote>')
  t = t.replace(/\n\n/g, '\x00')
  t = t.replace(/\n/g, '<br>')
  t = t.replace(/\x00/g, '<br><br>')
  return t
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

function handleWidgetKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const messages = ref([
  { role: 'assistant', content: '你好呀！我是小宠，有宠平台的AI助手 🐾\n\n有什么需要帮忙的吗？' }
])

function newSessionId() {
  return crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36) + Math.random().toString(36).slice(2)
}

function openWidget() {
  if (didDrag.value) return
  if (!sessionId.value) {
    sessionId.value = newSessionId()
  }
  visible.value = true
}

function handleNewChat() {
  sessionId.value = newSessionId()
  messages.value = [
    { role: 'assistant', content: '你好呀！我是小宠，有宠平台的AI助手 🐾\n\n有什么需要帮忙的吗？' }
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
.float-wrapper {
  position: fixed;
  z-index: 999;
  cursor: grab;
}

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
  gap: 8px;
  align-items: flex-start;
}
.msg-row.user {
  justify-content: flex-end;
}
.widget-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}
.msg-bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}
.msg-row.assistant .msg-bubble {
  background: #f0f4f8;
  color: #1a2332;
  border-bottom-left-radius: 4px;
}
.msg-row.user .msg-bubble {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(59,130,246,0.2);
}
.thinking {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399 !important;
}
.widget-input {
  padding: 12px 0 0;
  border-top: 1px solid #d0d9e8;
}

.widget-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.widget-input-field {
  flex: 1;
}

:deep(.widget-send-btn) {
  background: #3B82F6 !important;
  border: 1px solid #3B82F6 !important;
  color: #fff !important;
  font-weight: 600;
  height: 36px;
  padding: 0 18px;
  border-radius: 8px;
  flex-shrink: 0;
}
:deep(.widget-send-btn:hover) {
  background: #2563EB !important;
  border-color: #2563EB !important;
  color: #fff !important;
}

/* 复制按钮 */
.msg-bubble {
  position: relative;
}
.w-copy-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(255,255,255,0.9);
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
  transition: all 0.15s;
}
.w-copy-btn:hover {
  background: #fff;
  color: #3B82F6;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}



/* 输入框多行 */
.widget-input-field :deep(.el-textarea__inner) {
  border: 1px solid #d0d9e8;
  border-radius: 8px;
  box-shadow: none;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
}
.widget-input-field :deep(.el-textarea__inner:focus) {
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
}

.widget-header-actions {
  display: flex;
  align-items: center;
}

.widget-drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a2332;
}

/* 浮动按钮蓝色 */
:deep(.ai-float-btn) {
  background: #3B82F6;
  border: none;
  color: #fff;
  box-shadow: 0 4px 16px rgba(59,130,246,0.3);
}
:deep(.ai-float-btn:hover) {
  background: #2563EB;
  box-shadow: 0 6px 20px rgba(59,130,246,0.4);
  color: #fff;
}
:deep(.el-drawer) {
  border-radius: 12px 0 0 12px;
}
:deep(.el-drawer__header) {
  background: #f8fafc;
  margin: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #d0d9e8;
}
</style>

<style>
/* v-html 渲染的 Markdown 样式（非 scoped 确保生效） */
.msg-content {
  line-height: 1.6;
}
.msg-content strong {
  font-weight: 700;
}
.msg-content code {
  background: rgba(0,0,0,0.06);
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 12px;
  font-family: 'Consolas', monospace;
}
.msg-content a {
  color: #3B82F6;
  text-decoration: underline;
}
</style>
