<template>
  <div class="chat-layout">
    <!-- 左侧：会话列表 -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <span>💬 消息</span>
      </div>
      <div v-if="convLoading" class="sidebar-loading"><el-icon class="is-loading" :size="20"><Loading /></el-icon></div>
      <div v-else-if="conversations.length === 0" class="sidebar-empty">暂无消息</div>
      <div v-else class="sidebar-list">
        <div
          v-for="item in conversations"
          :key="item.convId || item.id"
          class="conv-item"
          :class="{ 'conv-active': item.petId === activePetId && item.otherId === activeOtherId }"
          @click="switchConversation(item)"
        >
          <el-avatar :size="40" :src="item.otherAvatar" class="conv-avatar">{{ getOtherName(item)[0] }}</el-avatar>
          <div class="conv-info">
            <div class="conv-name">{{ getOtherName(item) }}</div>
            <div class="conv-pet">关于：{{ item.petName }}</div>
            <div class="conv-preview">{{ item.content }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：聊天详情 -->
    <div v-if="!activePetId" class="chat-placeholder">
      <div class="placeholder-content">
        <el-icon :size="48" color="var(--yc-text-tertiary)"><ChatDotSquare /></el-icon>
        <p>选择一个会话开始聊天</p>
      </div>
    </div>
    <div v-else class="chat-detail-page">
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
      <div ref="msgContainer" class="msg-container" @scroll="onScroll">
        <div v-if="msgLoading" class="loading-center"><el-icon class="is-loading" :size="24"><Loading /></el-icon></div>
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
                <div v-if="msg.imageUrl" class="msg-bubble bubble-other" style="padding:4px">
                  <el-image :src="msg.imageUrl" fit="cover" style="max-width:200px;max-height:200px;border-radius:12px;cursor:pointer;display:block" :preview-src-list="[msg.imageUrl]" preview-teleported />
                </div>
                <div v-if="msg.content" class="msg-bubble bubble-other">{{ msg.content }}</div>
              </div>
            </div>
            <!-- 我的消息：右对齐 -->
            <div v-else class="msg-row msg-right">
              <div class="msg-body msg-body-right">
                <div class="msg-meta msg-meta-right">
                  <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
                  <span class="msg-author">我</span>
                </div>
                <div v-if="msg.imageUrl" class="msg-bubble bubble-self" style="padding:4px">
                  <el-image :src="msg.imageUrl" fit="cover" style="max-width:200px;max-height:200px;border-radius:12px;cursor:pointer;display:block" :preview-src-list="[msg.imageUrl]" preview-teleported />
                </div>
                <div v-if="msg.content" class="msg-bubble bubble-self">{{ msg.content }}</div>
              </div>
              <el-avatar :size="32" :src="myAvatar" class="msg-avatar">{{ myName[0] }}</el-avatar>
            </div>
          </div>
        </template>
      </div>

      <!-- 待发送图片预览区 -->
      <div v-if="pendingImage" class="pending-preview">
        <div class="pending-item">
          <el-image
            :src="pendingImage"
            fit="cover"
            class="pending-thumb"
            @click="previewPendingVisible = true"
          />
          <div class="pending-overlay" @click="pendingImage = null">
            <el-icon class="pending-close"><Close /></el-icon>
          </div>
        </div>
      </div>

      <!-- 输入 -->
      <div class="msg-input-bar">
        <div class="input-area">
          <!-- 回到底部按钮 - 输入框正上方居中 -->
          <transition name="arrow-fade">
            <div v-if="showScrollBtn" class="scroll-bottom-btn" @click="scrollToBottom">
              <el-icon><ArrowDown /></el-icon>
            </div>
          </transition>
          <el-input v-model="inputText" placeholder="输入消息..." :rows="2" type="textarea" resize="none" @keyup.enter.prevent="handleSend" />
          <div class="input-toolbar">
            <el-upload
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
              accept="image/*"
              class="img-upload-btn"
              :disabled="uploading"
            >
              <el-button class="upload-img-btn" size="small" :icon="Picture" :loading="uploading" />
            </el-upload>
          </div>
        </div>
        <el-button type="primary" :loading="sending" @click="handleSend" style="height:52px;border-radius:10px;flex-shrink:0;min-width:80px">发送</el-button>
      </div>

      <!-- 待发送图片大图预览 -->
      <el-dialog v-model="previewPendingVisible" width="520px" :show-close="true" append-to-body>
        <img :src="pendingImage" style="width:100%;display:block;border-radius:8px" />
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Picture, Close, ArrowDown, ChatDotSquare } from '@element-plus/icons-vue'
import { getConversation, sendMessage, markChatAsRead, getConversations } from '@/api/chat'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const myId = userStore.userInfo?.id
const myName = userStore.userInfo?.nickname || userStore.userInfo?.username || '我'
const myAvatar = userStore.userInfo?.avatar || ''

