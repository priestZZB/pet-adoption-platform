<template>
  <div class="admin-page">
    <h3 class="page-title">订单管理</h3>

    <el-card>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:130px" @change="loadList">
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
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PAID'" type="primary" size="small" @click="openShip(row)">发货</el-button>
            <el-button size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </el-card>

    <el-dialog v-model="shipVisible" title="发货" width="400px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="物流单号" required>
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
        <el-form-item label="物流状态">
          <el-input v-model="shipForm.logisticsStatus" placeholder="如：已揽件、运输中" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="handleShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllOrders, getAdminOrderDetail, shipOrder } from '@/api/admin'
import { ORDER_STATUS } from '@/utils/constants'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')
const loading = ref(false)
const shipVisible = ref(false)
const shipOrderId = ref(null)
const shipping = ref(false)
const shipForm = reactive({ logisticsNo: '', logisticsStatus: '' })

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

function openShip(row) {
  shipOrderId.value = row.id
  shipForm.logisticsNo = ''
  shipForm.logisticsStatus = ''
  shipVisible.value = true
}

async function handleShip() {
  if (!shipForm.logisticsNo) { ElMessage.warning('请输入物流单号'); return }
  shipping.value = true
  try {
    await shipOrder(shipOrderId.value, { logisticsNo: shipForm.logisticsNo, logisticsStatus: shipForm.logisticsStatus })
    ElMessage.success('已发货')
    shipVisible.value = false; loadList()
  } finally { shipping.value = false }
}

async function showDetail(row) { /* 可弹窗显示订单商品明细 */ }

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.toolbar { margin-bottom: 16px; }
</style>
