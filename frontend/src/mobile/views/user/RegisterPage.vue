<template>
  <div class="register-page">
    <!-- 品牌条 -->
    <div class="brand-bar">
      <router-link to="/login" class="brand-back"><i class="fas fa-chevron-left"></i></router-link>
      <img src="/images/logo.jpg" class="brand-logo" />
      <span class="brand-name">有宠</span>
    </div>

    <!-- 表单卡片 -->
    <div class="card-wrap">
      <div class="form-card">
        <h2 class="form-title">创建账号</h2>
        <p class="form-sub">加入有宠大家庭，开启领养之旅</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" @keyup.enter="handleRegister">
          <!-- 系统分配用户名 -->
          <el-form-item>
            <div class="i-wrap">
              <i class="i-icon fas fa-user"></i>
              <el-input :model-value="generatedUsername" disabled placeholder="正在生成用户名..." />
              <span class="hint"><i class="fas fa-info-circle"></i></span>
            </div>
            <p class="field-tip">用户名由系统自动分配</p>
          </el-form-item>

          <el-form-item prop="password">
            <div class="i-wrap">
              <i class="i-icon fas fa-lock"></i>
              <el-input v-model="form.password" type="password" placeholder="密码（6-20个字符）" show-password />
            </div>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <div class="i-wrap">
              <i class="i-icon fas fa-lock"></i>
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password />
            </div>
          </el-form-item>

          <el-form-item prop="nickname">
            <div class="i-wrap">
              <i class="i-icon fas fa-tag"></i>
              <el-input v-model="form.nickname" placeholder="昵称（选填）" />
            </div>
          </el-form-item>

          <el-form-item prop="phone">
            <div class="i-wrap">
              <i class="i-icon fas fa-mobile-alt"></i>
              <el-input v-model="form.phone" placeholder="手机号" maxlength="11" />
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

          <div class="warn-tip">⚠️ 请牢记您的用户名和密码</div>

          <!-- 协议 -->
          <div class="agree-row">
            <label class="agree-label">
              <input type="checkbox" v-model="agreed" class="agree-check" />
              <span>我已阅读并同意</span>
            </label>
            <div class="legal-links">
              <a href="javascript:void(0)" @click="showLegal('privacy')">隐私政策</a>
              <span class="sep">|</span>
              <a href="javascript:void(0)" @click="showLegal('terms')">用户协议</a>
              <span class="sep">|</span>
              <a href="javascript:void(0)" @click="showLegal('disclaimer')">免责声明</a>
              <span class="sep">|</span>
              <a href="javascript:void(0)" @click="showLegal('help')">帮助中心</a>
            </div>
          </div>

          <el-form-item>
            <button type="button" class="form-btn" :disabled="submitting" @click="handleRegister">
              {{ submitting ? '注册中...' : '注册' }}
            </button>
          </el-form-item>
        </el-form>

        <div class="foot">
          已有账号？<router-link to="/login" class="link">去登录</router-link>
        </div>
      </div>
    </div>

    <CaptchaSlider ref="captchaRef" />
    <LegalDialogs ref="legalRef" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { register, generateUsername } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import CaptchaSlider from '@/components/CaptchaSlider.vue'
import LegalDialogs from '@/components/LegalDialogs.vue'

const router = useRouter()
const formRef = ref(null)
const captchaRef = ref(null)
const legalRef = ref(null)
const submitting = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
const generatedUsername = ref('')
const agreed = ref(false)
let smsTimer = null

const form = reactive({
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  smsCode: ''
})

const validateConfirm = (r, v, cb) => {
  if (v !== form.password) cb(new Error('两次输入的密码不一致'))
  else cb()
}

const rules = {
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20个字符', trigger: 'blur' }
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
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '6位数字', trigger: 'blur' }
  ]
}

const validPhone = computed(() => /^1[3-9]\d{9}$/.test(form.phone))

function showLegal(name) { legalRef.value?.open(name) }

onMounted(async () => {
  try { const r = await generateUsername(); generatedUsername.value = r?.username || '' } catch {}
})

