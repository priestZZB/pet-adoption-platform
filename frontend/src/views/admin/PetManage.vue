<template>
  <div class="admin-page">
    <h3 class="page-title">宠物管理</h3>

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
          <el-option label="待初审" value="PENDING" />
          <el-option label="待终审" value="FIRST_PASS" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已打回" value="REJECTED" />
          <el-option label="已领养" value="ADOPTED" />
          <el-option label="已下架" value="OFFLINE" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索宠物名称" clearable style="width:200px" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleExport">导出CSV</el-button>
      </div>

      <div v-if="selectedIds.length > 0" class="batch-bar">
        <span class="batch-tip">已选 {{ selectedIds.length }} 项</span>
        <el-button type="success" @click="handleBatchStatus('APPROVED')">批量上架</el-button>
        <el-button type="info" @click="handleBatchStatus('OFFLINE')">批量下架</el-button>
        <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
        <el-button @click="selectedIds = []">取消选择</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="封面" width="60">
          <template #default="{ row }">
            <el-image :src="row.coverImage" fit="cover" style="width:40px;height:40px;border-radius:4px">
              <template #error><div class="img-xs" /></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="100" />
        <el-table-column prop="userNickname" label="送养人" width="100" />
        <el-table-column prop="categoryName" label="分类" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="PET_STATUS[row.status]?.type || 'info'" size="small">
              {{ PET_STATUS[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="160" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button v-if="row.status === 'FIRST_PASS'" type="warning" size="small" @click="$router.push('/admin/review/' + row.id)">去审核</el-button>
            <el-button v-if="row.status === 'PENDING'" type="info" size="small" disabled>待初审</el-button>
            <el-button size="small" @click="handleStatus(row, row.status === 'OFFLINE' ? 'APPROVED' : 'OFFLINE')">
              {{ row.status === 'OFFLINE' ? '上架' : '下架' }}
            </el-button>
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
import { getAllPets, updatePetStatus, batchUpdatePetStatus, batchDeletePets } from '@/api/admin'
import { PET_STATUS } from '@/utils/constants'
import { exportToCSV } from '@/utils/export'
import Pagination from '@/components/Pagination.vue'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const selectedIds = ref([])
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getAllPets(params)
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

async function handleStatus(row, status) {
  await ElMessageBox.confirm(`确定${status === 'OFFLINE' ? '下架' : '上架'}该宠物？`, '提示')
  await updatePetStatus(row.id, status)
  ElMessage.success('操作成功')
  loadList()
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchStatus(status) {
  if (!selectedIds.value.length) return
  const label = status === 'APPROVED' ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定批量${label}所选 ${selectedIds.value.length} 个宠物？`, '提示')
    await batchUpdatePetStatus({ ids: selectedIds.value, status })
    ElMessage.success('批量' + label + '成功')
    selectedIds.value = []
    loadList()
  } catch { /* ignore */ }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定批量删除所选 ${selectedIds.value.length} 个宠物？（软删除，可恢复）`, '警告')
    await batchDeletePets({ ids: selectedIds.value })
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadList()
  } catch { /* ignore */ }
}

async function handleExport() {
  try {
    const params = { page: 1, size: 999999 }
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value) params.status = statusFilter.value
    const res = await getAllPets(params)
    const data = (res.list || []).map(r => ({
      'ID': r.id,
      '名称': r.name,
      '送养人': r.userNickname,
      '分类': r.categoryName,
      '状态': PET_STATUS[r.status]?.label || r.status,
      '发布时间': r.createdAt
    }))
    exportToCSV(data, '宠物列表.csv')
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(loadList)
onUnmounted(cleanupSelectAutoClose)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.batch-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; background: #f0f9ff; border-radius: 6px; margin-bottom: 12px;
}
.batch-tip { font-size: 13px; color: #409eff; font-weight: 500; margin-right: 8px; }
.img-xs { width: 40px; height: 40px; background: #f5f7fa; border-radius: 4px; }
</style>
