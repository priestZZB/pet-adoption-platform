<template>
  <div class="pending-page donor-detail-page">
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

          <!-- 已打回 → 显示打回原因 -->
          <div v-if="pet.status === 'REJECTED' && pet.reviewRemark" class="status-info error">
            <span class="si-icon">❌</span>
            <span><strong>打回原因：</strong>{{ pet.reviewRemark }}</span>
          </div>

          <!-- 已通过 → 显示可领养 -->
          <div v-else-if="pet.status === 'APPROVED'" class="status-info success">
            <span class="si-icon">✅</span>
            <span>审核已通过，等待被领养</span>
          </div>

          <!-- 已被领养 → 显示领养信息 -->
          <div v-else-if="pet.status === 'ADOPTED'" class="status-info info">
            <span class="si-icon">🏡</span>
            <span>已被领养</span>
          </div>

          <!-- 初审通过 → 显示等终审 -->
          <div v-else-if="pet.status === 'FIRST_PASS'" class="status-info warning">
            <span class="si-icon">⏳</span>
            <span>初审已通过，等待管理员终审</span>
          </div>

          <!-- 待审核 -->
          <div v-else-if="pet.status === 'PENDING'" class="status-info pending">
            <span class="si-icon">📋</span>
            <span>等待志愿者审核</span>
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
        </div>
      </div>

      <!-- 时间线 -->
      <div class="timeline-section">
        <h3 class="section-title">📋 事件时间线</h3>
        <div v-if="timeline.length === 0" class="empty-hint">暂无记录</div>
        <div v-else class="timeline-list">
          <div v-for="(evt, idx) in timeline" :key="idx" class="timeline-item">
            <!-- 当前状态指示器（第一条） -->
            <div v-if="idx === 0" class="tl-current-indicator">当前</div>

            <!-- 连接线 + 节点 -->
            <div class="tl-node-col">
              <div class="tl-dot" :class="getDotClass(evt)"></div>
              <div v-if="idx < timeline.length - 1" class="tl-line"></div>
            </div>

            <div class="tl-content">
              <div class="tl-header">
                <span class="tl-icon">{{ getEventIcon(evt.type, evt.action) }}</span>
                <span class="tl-text">{{ getEventText(evt) }}</span>
                <span v-if="evt.type === 'FINAL_REVIEW' && evt.action === 'REJECTED' && evt.remark" class="tl-remark">（{{ evt.remark }}）</span>
              </div>
              <div class="tl-time">{{ formatTime(evt.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="empty-tip"><el-empty description="宠物信息不存在" /></div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, CircleCheckFilled, RemoveFilled } from '@element-plus/icons-vue'
import { getPetDetail } from '@/api/pet'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'
import request from '@/api/request'

const route = useRoute()
const pet = ref(null)
const timeline = ref([])
const loading = ref(true)
const activeIdx = ref(0)
const carouselRef = ref(null)

async function loadDetail() {
  loading.value = true
  try {
    const [petData, events] = await Promise.all([
      getPetDetail(route.params.id),
      request.get('/donate/pets/' + route.params.id + '/timeline')
    ])
    pet.value = petData
    timeline.value = events || []
  } catch {
    pet.value = null
  } finally {
    loading.value = false
  }
}

function getEventIcon(type, action) {
  if (type === 'PUBLISH') return '📝'
  if (type === 'EDIT') return '✏️'
  if (type === 'OFFLINE') return '⬇️'
  if (type === 'FIRST_REVIEW') return action === 'APPROVED' ? '✅' : '❌'
  if (type === 'FINAL_REVIEW') return action === 'APPROVED' ? '✅' : '❌'
  return '📌'
}

function getEventText(event) {
  switch (event.type) {
    case 'PUBLISH': return '提交审核'
    case 'EDIT': return '编辑后重新提交'
    case 'OFFLINE': return '下架'
    case 'FIRST_REVIEW': return `志愿者${event.action === 'APPROVED' ? '通过' : '打回'}初审`
    case 'FINAL_REVIEW': return `管理员${event.action === 'APPROVED' ? '通过' : '打回'}终审`
    default: return '未知事件'
  }
}

function getDotClass(evt) {
  if (evt.type === 'OFFLINE') return 'dot-offline'
  if (evt.type === 'EDIT' || evt.type === 'PUBLISH') return 'dot-publish'
  if (evt.action === 'REJECTED') return 'dot-danger'
  return 'dot-normal'
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 19).replace('T', ' ')
}

function handleThumbnailClick(idx) {
  activeIdx.value = idx
  if (carouselRef.value) carouselRef.value.setActiveItem(idx)
}

onMounted(loadDetail)
</script>

<style scoped>
.donor-detail-page { max-width: 1100px; margin: 0 auto; padding: 24px 0 40px; }
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
.info-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.pet-name { margin: 0; font-size: 24px; color: var(--yc-text-primary); }

/* 状态信息条 */
.status-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: var(--yc-radius-tag);
  font-size: 14px;
  margin-bottom: 4px;
}
.status-info.error { background: #fef0f0; color: #F56C6C; }
.status-info.success { background: #f0f9eb; color: #67C23A; }
.status-info.info { background: #e8f4fd; color: #409EFF; }
.status-info.warning { background: #fdf6ec; color: #E6A23C; }
.status-info.pending { background: var(--yc-bg-page); color: var(--yc-text-secondary); }
.si-icon { font-size: 16px; }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item .label { font-size: 12px; color: var(--yc-text-tertiary); }
.info-item .value { font-size: 14px; color: var(--yc-text-primary); display: flex; align-items: center; gap: 4px; }
.section h4 { margin: 0 0 8px; font-size: 14px; color: var(--yc-text-secondary); }
.section p { margin: 0; font-size: 14px; color: var(--yc-text-primary); line-height: 1.6; white-space: pre-wrap; }
.na-text { font-size: 14px; color: var(--yc-text-tertiary); }
.img-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: var(--yc-bg-card); color: var(--yc-text-tertiary); font-size: 14px; }
:deep(.el-divider--horizontal) { border-top: 1px solid var(--yc-border); margin: 18px 0; }
:deep(.el-carousel__arrow) { background: var(--yc-bg-card); color: var(--yc-text-primary); }
:deep(.el-carousel__arrow:hover) { background: var(--yc-btn-primary); }

/* 时间线 */
.timeline-section { margin-top: 28px; }
.section-title { font-size: 18px; color: var(--yc-text-primary); margin: 0 0 20px; }
.empty-hint { text-align: center; padding: 40px; color: var(--yc-text-tertiary); font-size: 14px; }

.timeline-list { padding-left: 70px; }
.timeline-item { position: relative; padding-bottom: 20px; display: flex; align-items: flex-start; gap: 14px; }
.timeline-item:last-child { padding-bottom: 0; }

.tl-current-indicator {
  position: absolute; left: -70px; top: 0;
  font-size: 11px; color: #fff;
  background: var(--yc-accent);
  padding: 2px 8px; border-radius: 10px;
  white-space: nowrap; font-weight: 500;
}

.tl-node-col {
  display: flex; flex-direction: column; align-items: center;
  flex-shrink: 0; width: 14px;
}
.tl-dot {
  width: 12px; height: 12px; border-radius: 50%;
  flex-shrink: 0; margin-top: 3px;
}
.tl-dot.dot-danger { background: #F56C6C; }
.tl-dot.dot-normal { background: #67C23A; }
.tl-dot.dot-publish { background: var(--yc-accent); }
.tl-dot.dot-offline { background: #909399; }
.tl-line {
  width: 2px; flex: 1; min-height: 10px;
  background: var(--yc-border);
}

.tl-content { flex: 1; min-width: 0; }
.tl-header { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.tl-icon { font-size: 14px; flex-shrink: 0; }
.tl-text { font-size: 14px; color: var(--yc-text-primary); font-weight: 500; }
.tl-remark { font-size: 12px; color: #E6A23C; }
.tl-time { font-size: 12px; color: var(--yc-text-tertiary); margin-top: 2px; }
</style>
