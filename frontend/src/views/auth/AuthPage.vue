<template>
  <div class="auth-page">
    <div class="auth-bg"></div>

    <div class="auth-main">
      <!-- 品牌 -->
      <div class="brand-row">
        <img src="/images/logo.jpg" class="brand-logo" />
        <div>
          <div class="brand-name">有宠</div>
          <div class="brand-slogan">用领养代替购买 ❤️</div>
        </div>
      </div>

      <!-- ===== 主体区域：轮播+登录 左右并列 ===== -->
      <div class="main-body">
        <!-- 左：轮播 -->
        <div class="body-left">
          <el-carousel
            class="pet-carousel"
            height="400px"
            :interval="4500"
            indicator-position="outside"
            arrow="always"
            autoplay
          >
            <el-carousel-item v-for="(item, idx) in banners" :key="idx">
              <img :src="item.imageUrl" class="carousel-img" />
            </el-carousel-item>
          </el-carousel>
        </div>

        <!-- 右：登录 -->
        <div class="body-right">
          <!-- 子标签 -->
          <div class="sub-tabs">
            <button :class="['st', { active: loginTab === 'username' }]" @click="loginTab = 'username'">密码登录</button>
            <button :class="['st', { active: loginTab === 'phone' }]" @click="loginTab = 'phone'">手机号登录</button>
          </div>

          <el-form v-show="loginTab === 'username'" ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" size="large" @keyup.enter="handleLogin">
            <el-form-item prop="username" class="username-item">
              <div class="i-wrap">
                <i class="i-icon fas fa-user"></i>
                <el-input v-model="loginForm.username" placeholder="请输入用户名或手机号" @focus="showHistory" @blur="hideHistoryDelay" />
                <!-- 历史账号下拉 -->
                <div v-if="historyVisible && historyList.length > 0" class="history-dropdown">
                  <div class="history-title">最近登录</div>
                  <div
                    v-for="(h, i) in historyList"
                    :key="i"
                    class="history-item"
                    @mousedown.prevent="selectHistory(h)"
                  >
                    <i class="fas fa-user-circle"></i>
                    <span>{{ h }}</span>
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-form-item prop="password" class="password-item">
              <div class="i-wrap"><i class="i-icon fas fa-lock"></i><el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password /></div>
            </el-form-item>
            <el-form-item><button type="button" class="login-btn" :disabled="submitting" @click="handleLogin">{{ submitting ? '登录中…' : '登录' }}</button></el-form-item>
            <!-- 记住我 + 协议勾选 -->
            <div class="agree-row">
              <label class="agree-label">
                <input type="checkbox" v-model="rememberMe" class="agree-check" />
                <span class="agree-text">记住我</span>
              </label>
              <span class="agree-sep" style="margin:0 6px">|</span>
              <label class="agree-label">
                <input type="checkbox" v-model="agreed" class="agree-check" />
                <span class="agree-text">已阅读并同意</span>
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
          </el-form>

          <el-form v-show="loginTab === 'phone'" ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="0" size="large" @keyup.enter="handlePhoneLogin">
            <el-form-item prop="phone">
              <div class="i-wrap"><i class="i-icon fas fa-mobile-alt"></i><el-input v-model="phoneForm.phone" placeholder="请输入手机号" maxlength="11" /></div>
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="sms-row">
                <div class="i-wrap flex-1"><i class="i-icon fas fa-shield-alt"></i><el-input v-model="phoneForm.smsCode" placeholder="短信验证码" maxlength="6" /></div>
                <button type="button" class="sms-btn" :disabled="smsCountdown > 0 || !validPhone" :class="{ sending: smsSending }" @click="handleSendSms">{{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}</button>
              </div>
            </el-form-item>
            <el-form-item><button type="button" class="login-btn" :disabled="submitting" @click="handlePhoneLogin">{{ submitting ? '登录中…' : '登录' }}</button></el-form-item>
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
          </el-form>

          <div class="foot-links">
            <span>还没有账号？</span><router-link to="/register" class="link">去注册</router-link>
            <span class="sep">|</span>
            <router-link to="/reset-password" class="link">忘记密码？</router-link>
          </div>
        </div>
      </div>

      <!-- ===== 分隔 ===== -->
      <div class="divider"></div>

      <!-- ===== 公告区域（始终显示在下面）===== -->
      <div class="notice-area" v-if="notices.length > 0">
        <div class="notice-head">
          <span class="n-icon">📢</span>
          <span class="n-label">平台公告</span>
          <span class="n-more" @click="showAllNotices">查看全部 ›</span>
        </div>
        <div class="notice-grid">
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
  loadBanners(); loadNotices()
  // 从注册页带过来的用户名，自动填入
  const u = route.query.username
  if (u) { loginForm.username = u }
})

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

