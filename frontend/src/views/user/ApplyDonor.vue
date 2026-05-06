<template>
  <div class="apply-page">
    <h3 class="page-title">申请成为送养人</h3>

    <el-card>
      <!-- 未实名 -->
      <div v-if="userStore.userInfo?.isRealName !== 1" class="result-content">
        <el-icon :size="48" color="#E6A23C"><WarningFilled /></el-icon>
        <h3>请先完成实名认证</h3>
        <p class="result-desc">申请成为送养人需要先进行实名认证</p>
        <el-button type="primary" @click="$router.push('/user/real-name')">
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
            type="primary"
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
          <el-button type="success" @click="$router.push('/donate/publish')">
            发布送养宠物
          </el-button>
        </template>

        <!-- 已驳回 -->
        <template v-else-if="status === 'REJECTED'">
          <el-icon :size="48" color="#F56C6C"><CircleCloseFilled /></el-icon>
          <h3>申请未通过</h3>
          <p class="result-desc">你的送养人申请被驳回了，如有疑问请联系管理员</p>
          <el-button type="primary" @click="handleApply">
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
import { WarningFilled, UserFilled, Clock, CircleCheck, CircleCloseFilled } from '@element-plus/icons-vue'
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
  color: #303133;
  margin: 0 0 20px;
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
  color: #303133;
}
.result-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
  max-width: 360px;
  line-height: 1.6;
}
</style>
