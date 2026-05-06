<template>
  <div class="realname-page">
    <h3 class="page-title">实名认证</h3>

    <!-- 已认证 -->
    <el-card v-if="isVerified" class="result-card">
      <div class="result-content">
        <el-icon :size="48" color="#67C23A"><CircleCheck /></el-icon>
        <h3>已通过实名认证</h3>
        <p class="result-info">
          姓名：{{ userStore.userInfo?.realName }}<br>
          身份证：{{ maskIdCard(userStore.userInfo?.idCard) }}
        </p>
      </div>
    </el-card>

    <!-- 未认证，显示表单 -->
    <el-card v-else>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="人脸图片">
          <div class="face-upload">
            <el-upload
              ref="uploadRef"
              :show-file-list="false"
              :before-upload="handleFaceUpload"
              accept="image/*"
            >
              <div v-if="!facePreview" class="upload-placeholder">
                <el-icon :size="32"><Plus /></el-icon>
                <span>点击上传人脸照片</span>
              </div>
              <el-image v-else :src="facePreview" fit="cover" class="face-preview" />
            </el-upload>
            <p class="face-tip">
              请上传本人正面近照，用于公安人脸比对认证
            </p>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交认证
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Plus } from '@element-plus/icons-vue'
import { realNameAuth } from '@/api/user'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const facePreview = ref('')
const faceImage = ref('')

const form = reactive({
  realName: '',
  idCard: '',
  phone: ''
})

const isVerified = computed(() => userStore.userInfo?.isRealName === 1)

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function maskIdCard(id) {
  if (!id || id.length < 10) return id || '—'
  return id.substring(0, 4) + '**********' + id.substring(14)
}

// 人脸图片上传 → 拿到URL
async function handleFaceUpload(file) {
  facePreview.value = URL.createObjectURL(file)
  try {
    const res = await uploadFile(file, 'common')
    faceImage.value = res.url
  } catch {
    facePreview.value = ''
    ElMessage.error('图片上传失败')
  }
  return false
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!faceImage.value) {
    ElMessage.warning('请上传人脸照片')
    return
  }

  submitting.value = true
  try {
    await realNameAuth({
      realName: form.realName,
      idCard: form.idCard,
      phone: form.phone,
      imageUrl: faceImage.value
    })
    ElMessage.success('实名认证成功')
    // 刷新用户信息
    await userStore.fetchUserInfo()
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.realname-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}

.result-card {
  text-align: center;
}
.result-content {
  padding: 20px 0;
}
.result-content h3 {
  margin: 12px 0 8px;
  font-size: 18px;
  color: #303133;
}
.result-info {
  font-size: 14px;
  color: #909399;
  line-height: 1.8;
}

.face-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
.upload-placeholder {
  width: 200px;
  height: 200px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  cursor: pointer;
  transition: border-color 0.2s;
}
.upload-placeholder:hover {
  border-color: #409EFF;
  color: #409EFF;
}
.face-preview {
  width: 200px;
  height: 200px;
  border-radius: 8px;
}
.face-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
</style>
