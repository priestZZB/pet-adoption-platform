<template>
  <div class="pending-page review-detail-page">
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="pet">
      <!-- 宠物信息 -->
      <div class="detail-layout">
        <div class="detail-left">
          <el-carousel ref="carouselRef" height="400px" :interval="4000" arrow="always">
            <el-carousel-item v-for="(img, idx) in pet.images" :key="idx">
              <el-image :src="img" fit="contain" style="width:100%;height:100%"
                        :preview-src-list="pet.images" :initial-index="idx" preview-teleported>
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
        <div class="detail-right">
          <div class="info-header">
            <h2 class="pet-name">{{ pet.name }}</h2>
            <el-tag :type="PET_STATUS[pet.status]?.type || 'info'">{{ PET_STATUS[pet.status]?.label || pet.status }}</el-tag>
          </div>
          <el-divider />
          <div class="info-grid">
            <div class="info-item"><span class="label">分类</span><span class="value">{{ pet.categoryName }}</span></div>
            <div class="info-item"><span class="label">年龄</span><span class="value">{{ pet.age }}</span></div>
            <div class="info-item"><span class="label">性别</span><span class="value">{{ GENDER_MAP[pet.gender] || pet.gender }}</span></div>
            <div class="info-item"><span class="label">绝育</span><span class="value"><el-icon v-if="pet.isNeutered === 1" color="#67C23A"><CircleCheckFilled /></el-icon><el-icon v-else color="#909399"><RemoveFilled /></el-icon>{{ pet.isNeutered === 1 ? '已绝育' : '未绝育' }}</span></div>
            <div class="info-item"><span class="label">疫苗</span><span class="value"><el-icon v-if="pet.isVaccinated === 1" color="#67C23A"><CircleCheckFilled /></el-icon><el-icon v-else color="#909399"><RemoveFilled /></el-icon>{{ pet.isVaccinated === 1 ? '已接种' : '未接种' }}</span></div>
          </div>
          <el-divider />
          <div class="section"><h4>健康证明</h4><el-image v-if="pet.healthCert" :src="pet.healthCert" fit="contain" style="max-width:200px;max-height:150px;border-radius:6px;cursor:pointer" :preview-src-list="[pet.healthCert]" preview-teleported /><span v-else class="na-text">暂无</span></div>
          <el-divider />
          <div class="section"><h4>性格描述</h4><p>{{ pet.personality }}</p></div>
          <div v-if="pet.habit" class="section"><h4>生活习惯</h4><p>{{ pet.habit }}</p></div>
          <div class="section"><h4>送养原因</h4><p>{{ pet.reason }}</p></div>
          <el-divider />
          <div class="donor-info"><h4>送养人</h4><div class="donor-card"><el-avatar :size="40" :src="pet.userAvatar">{{ pet.userNickname?.[0] || '?' }}</el-avatar><div class="donor-text"><span class="donor-name">{{ pet.userNickname }}</span><span class="donor-phone">{{ pet.userPhone }}</span></div></div></div>
        </div>
      </div>

      <!-- 审核记录 -->
      <div class="review-history-section">
        <h3 class="section-title">📋 最新审核记录</h3>
        <el-table :data="latestRecords" border stripe v-if="latestRecords.length > 0" class="review-table">
          <el-table-column label="审核类型" width="80">
            <template #default="{ row }">{{ row.reviewType === 'FIRST' ? '初审' : '终审' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-tag :type="row.action === 'APPROVED' ? 'success' : 'danger'" size="small">
                {{ row.action === 'APPROVED' ? '通过' : '打回' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核人" width="80">
            <template #default="{ row }">{{ row.reviewType === 'FIRST' ? '志愿者' : '管理员' }}</template>
          </el-table-column>
          <el-table-column label="时间" width="150">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="原因/备注" min-width="120">
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
        </el-table>
        <div v-else class="empty-hint">暂无审核记录</div>

        <!-- 历史轮次折叠 -->
        <div v-if="hasHistory" class="history-toggle">
          <el-button text @click="showAll = !showAll" class="toggle-btn">
            {{ showAll ? '收起历史记录' : '查看历史记录（' + historyRecords.length + '条）' }}
            <el-icon :class="{ rotated: showAll }"><ArrowDown /></el-icon>
          </el-button>
          <el-table :data="historyRecords" border stripe v-if="showAll" class="review-table" style="margin-top:12px">
            <el-table-column label="审核类型" width="80">
              <template #default="{ row }">{{ row.reviewType === 'FIRST' ? '初审' : '终审' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">{{ row.action === 'APPROVED' ? '通过' : '打回' }}</template>
            </el-table-column>
            <el-table-column label="时间" width="150">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="原因" min-width="120">
              <template #default="{ row }">{{ row.remark || '-' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="empty-tip"><el-empty description="宠物信息不存在" /></div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, CircleCheckFilled, RemoveFilled, ArrowDown } from '@element-plus/icons-vue'
import { getPetDetail } from '@/api/pet'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'
import request from '@/api/request'

const route = useRoute()
const pet = ref(null)
const reviewRecords = ref([])
const loading = ref(true)
const activeIdx = ref(0)
const carouselRef = ref(null)
const showAll = ref(false)

// 最新的审核批次 = 最新的FIRST + 之后的所有记录
const latestRecords = computed(() => {
  const records = reviewRecords.value
  if (records.length === 0) return []
  // 找到最后一个 FIRST 审核的索引
  let lastFirstIdx = -1
  for (let i = records.length - 1; i >= 0; i--) {
    if (records[i].reviewType === 'FIRST') {
      lastFirstIdx = i
      break
    }
  }
  if (lastFirstIdx === -1) return records.slice(-2) // 兜底
  return records.slice(lastFirstIdx)
})

const historyRecords = computed(() => {
  const records = reviewRecords.value
  if (records.length === 0) return []
  let lastFirstIdx = -1
  for (let i = records.length - 1; i >= 0; i--) {
    if (records[i].reviewType === 'FIRST') {
      lastFirstIdx = i
      break
    }
  }
  if (lastFirstIdx <= 0) return []
  return records.slice(0, lastFirstIdx)
})

const hasHistory = computed(() => historyRecords.value.length > 0)

async function loadDetail() {
  loading.value = true
  try {
    const [petData, records] = await Promise.all([
      getPetDetail(route.params.id),
      request.get('/volunteer/pets/' + route.params.id + '/review-records')
    ])
    pet.value = petData
    reviewRecords.value = records || []
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

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 19).replace('T', ' ')
}

onMounted(loadDetail)
</script>

<style scoped>
.review-detail-page { max-width: 1100px; margin: 0 auto; padding: 24px 0 40px; }
.loading-center { display: flex; justify-content: center; padding: 80px 0; }
.empty-tip { padding: 60px 0; }

.detail-layout {
  display: flex; gap: 32px;
  background: var(--yc-bg-card); border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card); padding: 28px;
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

/* 审核记录区 */
.review-history-section { margin-top: 28px; }
.section-title { font-size: 18px; color: var(--yc-text-primary); margin: 0 0 16px; }
.review-table { width: 100%; border-radius: 8px; overflow: hidden; }
.empty-hint { text-align: center; padding: 40px; color: var(--yc-text-tertiary); font-size: 14px; }

.history-toggle { margin-top: 16px; text-align: center; }
.toggle-btn { color: var(--yc-text-secondary); font-size: 13px; }
.toggle-btn:hover { color: var(--yc-accent); }
.toggle-btn .el-icon { transition: transform 0.2s; margin-left: 4px; }
.toggle-btn .rotated { transform: rotate(180deg); }
</style>
