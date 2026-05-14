<template>
  <div class="admin-page">
    <div class="page-header">
      <h3 class="page-title">轮播管理</h3>
      <el-button type="primary" @click="openDialog()">新增轮播</el-button>
    </div>

    <el-card>
      <!-- 固定欢迎页 -->
      <div class="banner-row fixed">
        <div class="banner-preview welcome-preview">
          <span class="welcome-icon">🏠</span>
        </div>
        <div class="banner-info">
          <span class="banner-title">欢迎来到毛球寻觅</span>
          <span class="banner-tag tag-fixed">固定</span>
        </div>
        <div class="banner-sort">—</div>
        <div class="banner-actions">
          <el-tag type="info">不可操作</el-tag>
        </div>
      </div>

      <el-divider style="margin:12px 0" />

      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无轮播图，请新增" :image-size="60" />
      </div>

      <div v-for="item in list" :key="item.id" class="banner-row">
        <div class="banner-preview">
          <el-image :src="item.imageUrl" fit="cover" style="width:100%;height:100%;border-radius:4px">
            <template #error><div class="img-ph">暂无</div></template>
          </el-image>
        </div>
        <div class="banner-info">
          <span class="banner-title">{{ item.title || '无标题' }}</span>
        </div>
        <div class="banner-sort">{{ item.sortOrder || 0 }}</div>
        <div class="banner-actions">
          <el-button size="small" @click="openDialog(item)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(item.id)">删除</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="图片" required>
          <el-upload
            :show-file-list="false"
            :before-upload="handleImageUpload"
            accept="image/*"
          >
            <el-image
              v-if="form.imageUrl"
              :src="form.imageUrl"
              fit="cover"
              style="width:200px;height:120px;border-radius:6px;display:block"
            />
            <div v-else class="upload-box">
              <el-icon :size="24"><Plus /></el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="轮播图标题（选填）" maxlength="100" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width:120px" />
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
import { getBanners, addBanner, updateBanner, deleteBanner } from '@/api/admin'
import { uploadFile } from '@/api/file'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = reactive({ imageUrl: '', title: '', sortOrder: 0 })

async function loadList() {
  loading.value = true
  try {
    const res = await getBanners()
    list.value = res || []
  } catch { list.value = [] }
  finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true; editId.value = row.id
    Object.assign(form, { imageUrl: row.imageUrl, title: row.title || '', sortOrder: row.sortOrder || 0 })
  } else {
    isEdit.value = false; editId.value = null
    Object.assign(form, { imageUrl: '', title: '', sortOrder: 0 })
  }
  dialogVisible.value = true
}

async function handleImageUpload(file) {
  try {
    const res = await uploadFile(file, 'banner')
    if (res && res.url) {
      form.imageUrl = res.url
    }
  } catch {
    ElMessage.error('图片上传失败')
  }
  return false
}

async function handleSave() {
  if (!form.imageUrl) { ElMessage.warning('请上传图片'); return }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateBanner(editId.value, form)
      ElMessage.success('已更新')
    } else {
      await addBanner(form)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false; loadList()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该轮播图？', '提示')
  await deleteBanner(id)
  ElMessage.success('已删除'); loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 900px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: #303133; }

.banner-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}
.banner-row + .banner-row {
  border-top: 1px solid #f5f7fa;
}
.banner-row.fixed {
  background: #fafafa;
  border-radius: 6px;
  padding: 12px 16px;
}
.banner-preview {
  width: 160px;
  height: 90px;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f7fa;
  flex-shrink: 0;
}
.welcome-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.welcome-icon {
  font-size: 36px;
}
.img-ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
}
.banner-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.banner-title {
  font-size: 14px;
  color: #303133;
}
.banner-tag {
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 3px;
  width: fit-content;
}
.tag-fixed {
  background: #ecf5ff;
  color: #409EFF;
}
.banner-sort {
  width: 40px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}
.banner-actions {
  width: 140px;
  text-align: right;
  flex-shrink: 0;
}
.empty-tip {
  padding: 20px 0;
}

.upload-box {
  width: 200px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  color: #909399;
  font-size: 13px;
  gap: 4px;
  transition: border-color 0.2s;
}
.upload-box:hover {
  border-color: #409EFF;
  color: #409EFF;
}
</style>
