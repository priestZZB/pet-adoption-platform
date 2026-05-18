<template>
  <div class="applications-page">
    <h3 class="page-title">我的领养申请</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无领养申请">
          <el-button class="go-pets-btn" @click="$router.push('/')">去看看宠物</el-button>
        </el-empty>
      </div>

      <div v-else class="application-list">
        <el-card
          v-for="item in list"
          :key="item.id"
          class="application-card"
          shadow="hover"
        >
          <div class="app-content">
            <!-- 宠物封面 -->
            <el-image
              :src="item.petCoverImage"
              fit="cover"
              style="width:80px;height:80px;border-radius:8px;flex-shrink:0"
            >
              <template #error>
                <div class="img-placeholder" />
              </template>
            </el-image>

            <!-- 信息 -->
            <div class="app-info">
              <h4 class="pet-name">{{ item.petName }}</h4>
              <div class="app-meta">
                <span class="app-time">{{ item.createdAt }}</span>
                <el-tag
                  :type="APPLY_STATUS[item.status]?.type || 'info'"
                  size="small"
                >
                  {{ APPLY_STATUS[item.status]?.label || item.status }}
                </el-tag>
              </div>
            </div>

            <!-- 操作 -->
            <el-button class="detail-btn" @click="$router.push('/user/adopt-application/' + item.id)">
              查看详情
            </el-button>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getMyApplications } from '@/api/adopt'
import { APPLY_STATUS } from '@/utils/constants'

const list = ref([])
const loading = ref(true)

async function loadList() {
  loading.value = true
  try {
    list.value = await getMyApplications()
  } catch {
    list.value = []
  } finally {
    loading.value = false
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
  margin: 0 0 20px;
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

.app-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.img-placeholder {
  width: 80px;
  height: 80px;
  background: var(--yc-bg-card);
  border-radius: 8px;
}

.app-info {
  flex: 1;
  min-width: 0;
}
.pet-name {
  margin: 0 0 6px;
  font-size: 15px;
  color: var(--yc-text-primary);
}
.app-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.app-time {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}

/* 申请卡片暖色 */
:deep(.application-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

/* 查看详情按钮 */
:deep(.detail-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.detail-btn:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}

/* 空状态按钮 */
:deep(.go-pets-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.go-pets-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}

.detail-row .label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  color: var(--yc-text-tertiary);
}
</style>
