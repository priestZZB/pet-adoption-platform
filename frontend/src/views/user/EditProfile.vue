<template>
  <div class="edit-page">
    <h3 class="page-title">编辑个人资料</h3>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        style="max-width:500px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="50" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>

        <el-form-item>
          <el-button class="save-btn" :loading="submitting" @click="handleSubmit">
            保存修改
          </el-button>
          <el-button class="cancel-btn" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updateUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  nickname: '',
  email: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

onMounted(() => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.email = userStore.userInfo.email || ''
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updateUserInfo({
      nickname: form.nickname,
      email: form.email
    })
    await userStore.fetchUserInfo()
    ElMessage.success('资料已更新')
    router.push('/user/profile')
  } catch {}
  finally { submitting.value = false }
}
</script>

<style scoped>
.edit-page { max-width: 700px; margin: 0 auto; padding: 24px 0 40px; }
.page-title { font-size: 20px; color: var(--yc-text-primary); margin: 0 0 20px; }
:deep(.el-card) { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-card); background: var(--yc-bg-card); }
:deep(.save-btn) { background: var(--yc-btn-primary); border: 1px solid var(--yc-border); color: var(--yc-btn-text); border-radius: var(--yc-radius-btn); font-weight: 500; }
:deep(.save-btn:hover) { background: var(--yc-btn-hover); border-color: var(--yc-border-hover); color: var(--yc-btn-text); }
:deep(.cancel-btn) { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-btn); color: var(--yc-text-primary); }
:deep(.cancel-btn:hover) { border-color: var(--yc-border-hover); color: var(--yc-accent); }
</style>
