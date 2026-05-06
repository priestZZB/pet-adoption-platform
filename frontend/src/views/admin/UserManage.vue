<template>
  <div class="admin-page">
    <h3 class="page-title">用户管理</h3>

    <el-card>
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名/昵称/手机号"
          clearable
          style="width:300px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="140">
          <template #default="{ row }">
            <el-tag
              v-for="r in row.roles"
              :key="r"
              size="small"
              :type="r === 'ADMIN' ? 'danger' : r === 'VOLUNTEER' ? 'success' : r === 'USER_ADOPTER' ? 'warning' : 'info'"
              style="margin-right:4px"
            >
              {{ ROLE_MAP[r] || r }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRealName" label="实名" width="60">
          <template #default="{ row }">
            <el-tag :type="row.isRealName === 1 ? 'success' : 'info'" size="small">
              {{ row.isRealName === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @click="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="角色分配" width="160">
          <template #default="{ row }">
            <el-select
              :model-value="row.roles?.[0]"
              size="small"
              @change="(val) => handleAssignRole(row, val)"
            >
              <el-option
                v-for="r in roleOptions"
                :key="r.id"
                :label="r.roleName"
                :value="r.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
      </el-table>

      <Pagination
        :total="total"
        :page="page"
        :size="size"
        @change="onPageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, toggleUserStatus, getRoles, assignRole } from '@/api/admin'
import { ROLE_MAP } from '@/utils/constants'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)
const roleOptions = ref([])

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await getUserList(params)
    list.value = res.list || []
    total.value = res.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    roleOptions.value = await getRoles()
  } catch {
    roleOptions.value = []
  }
}

function handleSearch() {
  page.value = 1
  loadList()
}

function onPageChange({ page: p, size: s }) {
  page.value = p
  size.value = s
  loadList()
}

async function handleToggleStatus(row) {
  try {
    await toggleUserStatus(row.id)
    row.status = row.status === 0 ? 1 : 0
    ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
  } catch {
    // 请求拦截器统一处理
  }
}

async function handleAssignRole(row, roleId) {
  try {
    await assignRole(row.id, roleId)
    ElMessage.success('角色修改成功')
    loadList()
  } catch {
    // 请求拦截器统一处理
  }
}

onMounted(() => {
  loadRoles()
  loadList()
})
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
