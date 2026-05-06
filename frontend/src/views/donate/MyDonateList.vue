<template>
  <div class="donate-list-page">
    <div class="page-header">
      <h3 class="page-title">我发布的宠物</h3>
      <el-button type="primary" @click="$router.push('/donate/publish')">
        发布新宠物
      </el-button>
    </div>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="还没有发布过宠物">
          <el-button type="primary" @click="$router.push('/donate/publish')">
            去发布
          </el-button>
        </el-empty>
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

            <div class="pet-actions">
              <el-button
                size="small"
                @click="$router.push('/donate/pets/' + pet.id + '/applications')"
              >
                查看申请
              </el-button>
              <el-button
                size="small"
                :disabled="pet.status === 'OFFLINE'"
                @click="handleOffline(pet.id)"
              >
                下架
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getMyPets, offlinePet } from '@/api/pet'
import { PET_STATUS, GENDER_MAP } from '@/utils/constants'

const list = ref([])
const loading = ref(true)

async function loadList() {
  loading.value = true
  try {
    list.value = await getMyPets()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function handleOffline(petId) {
  try {
    await ElMessageBox.confirm('确定下架该宠物？', '提示')
    await offlinePet(petId)
    ElMessage.success('已下架')
    loadList()
  } catch {
    // 取消或失败
  }
}

onMounted(loadList)
</script>

<style scoped>
.donate-list-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  font-size: 16px;
  color: #303133;
}
.pet-meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.pet-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}
</style>
