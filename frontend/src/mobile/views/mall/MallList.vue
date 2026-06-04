<template>
  <div class="mall-page">
    <!-- 搜索 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品..." :prefix-icon="Search" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
    </div>

    <!-- 分类横滑 -->
    <div class="category-row">
      <button :class="['cat-btn', { active: !query.categoryId }]" @click="selectCategory(null)">全部</button>
      <button v-for="c in categories" :key="c.id" :class="['cat-btn', { active: query.categoryId === c.id }]" @click="selectCategory(c.id)">{{ c.name }}</button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>

    <!-- 空 -->
    <MobileEmpty v-else-if="products.length === 0" description="暂无商品" />

    <!-- 商品网格 -->
    <div v-else class="product-grid">
      <MobileCard v-for="p in products" :key="p.id" clickable @click="goDetail(p.id)">
        <template #image><img :src="p.coverImage" class="prod-img" /></template>
        <h3>{{ p.name }}</h3>
        <div class="price-row">
          <span class="price">¥{{ p.price }}</span>
          <span v-if="p.originalPrice > p.price" class="orig-price">¥{{ p.originalPrice }}</span>
        </div>
      </MobileCard>
    </div>

    <div v-if="products.length < total" class="load-more">
      <button class="more-btn" :disabled="loadingMore" @click="loadMore">{{ loadingMore ? '加载中...' : '加载更多' }}</button>
    </div>
    <div class="bottom-spacer"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getMallCategories, getMallProducts } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const categories = ref([])
const products = ref([])
const total = ref(0)
const loading = ref(true)
const loadingMore = ref(false)
const keyword = ref('')

const query = reactive({ categoryId: null, page: 1, size: 10 })

async function loadCategories() { try { categories.value = await getMallCategories() } catch {} }

async function loadProducts(reset = false) {
  if (reset) { query.page = 1; loading.value = true }
  try {
    const params = { page: query.page, size: query.size }
    if (query.categoryId) params.categoryId = query.categoryId
    if (keyword.value) params.keyword = keyword.value
    const res = await getMallProducts(params)
    if (reset) { products.value = res.records || [] } else { products.value.push(...(res.records || [])) }
    total.value = res.total || 0
  } catch {} finally { loading.value = false; loadingMore.value = false }
}

function selectCategory(id) { query.categoryId = id; loadProducts(true) }
function handleSearch() { loadProducts(true) }
async function loadMore() { if (loadingMore.value) return; loadingMore.value = true; query.page++; await loadProducts(false) }
function goDetail(id) { router.push('/mall/products/' + id) }

onMounted(() => { loadCategories(); loadProducts(true) })
</script>

<style scoped>
.mall-page { padding: 12px 16px; }
.search-bar { margin-bottom: 8px; }
.search-bar :deep(.el-input__wrapper) { height: 42px; border-radius: 20px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
.category-row { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 8px; margin-bottom: 8px; -webkit-overflow-scrolling: touch; }
.category-row::-webkit-scrollbar { display: none; }
.cat-btn { flex-shrink: 0; height: 32px; padding: 0 14px; border: 1px solid #d1e7dd; border-radius: 16px; background: #fefaf5; color: #5a4a42; font-size: 13px; cursor: pointer; white-space: nowrap; }
.cat-btn.active { background: #8ab8a0; border-color: #8ab8a0; color: #fff; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.product-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.prod-img { width: 100%; aspect-ratio: 1; object-fit: cover; display: block; }
.product-grid :deep(h3) { font-size: 14px; margin-bottom: 6px; }
.price-row { display: flex; align-items: baseline; gap: 6px; }
.price { font-size: 16px; font-weight: 600; color: #e8564a; }
.orig-price { font-size: 12px; color: #b5a898; text-decoration: line-through; }
.load-more { text-align: center; padding: 20px 0; }
.more-btn { height: 40px; padding: 0 32px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.bottom-spacer { height: 80px; }
</style>
