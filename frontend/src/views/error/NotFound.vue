<template>
  <div class="error-page">
    <div class="error-bg"></div>
    <div class="error-card">
      <div class="error-illustration">
        <el-icon :size="80" color="#909399"><QuestionFilled /></el-icon>
      </div>
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-desc">您访问的页面不存在或已被移除</p>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="$router.replace('/')">
          返回首页
        </el-button>
        <el-button size="large" @click="$router.go(-1)">
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { QuestionFilled } from '@element-plus/icons-vue'
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
.error-code {
  font-size: 72px;
  font-weight: 800;
  color: #303133;
  margin: 0 0 8px;
  line-height: 1;
  letter-spacing: -2px;
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
