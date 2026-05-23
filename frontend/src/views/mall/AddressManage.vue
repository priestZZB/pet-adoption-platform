<template>
  <div class="address-page">
    <h3 class="page-title">收货地址</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <!-- 新增按钮 -->
      <div class="toolbar">
        <el-button class="add-btn" @click="openDialog(null)">
          <el-icon><Plus /></el-icon> 新增地址
        </el-button>
      </div>

      <!-- 地址列表 -->
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="还没有收货地址">
          <el-button class="go-add-btn" @click="openDialog(null)">立即添加</el-button>
        </el-empty>
      </div>

      <div v-else class="address-list">
        <div v-for="item in list" :key="item.id" class="address-card">
          <div class="address-info">
            <div class="address-top">
              <span class="receiver-name">{{ item.receiverName }}</span>
              <span class="receiver-phone">{{ item.receiverPhone }}</span>
              <el-tag v-if="item.isDefault === 1" size="small" type="warning" class="default-tag">默认</el-tag>
            </div>
            <p class="address-detail">{{ item.receiverAddress }}</p>
            <p class="address-full">{{ item.province }} {{ item.city }} {{ item.district }}<span v-if="item.specificPlace"> · {{ item.specificPlace }}</span><span v-if="item.roomNo"> {{ item.roomNo }}</span></p>
          </div>
          <div class="address-actions">
            <el-button v-if="item.isDefault !== 1" class="action-btn" size="small" @click="handleSetDefault(item.id)">设为默认</el-button>
            <el-button class="action-btn" size="small" @click="openDialog(item)">编辑</el-button>
            <el-button class="action-btn danger" size="small" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑收货地址' : '新增收货地址'" width="540px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">

        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入收货人手机号" maxlength="11" />
        </el-form-item>

        <!-- 省市区（完全保持原样式）-->
        <el-form-item label="省市区" prop="province">
          <div class="region-selector">
            <el-select v-model="form.province" placeholder="省份" class="region-select" @change="onProvinceChange">
              <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
            </el-select>
            <el-select v-model="form.city" placeholder="城市" class="region-select" @change="onCityChange">
              <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
            <el-option v-if="cities.length === 0" label="请先选择省份" value="" disabled />
            </el-select>
            <el-select v-model="form.district" placeholder="区县" class="region-select">
              <el-option v-for="d in districts" :key="d" :label="d" :value="d" />
              <el-option v-if="districts.length === 0" label="请先选择城市" value="" disabled />
            </el-select>
          </div>
        </el-form-item>

        <el-form-item label=" ">
          <el-button class="map-pick-btn" @click="showMapPicker = true">
            在地图上选择
          </el-button>
        </el-form-item>

        <!-- 具体位置（必填）-->
        <el-form-item label="具体位置" prop="specificPlace">
          <el-input v-model="form.specificPlace" placeholder="小区、酒店、街道名称（必填）" maxlength="100" />
        </el-form-item>

        <!-- 门牌号（选填）-->
        <el-form-item label="门牌号">
          <el-input v-model="form.roomNo" placeholder="楼层/房号（可不填）" maxlength="50" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.isDefault" :true-value="1" :false-value="0">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="cancel-dialog-btn" @click="dialogVisible = false">取消</el-button>
        <el-button class="save-dialog-btn" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 🗺️ 地图选点 -->
    <MapPicker v-model="showMapPicker" :on-confirm="onMapPick" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Plus } from '@element-plus/icons-vue'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/mall'
import { regionData } from '@/utils/region'
import MapPicker from '@/components/MapPicker.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const showMapPicker = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const list = ref([])

const provinces = ref([])
const cities = ref([])
const districts = ref([])

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  specificPlace: '',
  roomNo: '',
  isDefault: 0
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  specificPlace: [{ required: true, message: '请输入具体位置', trigger: 'blur' }]
}

function onProvinceChange(val) {
  form.city = ''
  form.district = ''
  cities.value = regionData[val] ? Object.keys(regionData[val]) : []
  districts.value = []
}

function onCityChange(val) {
  form.district = ''
  if (form.province && regionData[form.province] && regionData[form.province][val]) {
    districts.value = regionData[form.province][val]
  } else {
    districts.value = []
  }
}

function resetForm() {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.specificPlace = ''
  form.roomNo = ''
  form.isDefault = 0
  cities.value = []
  districts.value = []
}

