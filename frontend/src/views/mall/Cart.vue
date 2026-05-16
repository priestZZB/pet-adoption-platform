<template>
  <div class="cart-page">
    <h3 class="page-title">购物车</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="cartList.length === 0" class="empty-tip">
        <el-empty description="购物车是空的">
          <el-button class="go-mall-btn" @click="$router.push('/mall')">去商城逛逛</el-button>
        </el-empty>
      </div>

      <template v-else>
        <!-- 购物车列表 -->
        <div class="cart-table">
          <div class="cart-header">
            <el-checkbox v-model="allChecked" @change="toggleAll">
              全选
            </el-checkbox>
            <span class="col-product">商品</span>
            <span class="col-price">单价</span>
            <span class="col-qty">数量</span>
            <span class="col-subtotal">小计</span>
            <span class="col-action">操作</span>
          </div>

          <div v-for="item in cartList" :key="item.productId" class="cart-row">
            <el-checkbox v-model="item.checked" @change="updateChecked" />
            <div class="col-product">
              <el-image
                :src="item.productImage"
                fit="cover"
                style="width:80px;height:80px;border-radius:6px"
              >
                <template #error>
                  <div class="img-placeholder-sm" />
                </template>
              </el-image>
              <span class="product-name">{{ item.productName }}</span>
            </div>
            <div class="col-price">¥{{ item.price }}</div>
            <div class="col-qty">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                size="small"
                style="width:120px"
                @change="(val) => handleQtyChange(item.productId, val)"
              />
            </div>
            <div class="col-subtotal">¥{{ item.subtotal }}</div>
            <div class="col-action">
              <el-button
                type="danger"
                size="small"
                link
                @click="handleRemove(item.productId)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="cart-footer">
          <el-button type="danger" size="small" plain @click="handleClear">
            清空购物车
          </el-button>
          <div class="footer-right">
            <span class="total-text">
              已选 <b>{{ checkedCount }}</b> 件，合计：
              <b class="total-price">¥{{ totalAmount }}</b>
            </span>
            <el-button
              class="checkout-btn"
              size="large"
              :disabled="checkedCount === 0"
              @click="goCheckout"
            >
              去结算
            </el-button>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getCart, updateCartItem, removeCartItem, clearCart } from '@/api/mall'

const router = useRouter()
const cartList = ref([])
const loading = ref(true)
const allChecked = ref(false)

// 计算选中项
const checkedItems = computed(() => cartList.value.filter(i => i.checked))
const checkedCount = computed(() => checkedItems.value.length)
const totalAmount = computed(() => {
  return checkedItems.value
    .reduce((sum, i) => sum + parseFloat(i.subtotal || 0), 0)
    .toFixed(2)
})

async function loadCart() {
  loading.value = true
  try {
    const list = await getCart()
    cartList.value = (list || []).map(i => ({ ...i, checked: true }))
    allChecked.value = cartList.value.length > 0
  } catch {
    cartList.value = []
  } finally {
    loading.value = false
  }
}

function toggleAll(val) {
  cartList.value.forEach(i => { i.checked = val })
}

function updateChecked() {
  allChecked.value = cartList.value.every(i => i.checked)
}

async function handleQtyChange(productId, quantity) {
  try {
    await updateCartItem(productId, quantity)
    const item = cartList.value.find(i => i.productId === productId)
    if (item) {
      item.quantity = quantity
      item.subtotal = (parseFloat(item.price) * quantity).toFixed(2)
    }
  } catch {
    loadCart()
  }
}

async function handleRemove(productId) {
  try {
    await removeCartItem(productId)
    cartList.value = cartList.value.filter(i => i.productId !== productId)
    updateChecked()
    ElMessage.success('已删除')
  } catch {
    // 请求拦截器统一处理
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定清空购物车？', '提示')
    await clearCart()
    cartList.value = []
    allChecked.value = false
    ElMessage.success('已清空')
  } catch {
    // 取消或失败
  }
}

function goCheckout() {
  router.push('/mall/checkout')
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page {
  max-width: 1000px;
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

.cart-table {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  overflow: hidden;
}
.cart-header,
.cart-row {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 12px;
}
.cart-header {
  background: var(--yc-bg-page);
  font-size: 14px;
  color: var(--yc-text-tertiary);
  font-weight: 500;
}
.cart-row {
  border-top: 1px solid var(--yc-border);
  font-size: 14px;
  color: var(--yc-text-primary);
}
.cart-row .el-checkbox {
  flex-shrink: 0;
}
.col-product {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.product-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.col-price,
.col-qty,
.col-subtotal,
.col-action {
  width: 120px;
  flex-shrink: 0;
  text-align: center;
}
.col-subtotal {
  color: #F56C6C;
  font-weight: 500;
}

.img-placeholder-sm {
  width: 80px;
  height: 80px;
  background: var(--yc-bg-card);
  border-radius: 6px;
}

/* 底部操作栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px 20px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
}
.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.total-text {
  font-size: 14px;
  color: var(--yc-text-primary);
}
.total-price {
  font-size: 20px;
  color: #F56C6C;
}

/* 按钮暖色 */
:deep(.go-mall-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.go-mall-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.checkout-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  padding: 12px 28px;
  font-size: 15px;
}
:deep(.checkout-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
</style>
