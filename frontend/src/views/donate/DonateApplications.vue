<template>
  <div class="applications-page">
    <el-page-header :icon="ArrowLeft" title="返回我的发布" @back="$router.push('/donate/pets')" />
    <h3 class="page-title">领养申请列表</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无领养申请" />
      </div>

      <div v-else class="application-list">
        <el-card v-for="item in list" :key="item.id" class="app-card">
          <div class="app-header">
            <el-avatar :size="32" :src="item.userAvatar">
              {{ item.userNickname?.[0] || '?' }}
            </el-avatar>
            <div class="app-user">
              <span class="user-name">{{ item.userNickname }}</span>
              <span class="user-phone">{{ item.userPhone }}</span>
            </div>
            <el-tag
              :type="APPLY_STATUS[item.status]?.type || 'info'"
              size="small"
            >
              {{ APPLY_STATUS[item.status]?.label || item.status }}
            </el-tag>
          </div>

          <el-divider style="margin:12px 0" />

          <div class="app-detail">
            <div class="detail-row">
              <span class="label">居住环境</span>
              <p>{{ item.livingEnv }}</p>
            </div>
            <div v-if="item.petExp" class="detail-row">
              <span class="label">养宠经验</span>
              <p>{{ item.petExp }}</p>
            </div>
            <div class="detail-row">
              <span class="label">领养承诺</span>
              <p>{{ item.commitment }}</p>
            </div>
            <div class="detail-row">
              <span class="label">申请时间</span>
              <span class="time">{{ item.createdAt }}</span>
            </div>
          </div>

          <div v-if="item.status === 'PENDING'" class="app-actions">
            <el-button
              type="success"
              :loading="reviewing === item.id"
              @click="handleReview(item.id, 'APPROVED')"
            >
              同意
            </el-button>
            <el-button
              type="danger"
              :loading="reviewing === item.id"
              @click="handleReview(item.id, 'REJECTED')"
            >
              拒绝
            </el-button>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { getPetApplications, reviewApplication } from '@/api/pet'
import { APPLY_STATUS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const list = ref([])
const loading = ref(true)
const reviewing = ref(null)

async function loadList() {
  loading.value = true
  try {
    list.value = await getPetApplications(route.params.id)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function handleReview(appId, action) {
  const text = action === 'APPROVED' ? '同意' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定${text}该领养申请？`, '提示')
    reviewing.value = appId
    await reviewApplication(appId, action)
    ElMessage.success(`已${text}`)
    loadList()
  } catch {
    // 取消或失败
  } finally {
    reviewing.value = null
  }
}

onMounted(loadList)
</script>

<style scoped>
.applications-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 16px 0 20px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 卡片暖色 */
:deep(.app-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

.app-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.app-user {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.user-name {
  font-size: 14px;
  color: var(--yc-text-primary);
  font-weight: 500;
}
.user-phone {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}

.detail-row {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}
.detail-row:last-child {
  margin-bottom: 0;
}
.detail-row .label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--yc-text-tertiary);
}
.detail-row p {
  margin: 0;
  font-size: 14px;
  color: var(--yc-text-primary);
  line-height: 1.6;
  white-space: pre-wrap;
}
.detail-row .time {
  font-size: 13px;
  color: var(--yc-text-tertiary);
}

.app-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
