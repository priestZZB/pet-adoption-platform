<template>
  <div v-if="show" class="mp-overlay" @click.self="close">
    <div class="mp-card" @click.stop>
      <div class="mp-hd">
        <span class="mp-hd-title">选择收货地址</span>
        <button class="mp-hd-close" @click="close">✕</button>
      </div>

      <!-- 搜索 -->
      <div class="mp-search">
        <el-input
          v-model="kw"
          placeholder="搜索小区、酒店、地标"
          clearable
          :prefix-icon="Search"
          @keyup.enter="doSearch"
        >
          <template #append><el-button @click="doSearch">搜索</el-button></template>
        </el-input>
        <!-- 搜索结果列表 -->
        <div v-if="results.length > 0" class="mp-results">
          <div
            v-for="(r, i) in results"
            :key="i"
            class="mp-result-item"
            @click="jumpTo(r)"
          >
            <span class="mp-ri-icon">📍</span>
            <div class="mp-ri-text">
              <div class="mp-ri-name">{{ r.name }}</div>
              <div class="mp-ri-addr">{{ r.pname }}{{ r.cityname }}{{ r.adname }} {{ r.address }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 地图 -->
      <div class="mp-map-area">
        <div ref="mapBox" class="mp-map-box"></div>
        <div class="mp-hint" v-if="!info">点击地图选点</div>
        <button class="mp-gps" @click="locate" title="定位到我的位置">🎯</button>
      </div>

      <!-- 已选 -->
      <div class="mp-info" v-if="info">
        <div class="mp-info-icon">📍</div>
        <div class="mp-info-text">
          <div class="mp-info-addr">{{ info.address }}</div>
          <div class="mp-info-poi" v-if="info.poi">{{ info.poi }}</div>
        </div>
      </div>
      <div class="mp-info mp-info-empty" v-else>点击地图选择收货地址</div>

      <!-- 底 -->
      <div class="mp-ft">
        <button class="mp-btn" @click="close">取消</button>
        <button class="mp-btn mp-btn-primary" :disabled="!info" @click="ok">确认选择</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: Boolean,
  onConfirm: Function
})
const emit = defineEmits(['update:modelValue'])

const KEY = '021b30c1120021378e95f324a22f35da'
const SECRET = '1433a487d77903bfe8450d535a9d399d'

const show = ref(false)
const kw = ref('')
const info = ref(null)
const results = ref([])
const mapBox = ref(null)

let map = null
let marker = null
let ready = false
let geocoder = null
let centerCity = ''

// ========== 打开 ==========

watch(() => props.modelValue, (v) => {
  show.value = v
  if (v) {
    info.value = null
    kw.value = ''
    results.value = []
    setTimeout(start, 400)
  } else {
    cleanup()
  }
})

async function start() {
  if (!mapBox.value || mapBox.value.clientWidth < 10) {
    return setTimeout(start, 200)
  }
  ready = false

  window._AMapSecurityConfig = { securityJsCode: SECRET }

  if (window.AMap && window.__pluginsReady) {
    return createMap()
  }

  await loadScript()
  await loadPlugins()
  window.__pluginsReady = true
  createMap()
}

function loadScript() {
  return new Promise((resolve, reject) => {
    if (window.AMap) return resolve()
    const s = document.createElement('script')
    s.src = 'https://webapi.amap.com/maps?v=2.0&key=' + KEY
    s.async = true
    s.onload = resolve
    s.onerror = () => reject(Error('高德加载失败'))
    document.head.appendChild(s)
  })
}

function loadPlugins() {
  return new Promise((resolve) => {
    if (!window.AMap) return resolve()
    window.AMap.plugin([
      'AMap.Geocoder', 'AMap.PlaceSearch', 'AMap.Geolocation'
    ], () => {
      console.log('✅ 插件就绪')
      resolve()
    })
  })
}

// ========== 创建地图 ==========

