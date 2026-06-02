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
        <el-button @click="handleExport">导出CSV</el-button>
      </div>

      <div v-if="selectedIds.length > 0" class="batch-bar">
        <span class="batch-tip">已选 {{ selectedIds.length }} 项</span>
        <el-button type="success" @click="handleBatchEnable">批量启用</el-button>
        <el-button type="danger" @click="handleBatchDisable">批量禁用</el-button>
        <el-button type="warning" @click="handleBatchRole">批量分配角色</el-button>
        <el-button @click="selectedIds = []">取消选择</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading" @selection-change="onSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="50" />
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
        <el-table-column label="角色分配" width="180">
          <template #default="{ row }">
            <div class="select-tag-area">
              <el-tooltip
                v-if="isRowDisabled(row)"
                :content="row.id === userStore.userInfo?.id ? '不能修改自己的角色' : '只有超级管理员才能管理管理员角色'"
                placement="top"
              >
                <el-select
                  :ref="(el) => setSelectRef(row.id, el)"
                  :model-value="getRowRoleIds(row)"
                  size="small"
                  multiple
                  disabled
                  popper-class="auto-close-popper"
                >
                  <el-option
                    v-for="r in roleOptions"
                    :key="r.id"
                    :label="r.roleName"
                    :value="r.id"
                  />
                </el-select>
              </el-tooltip>
              <el-select
                v-else
                :ref="(el) => setSelectRef(row.id, el)"
                :model-value="getRowRoleIds(row)"
                size="small"
                multiple
                popper-class="auto-close-popper"
                @change="(val) => handleAssignRole(row, val)"
                @visible-change="(v) => onSelectVisible(v, row.id)"
              >
                <el-option
                  v-for="r in roleOptions"
                  :key="r.id"
                  :label="r.roleName"
                  :value="r.id"
                  :disabled="isOptionDisabled(r.id)"
                />
              </el-select>
            </div>
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
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, toggleUserStatus, getRoles, assignRole, batchToggleUserStatus, batchAssignRole } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import { ROLE_MAP } from '@/utils/constants'
import { exportToCSV } from '@/utils/export'
import Pagination from '@/components/Pagination.vue'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'

// ---- 鼠标移开即关闭下拉 ----
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

// ---- 页面状态 ----
const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)
const roleOptions = ref([])
const adminRoleId = ref(null)
const selectedIds = ref([])
const tableRef = ref(null)

// 当前登录用户是否是超级管理员
const isSuperAdmin = computed(() => userStore.userInfo?.isSuperAdmin === 1)

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
    const roles = await getRoles()
    roleOptions.value = roles
    const admin = roles.find(r => r.roleCode === 'ADMIN')
    if (admin) adminRoleId.value = admin.id
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
  } catch { /* ignore */ }
}

function getRowRoleIds(row) {
  if (!row.roles || !roleOptions.value.length) return []
  return row.roles.map(code => {
    const found = roleOptions.value.find(r => r.roleCode === code)
    return found ? found.id : null
  }).filter(Boolean)
}

/** 某行是否禁用角色选择（自己不能改自己、普通管理员不能改其他管理员） */
function isRowDisabled(row) {
  if (row.id === userStore.userInfo?.id) return true
  const isTargetAdmin = row.roles && row.roles.includes('ADMIN')
  if (isTargetAdmin && !isSuperAdmin.value) return true
  return false
}

/** 某角色选项是否禁用（普通管理员不能选ADMIN） */
function isOptionDisabled(roleId) {
  if (!isSuperAdmin.value && roleId === adminRoleId.value) return true
  return false
}

