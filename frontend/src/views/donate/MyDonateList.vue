<template>
  <div class="donate-list-page">
    <div class="page-header">
      <h3 class="page-title">我发布的宠物</h3>
      <el-button class="publish-new-btn" @click="$router.push('/donate/publish')">发布新宠物</el-button>
    </div>

    <!-- 状态筛选 -->
    <el-tabs v-model="statusFilter" @tab-change="loadList">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待审核" name="PENDING" />
      <el-tab-pane label="待领养" name="APPROVED" />
      <el-tab-pane label="已打回" name="REJECTED" />
      <el-tab-pane label="已下架" name="OFFLINE" />
      <el-tab-pane label="已被领养" name="ADOPTED" />
    </el-tabs>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="filteredList.length === 0" class="empty-tip">
        <el-empty :description="statusFilter ? '暂无该状态的宠物' : '还没有发布过宠物'">
          <el-button v-if="!statusFilter" class="publish-new-btn" @click="$router.push('/donate/publish')">去发布</el-button>
        </el-empty>
      </div>

      <div v-else class="pet-list">
        <el-card v-for="pet in filteredList" :key="pet.id" class="pet-card">
          <div class="pet-content">
            <el-image
              :src="pet.coverImage"
              fit="cover"
              style="width:100px;height:100px;border-radius:8px;flex-shrink:0"
            >
              <template #error><div class="img-placeholder" /></template>
            </el-image>

            <div class="pet-info">
              <div class="pet-header">
                <h4>{{ pet.name }}</h4>
                <el-tag :type="PET_STATUS[pet.status]?.type || 'info'" size="small">
                  {{ PET_STATUS[pet.status]?.label || pet.status }}
                </el-tag>
              </div>
              <p class="pet-meta">
                {{ pet.categoryName }} · {{ pet.age }} · {{ GENDER_MAP[pet.gender] || pet.gender }}
              </p>
            </div>

            <div class="pet-actions">
              <el-button size="small" @click="$router.push('/donate/detail/' + pet.id)">查看详情</el-button>
              <el-button size="small" @click="openEdit(pet.id)">编辑</el-button>
              <el-button size="small" @click="$router.push('/donate/pets/' + pet.id + '/applications')">查看申请</el-button>

              <el-button v-if="pet.status === 'ADOPTED'" size="small" disabled type="info">已被领养</el-button>

              <el-button v-else-if="pet.status === 'REJECTED' || pet.status === 'OFFLINE'" size="small" type="primary" @click="openEdit(pet.id)">重新上架</el-button>

              <el-button v-else-if="pet.status === 'PENDING'" size="small" class="withdraw-btn" @click="handleWithdraw(pet.id)">撤回</el-button>

              <el-button v-else-if="pet.status === 'FIRST_PASS'" size="small" type="info" disabled>审核中</el-button>

              <el-button v-else-if="pet.status === 'APPROVED'" size="small" @click="handleOffline(pet.id)">下架</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </template>

    <!-- 重新上架编辑弹窗 -->
    <el-dialog v-model="editVisible" title="重新上架" width="600px">
      <el-form :model="editForm" label-width="100px" size="large">
        <el-form-item label="健康证明">
          <el-upload
            :before-upload="handleHealthCertUpload"
            accept="image/*"
            list-type="picture-card"
            :file-list="healthCertFileList"
            :on-remove="handleHealthCertRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="宠物分类" required>
          <el-select
            v-model="editForm.categoryId"
            style="width:200px"
            popper-class="auto-close-popper"
            :ref="(el) => setSelectRef('_cat', el)"
            @visible-change="(v) => onSelectVisible(v, '_cat')"
          >
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物名称" required>
          <el-input v-model="editForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="年龄" required>
          <el-select
            v-model="editForm.age"
            style="width:200px"
            popper-class="auto-close-popper"
            :ref="(el) => setSelectRef('_age', el)"
            @visible-change="(v) => onSelectVisible(v, '_age')"
          >
            <el-option v-for="n in 100" :key="n" :label="n + '岁'" :value="n + '岁'" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" required>
          <el-radio-group v-model="editForm.gender">
            <el-radio value="male">公</el-radio>
            <el-radio value="female">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="绝育">
          <el-switch v-model="editForm.isNeutered" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="疫苗">
          <el-switch v-model="editForm.isVaccinated" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="性格描述" required>
          <el-input v-model="editForm.personality" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="送养原因" required>
          <el-input v-model="editForm.reason" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="宠物照片" prop="images">
          <div class="multi-upload-wrap">
            <el-upload
              :file-list="imageFileList"
              :before-upload="handleImageUpload"
              :on-remove="handleImageRemove"
              list-type="picture-card"
              accept="image/*"
              multiple
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <p class="upload-tip">至少上传3张照片，第1张作为封面</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="dialog-cancel-btn" @click="editVisible = false">取消</el-button>
        <el-button class="dialog-submit-btn" :loading="editSaving" @click="handleEditSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Plus } from '@element-plus/icons-vue'
import { getCategories, getMyPets, getPetDetail, offlinePet, withdrawPet, updatePet } from '@/api/pet'
import { uploadFile, uploadFiles } from '@/api/file'
import { PET_STATUS, GENDER_MAP } from '@/utils/constants'
import { useSelectAutoClose } from '@/composables/useSelectAutoClose'
const { setSelectRef, onSelectVisible, cleanupSelectAutoClose } = useSelectAutoClose()

