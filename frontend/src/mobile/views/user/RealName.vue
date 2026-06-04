<template>
  <div class="page">
    <div class="card-wrap">
      <MobileCard>
        <div class="form-group"><label>真实姓名</label><el-input v-model="form.realName" placeholder="与身份证一致" /></div>
        <div class="form-group"><label>身份证号</label><el-input v-model="form.idCard" placeholder="18位身份证号" maxlength="18" /></div>
        <div class="form-group"><label>人脸照片</label>
          <div class="upload-row">
            <button class="upload-btn" @click="handleUpload">选择照片</button>
            <span v-if="faceImage" class="upload-ok"><i class="fas fa-check-circle"></i> 已选择</span>
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
        </div>
        <button class="save-btn" :disabled="submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '提交认证' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { realNameAuth } from '@/api/user'
import MobileCard from '../../components/MobileCard.vue'

const fileInput = ref(null)
const faceImage = ref(null)
const submitting = ref(false)
const form = reactive({ realName: '', idCard: '' })

function handleUpload() { fileInput.value?.click() }

function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => { faceImage.value = reader.result }
  reader.readAsDataURL(file)
}

async function handleSubmit() {
  if (!form.realName || !form.idCard) { ElMessage.warning('请填写完整信息'); return }
  if (!faceImage.value) { ElMessage.warning('请上传人脸照片'); return }
  submitting.value = true
  try {
    await realNameAuth({ realName: form.realName, idCard: form.idCard, faceImage: faceImage.value })
    ElMessage.success('认证信息已提交，请等待审核')
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.page { padding: 12px 16px; }
.card-wrap { margin-bottom: 12px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
.form-group :deep(.el-input__wrapper) { height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
.upload-row { display: flex; align-items: center; gap: 10px; }
.upload-btn { height: 40px; padding: 0 16px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.upload-ok { font-size: 13px; color: #8ab8a0; }
.save-btn { width: 100%; height: 46px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
</style>
