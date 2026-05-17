<template>
  <div class="page">
    <div class="auth-bg"></div>
    <div class="main">
      <div class="card">
        <div class="card-body">
          <!-- 左：装饰 -->
          <div class="card-left">
            <div class="deco-icon">🐾</div>
            <h2 class="deco-title">加入有宠大家庭</h2>
            <p class="deco-desc">注册账号，开启你的宠物领养之旅<br/>用领养代替购买，给毛孩子一个温暖的家</p>
            <div class="deco-list">
              <div class="deco-item"><span>🐱</span> 浏览待领养宠物</div>
              <div class="deco-item"><span>📋</span> 参加领养考试</div>
              <div class="deco-item"><span>🛒</span> 宠物商城购物</div>
              <div class="deco-item"><span>🤖</span> AI智能咨询</div>
            </div>
          </div>
          <!-- 右：注册表单 -->
          <div class="card-right">
            <div class="form-header">
              <img src="/images/logo.jpg" class="form-logo" />
              <span class="form-brand">有宠</span>
            </div>
            <p class="form-sub">创建你的账号 🐾</p>

            <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" @keyup.enter="handleRegister">
              <el-form-item>
                <div class="i-wrap">
                  <i class="i-icon fas fa-user"></i>
                  <el-input :model-value="generatedUsername" disabled placeholder="正在生成…" />
                  <el-tooltip content="用户名由系统分配，不可更改" placement="top">
                    <span class="hint"><i class="fas fa-info-circle"></i></span>
                  </el-tooltip>
                </div>
              </el-form-item>
              <el-form-item prop="password">
                <div class="i-wrap"><i class="i-icon fas fa-lock"></i><el-input v-model="form.password" type="password" placeholder="密码（6-20个字符）" show-password /></div>
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <div class="i-wrap"><i class="i-icon fas fa-lock"></i><el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password /></div>
              </el-form-item>
              <el-form-item prop="nickname">
                <div class="i-wrap"><i class="i-icon fas fa-tag"></i><el-input v-model="form.nickname" placeholder="昵称（选填）" /></div>
              </el-form-item>
              <el-form-item prop="phone">
                <div class="i-wrap"><i class="i-icon fas fa-mobile-alt"></i><el-input v-model="form.phone" placeholder="手机号" maxlength="11" /></div>
              </el-form-item>
              <el-form-item prop="smsCode">
                <div class="sms-row">
                  <div class="i-wrap flex-1"><i class="i-icon fas fa-shield-alt"></i><el-input v-model="form.smsCode" placeholder="短信验证码" maxlength="6" /></div>
                  <button type="button" class="sms-btn" :disabled="smsCountdown > 0 || !validPhone" :class="{ sending: smsSending }" @click="handleSendSms">{{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}</button>
                </div>
              </el-form-item>
              <div class="tip">⚠️ 请牢记您的用户名和密码</div>
              <!-- 协议勾选 -->
              <div class="agree-row">
                <label class="agree-label">
                  <input type="checkbox" v-model="agreed" class="agree-check" />
                  <span class="agree-text">我已阅读并同意</span>
                </label>
                <span class="agree-links">
                  <a href="javascript:void(0)" @click="showLegal('privacy')">隐私政策</a>
                  <span class="agree-sep">|</span>
                  <a href="javascript:void(0)" @click="showLegal('terms')">用户协议</a>
                  <span class="agree-sep">|</span>
                  <a href="javascript:void(0)" @click="showLegal('disclaimer')">免责声明</a>
                  <span class="agree-sep">|</span>
                  <a href="javascript:void(0)" @click="showLegal('help')">帮助中心</a>
                </span>
              </div>
              <el-form-item><button type="button" class="form-btn" :disabled="submitting" @click="handleRegister">{{ submitting ? '注册中…' : '注册' }}</button></el-form-item>
            </el-form>
            <div class="foot">已有账号？<router-link to="/login" class="link">去登录</router-link></div>
          </div>
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

const router = useRouter(); const formRef = ref(null); const captchaRef = ref(null); const legalRef = ref(null)
const submitting = ref(false); const smsSending = ref(false); const smsCountdown = ref(0); const generatedUsername = ref('')
const agreed = ref(false)
let smsTimer = null
const form = reactive({ password: '', confirmPassword: '', nickname: '', phone: '', smsCode: '' })
const validateConfirm = (r, v, cb) => { if (v !== form.password) cb(new Error('两次输入的密码不一致')); else cb() }
const rules = { password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '6-20个字符', trigger: 'blur' }], confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }], phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }], smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { len: 6, message: '6位数字', trigger: 'blur' }] }
const validPhone = computed(() => /^1[3-9]\d{9}$/.test(form.phone))
function showLegal(name) { legalRef.value?.open(name) }

onMounted(async () => { try { const r = await generateUsername(); generatedUsername.value = r?.username || '' } catch {} })

async function handleSendSms() { if (smsSending.value || smsCountdown.value > 0) return; if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }; try { const c = await captchaRef.value.showCaptcha(); smsSending.value = true; await sendSmsCode({ phone: form.phone, ...c }); ElMessage.success('验证码已发送'); smsCountdown.value = 60; smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000) } catch {} finally { smsSending.value = false } }

