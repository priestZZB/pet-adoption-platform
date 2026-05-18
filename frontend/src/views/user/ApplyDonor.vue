<template>
  <div class="apply-page">
    <h3 class="page-title">申请成为送养人</h3>

    <el-card>
      <!-- 管理员禁止申请 -->
      <div v-if="userStore.hasRole('ADMIN')" class="result-content">
        <el-icon :size="48" color="#909399"><InfoFilled /></el-icon>
        <h3>管理员无需申请送养人</h3>
        <p class="result-desc">你已是系统管理员，拥有最高管理权限，无需申请成为送养人</p>
      </div>

      <!-- 未实名 -->
      <div v-else-if="userStore.userInfo?.isRealName !== 1" class="result-content">
        <el-icon :size="48" color="#E6A23C"><WarningFilled /></el-icon>
        <h3>请先完成实名认证</h3>
        <p class="result-desc">申请成为送养人需要先进行实名认证</p>
        <el-button class="dono-action-btn" @click="$router.push('/user/real-name')">
          去实名认证
        </el-button>
      </div>

      <!-- 已实名，查看状态 -->
      <div v-else class="result-content">
        <!-- 未申请 -->
        <template v-if="status === 'NONE'">
          <el-icon :size="48" color="#F56C6C"><UserFilled /></el-icon>
          <h3>成为送养人</h3>
          <p class="result-desc">
            送养人可以发布待领养宠物信息、审核领养申请，为流浪动物寻找新家
          </p>
          <el-button
            class="dono-action-btn"
            :loading="submitting"
            @click="handleApply"
          >
            提交申请
          </el-button>
        </template>

        <!-- 待审核 -->
        <template v-else-if="status === 'PENDING'">
          <el-icon :size="48" color="#E6A23C"><Clock /></el-icon>
          <h3>申请已提交，等待管理员审核</h3>
          <p class="result-desc">请耐心等待，审核结果会通过系统通知</p>
        </template>

        <!-- 已通过 -->
        <template v-else-if="status === 'APPROVED'">
          <el-icon :size="48" color="#67C23A"><CircleCheck /></el-icon>
          <h3>恭喜，你已成为送养人！</h3>
          <p class="result-desc">现在可以发布送养宠物信息了</p>
          <el-button class="dono-action-btn green" @click="$router.push('/donate/publish')">
            发布送养宠物
          </el-button>
        </template>

        <!-- 已驳回 -->
        <template v-else-if="status === 'REJECTED'">
          <el-icon :size="48" color="#F56C6C"><CircleCloseFilled /></el-icon>
          <h3>申请未通过</h3>
          <p class="result-desc">你的送养人申请被驳回了，如有疑问请联系管理员</p>
          <el-button class="dono-action-btn" @click="handleApply">
            重新申请
          </el-button>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled, UserFilled, Clock, CircleCheck, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'
import { donorApply, getDonorStatus } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const status = ref('NONE')
const submitting = ref(false)

async function loadStatus() {
  try {
    const res = await getDonorStatus()
    status.value = res.donorStatus || 'NONE'
  } catch {
    status.value = 'NONE'
  }
}

async function handleApply() {
  submitting.value = true
  try {
    await donorApply()
    ElMessage.success('申请已提交，等待审核')
    status.value = 'PENDING'
  } catch {
    // 请求拦截器统一处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadStatus)
</script>

<style scoped>
.apply-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}

:deep(.el-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

.result-content {
  text-align: center;
  padding: 30px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.result-content h3 {
  margin: 0;
  font-size: 18px;
  color: var(--yc-text-primary);
}
.result-desc {
  margin: 0;
  font-size: 14px;
  color: var(--yc-text-secondary);
  max-width: 360px;
  line-height: 1.6;
}

:deep(.dono-action-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  padding: 10px 24px;
  font-size: 14px;
}
:deep(.dono-action-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.dono-action-btn.green) {
  background: var(--yc-accent);
  border-color: var(--yc-accent);
  color: #fff;
}
:deep(.dono-action-btn.green:hover) {
  background: #7aaa92;
  border-color: #7aaa92;
}
</style>
