<template>
  <div class="address-page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>

    <MobileEmpty v-else-if="addresses.length === 0" icon="fas fa-map-marker-alt" description="暂无收货地址" action-label="新增地址" @action="openAdd" />

    <div v-else class="addr-list">
      <MobileCard v-for="a in addresses" :key="a.id" class="addr-item">
        <div class="addr-top">
          <span class="addr-contact">{{ a.contact }} {{ a.phone }}</span>
          <span v-if="a.isDefault" class="default-tag">默认</span>
        </div>
        <div class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
        <div class="addr-actions">
          <button @click="openEdit(a)">编辑</button>
          <button v-if="!a.isDefault" @click="setDefault(a.id)">设为默认</button>
          <button class="del" @click="handleDelete(a.id)">删除</button>
        </div>
      </MobileCard>
    </div>

    <div class="bottom-btn">
      <button class="add-btn" @click="openAdd">+ 新增地址</button>
    </div>

    <!-- 编辑/新增弹窗 -->
    <MobileDialog v-model="dialogVisible" :title="editId ? '编辑地址' : '新增地址'" @close="resetForm">
      <div class="form-group"><label>收货人</label><el-input v-model="form.contact" placeholder="姓名" /></div>
      <div class="form-group"><label>手机号</label><el-input v-model="form.phone" placeholder="手机号" maxlength="11" /></div>
      <div class="form-group"><label>省市区</label><el-input v-model="form.province" placeholder="省" style="margin-bottom:6px" /><el-input v-model="form.city" placeholder="市" style="margin-bottom:6px" /><el-input v-model="form.district" placeholder="区" /></div>
      <div class="form-group"><label>详细地址</label><el-input v-model="form.detail" placeholder="街道/门牌号" /></div>
      <template #footer>
        <button class="save-btn" @click="handleSave">保存</button>
      </template>
    </MobileDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'
import MobileDialog from '../../components/MobileDialog.vue'

const addresses = ref([]); const loading = ref(true)
const dialogVisible = ref(false); const editId = ref(null)
const form = reactive({ contact: '', phone: '', province: '', city: '', district: '', detail: '' })

async function loadAddresses() { loading.value = true; try { addresses.value = await getAddressList() } catch {} finally { loading.value = false } }

function openAdd() { editId.value = null; resetForm(); dialogVisible.value = true }
function openEdit(a) { editId.value = a.id; Object.assign(form, { contact: a.contact, phone: a.phone, province: a.province, city: a.city, district: a.district, detail: a.detail }); dialogVisible.value = true }
function resetForm() { Object.assign(form, { contact: '', phone: '', province: '', city: '', district: '', detail: '' }) }

async function handleSave() {
  try {
    if (editId.value) { await updateAddress(editId.value, form) } else { await addAddress(form) }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadAddresses()
  } catch {}
}

async function setDefault(id) { try { await setDefaultAddress(id); ElMessage.success('已设为默认'); loadAddresses() } catch {} }

async function handleDelete(id) {
  try { await ElMessageBox.confirm('确定删除？', '提示'); await deleteAddress(id); ElMessage.success('已删除'); loadAddresses() } catch {}
}
onMounted(loadAddresses)
</script>

<style scoped>
.address-page { padding: 12px 16px 80px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.addr-item { margin-bottom: 10px; }
.addr-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.addr-contact { font-size: 15px; font-weight: 500; color: #5a4a42; }
.default-tag { font-size: 11px; padding: 1px 8px; border-radius: 4px; background: rgba(139,184,160,0.12); color: #8ab8a0; }
.addr-detail { font-size: 13px; color: #a09080; line-height: 1.4; margin-bottom: 8px; }
.addr-actions { display: flex; gap: 8px; }
.addr-actions button { height: 28px; padding: 0 12px; border: 1px solid #d1e7dd; border-radius: 14px; background: #fefaf5; color: #5a4a42; font-size: 12px; cursor: pointer; }
.addr-actions button.del { color: #e8564a; border-color: #e8564a; }
.bottom-btn { padding: 12px 0; }
.add-btn { width: 100%; height: 44px; border: 1px dashed #d1e7dd; border-radius: 12px; background: none; color: #8ab8a0; font-size: 15px; cursor: pointer; }

.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 4px; }
.form-group :deep(.el-input__wrapper) { height: 42px; border-radius: 8px; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; }
</style>
