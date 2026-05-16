<template>
  <div class="page">
    <div class="auth-bg"></div>
    <div class="main">
      <div class="card">
        <div class="card-body">
          <!-- 左：装饰 -->
          <div class="card-left">
            <div class="deco-icon">🔑</div>
            <h2 class="deco-title">忘记密码了？</h2>
            <p class="deco-desc">别担心，通过注册时绑定的手机号<br/>即可快速重置密码</p>
            <div class="deco-steps">
              <div class="deco-step"><span class="step-num">1</span> 输入用户名</div>
              <div class="deco-step"><span class="step-num">2</span> 验证手机号</div>
              <div class="deco-step"><span class="step-num">3</span> 短信验证</div>
              <div class="deco-step"><span class="step-num">4</span> 设置新密码</div>
            </div>
          </div>
          <!-- 右：重置密码表单 -->
          <div class="card-right">
            <div class="form-header">
              <img src="/images/logo.jpg" class="form-logo" />
              <span class="form-brand">有宠</span>
            </div>
            <p class="form-sub">重置你的密码 🔑</p>

            <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" @keyup.enter="handleReset">
              <el-form-item prop="username">
                <div class="i-wrap"><i class="i-icon fas fa-user"></i><el-input v-model="form.username" placeholder="用户名" /></div>
              </el-form-item>
              <el-form-item prop="phone">
                <div class="i-wrap"><i class="i-icon fas fa-mobile-alt"></i><el-input v-model="form.phone" placeholder="注册时使用的手机号" maxlength="11" /></div>
              </el-form-item>
              <el-form-item prop="smsCode">
                <div class="sms-row">
                  <div class="i-wrap flex-1"><i class="i-icon fas fa-shield-alt"></i><el-input v-model="form.smsCode" placeholder="短信验证码" maxlength="6" /></div>
                  <button type="button" class="sms-btn" :disabled="smsCountdown > 0 || !validPhone" :class="{ sending: smsSending }" @click="handleSendSms">{{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}</button>
                </div>
              </el-form-item>
              <el-form-item prop="newPassword">
                <div class="i-wrap"><i class="i-icon fas fa-lock"></i><el-input v-model="form.newPassword" type="password" placeholder="新密码（6-20个字符）" show-password /></div>
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <div class="i-wrap"><i class="i-icon fas fa-lock"></i><el-input v-model="form.confirmPassword" type="password" placeholder="确认新密码" show-password /></div>
              </el-form-item>
              <el-form-item><button type="button" class="form-btn" :disabled="submitting" @click="handleReset">{{ submitting ? '重置中…' : '重置密码' }}</button></el-form-item>
            </el-form>
            <div class="foot"><router-link to="/login" class="link">返回登录</router-link></div>
          </div>
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

const router = useRouter(); const formRef = ref(null); const captchaRef = ref(null)
const submitting = ref(false); const smsSending = ref(false); const smsCountdown = ref(0)
let smsTimer = null
const form = reactive({ username: '', phone: '', smsCode: '', newPassword: '', confirmPassword: '' })
const validateConfirm = (r, v, cb) => { if (v !== form.newPassword) cb(new Error('两次输入的密码不一致')); else cb() }
const rules = { username: [{ required: true, message: '请输入用户名', trigger: 'blur' }], phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }], smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { len: 6, message: '6位数字', trigger: 'blur' }], newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '6-20个字符', trigger: 'blur' }], confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }] }
const validPhone = computed(() => /^1[3-9]\d{9}$/.test(form.phone))


async function handleSendSms() { if (smsSending.value || smsCountdown.value > 0) return; if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }; try { const c = await captchaRef.value.showCaptcha(); smsSending.value = true; await sendSmsCode({ phone: form.phone, type: 'reset', ...c }); ElMessage.success('验证码已发送'); smsCountdown.value = 60; smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000) } catch {} finally { smsSending.value = false } }

async function handleReset() { const v = await formRef.value.validate().catch(() => false); if (!v) return; submitting.value = true; try { await resetPassword({ username: form.username, phone: form.phone, smsCode: form.smsCode, newPassword: form.newPassword }); ElMessage.success('密码重置成功，请重新登录'); router.push('/login') } catch {} finally { submitting.value = false } }
</script>

<style scoped>
.page { width: 100vw; min-height: 100vh; position: relative; display: flex; align-items: center; justify-content: center; background: #f7f1e5; }
.auth-bg { position: fixed; inset: 0; z-index: 0; background: linear-gradient(135deg, rgba(247,241,229,0.82) 0%, rgba(254,250,245,0.9) 100%), url('/images/bg.png') center/cover no-repeat; }
.main { position: relative; z-index: 1; width: 100%; max-width: 960px; padding: 20px; }

.card { background: #fefaf5; border: 1px solid #d1e7dd; border-radius: 20px; padding: 24px; box-shadow: 0 8px 32px rgba(0,0,0,0.05); }
.card-body { display: flex; gap: 28px; align-items: stretch; }

/* 左 */
.card-left { flex: 1; display: flex; flex-direction: column; justify-content: center; padding: 12px 8px; }
.deco-icon { font-size: 44px; text-align: center; margin-bottom: 10px; }
.deco-title { font-size: 22px; font-weight: 700; color: #5a4a42; text-align: center; margin: 0 0 6px; }
.deco-desc { font-size: 13.5px; color: #8a7a6a; text-align: center; line-height: 1.7; margin: 0 0 20px; }
.deco-steps { display: flex; flex-direction: column; gap: 8px; }
.deco-step { font-size: 14px; color: #5a4a42; padding: 8px 14px; background: #f7f1e5; border-radius: 10px; display: flex; align-items: center; gap: 8px; }
.step-num { width: 24px; height: 24px; border-radius: 50%; background: #d1e7dd; color: #5a4a42; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; flex-shrink: 0; }

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

.sms-row { display: flex; gap: 8px; width: 100%; }
.flex-1 { flex: 1; }
.sms-btn { flex-shrink: 0; min-width: 100px; height: 44px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 13px; cursor: pointer; padding: 0 10px; white-space: nowrap; }
.sms-btn:hover:not(:disabled) { background: #f0e8dc; }
.sms-btn:disabled { color: #b5a898; cursor: not-allowed; }

.form-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #f8e8d8; color: #5a4a42; font-size: 15px; font-weight: 500; cursor: pointer; letter-spacing: 1px; }
.form-btn:hover { background: #f0d8c0; }
.form-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.foot { text-align: center; font-size: 13px; color: #a09080; margin-top: 12px; }
.link { color: #5a4a42; font-weight: 500; text-decoration: none; }
.link:hover { text-decoration: underline; }

@media (max-width: 800px) { .card-body { flex-direction: column; } .card-right { width: 100%; } }
</style>