// 会话列表
const conversations = ref([])
const convLoading = ref(true)
const activePetId = ref(null)
const activeOtherId = ref(null)

// 当前聊天
const petId = ref(null)
const otherUserId = ref(null)
const petName = ref('宠物')
const petCover = ref('')
const petCategory = ref('')
const petAge = ref('')
const petGender = ref('')
const otherName = ref('用户')
const otherAvatar = ref('')
const messages = ref([])
const msgLoading = ref(false)
const sending = ref(false)
const uploading = ref(false)
const inputText = ref('')
const pendingImage = ref(null)
const previewPendingVisible = ref(false)
const showScrollBtn = ref(false)
const msgContainer = ref(null)

async function loadConversations() {
  convLoading.value = true
  try {
    const data = await getConversations()
    const enriched = await Promise.all((data || []).map(async (item) => {
      const otherId = item.senderId === myId ? item.receiverId : item.senderId
      try {
        const pet = await request.get('/pets/' + item.petId)
        item.petName = pet?.name || '宠物#' + item.petId
      } catch { item.petName = '宠物#' + item.petId }
      try {
        const user = await request.get('/user/public/' + otherId)
        item.otherName = user?.nickname || user?.username || '用户#' + otherId
        item.otherAvatar = user?.avatar || ''
      } catch { item.otherName = '用户#' + otherId; item.otherAvatar = '' }
      item.otherId = otherId
      return item
    }))
    conversations.value = enriched
  } catch { conversations.value = [] }
  finally { convLoading.value = false }
}

function getOtherName(item) {
  return item.otherName || '用户'
}

async function switchConversation(item) {
  activePetId.value = item.petId
  activeOtherId.value = item.otherId
  petId.value = item.petId
  otherUserId.value = item.otherId
  petName.value = item.petName || '宠物'
  otherName.value = item.otherName || '用户'
  otherAvatar.value = item.otherAvatar || ''
  petCover.value = ''
  petCategory.value = ''
  petAge.value = ''
  petGender.value = ''
  inputText.value = ''
  pendingImage.value = null
  await loadDetail()
}

async function loadDetail() {
  if (!petId.value || !otherUserId.value) return
  msgLoading.value = true
  try {
    try {
      const u = await request.get('/user/public/' + otherUserId.value)
      otherName.value = u?.nickname || u?.username || '用户'
      otherAvatar.value = u?.avatar || ''
    } catch {}
    try {
      const p = await request.get('/pets/' + petId.value)
      petName.value = p?.name || petName.value
      petCover.value = p?.images?.[0] || p?.coverImage || ''
      petCategory.value = p?.categoryName || ''
      petAge.value = p?.age || ''
      petGender.value = (p?.gender === 'male' ? '公' : p?.gender === 'female' ? '母' : p?.gender) || ''
    } catch {}
    messages.value = await getConversation({ petId: petId.value, otherUserId: otherUserId.value })
    // 等 DOM 渲染完成后滚动到最底部
    await nextTick()
    scrollToBottom()
    // 等图片加载后再次滚动（el-image 异步加载）
    setTimeout(scrollToBottom, 300)
  } catch { messages.value = [] }
  finally { msgLoading.value = false }
}

async function handleBeforeUpload(file) {
  uploading.value = true
  try {
    const res = await uploadFile(file)
    if (res && res.url) {
      pendingImage.value = res.url
    }
  } catch {
    ElMessage.error('图片上传失败')
  }
  uploading.value = false
  return false
}

async function handleSend() {
  const text = inputText.value.trim()
  const img = pendingImage.value
  if (!text && !img) return

  if (text && containsSensitive(text)) {
    ElMessage.warning('消息包含联系方式或导流内容，请删除后重试')
    return
  }

  const params = { receiverId: otherUserId.value, petId: petId.value }
  if (text) params.content = text
  if (img) params.imageUrl = img

  sending.value = true
  try {
    await sendMessage(params)
    inputText.value = ''
    pendingImage.value = null
    await loadDetail()
  } catch (e) {
    const errMsg = e?.message || '发送失败'
    // 拦截器已弹提示，这里不再重复弹
  }
  finally { sending.value = false }
}

