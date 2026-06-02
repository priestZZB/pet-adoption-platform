<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">试题管理</h3>
      <div class="header-actions">
        <el-button @click="handleExport">导出CSV</el-button>
        <el-button @click="handleImport">批量导入</el-button>
        <el-button type="primary" @click="openDialog()">新增试题</el-button>
      </div>
    </div>

    <el-card>
      <div v-if="selectedIds.length > 0" class="batch-bar">
        <span class="batch-tip">已选 {{ selectedIds.length }} 项</span>
        <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
        <el-button @click="selectedIds = []">取消选择</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="question" label="题目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="optionA" label="A" width="120" show-overflow-tooltip />
        <el-table-column prop="optionB" label="B" width="120" show-overflow-tooltip />
        <el-table-column prop="optionC" label="C" width="120" show-overflow-tooltip />
        <el-table-column prop="optionD" label="D" width="120" show-overflow-tooltip />
        <el-table-column label="答案" width="60">
          <template #default="{ row }">{{ row.correctAnswer }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <div class="action-group">
              <el-button size="small" @click="openDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </div>
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
          <el-select
            v-model="form.correctAnswer"
            style="width:120px"
            popper-class="auto-close-popper"
            :ref="(el) => setSelectRef('_self', el)"
            @visible-change="(v) => onSelectVisible(v, '_self')"
          >
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

    <!-- 批量导入对话框 -->
    <el-dialog v-model="importVisible" title="批量导入试题" width="500px">
      <div class="import-guide">
        <p>请上传 CSV 文件，格式如下（第一行为表头）：</p>
        <p class="import-example">题目,选项A,选项B,选项C,选项D,正确答案</p>
        <p class="import-example">猫咪的寿命一般是几年？,5-8年,10-15年,15-20年,20-25年,B</p>
      </div>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".csv"
        :on-change="onFileChange"
        :file-list="importFileList"
      >
        <el-button type="primary">选择 CSV 文件</el-button>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestions, addQuestion, updateQuestion, deleteQuestion, batchDeleteQuestions, importQuestions } from '@/api/admin'
import { exportToCSV } from '@/utils/export'
import Pagination from '@/components/Pagination.vue'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const selectedIds = ref([])
const importVisible = ref(false)
const importing = ref(false)
const importFileList = ref([])
let importFile = null

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

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定批量删除所选 ${selectedIds.value.length} 道试题？`, '警告')
    await batchDeleteQuestions({ ids: selectedIds.value })
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadList()
  } catch { /* ignore */ }
}

function handleImport() {
  importFileList.value = []
  importFile = null
  importVisible.value = true
}

function onFileChange(file) {
  importFile = file.raw
}

async function doImport() {
  if (!importFile) { ElMessage.warning('请选择文件'); return }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile)
    const res = await importQuestions(formData)
    ElMessage.success(res.data || '导入成功')
    importVisible.value = false
    importFileList.value = []
    loadList()
  } catch { /* ignore */ }
  finally { importing.value = false }
}

async function handleExport() {
  try {
    const res = await getQuestions({ page: 1, size: 999999 })
    const data = (res.list || []).map(r => ({
      '题目': r.question,
      '选项A': r.optionA,
      '选项B': r.optionB,
      '选项C': r.optionC,
      '选项D': r.optionD,
      '正确答案': r.correctAnswer
    }))
    exportToCSV(data, '试题列表.csv')
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(loadList)
onUnmounted(cleanupSelectAutoClose)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.action-group {
  display: flex;
  gap: 4px;
  align-items: center;
}
.batch-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; background: #f0f9ff; border-radius: 6px; margin-bottom: 12px;
}
.batch-tip { font-size: 13px; color: #409eff; font-weight: 500; margin-right: 8px; }
.import-guide { margin-bottom: 16px; color: #606266; }
.import-example { font-family: monospace; font-size: 13px; color: #909399; margin: 4px 0; }
</style>
