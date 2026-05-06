<template>
  <div class="detail-page">
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="product">
      <div class="detail-layout">
        <!-- 商品图片 -->
        <div class="detail-left">
          <el-image
            :src="product.image"
            fit="contain"
            style="width:100%;height:400px;border-radius:8px"
          >
            <template #error>
              <div class="img-placeholder">暂无图片</div>
            </template>
          </el-image>
        </div>

        <!-- 商品信息 -->
        <div class="detail-right">
          <h2 class="product-name">{{ product.name }}</h2>
          <p class="product-desc">{{ product.description || '暂无描述' }}</p>

          <el-divider />

          <div class="price-row">
            <span class="label">价格</span>
            <span class="price">¥{{ product.price }}</span>
          </div>

          <div class="info-row">
            <span class="label">分类</span>
            <span>{{ product.categoryName || '—' }}</span>
          </div>

          <div class="info-row">
            <span class="label">库存</span>
            <span :class="product.stock > 0 ? 'in-stock' : 'out-stock'">
              {{ product.stock > 0 ? '有货（' + product.stock + '件）' : '暂时缺货' }}
            </span>
          </div>

          <el-divider />

          <!-- 数量选择 + 加入购物车 -->
          <div class="action-row">
            <div class="quantity-wrapper">
              <span class="label">数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="Math.max(product.stock, 1)"
                size="large"
              />
            </div>
            <el-button
              type="primary"
              size="large"
              :icon="ShoppingCart"
              :disabled="product.stock <= 0"
              @click="handleAddCart"
            >
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="empty-tip">
        <el-empty description="商品不存在" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, ShoppingCart } from '@element-plus/icons-vue'
import { getMallProductDetail, addToCart } from '@/api/mall'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const quantity = ref(1)

async function loadDetail() {
  loading.value = true
  try {
    product.value = await getMallProductDetail(route.params.id)
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function handleAddCart() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch {
    // 请求拦截器统一处理
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.detail-layout {
  display: flex;
  gap: 40px;
}
.detail-left {
  flex: 1;
  min-width: 0;
}
.detail-right {
  width: 420px;
  flex-shrink: 0;
}
.img-placeholder {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
  border-radius: 8px;
}

.product-name {
  margin: 0 0 10px;
  font-size: 22px;
  color: #303133;
}
.product-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
}

.price-row,
.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.price-row .label,
.info-row .label {
  font-size: 14px;
  color: #909399;
  width: 50px;
  flex-shrink: 0;
}
.price {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}
.in-stock {
  color: #67C23A;
  font-size: 14px;
}
.out-stock {
  color: #F56C6C;
  font-size: 14px;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.quantity-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}
.quantity-wrapper .label {
  font-size: 14px;
  color: #909399;
}
</style>
