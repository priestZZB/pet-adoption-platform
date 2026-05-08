<template>
  <div class="history-page">
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
        <el-empty description="暂无审核记录" />
      </div>

      <div v-else class="pet-list">
        <el-card v-for="pet in list" :key="pet.id" class="pet-card">
          <div class="pet-content">
            <el-image
              :src="pet.coverImage"
              fit="cover"
              style="width:80px;height:80px;border-radius:8px;flex-shrink:0"
            >
              <template #error>
                <div class="img-placeholder" />
              </template>
            </el-image>

            <div class="pet-info">
              <div class="pet-header">
                <h4>{{ pet.name }}</h4>
                <el-tag
                  :type="PET_STATUS[pet.status]?.type || 'info'"
                  size="small"
                >
                  {{ PET_STATUS[pet.status]?.label || pet.status }}
                </el-tag>
              </div>
              <p class="pet-meta">
                {{ pet.categoryName }} · {{ pet.age }} · {{ GENDER_MAP[pet.gender] || pet.gender }}
              </p>
            </div>

            <el-button
              size="small"
              @click="$router.push('/pets/' + pet.id)"
            >
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
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import request from '@/api/request'
import { PET_STATUS, GENDER_MAP } from '@/utils/constants'

const router = useRouter()
const activeTab = ref('reviewed')
const list = ref([])
const loading = ref(true)

async function loadList() {
  loading.value = true
  try {
    list.value = await request.get('/volunteer/pets/reviewed')
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function onTabChange(tab) {
  if (tab === 'pending') router.push('/volunteer/pending')
  else if (tab === 'add') router.push('/volunteer/visits/add')
  else if (tab === 'visits') router.push('/volunteer/visits')
}

onMounted(loadList)
</script>

<style scoped>
.history-page {
  max-width: 800px;
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
  width: 80px;
  height: 80px;
  background: #f5f7fa;
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
  font-size: 15px;
  color: #303133;
}
.pet-meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
</style>
