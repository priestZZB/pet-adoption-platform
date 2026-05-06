<template>
  <div class="visits-page">
    <h3 class="page-title">走访记录</h3>

    <!-- 新增走访记录 -->
    <el-card class="submit-card">
      <template #header><span>新增走访记录</span></template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        size="large"
      >
        <el-form-item label="关联宠物">
          <el-input
            v-model="form.petId"
            placeholder="关联宠物ID（选填）"
          />
        </el-form-item>

        <el-form-item label="走访日期" prop="visitDate">
          <el-date-picker
            v-model="form.visitDate"
            type="date"
            placeholder="选择走访日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>

        <el-form-item label="走访内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请描述走访情况"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
            :file-list="imageFileList"
            :before-upload="handleImageUpload"
            list-type="picture-card"
            accept="image/*"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交记录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 走访记录列表 -->
    <el-card class="list-card">
      <template #header><span>我的走访记录</span></template>

      <div v-if="listLoading" class="loading-center">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>

      <template v-else>
        <div v-if="visitList.length === 0" class="empty-tip">
          <el-empty description="暂无走访记录" :image-size="80" />
        </div>

        <div v-else class="visit-list">
          <div
            v-for="item in visitList"
            :key="item.id"
            class="visit-item"
            @click="showDetail(item)"
          >
            <div class="visit-header">
              <span class="visit-date">{{ item.visitDate }}</span>
              <span class="visit-time">{{ item.createdAt }}</span>
            </div>
            <p class="visit-content">{{ item.content }}</p>
            <div v-if="item.images && item.images.length > 0" class="visit-thumbs">
              <el-image
                v-for="(img, idx) in item.images.slice(0, 4)"
                :key="idx"
                :src="img"
                fit="cover"
                class="thumb"
              />
              <span v-if="item.images.length > 4" class="more">
                +{{ item.images.length - 4 }}
              </span>
            </div>
          </div>
        </div>

        <Pagination
          :total="total"
          :page="page"
          :size="size"
          @change="onPageChange"
        />
      </template>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="走访记录详情" width="500px">
      <div v-if="currentVisit" class="detail-body">
        <div class="detail-row">
          <span class="label">走访日期</span>
          <span>{{ currentVisit.visitDate }}</span>
        </div>
        <div v-if="currentVisit.petId" class="detail-row">
          <span class="label">关联宠物</span>
          <span>宠物ID: {{ currentVisit.petId }}</span>
        </div>
        <div class="detail-row">
          <span class="label">走访内容</span>
          <p>{{ currentVisit.content }}</p>
        </div>
        <div v-if="currentVisit.images && currentVisit.images.length > 0" class="detail-images">
          <span class="label">现场图片</span>
          <div class="img-list">
            <el-image
              v-for="(img, idx) in currentVisit.images"
              :key="idx"
              :src="img"
              fit="cover"
              class="detail-img"
              :preview-src-list="currentVisit.images"
              preview-teleported
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import { submitVisit, getMyVisits, getVisitDetail } from '@/api/volunteer'
import { uploadFiles } from '@/api/file'
import Pagination from '@/components/Pagination.vue'

const formRef = ref(null)
const submitting = ref(false)
const listLoading = ref(true)
const imageFileList = ref([])
const imageUrlList = ref([])
const visitList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const detailVisible = ref(false)
const currentVisit = ref(null)

const form = reactive({
  petId: '',
  visitDate: '',
  content: ''
})

const rules = {
  visitDate: [{ required: true, message: '请选择走访日期', trigger: 'change' }],
  content: [{ required: true, message: '请输入走访内容', trigger: 'blur' }]
}

async function handleImageUpload(file) {
  try {
    const res = await uploadFiles([file], 'volunteer')
    imageUrlList.value = imageUrlList.value.concat(res.urls || [])
  } catch {
    ElMessage.error('图片上传失败')
  }
  return false
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      petId: form.petId ? parseInt(form.petId) : null,
      visitDate: form.visitDate,
      content: form.content
    }
    if (imageUrlList.value.length > 0) {
      data.images = imageUrlList.value
    }
    await submitVisit(data)
    ElMessage.success('走访记录已提交')
    form.petId = ''
    form.visitDate = ''
    form.content = ''
    imageUrlList.value = []
    imageFileList.value = []
    page.value = 1
    loadList()
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

async function loadList() {
  listLoading.value = true
  try {
    const res = await getMyVisits({ page: page.value, size: size.value })
    visitList.value = res.list || []
    total.value = res.total || 0
  } catch {
    visitList.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

function onPageChange({ page: p, size: s }) {
  page.value = p
  size.value = s
  loadList()
}

async function showDetail(item) {
  try {
    currentVisit.value = await getVisitDetail(item.id)
    detailVisible.value = true
  } catch {
    // 请求拦截器统一处理
  }
}

onMounted(loadList)
</script>

<style scoped>
.visits-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}
.submit-card { margin-bottom: 20px; }
.loading-center {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.empty-tip { padding: 20px 0; }

.visit-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.visit-item {
  padding: 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s;
}
.visit-item:hover { border-color: #409EFF; }
.visit-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.visit-date {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.visit-time { font-size: 12px; color: #909399; }
.visit-content {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.visit-thumbs {
  display: flex;
  gap: 6px;
  align-items: center;
}
.thumb {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}
.more { font-size: 12px; color: #909399; }

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-row {
  display: flex;
  gap: 12px;
}
.detail-row .label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  color: #909399;
}
.detail-row span,
.detail-row p {
  font-size: 14px;
  color: #303133;
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}
.detail-images .label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.img-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.detail-img {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  cursor: pointer;
}
</style>
