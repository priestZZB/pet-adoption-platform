<template>
  <div class="apply-page">
    <h3 class="page-title">提交领养申请</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="pet">
      <!-- 宠物信息摘要 -->
      <el-card class="pet-summary">
        <div class="pet-summary-inner">
          <el-image
            :src="pet.coverImage || pet.images?.[0]"
            fit="cover"
            style="width:100px;height:100px;border-radius:8px;flex-shrink:0"
          >
            <template #error>
              <div class="img-placeholder" />
            </template>
          </el-image>
          <div class="pet-meta">
            <h4>{{ pet.name }}</h4>
            <p>{{ pet.categoryName }} · {{ pet.age }} · {{ GENDER_MAP[pet.gender] || pet.gender }}</p>
          </div>
        </div>
      </el-card>

      <!-- 申请表单 -->
      <el-card class="form-card">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          size="large"
        >
          <el-form-item label="居住环境" prop="livingEnv">
            <el-input
              v-model="form.livingEnv"
              type="textarea"
              :rows="4"
              placeholder="请描述你的居住环境（如：自有住房、租房、有无阳台等）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="养宠经验" prop="petExp">
            <el-input
              v-model="form.petExp"
              type="textarea"
              :rows="4"
              placeholder="请描述你是否有养宠经验（选填）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="领养承诺" prop="commitment">
            <el-input
              v-model="form.commitment"
              type="textarea"
              :rows="4"
              placeholder="请写下你的领养承诺（如：不离不弃、定期疫苗、适龄绝育等）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button
              class="submit-adopt-btn"
              :loading="submitting"
              @click="handleSubmit"
            >
              提交申请
            </el-button>
            <el-button class="cancel-adopt-btn" @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>

    <template v-else>
      <div class="empty-tip">
        <el-empty description="宠物信息不存在" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getPetDetail } from '@/api/pet'
import { getExamHistory, submitApplication } from '@/api/adopt'
import { GENDER_MAP } from '@/utils/constants'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const loading = ref(true)
const pet = ref(null)

const form = reactive({
  livingEnv: '',
  petExp: '',
  commitment: ''
})

const rules = {
  livingEnv: [{ required: true, message: '请描述居住环境', trigger: 'blur' }],
  commitment: [{ required: true, message: '请填写领养承诺', trigger: 'blur' }]
}

async function loadPet() {
  loading.value = true
  try {
    pet.value = await getPetDetail(route.params.petId)
  } catch {
    pet.value = null
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submitApplication({
      petId: parseInt(route.params.petId),
      livingEnv: form.livingEnv,
      petExp: form.petExp,
      commitment: form.commitment
    })
    ElMessage.success('领养申请已提交，请等待审核')
    router.push('/user/adopt-applications')
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 检查是否登录
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 检查实名认证
  await userStore.fetchUserInfo()
  if (userStore.userInfo?.isRealName !== 1) {
    ElMessageBox.confirm(
      '领养前需要先完成实名认证，是否前往认证？',
      '实名认证',
      { confirmButtonText: '去认证', cancelButtonText: '取消', type: 'warning' }
    ).then(() => {
      router.push('/user/real-name')
    }).catch(() => {
      router.back()
    })
    return
  }

  // 检查最近一次考试是否满分
  try {
    const history = await getExamHistory()
    const passed = Array.isArray(history) && history.some(h => h.isPassed === 1)
    if (!passed) {
      ElMessageBox.confirm(
        '申请领养前需要通过领养考试（满分100分），是否前往考试？',
        '领养考试',
        { confirmButtonText: '去考试', cancelButtonText: '取消', type: 'warning' }
      ).then(() => {
        router.push('/adopt/exam')
      }).catch(() => {
        router.back()
      })
      return
    }
  } catch {
    // 查询失败时放行，后端会二次校验
  }

  loadPet()
})
</script>

<style scoped>
.apply-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
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

.pet-summary {
  margin-bottom: 16px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}
.pet-summary-inner {
  display: flex;
  gap: 16px;
  align-items: center;
}
.pet-meta h4 {
  margin: 0 0 6px;
  font-size: 16px;
  color: var(--yc-text-primary);
}
.pet-meta p {
  margin: 0;
  font-size: 14px;
  color: var(--yc-text-secondary);
}
.img-placeholder {
  width: 100px;
  height: 100px;
  background: var(--yc-bg-card);
  border-radius: 8px;
}

.form-card {
  margin-bottom: 20px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

/* 提交按钮暖色 */
:deep(.submit-adopt-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  padding: 12px 28px;
  font-size: 15px;
}
:deep(.submit-adopt-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
/* 取消按钮暖色 */
:deep(.cancel-adopt-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.cancel-adopt-btn:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
</style>
