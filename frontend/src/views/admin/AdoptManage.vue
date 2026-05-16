<template>
  <div class="admin-page">
    <h3 class="page-title">领养管理</h3>

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
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userNickname" label="申请人" width="100" />
        <el-table-column prop="petName" label="宠物" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="APPLY_STATUS[row.status]?.type || 'info'" size="small">
              {{ APPLY_STATUS[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleReview(row.id, 'APPROVED')">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleReview(row.id, 'REJECTED')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </el-card>

    <el-dialog v-model="detailVisible" title="申请详情" width="500px">
      <div v-if="detail" class="detail-body">
        <div class="detail-row"><span class="label">申请人</span><span>{{ detail.userNickname }}</span></div>
        <div class="detail-row"><span class="label">宠物</span><span>{{ detail.petName }}</span></div>
        <div class="detail-row"><span class="label">居住环境</span><p>{{ detail.livingEnv }}</p></div>
        <div class="detail-row" v-if="detail.petExp"><span class="label">养宠经验</span><p>{{ detail.petExp }}</p></div>
        <div class="detail-row"><span class="label">领养承诺</span><p>{{ detail.commitment }}</p></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllApplications, getAdminAppDetail, adminReviewApplication } from '@/api/admin'
import { APPLY_STATUS } from '@/utils/constants'
import Pagination from '@/components/Pagination.vue'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getAllApplications(params)
    list.value = res.list || []
    total.value = res.total || 0
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

function onPageChange({ page: p, size: s }) { page.value = p; size.value = s; loadList() }

async function showDetail(id) {
  detail.value = await getAdminAppDetail(id)
  detailVisible.value = true
}

async function handleReview(id, action) {
  await ElMessageBox.confirm(`确定${action === 'APPROVED' ? '通过' : '拒绝'}该申请？`, '提示')
  await adminReviewApplication(id, action)
  ElMessage.success('操作成功'); loadList()
}

onMounted(loadList)
onUnmounted(cleanupSelectAutoClose)
</script>

<style scoped>
.admin-page { max-width: 1000px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.toolbar { margin-bottom: 16px; }
.detail-body { display: flex; flex-direction: column; gap: 12px; }
.detail-row { display: flex; gap: 12px; }
.detail-row .label { width: 80px; flex-shrink: 0; font-size: 14px; color: #909399; }
.detail-row p { margin: 0; font-size: 14px; color: #303133; line-height: 1.6; white-space: pre-wrap; }
</style>
