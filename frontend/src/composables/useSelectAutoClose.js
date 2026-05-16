/**
 * useSelectAutoClose — el-select 鼠标移开即自动关闭下拉
 *
 * 用法（单下拉）：
 *   import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
 *   const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()
 *   // template:
 *   <el-select
 *     :ref="(el) => setSelectRef('_self', el)"
 *     popper-class="auto-close-popper"
 *     @visible-change="(v) => onSelectVisible(v, '_self')"
 *   >
 *
 * 用法（表格 v-for）：
 *   <el-select
 *     :ref="(el) => setSelectRef(row.id, el)"
 *     popper-class="auto-close-popper"
 *     @visible-change="(v) => onSelectVisible(v, row.id)"
 *   >
 */
export function useSelectAutoClose() {
  const selectRefMap = {}
  let activeId = null
  let closeTimer = null

  /** 保存 ref（key 可以是 '_self'、row.id 等） */
  function setSelectRef(key, el) {
    if (el) selectRefMap[key] = el
  }

  /** 关闭指定 key 的下拉 */
  function closeSelect(key) {
    const inst = selectRefMap[key]
    if (inst && typeof inst.blur === 'function') {
      inst.blur()
    }
  }

  function cancelTimer() {
    if (closeTimer) {
      clearTimeout(closeTimer)
      closeTimer = null
    }
  }

  function startTimer(key) {
    cancelTimer()
    closeTimer = setTimeout(() => closeSelect(key), 200)
  }

  /** 全局 mouseover：鼠标在我们范围内则取消关闭倒计时 */
  function onGlobalMouseOver(e) {
    if (!activeId) return
    const isOnOurArea =
      e.target.closest('.auto-close-popper') ||
      e.target.closest('.el-select')
    if (isOnOurArea) {
      cancelTimer()
    } else {
      startTimer(activeId)
    }
  }

  /**
   * 挂到 @visible-change 上
   * @param {boolean} visible
   * @param {string|number} key — 对应 setSelectRef 时的 key
   */
  function onSelectVisible(visible, key) {
    if (visible) {
      activeId = key
      cancelTimer()
      document.addEventListener('mouseover', onGlobalMouseOver)
    } else {
      document.removeEventListener('mouseover', onGlobalMouseOver)
      cancelTimer()
      if (activeId === key) activeId = null
    }
  }

  /** 在组件的 onUnmounted 中调用 */
  function cleanupSelectAutoClose() {
    document.removeEventListener('mouseover', onGlobalMouseOver)
    cancelTimer()
    // 清掉引用方便 GC
    Object.keys(selectRefMap).forEach(k => delete selectRefMap[k])
  }

  return {
    setSelectRef,
    onSelectVisible,
    cleanupSelectAutoClose,
  }
}
