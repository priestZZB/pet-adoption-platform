<template>
  <div class="mall-page">
    <h3 class="page-title">宠物商城</h3>

    <!-- 分类 Tabs -->
    <el-tabs v-model="categoryId" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane
        v-for="c in categories"
        :key="c.id"
        :label="c.name"
        :name="c.id"
      />
    </el-tabs>

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { getMallCategories, getMallProducts } from '@/api/mall'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()

const categories = ref([])
const productList = ref([])
const total = ref(0)
const loading = ref(true)
const categoryId = ref(null)
const page = ref(1)
const size = ref(12)

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
</style>
