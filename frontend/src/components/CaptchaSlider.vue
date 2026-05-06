<template>
  <div class="captcha-wrapper">
    <el-button
      type="warning"
      :loading="loading"
      @click="openCaptcha"
      style="width:100%"
    >
      {{ loading ? '验证中...' : '点击进行滑块验证' }}
    </el-button>
    <p v-if="errorMsg" class="captcha-error">{{ errorMsg }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const CAPTCHA_APP_ID = '193347059'
const CAPTCHA_APP_SECRET = 'vzvaQXQjhqgQ7pDXb80NTadLU'
const SDK_URL = 'https://api-web.lianhdt.com/action-captcha/action-captcha.min.js'

const emit = defineEmits(['verified'])
const loading = ref(false)
const errorMsg = ref('')
let captchaInstance = null
let sdkLoaded = false

// 动态加载怜花SDK
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
    script.onload = () => {
      sdkLoaded = true
      resolve()
    }
    script.onerror = () => {
      errorMsg.value = '验证码加载失败，请刷新页面'
      resolve()
    }
    document.head.appendChild(script)
  })
}

onMounted(async () => {
  await loadSDK()
})

onUnmounted(() => {
  captchaInstance = null
})

function openCaptcha() {
  loading.value = true
  errorMsg.value = ''

  if (!sdkLoaded && typeof JumeiActionCaptcha === 'undefined') {
    errorMsg.value = '验证码SDK未加载，请稍后再试'
    loading.value = false
    return
  }

  captchaInstance = new JumeiActionCaptcha(CAPTCHA_APP_ID, captchaCallback, {})
  captchaInstance.show()
}

function captchaCallback(captchaRes) {
  loading.value = false

  if (captchaRes.ret === 0) {
    // 验证成功
    const { ticket, randstr } = captchaRes
    const captchaSign = MD5(ticket + randstr + CAPTCHA_APP_SECRET)
    emit('verified', { ticket, randstr, captchaSign })
    errorMsg.value = ''
  } else if (captchaRes.ret === 2) {
    // 用户主动关闭
    errorMsg.value = ''
  } else {
    errorMsg.value = '验证失败，请重试'
  }
}

