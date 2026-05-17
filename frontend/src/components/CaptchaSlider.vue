<template>
  <!-- 无UI渲染，纯逻辑组件，由父组件通过 ref 触发 -->
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 开启滑块验证码
const CAPTCHA_ENABLED = true

const CAPTCHA_APP_ID = '193347059'
const CAPTCHA_APP_SECRET = 'vzvaQXQjhqgQ7pDXb80NTadLU'
const SDK_URL = 'https://api-web.lianhdt.com/action-captcha/action-captcha.min.js'

const errorMsg = ref('')
let captchaInstance = null
let sdkLoaded = false

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
    script.onerror = () => { errorMsg.value = '验证码加载失败，请刷新页面'; resolve() }
    document.head.appendChild(script)
  })
}

onMounted(async () => { await loadSDK() })
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
 * 弹出滑块验证码，返回 Promise
 * @returns {Promise<{ticket: string, randstr: string, captchaSign: string}>}
 */
async function showCaptcha() {
  // 开发模式直接跳过，演示时改 CAPTCHA_ENABLED = true
  if (!CAPTCHA_ENABLED) {
    return { ticket: 'dev', randstr: 'dev', captchaSign: 'dev' }
  }

  return new Promise((resolve, reject) => {
    if (!sdkLoaded && typeof JumeiActionCaptcha === 'undefined') {
      reject(new Error('验证码SDK未加载，请稍后再试'))
      return
    }

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
