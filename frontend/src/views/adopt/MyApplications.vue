<template>
  <div class="applications-page">
    <h3 class="page-title">我的领养申请</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无领养申请">
          <el-button type="primary" @click="$router.push('/')">去看看宠物</el-button>
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
            <el-button @click="showDetail(item)">
              查看详情
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 申请详情弹窗 -->
      <el-dialog v-model="dialogVisible" title="申请详情" width="500px">
        <div v-if="currentApp" class="dialog-body">
          <div class="detail-row">
            <span class="label">宠物</span>
            <span>{{ currentApp.petName }}</span>
          </div>
          <div class="detail-row">
            <span class="label">申请时间</span>
            <span>{{ currentApp.createdAt }}</span>
          </div>
          <div class="detail-row">
            <span class="label">状态</span>
            <el-tag
              :type="APPLY_STATUS[currentApp.status]?.type || 'info'"
            >
              {{ APPLY_STATUS[currentApp.status]?.label || currentApp.status }}
            </el-tag>
          </div>
          <el-divider />
          <div class="detail-row">
            <span class="label">居住环境</span>
            <p>{{ currentApp.livingEnv }}</p>
          </div>
          <div class="detail-row" v-if="currentApp.petExp">
            <span class="label">养宠经验</span>
            <p>{{ currentApp.petExp }}</p>
          </div>
          <div class="detail-row">
            <span class="label">领养承诺</span>
            <p>{{ currentApp.commitment }}</p>
          </div>
        </div>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getMyApplications, getApplicationDetail } from '@/api/adopt'
import { APPLY_STATUS } from '@/utils/constants'

const list = ref([])
const loading = ref(true)
const dialogVisible = ref(false)
const currentApp = ref(null)

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

async function showDetail(item) {
  try {
    currentApp.value = await getApplicationDetail(item.id)
    dialogVisible.value = true
  } catch {
    // 请求拦截器统一处理
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
  color: #303133;
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
  background: #f5f7fa;
  border-radius: 8px;
}

.app-info {
  flex: 1;
  min-width: 0;
}
.pet-name {
  margin: 0 0 6px;
  font-size: 15px;
  color: #303133;
}
.app-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.app-time {
  font-size: 12px;
  color: #909399;
}

/* 弹窗详情 */
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-row {
  display: flex;
  gap: 12px;
}
.detail-row .label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  color: #909399;
}
.detail-row span,
.detail-row p {
  font-size: 14px;
  color: #303133;
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