// 轻量MD5（公域实现，无外部依赖）
function MD5(str) {
  function md5cycle(x, k) {
    var a = x[0], b = x[1], c = x[2], d = x[3]
    a = ff(a, b, c, d, k[0], 7, -680876936)
    d = ff(d, a, b, c, k[1], 12, -389564586)
    c = ff(c, d, a, b, k[2], 17, 606105819)
    b = ff(b, c, d, a, k[3], 22, -1044525330)
    a = ff(a, b, c, d, k[4], 7, -176418897)
    d = ff(d, a, b, c, k[5], 12, 1200080426)
    c = ff(c, d, a, b, k[6], 17, -1473231341)
    b = ff(b, c, d, a, k[7], 22, -45705983)
    a = ff(a, b, c, d, k[8], 7, 1770035416)
    d = ff(d, a, b, c, k[9], 12, -1958414417)
    c = ff(c, d, a, b, k[10], 17, -42063)
    b = ff(b, c, d, a, k[11], 22, -1990404162)
    a = ff(a, b, c, d, k[12], 7, 1804603682)
    d = ff(d, a, b, c, k[13], 12, -40341101)
    c = ff(c, d, a, b, k[14], 17, -1502002290)
    b = ff(b, c, d, a, k[15], 22, 1236535329)
    a = gg(a, b, c, d, k[1], 5, -165796510)
    d = gg(d, a, b, c, k[6], 9, -1069501632)
    c = gg(c, d, a, b, k[11], 14, 643717713)
    b = gg(b, c, d, a, k[0], 20, -373897302)
    a = gg(a, b, c, d, k[5], 5, -701558691)
    d = gg(d, a, b, c, k[10], 9, 38016083)
    c = gg(c, d, a, b, k[15], 14, -660478335)
    b = gg(b, c, d, a, k[4], 20, -405537848)
    a = gg(a, b, c, d, k[9], 5, 568446438)
    d = gg(d, a, b, c, k[14], 9, -1019803690)
    c = gg(c, d, a, b, k[3], 14, -187363961)
    b = gg(b, c, d, a, k[8], 20, 1163531501)
    a = gg(a, b, c, d, k[13], 5, -1444681467)
    d = gg(d, a, b, c, k[2], 9, -51403784)
    c = gg(c, d, a, b, k[7], 14, 1735328473)
    b = gg(b, c, d, a, k[12], 20, -1926607734)
    a = hh(a, b, c, d, k[5], 4, -378558)
    d = hh(d, a, b, c, k[8], 11, -2022574463)
    c = hh(c, d, a, b, k[11], 16, 1839030562)
    b = hh(b, c, d, a, k[14], 23, -35309556)
    a = hh(a, b, c, d, k[1], 4, -1530992060)
    d = hh(d, a, b, c, k[4], 11, 1272893353)
    c = hh(c, d, a, b, k[7], 16, -155497632)
    b = hh(b, c, d, a, k[10], 23, -1094730640)
    a = hh(a, b, c, d, k[13], 4, 681279174)
    d = hh(d, a, b, c, k[0], 11, -358537222)
    c = hh(c, d, a, b, k[3], 16, -722521979)
    b = hh(b, c, d, a, k[6], 23, 76029189)
    a = hh(a, b, c, d, k[9], 4, -640364487)
    d = hh(d, a, b, c, k[12], 11, -421815835)
    c = hh(c, d, a, b, k[15], 16, 530742520)
    b = hh(b, c, d, a, k[2], 23, -995338651)
    a = ii(a, b, c, d, k[0], 6, -198630844)
    d = ii(d, a, b, c, k[7], 10, 1126891415)
    c = ii(c, d, a, b, k[14], 15, -1416354905)
    b = ii(b, c, d, a, k[5], 21, -57434055)
    a = ii(a, b, c, d, k[12], 6, 1700485571)
    d = ii(d, a, b, c, k[3], 10, -1894986606)
    c = ii(c, d, a, b, k[10], 15, -1051523)
    b = ii(b, c, d, a, k[1], 21, -2054922799)
    a = ii(a, b, c, d, k[8], 6, 1873313359)
    d = ii(d, a, b, c, k[15], 10, -30611744)
    c = ii(c, d, a, b, k[6], 15, -1560198380)
    b = ii(b, c, d, a, k[13], 21, 1309151649)
    a = ii(a, b, c, d, k[4], 6, -145523070)
    d = ii(d, a, b, c, k[11], 10, -1120210379)
    c = ii(c, d, a, b, k[2], 15, 718787259)
    b = ii(b, c, d, a, k[9], 21, -343485551)
    x[0] = add32(a, x[0]); x[1] = add32(b, x[1]); x[2] = add32(c, x[2]); x[3] = add32(d, x[3])
  }
  function cmn(q, a, b, x, s, t) { return add32(bit32(rot(a, s) + q + x + t), b) }
  function ff(a, b, c, d, x, s, t) { return cmn(bit32(b & c | ~b & d), a, b, x, s, t) }
  function gg(a, b, c, d, x, s, t) { return cmn(bit32(b & d | c & ~d), a, b, x, s, t) }
  function hh(a, b, c, d, x, s, t) { return cmn(bit32(b ^ c ^ d), a, b, x, s, t) }
  function ii(a, b, c, d, x, s, t) { return cmn(bit32(c ^ (b | ~d)), a, b, x, s, t) }
  function rot(n, s) { return n << s | n >>> (32 - s) }
  function bit32(v) { return v & 0xFFFFFFFF }
  function add32(a, b) { return bit32(a + b) }
  function hex(x) { return ('00000000' + (x >>> 0).toString(16)).slice(-8) }
  var utf8 = unescape(encodeURIComponent(str))
  var block = [], i
  for (i = 0; i < utf8.length; i++) { block[i >> 2] |= utf8.charCodeAt(i) << ((i % 4) * 8) }
  block[i >> 2] |= 0x80 << ((i % 4) * 8)
  block[(((i + 8) >> 6) << 4) + 14] = utf8.length * 8
  var a = [1732584193, -271733879, -1732584194, 271733878]
  for (i = 0; i < block.length; i += 16) md5cycle(a, block.slice(i, i + 16))
  return hex(a[0]) + hex(a[1]) + hex(a[2]) + hex(a[3])
}
</script>

<style scoped>
.captcha-wrapper {
  margin: 10px 0;
}
.captcha-error {
  color: #F56C6C;
  font-size: 12px;
  margin-top: 6px;
}
</style>
