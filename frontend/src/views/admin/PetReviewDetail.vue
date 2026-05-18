<template>
  <div class="admin-page review-detail-page">
    <div class="review-header">
      <h3 class="page-title">终审 — {{ pet?.name || '宠物审核' }}</h3>
    </div>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="pet">
      <div class="detail-layout">
        <!-- 左侧图 -->
        <div class="detail-left">
          <el-carousel ref="carouselRef" height="400px" :interval="4000" arrow="always">
            <el-carousel-item v-for="(img, idx) in pet.images" :key="idx">
              <el-image
                :src="img" fit="contain" style="width:100%;height:100%"
                :preview-src-list="pet.images" :initial-index="idx" preview-teleported
              >
                <template #error><div class="img-placeholder">加载失败</div></template>
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
            <h2>{{ pet.name }}</h2>
            <el-tag :type="PET_STATUS[pet.status]?.type || 'info'" size="small">
              {{ PET_STATUS[pet.status]?.label || pet.status }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-grid">
            <div><span class="label">送养人</span><span class="value">{{ pet.userNickname }}</span></div>
            <div><span class="label">分类</span><span class="value">{{ pet.categoryName }}</span></div>
            <div><span class="label">年龄</span><span class="value">{{ pet.age }}</span></div>
            <div><span class="label">性别</span><span class="value">{{ GENDER_MAP[pet.gender] || pet.gender }}</span></div>
            <div>
              <span class="label">绝育</span>
              <span class="value">{{ pet.isNeutered === 1 ? '✅ 已绝育' : '❌ 未绝育' }}</span>
            </div>
            <div>
              <span class="label">疫苗</span>
              <span class="value">{{ pet.isVaccinated === 1 ? '✅ 已接种' : '❌ 未接种' }}</span>
            </div>
          </div>
          <el-divider />
          <div class="section">
            <h4>健康证明</h4>
            <el-image v-if="pet.healthCert" :src="pet.healthCert" fit="contain"
                      style="max-width:200px;max-height:150px;border-radius:6px;cursor:pointer"
                      :preview-src-list="[pet.healthCert]" preview-teleported />
            <span v-else style="color:#909399">暂无</span>
          </div>
          <el-divider />
          <div class="section"><h4>性格描述</h4><p>{{ pet.personality }}</p></div>
          <div v-if="pet.habit" class="section"><h4>生活习惯</h4><p>{{ pet.habit }}</p></div>
          <div class="section"><h4>送养原因</h4><p>{{ pet.reason }}</p></div>
          <el-divider />
          <div class="donor-info">
            <h4>送养人联系方式</h4>
            <p>{{ pet.userNickname }} · {{ pet.userPhone }}</p>
          </div>
        </div>
      </div>

      <!-- 审核操作区 -->
      <div class="review-bar">
        <el-button type="primary" size="large" :loading="submitting" @click="doFinal('APPROVED')">
          <el-icon><CircleCheck /></el-icon> 终审通过
        </el-button>
        <el-button type="danger" size="large" :loading="submitting" @click="openRejectDialog">
          <el-icon><CloseBold /></el-icon> 终审打回
        </el-button>
      </div>
    </template>

    <template v-else>
      <div class="empty-tip"><el-empty description="宠物信息不存在" /></div>
    </template>

    <!-- 打回弹窗 -->
    <el-dialog v-model="rejectVisible" title="终审打回" width="420px">
      <el-form label-position="top">
        <el-form-item label="打回原因">
          <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请填写打回原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!rejectRemark" @click="confirmReject">
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
import { Loading, ArrowLeft, CircleCheck, CloseBold } from '@element-plus/icons-vue'
import { getPetDetail } from '@/api/pet'
import { finalReview } from '@/api/admin'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'

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

async function doFinal(action) {
  submitting.value = true
  try {
    await finalReview(route.params.id, { action, remark: '' })
    ElMessage.success('终审通过 ✅')
    router.push('/admin/pets')
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
    await finalReview(route.params.id, { action: 'REJECTED', remark: rejectRemark.value })
    ElMessage.success('已打回')
    rejectVisible.value = false
    router.push('/admin/pets')
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
}
.review-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

/* 信息卡片（管理后台简约风格） */
.detail-layout {
  display: flex;
  gap: 32px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 28px;
}
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
  border-radius: 4px;
  overflow: hidden;
}
.thumbnail-item.active {
  border-color: #409EFF;
}
.detail-right {
  width: 440px;
  flex-shrink: 0;
}
.info-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.info-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.info-grid .label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}
.info-grid .value {
  font-size: 14px;
  color: #303133;
}
.section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
}
.section p {
  margin: 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
}
.donor-info p {
  margin: 0;
  font-size: 14px;
  color: #303133;
}
.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

/* 审核操作区 */
.review-bar {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 28px;
  padding: 24px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}
.review-bar .el-button {
  min-width: 160px;
  height: 44px;
  font-size: 16px;
}
</style>
