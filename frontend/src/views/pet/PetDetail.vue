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
    </template>

    <template v-else>
      <div class="empty-tip">
        <el-empty description="宠物信息不存在" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheckFilled, RemoveFilled, Star, Check, ChatDotSquare } from '@element-plus/icons-vue'
import { getPetDetail, favorite, unfavorite } from '@/api/pet'
import { getExamHistory } from '@/api/adopt'
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
  if (pet.value.userId === userStore.userInfo?.id) {
    ElMessage.info('这是你发布的宠物')
    return
  }
  router.push('/user/chat?petId=' + pet.value.id + '&otherUserId=' + pet.value.userId + '&petName=' + encodeURIComponent(pet.value.name || '') + '&otherName=' + encodeURIComponent(pet.value.userNickname || ''))
}

onMounted(loadDetail)
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
</style>
