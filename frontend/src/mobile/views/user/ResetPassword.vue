<template>
  <div class="reset-page">
    <!-- 品牌条 -->
    <div class="brand-bar">
      <router-link to="/login" class="brand-back"><i class="fas fa-chevron-left"></i></router-link>
      <img src="/images/logo.jpg" class="brand-logo" />
      <span class="brand-name">有宠</span>
    </div>

    <!-- 表单卡片 -->
    <div class="card-wrap">
      <div class="form-card">
        <h2 class="form-title">重置密码</h2>
        <p class="form-sub">通过注册时绑定的手机号验证身份</p>

        <!-- 步骤指示器 -->
        <div class="steps">
          <div class="step" :class="{ active: step >= 1, done: step > 1 }">
            <span class="step-num">{{ step > 1 ? '✓' : '1' }}</span>
            <span class="step-label">验证身份</span>
          </div>
          <div class="step-line" :class="{ done: step > 1 }"></div>
          <div class="step" :class="{ active: step >= 2 }">
            <span class="step-num">2</span>
            <span class="step-label">设置密码</span>
          </div>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" @keyup.enter="handleSubmit">
          <el-form-item prop="username">
            <div class="i-wrap">
              <i class="i-icon fas fa-user"></i>
              <el-input v-model="form.username" placeholder="用户名" />
            </div>
          </el-form-item>

          <el-form-item prop="phone">
            <div class="i-wrap">
              <i class="i-icon fas fa-mobile-alt"></i>
              <el-input v-model="form.phone" placeholder="注册时使用的手机号" maxlength="11" />
            </div>
          </el-form-item>

          <el-form-item prop="smsCode">
            <div class="sms-row">
              <div class="flex-1 i-wrap">
                <i class="i-icon fas fa-shield-alt"></i>
                <el-input v-model="form.smsCode" placeholder="短信验证码" maxlength="6" />
              </div>
              <button type="button" class="sms-btn" :disabled="smsCountdown > 0 || !validPhone" @click="handleSendSms">
                {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
              </button>
            </div>
          </el-form-item>

          <el-form-item prop="newPassword">
            <div class="i-wrap">
              <i class="i-icon fas fa-lock"></i>
              <el-input v-model="form.newPassword" type="password" placeholder="新密码（6-20个字符）" show-password />
            </div>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <div class="i-wrap">
              <i class="i-icon fas fa-lock"></i>
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认新密码" show-password />
            </div>
          </el-form-item>

          <el-form-item>
            <button type="button" class="form-btn" :disabled="submitting" @click="handleSubmit">
              {{ submitting ? '重置中...' : '重置密码' }}
            </button>
          </el-form-item>
        </el-form>

        <div class="foot">
          <router-link to="/login" class="link">返回登录</router-link>
        </div>
      </div>
    </div>

    <CaptchaSlider ref="captchaRef" />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const router = useRouter()
const formRef = ref(null)
const captchaRef = ref(null)
const submitting = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
const step = ref(1)
let smsTimer = null

const form = reactive({
  username: '',
  phone: '',
  smsCode: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (r, v, cb) => {
  if (v !== form.newPassword) cb(new Error('两次输入的密码不一致'))
  else cb()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const validPhone = computed(() => /^1[3-9]\d{9}$/.test(form.phone))

async function handleSendSms() {
  if (smsSending.value || smsCountdown.value > 0) return
  if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }
  try {
    const c = await captchaRef.value.showCaptcha()
    smsSending.value = true
    await sendSmsCode({ phone: form.phone, type: 'reset', ...c })
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000)
  } catch {} finally { smsSending.value = false }
}

async function handleSubmit() {
  const v = await formRef.value.validate().catch(() => false)
  if (!v) return
  submitting.value = true
  try {
    await resetPassword({
      username: form.username,
      phone: form.phone,
      smsCode: form.smsCode,
      newPassword: form.newPassword
    })
    step.value = 2
    ElMessage.success('密码重置成功，请重新登录')
    setTimeout(() => router.push('/login'), 1500)
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.reset-page {
  min-height: 100vh; min-height: 100dvh;
  background: linear-gradient(180deg, #c19a6b 0%, #f7f1e5 40%);
  padding-bottom: 40px;
}

/* === 品牌条 === */
.brand-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px;
}
.brand-back { color: #fff; font-size: 18px; text-decoration: none; display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; }
.brand-logo { width: 36px; height: 36px; border-radius: 10px; }
.brand-name { font-size: 20px; font-weight: 700; color: #fff; }

/* === 表单卡片 === */
.card-wrap { padding: 16px 16px 0; }
.form-card {
  width: 100%;
  padding: 24px 20px 20px;
  border-radius: 16px;
  background: rgba(254,250,245,0.97);
  border: 1px solid rgba(210,195,175,0.4);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04), 0 8px 24px rgba(0,0,0,0.06);
}
.form-title { text-align: center; font-size: 22px; font-weight: 700; color: #5a4a42; margin: 0 0 4px; }
.form-sub { text-align: center; font-size: 13px; color: #a09080; margin: 0 0 20px; }

/* === 步骤指示器 === */
.steps { display: flex; align-items: center; justify-content: center; margin-bottom: 24px; }
.step { display: flex; align-items: center; gap: 6px; }
.step-num {
  width: 24px; height: 24px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600;
  background: #ece4d8; color: #a09080;
}
.step.active .step-num { background: #c19a6b; color: #fff; }
.step.done .step-num { background: #8ab8a0; color: #fff; }
.step-label { font-size: 13px; color: #a09080; }
.step.active .step-label { color: #5a4a42; }
.step-line { width: 40px; height: 2px; background: #ece4d8; margin: 0 8px; }
.step-line.done { background: #8ab8a0; }

/* === 输入框 === */
.i-wrap { position: relative; width: 100%; }
.i-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #b5a898; font-size: 16px; z-index: 2; pointer-events: none; }
.form-card :deep(.el-input__wrapper) { padding-left: 42px !important; height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
.form-card :deep(.el-input__wrapper.is-focus) { border-color: #8ab8a0; box-shadow: 0 0 0 2px rgba(139,184,160,0.15) !important; }
.form-card :deep(.el-input__inner) { font-size: 15px; color: #5a4a42; }
.form-card :deep(.el-form-item) { padding-bottom: 16px; margin-bottom: 0; }
.form-card :deep(.el-form-item__error) { font-size: 12px; bottom: 2px; }

/* === 短信按钮 === */
.sms-row { display: flex; gap: 10px; width: 100%; }
.flex-1 { flex: 1; }
.sms-btn { flex-shrink: 0; min-width: 100px; height: 46px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 13px; cursor: pointer; padding: 0 12px; white-space: nowrap; }
.sms-btn:active { background: #f0e8dc; }
.sms-btn:disabled { color: #b5a898; cursor: not-allowed; }

/* === 按钮 === */
.form-btn {
  width: 100%; height: 48px; border: none; border-radius: 12px;
  background: linear-gradient(135deg, #c19a6b 0%, #b0895a 100%);
  color: #fff; font-size: 16px; font-weight: 600; letter-spacing: 2px;
  cursor: pointer; box-shadow: 0 4px 14px rgba(177,137,90,0.3);
}
.form-btn:active { transform: scale(0.98); }
.form-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; box-shadow: none; }

/* === 底部 === */
.foot { text-align: center; font-size: 14px; color: #a09080; margin-top: 16px; }
.link { color: #8ab8a0; font-weight: 500; text-decoration: none; }
</style>
