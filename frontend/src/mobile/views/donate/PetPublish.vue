<template>
  <div class="page">
    <div class="card-wrap">
      <MobileCard>
        <div class="section-title">发布送养信息</div>
        <div class="form-group"><label>宠物名称</label><el-input v-model="form.name" placeholder="给宠物取个名字" /></div>
        <div class="form-group"><label>品种</label><el-input v-model="form.breed" placeholder="例如：中华田园猫" /></div>
        <div class="form-group"><label>年龄</label><el-input v-model="form.age" placeholder="例如：1岁" /></div>
        <div class="form-group"><label>性别</label>
          <div class="radio-row">
            <button :class="['radio', { active: form.gender === 1 }]" @click="form.gender = 1">公</button>
            <button :class="['radio', { active: form.gender === 2 }]" @click="form.gender = 2">母</button>
          </div>
        </div>
        <div class="form-group"><label>描述</label><textarea v-model="form.description" placeholder="描述宠物的性格、健康状况等..." rows="3"></textarea></div>
        <div class="form-group"><label>封面图片</label>
          <div class="upload-row">
            <button class="upload-btn" @click="handleCoverUpload">选择图片</button>
            <span v-if="coverImage" class="upload-ok"><i class="fas fa-check-circle"></i> 已选择</span>
          </div>
          <input ref="coverInput" type="file" accept="image/*" style="display:none" @change="onCoverChange" />
        </div>
        <button class="save-btn" :disabled="submitting" @click="handleSubmit">{{ submitting ? '发布中...' : '发布送养' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publishPet } from '@/api/pet'
import { uploadAvatar } from '@/api/user'
import MobileCard from '../../components/MobileCard.vue'

const router = useRouter()
const submitting = ref(false)
const coverInput = ref(null)
const coverImage = ref('')

const form = reactive({ name: '', breed: '', age: '', gender: 1, description: '', coverImage: '' })

function handleCoverUpload() { coverInput.value?.click() }

async function onCoverChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadAvatar(file)
    coverImage.value = res?.avatar || res?.url || ''
    ElMessage.success('图片已上传')
  } catch {}
}

async function handleSubmit() {
  if (!form.name || !form.breed) { ElMessage.warning('请填写宠物名称和品种'); return }
  submitting.value = true
  try {
    form.coverImage = coverImage.value
    await publishPet(form)
    ElMessage.success('发布成功')
    router.push('/donate/pets')
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.card-wrap { margin-bottom: 12px; }
.section-title { font-size: 16px; font-weight: 600; color: #5a4a42; margin-bottom: 16px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
.form-group :deep(.el-input__wrapper) { height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
textarea { width: 100%; padding: 10px 12px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; font-size: 14px; color: #5a4a42; resize: none; outline: none; box-sizing: border-box; }
textarea:focus { border-color: #8ab8a0; }
.radio-row { display: flex; gap: 10px; }
.radio { flex: 1; height: 42px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.radio.active { background: #8ab8a0; border-color: #8ab8a0; color: #fff; }
.upload-row { display: flex; align-items: center; gap: 10px; }
.upload-btn { height: 40px; padding: 0 16px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.upload-ok { font-size: 13px; color: #8ab8a0; }
.save-btn { width: 100%; height: 48px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
</style>
