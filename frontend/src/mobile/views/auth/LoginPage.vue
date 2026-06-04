<template>
  <div class="login-page">
    <!-- 品牌条 -->
    <div class="brand-bar">
      <div class="brand-row">
        <img src="/images/logo.jpg" class="brand-logo" />
        <div>
          <div class="brand-name">有宠</div>
          <div class="brand-slogan">用领养代替购买</div>
        </div>
      </div>
    </div>

    <!-- 轮播 -->
    <div class="carousel-wrap">
      <el-carousel height="100%" :interval="4500" indicator-position="none" arrow="never" autoplay>
        <el-carousel-item v-for="(item, idx) in banners" :key="idx">
          <el-image :src="item.imageUrl" fit="cover" style="width:100%;height:100%" />
        </el-carousel-item>
      </el-carousel>
      <div class="carousel-overlay"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="card-wrap">
      <div class="login-card">
        <!-- Tab 切换 -->
        <div class="sub-tabs">
          <button :class="['st', { active: loginTab === 'username' }]" @click="loginTab = 'username'">密码登录</button>
          <button :class="['st', { active: loginTab === 'phone' }]" @click="loginTab = 'phone'">短信登录</button>
        </div>

        <!-- 密码登录表单 -->
        <el-form v-show="loginTab === 'username'" ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <div class="i-wrap">
              <i class="i-icon fas fa-user"></i>
              <el-input v-model="loginForm.username" placeholder="用户名或手机号" @focus="showHistory" @blur="hideHistoryDelay" />
              <div v-if="historyVisible && historyList.length > 0" class="history-dropdown">
                <div class="history-title">最近登录</div>
                <div v-for="(h, i) in historyList" :key="i" class="history-item">
                  <span class="history-name" @mousedown.prevent="selectHistory(h)">
                    <i class="fas fa-user-circle"></i><span>{{ h }}</span>
                  </span>
                  <span class="history-x" @mousedown.prevent="removeHistory(i)">&#10005;</span>
                </div>
              </div>
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <div class="i-wrap">
              <i class="i-icon fas fa-lock"></i>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password />
            </div>
          </el-form-item>
          <el-form-item>
            <button type="button" class="login-btn" :disabled="submitting" @click="handleLogin">{{ submitting ? '登录中...' : '登录' }}</button>
          </el-form-item>
          <div class="agree-row">
            <label class="agree-label">
              <input type="checkbox" v-model="rememberMe" class="agree-check" />
              <span>记住我</span>
            </label>
          </div>
          <div class="agree-row agree-row--legal">
            <label class="agree-label">
              <input type="checkbox" v-model="agreed" class="agree-check" />
              <span>已阅读并同意</span>
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
          <div class="switch-row">
            <span class="link" @click="loginTab = 'phone'">手机号登录</span>
            <router-link to="/reset-password" class="link">忘记密码</router-link>
          </div>
          <div class="register-row">
            还没有账号？<router-link to="/register" class="link">立即注册</router-link>
          </div>
        </el-form>

        <!-- 短信登录表单 -->
        <el-form v-show="loginTab === 'phone'" ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="0" size="large" @keyup.enter="handlePhoneLogin">
          <el-form-item prop="phone">
            <div class="i-wrap">
              <i class="i-icon fas fa-phone"></i>
              <el-input v-model="phoneForm.phone" placeholder="手机号" maxlength="11" />
            </div>
          </el-form-item>
          <el-form-item prop="smsCode">
            <div class="sms-row">
              <div class="flex-1 i-wrap">
                <i class="i-icon fas fa-shield-alt"></i>
                <el-input v-model="phoneForm.smsCode" placeholder="6位验证码" maxlength="6" />
              </div>
              <button type="button" class="sms-btn" :disabled="smsSending || smsCountdown > 0" @click="handleSendSms">
                {{ smsCountdown > 0 ? smsCountdown + 's' : (smsSending ? '发送中' : '获取验证码') }}
              </button>
            </div>
          </el-form-item>
          <el-form-item>
            <button type="button" class="login-btn" :disabled="submitting" @click="handlePhoneLogin">{{ submitting ? '登录中...' : '登录' }}</button>
          </el-form-item>
          <div class="agree-row">
            <label class="agree-label">
              <input type="checkbox" v-model="rememberMe" class="agree-check" />
              <span>记住我</span>
            </label>
          </div>
          <div class="agree-row agree-row--legal">
            <label class="agree-label">
              <input type="checkbox" v-model="agreed" class="agree-check" />
              <span>已阅读并同意</span>
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
          <div class="switch-row">
            <span class="link" @click="loginTab = 'username'">密码登录</span>
            <router-link to="/reset-password" class="link">忘记密码</router-link>
          </div>
          <div class="register-row">
            还没有账号？<router-link to="/register" class="link">立即注册</router-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 公告区域 -->
    <div class="notice-area" v-if="notices.length > 0">
      <div class="notice-inner">
        <div class="notice-head">
          <span>📢</span>
          <span class="n-label">平台公告</span>
          <span class="n-more" @click="showAllNotices">查看全部</span>
        </div>
        <div class="notice-card" v-for="(item, idx) in notices.slice(0, 4)" :key="idx" @click="showNoticeDetail(item)">
          <span class="nc-bullet"></span>
          <span class="nc-text">{{ item.title }}</span>
          <span class="nc-time">{{ formatDate(item.createdAt) }}</span>
        </div>
      </div>
    </div>

    <el-dialog v-model="noticeVisible" :title="noticeDetail?.title" width="92%" top="6vh">
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
const banners = ref([{ imageUrl: WELCOME_IMAGE }])
const notices = ref([])
const noticeVisible = ref(false); const noticeDetail = ref(null)

