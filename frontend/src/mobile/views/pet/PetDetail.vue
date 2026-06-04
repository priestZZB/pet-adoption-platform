<template>
  <div class="detail-page">
    <!-- 加载 -->
    <div v-if="loading" class="loading-wrap">
      <i class="fas fa-spinner fa-pulse"></i>
    </div>

    <template v-else-if="pet">
      <!-- 图片轮播 -->
      <div class="carousel-wrap">
        <el-carousel height="100%" :interval="4000">
          <el-carousel-item v-for="(img, idx) in images" :key="idx">
            <el-image :src="img" fit="cover" style="width:100%;height:100%" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 基本信息卡片 -->
      <div class="card-wrap">
        <MobileCard>
          <div class="pet-header">
            <h2>{{ pet.name }}</h2>
            <button class="fav-btn" :class="{ favorited: isFav }" @click="toggleFav">
              <i :class="isFav ? 'fas fa-star' : 'far fa-star'"></i>
              <span>{{ pet.favoriteCount || 0 }}</span>
            </button>
          </div>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">品种</span>
              <span class="info-value">{{ pet.breed || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">性别</span>
              <span class="info-value">{{ genderMap[pet.gender] || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">年龄</span>
              <span class="info-value">{{ pet.age || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <span class="info-value status-text">{{ pet.status || '-' }}</span>
            </div>
          </div>

          <div v-if="pet.description" class="desc-section">
            <h4>简介</h4>
            <p>{{ pet.description }}</p>
          </div>
        </MobileCard>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar" v-if="pet.status === '待领养' || pet.status === '可领养'">
        <button v-if="!userStore.isAdmin" class="btn-primary" @click="goAdopt">申请领养</button>
        <button class="btn-secondary" @click="goExam">领养考试</button>
      </div>

      <!-- 评论 -->
      <div class="card-wrap">
        <MobileCard>
          <div class="comments-header">
            <h4>评论 ({{ commentTotal }})</h4>
            <button class="sort-btn" @click="toggleSort">
              {{ commentSort === 'latest' ? '最新' : '最热' }}
              <i class="fas fa-sort"></i>
            </button>
          </div>

          <!-- 发表评论 -->
          <div class="comment-input" v-if="userStore.isLogin">
            <textarea v-model="commentText" placeholder="写下你的评论..." rows="2"></textarea>
            <button class="submit-btn" :disabled="!commentText.trim() || commentSubmitting" @click="submitComment">
              {{ commentSubmitting ? '发送中' : '发送' }}
            </button>
          </div>

          <!-- 评论列表 -->
          <div v-if="commentList.length > 0" class="comment-list">
            <div v-for="c in commentList" :key="c.id" class="comment-item">
              <div class="comment-user">{{ c.nickname || '用户' }}</div>
              <div class="comment-content">{{ c.content }}</div>
              <div class="comment-time">{{ formatTime(c.createdAt) }}</div>
            </div>
          </div>
          <MobileEmpty v-else icon="fas fa-comments" description="暂无评论" />
        </MobileCard>
      </div>
    </template>

    <div class="bottom-spacer"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPetDetail, favorite, unfavorite } from '@/api/pet'
import { getPetComments, addPetComment } from '@/api/comment'
import { GENDER_MAP } from '@/utils/constants'
import { useUserStore } from '@/stores/user'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pet = ref(null)
const images = ref([])
const loading = ref(true)
const isFav = ref(false)

const genderMap = GENDER_MAP || { 1: '公', 2: '母' }

const commentList = ref([])
const commentTotal = ref(0)
const commentSort = ref('latest')
const commentText = ref('')
const commentSubmitting = ref(false)

onMounted(async () => {
  try {
    pet.value = await getPetDetail(route.params.id)
    images.value = pet.value.images && pet.value.images.length > 0
      ? pet.value.images.map(i => i.url || i)
      : [pet.value.coverImage].filter(Boolean)
  } catch {} finally {
    loading.value = false
  }
  loadComments()
})

async function toggleFav() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFav.value) {
      await unfavorite(pet.value.id)
      isFav.value = false
      pet.value.favoriteCount = Math.max(0, (pet.value.favoriteCount || 0) - 1)
    } else {
      await favorite(pet.value.id)
      isFav.value = true
      pet.value.favoriteCount = (pet.value.favoriteCount || 0) + 1
    }
  } catch {}
}

function goAdopt() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/adopt/apply/' + pet.value.id)
}

function goExam() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/adopt/exam')
}

