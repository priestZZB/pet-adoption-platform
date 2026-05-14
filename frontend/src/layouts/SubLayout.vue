<template>
  <div class="sub-layout">
    <!-- 顶部 -->
    <div class="sub-header">
      <div class="sub-header-left">
        <el-button text :icon="ArrowLeft" size="large" @click="goBack" style="font-size:16px">
          返回
        </el-button>
      </div>
      <div class="sub-header-title">
        <el-image src="/images/logo.jpg" fit="contain" style="width:24px;height:24px;vertical-align:middle;margin-right:6px;border-radius:4px" />
        <span style="font-size:16px;font-weight:bold;color:#303133">毛球寻觅</span>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="sub-content">
      <router-view />
    </div>

    <!-- 页脚 -->
    <Footer />
  </div>
</template>

<script setup>
import { ArrowLeft, StarFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import Footer from '@/components/Footer.vue'

const router = useRouter()

function goBack() {
  router.back()
}
</script>

<style scoped>
.sub-layout {
  min-height: 100vh;
  background: #f5f7fa;
}
.sub-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;
}
.sub-header-left {
  flex: 1;
}
.sub-header-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}
.sub-content {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style>