async function loadBanners() { try { const r = await request.get('/banners'); if (r && r.length > 0) { banners.value = [{ imageUrl: WELCOME_IMAGE }, ...r.map(b => ({ imageUrl: b.imageUrl }))] } } catch {} }
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

watch(() => route.query.reason, (nv) => { if (nv) showLoginReason() })
function showLoginReason() {
  const reason = route.query.reason
  if (reason) { ElMessage.warning(decodeURIComponent(reason)) }
}

const loginFormRef = ref(null); const phoneFormRef = ref(null)
const loginTab = ref('username'); const submitting = ref(false); const smsSending = ref(false); const smsCountdown = ref(0)
const rememberMe = ref(false)
const historyVisible = ref(false); const historyList = ref([])
let historyHideTimer = null; let smsTimer = null
const loginForm = reactive({ username: '', password: '' })
const phoneForm = reactive({ phone: '', smsCode: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const HISTORY_KEY = 'login_history_usernames'
function loadHistory() { try { const raw = localStorage.getItem(HISTORY_KEY); historyList.value = raw ? JSON.parse(raw) : [] } catch { historyList.value = [] } }
function saveHistory(username) {
  loadHistory(); let list = [username]
  for (const h of historyList.value) { if (h !== username) list.push(h) }
  if (list.length > 5) list = list.slice(0, 5)
  historyList.value = list; localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
}
function showHistory() { if (historyHideTimer) clearTimeout(historyHideTimer); loadHistory(); historyVisible.value = true }
function hideHistoryDelay() { historyHideTimer = setTimeout(() => { historyVisible.value = false }, 200) }
function removeHistory(index) { historyList.value.splice(index, 1); localStorage.setItem(HISTORY_KEY, JSON.stringify(historyList.value)) }
function selectHistory(value) {
  if (loginTab.value === 'username') { loginForm.username = value; loginForm.password = '' }
  else { phoneForm.phone = value }
  rememberMe.value = true; historyVisible.value = false
  setTimeout(() => {
    const sel = loginTab.value === 'username' ? '.password-item .el-input__inner' : '.sms-row .el-input__inner'
    const next = document.querySelector(sel); if (next) next.focus()
  }, 100)
}
watch(rememberMe, (val) => {
  if (!val) {
    const current = loginTab.value === 'username' ? loginForm.username : phoneForm.phone
    if (current) { loadHistory(); const filtered = historyList.value.filter(h => h !== current); if (filtered.length !== historyList.value.length) { historyList.value = filtered; localStorage.setItem(HISTORY_KEY, JSON.stringify(filtered)) } }
  }
})

const phoneRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { len: 6, message: '6位数字', trigger: 'blur' }]
}
const validPhone = computed(() => /^1[3-9]\d{9}$/.test(phoneForm.phone))

