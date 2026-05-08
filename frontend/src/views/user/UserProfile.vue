<template>
  <div class="profile-page">
    <el-card class="user-card">
      <div class="user-header">
        <!-- 头像 -->
        <el-upload
          :show-file-list="false"
          :before-upload="handleAvatarUpload"
          accept="image/*"
        >
          <el-avatar :size="80" :src="userStore.userInfo?.avatar" class="avatar">
            {{ userStore.userInfo?.nickname?.[0] || 'U' }}
          </el-avatar>
          <span class="avatar-hint">点击更换</span>
        </el-upload>

        <div class="user-meta">
          <h3>{{ userStore.userInfo?.nickname || '用户' }}</h3>
          <p class="username">@{{ userStore.userInfo?.username }}</p>
          <div class="role-tags">
            <el-tag
              v-for="role in userStore.roles"
              :key="role"
              :type="role === 'ADMIN' ? 'danger' : role === 'VOLUNTEER' ? 'success' : role === 'USER_ADOPTER' ? 'warning' : 'info'"
              size="small"
            >
              {{ ROLE_MAP[role] || role }}
            </el-tag>
          </div>
          <p v-if="userStore.userInfo?.isRealName === 1" class="verified">
            <el-icon color="#67C23A"><CircleCheck /></el-icon> 已实名认证
          </p>
        </div>
      </div>
    </el-card>

    <!-- 功能入口 -->
    <el-card class="menu-card">
      <template #header><span>功能入口</span></template>
      <div class="menu-grid">
        <div class="menu-item" @click="$router.push('/user/real-name')">
          <el-icon :size="24" color="#409EFF"><Postcard /></el-icon>
          <span>实名认证</span>
        </div>
        <div class="menu-item" @click="$router.push('/user/password')">
          <el-icon :size="24" color="#E6A23C"><Lock /></el-icon>
          <span>修改密码</span>
        </div>
        <div class="menu-item" @click="$router.push('/user/volunteer-apply')">
          <el-icon :size="24" color="#67C23A"><User /></el-icon>
          <span>申请志愿者</span>
        </div>
        <div class="menu-item" @click="$router.push('/user/donor-apply')">
          <el-icon :size="24" color="#F56C6C"><UserFilled /></el-icon>
          <span>申请送养人</span>
        </div>
        <div class="menu-item" @click="$router.push('/user/favorites')">
          <el-icon :size="24" color="#F56C6C"><Star /></el-icon>
          <span>我的收藏</span>
        </div>
        <div class="menu-item" @click="$router.push('/user/orders')">
          <el-icon :size="24" color="#409EFF"><Tickets /></el-icon>
          <span>我的订单</span>
        </div>
        <div class="menu-item" @click="$router.push('/user/adopt-applications')">
          <el-icon :size="24" color="#67C23A"><Select /></el-icon>
          <span>我的领养</span>
        </div>
        <div class="menu-item" @click="$router.push('/user/feedback')">
          <el-icon :size="24" color="#909399"><ChatLineSquare /></el-icon>
          <span>意见反馈</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import {
  CircleCheck, Postcard, Lock, User, UserFilled,
  Star, Tickets, Select, ChatLineSquare
} from '@element-plus/icons-vue'
import { uploadFile } from '@/api/file'
import { uploadAvatar } from '@/api/user'
import { ROLE_MAP } from '@/utils/constants'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

async function handleAvatarUpload(file) {
  try {
    await uploadAvatar(file)
    await userStore.fetchUserInfo()
    ElMessage.success('头像已更新')
  } catch {
    // 请求拦截器统一处理
  }
  return false
}
</script>

<style scoped>
.profile-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}

.user-card {
  margin-bottom: 20px;
}
.user-header {
  display: flex;
  align-items: center;
  gap: 24px;
}
.avatar {
  cursor: pointer;
}
.avatar-hint {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.user-meta h3 {
  margin: 0 0 4px;
  font-size: 20px;
  color: #303133;
}
.user-meta .username {
  margin: 0 0 8px;
  font-size: 14px;
  color: #909399;
}
.role-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 6px;
}
.verified {
  margin: 0;
  font-size: 13px;
  color: #67C23A;
  display: flex;
  align-items: center;
  gap: 4px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.menu-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}
.menu-item span {
  font-size: 13px;
  color: #606266;
}
</style>
