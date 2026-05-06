<template>
  <div class="reset-page">
    <div class="reset-card">
      <h2 class="reset-title">找回密码</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleReset"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="注册时使用的手机号"
            :prefix-icon="Iphone"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item prop="smsCode">
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

        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="新密码（6-20个字符）"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认新密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <CaptchaSlider @verified="onCaptchaVerified" />

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            style="width:100%"
            @click="handleReset"
          >
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="reset-footer">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import { resetPassword } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

const form = reactive({
  username: '',
  phone: '',
  smsCode: '',
  newPassword: '',
  confirmPassword: ''
})

const captchaData = reactive({
  ticket: '',
  randstr: '',
  captchaSign: ''
})

// 验证密码确认
const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
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

async function handleReset() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!captchaData.ticket) {
    ElMessage.warning('请先完成滑块验证')
    return
  }

  submitting.value = true
  try {
    await resetPassword({
      username: form.username,
      phone: form.phone,
      smsCode: form.smsCode,
      newPassword: form.newPassword,
      ticket: captchaData.ticket,
      randstr: captchaData.randstr,
      captchaSign: captchaData.captchaSign
    })
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.reset-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.reset-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.reset-title {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
  color: #303133;
}
.reset-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
.reset-footer a {
  color: #409EFF;
  text-decoration: none;
}
.reset-footer a:hover {
  text-decoration: underline;
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
