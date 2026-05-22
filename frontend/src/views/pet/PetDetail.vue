<template>
  <div class="detail-page">
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="pet">
      <div class="detail-layout">
        <!-- 左侧：图片轮播 -->
        <div class="detail-left">
          <el-carousel ref="carouselRef" height="400px" :interval="4000" arrow="always">
            <el-carousel-item v-for="(img, idx) in pet.images" :key="idx">
              <el-image
                :src="img"
                fit="contain"
                style="width:100%;height:100%"
                :preview-src-list="pet.images"
                :initial-index="idx"
                preview-teleported
              >
                <template #error>
                  <div class="img-placeholder">图片加载失败</div>
                </template>
              </el-image>
            </el-carousel-item>
          </el-carousel>

          <!-- 缩略图列表 -->
          <div class="thumbnail-list">
            <div
              v-for="(img, idx) in pet.images"
              :key="idx"
              class="thumbnail-item"
              :class="{ active: idx === activeIdx }"
              @click="handleThumbnailClick(idx)"
            >
              <el-image
                :src="img"
                fit="cover"
                style="width:60px;height:60px;border-radius:4px;cursor:pointer"
              />
            </div>
          </div>
        </div>

        <!-- 右侧：信息 -->
        <div class="detail-right">
          <!-- 基本信息 -->
          <div class="info-header">
            <h2 class="pet-name">{{ pet.name }}</h2>
            <el-tag
              :type="PET_STATUS[pet.status]?.type || 'info'"
            >
              {{ PET_STATUS[pet.status]?.label || pet.status }}
            </el-tag>
          </div>

          <el-divider />

          <div class="info-grid">
            <div class="info-item">
              <span class="label">分类</span>
              <span class="value">{{ pet.categoryName }}</span>
            </div>
            <div class="info-item">
              <span class="label">年龄</span>
              <span class="value">{{ pet.age }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别</span>
              <span class="value">{{ GENDER_MAP[pet.gender] || pet.gender }}</span>
            </div>
            <div class="info-item">
              <span class="label">绝育</span>
              <span class="value">
                <el-icon v-if="pet.isNeutered === 1" color="#67C23A"><CircleCheckFilled /></el-icon>
                <el-icon v-else color="#909399"><RemoveFilled /></el-icon>
                {{ pet.isNeutered === 1 ? '已绝育' : '未绝育' }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">疫苗</span>
              <span class="value">
                <el-icon v-if="pet.isVaccinated === 1" color="#67C23A"><CircleCheckFilled /></el-icon>
                <el-icon v-else color="#909399"><RemoveFilled /></el-icon>
                {{ pet.isVaccinated === 1 ? '已接种' : '未接种' }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">收藏</span>
              <span class="value">{{ pet.favoriteCount }} 人收藏</span>
            </div>
          </div>

          <el-divider />

          <!-- 健康证明 -->
          <div class="section">
            <h4>健康证明</h4>
            <el-image
              v-if="pet.healthCert"
              :src="pet.healthCert"
              fit="contain"
              style="max-width:200px;max-height:150px;border-radius:6px;cursor:pointer"
              :preview-src-list="[pet.healthCert]"
              preview-teleported
            />
            <span v-else class="na-text">暂无</span>
          </div>

          <el-divider />

          <!-- 性格 / 习惯 / 送养原因 -->
          <div class="section">
            <h4>性格描述</h4>
            <p>{{ pet.personality }}</p>
          </div>
          <div v-if="pet.habit" class="section">
            <h4>生活习惯</h4>
            <p>{{ pet.habit }}</p>
          </div>
          <div class="section">
            <h4>送养原因</h4>
            <p>{{ pet.reason }}</p>
          </div>

          <el-divider />

          <!-- 送养人信息 -->
          <div class="donor-info">
            <h4>送养人</h4>
            <div class="donor-card">
              <el-avatar :size="40" :src="pet.userAvatar">
                {{ pet.userNickname?.[0] || '?' }}
              </el-avatar>
              <div class="donor-text">
                <span class="donor-name">{{ pet.userNickname }}</span>
                <span class="donor-phone">{{ pet.userPhone }}</span>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 操作按钮 -->
          <div class="action-bar">
            <el-button
              :type="isFav ? 'danger' : 'default'"
              :icon="Star"
              class="fav-btn"
              @click="toggleFavorite"
            >
              {{ isFav ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button
              :icon="ChatDotSquare"
              class="chat-btn"
              :disabled="!userStore.isLogin || pet.userId === userStore.userInfo?.id"
              @click="handleChat"
            >
              联系送养人
            </el-button>
            <el-button
              class="adopt-btn"
              :icon="Check"
              :disabled="pet.status !== 'APPROVED'"
              @click="handleAdopt"
            >
              申请领养
            </el-button>
          </div>
        </div>
      </div>
    <!-- ===== 评论区 ===== -->
    <div class="comments-section">
      <div class="comments-header">
        <h3>评论（{{ commentTotal }}）</h3>
        <el-radio-group v-model="commentSort" size="small" @change="loadComments">
          <el-radio-button value="latest">最新</el-radio-button>
          <el-radio-button value="hot">最热</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="userStore.isLogin" class="comment-input-area">
        <el-input v-model="commentText" type="textarea" :rows="2" placeholder="说说你的想法..." maxlength="500" show-word-limit />
        <div class="comment-input-actions">
          <div>
            <el-button size="small" @click="triggerCommentUpload">📷 添加图片</el-button>
            <span v-if="commentImages.length > 0" class="comment-img-count">{{ commentImages.length }}张</span>
            <input ref="commentFileInput" type="file" multiple accept="image/*" style="display:none" @change="handleCommentFileChange" />
          </div>
          <el-button type="primary" size="small" :loading="commentSubmitting" @click="submitComment">发表</el-button>
        </div>
        <div v-if="commentImages.length > 0" class="comment-img-preview">
          <div v-for="(img, idx) in commentImages" :key="idx" class="comment-img-item">
            <el-image :src="img" fit="cover" style="width:60px;height:60px;border-radius:4px" />
            <span class="comment-img-remove" @click="commentImages.splice(idx, 1)">✕</span>
          </div>
        </div>
      </div>

      <div v-if="commentList.length > 0" class="comment-list">
        <div v-for="item in commentList" :key="item.id" class="comment-item">
          <el-avatar :size="36" :src="item.avatar">{{ item.nickname?.[0] || '?' }}</el-avatar>
          <div class="comment-body">
            <div class="comment-meta">
              <span class="comment-nickname">{{ item.nickname }}</span>
              <span class="comment-time">{{ formatTime(item.createdAt) }}</span>
            </div>

            <!-- 折叠状态只显示提示条 -->
            <div v-if="item.disliked" class="comment-collapsed">
              该评论已被折叠
              <el-button text size="small" class="unfold-btn" @click="toggleDislike(item)">展开</el-button>
            </div>

            <!-- 未折叠显示全部 -->
            <template v-else>
              <div class="comment-content">{{ item.content }}</div>
              <div v-if="item.images && item.images.length > 0" class="comment-imgs">
                <el-image v-for="(img, idx) in item.images" :key="idx" :src="img" fit="cover" style="width:60px;height:60px;border-radius:4px;cursor:pointer" :preview-src-list="item.images" :initial-index="idx" preview-teleported />
              </div>
            </template>

            <!-- 操作按钮始终可见 -->
            <div class="comment-actions">
              <el-button text size="small" @click="toggleLike(item)">{{ item.liked ? '❤️' : '🤍' }} {{ item.likeCount || 0 }}</el-button>
              <el-button text size="small" @click="toggleDislike(item)" :class="{ 'is-disliked': item.disliked }">{{ item.disliked ? '💔' : '🖤' }} {{ item.dislikeCount || 0 }}</el-button>
              <el-button text size="small" @click="showReplyInput(item)">回复</el-button>
              <el-button v-if="item.userId === userStore.userInfo?.id" text size="small" type="danger" @click="handleDeleteComment(item.id)">删除</el-button>
              <el-button v-if="pet.userId === userStore.userInfo?.id && item.userId !== userStore.userInfo?.id" text size="small" type="danger" @click="handleHideComment(item.id)" style="margin-left:auto">删除</el-button>
            </div>
            <div v-if="item.replies && item.replies.length > 0" class="reply-list">
              <div v-for="reply in item.replies" :key="reply.id" class="reply-item">
                <span class="reply-nickname">{{ reply.nickname }}</span>
                <span v-if="reply.replyToName" class="reply-at">回复</span>
                <span v-if="reply.replyToName" class="reply-nickname">{{ reply.replyToName }}</span>
                <span class="reply-text">：{{ reply.content }}</span>
              </div>
            </div>
            <div v-if="replyTargetId === item.id" class="reply-input-area">
              <el-input v-model="replyText" size="small" :placeholder="'回复 ' + item.nickname" @keyup.enter="submitReply(item)" />
              <div class="reply-input-bottom">
                <el-button size="small" text @click="triggerReplyUpload">📷</el-button>
                <input ref="replyFileInput" type="file" multiple accept="image/*" style="display:none" @change="handleReplyFileChange" />
                <span v-if="replyImages.length > 0" class="comment-img-count">{{ replyImages.length }}张</span>
                <el-button size="small" type="primary" @click="submitReply(item)">发送</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="comment-empty">暂无评论，来说两句吧~</div>
    </div>
  </template>

  <template v-else>
    <div class="empty-tip">
      <el-empty description="宠物信息不存在" />
    </div>
  </template>
</div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheckFilled, RemoveFilled, Star, Check, ChatDotSquare } from '@element-plus/icons-vue'
import { getPetDetail, favorite, unfavorite } from '@/api/pet'
import { getExamHistory } from '@/api/adopt'
import { getPetComments, addPetComment, replyComment, likeComment, unlikeComment, dislikeComment, undislikeComment, deleteComment, hideComment } from '@/api/comment'
import { uploadFile } from '@/api/file'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pet = ref(null)
const loading = ref(true)
const isFav = ref(false)
const activeIdx = ref(0)
const carouselRef = ref(null)

async function loadDetail() {
  loading.value = true
  try {
    pet.value = await getPetDetail(route.params.id)
    isFav.value = pet.value.isFavorited === true
    loadComments()
  } catch {
    pet.value = null
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFav.value) {
      await unfavorite(pet.value.id)
      isFav.value = false
      // 重新获取最新收藏数
      const updated = await getPetDetail(pet.value.id)
      pet.value.favoriteCount = updated.favoriteCount
      ElMessage.success('已取消收藏')
    } else {
      await favorite(pet.value.id)
      isFav.value = true
      // 重新获取最新收藏数
      const updated = await getPetDetail(pet.value.id)
      pet.value.favoriteCount = updated.favoriteCount
      ElMessage.success('收藏成功')
    }
  } catch {
    // 请求拦截器统一处理
  }
}

async function handleAdopt() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (userStore.isAdmin) {
    ElMessage.warning('管理员不能执行此操作')
    return
  }
  if (!pet.value || pet.value.status !== 'APPROVED') {
    ElMessage.warning('该宠物当前不可申请领养')
    return
  }

  // 不能领养自己发布的宠物
  if (pet.value.userId && userStore.userInfo?.id && pet.value.userId === userStore.userInfo.id) {
    ElMessage.warning('您不能领养自己发布的宠物')
    return
  }

  // 刷新用户信息，检查实名认证状态
  if (userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  if (userStore.userInfo?.isRealName !== 1) {
    ElMessageBox.confirm(
      '领养前需要先完成实名认证，是否前往认证？',
      '实名认证',
      { confirmButtonText: '去认证', cancelButtonText: '取消', type: 'warning' }
    ).then(() => {
      router.push('/user/real-name')
    }).catch(() => {})
    return
  }

  // 检查最近一次考试是否满分（与后端逻辑一致：只查最新一次）
  try {
    const history = await getExamHistory()
    const latest = Array.isArray(history) && history.length > 0 ? history[0] : null
    const passed = latest && latest.isPassed === 1
    if (!passed) {
      ElMessageBox.confirm(
        '申请领养前需要通过领养考试（满分100分），是否前往考试？',
        '领养考试',
        { confirmButtonText: '去考试', cancelButtonText: '取消', type: 'warning' }
      ).then(() => {
        router.push('/adopt/exam')
      }).catch(() => {})
      return
    }
  } catch {
    // 查询失败时放行，后端会二次校验
  }

  router.push('/adopt/apply/' + pet.value.id)
}

// ===== 评论区 =====
const commentList = ref([])
const commentTotal = ref(0)
const commentSort = ref('latest')
const commentText = ref('')
const commentImages = ref([])
const commentSubmitting = ref(false)
const commentFileInput = ref(null)
const replyTargetId = ref(null)
const replyText = ref('')
const replyImages = ref([])
const replyFileInput = ref(null)

function triggerReplyUpload() {
  replyFileInput.value?.click()
}

async function handleReplyFileChange(e) {
  const files = e.target.files
  if (!files || files.length === 0) return
  for (const f of files) {
    try {
      const res = await uploadFile(f, 'comment')
      if (res && res.url) replyImages.value.push(res.url)
    } catch {}
  }
  e.target.value = ''
}

function triggerCommentUpload() {
  commentFileInput.value?.click()
}

async function handleCommentFileChange(e) {
  const files = e.target.files
  if (!files || files.length === 0) return
  for (const f of files) {
    try {
      const res = await uploadFile(f, 'comment')
      if (res && res.url) commentImages.value.push(res.url)
    } catch {}
  }
  e.target.value = ''
}

async function submitComment() {
  if (!commentText.value.trim() && commentImages.value.length === 0) {
    ElMessage.warning('请输入评论内容或添加图片')
    return
  }
  commentSubmitting.value = true
  try {
    await addPetComment(pet.value.id, { content: commentText.value, images: commentImages.value })
    ElMessage.success('评论成功')
    commentText.value = ''
    commentImages.value = []
    loadComments()
  } catch {} finally { commentSubmitting.value = false }
}

async function loadComments() {
  if (!pet.value) return
  try {
    const res = await getPetComments(pet.value.id, { sort: commentSort.value, page: 1, size: 20 })
    commentList.value = res.list || []
    commentTotal.value = res.total || 0
  } catch {}
}

function showReplyInput(c) {
  replyTargetId.value = replyTargetId.value === c.id ? null : c.id
  replyText.value = ''
}

async function submitReply(c) {
  if (!replyText.value.trim() && replyImages.value.length === 0) return
  try {
    const data = { content: replyText.value, replyTo: c.userId }
    if (replyImages.value.length > 0) data.images = replyImages.value
    await replyComment(c.id, data)
    ElMessage.success('回复成功')
    replyText.value = ''
    replyImages.value = []
    replyTargetId.value = null
    loadComments()
  } catch {}
}

async function toggleDislike(item) {
  try {
    if (item.disliked) {
      await undislikeComment(item.id)
      item.disliked = false
      item.dislikeCount = Math.max(0, (item.dislikeCount || 0) - 1)
    } else {
      await dislikeComment(item.id)
      item.disliked = true
      item.dislikeCount = (item.dislikeCount || 0) + 1
      if (item.liked) {
        item.liked = false
        await unlikeComment(item.id)
        item.likeCount = Math.max(0, (item.likeCount || 0) - 1)
      }
    }
  } catch {}
}

async function toggleLike(item) {
  try {
    if (item.liked) {
      await unlikeComment(item.id)
      item.liked = false
      item.likeCount = Math.max(0, (item.likeCount || 0) - 1)
    } else {
      await likeComment(item.id)
      item.liked = true
      item.likeCount = (item.likeCount || 0) + 1
    }
  } catch {}
}

async function handleDeleteComment(id) {
  try { await deleteComment(id); ElMessage.success('已删除'); loadComments() } catch {}
}

async function handleHideComment(id) {
  try { await hideComment(id); ElMessage.success('已删除'); loadComments() } catch {}
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function handleThumbnailClick(idx) {
  activeIdx.value = idx
  if (carouselRef.value) {
    carouselRef.value.setActiveItem(idx)
  }
}

function handleChat() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (userStore.isAdmin) {
    ElMessage.warning('管理员不能执行此操作')
    return
  }
  if (pet.value.userId === userStore.userInfo?.id) {
    ElMessage.info('这是你发布的宠物')
    return
  }
  router.push('/user/chat?petId=' + pet.value.id + '&otherUserId=' + pet.value.userId + '&petName=' + encodeURIComponent(pet.value.name || '') + '&otherName=' + encodeURIComponent(pet.value.userNickname || ''))
}

onMounted(loadDetail)

// 轮询：每30秒刷新评论
let commentTimer = null
onMounted(() => {
  commentTimer = setInterval(() => {
    if (pet.value) loadComments()
  }, 10000)
})
onUnmounted(() => {
  if (commentTimer) clearInterval(commentTimer)
})
</script>

<style scoped>
.detail-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.detail-layout {
  display: flex;
  gap: 32px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  padding: 28px;
  box-shadow: var(--yc-shadow-card);
}

/* 左侧图片区 */
.detail-left {
  flex: 1;
  min-width: 0;
}
.thumbnail-list {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.thumbnail-item {
  border: 2px solid transparent;
  border-radius: var(--yc-radius-tag);
  overflow: hidden;
}
.thumbnail-item.active {
  border-color: var(--yc-accent);
}

/* 右侧信息区 */
.detail-right {
  width: 440px;
  flex-shrink: 0;
}
.info-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pet-name {
  margin: 0;
  font-size: 24px;
  color: var(--yc-text-primary);
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-item .label {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.info-item .value {
  font-size: 14px;
  color: var(--yc-text-primary);
  display: flex;
  align-items: center;
  gap: 4px;
}
.section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--yc-text-secondary);
}
.section p {
  margin: 0;
  font-size: 14px;
  color: var(--yc-text-primary);
  line-height: 1.6;
  white-space: pre-wrap;
}
.na-text {
  font-size: 14px;
  color: var(--yc-text-tertiary);
}

/* 送养人信息 */
.donor-card {
  display: flex;
  align-items: center;
  gap: 12px;
}
.donor-text {
  display: flex;
  flex-direction: column;
}
.donor-name {
  font-size: 14px;
  color: var(--yc-text-primary);
  font-weight: 500;
}
.donor-phone {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}

/* 操作按钮 */
.action-bar {
  display: flex;
  gap: 12px;
}
.action-bar :deep(.el-button--default) {
  flex: 1;
  border-radius: var(--yc-radius-btn);
  border: 1px solid var(--yc-border);
  color: var(--yc-text-primary);
}
.action-bar :deep(.fav-btn) {
  flex: 1;
  min-width: 130px;
  justify-content: center;
  border-radius: var(--yc-radius-btn) !important;
}
.action-bar :deep(.el-button--default:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
.action-bar :deep(.chat-btn) {
  flex: 1;
  border: 1px solid var(--yc-border);
  color: var(--yc-text-primary);
  border-radius: var(--yc-radius-btn);
}
.action-bar :deep(.chat-btn:hover) {
  border-color: var(--yc-accent);
  color: var(--yc-accent);
}
.action-bar :deep(.adopt-btn) {
  flex: 1;
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
.action-bar :deep(.adopt-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}

/* 图片占位 */
.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--yc-bg-card);
  color: var(--yc-text-tertiary);
  font-size: 14px;
}

/* 分隔线暖色 */
:deep(.el-divider--horizontal) {
  border-top: 1px solid var(--yc-border);
  margin: 18px 0;
}

/* 左侧轮播 */
:deep(.el-carousel__arrow) {
  background: var(--yc-bg-card);
  color: var(--yc-text-primary);
}
:deep(.el-carousel__arrow:hover) {
  background: var(--yc-btn-primary);
}

/* ===== 评论区 ===== */
.comments-section {
  margin-top: 24px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  padding: 24px 28px;
  box-shadow: var(--yc-shadow-card);
}
.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.comments-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--yc-text-primary);
}
.comment-input-area {
  margin-bottom: 20px;
}
.comment-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.comment-img-count {
  font-size: 12px;
  color: var(--yc-text-tertiary);
  margin-left: 4px;
}
.comment-img-preview {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.comment-img-item {
  position: relative;
}
.comment-img-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #F56C6C;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--yc-border);
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 4px;
}
.comment-nickname {
  font-size: 14px;
  font-weight: 500;
  color: var(--yc-accent);
}
.comment-time {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.comment-content {
  font-size: 14px;
  color: var(--yc-text-primary);
  line-height: 1.5;
  white-space: pre-wrap;
  margin-bottom: 6px;
}
.comment-imgs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}
.comment-actions {
  display: flex;
  gap: 4px;
}
.reply-list {
  margin-top: 8px;
  padding: 8px 10px;
  background: var(--yc-bg-page);
  border-radius: 6px;
}
.reply-item {
  font-size: 13px;
  color: var(--yc-text-primary);
  line-height: 1.6;
}
.reply-nickname {
  font-weight: 500;
  color: var(--yc-accent);
}
.reply-at {
  color: var(--yc-text-tertiary);
  margin: 0 2px;
}
.reply-text {
  color: var(--yc-text-primary);
}
.reply-input-area {
  margin-top: 8px;
}
.reply-input-bottom {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}
.comment-empty {
  text-align: center;
  padding: 30px 0;
  color: var(--yc-text-tertiary);
  font-size: 14px;
}
.comment-collapsed {
  padding: 8px 10px;
  background: var(--yc-bg-page);
  border-radius: 6px;
  font-size: 13px;
  color: var(--yc-text-tertiary);
  text-align: center;
  margin: 4px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.unfold-btn {
  font-size: 12px;
}
.is-disliked :deep(.el-button__text) {
  color: #e8564a !important;
}
</style>