// 历史账号管理
const HISTORY_KEY = 'login_history'
function loadHistory() {
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    historyList.value = raw ? JSON.parse(raw) : []
  } catch { historyList.value = [] }
}
function saveHistory(username) {
  let list = [username]
  // 去重，新的放最前面
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
function selectHistory(username) {
  loginForm.username = username
  // 选择了已记住的用户名 → 自动勾选「记住我」
  rememberMe.value = true
  historyVisible.value = false
  // 自动聚焦到密码框
  setTimeout(() => {
    const pwInput = document.querySelector('.password-item .el-input__inner')
    if (pwInput) pwInput.focus()
  }, 100)
}

// 监听「记住我」取消 → 从历史中删除当前用户名
watch(rememberMe, (val) => {
  if (!val && loginForm.username) {
    const username = loginForm.username
    loadHistory()
    const filtered = historyList.value.filter(h => h !== username)
    if (filtered.length !== historyList.value.length) {
      historyList.value = filtered
      localStorage.setItem(HISTORY_KEY, JSON.stringify(filtered))
    }
  }
})
const phoneRules = { phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }], smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { len: 6, message: '6位数字', trigger: 'blur' }] }
const validPhone = computed(() => /^1[3-9]\d{9}$/.test(phoneForm.phone))
async function handleSendSms() { if (smsSending.value || smsCountdown.value > 0) return; if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }; try { const c = await captchaRef.value.showCaptcha(); smsSending.value = true; await sendSmsCode({ phone: phoneForm.phone, type: 'login', ...c }); ElMessage.success('验证码已发送'); smsCountdown.value = 60; smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000) } catch {} finally { smsSending.value = false } }
async function handleLogin() {
  const v = await loginFormRef.value.validate().catch(() => false); if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  try { const c = await captchaRef.value.showCaptcha(); submitting.value = true; const r = await login({ username: loginForm.username, password: loginForm.password, ...c }); userStore.setToken(r.token); await userStore.fetchUserInfo(); if (rememberMe.value) saveHistory(loginForm.username); ElMessage.success('登录成功'); router.push('/') } catch {} finally { submitting.value = false } }
async function handlePhoneLogin() {
  const v = await phoneFormRef.value.validate().catch(() => false); if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  submitting.value = true; try { const r = await phoneLogin({ phone: phoneForm.phone, smsCode: phoneForm.smsCode }); userStore.setToken(r.token); await userStore.fetchUserInfo(); ElMessage.success('登录成功'); router.push('/') } catch {} finally { submitting.value = false } }
onUnmounted(() => { if (smsTimer) clearInterval(smsTimer) })
</script>

