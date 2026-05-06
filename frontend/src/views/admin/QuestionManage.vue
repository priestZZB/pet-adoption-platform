<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">试题管理</h3>
      <el-button type="primary" @click="openDialog()">新增试题</el-button>
    </div>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="question" label="题目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="optionA" label="A" width="120" show-overflow-tooltip />
        <el-table-column prop="optionB" label="B" width="120" show-overflow-tooltip />
        <el-table-column prop="optionC" label="C" width="120" show-overflow-tooltip />
        <el-table-column prop="optionD" label="D" width="120" show-overflow-tooltip />
        <el-table-column label="答案" width="60">
          <template #default="{ row }">{{ row.correctAnswer }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑试题' : '新增试题'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题目" required>
          <el-input v-model="form.question" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="选项A" required>
          <el-input v-model="form.optionA" />
        </el-form-item>
        <el-form-item label="选项B" required>
          <el-input v-model="form.optionB" />
        </el-form-item>
        <el-form-item label="选项C" required>
          <el-input v-model="form.optionC" />
        </el-form-item>
        <el-form-item label="选项D">
          <el-input v-model="form.optionD" />
        </el-form-item>
        <el-form-item label="正确答案" required>
          <el-select v-model="form.correctAnswer" style="width:120px">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="D" value="D" />
          </el-select>
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
import { getQuestions, addQuestion, updateQuestion, deleteQuestion } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)

const form = reactive({ question: '', optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A' })

async function loadList() {
  loading.value = true
  try {
    const res = await getQuestions({ page: page.value, size: size.value })
    list.value = res.list || []
    total.value = res.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onPageChange({ page: p, size: s }) { page.value = p; size.value = s; loadList() }

function openDialog(row) {
  if (row) {
    isEdit.value = true; editId.value = row.id
    Object.assign(form, { question: row.question, optionA: row.optionA, optionB: row.optionB, optionC: row.optionC, optionD: row.optionD || '', correctAnswer: row.correctAnswer })
  } else {
    isEdit.value = false; editId.value = null
    Object.assign(form, { question: '', optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.question || !form.optionA || !form.optionB || !form.optionC) { ElMessage.warning('请填写完整'); return }
  saving.value = true
  try {
    const params = { question: form.question, optionA: form.optionA, optionB: form.optionB, optionC: form.optionC, optionD: form.optionD, correctAnswer: form.correctAnswer }
    if (isEdit.value) { await updateQuestion(editId.value, params); ElMessage.success('已更新') }
    else { await addQuestion(params); ElMessage.success('已新增') }
    dialogVisible.value = false; loadList()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示')
  await deleteQuestion(id)
  ElMessage.success('已删除'); loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: #303133; }
</style>
