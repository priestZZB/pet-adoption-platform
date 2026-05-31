<template>
  <!-- 无UI渲染，纯逻辑组件，由父组件通过 ref 触发 -->
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAppConfig } from '@/composables/useAppConfig'

const { loadConfig } = useAppConfig()

const CAPTCHA_APP_ID = '193347059'
const CAPTCHA_APP_SECRET = 'vzvaQXQjhqgQ7pDXb80NTadLU'
const SDK_URL = 'https://api-web.lianhdt.com/action-captcha/action-captcha.min.js'

const errorMsg = ref('')
let captchaInstance = null
let sdkLoaded = false
let configReady = false
let useMock = true

function loadSDK() {
  return new Promise((resolve) => {
    if (typeof JumeiActionCaptcha !== 'undefined') {
      sdkLoaded = true
      resolve()
      return
    }
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = SDK_URL
    script.onload = () => { sdkLoaded = true; resolve() }
    script.onerror = () => {
      console.warn('[Captcha] SDK加载失败，使用本地mock')
      resolve()
    }
    document.head.appendChild(script)
  })
}

onMounted(async () => {
  const config = await loadConfig()
  useMock = config?.captchaMock ?? true
  configReady = true
  if (!useMock) {
    await loadSDK()
  }
})
onUnmounted(() => { captchaInstance = null })

/** 计算 captchaSign：sha256(captchaAppId + captchaAppSecret + ticket + randstr) */
async function computeSign(ticket, randstr) {
  const str = CAPTCHA_APP_ID + CAPTCHA_APP_SECRET + ticket + randstr
  const encoder = new TextEncoder()
  const data = encoder.encode(str)
  const hashBuffer = await crypto.subtle.digest('SHA-256', data)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

/**
 * 弹出滑块验证码
 * 由后端 captcha.mock 配置决定：
 *   mock=true  → 跳过SDK加载，直接返回模拟数据
 *   mock=false → 加载怜花SDK，弹窗验证
 * SDK加载失败时自动降级为mock（离线/开发环境兜底）
 */
async function showCaptcha() {
  // 等待配置加载完成
  if (!configReady) {
    const config = await loadConfig()
    useMock = config?.captchaMock ?? true
    configReady = true
  }

  if (useMock) {
    return { ticket: 'mock', randstr: 'mock', captchaSign: 'mock' }
  }

  // SDK未就绪时降级
  if (!sdkLoaded && typeof JumeiActionCaptcha === 'undefined') {
    console.warn('[Captcha] SDK不可用，使用本地mock')
    return { ticket: 'mock', randstr: 'mock', captchaSign: 'mock' }
  }

  return new Promise((resolve, reject) => {
    captchaInstance = new JumeiActionCaptcha(CAPTCHA_APP_ID, async (captchaRes) => {
      if (captchaRes.ret === 0) {
        const { ticket, randstr } = captchaRes
        const captchaSign = await computeSign(ticket, randstr)
        resolve({ ticket, randstr, captchaSign })
      } else if (captchaRes.ret === 2) {
        reject(new Error('VERIFY_CANCELLED'))
      } else {
        reject(new Error('滑块验证失败，请重试'))
      }
    }, {})

    captchaInstance.show()
  })
}

defineExpose({ showCaptcha })
</script>
