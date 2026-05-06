<template>
  <div class="password-page">
    <h3 class="page-title">修改密码</h3>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        @keyup.enter="handleSubmit"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="新密码（6-20个字符）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入当前账号绑定的手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="smsCode">
          <div class="sms-row">
            <el-input
              v-model="form.smsCode"
              placeholder="短信验证码"
              maxlength="6"
            />
            <el-button
              :disabled="smsCountdown > 0 || !validPhone"
              :loading="smsSending"
              @click="handleSendSms"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <CaptchaSlider @verified="onCaptchaVerified" />

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            确认修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import { useUserStore } from '@/stores/user'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  phone: '',
  smsCode: ''
})

const captchaData = reactive({
  ticket: '',
  randstr: '',
  captchaSign: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const validPhone = computed(() => /^1[3-9]\d{9}$/.test(form.phone))

function onCaptchaVerified(data) {
  Object.assign(captchaData, data)
}

async function handleSendSms() {
  if (smsSending.value || smsCountdown.value > 0) return
  if (!validPhone.value) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  smsSending.value = true
  try {
    await sendSmsCode(form.phone)
    ElMessage.success('验证码已发送')
    startSmsCountdown()
  } catch {
    // 请求拦截器统一处理
  } finally {
    smsSending.value = false
  }
}

function startSmsCountdown() {
  smsCountdown.value = 60
  smsTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      clearInterval(smsTimer)
      smsTimer = null
    }
  }, 1000)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!captchaData.ticket) {
    ElMessage.warning('请先完成滑块验证')
    return
  }

  submitting.value = true
  try {
    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      phone: form.phone,
      smsCode: form.smsCode,
      ticket: captchaData.ticket,
      randstr: captchaData.randstr,
      captchaSign: captchaData.captchaSign
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.password-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}
.sms-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.sms-row .el-input {
  flex: 1;
}
.sms-row .el-button {
  flex-shrink: 0;
  min-width: 110px;
}
</style>
