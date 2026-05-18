<template>
  <div class="pending-page review-detail-page">
    <div class="review-header">
      <h3>宠物审核</h3>
    </div>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="pet">
      <!-- 宠物信息面板（复用前台展示风格） -->
      <div class="detail-layout">
        <!-- 左侧图 -->
        <div class="detail-left">
          <el-carousel ref="carouselRef" height="400px" :interval="4000" arrow="always">
            <el-carousel-item v-for="(img, idx) in pet.images" :key="idx">
              <el-image
                :src="img" fit="contain" style="width:100%;height:100%"
                :preview-src-list="pet.images" :initial-index="idx" preview-teleported
              >
                <template #error><div class="img-placeholder">图片加载失败</div></template>
              </el-image>
            </el-carousel-item>
          </el-carousel>
          <div class="thumbnail-list">
            <div v-for="(img, idx) in pet.images" :key="idx"
                 class="thumbnail-item" :class="{ active: activeIdx === idx }"
                 @click="handleThumbnailClick(idx)">
              <el-image :src="img" fit="cover" style="width:60px;height:60px;border-radius:4px;cursor:pointer" />
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="detail-right">
          <div class="info-header">
            <h2 class="pet-name">{{ pet.name }}</h2>
            <el-tag :type="PET_STATUS[pet.status]?.type || 'info'">
              {{ PET_STATUS[pet.status]?.label || pet.status }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-grid">
            <div class="info-item"><span class="label">分类</span><span class="value">{{ pet.categoryName }}</span></div>
            <div class="info-item"><span class="label">年龄</span><span class="value">{{ pet.age }}</span></div>
            <div class="info-item"><span class="label">性别</span><span class="value">{{ GENDER_MAP[pet.gender] || pet.gender }}</span></div>
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
          </div>
          <el-divider />
          <div class="section">
            <h4>健康证明</h4>
            <el-image v-if="pet.healthCert" :src="pet.healthCert" fit="contain"
                      style="max-width:200px;max-height:150px;border-radius:6px;cursor:pointer"
                      :preview-src-list="[pet.healthCert]" preview-teleported />
            <span v-else class="na-text">暂无</span>
          </div>
          <el-divider />
          <div class="section"><h4>性格描述</h4><p>{{ pet.personality }}</p></div>
          <div v-if="pet.habit" class="section"><h4>生活习惯</h4><p>{{ pet.habit }}</p></div>
          <div class="section"><h4>送养原因</h4><p>{{ pet.reason }}</p></div>
          <el-divider />
          <div class="donor-info">
            <h4>送养人</h4>
            <div class="donor-card">
              <el-avatar :size="40" :src="pet.userAvatar">{{ pet.userNickname?.[0] || '?' }}</el-avatar>
              <div class="donor-text">
                <span class="donor-name">{{ pet.userNickname }}</span>
                <span class="donor-phone">{{ pet.userPhone }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 审核操作区 -->
      <div class="review-bar">
        <el-button
          class="review-approve-btn"
          :loading="submitting"
          @click="doReview('APPROVED')"
        >
          <el-icon><CircleCheck /></el-icon> 初审通过
        </el-button>
        <el-button
          class="review-reject-btn"
          :loading="submitting"
          @click="openRejectDialog"
        >
          <el-icon><CloseBold /></el-icon> 打回
        </el-button>
      </div>
    </template>

    <template v-else>
      <div class="empty-tip"><el-empty description="宠物信息不存在" /></div>
    </template>

    <!-- 打回弹窗 -->
    <el-dialog v-model="rejectVisible" title="初审打回" width="420px">
      <el-form>
        <el-form-item label="打回原因">
          <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请填写打回原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="review-cancel-btn" @click="rejectVisible = false">取消</el-button>
        <el-button class="review-confirm-btn" :loading="submitting" :disabled="!rejectRemark" @click="confirmReject">
          确认打回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, ArrowLeft, CircleCheck, CloseBold, CircleCheckFilled, RemoveFilled } from '@element-plus/icons-vue'
import { getPetDetail } from '@/api/pet'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()

const pet = ref(null)
const loading = ref(true)
const activeIdx = ref(0)
const carouselRef = ref(null)
const submitting = ref(false)
const rejectVisible = ref(false)
const rejectRemark = ref('')

async function loadDetail() {
  loading.value = true
  try {
    pet.value = await getPetDetail(route.params.id)
  } catch {
    pet.value = null
  } finally {
    loading.value = false
  }
}

function handleThumbnailClick(idx) {
  activeIdx.value = idx
  if (carouselRef.value) carouselRef.value.setActiveItem(idx)
}

async function doReview(action) {
  submitting.value = true
  try {
    await request.post('/volunteer/pets/' + route.params.id + '/review', {
      action,
      remark: ''
    })
    ElMessage.success('初审通过 ✅')
    router.push('/volunteer/pending')
  } catch {
    // 拦截器处理
  } finally {
    submitting.value = false
  }
}

function openRejectDialog() {
  rejectRemark.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectRemark.value) {
    ElMessage.warning('请填写打回原因')
    return
  }
  submitting.value = true
  try {
    await request.post('/volunteer/pets/' + route.params.id + '/review', {
      action: 'REJECTED',
      remark: rejectRemark.value
    })
    ElMessage.success('已打回')
    rejectVisible.value = false
    router.push('/volunteer/pending')
  } catch {
    // 拦截器处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.review-detail-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.review-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.review-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--yc-text-primary);
}
.loading-center { display: flex; justify-content: center; padding: 80px 0; }
.empty-tip { padding: 60px 0; }

/* 宠物信息卡片（同前台暖色风格） */
.detail-layout {
  display: flex;
  gap: 32px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  padding: 28px;
  box-shadow: var(--yc-shadow-card);
}
.detail-left { flex: 1; min-width: 0; }
.thumbnail-list { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
.thumbnail-item { border: 2px solid transparent; border-radius: var(--yc-radius-tag); overflow: hidden; }
.thumbnail-item.active { border-color: var(--yc-accent); }
.detail-right { width: 440px; flex-shrink: 0; }
.info-header { display: flex; align-items: center; gap: 12px; }
.pet-name { margin: 0; font-size: 24px; color: var(--yc-text-primary); }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item .label { font-size: 12px; color: var(--yc-text-tertiary); }
.info-item .value { font-size: 14px; color: var(--yc-text-primary); display: flex; align-items: center; gap: 4px; }
.section h4 { margin: 0 0 8px; font-size: 14px; color: var(--yc-text-secondary); }
.section p { margin: 0; font-size: 14px; color: var(--yc-text-primary); line-height: 1.6; white-space: pre-wrap; }
.na-text { font-size: 14px; color: var(--yc-text-tertiary); }
.donor-card { display: flex; align-items: center; gap: 12px; }
.donor-text { display: flex; flex-direction: column; }
.donor-name { font-size: 14px; color: var(--yc-text-primary); font-weight: 500; }
.donor-phone { font-size: 12px; color: var(--yc-text-tertiary); }
.img-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: var(--yc-bg-card); color: var(--yc-text-tertiary); font-size: 14px; }
:deep(.el-divider--horizontal) { border-top: 1px solid var(--yc-border); margin: 18px 0; }
:deep(.el-carousel__arrow) { background: var(--yc-bg-card); color: var(--yc-text-primary); }
:deep(.el-carousel__arrow:hover) { background: var(--yc-btn-primary); }

/* 审核操作区 */
.review-bar {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 28px;
  padding: 24px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
}
.review-bar .el-button {
  min-width: 160px;
  height: 44px;
  font-size: 16px;
  border-radius: var(--yc-radius-btn);
}
.review-approve-btn {
  background: #67C23A !important;
  border-color: #67C23A !important;
  color: #fff !important;
}
.review-approve-btn:hover {
  background: #5daf34 !important;
  border-color: #5daf34 !important;
}
.review-reject-btn {
  background: #F56C6C !important;
  border-color: #F56C6C !important;
  color: #fff !important;
}
.review-reject-btn:hover {
  background: #e06060 !important;
  border-color: #e06060 !important;
}

/* 弹窗暖色按钮 */
:deep(.review-cancel-btn) { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-btn); color: var(--yc-text-primary); }
:deep(.review-cancel-btn:hover) { border-color: var(--yc-border-hover); color: var(--yc-accent); }
:deep(.review-confirm-btn) { background: var(--yc-btn-primary); border: 1px solid var(--yc-border); color: var(--yc-btn-text); border-radius: var(--yc-radius-btn); font-weight: 500; }
:deep(.review-confirm-btn:hover) { background: var(--yc-btn-hover); border-color: var(--yc-border-hover); color: var(--yc-btn-text); }

</style>
