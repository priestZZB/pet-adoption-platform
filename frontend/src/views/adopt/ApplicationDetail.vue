<template>
  <div class="pending-page app-detail-page">
    <div v-if="loading" class="loading-center"><el-icon class="is-loading" :size="32"><Loading /></el-icon></div>

    <template v-else-if="app">
      <!-- 宠物信息 -->
      <div class="detail-layout">
        <div class="detail-left">
          <el-carousel ref="carouselRef" height="400px" :interval="4000" arrow="always">
            <el-carousel-item v-for="(img, idx) in petImages" :key="idx">
              <el-image :src="img" fit="contain" style="width:100%;height:100%"
                        :preview-src-list="petImages" :initial-index="idx" preview-teleported>
                <template #error><div class="img-placeholder">加载失败</div></template>
              </el-image>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="detail-right">
          <div class="info-header">
            <h2 class="pet-name">{{ app.petName }}</h2>
            <el-tag :type="APPLY_STATUS[app.status]?.type || 'info'" size="small">
              {{ APPLY_STATUS[app.status]?.label || app.status }}
            </el-tag>
          </div>
          <el-divider />

          <!-- 申请信息 -->
          <div class="section">
            <h4>📋 申请信息</h4>
            <div class="info-grid">
              <div class="info-item"><span class="label">申请时间</span><span class="value">{{ formatTime(app.createdAt) }}</span></div>
              <div class="info-item"><span class="label">居住环境</span><p class="value-text">{{ app.livingEnv }}</p></div>
              <div v-if="app.petExp" class="info-item"><span class="label">养宠经验</span><p class="value-text">{{ app.petExp }}</p></div>
              <div class="info-item"><span class="label">领养承诺</span><p class="value-text">{{ app.commitment }}</p></div>
            </div>
          </div>
          <el-divider />

          <!-- 宠物信息 -->
          <div class="section">
            <h4>🐱 宠物信息</h4>
            <div class="info-grid">
              <div class="info-item"><span class="label">分类</span><span class="value">{{ categoryName }}</span></div>
              <div class="info-item"><span class="label">年龄</span><span class="value">{{ petAge }}</span></div>
              <div class="info-item"><span class="label">性别</span><span class="value">{{ petGender }}</span></div>
              <div class="info-item"><span class="label">绝育</span><span class="value">{{ isNeutered ? '已绝育' : '未绝育' }}</span></div>
              <div class="info-item"><span class="label">疫苗</span><span class="value">{{ isVaccinated ? '已接种' : '未接种' }}</span></div>
            </div>
            <div class="section" v-if="petPersonality">
              <h4>性格描述</h4>
              <p>{{ petPersonality }}</p>
            </div>
            <div class="section" v-if="petReason">
              <h4>送养原因</h4>
              <p>{{ petReason }}</p>
            </div>
          </div>

          <!-- 联系区 -->
          <el-divider />
          <div class="contact-section">
            <h4>📞 联系原主人</h4>
            <div class="contact-buttons">
              <el-button class="contact-chat-btn" @click="handleChat">
                <el-icon><ChatDotSquare /></el-icon> 在线联系
              </el-button>
              <el-button class="contact-phone-btn" @click="showPhone = !showPhone">
                <el-icon><Phone /></el-icon> 电话联系
              </el-button>
            </div>
            <div v-if="showPhone && donorPhone" class="phone-display">
              <span class="phone-number">{{ donorPhone }}</span>
              <el-button text size="small" class="copy-btn" @click="copyPhone">复制</el-button>
            </div>
            <div v-if="showPhone && !donorPhone" class="phone-display">
              <span class="phone-na">暂无联系方式</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="empty-tip"><el-empty description="申请不存在" /></div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading, ChatDotSquare, Phone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getApplicationDetail } from '@/api/adopt'
