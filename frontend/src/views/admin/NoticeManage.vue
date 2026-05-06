<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">公告管理</h3>
      <el-button type="primary" @click="openDialog()">发布公告</el-button>
    </div>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="160" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="显示">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getAllNotices, sendNotice, updateNotice, deleteNotice } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = reactive({ title: '', content: '', status: 1 })

async function loadList() {
  loading.value = true
  try { list.value = await getAllNotices() }
  catch { list.value = [] }
  finally { loading.value = false }
}

function openDialog(row) {
  if (row) { isEdit.value = true; editId.value = row.id; form.title = row.title; form.content = row.content; form.status = row.status }
  else { isEdit.value = false; editId.value = null; form.title = ''; form.content = ''; form.status = 1 }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.title || !form.content) { ElMessage.warning('请填写完整'); return }
  saving.value = true
  try {
    const params = { title: form.title, content: form.content, status: form.status }
    if (isEdit.value) { await updateNotice(editId.value, params); ElMessage.success('已更新') }
    else { await sendNotice(params); ElMessage.success('已发布') }
    dialogVisible.value = false; loadList()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该公告？', '提示')
  await deleteNotice(id); ElMessage.success('已删除'); loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 900px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: #303133; }
</style>
