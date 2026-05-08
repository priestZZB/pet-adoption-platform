<template>
  <div class="admin-page">
    <h3 class="page-title">宠物管理</h3>

    <el-card>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:130px" @change="handleSearch">
          <el-option label="待初审" value="PENDING" />
          <el-option label="待终审" value="FIRST_PASS" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已打回" value="REJECTED" />
          <el-option label="已领养" value="ADOPTED" />
          <el-option label="已下架" value="OFFLINE" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索宠物名称" clearable style="width:200px" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
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
            <el-button v-if="row.status === 'FIRST_PASS'" type="success" size="small" @click="handleFinal(row, 'APPROVED')">终审通过</el-button>
            <el-button v-if="row.status === 'FIRST_PASS'" type="danger" size="small" @click="handleFinal(row, 'REJECTED')">终审打回</el-button>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllPets, updatePetStatus, finalReview } from '@/api/admin'
import { PET_STATUS } from '@/utils/constants'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const statusFilter = ref('')
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

async function handleFinal(row, action) {
  await ElMessageBox.confirm(`确定${action === 'APPROVED' ? '通过' : '打回'}终审？`, '提示')
  await finalReview(row.id, { action })
  ElMessage.success('终审完成')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.img-xs { width: 40px; height: 40px; background: #f5f7fa; border-radius: 4px; }
</style>
