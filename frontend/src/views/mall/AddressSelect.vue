<template>
  <div class="address-select-page">
    <h3 class="page-title">收货地址</h3>

    <!-- 提示条 -->
    <div v-if="isSelectMode" class="mode-hint">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>请选择一个收货地址用于结算，也可以直接新增</template>
      </el-alert>
    </div>

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
        <el-empty :description="isSelectMode ? '还没有收货地址，请先添加' : '还没有收货地址'">
          <el-button class="go-add-btn" @click="openDialog(null)">立即添加</el-button>
        </el-empty>
      </div>

      <div v-else class="address-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="address-card"
          :class="{ selected: isSelectMode && selectedId === item.id }"
        >
          <div class="address-main" @click="isSelectMode && selectAddress(item)">
            <template v-if="isSelectMode">
              <el-radio v-model="selectedId" :value="item.id" class="radio-btn" @click.stop="selectAddress(item)">
                <div class="address-info">
                  <div class="address-top">
                    <span class="receiver-name">{{ item.receiverName }}</span>
                    <span class="receiver-phone">{{ item.receiverPhone }}</span>
                    <el-tag v-if="item.isDefault === 1" size="small" type="warning" class="default-tag">默认</el-tag>
                  </div>
                  <p class="address-detail">{{ item.receiverAddress }}</p>
              </div>
              </el-radio>
            </template>
            <template v-else>
              <div class="address-info address-info-block">
                <div class="address-top">
                  <span class="receiver-name">{{ item.receiverName }}</span>
                  <span class="receiver-phone">{{ item.receiverPhone }}</span>
                  <el-tag v-if="item.isDefault === 1" size="small" type="warning" class="default-tag">默认</el-tag>
                </div>
                <p class="address-detail">{{ item.receiverAddress }}</p>
                <p class="address-full">省: {{ item.province }} / 市: {{ item.city }} / 区: {{ item.district }} / {{ item.detailAddress }}</p>
              </div>
            </template>
          </div>
          <div class="address-actions">
            <el-button v-if="item.isDefault !== 1" class="action-btn" size="small" @click="handleSetDefault(item.id)">设为默认</el-button>
            <el-button class="action-btn" size="small" @click="openDialog(item)">编辑</el-button>
            <el-button class="action-btn danger" size="small" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div v-if="isSelectMode" class="bottom-bar">
        <el-button class="back-btn" @click="$router.back()">返回</el-button>
        <el-button class="confirm-btn" :disabled="!selectedId" @click="confirmAddress">确认使用该地址</el-button>
      </div>
    </template>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑收货地址' : '新增收货地址'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入收货人手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="省市区" prop="province">
          <el-cascader
            v-model="regionValue"
            :options="cascaderOptions"
            placeholder="请选择省/市/区"
            class="region-cascader"
            clearable
            @change="onRegionChange"
          />
        </el-form-item>
        <el-form-item label="门牌号" prop="detailAddress">
          <el-input v-model="form.detailAddress" placeholder="街道、门牌号、楼层等详细信息" maxlength="100" />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Plus } from '@element-plus/icons-vue'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/mall'
import { regionData } from '@/utils/region'

const route = useRoute()
const router = useRouter()

// 是否选择模式（从结算页过来的？）
const isSelectMode = ref(route.query.mode === 'select')

const loading = ref(true)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const list = ref([])
const selectedId = ref(null)

const regionValue = ref([])

// 级联选择器选项
const cascaderOptions = computed(() => {
  return Object.entries(regionData).map(([province, cities]) => ({
    value: province,
    label: province,
    children: Object.entries(cities).map(([city, districts]) => ({
      value: city,
      label: city,
      children: districts.map(d => ({ value: d, label: d }))
    }))
  }))
})

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请选择省市区', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细门牌号', trigger: 'blur' }]
}

function onRegionChange(val) {
  if (val && val.length >= 2) {
    form.province = val[0]
    form.city = val[1]
    form.district = val.length >= 3 ? val[2] : ''
  } else {
    form.province = ''
    form.city = ''
    form.district = ''
  }
}

function resetForm() {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.isDefault = 0
  regionValue.value = []
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
    form.detailAddress = item.detailAddress
    form.isDefault = item.isDefault

    // 设置级联选择器回显
    const vals = [item.province]
    if (item.city) vals.push(item.city)
    if (item.district) vals.push(item.district)
    regionValue.value = vals
  } else {
    isEdit.value = false
    editId.value = null
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getAddressList()
    // 选择模式：默认选中默认地址
    if (isSelectMode.value && list.value.length > 0) {
      const defaultAddr = list.value.find(a => a.isDefault === 1)
      selectedId.value = defaultAddr ? defaultAddr.id : list.value[0].id
    }
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
  if (!form.district) {
    form.district = ''
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateAddress(editId.value, { ...form })
      ElMessage.success('地址已更新')
    } else {
      await addAddress({ ...form })
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

function selectAddress(item) {
  selectedId.value = item.id
}

function confirmAddress() {
  const addr = list.value.find(a => a.id === selectedId.value)
  if (!addr) return

  router.replace({
    path: '/mall/checkout',
    query: {
      addressId: addr.id,
      receiverName: addr.receiverName,
      receiverPhone: addr.receiverPhone,
      receiverAddress: addr.receiverAddress
    }
  })
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.address-select-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  margin: 0 0 16px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.mode-hint {
  margin-bottom: 16px;
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
  padding: 16px 20px;
  background: #fff;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  transition: all 0.2s;
}
.address-card:hover {
  border-color: #c19a6b;
}
.address-card.selected {
  border-color: #c19a6b;
  background: #fffbf5;
}
.address-main {
  cursor: default;
}
.address-card.selected .address-main {
  cursor: pointer;
}

.radio-btn {
  display: flex;
  align-items: flex-start;
  width: 100%;
}
.radio-btn :deep(.el-radio__label) {
  flex: 1;
  padding-left: 8px;
}

.address-info {
  flex: 1;
}
.address-info-block {
  padding: 4px 0;
}
.address-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
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
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.action-btn {
  border: 1px solid #d9d9d9;
  color: #555;
  border-radius: 8px;
  font-size: 12px;
}
.action-btn:hover {
  border-color: #c19a6b;
  color: #c19a6b;
}
.action-btn.danger:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

.region-cascader {
  width: 100%;
}

.bottom-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding: 16px 0;
}
.confirm-btn {
  background: #c19a6b;
  border: 1px solid #c19a6b;
  color: #fff;
  border-radius: 12px;
  padding: 10px 32px;
  font-size: 15px;
}
.confirm-btn:hover {
  background: #b0895a;
  border-color: #b0895a;
}
.confirm-btn:disabled {
  background: #d9d9d9;
  border-color: #d9d9d9;
}
.back-btn {
  border: 1px solid #d9d9d9;
  border-radius: 12px;
}

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
