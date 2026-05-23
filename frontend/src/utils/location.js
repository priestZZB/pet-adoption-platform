/**
 * 定位工具 — 浏览器定位 + 高德逆地理编码 + POI搜索
 *
 * 使用场景：
 * 1. 点击「自动定位」→ getBrowserLocation() → reverseGeocode() → 自动填地址表单
 * 2. 搜索POI → placeSuggest() → 展示下拉 → 选中后填表单
 */
import request from '@/api/request'

/**
 * 获取浏览器定位坐标
 * 使用 HTML5 Geolocation API
 *
 * @param {Object} options - 定位选项
 * @param {number} options.timeout - 超时时间（毫秒），默认 10000
 * @param {boolean} options.enableHighAccuracy - 是否开启高精度，默认 true
 * @returns {Promise<{lat: number, lng: number}>}
 */
export function getBrowserLocation(options = {}) {
  const { timeout = 10000, enableHighAccuracy = true } = options

  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('您的浏览器不支持定位功能，请使用现代浏览器或手动填写地址'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          lat: position.coords.latitude,
          lng: position.coords.longitude
        })
      },
      (error) => {
        let message = '定位失败'
        switch (error.code) {
          case error.PERMISSION_DENIED:
            message = '定位权限被拒绝，请在浏览器设置中允许位置访问'
            break
          case error.POSITION_UNAVAILABLE:
            message = '无法获取位置信息，请检查网络或GPS设置'
            break
          case error.TIMEOUT:
            message = '定位超时，请稍后重试'
            break
          default:
            message = '定位失败，请手动填写地址'
        }
        reject(new Error(message))
      },
      {
        enableHighAccuracy,
        timeout,
        maximumAge: 0
      }
    )
  })
}

/**
 * 逆地理编码：坐标 → 地址信息
 * 调后端代理的高德API
 *
 * @param {number} lat - 纬度
 * @param {number} lng - 经度
 * @returns {Promise<{province, city, district, nearestPoi, poiAddress, formattedAddress}>}
 */
export function reverseGeocode(lat, lng) {
  return request.post('/map/reverse-geocode', { lat, lng })
    .then(res => res.data || res)
}

/**
 * POI搜索建议：关键词 → 地点列表
 * 调后端代理的高德API
 *
 * @param {string} keywords - 搜索关键词（如"深圳湾万象城"）
 * @param {string} [city] - 限定城市（可选）
 * @returns {Promise<Array<{name, province, city, district, address, display}>>}
 */
export function placeSuggest(keywords, city) {
  const params = { keywords }
  if (city) params.city = city
  return request.get('/map/place-suggest', { params })
    .then(res => res.data || res)
}