async function handleSendSms() {
  if (smsSending.value || smsCountdown.value > 0) return
  if (!validPhone.value) { ElMessage.warning('请输入正确手机号'); return }
  try { const c = await captchaRef.value.showCaptcha(); smsSending.value = true; await sendSmsCode({ phone: phoneForm.phone, type: 'login', ...c }); ElMessage.success('验证码已发送'); smsCountdown.value = 60; smsTimer = setInterval(() => { smsCountdown.value--; if (smsCountdown.value <= 0) { clearInterval(smsTimer); smsTimer = null } }, 1000) } catch {} finally { smsSending.value = false }
}

async function handleLogin() {
  const v = await loginFormRef.value.validate().catch(() => false); if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  try { const c = await captchaRef.value.showCaptcha(); submitting.value = true; const r = await login({ username: loginForm.username, password: loginForm.password, ...c }); userStore.setToken(r.token); await userStore.fetchUserInfo(); if (rememberMe.value) saveHistory(loginForm.username); ElMessage.success('登录成功'); router.push('/') } catch {} finally { submitting.value = false }
}

async function handlePhoneLogin() {
  const v = await phoneFormRef.value.validate().catch(() => false); if (!v) return
  if (!agreed.value) { ElMessage.warning('请先阅读并同意相关协议'); return }
  submitting.value = true; try { const r = await phoneLogin({ phone: phoneForm.phone, smsCode: phoneForm.smsCode }); userStore.setToken(r.token); await userStore.fetchUserInfo(); if (rememberMe.value) saveHistory(phoneForm.phone); ElMessage.success('登录成功'); router.push('/') } catch {} finally { submitting.value = false }
}

onUnmounted(() => { if (smsTimer) clearInterval(smsTimer) })
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: #f7f1e5;
  padding-bottom: 40px;
}

/* === 品牌条 === */
.brand-bar {
  width: 100%;
  background: linear-gradient(135deg, #c19a6b 0%, #b0895a 100%);
  padding: 14px 0;
}
.brand-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
}
.brand-logo { width: 36px; height: 36px; border-radius: 10px; }
.brand-name { font-size: 20px; font-weight: 700; color: #fff; line-height: 1.2; }
.brand-slogan { font-size: 13px; color: rgba(255,255,255,0.85); }

/* === 轮播 === */
.carousel-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: #e8ddd0;
}
.carousel-wrap :deep(.el-carousel) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__container) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__item) { height: 100% !important; }
.carousel-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.25) 0%, rgba(0,0,0,0.0) 100%);
  pointer-events: none;
}

/* === 登录卡片 === */
.card-wrap {
  padding: 20px 16px;
}
.login-card {
  width: 100%;
  padding: 24px 20px 20px;
  border-radius: 16px;
  background: rgba(254, 250, 245, 0.97);
  border: 1px solid rgba(210, 195, 175, 0.4);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04), 0 8px 24px rgba(0,0,0,0.06);
}

