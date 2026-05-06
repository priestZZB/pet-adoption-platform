<template>
  <div class="feedback-page">
    <h3 class="page-title">意见反馈</h3>

    <!-- 提交反馈 -->
    <el-card class="submit-card">
      <template #header><span>提交反馈</span></template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        @keyup.enter="handleSubmit"
      >
        <el-form-item prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请描述你的问题或建议..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="">
          <el-upload
            :file-list="fileList"
            :before-upload="handleUpload"
            :on-remove="handleRemove"
            list-type="picture-card"
            accept="image/*"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交反馈
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的反馈列表 -->
    <el-card class="list-card">
      <template #header><span>我的反馈</span></template>

      <div v-if="loading" class="loading-center">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>

      <div v-else-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无反馈记录" :image-size="80" />
      </div>

      <div v-else class="feedback-list">
        <div v-for="item in list" :key="item.id" class="feedback-item">
          <div class="feedback-header">
            <span class="feedback-time">{{ item.createdAt }}</span>
            <el-tag v-if="item.status === 1" type="success" size="small">已回复</el-tag>
            <el-tag v-else type="info" size="small">待回复</el-tag>
          </div>
          <p class="feedback-content">{{ item.content }}</p>
          <div v-if="item.images" class="feedback-images">
            <el-image
              v-for="(img, idx) in item.images.split(',')"
              :key="idx"
              :src="img"
              fit="cover"
              class="feedback-img"
              :preview-src-list="item.images.split(',')"
              preview-teleported
            />
          </div>
          <div v-if="item.reply" class="feedback-reply">
            <span class="reply-label">管理员回复：</span>
            <span>{{ item.reply }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import { submitFeedback, getMyFeedback } from '@/api/feedback'
import { uploadFiles } from '@/api/file'

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(true)
const list = ref([])
const fileList = ref([])
const imageUrls = ref([])

const form = reactive({
  content: ''
})

const rules = {
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' }
  ]
}

// 图片上传
async function handleUpload(file) {
  try {
    const res = await uploadFiles([file], 'feedback')
    imageUrls.value = imageUrls.value.concat(res.urls || [])
  } catch {
    ElMessage.error('图片上传失败')
  }
  return false
}

function handleRemove(file) {
  // 从 imageUrls 中移除对应 URL
  // 简单处理：重建 fileList 后重新匹配（此处留空，上传失败可忽略）
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submitFeedback({
      content: form.content,
      images: imageUrls.value.join(',')
    })
    ElMessage.success('反馈已提交，感谢你的建议')
    form.content = ''
    imageUrls.value = []
    fileList.value = []
    loadList()
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getMyFeedback()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.feedback-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}

.submit-card {
  margin-bottom: 20px;
}
.list-card {
  margin-bottom: 20px;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.empty-tip {
  padding: 20px 0;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.feedback-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}
.feedback-item + .feedback-item {
  margin-top: 8px;
}
.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.feedback-time {
  font-size: 12px;
  color: #909399;
}
.feedback-content {
  margin: 0 0 8px;
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
}
.feedback-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.feedback-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}
.feedback-reply {
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
.reply-label {
  font-weight: 500;
  color: #409EFF;
}
</style>
