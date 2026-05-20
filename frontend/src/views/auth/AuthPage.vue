<template>
  <div class="auth-page">
    <div class="auth-bg"></div>

    <!-- ===== 品牌条（顶部，暖色背景全宽）===== -->
    <div class="brand-bar">
      <div class="brand-row">
        <img src="/images/logo.jpg" class="brand-logo" />
        <div>
          <div class="brand-name">有宠</div>
          <div class="brand-slogan">用领养代替购买 ❤️</div>
        </div>
      </div>
    </div>

    <!-- ===== 轮播满宽区（含浮层登录卡）===== -->
    <div class="banner-section">
      <!-- 轮播背景 -->
      <div class="banner-carousel-bg">
        <el-carousel
          class="login-carousel"
          height="100%"
          :interval="4500"
          indicator-position="none"
          arrow="never"
          autoplay
        >
          <el-carousel-item v-for="(item, idx) in banners" :key="idx">
            <div class="carousel-item-inner">
              <el-image :src="item.imageUrl" fit="cover" style="width:100%;height:100%" />
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 遮罩 -->
      <div class="banner-overlay"></div>

      <!-- 登录卡（浮在轮播上方，居中）-->
      <div class="login-overlay">
        <div class="login-card">
          <!-- 子标签 -->
          <div class="sub-tabs">
            <button :class="['st', { active: loginTab === 'username' }]" @click="loginTab = 'username'">密码登录</button>
            <button :class="['st', { active: loginTab === 'phone' }]" @click="loginTab = 'phone'">短信登录</button>
          </div>

          <el-form v-show="loginTab === 'username'" ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" size="large" @keyup.enter="handleLogin">
            <el-form-item prop="username" class="username-item">
              <div class="i-wrap">
                <el-input v-model="loginForm.username" placeholder="请输入用户名或手机号" @focus="showHistory" @blur="hideHistoryDelay">
                  <template #prefix><i class="fas fa-user"></i></template>
                </el-input>
                <div v-if="historyVisible && historyList.length > 0" class="history-dropdown">
                  <div class="history-title">最近登录</div>
                  <div v-for="(h, i) in historyList" :key="i" class="history-item" @mousedown.prevent="selectHistory(h)">
                    <i class="fas fa-user-circle"></i><span>{{ h }}</span>
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-form-item prop="password" class="password-item">
              <div class="i-wrap">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password>
                  <template #prefix><i class="fas fa-lock"></i></template>
                </el-input>
              </div>
            </el-form-item>
            <el-form-item><button type="button" class="login-btn" :disabled="submitting" @click="handleLogin">{{ submitting ? '登录中…' : '登录' }}</button></el-form-item>
            <div class="agree-row">
              <label class="agree-label">
                <input type="checkbox" v-model="rememberMe" class="agree-check" />
                <span class="agree-text">记住我</span>
              </label>
            </div>
            <div class="agree-row">
              <label class="agree-label">
                <input type="checkbox" v-model="agreed" class="agree-check" />
                <span class="agree-text">已阅读并同意</span>
              </label>
              <span class="agree-sep" style="margin:0 4px">|</span>
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
            <div class="switch-row">
              <span class="link" @click="loginTab = 'phone'">使用手机号登录 ›</span>
              <router-link to="/reset-password" class="link">忘记密码？</router-link>
            </div>
            <div class="register-row">
              还没有账号？<router-link to="/register" class="link">立即注册</router-link>
            </div>
          </el-form>

          <el-form v-show="loginTab === 'phone'" ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="0" size="large" @keyup.enter="handlePhoneLogin">
            <el-form-item prop="phone">
              <div class="i-wrap">
                <el-input v-model="phoneForm.phone" placeholder="请输入手机号" maxlength="11" @focus="showHistory" @blur="hideHistoryDelay">
                  <template #prefix><i class="fas fa-phone"></i></template>
                </el-input>
              </div>
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="sms-row">
                <div class="flex-1 i-wrap">
                  <el-input v-model="phoneForm.smsCode" placeholder="6位验证码" maxlength="6">
                    <template #prefix><i class="fas fa-lock"></i></template>
                  </el-input>
                </div>
                <button type="button" class="sms-btn" :disabled="smsSending || smsCountdown > 0" @click="handleSendSms">
                  {{ smsCountdown > 0 ? smsCountdown + 's' : (smsSending ? '发送中…' : '获取验证码') }}
                </button>
              </div>
            </el-form-item>
            <el-form-item><button type="button" class="login-btn" :disabled="submitting" @click="handlePhoneLogin">{{ submitting ? '登录中…' : '登录' }}</button></el-form-item>
            <div class="agree-row">
              <label class="agree-label">
                <input type="checkbox" v-model="rememberMe" class="agree-check" />
                <span class="agree-text">记住我</span>
              </label>
            </div>
            <div class="agree-row">
              <label class="agree-label">
                <input type="checkbox" v-model="agreed" class="agree-check" />
                <span class="agree-text">已阅读并同意</span>
              </label>
              <span class="agree-sep" style="margin:0 4px">|</span>
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
            <div class="switch-row">
              <span class="link" @click="loginTab = 'username'">使用密码登录 ›</span>
              <router-link to="/reset-password" class="link">忘记密码？</router-link>
            </div>
            <div class="register-row">
              还没有账号？<router-link to="/register" class="link">立即注册</router-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- ===== 公告区域（在轮播下方）===== -->
    <div class="notice-area">
      <div class="notice-inner">
        <div class="notice-head" v-if="notices.length > 0">
          <span class="n-icon">📢</span>
          <span class="n-label">平台公告</span>
          <span class="n-more" @click="showAllNotices">查看全部 ›</span>
        </div>
        <div class="notice-grid" v-if="notices.length > 0">
          <div class="notice-card" v-for="(item, idx) in notices.slice(0, 4)" :key="idx" @click="showNoticeDetail(item)">
            <div class="nc-row">
              <span class="nc-bullet"></span>
              <span class="nc-text">{{ item.title }}</span>
              <span class="nc-time">{{ formatDate(item.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="noticeVisible" :title="noticeDetail?.title" width="520px" top="12vh">
      <div class="notice-body">{{ noticeDetail?.content }}</div>
    </el-dialog>
    <CaptchaSlider ref="captchaRef" />
    <LegalDialogs ref="legalRef" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { login, phoneLogin } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import { getNoticeList } from '@/api/notice'
import { useUserStore } from '@/stores/user'
import CaptchaSlider from '@/components/CaptchaSlider.vue'
import LegalDialogs from '@/components/LegalDialogs.vue'

const router = useRouter(); const route = useRoute(); const userStore = useUserStore()
const captchaRef = ref(null); const legalRef = ref(null); const agreed = ref(false)

const WELCOME_IMAGE = '/images/welcome.jpg'
const welcomeBanner = { imageUrl: WELCOME_IMAGE }
const banners = ref([welcomeBanner]); const notices = ref([]); const noticeVisible = ref(false); const noticeDetail = ref(null)
async function loadBanners() { try { const r = await request.get('/banners'); if (r && r.length > 0) { banners.value = [welcomeBanner, ...r.map(b => ({ imageUrl: b.imageUrl }))] } } catch {} }
async function loadNotices() { try { const r = await getNoticeList(); notices.value = Array.isArray(r) ? r : [] } catch {} }
function showNoticeDetail(i) { noticeDetail.value = i; noticeVisible.value = true }
function showAllNotices() { router.push('/notices') }
function formatDate(s) { if (!s) return ''; const d = new Date(s); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` }
function showLegal(name) { legalRef.value?.open(name) }

onMounted(() => {
  loadBanners(); loadNotices(); loadHistory()
  const u = route.query.username
  if (u) { loginForm.username = u }
  showLoginReason()
})

// 监听路由参数变化（已在登录页时被重定向过来也能显示）
watch(() => route.query.reason, (nv) => {
  if (nv) showLoginReason()
})

function showLoginReason() {
  const reason = route.query.reason
  if (reason) {
    ElMessage.warning(decodeURIComponent(reason))
  }
}

const loginFormRef = ref(null); const phoneFormRef = ref(null)
const loginTab = ref('username'); const submitting = ref(false); const smsSending = ref(false); const smsCountdown = ref(0)
const rememberMe = ref(false)
const historyVisible = ref(false)
const historyList = ref([])
let historyHideTimer = null
let smsTimer = null
const loginForm = reactive({ username: '', password: '' })
const phoneForm = reactive({ phone: '', smsCode: '' })
const loginRules = { username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }

const HISTORY_KEY = 'login_history_usernames'
function loadHistory() {
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    historyList.value = raw ? JSON.parse(raw) : []
  } catch { historyList.value = [] }
}
function saveHistory(username) {
  // 先重新加载 localStorage，确保拿到最新的历史数据
  loadHistory()
  let list = [username]
  for (const h of historyList.value) {
    if (h !== username) list.push(h)
  }
  if (list.length > 5) list = list.slice(0, 5)
  historyList.value = list
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
}
function showHistory() {
  if (historyHideTimer) clearTimeout(historyHideTimer)
  loadHistory()
  historyVisible.value = true
}
function hideHistoryDelay() {
  historyHideTimer = setTimeout(() => { historyVisible.value = false }, 200)
}
function selectHistory(value) {
  if (loginTab.value === 'username') {
    loginForm.username = value
    loginForm.password = ''  // 切换账号时清空密码
  } else {
    phoneForm.phone = value
  }
  rememberMe.value = true
  historyVisible.value = false
  setTimeout(() => {
    const selector = loginTab.value === 'username' ? '.password-item .el-input__inner' : '.sms-row .el-input__inner'
    const next = document.querySelector(selector)
    if (next) next.focus()
  }, 100)
}

watch(rememberMe, (val) => {
  if (!val) {
    const current = loginTab.value === 'username' ? loginForm.username : phoneForm.phone
    if (current) {
      loadHistory()
      const filtered = historyList.value.filter(h => h !== current)
      if (filtered.length !== historyList.value.length) {
        historyList.value = filtered
        localStorage.setItem(HISTORY_KEY, JSON.stringify(filtered))
      }
    }
  }
})

const phoneRules = { phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }], smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { len: 6, message: '6位数字', trigger: 'blur' }] }
const validPhone = computed(() => /^1[3-9]\d{9}$/.test(phoneForm.phone))

async function handleSendSms() {
  if (smsSending.value || smsCountdown.value > 0) return
  if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }
  try { const c = await captchaRef.value.showCaptcha(); smsSending.value = true; await sendSmsCode({ phone: phoneForm.phone, type: 'login', ...c }); ElMessage.success('验证码已发送'); smsCountdown.value = 60; smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000) } catch (e) {} finally { smsSending.value = false }
}

async function handleLogin() {
  const v = await loginFormRef.value.validate().catch(() => false); if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  try { const c = await captchaRef.value.showCaptcha(); submitting.value = true; const r = await login({ username: loginForm.username, password: loginForm.password, ...c }); userStore.setToken(r.token); await userStore.fetchUserInfo(); if (rememberMe.value) saveHistory(loginForm.username); ElMessage.success('登录成功'); router.push('/') } catch (e) {} finally { submitting.value = false }
}

async function handlePhoneLogin() {
  const v = await phoneFormRef.value.validate().catch(() => false); if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  submitting.value = true; try { const r = await phoneLogin({ phone: phoneForm.phone, smsCode: phoneForm.smsCode }); userStore.setToken(r.token); await userStore.fetchUserInfo(); if (rememberMe.value) saveHistory(phoneForm.phone); ElMessage.success('登录成功'); router.push('/') } catch (e) {} finally { submitting.value = false }
}

onUnmounted(() => { if (smsTimer) clearInterval(smsTimer) })
</script>

<style scoped>
.auth-page {
  width: 100%;
  min-height: 100vh;
  position: relative;
  background: #f7f1e5;
}
.auth-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: linear-gradient(135deg, rgba(247,241,229,0.78) 0%, rgba(254,250,245,0.88) 100%), url('/images/bg.png') center/cover no-repeat;
}

/* ===== 轮播满宽区 ===== */
.banner-section {
  position: relative;
  width: 100%;
  max-width: 1400px;
  margin: 24px auto 0;
  aspect-ratio: 3 / 1;
}

/* 轮播背景 — 填满 banner-section */
.banner-carousel-bg {
  position: absolute;
  inset: 0;
}
.banner-carousel-bg :deep(.el-carousel),
.banner-carousel-bg :deep(.el-carousel__container),
.banner-carousel-bg :deep(.el-carousel__item) {
  height: 100% !important;
}
.login-carousel {
  height: 100% !important;
}
.carousel-item-inner {
  width: 100%;
  height: 100%;
}

/* 遮罩 — 深色半透明渐变，让文字更清晰 */
.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.15) 100%);
  z-index: 1;
  pointer-events: none;
}

/* ===== 品牌条（顶部全宽暖色背景）===== */
.brand-bar {
  position: relative;
  z-index: 2;
  width: 100vw;
  left: 50%;
  margin-left: -50vw;
  background: linear-gradient(135deg, #c19a6b 0%, #b0895a 100%);
  padding: 16px 0;
}
.brand-row {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 24px;
}
.brand-logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}
.brand-name {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}
.brand-slogan {
  font-size: 14px;
  color: rgba(255,255,255,0.85);
  margin-top: 2px;
}

/* ===== 登录浮层卡（靠右）===== */
.login-overlay {
  position: absolute;
  top: 50%;
  right: 48px;
  transform: translateY(-50%);
  z-index: 2;
}
.login-card {
  width: 340px;
  padding: 14px 24px 12px;
  display: flex;
  flex-direction: column;
  background: rgba(254,250,245,0.95);
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 14px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.15);
  backdrop-filter: blur(12px);
}
.login-card :deep(.el-form) {
  flex: 1;
}

.sub-tabs {
  display: flex;
  border-bottom: 2px solid #d1e7dd;
  margin-bottom: 10px;
}
.st {
  flex: 1;
  border: none;
  background: none;
  padding: 8px 0 6px;
  font-size: 13px;
  font-weight: 500;
  color: #a09080;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
  text-decoration: none;
  text-align: center;
}
.st.active { color: #5a4a42; }
.st.active::after { content: ''; position: absolute; bottom: -2px; left: 20%; right: 20%; height: 2px; background: #5a4a42; border-radius: 2px; }
.st:hover { color: #5a4a42; }

.i-wrap { position: relative; width: 100%; }
:deep(.el-input__wrapper) { border-radius: 8px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; height: 38px; }
:deep(.el-input__wrapper:hover) { border-color: #b5d5c5; }
:deep(.el-input__wrapper.is-focus) { border-color: #8ab8a0; box-shadow: 0 0 0 2px rgba(139,184,160,0.12) !important; }
:deep(.el-form-item) { margin-bottom: 0; padding-bottom: 18px; }
:deep(.el-form-item--large) { margin-bottom: 0; }
:deep(.el-form-item__error) { position: absolute; bottom: 2px; left: 0; font-size: 11px; }
:deep(.el-input__inner) { color: #5a4a42; font-size: 14px; }
:deep(.el-input__prefix) { font-size: 15px; color: #b5a898; margin-right: 6px; }

.login-btn { width: 100%; height: 40px; border: none; border-radius: 8px; background: #f8e8d8; color: #5a4a42; font-size: 15px; font-weight: 500; cursor: pointer; transition: all 0.2s; letter-spacing: 1px; }
.login-btn:hover { background: #f0d8c0; }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.sms-row { display: flex; gap: 8px; width: 100%; }
.flex-1 { flex: 1; }
.sms-btn { flex-shrink: 0; min-width: 100px; height: 38px; border: 1px solid #d1e7dd; border-radius: 8px; background: #fefaf5; color: #5a4a42; font-size: 12px; cursor: pointer; transition: all 0.2s; padding: 0 8px; white-space: nowrap; }
.sms-btn:hover:not(:disabled) { background: #f0e8dc; }
.sms-btn:disabled { color: #b5a898; cursor: not-allowed; }

.agree-row { display: flex; align-items: center; gap: 2px; margin-bottom: 8px; font-size: 12px; }
.agree-links a { color: #8ab8a0; text-decoration: none; }
.agree-links a:hover { color: #5a4a42; text-decoration: underline; }
.agree-label { display: flex; align-items: center; gap: 4px; cursor: pointer; }
.agree-check { width: 14px; height: 14px; cursor: pointer; accent-color: #8ab8a0; }
.agree-links a { color: #8ab8a0; text-decoration: none; }
.agree-links a:hover { color: #5a4a42; text-decoration: underline; }
.agree-sep { color: #d1e7dd; }

.switch-row { display: flex; justify-content: space-between; font-size: 12.5px; margin-top: 6px; }
.switch-row .link { color: #8ab8a0; cursor: pointer; text-decoration: none; border: none; background: none; padding: 0; font-size: 12.5px; }
.switch-row .link:hover { color: #5a4a42; text-decoration: underline; }

.register-row { text-align: center; font-size: 12.5px; color: #a09080; margin-top: 8px; padding-top: 8px; border-top: 1px solid #ece4d8; }
.register-row .link { color: #8ab8a0; text-decoration: none; font-weight: 500; }
.register-row .link:hover { color: #5a4a42; text-decoration: underline; }

/* 历史账号下拉 */
.history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  border: 1px solid #d1e7dd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  padding: 6px 0;
  margin-top: 2px;
}
.history-title { font-size: 12px; color: #a09080; padding: 4px 14px; }
.history-item { display: flex; align-items: center; gap: 8px; padding: 7px 14px; cursor: pointer; font-size: 14px; color: #5a4a42; }
.history-item:hover { background: #f8f2ea; }
.history-item i { color: #b5a898; font-size: 14px; }

/* ===== 公告区域（跟轮播等宽）===== */
.notice-area {
  position: relative;
  z-index: 1;
  padding: 28px 0 60px;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
}
.notice-inner {
  background: #fefaf5;
  border: 1px solid #d1e7dd;
  border-radius: 16px;
  padding: 18px 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
  max-width: 1200px;
  margin: 0 auto;
}
.notice-head { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.n-icon { font-size: 16px; }
.n-label { font-size: 14px; font-weight: 600; color: #5a4a42; flex: 1; }
.n-more { font-size: 12.5px; color: #8ab8a0; cursor: pointer; }
.n-more:hover { color: #5a4a42; }
.notice-grid { display: flex; flex-direction: column; gap: 0; }
.notice-card { padding: 8px 0; cursor: pointer; border-bottom: 1px solid #f5f0e8; }
.notice-card:last-child { border-bottom: none; }
.nc-row { display: flex; align-items: center; gap: 8px; }
.nc-bullet { width: 6px; height: 6px; border-radius: 50%; background: #d1e7dd; flex-shrink: 0; }
.nc-text { flex: 1; font-size: 14px; color: #5a4a42; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nc-time { font-size: 12px; color: #a09080; flex-shrink: 0; }

.notice-body { white-space: pre-wrap; font-size: 14px; color: #606266; line-height: 1.8; }

/* ===== 全局 El-Dialog 覆盖（登录页用）===== */
:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-dialog__header) { margin: 0; padding: 18px 20px 12px; }
:deep(.el-dialog__title) { font-size: 17px; }
</style>
