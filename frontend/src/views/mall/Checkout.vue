<template>
  <div class="checkout-page">
    <h3 class="page-title">确认订单</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="cartList.length > 0">
      <!-- 收货信息 -->
      <el-card class="section-card">
        <template #header><span>收货信息</span></template>
        <el-form
          ref="formRef"
          :model="receiver"
          :rules="rules"
          label-width="80px"
        >
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="receiver.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="receiverPhone">
            <el-input v-model="receiver.receiverPhone" placeholder="请输入收货人手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="收货地址" prop="receiverAddress">
            <el-input
              v-model="receiver.receiverAddress"
              type="textarea"
              :rows="3"
              placeholder="请输入详细收货地址"
            />
          </el-form-item>
        </el-form>
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
          <el-button class="back-cart-btn" @click="$router.back()">返回购物车</el-button>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getCart, createOrder } from '@/api/mall'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const loading = ref(true)

const cartList = ref([])

const receiver = ref({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: ''
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const totalAmount = computed(() => {
  return cartList.value
    .reduce((sum, i) => sum + parseFloat(i.subtotal || 0), 0)
    .toFixed(2)
})

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
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const order = await createOrder({
      receiverName: receiver.value.receiverName,
      receiverPhone: receiver.value.receiverPhone,
      receiverAddress: receiver.value.receiverAddress
    })
    ElMessage.success('订单提交成功')
    router.push('/mall/pay/' + order.id)
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadCart)
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