function containsSensitive(text) {
  const patterns = [
    // ── 零、原始基础规则 ──
    /1[3-9]\d{9}/,                                     // 手机号
    /https?:\/\//,                                     // http 链接
    /www\.[a-zA-Z0-9.-]+\.(com|cn|top|xyz|net|org)/,  // 网址
    /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/, // 邮箱

    // ── 一、微信相关导流词 ──
    /微信|微新|威信|微心|维信|围信|崴信|薇信/,          // 微信变体
    /[vV]\s*信/,                                        // V 信 / V信
    /[vV][xX]|[wW][xX]/,                                // vx VX wx WX（大小写）
    /[vV]\s*号/,                                        // v 号 / V号
    /微号/,                                              // 微号
    /微信号/,                                            // 微信号
    /加微|加\s*[vV]/,                                   // 加微 / 加 v / 加V
    /来微|去微/,                                         // 来微 / 去微
    /微聊|[vV]\s*聊/,                                   // 微聊 / v 聊 / V聊
    /绿泡泡/,                                            // 绿泡泡（微信隐语）

    // ── 二、QQ 相关导流词 ──
    /扣扣|寇寇|口口|球球|秋秋/,                          // QQ 中文变体
    /[qQ][qQ]/,                                          // QQ / qq
    /秋秋号|扣扣号/,                                     // 秋秋号 / 扣扣号
    /加\s*[qQ][qQ]/,                                     // 加 qq / 加QQ / 加qq
    /加扣扣/,                                            // 加扣扣

    // ── 三、私下转移交流类 ──
    /私下聊|私下说|私底下聊/,                            // 私下聊
    /私聊/,                                              // 私聊
    /私我/,                                              // 私我
    /私发/,                                              // 私发
    /私联/,                                              // 私联
    /私加/,                                              // 私加
    /单独聊|单独说/,                                     // 单独聊/说
    /悄悄说|偷偷聊/,                                     // 悄悄说/偷偷聊

    // ── 四、站外跨平台转移词 ──
    /站外|出平台|换平台/,                                // 站外/出平台/换平台
    /别的平台|其他平台/,                                  // 别的平台/其他平台
    /外面聊|出去聊|场外聊/,                              // 外面聊/出去聊/场外聊
    /脱离平台/,                                          // 脱离平台
    /换地方聊/,                                          // 换地方聊

    // ── 五、引导加好友导流词 ──
    /加我|来加我|加一下/,                                // 加我/来加我/加一下
    /互加/,                                              // 互加
    /加好友/,                                            // 加好友
    /扩列/,                                              // 扩列
    /来找我/,                                            // 来找我
    /来找我聊/,                                          // 来找我聊（已包含在上面但明确写出）
    /联系我/,                                            // 联系我
    /来找我细说/,                                        // 来找我细说

    // ── 六、主页资料引流词 ──
    /看主页/,                                            // 看主页
    /看我资料|看我简介|看我名字|看我头像|看我备注/,      // 看我xxx
    /资料里有/,                                          // 资料里有
    /主页自取/,                                          // 主页自取

    // ── 七、发送推送导流词 ──
    /发你|发我|推你|推我/,                              // 发你/发我/推你/推我
    /发给你/,                                            // 发给你
    /私发给你/,                                          // 私发给你
    /发联系方式|发号码/,                                  // 发联系方式/发号码

    // ── 八、其他社交平台导流词 ──
    /抖音|快手/,                                         // 抖音/快手
    /小红书|红书/,                                       // 小红书/红书
    /微博/,                                              // 微博
    /B\s*站|哔哩/,                                      // B站 / B 站 / 哔哩
    /闲鱼/,                                              // 闲鱼
    /淘宝|拼多多/,                                       // 淘宝/拼多多
    /虎牙|斗鱼/,                                         // 虎牙/斗鱼
    /网易云/,                                            // 网易云

    // ── 九、号码联系方式导流词 ──
    /手机号|电话号|电话号码/,                            // 手机号/电话号/电话号码
    /座机/,                                              // 座机
    /号码/,                                              // 号码
    /联系方式|联络方式/,                                  // 联系方式/联络方式

    // ── 十、隐晦暗语导流词 ──
    /绿色软件|绿色图标/,                                  // 绿色软件/绿色图标
    /聊天软件|常用软件|常用聊天/,                        // 聊天软件/常用软件/常用聊天
    /老地方|老位置/,                                     // 老地方/老位置
    /别处|别处聊/,                                       // 别处/别处聊
  ]

  return patterns.some(p => p.test(text))
}

function scrollToBottom() {
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
  showScrollBtn.value = false
}

function onScroll() {
  const el = msgContainer.value
  if (!el) return
  const diff = el.scrollHeight - el.scrollTop - el.clientHeight
  showScrollBtn.value = diff > 120
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}

onMounted(async () => {
  await loadConversations()
  // 从路由参数加载指定会话
  const qPetId = route.query.petId
  const qOtherUserId = parseInt(route.query.otherUserId)
  if (qPetId && qOtherUserId) {
    activePetId.value = qPetId
    activeOtherId.value = qOtherUserId
    petId.value = qPetId
    otherUserId.value = qOtherUserId
    petName.value = route.query.petName || '宠物'
    otherName.value = route.query.otherName || '用户'
    await loadDetail()
    try { await markChatAsRead({ petId: qPetId, otherUserId: qOtherUserId }) } catch {}
  } else if (conversations.value.length > 0) {
    // 默认选中第一个会话
    await switchConversation(conversations.value[0])
  }
})
</script>

