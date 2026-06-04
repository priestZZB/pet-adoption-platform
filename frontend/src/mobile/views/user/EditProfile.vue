<template>
  <div class="edit-page">
    <div class="card-wrap">
      <MobileCard>
        <div class="form-group">
          <label>头像</label>
          <div class="avatar-row">
            <img :src="avatar || user?.avatar || '/images/logo.jpg'" class="avatar-img" />
            <button class="upload-btn" @click="handleUpload">更换头像</button>
            <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
          </div>
        </div>
        <div class="form-group">
          <label>昵称</label>
          <el-input v-model="form.nickname" placeholder="昵称" />
        </div>
        <button class="save-btn" :disabled="saving" @click="handleSave">{{ saving ? '保存中...' : '保存' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, uploadAvatar } from '@/api/user'
import MobileCard from '../../components/MobileCard.vue'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const fileInput = ref(null)
const avatar = ref('')
const saving = ref(false)

const form = reactive({
  nickname: user.value?.nickname || ''
})

function handleUpload() { fileInput.value?.click() }

async function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadAvatar(file)
    avatar.value = res?.avatar || res?.url || ''
    ElMessage.success('头像已更新')
  } catch {}
}

async function handleSave() {
  saving.value = true
  try {
    await updateUserInfo({ nickname: form.nickname })
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}
</script>

<style scoped>
.edit-page { padding: 12px 16px; }
.card-wrap { margin-bottom: 12px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
.form-group :deep(.el-input__wrapper) { height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
.avatar-row { display: flex; align-items: center; gap: 12px; }
.avatar-img { width: 64px; height: 64px; border-radius: 50%; object-fit: cover; }
.upload-btn { height: 36px; padding: 0 16px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 13px; cursor: pointer; }
.save-btn { width: 100%; height: 46px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
</style>
