import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 响应式移动端检测 composable
 * - 宽度 < 768px（竖屏手机/平板）
 * - 高度 < 500px 且宽度 >= 568px（横屏手机，如 iPhone 12 横屏 844×390）
 *
 * @param {number} breakpoint - 断点宽度，默认 768px
 * @returns {{ isMobile: import('vue').Ref<boolean> }}
 */
export function useMobile(breakpoint = 768) {
  const isMobile = ref(false)

  let widthMql = null
  let landscapeMql = null

  function check() {
    const widthMatch = widthMql ? widthMql.matches : window.innerWidth < breakpoint
    const landscapeMatch = landscapeMql
      ? landscapeMql.matches
      : (window.innerHeight < 500 && window.innerWidth >= 568)
    isMobile.value = widthMatch || landscapeMatch
  }

  function onChange() {
    check()
  }

  onMounted(() => {
    if (window.matchMedia) {
      widthMql = window.matchMedia(`(max-width: ${breakpoint - 1}px)`)
      // 横屏手机：短（高度<500px）且宽（>=568px，最小横屏 iPhone 宽度）
      landscapeMql = window.matchMedia('(max-height: 499px) and (min-width: 568px)')
      widthMql.addEventListener('change', onChange)
      landscapeMql.addEventListener('change', onChange)
    } else {
      window.addEventListener('resize', onChange)
    }
    check()
  })

  onUnmounted(() => {
    if (widthMql) {
      widthMql.removeEventListener('change', onChange)
      landscapeMql.removeEventListener('change', onChange)
    } else {
      window.removeEventListener('resize', onChange)
    }
  })

  return { isMobile }
}