function createMap() {
  const AMap = window.AMap
  map = new AMap.Map(mapBox.value, {
    zoom: 14,
    center: [116.397, 39.909],
    resizeEnable: true,
    layers: [new AMap.TileLayer()]
  })
  map.resize()

  // 逆地理（带扩展，才能返回最近POI）
  geocoder = new AMap.Geocoder({ city: '全国', radius: 500, extensions: 'all' })
  console.log('✅ Geocoder 创建成功')

  map.on('click', (e) => {
    const { lng, lat } = e.lnglat
    results.value = []
    pick(lng, lat)
  })

  // 打开时自动定位
  locate()

  ready = true
  console.log('✅ 地图就绪')
}

// ========== 选点 ==========

function pick(lng, lat, poiName) {
  if (!window.AMap || !geocoder) return

  if (marker) {
    marker.setPosition([lng, lat])
  } else {
    marker = new window.AMap.Marker({ position: [lng, lat], draggable: true })
    map.add(marker)
    marker.on('dragend', (e) => {
      const p = e.target.getPosition()
      pick(p.lng, p.lat)
    })
  }
  map.setCenter([lng, lat])

  // 如果传了 POI 名称（来自搜索），直接用
  if (poiName) {
    geocoder.getAddress([lng, lat], (s, r) => {
      if (s === 'complete' && r && r.regeocode) {
        const c = r.regeocode.addressComponent
        centerCity = c.city || c.province || ''
        const pv = c.province || ''
        const ct = c.city || pv || ''
        const dt = c.district || ''
        info.value = {
          province: pv, city: ct, district: dt,
          specificPlace: poiName,
          nearestPoi: poiName,
          address: r.regeocode.formattedAddress || '',
          lng, lat
        }
      }
    })
    return
  }

  // 获取当前城市（用于搜索限定）
  geocoder.getAddress([lng, lat], (s, r) => {
    if (s === 'complete' && r && r.regeocode) {
      const c = r.regeocode.addressComponent
      centerCity = c.city || c.province || ''
      const pv = c.province || ''
      const ct = c.city || pv || ''
      const dt = c.district || ''

      // POI
      let poi = ''
      if (r.regeocode.pois && r.regeocode.pois.length) {
        poi = r.regeocode.pois[0].name
      }

      // 无POI时从地址取最后一段
      let sp = poi || ''
      if (!sp) {
        const addr = (r.regeocode.formattedAddress || '')
          .replace(pv, '').replace(ct, '').replace(dt, '').replace('市辖区', '')
          .trim()
        const parts = addr.split(/[，,、]/)
        sp = parts[parts.length - 1]?.trim() || dt
      }

      info.value = {
        province: pv,
        city: ct,
        district: dt,
        specificPlace: sp,
        nearestPoi: poi,
        address: r.regeocode.formattedAddress || '',
        lng, lat
      }
    }
  })
}

// ========== 搜索 ==========

function doSearch() {
  const q = kw.value.trim()
  if (!q || q.length < 2) { ElMessage.warning('输入至少2个字'); return }
  if (!window.AMap) return

  // 限定城市搜索
  const opts = { pageSize: 8 }
  if (centerCity) opts.city = centerCity

  new window.AMap.PlaceSearch(opts).search(q, (s, r) => {
    if (s === 'complete' && r.poiList && r.poiList.pois.length) {
      results.value = r.poiList.pois
      ElMessage.success(`找到 ${r.poiList.pois.length} 个结果，请点击选择`)
    } else {
      results.value = []
      ElMessage.warning('未找到相关地点')
    }
  })
}

function jumpTo(poi) {
  results.value = []
  kw.value = poi.name
  map.setZoom(16)
  map.setCenter([poi.location.lng, poi.location.lat])
  pick(poi.location.lng, poi.location.lat, poi.name)
  ElMessage.success('已选择「' + poi.name + '」')
}

// ========== 定位（仅用户点击触发）=========

function locate() {
  if (!map) return
  if (!navigator.geolocation) { ElMessage.info('浏览器不支持定位'); return }

  navigator.geolocation.getCurrentPosition(
    (p) => {
      const { longitude: lng, latitude: lat } = p.coords
      map.setZoom(15)
      map.setCenter([lng, lat])
      pick(lng, lat)
    },
    () => {
      try {
        new window.AMap.Geolocation().getCurrentPosition((s, r) => {
          if (s === 'complete' && r && r.position) {
            map.setCenter([r.position.lng, r.position.lat])
            pick(r.position.lng, r.position.lat)
          }
        })
      } catch (e) {}
    },
    { enableHighAccuracy: true, timeout: 5000 }
  )
}

