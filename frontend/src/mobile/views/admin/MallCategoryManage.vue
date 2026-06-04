<template>
  <div class="page">
    <div class="top-bar"><button class="add-btn" @click="openAdd">+ 新增分类</button></div>
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无分类" />
    <div v-else class="list">
      <MobileCard v-for="c in list" :key="c.id" class="item">
        <div class="row"><span class="name">{{ c.name }}</span><div class="acts"><button @click="openEdit(c)">编辑</button><button class="del" @click="handleDelete(c.id)">删除</button></div></div>
      </MobileCard>
    </div>
    <MobileDialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新增分类'" @close="editId = null">
      <div class="form-group"><label>分类名称</label><el-input v-model="form.name" placeholder="输入名称" /></div>
      <template #footer><button class="save-btn" @click="handleSave">保存</button></template>
    </MobileDialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMallCategories, addMallCategory, updateMallCategory, deleteMallCategory } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'
import MobileDialog from '../../components/MobileDialog.vue'

const list = ref([]); const loading = ref(true); const dialogVisible = ref(false); const editId = ref(null)
const form = reactive({ name: '' })

async function loadData() { loading.value = true; try { list.value = await getMallCategories() } catch {} finally { loading.value = false } }
function openAdd() { editId.value = null; form.name = ''; dialogVisible.value = true }
function openEdit(c) { editId.value = c.id; form.name = c.name; dialogVisible.value = true }
async function handleSave() {
  try {
    if (editId.value) { await updateMallCategory(editId.value, form.name) } else { await addMallCategory(form.name) }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch {}
}
async function handleDelete(id) { try { await ElMessageBox.confirm('确定删除？'); await deleteMallCategory(id); ElMessage.success('已删除'); loadData() } catch {} }

import { onMounted } from 'vue'
onMounted(loadData)
</script>

<style scoped>
.page { padding: 12px 12px 40px; }
.top-bar { margin-bottom: 10px; }
.add-btn { width: 100%; height: 36px; border: 1px dashed #409EFF; border-radius: 8px; background: none; color: #409EFF; font-size: 14px; cursor: pointer; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; }
.name { font-size: 15px; font-weight: 600; color: #303133; }
.acts { display: flex; gap: 6px; }
.acts button { height: 28px; padding: 0 10px; border: 1px solid #dcdfe6; border-radius: 6px; background: #fff; font-size: 12px; cursor: pointer; }
.acts button.del { color: #e8564a; border-color: #e8564a; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #909399; margin-bottom: 4px; }
.form-group :deep(.el-input__wrapper) { height: 42px; border-radius: 8px; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #409EFF; color: #fff; font-size: 15px; cursor: pointer; }
</style>