/* === Tab === */
.sub-tabs {
  display: flex;
  border-bottom: 2px solid #d1e7dd;
  margin-bottom: 20px;
}
.st {
  flex: 1;
  border: none;
  background: none;
  padding: 12px 0 10px;
  font-size: 15px;
  font-weight: 500;
  color: #a09080;
  cursor: pointer;
  position: relative;
  text-align: center;
}
.st.active { color: #5a4a42; }
.st.active::after { content: ''; position: absolute; bottom: -2px; left: 25%; right: 25%; height: 2px; background: #5a4a42; border-radius: 2px; }

/* === 输入框 === */
.i-wrap { position: relative; width: 100%; }
.i-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #b5a898; font-size: 16px; z-index: 2; pointer-events: none; }
.login-card :deep(.el-input__wrapper) { padding-left: 42px !important; height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
.login-card :deep(.el-input__wrapper.is-focus) { border-color: #8ab8a0; box-shadow: 0 0 0 2px rgba(139,184,160,0.15) !important; }
.login-card :deep(.el-input__inner) { font-size: 15px; color: #5a4a42; }
.login-card :deep(.el-form-item) { padding-bottom: 16px; margin-bottom: 0; }
.login-card :deep(.el-form-item__error) { font-size: 12px; bottom: 2px; }

/* === 登录按钮 === */
.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #c19a6b 0%, #b0895a 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(177,137,90,0.3);
}
.login-btn:active { transform: scale(0.98); }
.login-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; box-shadow: none; }

/* === 短信按钮 === */
.sms-row { display: flex; gap: 10px; width: 100%; }
.flex-1 { flex: 1; }
.sms-btn {
  flex-shrink: 0;
  min-width: 100px; height: 46px;
  border: 1px solid #d1e7dd; border-radius: 10px;
  background: #fefaf5; color: #5a4a42; font-size: 13px;
  cursor: pointer; padding: 0 12px; white-space: nowrap;
}
.sms-btn:active { background: #f0e8dc; }
.sms-btn:disabled { color: #b5a898; cursor: not-allowed; }

/* === 协议行 === */
.agree-row {
  display: flex; align-items: center; gap: 6px;
  margin-bottom: 8px; font-size: 13px; color: #5a4a42;
}
.agree-row--legal { flex-wrap: wrap; align-items: flex-start; }
.agree-label { display: flex; align-items: center; gap: 4px; cursor: pointer; }
.agree-check { width: 16px; height: 16px; accent-color: #8ab8a0; cursor: pointer; flex-shrink: 0; }
.legal-links { width: 100%; padding-left: 20px; font-size: 12.5px; line-height: 1.8; display: flex; flex-wrap: wrap; align-items: center; gap: 2px; }
.legal-links a { color: #8ab8a0; text-decoration: none; padding: 2px 0; }
.sep { color: #c8dcd0; margin: 0 2px; }

/* === 切换 / 注册行 === */
.switch-row { display: flex; justify-content: space-between; font-size: 14px; margin-top: 12px; }
.switch-row .link { color: #8ab8a0; text-decoration: none; }
.register-row { text-align: center; font-size: 14px; color: #a09080; margin-top: 14px; padding-top: 14px; border-top: 1px solid #ece4d8; }
.register-row .link { color: #8ab8a0; text-decoration: none; font-weight: 500; }

/* === 历史下拉 === */
.history-dropdown { position: absolute; top: 100%; left: 0; right: 0; z-index: 100; background: #fff; border: 1px solid #d1e7dd; border-radius: 10px; box-shadow: 0 4px 16px rgba(0,0,0,0.08); padding: 6px 0; margin-top: 4px; }
.history-title { font-size: 12px; color: #a09080; padding: 4px 14px; }
.history-item { display: flex; align-items: center; justify-content: space-between; padding: 3px 14px; }
.history-name { display: flex; align-items: center; gap: 8px; flex: 1; padding: 4px 0; cursor: pointer; border-radius: 4px; color: #5a4a42; font-size: 14px; }
.history-name:hover { background: #f8f2ea; }
.history-x { display: flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; cursor: pointer; font-size: 12px; color: #c0b8a8; flex-shrink: 0; }
.history-x:hover { color: #fff; background: #e8564a; }

/* === 公告 === */
.notice-area { padding: 0 16px; }
.notice-inner { background: rgba(254,250,245,0.97); border: 1px solid rgba(210,195,175,0.3); border-radius: 14px; padding: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.03); }
.notice-head { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; font-size: 14px; }
.n-label { font-weight: 600; color: #5a4a42; flex: 1; }
.n-more { font-size: 13px; color: #8ab8a0; cursor: pointer; }
.notice-card { display: flex; align-items: center; gap: 8px; padding: 10px 0; cursor: pointer; border-bottom: 1px solid #f5f0e8; }
.notice-card:last-child { border-bottom: none; }
.nc-bullet { width: 6px; height: 6px; border-radius: 50%; background: #d1e7dd; flex-shrink: 0; }
.nc-text { flex: 1; font-size: 13.5px; color: #5a4a42; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nc-time { font-size: 11px; color: #a09080; flex-shrink: 0; }
.notice-body { white-space: pre-wrap; font-size: 14px; color: #606266; line-height: 1.8; padding: 8px 0; }
</style>
