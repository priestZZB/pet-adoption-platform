<template>
  <el-menu
    :default-active="route.path"
    :router="true"
    collapse-transition
    style="height:100%;border-right:none"
  >
    <el-menu-item index="/admin">
      <el-icon><DataBoard /></el-icon>
      <span>仪表盘</span>
    </el-menu-item>
    <el-menu-item index="/admin/users">
      <el-icon><User /></el-icon>
      <span>用户管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/volunteer">
      <el-icon><Checked /></el-icon>
      <span>志愿者审核</span>
    </el-menu-item>
    <el-menu-item index="/admin/donor">
      <el-icon><Checked /></el-icon>
      <span>送养人审核</span>
    </el-menu-item>
    <el-menu-item index="/admin/pet-categories">
      <el-icon><Grid /></el-icon>
      <span>宠物分类</span>
    </el-menu-item>
    <el-menu-item index="/admin/pets">
      <el-icon><Guide /></el-icon>
      <span>宠物管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/questions">
      <el-icon><Edit /></el-icon>
      <span>试题管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/adopt-applications">
      <el-icon><Document /></el-icon>
      <span>领养管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/mall-categories">
      <el-icon><Grid /></el-icon>
      <span>商品分类</span>
    </el-menu-item>
    <el-menu-item index="/admin/mall-products">
      <el-icon><Goods /></el-icon>
      <span>商品管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/mall-orders">
      <el-icon><List /></el-icon>
      <span>订单管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/notices">
      <el-icon><Bell /></el-icon>
      <span>公告管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/feedback">
      <el-icon><ChatLineSquare /></el-icon>
      <span>反馈管理</span>
    </el-menu-item>
    <el-menu-item index="/admin/logs">
      <el-icon><Clock /></el-icon>
      <span>操作日志</span>
    </el-menu-item>
    <el-menu-item index="/admin/ai-records">
      <el-icon><Cpu /></el-icon>
      <span>AI记录</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
