<template>
  <div class="checkout-page">
    <h3 class="page-title">确认订单</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="cartList.length > 0">
      <!-- 收货信息 -->
      <el-card class="section-card">
        <template #header>
          <div class="section-header">
            <span>收货信息</span>
            <el-button class="select-addr-btn" size="small" @click="goSelectAddress">选择收货地址</el-button>
          </div>
        </template>

        <div v-if="receiverName" class="addr-display">
          <div class="addr-top">
            <span class="addr-name">{{ receiverName }}</span>
            <span class="addr-phone">{{ receiverPhone }}</span>
          </div>
          <p class="addr-detail">{{ receiverAddress }}</p>
          <!-- 保留隐藏表单用于校验 -->
          <el-form ref="formRef" :model="formData" :rules="rules" label-width="0" class="hidden-form">
            <el-form-item prop="receiverName">
              <el-input v-model="formData.receiverName" style="display:none" />
            </el-form-item>
            <el-form-item prop="receiverPhone">
              <el-input v-model="formData.receiverPhone" style="display:none" />
            </el-form-item>
            <el-form-item prop="receiverAddress">
              <el-input v-model="formData.receiverAddress" style="display:none" />
            </el-form-item>
          </el-form>
        </div>
        <div v-else class="addr-empty">
          <p>请选择收货地址</p>
          <el-button class="go-select-btn" @click="goSelectAddress">去选择</el-button>
        </div>
      </el-card>

      <!-- 商品清单 -->
      <el-card class="section-card">
        <template #header><span>商品清单</span></template>
        <div class="order-items">
          <div v-for="item in cartList" :key="item.productId" class="order-item">
            <el-image
              :src="item.productImage"
              fit="cover"
              style="width:60px;height:60px;border-radius:4px"
            >
              <template #error>
                <div class="img-placeholder-xs" />
              </template>
            </el-image>
            <div class="item-info">
              <span class="item-name">{{ item.productName }}</span>
              <span class="item-price">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
            <span class="item-subtotal">¥{{ item.subtotal }}</span>
          </div>
        </div>
      </el-card>

      <!-- 金额汇总 -->
      <el-card class="section-card">
        <div class="summary">
          <div class="summary-row">
            <span>商品数量</span>
            <span>{{ cartList.length }} 件</span>
          </div>
          <div class="summary-row total">
            <span>合计</span>
            <span class="total-price">¥{{ totalAmount }}</span>
          </div>
        </div>
        <div style="text-align:right;margin-top:16px">
          <el-button class="back-cart-btn" @click="$router.push('/mall/cart')">返回购物车</el-button>
          <el-button
            class="submit-order-btn"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交订单
          </el-button>
        </div>
      </el-card>
    </template>

    <template v-else>
      <div class="empty-tip">
        <el-empty description="购物车为空，请先选购商品">
          <el-button type="primary" @click="$router.push('/mall')">去商城</el-button>
        </el-empty>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getCart, createOrder, getDefaultAddress } from '@/api/mall'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const loading = ref(true)

const cartList = ref([])

// 收货地址 - 从 query 或默认地址加载
const addressId = ref(null)
const receiverName = ref('')
const receiverPhone = ref('')
const receiverAddress = ref('')

// 隐藏表单数据用于校验
const formData = computed(() => ({
  receiverName: receiverName.value,
  receiverPhone: receiverPhone.value,
  receiverAddress: receiverAddress.value
}))

const rules = {
  receiverName: [{ required: true, message: '请选择收货地址', trigger: 'change' }],
  receiverPhone: [{ required: true, message: '请选择收货地址', trigger: 'change' }],
  receiverAddress: [{ required: true, message: '请选择收货地址', trigger: 'change' }]
}

const totalAmount = computed(() => {
  return cartList.value
    .reduce((sum, i) => sum + parseFloat(i.subtotal || 0), 0)
    .toFixed(2)
})

function goSelectAddress() {
  router.push('/mall/address-select?mode=select')
}

// 从 route.query 获取选中的地址
function loadAddressFromQuery() {
  if (route.query.addressId) {
    addressId.value = Number(route.query.addressId)
    receiverName.value = route.query.receiverName || ''
    receiverPhone.value = route.query.receiverPhone || ''
    receiverAddress.value = route.query.receiverAddress || ''
  }
}

// 自动加载默认地址
async function loadDefaultAddress() {
  try {
    const addr = await getDefaultAddress()
    if (addr) {
      addressId.value = addr.id
      receiverName.value = addr.receiverName
      receiverPhone.value = addr.receiverPhone
      receiverAddress.value = addr.receiverAddress
    }
  } catch {
    // 没有默认地址也没关系
  }
}

async function loadCart() {
  loading.value = true
  try {
    cartList.value = await getCart()
  } catch {
    cartList.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!receiverName.value || !receiverPhone.value || !receiverAddress.value) {
    ElMessage.warning('请先选择收货地址')
    return
  }

  submitting.value = true
  try {
    const order = await createOrder({
      addressId: addressId.value,
      receiverName: receiverName.value,
      receiverPhone: receiverPhone.value,
      receiverAddress: receiverAddress.value
    })
    ElMessage.success('订单提交成功')
    router.push('/mall/pay/' + order.id)
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// 监听路由 query 变化（从地址选择页返回时）
watch(
  () => route.query,
  () => {
    if (route.query.addressId) {
      loadAddressFromQuery()
    }
  }
)

onMounted(async () => {
  await loadCart()
  if (route.query.addressId) {
    loadAddressFromQuery()
  } else {
    await loadDefaultAddress()
  }
})
</script>

<style scoped>
.checkout-page {
  max-width: 800px;
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

.section-card {
  margin-bottom: 16px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.select-addr-btn {
  border: 1px solid #c19a6b;
  color: #c19a6b;
  border-radius: 8px;
}
.select-addr-btn:hover {
  background: #fff5e8;
}

.addr-display {
  padding: 4px 0;
}
.addr-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
.addr-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.addr-phone {
  font-size: 14px;
  color: #666;
}
.addr-detail {
  font-size: 14px;
  color: #555;
  margin: 0;
}

.addr-empty {
  text-align: center;
  padding: 20px 0;
  color: #999;
}
.go-select-btn {
  background: #c19a6b;
  border: 1px solid #c19a6b;
  color: #fff;
  border-radius: 12px;
}
.go-select-btn:hover {
  background: #b0895a;
  border-color: #b0895a;
}

.hidden-form {
  display: none;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}
.order-item + .order-item {
  border-top: 1px solid var(--yc-border);
}
.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.item-name {
  font-size: 14px;
  color: var(--yc-text-primary);
}
.item-price {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.item-subtotal {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 500;
}

.img-placeholder-xs {
  width: 60px;
  height: 60px;
  background: var(--yc-bg-card);
  border-radius: 4px;
}

.summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--yc-text-secondary);
}
.summary-row.total {
  font-size: 16px;
  color: var(--yc-text-primary);
  font-weight: 500;
  border-top: 1px solid var(--yc-border);
  padding-top: 8px;
}
.total-price {
  font-size: 24px;
  color: #F56C6C;
  font-weight: bold;
}

/* 按钮暖色 */
:deep(.submit-order-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  padding: 12px 28px;
  font-size: 15px;
}
:deep(.submit-order-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.back-cart-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.back-cart-btn:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
</style>
