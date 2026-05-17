<template>
  <div class="realname-page">
    <h3 class="page-title">实名认证</h3>

    <el-card v-if="isVerified" class="result-card">
      <div class="result-content">
        <el-icon :size="48" color="#67C23A"><CircleCheck /></el-icon>
        <h3>已通过实名认证</h3>
        <p class="result-info">
          姓名：{{ userStore.userInfo?.realName }}<br>
          身份证：{{ maskIdCard(userStore.userInfo?.idCard) }}
        </p>
      </div>
    </el-card>

    <template v-else>
      <el-card>
        <!-- 填信息 -->
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
          </el-form-item>
          <el-form-item>
            <el-button
              class="action-btn"
              :loading="loading"
              :disabled="loading"
              @click="startAuth"
            >开始认证</el-button>
          </el-form-item>
        </el-form>

        <!-- 认证状态 -->
        <div v-if="authStatus === 'checking'" class="status-line">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在验证身份信息…</span>
        </div>
        <div v-if="authStatus === 'liveness'" class="status-line">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在活体检测，请查看新打开的窗口…</span>
        </div>
        <div v-if="authStatus === 'comparing'" class="status-line">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在比对身份信息…</span>
        </div>
        <div v-if="authStatus === 'done'" class="status-line" style="color:#67C23A">
          <el-icon :size="20" color="#67C23A"><CircleCheck /></el-icon>
          <span>实名认证成功 ✅</span>
        </div>
        <div v-if="authStatus === 'failed'" class="status-line" style="color:#F56C6C">
          <el-icon :size="20" color="#F56C6C"><WarningFilled /></el-icon>
          <span>{{ authDesc }}</span>
          <el-button class="retry-btn" size="small" @click="resetAuth">重新认证</el-button>
        </div>
      </el-card>
    </template>

    <CaptchaSlider ref="captchaRef" />
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Loading, WarningFilled } from '@element-plus/icons-vue'
import { checkIdCard, realNameAuthDirect, livenessToken, livenessResult } from '@/api/user'
import { useUserStore } from '@/stores/user'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const userStore = useUserStore()
const formRef = ref(null)
const captchaRef = ref(null)
const loading = ref(false)
const authStatus = ref('')       // '' | checking | liveness | comparing | done | failed
const authDesc = ref('')
let livenessOrderNo = ''
let livenessFaceUrl = ''
let pollTimer = null
let pollCount = 0
const MAX_POLL = 40

const form = reactive({ realName: '', idCard: '' })
const isVerified = computed(() => userStore.userInfo?.isRealName === 1)

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

function maskIdCard(id) {
  if (!id || id.length < 10) return id || '—'
  return id.substring(0, 4) + '**********' + id.substring(14)
}

// ============================================================
//  开始认证（检查 → 滑块 → 活体 → 比对）
// ============================================================
async function startAuth() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  authStatus.value = 'checking'

  try {
    // ① 先检查身份证是否已被绑定（避免浪费API次数）
    await checkIdCard(form.idCard)

    // ② 滑块验证
    await captchaRef.value.showCaptcha()

    // ③ 活体检测
    authStatus.value = 'liveness'
    const data = await livenessToken({ returnUrl: window.location.origin + '/close.html' })
    livenessOrderNo = data.orderNo || ''

    if (data.url) {
      window.open(data.url, '_blank')
      startPolling()
    } else {
      // 没有URL（模拟模式）→ 直接走mock结果
      livenessOrderNo = data.orderNo
      authStatus.value = 'liveness'
      startPolling()
    }
  } catch (err) {
    authStatus.value = ''
    // 滑块取消或身份证已绑定：错误由拦截器提示
  } finally {
    loading.value = false
  }
}

/** 轮询活体结果 */
function startPolling() {
  pollCount = 0
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(async () => {
    pollCount++
    if (pollCount > MAX_POLL || !livenessOrderNo) {
      clearInterval(pollTimer); pollTimer = null
      authStatus.value = 'failed'
      authDesc.value = '活体检测超时，请重新认证'
      return
    }
    try {
      const data = await livenessResult({ orderNo: livenessOrderNo })
      const result = data.result

      if (result === 0) {
        clearInterval(pollTimer); pollTimer = null
        livenessFaceUrl = data.faceImageUrl || ''
        if (livenessFaceUrl) {
          await doFaceCompare()
        }
      } else if (result === 1) {
        clearInterval(pollTimer); pollTimer = null
        authStatus.value = 'failed'
        authDesc.value = data.desc || '活体检测未通过'
      }
    } catch { /* 继续轮询 */ }
  }, 3000)
}

/** 公安比对 */
async function doFaceCompare() {
  authStatus.value = 'comparing'
  try {
    await realNameAuthDirect({
      realName: form.realName,
      idCard: form.idCard,
      imageUrl: livenessFaceUrl
    })
    authStatus.value = 'done'
    await userStore.fetchUserInfo()
  } catch (err) {
    authStatus.value = 'failed'
    authDesc.value = err?.message || '身份比对未通过'
  }
}

function resetAuth() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
  authStatus.value = ''
  authDesc.value = ''
  livenessOrderNo = ''
  livenessFaceUrl = ''
}

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.realname-page { max-width: 560px; margin: 0 auto; padding: 24px 0 40px; }
.page-title { font-size: 20px; color: var(--yc-text-primary); margin: 0 0 20px; }
:deep(.el-card) { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-card); background: var(--yc-bg-card); }
.result-card { text-align: center; }
.result-content { padding: 20px 0; }
.result-content h3 { margin: 12px 0 8px; font-size: 18px; color: var(--yc-text-primary); }
.result-info { font-size: 14px; color: var(--yc-text-secondary); line-height: 1.8; }

.status-line { display: flex; align-items: center; gap: 8px; padding: 12px 0 0; font-size: 14px; color: var(--yc-text-secondary); }
.retry-btn { margin-left: auto; }

.action-btn {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  width: 100%;
}
.action-btn:hover { background: var(--yc-btn-hover); border-color: var(--yc-border-hover); }
</style>
