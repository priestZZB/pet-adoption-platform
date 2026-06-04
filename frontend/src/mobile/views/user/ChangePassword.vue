<template>
  <div class="page">
    <div class="card-wrap">
      <MobileCard>
        <div class="form-group"><label>手机号</label><el-input v-model="form.phone" placeholder="绑定手机号" maxlength="11" /></div>
        <div class="form-group"><label>验证码</label><div class="sms-row"><div class="flex-1"><el-input v-model="form.smsCode" placeholder="短信验证码" maxlength="6" /></div><button class="sms-btn" :disabled="smsCountdown > 0 || !validPhone" @click="handleSendSms">{{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}</button></div></div>
        <div class="form-group"><label>新密码</label><el-input v-model="form.newPassword" type="password" placeholder="6-20个字符" show-password /></div>
        <div class="form-group"><label>确认密码</label><el-input v-model="form.confirmPassword" type="password" placeholder="确认新密码" show-password /></div>
        <button class="save-btn" :disabled="submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '修改密码' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import MobileCard from '../../components/MobileCard.vue'

const submitting = ref(false); const smsSending = ref(false); const smsCountdown = ref(0)
let smsTimer = null

const form = reactive({ phone: '', smsCode: '', newPassword: '', confirmPassword: '' })
const validPhone = computed(() => /^1[3-9]\d{9}$/.test(form.phone))

async function handleSendSms() {
  if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }
  smsSending.value = true
  try { await sendSmsCode({ phone: form.phone, type: 'reset' }); ElMessage.success('验证码已发送'); smsCountdown.value = 60; smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000) } catch {} finally { smsSending.value = false }
}

async function handleSubmit() {
  if (form.newPassword !== form.confirmPassword) { ElMessage.warning('两次输入的密码不一致'); return }
  if (form.newPassword.length < 6 || form.newPassword.length > 20) { ElMessage.warning('密码长度6-20个字符'); return }
  submitting.value = true
  try { await changePassword({ phone: form.phone, smsCode: form.smsCode, newPassword: form.newPassword }); ElMessage.success('密码修改成功') } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.page { padding: 12px 16px; }
.card-wrap { margin-bottom: 12px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
.form-group :deep(.el-input__wrapper) { height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
.sms-row { display: flex; gap: 10px; }
.flex-1 { flex: 1; }
.sms-btn { flex-shrink: 0; min-width: 100px; height: 46px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 13px; cursor: pointer; }
.sms-btn:disabled { color: #b5a898; }
.save-btn { width: 100%; height: 46px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
</style>
