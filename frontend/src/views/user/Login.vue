<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">宠物领养救助平台</h2>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" stretch>
        <!-- Tab 1：用户名+密码登录 -->
        <el-tab-pane label="用户名登录" name="username">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
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
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="login-links">
            <router-link to="/register">去注册</router-link>
            <router-link to="/reset-password">忘记密码</router-link>
          </div>
        </el-tab-pane>

        <!-- Tab 2：手机号+短信验证码登录 -->
        <el-tab-pane label="手机号登录" name="phone">
          <el-form
            ref="phoneFormRef"
            :model="phoneForm"
            :rules="phoneRules"
            label-width="0"
            size="large"
            @keyup.enter="handlePhoneLogin"
          >
            <el-form-item prop="phone">
              <el-input
                v-model="phoneForm.phone"
                placeholder="请输入手机号"
                :prefix-icon="Iphone"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="sms-row">
                <el-input
                  v-model="phoneForm.smsCode"
                  placeholder="请输入短信验证码"
                  maxlength="6"
                />
                <el-button
                  :disabled="smsCountdown > 0 || !validPhone"
                  :loading="smsSending"
                  @click="handleSendSms(phoneForm.phone)"
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
                @click="handlePhoneLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="login-links">
            <router-link to="/register">去注册</router-link>
            <router-link to="/reset-password">忘记密码</router-link>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import { login, phoneLogin, getUserInfo } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import { useUserStore } from '@/stores/user'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('username')
const formRef = ref(null)
const phoneFormRef = ref(null)
const submitting = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

// 用户名+密码登录 表单
const form = reactive({
  username: '',
  password: ''
})

// 手机号登录 表单
const phoneForm = reactive({
  phone: '',
  smsCode: ''
})

// 滑块验证码数据
const captchaData = reactive({
  ticket: '',
  randstr: '',
  captchaSign: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码6-20个字符', trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 判断手机号是否有效
const validPhone = computed(() => /^1[3-9]\d{9}$/.test(phoneForm.phone))

// 滑块验证码回调
function onCaptchaVerified(data) {
  Object.assign(captchaData, data)
}

// 发送短信验证码
async function handleSendSms(phone) {
  if (smsSending.value || smsCountdown.value > 0) return
  smsSending.value = true
  try {
    await sendSmsCode(phone)
    ElMessage.success('验证码已发送')
    startSmsCountdown()
  } catch {
    // 错误由请求拦截器统一处理
  } finally {
    smsSending.value = false
  }
}

// 60s倒计时
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

// 用户名+密码登录
async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!captchaData.ticket) {
    ElMessage.warning('请先完成滑块验证')
    return
  }

  submitting.value = true
  try {
    const res = await login({
      username: form.username,
      password: form.password,
      ticket: captchaData.ticket,
      randstr: captchaData.randstr,
      captchaSign: captchaData.captchaSign
    })
    userStore.setToken(res.token)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // 错误由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// 手机号登录
async function handlePhoneLogin() {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (!captchaData.ticket) {
    ElMessage.warning('请先完成滑块验证')
    return
  }

  submitting.value = true
  try {
    const res = await phoneLogin({
      phone: phoneForm.phone,
      smsCode: phoneForm.smsCode,
      ticket: captchaData.ticket,
      randstr: captchaData.randstr,
      captchaSign: captchaData.captchaSign
    })
    userStore.setToken(res.token)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // 错误由请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.login-title {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
  color: #303133;
}
.login-links {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-top: 8px;
}
.login-links a {
  color: #409EFF;
  text-decoration: none;
}
.login-links a:hover {
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
