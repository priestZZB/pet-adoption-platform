<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">商品管理</h3>
      <el-button type="primary" @click="openDialog()">新增商品</el-button>
    </div>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="图片" width="60">
          <template #default="{ row }">
            <el-image :src="row.image" fit="cover" style="width:40px;height:40px;border-radius:4px">
              <template #error><div class="img-xs" /></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">{{ row.categoryName || '—' }}</template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" />
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" @click="handleToggle(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类" required>
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:200px" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="0" style="width:200px" />
        </el-form-item>
        <el-form-item label="主图">
          <el-input v-model="form.image" placeholder="图片URL" />
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
import { getMallCategories, getMallProducts } from '@/api/mall'
import { addProduct, updateProduct, toggleProductStatus } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = reactive({ categoryId: null, name: '', description: '', price: 0, stock: 0, image: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await getMallProducts({ page: page.value, size: size.value, all: true })
    list.value = res.list || []; total.value = res.total || 0
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

async function loadCategories() {
  try { categories.value = await getMallCategories() } catch { categories.value = [] }
}

function onPageChange({ page: p, size: s }) { page.value = p; size.value = s; loadList() }

function openDialog(row) {
  if (row) {
    isEdit.value = true; editId.value = row.id
    Object.assign(form, { categoryId: row.categoryId, name: row.name, description: row.description || '', price: row.price, stock: row.stock, image: row.image || '' })
  } else {
    isEdit.value = false; editId.value = null
    Object.assign(form, { categoryId: null, name: '', description: '', price: 0, stock: 0, image: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.categoryId || !form.name) { ElMessage.warning('请填写完整'); return }
  saving.value = true
  try {
    const params = { categoryId: form.categoryId, name: form.name, description: form.description, price: form.price, stock: form.stock, image: form.image }
    if (isEdit.value) { await updateProduct(editId.value, params); ElMessage.success('已更新') }
    else { await addProduct(params); ElMessage.success('已新增') }
    dialogVisible.value = false; loadList()
  } finally { saving.value = false }
}

async function handleToggle(row) {
  await ElMessageBox.confirm(`确定${row.status === 1 ? '下架' : '上架'}该商品？`, '提示')
  await toggleProductStatus(row.id)
  ElMessage.success('操作成功'); loadList()
}

onMounted(() => { loadCategories(); loadList() })
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: #303133; }
.img-xs { width: 40px; height: 40px; background: #f5f7fa; border-radius: 4px; }
</style>
