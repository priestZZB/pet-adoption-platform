<template>
  <div class="error-page">
    <div class="error-bg"></div>
    <div class="error-card">
      <div class="error-illustration">
        <el-icon :size="80" color="#409EFF"><Setting /></el-icon>
      </div>
      <h2 class="error-title">系统维护中</h2>
      <p class="error-desc">
        平台正在进行系统维护，预计很快完成<br />
        维护期间数据不会丢失，请您耐心等待
      </p>
      <div class="error-actions">
        <el-button type="primary" size="large" :loading="checking" @click="checkStatus">
          检查状态
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Setting } from '@element-plus/icons-vue'

const checking = ref(false)

function checkStatus() {
  checking.value = true
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
</style>
