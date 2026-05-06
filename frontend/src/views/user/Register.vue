<template>
  <div class="register-page">
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名（2-20个字符）"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（6-20个字符）"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="昵称（选填）"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号"
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

        <CaptchaSlider @verified="onCaptchaVerified" />

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            style="width:100%"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import { register } from '@/api/user'
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
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  smsCode: ''
})

const captchaData = reactive({
  ticket: '',
  randstr: '',
  captchaSign: ''
})

// 验证密码确认
const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
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

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!captchaData.ticket) {
    ElMessage.warning('请先完成滑块验证')
    return
  }

  submitting.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username,
      phone: form.phone,
      smsCode: form.smsCode,
      ticket: captchaData.ticket,
      randstr: captchaData.randstr,
      captchaSign: captchaData.captchaSign
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.register-title {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
  color: #303133;
}
.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
.register-footer a {
  color: #409EFF;
  text-decoration: none;
}
.register-footer a:hover {
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