async function handleAssignRole(row, roleIds) {
  if (!roleIds || roleIds.length === 0) {
    ElMessage.warning('至少选择一个角色')
    loadList()
    return
  }
  // 查找ADMIN/VOLUNTEER/DONOR对应的角色ID
  const adminRole = roleOptions.value.find(r => r.roleCode === 'ADMIN')
  const volunteerRole = roleOptions.value.find(r => r.roleCode === 'VOLUNTEER')
  const donorRole = roleOptions.value.find(r => r.roleCode === 'USER_ADOPTER')
  const hasAdmin = adminRole && roleIds.includes(adminRole.id)
  const hasVolunteer = volunteerRole && roleIds.includes(volunteerRole.id)
  const hasDonor = donorRole && roleIds.includes(donorRole.id)

  if (hasAdmin && (hasVolunteer || hasDonor)) {
    ElMessage.warning('管理员不能同时兼任志愿者或送养人')
    loadList()
    return
  }
  try {
    await assignRole(row.id, roleIds)
    ElMessage.success('角色修改成功')
    loadList()
  } catch { /* ignore */ }
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchEnable() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm('确定批量启用所选用户？', '提示')
    await batchToggleUserStatus({ ids: selectedIds.value, action: 'enable' })
    ElMessage.success('批量启用成功')
    selectedIds.value = []
    loadList()
  } catch { /* cancelled or error */ }
}

async function handleBatchDisable() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm('确定批量禁用所选用户？', '提示')
    await batchToggleUserStatus({ ids: selectedIds.value, action: 'disable' })
    ElMessage.success('批量禁用成功')
    selectedIds.value = []
    loadList()
  } catch { /* cancelled or error */ }
}

async function handleBatchRole() {
  if (!selectedIds.value.length) return
  if (!roleOptions.value.length) { ElMessage.warning('角色列表未加载'); return }
  // 普通管理员可选角色（排除ADMIN）
  const availableRoles = isSuperAdmin.value
    ? roleOptions.value
    : roleOptions.value.filter(r => r.roleCode !== 'ADMIN')
  try {
    await ElMessageBox.confirm(
      '将为所选 ' + selectedIds.value.length + ' 个用户统一分配角色，继续选择角色？',
      '批量分配角色'
    )
    // 弹出角色选择
    const { value: chosenLabels } = await ElMessageBox.prompt('请输入角色名称（用逗号分隔多个，如: 志愿者,送养人）', '选择角色', {
      inputPattern: /.+/,
      inputErrorMessage: '请输入角色名称'
    })
    if (!chosenLabels) return
    const roleIds = []
    for (const label of chosenLabels.split(',')) {
      const r = availableRoles.find(ro => ro.roleName === label.trim() || ro.roleCode === label.trim().toUpperCase())
      if (r) roleIds.push(r.id)
    }
    if (!roleIds.length) { ElMessage.warning('未识别到有效角色'); return }
    await batchAssignRole({ userIds: selectedIds.value, roleIds })
    ElMessage.success('批量分配角色成功')
    selectedIds.value = []
    loadList()
  } catch { /* cancelled */ }
}

async function handleExport() {
  try {
    const params = { page: 1, size: 999999 }
    if (keyword.value) params.keyword = keyword.value
    const res = await getUserList(params)
    const data = (res.list || []).map(r => ({
      'ID': r.id,
      '用户名': r.username,
      '昵称': r.nickname,
      '手机号': r.phone,
      '角色': (r.roles || []).map(c => ROLE_MAP[c] || c).join('、'),
      '实名': r.isRealName === 1 ? '是' : '否',
      '状态': r.status === 1 ? '启用' : '禁用',
      '注册时间': r.createdAt
    }))
    exportToCSV(data, '用户列表.csv')
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(() => {
  // 确保用户信息已加载（含isSuperAdmin）
  if (userStore.userInfo === null) {
    userStore.fetchUserInfo()
  }
  loadRoles()
  loadList()
})

onUnmounted(() => {
  cleanupSelectAutoClose()
})
</script>

<style scoped>
.admin-page { max-width: 1200px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.batch-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; background: #f0f9ff; border-radius: 6px; margin-bottom: 12px;
}
.batch-tip { font-size: 13px; color: #409eff; font-weight: 500; margin-right: 8px; }
</style>
