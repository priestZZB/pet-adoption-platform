<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">商品分类管理</h3>
      <el-button type="primary" @click="openDialog()">新增分类</el-button>
    </div>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sortOrder" label="排序号" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMallCategories } from '@/api/mall'
import { addMallCategory, updateMallCategory, deleteMallCategory } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = reactive({ name: '', sortOrder: 0 })

async function loadList() {
  loading.value = true
  try { list.value = await getMallCategories() }
  catch { list.value = [] }
  finally { loading.value = false }
}

function openDialog(row) {
  if (row) { isEdit.value = true; editId.value = row.id; form.name = row.name; form.sortOrder = row.sortOrder }
  else { isEdit.value = false; editId.value = null; form.name = ''; form.sortOrder = 0 }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name) { ElMessage.warning('请输入名称'); return }
  saving.value = true
  try {
    if (isEdit.value) { await updateMallCategory(editId.value, { name: form.name, sortOrder: form.sortOrder }); ElMessage.success('已更新') }
    else { await addMallCategory({ name: form.name, sortOrder: form.sortOrder }); ElMessage.success('已新增') }
    dialogVisible.value = false; loadList()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示')
  await deleteMallCategory(id); ElMessage.success('已删除'); loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 700px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: #303133; }
</style>