<style scoped>
/* ── 整体布局：左右分栏 ── */
.chat-layout {
  display: flex;
  gap: 0;
  max-width: 1060px;
  margin: 0 auto;
  height: calc(100vh - 120px);
}

/* ── 左侧：会话列表 ── */
.chat-sidebar {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid var(--yc-border);
  display: flex;
  flex-direction: column;
  background: var(--yc-bg-card);
  border-radius: var(--yc-radius-card) 0 0 var(--yc-radius-card);
  overflow: hidden;
}
.sidebar-header {
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  color: var(--yc-text-primary);
  border-bottom: 1px solid var(--yc-border);
  flex-shrink: 0;
}
.sidebar-loading,
.sidebar-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
  color: var(--yc-text-tertiary);
  font-size: 13px;
}
.sidebar-list {
  flex: 1;
  overflow-y: auto;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--yc-border-light, #f0f0f0);
  transition: background 0.15s;
}
.conv-item:hover {
  background: var(--yc-bg-page);
}
.conv-item.conv-active {
  background: var(--yc-bg-page);
  border-left: 3px solid var(--yc-accent);
  padding-left: 13px;
}
.conv-avatar {
  flex-shrink: 0;
  border-radius: 8px;
}
.conv-info {
  flex: 1;
  min-width: 0;
}
.conv-name {
  font-size: 14px;
  color: var(--yc-text-primary);
  font-weight: 500;
}
.conv-pet {
  font-size: 11px;
  color: var(--yc-accent);
  margin-top: 1px;
}
.conv-preview {
  font-size: 12px;
  color: var(--yc-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 1px;
}

/* ── 占位提示（未选择会话时） ── */
.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--yc-bg-card);
  border-radius: 0 var(--yc-radius-card) var(--yc-radius-card) 0;
}
.placeholder-content {
  text-align: center;
  color: var(--yc-text-tertiary);
}
.placeholder-content p {
  margin-top: 12px;
  font-size: 14px;
}

/* ── 右侧：聊天详情 ── */
.chat-detail-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--yc-bg-card);
  border-radius: 0 var(--yc-radius-card) var(--yc-radius-card) 0;
  padding: 0 20px;
  position: relative;
  overflow: hidden;
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
  background: var(--yc-bg-page); color: var(--yc-text-primary);
  border: 1px solid var(--yc-border);
  border-bottom-left-radius: 4px;
}

/* 待发送图片预览区 */
.pending-preview {
  display: flex;
  gap: 8px;
  padding: 6px 0 2px;
  flex-shrink: 0;
  border-top: 1px solid var(--yc-border);
}
.pending-item {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--yc-border);
  cursor: pointer;
  flex-shrink: 0;
}
.pending-thumb {
  width: 100%;
  height: 100%;
  display: block;
}
.pending-overlay {
  position: absolute;
  top: 0; right: 0; bottom: 0; left: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  opacity: 0;
  transition: opacity 0.15s;
  padding: 2px;
}
.pending-item:hover .pending-overlay {
  opacity: 1;
}
.pending-close {
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  background: rgba(0,0,0,0.6);
  border-radius: 50%;
  padding: 2px;
}

/* ── 回到底部按钮（输入框正上方居中） ── */
.scroll-bottom-btn {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: calc(100% + 6px);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid var(--yc-border);
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s;
  color: var(--yc-text-secondary);
  font-size: 16px;
}
.scroll-bottom-btn:hover {
  background: var(--yc-accent);
  color: #fff;
  border-color: var(--yc-accent);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.arrow-fade-enter-active { transition: opacity 0.2s, transform 0.2s; }
.arrow-fade-leave-active { transition: opacity 0.15s, transform 0.15s; }
.arrow-fade-enter-from { opacity: 0; transform: translateX(-50%) translateY(10px); }
.arrow-fade-leave-to { opacity: 0; transform: translateX(-50%) translateY(10px); }

/* 输入区 */
.msg-input-bar {
  display: flex; gap: 10px; align-items: stretch;
  padding: 10px 0; flex-shrink: 0;
  border-top: 1px solid var(--yc-border);
  margin-top: 8px;
}
.input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;  /* 给滚动按钮提供参照 */
}
.msg-input-bar :deep(.el-textarea__inner) { border-radius: 10px; resize: none; }

.input-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-left: 4px;
}
.upload-img-btn {
  border: none;
  font-size: 18px;
  color: #909399;
  padding: 4px;
}
.upload-img-btn:hover {
  color: var(--yc-accent);
  background: transparent;
}
</style>
