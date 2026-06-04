<template>
  <div class="page">
    <div class="top-bar"><button class="add-btn" @click="openAdd">+ 新增试题</button></div>
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无试题" />
    <div v-else class="list">
      <MobileCard v-for="q in list" :key="q.id" class="item">
        <div class="q-content">{{ q.content }}</div>
        <div class="q-options" v-if="q.options">{{ q.options }}</div>
        <div class="acts"><button @click="openEdit(q)">编辑</button><button class="del" @click="handleDelete(q.id)">删除</button></div>
      </MobileCard>
    </div>
    <div class="more" v-if="list.length < total"><button @click="loadMore">加载更多</button></div>
    <MobileDialog v-model="dialogVisible" :title="editId ? '编辑试题' : '新增试题'" @close="editId = null">
      <div class="form-group"><label>题目</label><el-input v-model="form.content" placeholder="题目内容" /></div>
      <div class="form-group"><label>选项（逗号分隔）</label><el-input v-model="form.options" placeholder="A,B,C,D" /></div>
      <div class="form-group"><label>正确答案（索引）</label><el-input v-model="form.answer" placeholder="0" /></div>
      <template #footer><button class="save-btn" @click="handleSave">保存</button></template>
    </MobileDialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestions, addQuestion, updateQuestion, deleteQuestion } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'
import MobileDialog from '../../components/MobileDialog.vue'

const list = ref([]); const total = ref(0); const loading = ref(true); const page = ref(1)
const dialogVisible = ref(false); const editId = ref(null)
const form = reactive({ content: '', options: '', answer: '0' })

async function loadData(reset = false) {
  if (reset) { page.value = 1; loading.value = true }
  try { const res = await getQuestions({ page: page.value, size: 10 }); if (reset) list.value = res.records || []; else list.value.push(...(res.records || [])); total.value = res.total || 0 } catch {} finally { loading.value = false }
}
async function loadMore() { page.value++; await loadData(false) }
function openAdd() { editId.value = null; Object.assign(form, { content: '', options: '', answer: '0' }); dialogVisible.value = true }
function openEdit(q) { editId.value = q.id; Object.assign(form, { content: q.content, options: q.options, answer: String(q.answer) }); dialogVisible.value = true }
async function handleSave() {
  try {
    const data = { content: form.content, options: form.options, answer: Number(form.answer) }
    if (editId.value) { await updateQuestion(editId.value, data) } else { await addQuestion(data) }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData(true)
  } catch {}
}
async function handleDelete(id) { try { await ElMessageBox.confirm('确定删除？'); await deleteQuestion(id); ElMessage.success('已删除'); loadData(true) } catch {} }

import { onMounted } from 'vue'
onMounted(() => loadData(true))
</script>

<style scoped>
.page { padding: 12px 12px 40px; }
.top-bar { margin-bottom: 10px; }
.add-btn { width: 100%; height: 36px; border: 1px dashed #409EFF; border-radius: 8px; background: none; color: #409EFF; font-size: 14px; cursor: pointer; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.q-content { font-size: 14px; color: #303133; margin-bottom: 4px; }
.q-options { font-size: 12px; color: #909399; margin-bottom: 6px; }
.acts { display: flex; gap: 6px; }
.acts button { height: 28px; padding: 0 10px; border: 1px solid #dcdfe6; border-radius: 6px; background: #fff; font-size: 12px; cursor: pointer; }
.acts button.del { color: #e8564a; border-color: #e8564a; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #909399; margin-bottom: 4px; }
.form-group :deep(.el-input__wrapper) { height: 42px; border-radius: 8px; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #409EFF; color: #fff; font-size: 15px; cursor: pointer; }
.more { text-align: center; padding: 16px 0; }
.more button { height: 36px; padding: 0 24px; border: 1px solid #dcdfe6; border-radius: 8px; background: #fff; color: #606266; cursor: pointer; }
</style>
