<template>
  <div class="admin-page">
    <h3 class="page-title">订单管理</h3>

    <el-card>
      <div class="toolbar">
        <el-select
          v-model="statusFilter"
          placeholder="全部状态"
          clearable
          style="width:130px"
          popper-class="auto-close-popper"
          :ref="(el) => setSelectRef('_self', el)"
          @change="loadList"
          @visible-change="(v) => onSelectVisible(v, '_self')"
        >
          <el-option label="待支付" value="PENDING_PAY" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已发货" value="SHIPPED" />
          <el-option label="已完成" value="RECEIVED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="receiverName" label="收货人" width="90" />
        <el-table-column prop="totalAmount" label="金额" width="90">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="ORDER_STATUS[row.status]?.type || 'info'" size="small">
              {{ ORDER_STATUS[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logisticsNo" label="物流单号" width="150" />
        <el-table-column prop="createdAt" label="下单时间" width="160" />
        <el-table-column label="操作" width="190">
          <template #default="{ row }">
            <div class="action-group">
              <el-button v-if="row.status === 'PAID'" type="primary" size="small" @click="openShip(row)">发货</el-button>
              <el-button size="small" @click="showDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </el-card>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipVisible" title="发货" width="400px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="快递公司" required>
          <el-select
            v-model="shipForm.courierCompany"
            placeholder="请选择快递公司"
            style="width:100%"
            popper-class="auto-close-popper"
            :ref="(el) => setSelectRef('_ship', el)"
            @change="handleCompanyChange"
            @visible-change="(v) => onSelectVisible(v, '_ship')"
          >
            <el-option
              v-for="c in courierOptions"
              :key="c.value"
              :label="c.label"
              :value="c.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input :model-value="generatedNo" disabled placeholder="选择快递公司后自动生成" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="handleShip">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <template v-if="detailData">
        <div class="detail-section">
          <h4>收货信息</h4>
          <p>收货人：{{ detailData.receiverName }}</p>
          <p>手机号：{{ detailData.receiverPhone }}</p>
          <p>收货地址：{{ detailData.receiverAddress }}</p>
        </div>
        <el-divider />
        <div class="detail-section">
          <h4>物流信息</h4>
          <p>物流单号：{{ detailData.logisticsNo || '暂无' }}</p>
          <p>物流状态：{{ detailData.logisticsStatus || '暂无' }}</p>
        </div>
        <el-divider />
        <div class="detail-section">
          <h4>商品清单</h4>
          <div v-for="item in detailData.items" :key="item.id" class="detail-item">
            <el-image
              :src="item.productImage"
              fit="cover"
              style="width:50px;height:50px;border-radius:4px"
            >
              <template #error><div class="img-xs" /></template>
            </el-image>
            <div class="detail-item-info">
              <span>{{ item.productName }}</span>
              <span class="detail-item-meta">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
            <span class="detail-item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
        <el-divider />
        <div class="detail-total">
          <span>订单编号：{{ detailData.orderNo }}</span>
          <span class="detail-total-price">合计：¥{{ detailData.totalAmount }}</span>
        </div>
      </template>
      <div v-else class="loading-center">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getAllOrders, getAdminOrderDetail, shipOrder } from '@/api/admin'
import { ORDER_STATUS } from '@/utils/constants'
import Pagination from '@/components/Pagination.vue'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

const courierOptions = [
  { label: '顺丰快递', value: '顺丰快递' },
  { label: '圆通快递', value: '圆通快递' },
  { label: '申通快递', value: '申通快递' },
  { label: '中通快递', value: '中通快递' },
  { label: 'EMS', value: 'EMS' },
  { label: '京东快递', value: '京东快递' },
  { label: '韵达快递', value: '韵达快递' },
  { label: '极兔快递', value: '极兔快递' }
]

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')
const loading = ref(false)
const shipVisible = ref(false)
const shipOrderId = ref(null)
const shipping = ref(false)
const shipForm = reactive({ courierCompany: '' })
const generatedNo = ref('')
const detailVisible = ref(false)
const detailData = ref(null)

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getAllOrders(params)
    list.value = res.list || []; total.value = res.total || 0
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

function onPageChange({ page: p, size: s }) { page.value = p; size.value = s; loadList() }

const COURIER_PREFIX = {
  '顺丰快递': 'SF', '圆通快递': 'YT', '申通快递': 'STO',
  '中通快递': 'ZTO', 'EMS': 'EMS', '京东快递': 'JD',
  '韵达快递': 'YD', '极兔快递': 'JT'
}
const COURIER_DIGITS = {
  '顺丰快递': 10, '圆通快递': 10, '申通快递': 9,
  '中通快递': 9, 'EMS': 10, '京东快递': 12,
  '韵达快递': 10, '极兔快递': 10
}

function generateNo(company) {
  const prefix = COURIER_PREFIX[company]
  const digits = COURIER_DIGITS[company]
  if (!prefix || !digits) return ''
  let no = prefix
  for (let i = 0; i < digits; i++) {
    no += Math.floor(Math.random() * 10)
  }
  return no
}

function handleCompanyChange(val) {
  generatedNo.value = generateNo(val)
}

function openShip(row) {
  shipOrderId.value = row.id
  shipForm.courierCompany = ''
  generatedNo.value = ''
  shipVisible.value = true
}

async function handleShip() {
  if (!shipForm.courierCompany) { ElMessage.warning('请选择快递公司'); return }
  shipping.value = true
  try {
    await shipOrder(shipOrderId.value, { courierCompany: shipForm.courierCompany })
    ElMessage.success('已发货')
    shipVisible.value = false; loadList()
  } finally { shipping.value = false }
}

async function showDetail(row) {
  detailData.value = null
  detailVisible.value = true
  try {
    const data = await getAdminOrderDetail(row.id)
    detailData.value = data
  } catch {
    detailData.value = null
  }
}

onMounted(loadList)
onUnmounted(cleanupSelectAutoClose)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.toolbar { margin-bottom: 16px; }
.img-xs { width: 50px; height: 50px; background: #f5f7fa; border-radius: 4px; }
.loading-center { display: flex; justify-content: center; padding: 20px 0; }

.detail-section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #303133;
}
.detail-section p {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
}
.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}
.detail-item + .detail-item {
  border-top: 1px solid #f5f7fa;
}
.detail-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 14px;
  color: #303133;
}
.detail-item-meta {
  font-size: 12px;
  color: #909399;
}
.detail-item-subtotal {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 500;
}
.detail-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}
.detail-total-price {
  font-size: 16px;
  color: #F56C6C;
  font-weight: bold;
}
.action-group {
  display: flex;
  gap: 4px;
  align-items: center;
}
</style>
