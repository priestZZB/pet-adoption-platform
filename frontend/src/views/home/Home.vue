<template>
  <div class="home-page">
    <!-- Banner 轮播 -->
    <div class="banner-container">
    <el-carousel height="100%" class="banner" :interval="5000">
      <el-carousel-item v-for="(item, idx) in banners" :key="idx">
        <div class="banner-item">
          <el-image
            :src="item.imageUrl"
            fit="cover"
            style="width:100%;height:100%"
          />
        </div>
      </el-carousel-item>
    </el-carousel>
    </div>

    <!-- 搜索 + 筛选栏 -->
    <div class="toolbar">
      <div class="toolbar-inner">
        <el-select
          v-model="query.categoryId"
          placeholder="全部分类"
          clearable
          style="width:160px"
          @change="handleSearch"
        >
          <el-option
            v-for="c in categories"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>

        <el-input
          v-model="query.keyword"
          placeholder="搜索宠物名称…"
          :prefix-icon="Search"
          clearable
          style="width:300px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />

        <el-button type="primary" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
      </div>
    </div>

    <!-- 宠物卡片网格 -->
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="petList.length === 0" class="empty-tip">
        <el-empty description="暂无待领养宠物" />
      </div>

      <div v-else class="pet-grid">
        <el-card
          v-for="pet in petList"
          :key="pet.id"
          :body-style="{ padding: '0' }"
          shadow="hover"
          class="pet-card"
          @click="goDetail(pet.id)"
        >
          <div class="pet-cover">
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
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <Pagination
        :total="total"
        :page="query.page"
        :size="query.size"
        @change="onPageChange"
      />
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Loading } from '@element-plus/icons-vue'
import { getCategories, getPetList } from '@/api/pet'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'
import request from '@/api/request'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()

const categories = ref([])
const petList = ref([])
const total = ref(0)
const loading = ref(true)

const query = reactive({
  page: 1,
  size: 12,
  categoryId: null,
  keyword: ''
})

// 欢迎页（固定写死，放在轮播图最前面，使用本地图片）
const WELCOME_IMAGE = '/uploads/banner/welcome.jpg'
const welcomeBanner = {
  imageUrl: WELCOME_IMAGE
}

// 从后端加载的轮播图
const apiBanners = ref([])
const banners = ref([welcomeBanner])

async function loadBanners() {
  try {
    const res = await request.get('/banners')
    if (res && res.length > 0) {
      // 欢迎页 + 后端轮播图拼接
      banners.value = [
        welcomeBanner,
        ...res.map(b => ({ imageUrl: b.imageUrl }))
      ]
    } else {
      // 没有轮播图时只显示欢迎页
      banners.value = [welcomeBanner]
    }
  } catch {
    banners.value = [welcomeBanner]
  }
}

// 加载分类
async function loadCategories() {
  try {
    categories.value = await getCategories()
  } catch {
    // 请求拦截器统一处理
  }
}

// 加载宠物列表
async function loadPets() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.keyword) params.keyword = query.keyword
    const res = await getPetList(params)
    petList.value = res.list || []
    total.value = res.total || 0
  } catch {
    petList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadPets()
}

function onPageChange({ page, size }) {
  query.page = page
  query.size = size
  loadPets()
}

function goDetail(id) {
  router.push('/pets/' + id)
}

onMounted(() => {
  loadBanners()
  loadCategories()
  loadPets()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

/* Banner */
.banner-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto 24px;
  aspect-ratio: 2279 / 1280;
  border-radius: 12px;
  overflow: hidden;
}
.banner {
  height: 100% !important;
  border-radius: 12px;
}
.banner-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 工具栏 */
.toolbar {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}
.toolbar-inner {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 宠物卡片网格 */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.pet-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.pet-card:hover {
  transform: translateY(-4px);
}
.pet-cover {
  position: relative;
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
}
.dot {
  margin: 0 4px;
}

/* 加载 & 空状态 */
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}


</style>
