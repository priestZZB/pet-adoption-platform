<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索宠物名称..."
        :prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
    </div>

    <!-- 轮播 Banner -->
    <div class="banner-wrap" v-if="banners.length > 0">
      <el-carousel height="100%" :interval="5000">
        <el-carousel-item v-for="(item, idx) in banners" :key="idx">
          <el-image :src="item.imageUrl" fit="cover" style="width:100%;height:100%" />
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 分类筛选 -->
    <div class="category-row">
      <button
        :class="['cat-btn', { active: !query.categoryId }]"
        @click="query.categoryId = null; handleSearch()"
      >全部</button>
      <button
        v-for="c in categories"
        :key="c.id"
        :class="['cat-btn', { active: query.categoryId === c.id }]"
        @click="query.categoryId = c.id; handleSearch()"
      >{{ c.name }}</button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="loading-wrap">
      <i class="fas fa-spinner fa-pulse"></i>
      <span>加载中...</span>
    </div>

    <!-- 空状态 -->
    <MobileEmpty v-else-if="petList.length === 0" description="暂无待领养宠物" />

    <!-- 宠物网格 2 列 -->
    <div v-else class="pet-grid">
      <MobileCard
        v-for="pet in petList"
        :key="pet.id"
        clickable
        @click="goDetail(pet.id)"
      >
        <template #image>
          <img :src="pet.coverImage" class="pet-img" />
        </template>
        <h3>{{ pet.name }}</h3>
        <p>{{ pet.breed }} · {{ pet.age }}</p>
        <div class="pet-tags">
          <span class="tag">{{ genderMap[pet.gender] || '未知' }}</span>
          <span class="tag status-tag">{{ pet.status }}</span>
        </div>
      </MobileCard>
    </div>

    <!-- 加载更多 -->
    <div v-if="petList.length < total" class="load-more">
      <button class="more-btn" :disabled="loadingMore" @click="loadMore">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </button>
    </div>

    <!-- 底部留白 -->
    <div class="bottom-spacer"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getCategories, getPetList } from '@/api/pet'
import { GENDER_MAP } from '@/utils/constants'
import request from '@/api/request'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()

const WELCOME_IMAGE = '/images/welcome.jpg'
const banners = ref([{ imageUrl: WELCOME_IMAGE }])
const categories = ref([])
const petList = ref([])
const total = ref(0)
const loading = ref(true)
const loadingMore = ref(false)

const query = reactive({
  categoryId: null,
  keyword: '',
  page: 1,
  size: 10
})

const genderMap = GENDER_MAP || { 1: '公', 2: '母' }

async function loadBanners() {
  try {
    const r = await request.get('/banners')
    if (r && r.length > 0) {
      banners.value = [{ imageUrl: WELCOME_IMAGE }, ...r.map(b => ({ imageUrl: b.imageUrl }))]
    }
  } catch {}
}

async function loadCategories() {
  try { categories.value = await getCategories() } catch {}
}

async function loadPets(reset = false) {
  if (reset) {
    query.page = 1
    loading.value = true
  }
  try {
    const params = { page: query.page, size: query.size }
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.keyword) params.keyword = query.keyword
    const res = await getPetList(params)
    if (reset) {
      petList.value = res.records || []
    } else {
      petList.value.push(...(res.records || []))
    }
    total.value = res.total || 0
  } catch {} finally {
    loading.value = false
    loadingMore.value = false
  }
}

function handleSearch() {
  loadPets(true)
}

async function loadMore() {
  if (loadingMore.value) return
  loadingMore.value = true
  query.page++
  await loadPets(false)
}

function goDetail(id) {
  router.push('/pets/' + id)
}

onMounted(() => {
  loadBanners()
  loadCategories()
  loadPets(true)
})
</script>

<style scoped>
.home-page {
  padding: 12px 16px;
}

/* === 搜索 === */
.search-bar {
  margin-bottom: 8px;
}
.search-bar :deep(.el-input__wrapper) {
  height: 42px;
  border-radius: 20px;
  border: 1px solid #d1e7dd;
  box-shadow: none !important;
  background: #fefaf5;
}

/* === 轮播 === */
.banner-wrap {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
  background: #e8ddd0;
}
.banner-wrap :deep(.el-carousel) { height: 100% !important; }
.banner-wrap :deep(.el-carousel__container) { height: 100% !important; }
.banner-wrap :deep(.el-carousel__item) { height: 100% !important; }

/* === 分类按钮 === */
.category-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 8px;
  margin-bottom: 8px;
  -webkit-overflow-scrolling: touch;
}
.category-row::-webkit-scrollbar { display: none; }
.cat-btn {
  flex-shrink: 0;
  height: 32px;
  padding: 0 14px;
  border: 1px solid #d1e7dd;
  border-radius: 16px;
  background: #fefaf5;
  color: #5a4a42;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}
.cat-btn.active {
  background: #8ab8a0;
  border-color: #8ab8a0;
  color: #fff;
}

/* === 加载 === */
.loading-wrap {
  display: flex;
  flex-direction: column; align-items: center; gap: 8px;
  padding: 48px 0; color: #a09080; font-size: 14px;
}
.loading-wrap i { font-size: 24px; }

/* === 宠物网格 === */
.pet-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.pet-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
}
.pet-grid :deep(h3) { font-size: 15px; margin-bottom: 2px; }
.pet-grid :deep(p) { font-size: 12px; margin-bottom: 6px; }
.pet-tags { display: flex; gap: 6px; }
.tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f5f0e8;
  color: #a09080;
}
.status-tag { background: rgba(139,184,160,0.12); color: #8ab8a0; }

/* === 加载更多 === */
.load-more { text-align: center; padding: 20px 0; }
.more-btn {
  height: 40px; padding: 0 32px;
  border: 1px solid #d1e7dd; border-radius: 10px;
  background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer;
}
.more-btn:active { background: #f0e8dc; }
.more-btn:disabled { color: #b5a898; }

.bottom-spacer { height: 16px; }
</style>
