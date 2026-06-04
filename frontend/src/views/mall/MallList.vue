<template>
  <div class="mall-page">
    <h3 class="page-title">宠物商城</h3>

    <!-- 搜索框 -->
    <div class="search-box">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称或描述"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 分类 Tabs（桌面端） -->
    <div class="category-tabs-desktop">
      <el-tabs v-model="categoryId" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane
          v-for="c in categories"
          :key="c.id"
          :label="c.name"
          :name="c.id"
        />
      </el-tabs>
    </div>

    <!-- 移动端分类筛选按钮 + 底部弹窗 -->
    <div class="category-filter-mobile">
      <el-button class="filter-btn" @click="filterDrawerOpen = true">
        <el-icon><Operation /></el-icon>
        {{ currentCategoryLabel }}
        <el-icon><ArrowDown /></el-icon>
      </el-button>
      <el-drawer
        v-model="filterDrawerOpen"
        direction="btt"
        size="auto"
        :with-header="false"
        :append-to-body="true"
      >
        <div class="filter-drawer">
          <div class="filter-drawer-header">
            <span class="filter-drawer-title">筛选分类</span>
            <el-icon class="filter-drawer-close" :size="20" @click="filterDrawerOpen = false"><Close /></el-icon>
          </div>
          <div class="filter-drawer-list">
            <div
              class="filter-drawer-item"
              :class="{ active: categoryId === 'all' }"
              @click="selectCategory('all')"
            >
              全部
              <el-icon v-if="categoryId === 'all'" color="var(--yc-accent)"><Select /></el-icon>
            </div>
            <div
              v-for="c in categories"
              :key="c.id"
              class="filter-drawer-item"
              :class="{ active: categoryId === c.id }"
              @click="selectCategory(c.id)"
            >
              {{ c.name }}
              <el-icon v-if="categoryId === c.id" color="var(--yc-accent)"><Select /></el-icon>
            </div>
          </div>
        </div>
      </el-drawer>
    </div>

    <!-- 商品网格 -->
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="productList.length === 0" class="empty-tip">
        <el-empty description="暂无商品" />
      </div>

      <div v-else class="product-grid">
        <el-card
          v-for="p in productList"
          :key="p.id"
          :body-style="{ padding: '0' }"
          shadow="hover"
          class="product-card"
          @click="goDetail(p.id)"
        >
          <el-image
            :src="(p.image || '').split(',')[0]"
            fit="cover"
            style="width:100%;height:180px"
          >
            <template #error>
              <div class="img-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div class="product-info">
            <h4 class="product-name">{{ p.name }}</h4>
            <div class="product-bottom">
              <span class="price">¥{{ p.price }}</span>
              <span class="stock">库存 {{ p.stock }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <Pagination
        :total="total"
        :page="page"
        :size="size"
        @change="onPageChange"
      />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, Search, Operation, ArrowDown, Close, Select } from '@element-plus/icons-vue'
import { getMallCategories, getMallProducts } from '@/api/mall'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()

const categories = ref([])
const productList = ref([])
const total = ref(0)
const loading = ref(true)
const categoryId = ref('all')
const keyword = ref('')
const page = ref(1)
const size = ref(12)
const filterDrawerOpen = ref(false)

// 当前选中的分类名称
const currentCategoryLabel = computed(() => {
  if (categoryId.value === 'all') return '全部分类'
  const cat = categories.value.find(c => c.id === categoryId.value)
  return cat?.name || '全部分类'
})

function selectCategory(id) {
  categoryId.value = id
  filterDrawerOpen.value = false
  handleTabChange()
}

async function loadCategories() {
  try {
    categories.value = await getMallCategories()
  } catch {
    // 请求拦截器统一处理
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (categoryId.value && categoryId.value !== 'all') params.categoryId = categoryId.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getMallProducts(params)
    productList.value = res.list || []
    total.value = res.total || 0
  } catch {
    productList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadProducts()
}

function handleTabChange() {
  page.value = 1
  loadProducts()
}

function onPageChange({ page: p, size: s }) {
  page.value = p
  size.value = s
  loadProducts()
}

function goDetail(id) {
  router.push('/mall/products/' + id)
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.mall-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}
.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.search-box .el-input {
  max-width: 360px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.product-card {
  cursor: pointer;
  transition: transform 0.2s;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
}
.product-card:hover {
  transform: translateY(-4px);
  border-color: var(--yc-border-hover);
}
.img-placeholder {
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--yc-bg-card);
  color: var(--yc-text-tertiary);
  font-size: 14px;
}
.product-info {
  padding: 12px 16px;
}
.product-name {
  margin: 0 0 8px;
  font-size: 15px;
  color: var(--yc-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
}
.stock {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}

/* Tabs 下划线暖色 */
:deep(.el-tabs__active-bar) {
  background: var(--yc-accent);
}
:deep(.el-tabs__item.is-active) {
  color: var(--yc-text-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--yc-accent);
}

/* 移动端分类筛选按钮（默认隐藏） */
.category-filter-mobile {
  display: none;
  margin-bottom: 16px;
}
.filter-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: var(--yc-radius-btn);
  border: 1px solid var(--yc-border);
  font-size: 14px;
}

/* 分类筛选底部弹窗 */
.filter-drawer {
  padding: 0 0 16px;
}
.filter-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f0f2f5;
}
.filter-drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.filter-drawer-close {
  cursor: pointer;
  color: #909399;
}
.filter-drawer-list {
  max-height: 50vh;
  overflow-y: auto;
  padding: 8px 0;
}
.filter-drawer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  font-size: 15px;
  color: #303133;
  cursor: pointer;
  transition: background 0.15s;
}
.filter-drawer-item:hover {
  background: #f5f7fa;
}
.filter-drawer-item.active {
  color: var(--yc-accent);
  font-weight: 600;
  background: rgba(139,184,160,0.08);
}
</style>