const list = ref([])
const loading = ref(true)
const statusFilter = ref('')
const categories = ref([])
const editVisible = ref(false)
const editSaving = ref(false)
const editPetId = ref(null)
const editForm = ref({
  categoryId: null, name: '', age: '', gender: 'male',
  isNeutered: 0, isVaccinated: 0,
  healthCert: '', personality: '', habit: '', reason: ''
})
const healthCertFileList = ref([])
const imageFileList = ref([])
const imageUrlList = ref([])

const filteredList = computed(() => {
  if (!statusFilter.value) return list.value
  return list.value.filter(p => p.status === statusFilter.value)
})

async function loadList() {
  loading.value = true
  try {
    list.value = await getMyPets()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try { categories.value = await getCategories() } catch { categories.value = [] }
}

async function openEdit(petId) {
  editPetId.value = petId
  try {
    const detail = await getPetDetail(petId)
    editForm.value = {
      categoryId: detail.categoryId,
      name: detail.name || '',
      age: detail.age || '',
      gender: detail.gender || 'male',
      isNeutered: detail.isNeutered || 0,
      isVaccinated: detail.isVaccinated || 0,
      healthCert: detail.healthCert || '',
      personality: detail.personality || '',
      habit: detail.habit || '',
      reason: detail.reason || ''
    }
    // 加载现有健康证和图片
    healthCertFileList.value = detail.healthCert ? [{ name: 'health-cert', url: detail.healthCert }] : []
    const existingImages = detail.images || []
    imageUrlList.value = [...existingImages]
    imageFileList.value = existingImages.map((url, i) => ({
      name: 'image-' + i,
      url: url
    }))
    editVisible.value = true
  } catch {
    ElMessage.error('获取宠物信息失败')
  }
}

async function handleHealthCertUpload(file) {
  try {
    const res = await uploadFile(file, 'pet')
    editForm.value.healthCert = res.url
    healthCertFileList.value = [{ name: file.name, url: res.url }]
  } catch {
    ElMessage.error('健康证上传失败')
  }
  return false
}

function handleHealthCertRemove() {
  healthCertFileList.value = []
  editForm.value.healthCert = ''
}

async function handleImageUpload(file) {
  try {
    const res = await uploadFiles([file], 'pet')
    const url = res.urls[0]
    imageUrlList.value.push(url)
    imageFileList.value.push({ name: file.name, url })
  } catch {
    ElMessage.error('图片上传失败')
  }
  return false
}

function handleImageRemove(file) {
  const idx = imageFileList.value.findIndex(f => f.url === file.url || f.uid === file.uid)
  if (idx !== -1) {
    imageFileList.value.splice(idx, 1)
    imageUrlList.value.splice(idx, 1)
  }
}

async function handleEditSubmit() {
  if (!editForm.value.categoryId || !editForm.value.name || !editForm.value.personality || !editForm.value.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (imageUrlList.value.length < 3) {
    ElMessage.warning('请至少上传3张宠物照片')
    return
  }
  editSaving.value = true
  try {
    const submitData = { ...editForm.value, images: imageUrlList.value }
    await updatePet(editPetId.value, submitData)
    ElMessage.success('已提交审核')
    editVisible.value = false
    loadList()
  } finally {
    editSaving.value = false
  }
}

async function handleOffline(petId) {
  try {
    await ElMessageBox.confirm('确定下架该宠物？', '提示')
    await offlinePet(petId)
    ElMessage.success('已下架')
    loadList()
  } catch {
    // 取消或失败
  }
}

async function handleWithdraw(petId) {
  try {
    await ElMessageBox.confirm(
      '确定撤回该送养申请？撤回后需重新提交审核。',
      '撤回确认',
      { confirmButtonText: '确认撤回', cancelButtonText: '取消', type: 'warning' }
    )
    await withdrawPet(petId)
    ElMessage.success('已撤回，请重新提交审核')
    loadList()
  } catch (e) { /* 拦截器已弹提示 */ }
}

onMounted(() => { loadCategories(); loadList() })
onUnmounted(cleanupSelectAutoClose)
</script>

<style scoped>
.donate-list-page { max-width: 800px; margin: 0 auto; padding: 24px 0 40px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; color: var(--yc-text-primary); }
.loading-center { display: flex; justify-content: center; padding: 80px 0; }
.empty-tip { padding: 60px 0; }

.pet-list { display: flex; flex-direction: column; gap: 12px; }
.pet-content { display: flex; align-items: center; gap: 16px; }
.img-placeholder { width: 100px; height: 100px; background: var(--yc-bg-card); border-radius: 8px; }

.pet-info { flex: 1; min-width: 0; }
.pet-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.pet-header h4 { margin: 0; font-size: 16px; color: var(--yc-text-primary); }
.pet-meta { margin: 0; font-size: 13px; color: var(--yc-text-secondary); }

.pet-actions { display: flex; flex-direction: column; gap: 8px; flex-shrink: 0; width: 120px; }
.pet-actions .el-button { width: 100%; }
.pet-actions .el-button + .el-button { margin-left: 0; }
.withdraw-btn { border-color: var(--yc-border) !important; color: var(--yc-text-primary) !important; }
.withdraw-btn:hover { border-color: var(--yc-border-hover) !important; color: var(--yc-accent) !important; }

/* 卡片暖色 */
:deep(.pet-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

/* Tabs 暖色 */
:deep(.el-tabs__active-bar) {
  background: var(--yc-accent);
}
:deep(.el-tabs__item.is-active) {
  color: var(--yc-text-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--yc-accent);
}

/* 按钮暖色 */
:deep(.publish-new-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.publish-new-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}

/* 弹窗按钮 */
:deep(.dialog-cancel-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.dialog-cancel-btn:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
:deep(.dialog-submit-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.dialog-submit-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}



.multi-upload-wrap {
  width: 100%;
}
.upload-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
</style>