async function handleSendSms() {
  if (smsSending.value || smsCountdown.value > 0) return
  if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }
  try {
    const c = await captchaRef.value.showCaptcha()
    smsSending.value = true
    await sendSmsCode({ phone: form.phone, ...c })
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000)
  } catch {} finally { smsSending.value = false }
}

async function handleRegister() {
  const v = await formRef.value.validate().catch(() => false)
  if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  submitting.value = true
  try {
    const r = await register({
      username: generatedUsername.value,
      password: form.password,
      nickname: form.nickname || '用户',
      phone: form.phone,
      smsCode: form.smsCode
    })
    const u = r?.username || '-'
    ElMessageBox.alert(
      `注册成功！<br>您的用户名为：<b>${u}</b><br>请使用用户名和密码登录。`,
      '注册成功',
      { dangerouslyUseHTMLString: true, confirmButtonText: '去登录', type: 'success' }
    ).then(() => router.push('/login?username=' + encodeURIComponent(u)))
  } catch (err) {
    if (err?.message?.includes('手机号已被绑定')) {
      ElMessageBox.confirm('该手机号已注册，是否直接登录？', '手机号已存在', {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => router.push('/login')).catch(() => {})
    }
  } finally { submitting.value = false }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh; min-height: 100dvh;
  background: linear-gradient(180deg, #c19a6b 0%, #f7f1e5 40%);
  padding-bottom: 40px;
}

/* === 品牌条 === */
.brand-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
}
.brand-back {
  color: #fff; font-size: 18px; text-decoration: none;
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px;
}
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

/* === 输入框 === */
.i-wrap { position: relative; width: 100%; }
.i-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #b5a898; font-size: 16px; z-index: 2; pointer-events: none; }
.form-card :deep(.el-input__wrapper) { padding-left: 42px !important; height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
.form-card :deep(.el-input__wrapper.is-focus) { border-color: #8ab8a0; box-shadow: 0 0 0 2px rgba(139,184,160,0.15) !important; }
.form-card :deep(.el-input__inner) { font-size: 15px; color: #5a4a42; }
.form-card :deep(.el-input.is-disabled .el-input__wrapper) { background: #f5f2ec; }
.form-card :deep(.el-form-item) { padding-bottom: 16px; margin-bottom: 0; }
.form-card :deep(.el-form-item__error) { font-size: 12px; bottom: 2px; }
.hint { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: #b5a898; font-size: 14px; z-index: 2; }
.field-tip { font-size: 11px; color: #b5a898; margin: 4px 0 0; padding-left: 4px; }

.warn-tip { text-align: center; font-size: 12px; color: #b5a898; margin: -6px 0 12px; }

/* === 短信按钮 === */
.sms-row { display: flex; gap: 10px; width: 100%; }
.flex-1 { flex: 1; }
.sms-btn {
  flex-shrink: 0; min-width: 100px; height: 46px;
  border: 1px solid #d1e7dd; border-radius: 10px;
  background: #fefaf5; color: #5a4a42; font-size: 13px;
  cursor: pointer; padding: 0 12px; white-space: nowrap;
}
.sms-btn:active { background: #f0e8dc; }
.sms-btn:disabled { color: #b5a898; cursor: not-allowed; }

/* === 协议 === */
.agree-row { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 4px; margin-bottom: 10px; font-size: 13px; color: #5a4a42; }
.agree-label { display: flex; align-items: center; gap: 4px; cursor: pointer; }
.agree-check { width: 16px; height: 16px; accent-color: #8ab8a0; cursor: pointer; flex-shrink: 0; }
.legal-links { width: 100%; padding-left: 20px; font-size: 12.5px; line-height: 1.8; display: flex; flex-wrap: wrap; align-items: center; gap: 2px; }
.legal-links a { color: #8ab8a0; text-decoration: none; padding: 2px 0; }
.sep { color: #c8dcd0; margin: 0 2px; }

/* === 注册按钮 === */
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
