<template>
  <div class="publish-page">
    <h3 class="page-title">发布送养宠物</h3>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="宠物分类" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            placeholder="请选择分类"
            style="width:200px"
            popper-class="auto-close-popper"
            :ref="(el) => setSelectRef('_cat', el)"
            @visible-change="(v) => onSelectVisible(v, '_cat')"
          >
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入宠物名称" maxlength="50" />
        </el-form-item>

        <el-form-item label="年龄" prop="age">
          <el-select
            v-model="form.age"
            placeholder="请选择年龄"
            style="width:200px"
            popper-class="auto-close-popper"
            :ref="(el) => setSelectRef('_age', el)"
            @visible-change="(v) => onSelectVisible(v, '_age')"
          >
            <el-option
              v-for="n in 100"
              :key="n"
              :label="n + '岁'"
              :value="n + '岁'"
            />
            <el-option label="100岁以上请联系管理员" value="100+" disabled />
          </el-select>
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">公</el-radio>
            <el-radio value="female">母</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="绝育">
          <el-switch v-model="form.isNeutered" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item label="疫苗">
          <el-switch v-model="form.isVaccinated" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item label="健康证明" prop="healthCert">
          <el-upload
            :show-file-list="false"
            :before-upload="handleHealthCertUpload"
            accept="image/*"
          >
            <el-image
              v-if="form.healthCert"
              :src="form.healthCert"
              fit="cover"
              style="width:120px;height:120px;border-radius:6px"
            />
            <div v-else class="upload-box">
              <el-icon :size="24"><Plus /></el-icon>
              <span>上传健康证明</span>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="宠物照片" prop="images">
          <div class="multi-upload-wrap">
            <el-upload
              :file-list="imageFileList"
              :before-upload="handleImageUpload"
              :on-remove="handleImageRemove"
              list-type="picture-card"
              accept="image/*"
              multiple
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <p class="upload-tip">至少上传3张照片，第1张作为封面</p>
          </div>
        </el-form-item>

        <el-form-item label="性格描述" prop="personality">
          <el-input
            v-model="form.personality"
            type="textarea"
            :rows="3"
            placeholder="请描述宠物的性格特点"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="生活习惯">
          <el-input
            v-model="form.habit"
            type="textarea"
            :rows="3"
            placeholder="请描述宠物的生活习惯（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="送养原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请说明送养原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button class="publish-submit-btn" :loading="submitting" @click="handleSubmit">
            提交发布
          </el-button>
          <el-button class="publish-cancel-btn" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategories, publishPet } from '@/api/pet'
import { uploadFile, uploadFiles } from '@/api/file'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const imageFileList = ref([])
const imageUrlList = ref([])

const form = reactive({
  categoryId: null,
  name: '',
  age: '',
  gender: 'male',
  isNeutered: 0,
  isVaccinated: 0,
  healthCert: '',
  personality: '',
  habit: '',
  reason: '',
  images: []
})

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  personality: [{ required: true, message: '请描述性格特点', trigger: 'blur' }],
  reason: [{ required: true, message: '请说明送养原因', trigger: 'blur' }]
}

async function loadCategories() {
  try {
    categories.value = await getCategories()
  } catch {
    // 请求拦截器统一处理
  }
}

// 健康证明单图上传
async function handleHealthCertUpload(file) {
  try {
    const res = await uploadFile(file, 'pet')
    form.healthCert = res.url
  } catch {
    ElMessage.error('健康证明上传失败')
  }
  return false
}

// 多图上传
async function handleImageUpload(file) {
  try {
    const res = await uploadFiles([file], 'pet')
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

function handleImageRemove(file) {
  // 简化处理：重新加载时忽略已删除的
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.healthCert) {
    ElMessage.warning('请上传健康证明')
    return
  }
  if (imageUrlList.value.length < 3) {
    ElMessage.warning('请至少上传3张宠物照片')
    return
  }

  submitting.value = true
  try {
    await publishPet({
      categoryId: form.categoryId,
      name: form.name,
      age: form.age,
      gender: form.gender,
      isNeutered: form.isNeutered,
      isVaccinated: form.isVaccinated,
      healthCert: form.healthCert,
      personality: form.personality,
      habit: form.habit,
      reason: form.reason,
      images: imageUrlList.value
    })
    ElMessage.success('发布成功，等待审核')
    router.push('/donate/pets')
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadCategories)
onUnmounted(cleanupSelectAutoClose)
</script>

<style scoped>
.publish-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}

/* Card 暖色 */
:deep(.el-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

.upload-box {
  width: 120px;
  height: 120px;
  border: 1px dashed var(--yc-border);
  border-radius: var(--yc-radius-input);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--yc-text-tertiary);
  cursor: pointer;
  transition: border-color 0.2s;
}
.upload-box:hover {
  border-color: var(--yc-accent);
  color: var(--yc-accent);
}

.multi-upload-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.upload-tip {
  margin: 0;
  font-size: 12px;
  color: var(--yc-text-tertiary);
}

/* 按钮暖色 */
:deep(.publish-submit-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.publish-submit-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.publish-cancel-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.publish-cancel-btn:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
</style>