async function handleRegister() {
  const v = await formRef.value.validate().catch(() => false); if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  submitting.value = true
  try { const r = await register({ username: generatedUsername.value, password: form.password, nickname: form.nickname || '用户', phone: form.phone, smsCode: form.smsCode }); const u = r?.username || '—'; ElMessageBox.alert(`注册成功！<br>您的用户名为：<b>${u}</b><br>请使用用户名和密码登录。`, '注册成功', { dangerouslyUseHTMLString: true, confirmButtonText: '去登录', type: 'success' }).then(() => router.push('/login?username=' + encodeURIComponent(u))) } catch (err) { if (err?.message?.includes('手机号已被绑定')) { ElMessageBox.confirm('该手机号已注册，是否直接登录？', '手机号已存在', { confirmButtonText: '去登录', cancelButtonText: '取消', type: 'info' }).then(() => router.push('/login')).catch(() => {}) } } finally { submitting.value = false } }
</script>

<style scoped>
.page { width: 100vw; min-height: 100vh; position: relative; display: flex; align-items: center; justify-content: center; background: #f7f1e5; }
.auth-bg { position: fixed; inset: 0; z-index: 0; background: linear-gradient(135deg, rgba(247,241,229,0.82) 0%, rgba(254,250,245,0.9) 100%), url('/images/bg.png') center/cover no-repeat; box-sizing: border-box; }
.main { position: relative; z-index: 1; width: 100%; max-width: 960px; padding: 20px; }

.card { background: #fefaf5; border: 1px solid #d1e7dd; border-radius: 20px; padding: 24px; box-shadow: 0 8px 32px rgba(0,0,0,0.05); }
.card-body { display: flex; gap: 28px; align-items: stretch; }

/* 左 */
.card-left { flex: 1; display: flex; flex-direction: column; justify-content: center; padding: 12px 8px; }
.deco-icon { font-size: 44px; text-align: center; margin-bottom: 10px; }
.deco-title { font-size: 22px; font-weight: 700; color: #5a4a42; text-align: center; margin: 0 0 6px; }
.deco-desc { font-size: 13.5px; color: #8a7a6a; text-align: center; line-height: 1.7; margin: 0 0 20px; }
.deco-list { display: flex; flex-direction: column; gap: 8px; }
.deco-item { font-size: 14px; color: #5a4a42; padding: 8px 14px; background: #f7f1e5; border-radius: 10px; display: flex; align-items: center; gap: 8px; }

/* 右 */
.card-right { width: 360px; flex-shrink: 0; }
.form-header { display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 2px; }
.form-logo { width: 40px; height: 40px; border-radius: 10px; }
.form-brand { font-size: 22px; font-weight: 700; color: #5a4a42; }
.form-sub { text-align: center; font-size: 12.5px; color: #a09080; margin: 0 0 14px; }

.i-wrap { position: relative; width: 100%; }
.i-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #b5a898; font-size: 15px; z-index: 2; pointer-events: none; }
:deep(.el-input__wrapper) { padding-left: 42px !important; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; height: 44px; }
:deep(.el-input__wrapper:hover) { border-color: #b5d5c5; }
:deep(.el-input__wrapper.is-focus) { border-color: #8ab8a0; box-shadow: 0 0 0 3px rgba(139,184,160,0.12) !important; }
:deep(.el-form-item) { margin-bottom: 0; }
:deep(.el-form-item--large) { margin-bottom: 22px; }
:deep(.el-input__inner) { color: #5a4a42; font-size: 14px; }

.hint { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); z-index: 2; color: #b5a898; font-size: 14px; cursor: pointer; }
.tip { text-align: center; font-size: 12px; color: #b5a898; margin-bottom: 10px; margin-top: -6px; }

.sms-row { display: flex; gap: 8px; width: 100%; }
.flex-1 { flex: 1; }
.sms-btn { flex-shrink: 0; min-width: 100px; height: 44px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 13px; cursor: pointer; padding: 0 10px; white-space: nowrap; }
.sms-btn:hover:not(:disabled) { background: #f0e8dc; }
.sms-btn:disabled { color: #b5a898; cursor: not-allowed; }

.form-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #f8e8d8; color: #5a4a42; font-size: 15px; font-weight: 500; cursor: pointer; letter-spacing: 1px; }
.form-btn:hover { background: #f0d8c0; }
.form-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.agree-row { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; margin-bottom: 10px; font-size: 12.5px; }
.agree-label { display: flex; align-items: center; gap: 4px; cursor: pointer; }
.agree-check { width: 14px; height: 14px; cursor: pointer; accent-color: #8ab8a0; }
.agree-text { color: #a09080; }
.agree-links { display: flex; align-items: center; gap: 2px; flex-wrap: wrap; }
.agree-links a { color: #5a4a42; text-decoration: none; font-weight: 500; }
.agree-links a:hover { text-decoration: underline; }
.agree-sep { color: #d1e7dd; margin: 0 3px; }

.foot { text-align: center; font-size: 13px; color: #a09080; margin-top: 12px; }
.link { color: #5a4a42; font-weight: 500; text-decoration: none; }
.link:hover { text-decoration: underline; }

@media (max-width: 800px) { .card-body { flex-direction: column; } .card-right { width: 100%; } }
</style>
