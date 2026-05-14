<template>
  <div class="add-visit-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange" class="page-tabs">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="审核历史" name="reviewed" />
      <el-tab-pane label="去走访" name="add" />
      <el-tab-pane label="走访记录" name="visits" />
    </el-tabs>

    <el-card class="submit-card">
      <template #header><span>新增走访记录</span></template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="走访宠物">
          <el-select
            v-model="form.petId"
            placeholder="请选择已领养宠物"
            filterable
            clearable
            style="width:100%"
            :loading="petsLoading"
          >
            <el-option
              v-for="pet in petOptions"
              :key="pet.id"
              :label="pet.name + ' (' + STATUS_LABEL[pet.status] + ')'"
              :value="pet.id"
            />
          </el-select>
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
          <el-button @click="$router.push('/volunteer/visits')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { submitVisit, getSelectablePets } from '@/api/volunteer'
import { uploadFiles } from '@/api/file'

const router = useRouter()
const activeTab = ref('add')
const formRef = ref(null)
const submitting = ref(false)
const imageFileList = ref([])
const imageUrlList = ref([])
const petOptions = ref([])
const petsLoading = ref(false)

const STATUS_LABEL = {
  PENDING: '待审核',
  FIRST_PASS: '待终审',
  APPROVED: '可领养',
  ADOPTED: '已领养'
}

const form = reactive({
  petId: null,
  visitDate: '',
  content: ''
})

const rules = {
  visitDate: [{ required: true, message: '请选择走访日期', trigger: 'change' }],
  content: [{ required: true, message: '请输入走访内容', trigger: 'blur' }]
}

async function loadPets() {
  petsLoading.value = true
  try {
    petOptions.value = await getSelectablePets()
  } catch {
    petOptions.value = []
  } finally {
    petsLoading.value = false
  }
}

async function handleImageUpload(file) {
  try {
    const res = await uploadFiles([file], 'volunteer')
    imageUrlList.value = imageUrlList.value.concat(res.urls || [])
    if (res.urls && res.urls[0]) {
      imageFileList.value = imageFileList.value.concat({
        uid: Date.now(),
        name: file.name,
        url: res.urls[0]
      })
    }
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
      petId: form.petId,
      visitDate: form.visitDate,
      content: form.content
    }
    if (imageUrlList.value.length > 0) {
      data.images = imageUrlList.value
    }
    await submitVisit(data)
    ElMessage.success('走访记录已提交')
    router.push('/volunteer/visits')
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

function onTabChange(tab) {
  if (tab === 'pending') router.push('/volunteer/pending')
  else if (tab === 'reviewed') router.push('/volunteer/reviewed')
  else if (tab === 'visits') router.push('/volunteer/visits')
}

onMounted(loadPets)
</script>

<style scoped>
.add-visit-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.submit-card {
  margin-top: 4px;
}
</style>