// ========== 确认/关闭 ==========

function ok() {
  if (!info.value || !props.onConfirm) return
  props.onConfirm({ ...info.value })
  close()
}

function close() { show.value = false; emit('update:modelValue', false) }

function cleanup() {
  if (map) { map.destroy(); map = null }
  marker = null; info.value = null; kw.value = ''; results.value = []
  ready = false; centerCity = ''
}
</script>

<style scoped>
.mp-overlay {
  position: fixed; inset: 0; z-index: 10000;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
}
.mp-card {
  width: 92%; max-width: 780px; max-height: 92vh;
  background: #fff; border-radius: 16px;
  display: flex; flex-direction: column;
  box-shadow: 0 8px 40px rgba(0,0,0,0.25);
}
.mp-hd { display: flex; justify-content: space-between; align-items: center; padding: 18px 20px 0; }
.mp-hd-title { font-size: 18px; font-weight: 600; color: #222; }
.mp-hd-close { background: none; border: none; font-size: 22px; color: #999; cursor: pointer; padding: 4px 8px; line-height: 1; }
.mp-hd-close:hover { color: #333; }

.mp-search { padding: 12px 20px 0; position: relative; z-index: 100; }
.mp-results {
  position: absolute; top: 100%; left: 20px; right: 20px; z-index: 100;
  background: #fff; border: 1px solid #e8e8e8; border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  max-height: 260px; overflow-y: auto;
}
.mp-result-item {
  display: flex; gap: 10px; padding: 10px 14px; cursor: pointer; align-items: flex-start;
  border-bottom: 1px solid #f5f5f5;
}
.mp-result-item:hover { background: #f8f4ef; }
.mp-result-item:last-child { border: none; }
.mp-ri-icon { font-size: 16px; flex-shrink: 0; margin-top: 2px; }
.mp-ri-text { flex: 1; min-width: 0; }
.mp-ri-name { font-size: 14px; font-weight: 500; color: #333; }
.mp-ri-addr { font-size: 12px; color: #999; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.mp-map-area {
  position: relative; margin: 12px 20px 0;
  height: 380px; border-radius: 12px; overflow: hidden; border: 1px solid #e8e8e8;
}
.mp-map-box { width: 100%; height: 100%; }
.mp-hint {
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0,0,0,0.55); color: #fff;
  padding: 8px 20px; border-radius: 20px;
  font-size: 14px; pointer-events: none; white-space: nowrap; z-index: 10;
}
.mp-gps {
  position: absolute; right: 12px; bottom: 50px; z-index: 10;
  width: 42px; height: 42px; border-radius: 50%; border: 1px solid #ddd;
  background: #fff; font-size: 20px; cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  display: flex; align-items: center; justify-content: center;
}
.mp-gps:hover { background: #f5f5f5; }

.mp-info {
  margin: 12px 20px 0; padding: 12px 14px;
  display: flex; gap: 10px;
  background: #f8f5f0; border-radius: 10px; border: 1px solid #eee;
}
.mp-info-empty { color: #bbb; display: block; text-align: center; padding: 16px; font-size: 14px; background: #f9f9f9; }
.mp-info-icon { font-size: 20px; flex-shrink: 0; margin-top: 1px; }
.mp-info-text { flex: 1; min-width: 0; }
.mp-info-addr { font-size: 14px; color: #333; font-weight: 500; }
.mp-info-poi { margin-top: 4px; font-size: 13px; color: #c19a6b; }

.mp-ft { padding: 14px 20px 18px; display: flex; justify-content: flex-end; gap: 10px; }
.mp-btn { padding: 8px 28px; border-radius: 8px; border: 1px solid #d9d9d9; background: #fff; font-size: 14px; cursor: pointer; color: #555; }
.mp-btn-primary { background: #c19a6b; border-color: #c19a6b; color: #fff; }
.mp-btn-primary:hover { background: #b0895a; }
.mp-btn-primary:disabled { background: #d9d9d9; border-color: #d9d9d9; cursor: not-allowed; }
</style>