function openDialog(item) {
  resetForm()
  if (item) {
    isEdit.value = true
    editId.value = item.id
    form.receiverName = item.receiverName
    form.receiverPhone = item.receiverPhone
    form.province = item.province
    form.city = item.city
    form.district = item.district
    form.specificPlace = item.specificPlace || ''
    form.roomNo = item.roomNo || ''
    form.isDefault = item.isDefault

    // 加载级联选项
    if (form.province && regionData[form.province]) {
      cities.value = Object.keys(regionData[form.province])
      if (form.city && regionData[form.province][form.city]) {
        districts.value = regionData[form.province][form.city]
      }
    }
  } else {
    isEdit.value = false
    editId.value = null
    // 快速填入：用最近地址的姓名电话，没有则用用户信息
    if (list.value.length > 0) {
      const last = list.value[0]
      form.receiverName = last.receiverName || ''
      form.receiverPhone = last.receiverPhone || ''
    } else if (userStore.userInfo) {
      form.receiverName = userStore.userInfo.nickname || ''
      form.receiverPhone = userStore.userInfo.phone || ''
    }
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getAddressList()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.city) {
    ElMessage.warning('请选择城市')
    return
  }

  saving.value = true
  try {
    const submitData = {
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      province: form.province,
      city: form.city,
      district: form.district || '',
      specificPlace: form.specificPlace,
      roomNo: form.roomNo || '',
      isDefault: form.isDefault
    }
    if (isEdit.value) {
      await updateAddress(editId.value, submitData)
      ElMessage.success('地址已更新')
    } else {
      await addAddress(submitData)
      ElMessage.success('地址已添加')
    }
    dialogVisible.value = false
    await loadList()
  } catch {
    // 拦截器统一处理
  } finally {
    saving.value = false
  }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm(`确定删除地址「${item.receiverName} - ${item.receiverPhone}」？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddress(item.id)
    ElMessage.success('已删除')
    await loadList()
  } catch {
    // 取消或已处理
  }
}

async function handleSetDefault(id) {
  try {
    await setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    await loadList()
  } catch {
    // 拦截器处理
  }
}

// ========== 地图选点回调 ==========

function onMapPick(data) {
  // data: { province, city, district, specificPlace, address, lng, lat }
  console.log('🗺️ 地图选点回调:', data)

  // 直接设置省市区（强制匹配 region.js，不匹配也填上）
  if (data.province && provinces.value.includes(data.province)) {
    form.province = data.province
    cities.value = regionData[data.province] ? Object.keys(regionData[data.province]) : []
  } else if (data.province) {
    form.province = data.province
  }

  if (data.city) {
    if (cities.value.includes(data.city)) {
      form.city = data.city
      if (form.province && regionData[form.province] && regionData[form.province][data.city]) {
        districts.value = regionData[form.province][data.city]
      }
    } else {
      form.city = data.city
    }
  }

  if (data.district) {
    if (districts.value.includes(data.district)) {
      form.district = data.district
    } else {
      form.district = data.district
    }
  }

  form.specificPlace = data.specificPlace || data.district || ''
  ElMessage.success('📍 已选择位置')
}

onMounted(() => {
  provinces.value = Object.keys(regionData)
  loadList()
})
</script>

<style scoped>
.address-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  margin: 0 0 20px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}
.toolbar {
  margin-bottom: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  transition: box-shadow 0.2s;
}
.address-card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.address-info {
  flex: 1;
  min-width: 0;
}
.address-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.receiver-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.receiver-phone {
  font-size: 14px;
  color: #666;
}
.default-tag {
  margin-left: 4px;
}
.address-detail {
  font-size: 14px;
  color: #555;
  margin: 0 0 2px;
}
.address-full {
  font-size: 12px;
  color: #999;
  margin: 0;
}
.address-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}
.action-btn {
  border: 1px solid #d9d9d9;
  color: #555;
  border-radius: 8px;
}
.action-btn:hover {
  border-color: #c19a6b;
  color: #c19a6b;
}
.action-btn.danger:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

.region-selector {
  display: flex;
  gap: 8px;
}
.region-select {
  flex: 1;
}

/* ========== 地图选点按钮 ========== */
.map-pick-btn {
  border: 1px solid #c19a6b; color: #c19a6b; border-radius: 8px;
  background: #fff; font-size: 14px;
}
.map-pick-btn:hover { background: #fff5e8; }

/* 按钮暖色 */
.add-btn {
  background: #f8e8d8;
  border: 1px solid #d1e7dd;
  color: #5a4a3a;
  border-radius: 12px;
}
.add-btn:hover {
  background: #f0dcc8;
}
.go-add-btn {
  background: #f8e8d8;
  border: 1px solid #d1e7dd;
  color: #5a4a3a;
  border-radius: 12px;
}
.save-dialog-btn {
  background: #f8e8d8;
  border: 1px solid #d1e7dd;
  color: #5a4a3a;
  border-radius: 8px;
}
.save-dialog-btn:hover {
  background: #f0dcc8;
}
.cancel-dialog-btn {
  border: 1px solid #d9d9d9;
  border-radius: 8px;
}
</style>