async function loadComments() {
  try {
    const res = await getPetComments(pet.value.id, { sort: commentSort.value, page: 1, size: 20 })
    commentList.value = res.records || []
    commentTotal.value = res.total || 0
  } catch {}
}

function toggleSort() {
  commentSort.value = commentSort.value === 'latest' ? 'hot' : 'latest'
  loadComments()
}

async function submitComment() {
  if (!commentText.value.trim()) return
  commentSubmitting.value = true
  try {
    await addPetComment({ petId: pet.value.id, content: commentText.value })
    commentText.value = ''
    ElMessage.success('评论成功')
    loadComments()
  } catch {} finally { commentSubmitting.value = false }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return `${d.getMonth() + 1}-${d.getDate()}`
}
</script>

<style scoped>
.detail-page { padding-bottom: 40px; }

/* === 加载 === */
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }

/* === 轮播 === */
.carousel-wrap {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #e8ddd0;
}
.carousel-wrap :deep(.el-carousel) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__container) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__item) { height: 100% !important; }

/* === 卡片 === */
.card-wrap { padding: 12px 16px 0; }

/* === 宠物头部 === */
.pet-header { display: flex; align-items: center; gap: 12px; }
.pet-header h2 { flex: 1; font-size: 20px; font-weight: 700; color: #5a4a42; margin: 0; }
.fav-btn {
  display: flex; align-items: center; gap: 4px;
  height: 36px; padding: 0 14px;
  border: 1px solid #d1e7dd; border-radius: 18px;
  background: #fefaf5; color: #a09080; font-size: 14px; cursor: pointer;
}
.fav-btn.favorited { color: #e6a23c; border-color: #e6a23c; background: rgba(230,162,60,0.06); }
.fav-btn i { font-size: 16px; }

/* === 信息网格 === */
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 16px; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 12px; color: #a09080; }
.info-value { font-size: 15px; color: #5a4a42; font-weight: 500; }
.status-text { color: #8ab8a0; }

/* === 简介 === */
.desc-section { margin-top: 16px; padding-top: 16px; border-top: 1px solid #f5f0e8; }
.desc-section h4 { font-size: 14px; font-weight: 600; color: #5a4a42; margin: 0 0 8px; }
.desc-section p { font-size: 14px; color: #a09080; line-height: 1.6; margin: 0; }

/* === 操作栏 === */
.action-bar { display: flex; gap: 10px; padding: 16px; }
.btn-primary {
  flex: 1; height: 48px; border: none; border-radius: 12px;
  background: linear-gradient(135deg, #c19a6b 0%, #b0895a 100%);
  color: #fff; font-size: 16px; font-weight: 600; cursor: pointer;
  box-shadow: 0 4px 14px rgba(177,137,90,0.3);
}
.btn-primary:active { transform: scale(0.98); }
.btn-secondary {
  flex: 1; height: 48px;
  border: 1px solid #d1e7dd; border-radius: 12px;
  background: #fefaf5; color: #5a4a42; font-size: 16px; font-weight: 500; cursor: pointer;
}
.btn-secondary:active { background: #f0e8dc; }

/* === 评论 === */
.comments-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.comments-header h4 { font-size: 15px; font-weight: 600; color: #5a4a42; margin: 0; }
.sort-btn { border: none; background: none; color: #8ab8a0; font-size: 13px; cursor: pointer; }

.comment-input { margin-bottom: 16px; }
.comment-input textarea {
  width: 100%; padding: 10px 12px; border: 1px solid #d1e7dd; border-radius: 10px;
  background: #fefaf5; font-size: 14px; color: #5a4a42; resize: none;
  outline: none; box-sizing: border-box;
}
.comment-input textarea:focus { border-color: #8ab8a0; }
.submit-btn {
  float: right; margin-top: 8px;
  height: 36px; padding: 0 20px; border: none; border-radius: 10px;
  background: #8ab8a0; color: #fff; font-size: 13px; font-weight: 500; cursor: pointer;
}
.submit-btn:active { background: #7a9a8a; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.comment-list { display: flex; flex-direction: column; gap: 14px; }
.comment-item { padding-bottom: 14px; border-bottom: 1px solid #f5f0e8; }
.comment-item:last-child { border-bottom: none; padding-bottom: 0; }
.comment-user { font-size: 13px; font-weight: 600; color: #5a4a42; margin-bottom: 4px; }
.comment-content { font-size: 14px; color: #5a4a42; line-height: 1.5; }
.comment-time { font-size: 11px; color: #a09080; margin-top: 4px; }

.bottom-spacer { height: 80px; }
</style>
