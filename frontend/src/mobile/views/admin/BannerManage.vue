<template>
  <div class="page">
    <div class="top-bar"><button class="add-btn" @click="openAdd">+ 新增轮播图</button></div>
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无轮播图" />
    <div v-else class="list">
      <MobileCard v-for="b in list" :key="b.id" class="item">
        <img :src="b.imageUrl" class="banner-img" />
        <div class="row"><span class="title">{{ b.title || '无标题' }}</span><span class="time">{{ b.createdAt?.slice(0, 10) }}</span></div>
        <div class="acts"><button @click="openEdit(b)">编辑</button><button class="del" @click="handleDelete(b.id)">删除</button></div>
      </MobileCard>
    </div>
    <MobileDialog v-model="dialogVisible" :title="editId ? '编辑轮播图' : '新增轮播图'" @close="editId = null">
      <div class="form-group"><label>标题</label><el-input v-model="form.title" placeholder="轮播图标题" /></div>
      <div class="form-group"><label>图片URL</label><el-input v-model="form.imageUrl" placeholder="图片地址" /></div>
      <template #footer><button class="save-btn" @click="handleSave">保存</button></template>
    </MobileDialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBanners, addBanner, updateBanner, deleteBanner } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'
import MobileDialog from '../../components/MobileDialog.vue'

const list = ref([]); const loading = ref(true); const dialogVisible = ref(false); const editId = ref(null)
const form = reactive({ title: '', imageUrl: '' })

async function loadData() { loading.value = true; try { list.value = await getBanners() } catch {} finally { loading.value = false } }
function openAdd() { editId.value = null; form.title = ''; form.imageUrl = ''; dialogVisible.value = true }
function openEdit(b) { editId.value = b.id; form.title = b.title; form.imageUrl = b.imageUrl; dialogVisible.value = true }
async function handleSave() {
  try {
    if (editId.value) { await updateBanner(editId.value, form) } else { await addBanner(form) }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch {}
}
async function handleDelete(id) { try { await ElMessageBox.confirm('确定删除？'); await deleteBanner(id); ElMessage.success('已删除'); loadData() } catch {} }

import { onMounted } from 'vue'
onMounted(loadData)
</script>

<style scoped>
.page { padding: 12px 12px 40px; }
.top-bar { margin-bottom: 10px; }
.add-btn { width: 100%; height: 36px; border: 1px dashed #409EFF; border-radius: 8px; background: none; color: #409EFF; font-size: 14px; cursor: pointer; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.banner-img { width: 100%; border-radius: 8px; margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.title { font-size: 15px; font-weight: 600; color: #303133; }
.time { font-size: 12px; color: #909399; }
.acts { display: flex; gap: 6px; }
.acts button { height: 28px; padding: 0 10px; border: 1px solid #dcdfe6; border-radius: 6px; background: #fff; font-size: 12px; cursor: pointer; }
.acts button.del { color: #e8564a; border-color: #e8564a; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #909399; margin-bottom: 4px; }
.form-group :deep(.el-input__wrapper) { height: 42px; border-radius: 8px; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #409EFF; color: #fff; font-size: 15px; cursor: pointer; }
</style>
