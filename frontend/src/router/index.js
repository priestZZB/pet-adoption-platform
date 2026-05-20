import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const routes = [
  // ===== 独立页（无布局）=====
  { path: '/login',         component: () => import('@/views/auth/AuthPage.vue') },
  { path: '/register',      component: () => import('@/views/user/Register.vue') },
  { path: '/reset-password', component: () => import('@/views/user/ResetPassword.vue') },

  // ===== 前台主布局（带导航栏，仅导航栏有的路由）=====
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '',           component: () => import('@/views/home/Home.vue') },
      { path: 'mall',       component: () => import('@/views/mall/MallList.vue') },
      { path: 'notices',    component: () => import('@/views/notice/NoticeList.vue') },
      { path: 'notices/:id', component: () => import('@/views/notice/NoticeDetail.vue') },
      { path: 'ai',         component: () => import('@/views/ai/AIChat.vue'), meta: { requiresAuth: true } },
      { path: 'user/orders',      component: () => import('@/views/mall/MyOrders.vue'), meta: { requiresAuth: true } },
      { path: 'mall/cart',         component: () => import('@/views/mall/Cart.vue'), meta: { requiresAuth: true } },
    ]
  },

  // ===== 简洁布局（带←返回，导航栏没有的都用这个）=====
  {
    path: '/',
    component: () => import('@/layouts/SubLayout.vue'),
    children: [
      { path: 'pets/:id',   component: () => import('@/views/pet/PetDetail.vue') },
      { path: 'mall/products/:id', component: () => import('@/views/mall/MallDetail.vue') },
      { path: 'user/profile',     component: () => import('@/views/user/UserProfile.vue'), meta: { requiresAuth: true } },
      { path: 'user/profile/edit', component: () => import('@/views/user/EditProfile.vue'), meta: { requiresAuth: true } },
      { path: 'user/password',     component: () => import('@/views/user/ChangePassword.vue'), meta: { requiresAuth: true } },
      { path: 'user/phone',      component: () => import('@/views/user/ChangePhone.vue'), meta: { requiresAuth: true } },
      { path: 'user/real-name',    component: () => import('@/views/user/RealName.vue'), meta: { requiresAuth: true } },
      { path: 'user/favorites',   component: () => import('@/views/pet/PetFavorites.vue'), meta: { requiresAuth: true } },
      { path: 'user/orders/:id',  component: () => import('@/views/mall/OrderDetail.vue'), meta: { requiresAuth: true } },
      { path: 'user/chats',       component: () => import('@/views/chat/ChatList.vue'), meta: { requiresAuth: true } },
      { path: 'user/chat',        component: () => import('@/views/chat/ChatDetail.vue'), meta: { requiresAuth: true } },
      { path: 'user/feedback',    component: () => import('@/views/user/MyFeedback.vue'), meta: { requiresAuth: true } },
      { path: 'user/adopt-applications', component: () => import('@/views/adopt/MyApplications.vue'), meta: { requiresAuth: true } },
      { path: 'user/volunteer-apply', component: () => import('@/views/user/ApplyVolunteer.vue'), meta: { requiresAuth: true } },
      { path: 'user/donor-apply',    component: () => import('@/views/user/ApplyDonor.vue'), meta: { requiresAuth: true } },
      { path: 'mall/pay/:id', component: () => import('@/views/mall/PayPage.vue'), meta: { requiresAuth: true } },
      { path: 'mall/checkout',     component: () => import('@/views/mall/Checkout.vue'), meta: { requiresAuth: true } },
      { path: 'mall/addresses',    component: () => import('@/views/mall/AddressSelect.vue'), meta: { requiresAuth: true } },
      { path: 'mall/address-select', component: () => import('@/views/mall/AddressSelect.vue'), meta: { requiresAuth: true } },
      { path: 'adopt/exam',        component: () => import('@/views/adopt/ExamPage.vue'), meta: { requiresAuth: true } },
      { path: 'adopt/apply/:petId', component: () => import('@/views/adopt/AdoptApply.vue'), meta: { requiresAuth: true } },
      { path: 'user/adopt-application/:id', component: () => import('@/views/adopt/ApplicationDetail.vue'), meta: { requiresAuth: true } },
      { path: 'donate/publish',    component: () => import('@/views/donate/PetPublish.vue'), meta: { requiresAuth: true, role: 'USER_ADOPTER' } },
      { path: 'donate/pets',       component: () => import('@/views/donate/MyDonateList.vue'), meta: { requiresAuth: true, role: 'USER_ADOPTER' } },
      { path: 'donate/detail/:id', component: () => import('@/views/donate/DonorPetDetail.vue'), meta: { requiresAuth: true, role: 'USER_ADOPTER' } },
      { path: 'donate/pets/:id/applications', component: () => import('@/views/donate/DonateApplications.vue'), meta: { requiresAuth: true, role: 'USER_ADOPTER' } },
      { path: 'volunteer/pending',  component: () => import('@/views/volunteer/PendingReviews.vue'), meta: { requiresAuth: true, role: 'VOLUNTEER' } },
      { path: 'volunteer/reviewed', component: () => import('@/views/volunteer/ReviewedHistory.vue'), meta: { requiresAuth: true, role: 'VOLUNTEER' } },
      { path: 'volunteer/review-history/:id', component: () => import('@/views/volunteer/ReviewHistoryDetail.vue'), meta: { requiresAuth: true, role: 'VOLUNTEER' } },
      { path: 'volunteer/visits',   component: () => import('@/views/volunteer/VisitRecords.vue'), meta: { requiresAuth: true, role: 'VOLUNTEER' } },
      { path: 'volunteer/visits/add', component: () => import('@/views/volunteer/AddVisit.vue'), meta: { requiresAuth: true, role: 'VOLUNTEER' } },
      { path: 'volunteer/review/:id', component: () => import('@/views/volunteer/PetReviewDetail.vue'), meta: { requiresAuth: true, role: 'VOLUNTEER' } },
    ]
  },

  // ===== 管理后台布局（仅ADMIN）=====
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '',                   component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'users',              component: () => import('@/views/admin/UserManage.vue') },
      { path: 'volunteer',          component: () => import('@/views/admin/VolunteerManage.vue') },
      { path: 'donor',              component: () => import('@/views/admin/DonorManage.vue') },
      { path: 'pet-categories',     component: () => import('@/views/admin/PetCategoryManage.vue') },
      { path: 'pets',               component: () => import('@/views/admin/PetManage.vue') },
      { path: 'questions',          component: () => import('@/views/admin/QuestionManage.vue') },
      { path: 'adopt-applications', component: () => import('@/views/admin/AdoptManage.vue') },
      { path: 'mall-categories',    component: () => import('@/views/admin/MallCategoryManage.vue') },
      { path: 'mall-products',      component: () => import('@/views/admin/ProductManage.vue') },
      { path: 'mall-orders',        component: () => import('@/views/admin/OrderManage.vue') },
      { path: 'notices',            component: () => import('@/views/admin/NoticeManage.vue') },
      { path: 'feedback',           component: () => import('@/views/admin/FeedbackManage.vue') },
      { path: 'logs',               component: () => import('@/views/admin/LogManage.vue') },
      { path: 'ai-records',         component: () => import('@/views/admin/AIRecords.vue') },
      { path: 'banners',            component: () => import('@/views/admin/BannerManage.vue') },
      { path: 'review/:id',         component: () => import('@/views/admin/PetReviewDetail.vue') },
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

// 路由守卫：未登录 → 跳登录 | 无权限 → 跳403
router.beforeEach(async (to, from) => {
  const token = getToken()
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  if (to.meta.role) {
    const userStore = useUserStore()
    // 每次进入需要角色的页面都重新获取用户信息，确保角色是最新的
    if (token) {
      await userStore.fetchUserInfo()
    }
    if (!userStore.hasRole(to.meta.role)) {
      ElMessage.error('权限不足')
      return '/'
    }
  }
  // 直接放行
})

export default router

