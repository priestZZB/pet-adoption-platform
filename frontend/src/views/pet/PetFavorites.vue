<template>
  <div class="favorites-page">
    <h3 class="page-title">我的收藏</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="还没有收藏的宠物" />
      </div>

      <div v-else class="pet-grid">
        <el-card
          v-for="pet in list"
          :key="pet.id"
          :body-style="{ padding: '0' }"
          shadow="hover"
          class="pet-card"
        >
          <div class="pet-cover" @click="goDetail(pet.id)">
            <el-image
              :src="pet.coverImage"
              fit="cover"
              style="width:100%;height:200px"
            >
              <template #error>
                <div class="img-placeholder">暂无图片</div>
              </template>
            </el-image>
            <el-tag
              :type="PET_STATUS[pet.status]?.type || 'info'"
              size="small"
              class="status-tag"
            >
              {{ PET_STATUS[pet.status]?.label || pet.status }}
            </el-tag>
          </div>
          <div class="pet-info">
            <h4 class="pet-name">{{ pet.name }}</h4>
            <div class="pet-meta">
              <span>{{ pet.categoryName }}</span>
              <span class="dot">·</span>
              <span>{{ pet.age }}</span>
              <span class="dot">·</span>
              <span>{{ GENDER_MAP[pet.gender] || pet.gender }}</span>
            </div>
            <div class="card-footer">
              <span class="donor">{{ pet.userNickname }}</span>
              <el-button
                type="danger"
                size="small"
                plain
                @click.stop="handleUnfavorite(pet.id)"
              >
                取消收藏
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getFavorites, unfavorite } from '@/api/pet'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'

const router = useRouter()
const list = ref([])
const loading = ref(true)

async function loadFavorites() {
  loading.value = true
  try {
    list.value = await getFavorites()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function handleUnfavorite(petId) {
  try {
    await unfavorite(petId)
    list.value = list.value.filter(p => p.id !== petId)
    ElMessage.success('已取消收藏')
  } catch {
    // 请求拦截器统一处理
  }
}

function goDetail(id) {
  router.push('/pets/' + id)
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorites-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 24px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.pet-card {
  transition: transform 0.2s;
}
.pet-card:hover {
  transform: translateY(-4px);
}
.pet-cover {
  position: relative;
  cursor: pointer;
}
.status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}
.img-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}
.pet-info {
  padding: 12px 16px;
}
.pet-name {
  margin: 0 0 6px;
  font-size: 16px;
  color: #303133;
}
.pet-meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}
.dot {
  margin: 0 4px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.donor {
  font-size: 12px;
  color: #909399;
}
</style>