<style scoped>
.auth-page { width: 100vw; min-height: 100vh; position: relative; background: #f7f1e5; }
.auth-bg { position: fixed; inset: 0; z-index: 0; background: linear-gradient(135deg, rgba(247,241,229,0.78) 0%, rgba(254,250,245,0.88) 100%), url('/images/bg.png') center/cover no-repeat; }

.auth-main { position: relative; z-index: 1; max-width: 1100px; margin: 0 auto; padding: 32px 28px 60px; }

/* 品牌 */
.brand-row { display: flex; align-items: center; gap: 14px; margin-bottom: 28px; }
.brand-logo { width: 50px; height: 50px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.brand-name { font-size: 26px; font-weight: 700; color: #5a4a42; line-height: 1.2; }
.brand-slogan { font-size: 13px; color: #a09080; margin-top: 1px; }

/* ===== 主体：左右并列 ===== */
.main-body {
  display: flex; gap: 28px;
  background: #fefaf5; border: 1px solid #d1e7dd; border-radius: 16px;
  padding: 24px; box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

/* 左：轮播 */
.body-left { flex: 1; min-width: 0; border-radius: 12px; overflow: hidden; border: 1px solid #e8ddd0; }
.pet-carousel { border-radius: 12px; overflow: hidden; }
.carousel-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.pet-carousel :deep(.el-carousel__indicator) { padding: 8px 5px; }
.pet-carousel :deep(.el-carousel__button) { width: 28px; height: 5px; border-radius: 3px; background: #d1e7dd; }
.pet-carousel :deep(.el-carousel__indicator.is-active .el-carousel__button) { background: #5a4a42; }
.pet-carousel :deep(.el-carousel__arrow) { background: rgba(254,250,245,0.85); color: #5a4a42; }
.pet-carousel :deep(.el-carousel__arrow:hover) { background: #fefaf5; }

/* 右：登录 */
.body-right { width: 380px; flex-shrink: 0; display: flex; flex-direction: column; justify-content: center; }

.sub-tabs { display: flex; border-bottom: 2px solid #d1e7dd; margin-bottom: 20px; }
.st { flex: 1; border: none; background: none; padding: 10px 0 8px; font-size: 14px; font-weight: 500; color: #a09080; cursor: pointer; position: relative; transition: color 0.2s; }
.st.active { color: #5a4a42; }
.st.active::after { content: ''; position: absolute; bottom: -2px; left: 20%; right: 20%; height: 2px; background: #5a4a42; border-radius: 2px; }
.st:hover { color: #5a4a42; }

.i-wrap { position: relative; width: 100%; }
.i-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #b5a898; font-size: 15px; z-index: 2; pointer-events: none; }
:deep(.el-input__wrapper) { padding-left: 42px !important; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; height: 48px; }
:deep(.el-input__wrapper:hover) { border-color: #b5d5c5; }
:deep(.el-input__wrapper.is-focus) { border-color: #8ab8a0; box-shadow: 0 0 0 3px rgba(139,184,160,0.12) !important; }
:deep(.el-form-item) { margin-bottom: 0; }
:deep(.el-form-item--large) { margin-bottom: 22px; }
:deep(.el-input__inner) { color: #5a4a42; font-size: 15px; }

.login-btn { width: 100%; height: 46px; border: none; border-radius: 10px; background: #f8e8d8; color: #5a4a42; font-size: 16px; font-weight: 500; cursor: pointer; transition: all 0.2s; letter-spacing: 1px; }
.login-btn:hover { background: #f0d8c0; }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.sms-row { display: flex; gap: 10px; width: 100%; }
.flex-1 { flex: 1; }
.sms-btn { flex-shrink: 0; min-width: 105px; height: 48px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 13px; cursor: pointer; transition: all 0.2s; padding: 0 10px; white-space: nowrap; }
.sms-btn:hover:not(:disabled) { background: #f0e8dc; }
.sms-btn:disabled { color: #b5a898; cursor: not-allowed; }

.agree-row { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; margin-bottom: 14px; font-size: 12.5px; }
.agree-label { display: flex; align-items: center; gap: 4px; cursor: pointer; }
.agree-check { width: 14px; height: 14px; cursor: pointer; accent-color: #8ab8a0; }

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
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  margin-top: 2px;
  overflow: hidden;
}
.history-title {
  padding: 8px 14px 4px;
  font-size: 11px;
  color: #b5a898;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  font-size: 14px;
  color: #5a4a42;
  cursor: pointer;
  transition: background 0.15s;
}
.history-item:hover {
  background: #f7f1e5;
}
.history-item i {
  color: #b5a898;
  font-size: 16px;
}
.agree-text { color: #a09080; }
.agree-links { display: flex; align-items: center; gap: 2px; flex-wrap: wrap; }
.agree-links a { color: #5a4a42; text-decoration: none; font-weight: 500; }
.agree-links a:hover { text-decoration: underline; }
.agree-sep { color: #d1e7dd; margin: 0 3px; }

.foot-links { text-align: center; font-size: 13.5px; color: #a09080; margin-top: 14px; }
.link { color: #5a4a42; font-weight: 500; text-decoration: none; }
.link:hover { text-decoration: underline; }
.sep { margin: 0 8px; color: #d1e7dd; }

/* ===== 分隔 ===== */
.divider { height: 28px; }

/* ===== 公告（始终在下面）===== */
.notice-area { background: rgba(254,250,245,0.75); backdrop-filter: blur(8px); border: 1px solid #d1e7dd; border-radius: 16px; padding: 20px 24px; }
.notice-head { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; }
.n-icon { font-size: 18px; }
.n-label { font-size: 15px; font-weight: 600; color: #5a4a42; }
.n-more { margin-left: auto; font-size: 13px; color: #a09080; cursor: pointer; transition: color 0.2s; }
.n-more:hover { color: #5a4a42; }
.notice-grid { display: flex; flex-direction: column; gap: 6px; }
.notice-card { display: flex; align-items: center; padding: 10px 14px; border-radius: 10px; cursor: pointer; transition: all 0.2s; }
.notice-card:hover { background: rgba(247,241,229,0.5); }
.nc-row { display: flex; align-items: center; gap: 10px; width: 100%; min-width: 0; }
.nc-bullet { width: 6px; height: 6px; border-radius: 50%; background: #d1e7dd; flex-shrink: 0; }
.notice-card:hover .nc-bullet { background: #8ab8a0; }
.nc-text { font-size: 14px; color: #5a4a42; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nc-time { font-size: 12px; color: #a09080; flex-shrink: 0; }

.notice-body { font-size: 14px; line-height: 1.8; color: #5a4a42; white-space: pre-wrap; }

@media (max-width: 820px) { .main-body { flex-direction: column; } .body-right { width: 100%; } }
</style>
