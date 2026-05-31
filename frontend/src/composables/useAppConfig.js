/**
 * 应用全局配置（从后端获取，无需手动维护环境变量）
 *
 * 用法：
 *   import { useAppConfig } from '@/composables/useAppConfig'
 *   const { config, loadConfig, isCaptchaMock } = useAppConfig()
 *   await loadConfig()
 *   if (isCaptchaMock.value) { ... }
 */
import { ref } from 'vue'

const config = ref(null)
const loading = ref(false)
let loaded = false

export function useAppConfig() {
  /**
   * 从后端加载配置（幂等，多次调用只请求一次）
   */
  async function loadConfig() {
    if (loaded || loading.value) return config.value
    loading.value = true
    try {
      const res = await fetch('/api/public/config')
      const json = await res.json()
      config.value = json.data || {}
      loaded = true
    } catch {
      // 后端不可达时降级使用环境变量
      console.warn('[AppConfig] 配置加载失败，使用环境变量降级')
      config.value = {
        captchaMock: import.meta.env.VITE_CAPTCHA_MOCK === 'true',
        smsMock: false,
        realnameMock: false,
        aiMock: false,
        mapMock: false
      }
    } finally {
      loading.value = false
    }
    return config.value
  }

  return {
    config,
    loadConfig,
    /** 滑块验证码mock开关 */
    isCaptchaMock: () => config.value?.captchaMock ?? false,
    /** SMS验证码mock开关 */
    isSmsMock: () => config.value?.smsMock ?? false,
    /** 实名认证mock开关 */
    isRealnameMock: () => config.value?.realnameMock ?? false,
    /** AI mock开关 */
    isAiMock: () => config.value?.aiMock ?? false,
    /** 地图服务mock开关 */
    isMapMock: () => config.value?.mapMock ?? false
  }
}
