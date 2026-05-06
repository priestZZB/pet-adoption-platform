<template>
  <div class="admin-page">
    <h3 class="page-title">操作日志</h3>

    <el-card>
      <div class="toolbar">
        <el-select v-model="moduleFilter" placeholder="全部模块" clearable style="width:150px" @change="loadList">
          <el-option label="用户管理" value="用户管理" />
          <el-option label="宠物管理" value="宠物管理" />
          <el-option label="系统管理" value="系统管理" />
          <el-option label="志愿者管理" value="志愿者管理" />
          <el-option label="送养人管理" value="送养人管理" />
          <el-option label="文件上传" value="文件上传" />
          <el-option label="领养考试" value="领养考试" />
          <el-option label="AI咨询" value="AI咨询" />
        </el-select>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="action" label="操作内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="createdAt" label="操作时间" width="170" />
      </el-table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLogs } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const moduleFilter = ref('')
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (moduleFilter.value) params.module = moduleFilter.value
    const res = await getLogs(params)
    list.value = res.list || []; total.value = res.total || 0
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

function onPageChange({ page: p, size: s }) { page.value = p; size.value = s; loadList() }

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.toolbar { margin-bottom: 16px; }
</style>
