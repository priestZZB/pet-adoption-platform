<template>
  <div class="profile-page">
    <el-card class="user-card">
      <div class="user-header">
        <!-- 头像 -->
        <div class="avatar-wrap">
          <el-upload
            :show-file-list="false"
            :before-upload="handleAvatarUpload"
            accept="image/*"
            class="avatar-upload"
          >
            <el-avatar :size="80" :src="userStore.userInfo?.avatar" class="avatar">
              {{ userStore.userInfo?.nickname?.[0] || 'U' }}
            </el-avatar>
            <div class="camera-badge">
              <el-icon :size="14"><Camera /></el-icon>
            </div>
          </el-upload>
          <span class="avatar-hint">点击更换</span>
        </div>

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

      <!-- 详细信息 -->
      <el-divider />
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">手机号</span>
          <span class="detail-value">{{ userStore.userInfo?.phone || '未设置' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邮箱</span>
          <span class="detail-value">{{ userStore.userInfo?.email || '未设置' }}</span>
        </div>
        <div class="detail-item" v-if="userStore.userInfo?.isRealName === 1">
          <span class="detail-label">真实姓名</span>
          <span class="detail-value">{{ userStore.userInfo?.realName }}</span>
        </div>
        <div class="detail-item" v-if="userStore.userInfo?.isRealName === 1">
          <span class="detail-label">身份证号</span>
          <span class="detail-value">{{ userStore.userInfo?.idCard }}</span>
        </div>
      </div>
    </el-card>

    <!-- 功能入口 -->
    <el-card class="menu-card">
      <template #header><span>功能入口</span></template>
      <div class="menu-grid">
        <div class="menu-item" @click="$router.push('/user/profile/edit')">
          <el-icon :size="24" color="#409EFF"><Edit /></el-icon>
          <span>编辑资料</span>
        </div>
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
        <div class="menu-item" @click="aboutVisible = true">
          <el-icon :size="24" color="#409EFF"><InfoFilled /></el-icon>
          <span>关于我们</span>
        </div>
      </div>
    </el-card>

    <!-- 关于我们弹窗 -->
    <el-dialog v-model="aboutVisible" title="关于有宠" width="620px">
      <div class="about-content">
        <div class="about-logo">
          <el-icon :size="40" color="#409EFF"><StarFilled /></el-icon>
          <h2>有宠</h2>
          <p class="about-slogan">用领养代替购买，给毛孩子一个温暖的家</p>
        </div>

        <el-divider />

        <div class="about-section">
          <h4>我们的使命</h4>
          <p>「有宠」是一个专注于宠物领养与救助的公益平台，致力于连接需要帮助的流浪动物与有爱心的领养人。我们相信每一个生命都值得被温柔以待。</p>
        </div>

        <div class="about-section">
          <h4>核心功能</h4>
          <ul>
            <li><b>宠物领养</b> — 浏览待领养宠物，参加领养考试，提交领养申请，给流浪毛孩子一个家</li>
            <li><b>送养发布</b> — 帮助流浪动物发布送养信息，寻找合适的领养人</li>
            <li><b>志愿者网络</b> — 志愿者参与送养审核与走访，确保每一只宠物都去往安全的环境</li>
            <li><b>宠物商城</b> — 精选宠物用品，购物即是对平台的支持</li>
            <li><b>AI助手</b> — 智能解答宠物领养、养护、救助等问题</li>
            <li><b>社区互助</b> — 意见反馈与公示机制，打造透明可信的救助生态</li>
          </ul>
        </div>

        <div class="about-section">
          <h4>平台信息</h4>
          <table class="about-table">
            <tr><td>平台名称</td><td>有宠（Pet Adoption Platform）</td></tr>
            <tr><td>项目类型</td><td>宠物领养救助管理平台</td></tr>
            <tr><td>技术栈</td><td>Spring Boot + Vue 3 + MySQL + Redis + Docker</td></tr>
            <tr><td>版本号</td><td>v1.0.0</td></tr>
            <tr><td>联系邮箱</td><td>zzb070104@163.com</td></tr>
          </table>
        </div>

        <div class="about-section">
          <h4>我们的愿景</h4>
          <p>让每一只流浪动物都能找到温暖的家，让每一次领养都成为幸福的开始。</p>
        </div>

        <el-divider />

        <p class="about-footer">© 2026 毛球寻觅 · 用领养代替购买</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { ref } from 'vue'
import {
  CircleCheck, Edit, Postcard, Lock, User, UserFilled,
  Star, Tickets, Select, ChatLineSquare, Camera, InfoFilled, StarFilled
} from '@element-plus/icons-vue'
import { uploadFile } from '@/api/file'
import { uploadAvatar } from '@/api/user'
import { ROLE_MAP } from '@/utils/constants'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const aboutVisible = ref(false)

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
.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.avatar-upload {
  position: relative;
}
.avatar {
  cursor: pointer;
}
.camera-badge {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 26px;
  height: 26px;
  background: #409EFF;
  border: 2px solid #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.avatar-hint {
  font-size: 12px;
  color: #909399;
  cursor: pointer;
}
.avatar-hint:hover {
  color: #409EFF;
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

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 0 4px;
}
.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.detail-label {
  font-size: 12px;
  color: #909399;
}
.detail-value {
  font-size: 14px;
  color: #303133;
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

/* 关于我们弹窗 */
.about-content {
  text-align: center;
}
.about-logo {
  padding: 20px 0 10px;
}
.about-logo h2 {
  margin: 10px 0 6px;
  font-size: 24px;
  color: #303133;
}
.about-slogan {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
.about-section {
  text-align: left;
  margin: 16px 0;
}
.about-section h4 {
  font-size: 15px;
  color: #303133;
  margin: 0 0 8px;
}
.about-section p {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  margin: 0;
}
.about-section ul {
  margin: 0;
  padding-left: 20px;
}
.about-section li {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}
.about-table {
  width: 100%;
  border-collapse: collapse;
}
.about-table td {
  padding: 8px 12px;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}
.about-table td:first-child {
  color: #909399;
  width: 100px;
  white-space: nowrap;
}
.about-table td:last-child {
  color: #303133;
}
.about-footer {
  font-size: 13px;
  color: #909399;
  margin: 8px 0 0;
}
</style>
