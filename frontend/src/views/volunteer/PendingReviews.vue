<template>
  <div class="pending-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange" class="page-tabs">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="审核历史" name="reviewed" />
      <el-tab-pane label="去走访" name="add" />
      <el-tab-pane label="走访记录" name="visits" />
    </el-tabs>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无待审核宠物" />
      </div>

      <div v-else class="pet-list">
        <el-card v-for="pet in list" :key="pet.id" class="pet-card">
          <div class="pet-content">
            <el-image
              :src="pet.coverImage"
              fit="cover"
              style="width:100px;height:100px;border-radius:8px;flex-shrink:0"
            >
              <template #error>
                <div class="img-placeholder" />
              </template>
            </el-image>

            <div class="pet-info">
              <div class="pet-header">
                <h4>{{ pet.name }}</h4>
                <el-tag type="warning" size="small">待审核</el-tag>
              </div>
              <p class="pet-meta">
                {{ pet.categoryName }} · {{ pet.age }} · {{ GENDER_MAP[pet.gender] || pet.gender }}
              </p>
              <p class="pet-donor">送养人：{{ pet.userNickname }}</p>
            </div>

            <!-- 审核操作 -->
            <div class="pet-actions">
              <el-button
                type="success"
                size="small"
                @click="openReview(pet.id, 'APPROVED')"
              >
                初审通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="openReview(pet.id, 'REJECTED')"
              >
                打回
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 审核弹窗（打回需填原因） -->
      <el-dialog v-model="reviewVisible" :title="reviewAction === 'APPROVED' ? '初审通过' : '初审打回'" width="400px">
        <el-form>
          <el-form-item v-if="reviewAction === 'REJECTED'" label="打回原因">
            <el-input
              v-model="reviewRemark"
              type="textarea"
              :rows="3"
              placeholder="请填写打回原因（必填）"
            />
          </el-form-item>
          <p v-else style="font-size:14px;color:#606266">确认初审通过该宠物？</p>
        </el-form>
        <template #footer>
          <el-button class="review-cancel-btn" @click="reviewVisible = false">取消</el-button>
          <el-button
            class="review-confirm-btn"
            :loading="reviewing"
            :disabled="reviewAction === 'REJECTED' && !reviewRemark"
            @click="confirmReview"
          >
            确认
          </el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import request from '@/api/request'
import { GENDER_MAP } from '@/utils/constants'

const router = useRouter()
const activeTab = ref('pending')
const list = ref([])
const loading = ref(true)
const reviewVisible = ref(false)
const reviewAction = ref('')
const reviewPetId = ref(null)
const reviewRemark = ref('')
const reviewing = ref(false)

async function loadList() {
  loading.value = true
  try {
    list.value = await request.get('/volunteer/pets/pending')
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openReview(petId, action) {
  reviewPetId.value = petId
  reviewAction.value = action
  reviewRemark.value = ''
  reviewVisible.value = true
}

async function confirmReview() {
  reviewing.value = true
  try {
    await request.post('/volunteer/pets/' + reviewPetId.value + '/review', {
      action: reviewAction.value,
      remark: reviewAction.value === 'REJECTED' ? reviewRemark.value : ''
    })
    ElMessage.success(reviewAction.value === 'APPROVED' ? '已通过初审' : '已打回')
    reviewVisible.value = false
    loadList()
  } catch {
    // 请求拦截器统一处理
  } finally {
    reviewing.value = false
  }
}

function onTabChange(tab) {
  if (tab === 'reviewed') router.push('/volunteer/reviewed')
  else if (tab === 'add') router.push('/volunteer/visits/add')
  else if (tab === 'visits') router.push('/volunteer/visits')
}

onMounted(loadList)
</script>

<style scoped>
.pending-page {
  max-width: 800px;
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

.pet-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pet-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.img-placeholder {
  width: 100px;
  height: 100px;
  background: var(--yc-bg-card);
  border-radius: 8px;
}

.pet-info {
  flex: 1;
  min-width: 0;
}
.pet-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.pet-header h4 {
  margin: 0;
  font-size: 16px;
  color: var(--yc-text-primary);
}
.pet-meta {
  margin: 0 0 4px;
  font-size: 13px;
  color: var(--yc-text-secondary);
}
.pet-donor {
  margin: 0;
  font-size: 12px;
  color: var(--yc-text-tertiary);
}

.pet-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

/* 卡片暖色 */
:deep(.pet-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

/* Tabs 暖色 */
:deep(.el-tabs__active-bar) {
  background: var(--yc-accent);
}
:deep(.el-tabs__item.is-active) {
  color: var(--yc-text-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--yc-accent);
}

/* 弹窗按钮暖色 */
:deep(.review-cancel-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.review-cancel-btn:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
:deep(.review-confirm-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.review-confirm-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
</style>
