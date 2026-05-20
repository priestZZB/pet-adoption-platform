/**
 * 图片压缩工具
 * 上传前用 Canvas 压缩图片，减小文件体积
 */

const MAX_WIDTH = 1920
const MAX_HEIGHT = 1920
const QUALITY = 0.8

/**
 * 压缩图片文件
 * @param {File} file - 原始图片文件
 * @param {Object} [options] - 可选配置
 * @param {number} [options.maxWidth=1920] - 最大宽度
 * @param {number} [options.maxHeight=1920] - 最大高度
 * @param {number} [options.quality=0.8] - 压缩质量 0-1
 * @returns {Promise<File>} 压缩后的文件
 */
export function compressImage(file, options = {}) {
  const mw = options.maxWidth || MAX_WIDTH
  const mh = options.maxHeight || MAX_HEIGHT
  const q = options.quality || QUALITY

  return new Promise((resolve, reject) => {
    // 非图片或小于 100KB 不压缩
    if (!file.type.startsWith('image/') || file.size < 100 * 1024) {
      return resolve(file)
    }

    const img = new Image()
    img.onload = () => {
      let { width, height } = img
      if (width > mw || height > mh) {
        const ratio = Math.min(mw / width, mh / height)
        width = Math.round(width * ratio)
        height = Math.round(height * ratio)
      }

      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)

      canvas.toBlob((blob) => {
        if (!blob) return resolve(file)
        const compressed = new File([blob], file.name, {
          type: file.type,
          lastModified: Date.now()
        })
        resolve(compressed)
      }, file.type, q)
    }
    img.onerror = () => resolve(file)
    const url = URL.createObjectURL(file)
    img.src = url
  })
}