import { getPetDetail } from '@/api/pet'
import request from '@/api/request'
import { APPLY_STATUS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const app = ref(null)
const loading = ref(true)
const showPhone = ref(false)
const donorPhone = ref('')
const carouselRef = ref(null)

// Pet info
const petImages = ref([])
const categoryName = ref('')
const petAge = ref('')
const petGender = ref('')
const isNeutered = ref(false)
const isVaccinated = ref(false)
const petPersonality = ref('')
const petReason = ref('')
const donorId = ref('')
const donorName = ref('')

async function loadDetail() {
  loading.value = true
  try {
    const detail = await getApplicationDetail(route.params.id)
    app.value = detail

    // 加载宠物详情 + 送养人信息
    try {
      const petDetail = await getPetDetail(detail.petId)
      petImages.value = petDetail.images || []
      categoryName.value = petDetail.categoryName || ''
      petAge.value = petDetail.age || ''
      petGender.value = petDetail.gender === 'male' ? '公' : petDetail.gender === 'female' ? '母' : petDetail.gender || ''
      isNeutered.value = petDetail.isNeutered === 1
      isVaccinated.value = petDetail.isVaccinated === 1
      petPersonality.value = petDetail.personality || ''
      petReason.value = petDetail.reason || ''
      // 送养人信息
      donorId.value = petDetail.userId || ''
      donorPhone.value = petDetail.userPhone || ''
      donorName.value = petDetail.userNickname || ''
    } catch {}
  } catch {
    app.value = null
  } finally {
    loading.value = false
  }
}

function handleChat() {
  if (!app.value || !donorId.value) return
  router.push('/user/chat?petId=' + app.value.petId + '&otherUserId=' + donorId.value + '&petName=' + encodeURIComponent(app.value.petName || '') + '&otherName=' + encodeURIComponent(donorName.value))
}

function copyPhone() {
  if (donorPhone.value) {
    navigator.clipboard.writeText(donorPhone.value).then(() => {
      ElMessage.success('已复制手机号')
    }).catch(() => {
      ElMessage.success(donorPhone.value)
    })
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 19).replace('T', ' ')
}

onMounted(loadDetail)
</script>

<style scoped>
.app-detail-page { max-width: 1100px; margin: 0 auto; padding: 24px 0 40px; }
.loading-center { display: flex; justify-content: center; padding: 80px 0; }
.empty-tip { padding: 60px 0; }

.detail-layout {
  display: flex; gap: 32px;
  background: var(--yc-bg-card); border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card); padding: 28px;
  box-shadow: var(--yc-shadow-card);
}
.detail-left { flex: 1; min-width: 0; }
.detail-right { width: 440px; flex-shrink: 0; }
.info-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.pet-name { margin: 0; font-size: 24px; color: var(--yc-text-primary); }

.section h4 { margin: 0 0 10px; font-size: 14px; color: var(--yc-text-secondary); }
.section p { margin: 0; font-size: 14px; color: var(--yc-text-primary); line-height: 1.6; white-space: pre-wrap; }
.info-grid { display: flex; flex-direction: column; gap: 10px; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-item .label { font-size: 12px; color: var(--yc-text-tertiary); }
.info-item .value { font-size: 14px; color: var(--yc-text-primary); }
.info-item .value-text { margin: 0; font-size: 14px; color: var(--yc-text-primary); line-height: 1.6; white-space: pre-wrap; }

.img-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: var(--yc-bg-card); color: var(--yc-text-tertiary); font-size: 14px; }
:deep(.el-divider--horizontal) { border-top: 1px solid var(--yc-border); margin: 18px 0; }
:deep(.el-carousel__arrow) { background: var(--yc-bg-card); color: var(--yc-text-primary); }
:deep(.el-carousel__arrow:hover) { background: var(--yc-btn-primary); }

/* 联系区 */
.contact-section { margin-top: 4px; }
.contact-buttons { display: flex; gap: 12px; margin-top: 10px; }
.contact-chat-btn {
  flex: 1; background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border); color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn); font-weight: 500;
}
.contact-chat-btn:hover { background: var(--yc-btn-hover); border-color: var(--yc-border-hover); }
.contact-phone-btn {
  flex: 1; border: 1px solid var(--yc-border);
  color: var(--yc-text-primary); border-radius: var(--yc-radius-btn);
}
.contact-phone-btn:hover { border-color: var(--yc-border-hover); color: var(--yc-accent); }
.phone-display {
  margin-top: 12px; padding: 12px 16px;
  background: var(--yc-bg-page); border-radius: var(--yc-radius-tag);
  display: flex; align-items: center; gap: 12px;
}
.phone-number { font-size: 18px; font-weight: 600; color: var(--yc-text-primary); letter-spacing: 2px; }
.phone-na { font-size: 14px; color: var(--yc-text-tertiary); }
.copy-btn { color: var(--yc-accent); font-weight: 500; }
.copy-btn:hover { opacity: 0.8; }
</style>
