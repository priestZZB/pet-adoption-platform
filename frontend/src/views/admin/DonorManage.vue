<template>
  <div class="admin-page">
    <h3 class="page-title">送养人审核</h3>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="申请状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.donorStatus === 'PENDING' ? 'warning' : 'info'">
              {{ row.donorStatus === 'PENDING' ? '待审核' : row.donorStatus === 'APPROVED' ? '已通过' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.donorStatus === 'PENDING'"
              type="success"
              size="small"
              :loading="reviewingId === row.id"
              @click="handleReview(row, 'APPROVED')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.donorStatus === 'PENDING'"
              type="danger"
              size="small"
              :loading="reviewingId === row.id"
              @click="handleReview(row, 'REJECTED')"
            >
              驳回
            </el-button>
            <el-tag v-else type="info" size="small">已处理</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDonorApplies, reviewDonor } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const reviewingId = ref(null)

async function loadList() {
  loading.value = true
  try {
    list.value = await getDonorApplies()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function handleReview(row, action) {
  reviewingId.value = row.id
  try {
    await reviewDonor(row.id, { action })
    ElMessage.success(action === 'APPROVED' ? '已通过' : '已驳回')
    loadList()
  } catch {
    // 请求拦截器统一处理
  } finally {
    reviewingId.value = null
  }
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 900px; }
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}
</style>
