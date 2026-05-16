<template>
  <div class="realname-page">
    <h3 class="page-title">实名认证</h3>

    <!-- 已认证 -->
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

    <!-- 未认证，显示表单 -->
    <el-card v-else>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="人脸拍照">
          <div class="camera-area">
            <!-- 未拍照：显示摄像头预览 -->
            <template v-if="!capturedImage">
              <div class="camera-view">
                <video ref="videoRef" autoplay playsinline class="video-preview" />
                <div v-if="!cameraReady" class="camera-loading">
                  <el-icon class="is-loading" :size="32"><Loading /></el-icon>
                  <p>正在打开摄像头…</p>
                </div>
              </div>
              <el-button
                class="capture-btn"
                :disabled="!cameraReady"
                :icon="Camera"
                @click="handleCapture"
              >
                拍照
              </el-button>
            </template>

            <!-- 已拍照：显示照片预览 -->
            <template v-else>
              <el-image :src="capturedImage" fit="cover" class="captured-preview" />
              <div class="camera-actions">
                <el-button @click="handleRetake">重新拍摄</el-button>
              </div>
            </template>

            <p class="face-tip">请确保面部清晰、光线充足，仅支持PC浏览器拍照</p>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            class="auth-submit-btn"
            :loading="submitting"
            :disabled="!capturedImage"
            @click="handleSubmit"
          >
            提交认证
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 隐藏的 canvas 用于截帧 -->
    <canvas ref="canvasRef" style="display:none" />

    <!-- 隐藏的滑块验证码组件 -->
    <CaptchaSlider ref="captchaRef" />
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Camera, Loading } from '@element-plus/icons-vue'
import { realNameAuth } from '@/api/user'
import { useUserStore } from '@/stores/user'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const userStore = useUserStore()
const formRef = ref(null)
const captchaRef = ref(null)
const videoRef = ref(null)
const canvasRef = ref(null)
const submitting = ref(false)
const cameraReady = ref(false)
const capturedImage = ref('')
let mediaStream = null

const form = reactive({
  realName: '',
  idCard: '',
  phone: ''
})

const isVerified = computed(() => userStore.userInfo?.isRealName === 1)

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function maskIdCard(id) {
  if (!id || id.length < 10) return id || '—'
  return id.substring(0, 4) + '**********' + id.substring(14)
}

// 打开摄像头
async function startCamera() {
  try {
    cameraReady.value = false
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: 'user'
      },
      audio: false
    })
    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
      await videoRef.value.play()
    }
    cameraReady.value = true
  } catch (err) {
    ElMessage.error('无法打开摄像头，请确保已授予摄像头权限')
    console.error('摄像头启动失败:', err)
  }
}

// 关闭摄像头
function stopCamera() {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
    mediaStream = null
  }
  cameraReady.value = false
}

// 拍照：从视频流截帧到 canvas → base64
function handleCapture() {
  const video = videoRef.value
  const canvas = canvasRef.value
  if (!video || !canvas) return

  const ctx = canvas.getContext('2d')
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

  // 转 base64（JPEG 格式，0.8 质量，去掉 data:image/...;base64, 前缀）
  const fullBase64 = canvas.toDataURL('image/jpeg', 0.8)
  capturedImage.value = fullBase64.replace(/^data:image\/[a-z]+;base64,/, '')

  // 关闭摄像头（省资源）
  stopCamera()
}

// 重新拍摄
function handleRetake() {
  capturedImage.value = ''
  startCamera()
}

// 提交实名认证
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const captchaData = await captchaRef.value.showCaptcha()

    submitting.value = true
    await realNameAuth({
      realName: form.realName,
      idCard: form.idCard,
      phone: form.phone,
      image: capturedImage.value,   // base64 字符串
      ...captchaData
    })
    ElMessage.success('实名认证成功')
    await userStore.fetchUserInfo()
  } catch (err) {
    // 用户取消验证时不提示，其他错误由拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  startCamera()
})

onUnmounted(() => {
  stopCamera()
})
</script>

<style scoped>
.realname-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}

:deep(.el-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

.result-card {
  text-align: center;
}
.result-content {
  padding: 20px 0;
}
.result-content h3 {
  margin: 12px 0 8px;
  font-size: 18px;
  color: var(--yc-text-primary);
}
.result-info {
  font-size: 14px;
  color: var(--yc-text-secondary);
  line-height: 1.8;
}

.camera-area {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}
.camera-view {
  position: relative;
  width: 320px;
  height: 240px;
  background: var(--yc-bg-page);
  border-radius: var(--yc-radius-input);
  overflow: hidden;
  border: 1px solid var(--yc-border);
}
.video-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.camera-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--yc-text-tertiary);
  font-size: 14px;
  gap: 8px;
}
.captured-preview {
  width: 320px;
  height: 240px;
  border-radius: var(--yc-radius-input);
  object-fit: cover;
  border: 1px solid var(--yc-border);
}
.camera-actions {
  display: flex;
  gap: 8px;
}
.face-tip {
  margin: 0;
  font-size: 12px;
  color: var(--yc-text-tertiary);
}

:deep(.capture-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
}
:deep(.capture-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
}
:deep(.auth-submit-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.auth-submit-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
}
</style>
