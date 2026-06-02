<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">领养管理</h3>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <el-card>
      <div class="toolbar">
        <el-select
          v-model="statusFilter"
          placeholder="全部状态"
          clearable
          style="width:130px"
          popper-class="auto-close-popper"
          :ref="(el) => setSelectRef('_self', el)"
          @change="handleSearch"
          @visible-change="(v) => onSelectVisible(v, '_self')"
        >
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="申请人" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :src="row.userAvatar" :size="28" />
              <span>{{ row.userNickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userPhone" label="手机号" width="130" />
        <el-table-column label="宠物" width="140">
          <template #default="{ row }">
            <div class="pet-cell">
              <el-image :src="row.petCoverImage" fit="cover" style="width:36px;height:36px;border-radius:4px">
                <template #error><div class="img-xs" /></template>
              </el-image>
              <span class="pet-name">{{ row.petName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="livingEnv" label="居住环境" min-width="120" show-overflow-tooltip />
        <el-table-column prop="petExp" label="养宠经验" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="APPLY_STATUS[row.status]?.type || 'info'" size="small">
              {{ APPLY_STATUS[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push('/admin/adopt-applications/' + row.id)">查看详情</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="success"
              @click="handleReview(row, 'APPROVED')"
            >通过</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="handleReview(row, 'REJECTED')"
            >拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllApplications, adminReviewApplication } from '@/api/admin'
import { APPLY_STATUS } from '@/utils/constants'
import { exportToCSV } from '@/utils/export'
import Pagination from '@/components/Pagination.vue'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getAllApplications(params)
    list.value = res.list || []
    total.value = res.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; loadList() }
function onPageChange({ page: p, size: s }) { page.value = p; size.value = s; loadList() }

async function handleReview(row, action) {
  const label = action === 'APPROVED' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定${label}该领养申请？`, '提示')
    await adminReviewApplication(row.id, action)
    ElMessage.success('审核完成')
    loadList()
  } catch { /* ignore */ }
}

async function handleExport() {
  try {
    const res = await getAllApplications({ page: 1, size: 999999, status: statusFilter.value || undefined })
    const data = (res.list || []).map(r => ({
      '申请ID': r.id,
      '申请人': r.userNickname,
      '手机号': r.userPhone,
      '宠物名称': r.petName,
      '居住环境': r.livingEnv,
      '养宠经验': r.petExp,
      '承诺': r.commitment,
      '状态': APPLY_STATUS[r.status]?.label || r.status,
      '申请时间': r.createdAt
    }))
    exportToCSV(data, '领养申请列表.csv')
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(loadList)
onUnmounted(cleanupSelectAutoClose)
</script>

<style scoped>
.admin-page { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: #303133; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.user-cell { display: flex; align-items: center; gap: 8px; }
.pet-cell { display: flex; align-items: center; gap: 8px; }
.pet-name { font-size: 13px; }
.img-xs { width: 36px; height: 36px; background: #f5f7fa; border-radius: 4px; }
</style>
