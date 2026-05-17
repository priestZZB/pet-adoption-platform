<template>
  <div class="phone-page">
    <h3 class="page-title">修改手机号</h3>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        @keyup.enter="handleSubmit"
      >
        <el-form-item label="当前手机号">
          <el-input :model-value="currentPhone" disabled placeholder="加载中…" />
        </el-form-item>

        <el-form-item label="新手机号" prop="newPhone">
          <el-input v-model="form.newPhone" placeholder="请输入新手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="验证码" prop="smsCode">
          <div class="sms-row">
            <el-input v-model="form.smsCode" placeholder="请输入短信验证码" maxlength="6" style="flex:1" />
            <el-button
              :disabled="smsCountdown > 0 || !validPhone"
              :loading="smsSending"
              @click="handleSendSms"
              style="flex-shrink:0;width:120px"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button class="save-btn" :loading="submitting" @click="handleSubmit">
            保存修改
          </el-button>
          <el-button class="cancel-btn" @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <CaptchaSlider ref="captchaRef" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updateUserInfo } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import { useUserStore } from '@/stores/user'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const captchaRef = ref(null)
const submitting = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

const currentPhone = ref('')
const form = reactive({ newPhone: '', smsCode: '' })

const validPhone = computed(() => /^1[3-9]\d{9}$/.test(form.newPhone))

const rules = {
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '6位数字验证码', trigger: 'blur' }
  ]
}

onMounted(() => {
  if (userStore.userInfo) {
    currentPhone.value = userStore.userInfo.phone || '未设置'
  }
})

async function handleSendSms() {
  if (smsSending.value || smsCountdown.value > 0) return
  if (!validPhone.value) { ElMessage.warning('请输入正确的新手机号'); return }
  try {
    const c = await captchaRef.value.showCaptcha()
    smsSending.value = true
    await sendSmsCode({ phone: form.newPhone, type: 'register', ...c })
    ElMessage.success('验证码已发送到新手机号')
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null }
    }, 1000)
  } catch {}
  finally { smsSending.value = false }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateUserInfo({ phone: form.newPhone, smsCode: form.smsCode })
    await userStore.fetchUserInfo()
    ElMessage.success('手机号已更新')
    router.back()
  } catch {}
  finally { submitting.value = false }
}
</script>

<style scoped>
.phone-page { max-width: 600px; margin: 0 auto; padding: 24px 0 40px; }
.page-title { font-size: 20px; color: var(--yc-text-primary); margin: 0 0 20px; }
:deep(.el-card) { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-card); background: var(--yc-bg-card); }
.sms-row { display: flex; gap: 8px; width: 100%; }
.save-btn { background: var(--yc-btn-primary); border: 1px solid var(--yc-border); color: var(--yc-btn-text); border-radius: var(--yc-radius-btn); font-weight: 500; }
.save-btn:hover { background: var(--yc-btn-hover); border-color: var(--yc-border-hover); }
.cancel-btn { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-btn); color: var(--yc-text-primary); }
.cancel-btn:hover { border-color: var(--yc-border-hover); color: var(--yc-accent); }
</style>
