<template>
  <div class="error-page">
    <div class="error-bg"></div>
    <div class="error-card">
      <div class="error-illustration">
        <el-icon :size="80" color="#909399"><Connection /></el-icon>
      </div>
      <h2 class="error-title">网络连接失败</h2>
      <p class="error-desc">
        请检查您的网络连接后重试<br />
        如果网络正常，可能是服务器暂时不可用
      </p>
      <div class="error-actions">
        <el-button type="primary" size="large" :loading="checking" @click="retry">
          重新加载
        </el-button>
        <el-button size="large" @click="$router.replace('/')">
          返回首页
        </el-button>
      </div>
      <p v-if="retryCount > 0" class="retry-hint">
        已尝试 {{ retryCount }} 次重连
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Connection } from '@element-plus/icons-vue'

const checking = ref(false)
const retryCount = ref(0)

function retry() {
  checking.value = true
  retryCount.value++
  // 先尝试 fetch 首页判断网络是否恢复
  fetch('/api/banners')
    .then(res => {
      if (res.ok) {
        window.location.href = '/'
      } else {
        checking.value = false
      }
    })
    .catch(() => {
      checking.value = false
    })
}
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 20px;
}
.error-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: linear-gradient(135deg, rgba(247,241,229,0.82) 0%, rgba(254,250,245,0.9) 100%);
}
.error-card {
  position: relative;
  z-index: 1;
  text-align: center;
  background: #fff;
  border-radius: 16px;
  padding: 60px 40px;
  max-width: 480px;
  width: 100%;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}
.error-illustration {
  margin-bottom: 16px;
}
.error-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}
.error-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px;
  line-height: 1.6;
}
.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.retry-hint {
  margin-top: 16px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
