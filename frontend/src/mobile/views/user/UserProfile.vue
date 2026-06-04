<template>
  <div class="profile-page">
    <!-- 头像区域 -->
    <div class="avatar-section">
      <div class="avatar-wrap" @click="handleAvatar">
        <img :src="user?.avatar || '/images/logo.jpg'" class="avatar-img" />
        <div class="avatar-edit"><i class="fas fa-camera"></i></div>
      </div>
      <h2 class="nickname">{{ user?.nickname || '用户' }}</h2>
      <p class="role-tag">{{ roleLabel }}</p>
    </div>

    <!-- 菜单列表 -->
    <div class="card-wrap">
      <MobileCard>
        <div class="menu-list">
          <router-link v-for="m in menus" :key="m.path" :to="m.path" class="menu-item">
            <i :class="m.icon"></i>
            <span>{{ m.label }}</span>
            <i class="fas fa-chevron-right arrow"></i>
          </router-link>
        </div>
      </MobileCard>
    </div>

    <div class="card-wrap">
      <MobileCard>
        <div class="menu-list">
          <div class="menu-item logout" @click="handleLogout">
            <i class="fas fa-sign-out-alt"></i>
            <span>退出登录</span>
          </div>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import MobileCard from '../../components/MobileCard.vue'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)

const roleLabel = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isVolunteer) return '志愿者'
  if (userStore.isDonor) return '送养人'
  return '普通用户'
})

const menus = [
  { path: '/user/profile/edit', label: '编辑资料', icon: 'fas fa-user-edit' },
  { path: '/user/password',     label: '修改密码', icon: 'fas fa-lock' },
  { path: '/user/phone',        label: '换绑手机', icon: 'fas fa-mobile-alt' },
  { path: '/user/real-name',     label: '实名认证', icon: 'fas fa-id-card' },
  { path: '/user/orders',        label: '我的订单', icon: 'fas fa-receipt' },
  { path: '/user/favorites',     label: '我的收藏', icon: 'fas fa-star' },
  { path: '/user/reviews',       label: '我的评价', icon: 'fas fa-comment-dots' },
  { path: '/user/feedback',      label: '我的反馈', icon: 'fas fa-comment' },
  { path: '/user/chats',         label: '我的消息', icon: 'fas fa-envelope' },
  { path: '/user/adopt-applications', label: '领养申请', icon: 'fas fa-clipboard-list' },
  { path: '/user/volunteer-apply',    label: '申请志愿者', icon: 'fas fa-hands-helping' },
  { path: '/user/donor-apply',        label: '申请送养人', icon: 'fas fa-hand-holding-heart' },
]

function handleAvatar() { router.push('/user/profile/edit') }

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示')
    userStore.logout()
    router.push('/login')
  } catch {}
}
</script>

<style scoped>
.profile-page { padding-bottom: 80px; }
.avatar-section { display: flex; flex-direction: column; align-items: center; padding: 32px 16px 24px; background: linear-gradient(180deg, #c19a6b 0%, #f7f1e5 100%); }
.avatar-wrap { position: relative; width: 80px; height: 80px; margin-bottom: 12px; cursor: pointer; }
.avatar-img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; border: 3px solid rgba(255,255,255,0.5); }
.avatar-edit { position: absolute; bottom: 0; right: 0; width: 28px; height: 28px; border-radius: 50%; background: rgba(255,255,255,0.9); display: flex; align-items: center; justify-content: center; color: #5a4a42; font-size: 12px; }
.nickname { font-size: 20px; font-weight: 700; color: #5a4a42; margin: 0 0 4px; }
.role-tag { font-size: 13px; color: #a09080; margin: 0; }
.card-wrap { padding: 12px 16px 0; }
.menu-list { display: flex; flex-direction: column; }
.menu-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 0; font-size: 15px; color: #5a4a42; text-decoration: none;
  border-bottom: 1px solid #f5f0e8;
}
.menu-item:last-child { border-bottom: none; }
.menu-item i:first-child { width: 22px; text-align: center; color: #8ab8a0; font-size: 16px; }
.arrow { margin-left: auto; color: #c0b8a8; font-size: 12px; }
.menu-item.logout { color: #e8564a; cursor: pointer; }
.menu-item.logout i:first-child { color: #e8564a; }
</style>
