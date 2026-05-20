<template>
  <div class="dashboard">
    <h3 class="page-title">控制台</h3>

    <div v-if="loading" class="loading-wrap">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
    </div>

    <template v-else>
      <el-card class="welcome-card" shadow="never">
        <img src="/images/logo.jpg" class="welcome-logo" />
        <div class="welcome-text">
          <h4 class="welcome-main">欢迎回来，{{ userStore.userInfo?.nickname || '管理员' }}</h4>
          <p class="welcome-sub">以领养代替购买，让世间充满爱 ❤️</p>
          <p class="welcome-desc">宠物领养救助管理平台 · 后台管理系统</p>
        </div>
      </el-card>

      <div class="stat-grid">
        <el-card v-for="s in statCards" :key="s.label" class="stat-card" shadow="hover" :style="{ '--card-accent': s.color }">
          <div class="stat-inner">
            <el-icon :size="32" :color="s.color"><component :is="s.icon" /></el-icon>
            <div class="stat-info">
              <span class="stat-value">{{ s.value }}</span>
              <span class="stat-label">{{ s.label }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <el-card class="todo-card" shadow="hover">
        <template #header>
          <div class="todo-header">
            <el-icon :size="20" color="#E6A23C"><Notification /></el-icon>
            <span class="todo-title">待办事项</span>
            <el-tag v-if="pendingTotal > 0" type="danger" size="small" effect="dark">{{ pendingTotal }} 项待处理</el-tag>
            <el-tag v-else type="success" size="small" effect="plain">暂无待办</el-tag>
          </div>
        </template>
        <div v-if="pendingTotal > 0" class="todo-list">
          <div v-for="item in pendingItems" :key="item.key" class="todo-item" :class="{ 'todo-done': item.count === 0 }">
            <div class="todo-left">
              <span class="todo-dot" :style="{ background: item.color }"></span>
              <span class="todo-text"><strong>{{ item.count }}</strong> {{ item.label }}</span>
            </div>
            <el-button v-if="item.count > 0" :type="item.btnType" size="small" plain @click="goPage(item.link)">{{ item.btnText }}</el-button>
            <span v-else class="todo-none">暂无</span>
          </div>
        </div>
        <div v-else class="todo-empty">
          <el-icon :size="48" color="#67C23A"><CircleCheck /></el-icon>
          <p>所有事项已处理完毕！</p>
        </div>
      </el-card>

      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="chart-header">
            <el-icon :size="20" color="#409EFF"><TrendCharts /></el-icon>
            <span class="chart-title">近 14 天趋势</span>
          </div>
        </template>
        <div ref="chartRef" class="chart-container"></div>
      </el-card>

    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDashboardStats } from '@/api/admin'
import * as echarts from 'echarts'
import {
  User, Guide, Warning, Tickets,
  Loading, Notification, CircleCheck, TrendCharts
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const stats = ref(null)
const chartRef = ref(null)
let chartInstance = null

const cardDefs = [
  { key: 'userCount', label: '注册用户', icon: User, color: '#409EFF' },
  { key: 'petCount', label: '宠物总数', icon: Guide, color: '#67C23A' },
  { key: 'pendingTotal', label: '待审核', icon: Warning, color: '#F56C6C' },
  { key: 'orderCount', label: '订单总数', icon: Tickets, color: '#909399' }
]

const statCards = computed(() =>
  cardDefs.map(c => ({ ...c, value: stats.value?.[c.key] ?? '—' }))
)

const pendingTotal = computed(() => stats.value?.pending?.total ?? 0)

const pendingDefs = [
  { key: 'petFinalReview', label: '个宠物待终审', color: '#67C23A', btnType: 'success', btnText: '前往终审', link: '/admin/pets' },
  { key: 'volunteerApplies', label: '个志愿者申请待审核', color: '#E6A23C', btnType: 'warning', btnText: '前往处理', link: '/admin/volunteer' },
  { key: 'donorApplies', label: '个送养人申请待审核', color: '#F56C6C', btnType: 'danger', btnText: '前往处理', link: '/admin/donor' },
  { key: 'feedbackPending', label: '条意见反馈待回复', color: '#409EFF', btnType: 'primary', btnText: '前往回复', link: '/admin/feedback' }
]

const pendingItems = computed(() =>
  pendingDefs.map(d => ({ ...d, count: stats.value?.pending?.[d.key] ?? 0 }))
)

async function loadStats() {
  loading.value = true
  try {
    const data = await getDashboardStats()
    stats.value = data
  } catch { stats.value = null }
  finally {
    loading.value = false
    // DOM 渲染完后初始化图表
    setTimeout(() => initChart(stats.value?.chart), 100)
  }
}

function initChart(chartData) {
  if (!chartRef.value) return
  // 后端无数据时用示例数据展示
  if (!chartData || !chartData.labels) {
    chartData = {
      labels: ['暂无'],
      users: [0],
      pets: [0],
      orders: [0]
    }
  }
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['注册用户', '宠物发布', '订单'], bottom: 0 },
    grid: { left: 40, right: 20, bottom: 40, top: 20 },
    xAxis: { type: 'category', data: chartData.labels, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '注册用户', type: 'line', smooth: true, data: chartData.users, lineStyle: { color: '#409EFF' }, itemStyle: { color: '#409EFF' } },
      { name: '宠物发布', type: 'line', smooth: true, data: chartData.pets, lineStyle: { color: '#67C23A' }, itemStyle: { color: '#67C23A' } },
      { name: '订单', type: 'line', smooth: true, data: chartData.orders, lineStyle: { color: '#E6A23C' }, itemStyle: { color: '#E6A23C' } }
    ]
  })
  window.addEventListener('resize', () => chartInstance?.resize())
}

function goPage(path) { router.push(path) }

onMounted(loadStats)
onUnmounted(() => { stats.value = null; if (chartInstance) chartInstance.dispose() })
</script>

<style scoped>
.dashboard { max-width: 1000px; }
.page-title { font-size: 20px; color: #1a2332; margin: 0 0 20px; padding-left: 12px; border-left: 3px solid #409EFF; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; }
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { cursor: default; border-radius: 12px; border: 1px solid #e8e8e8; position: relative; overflow: hidden; transition: transform 0.2s, box-shadow 0.2s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.stat-card::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 3px; background: var(--card-accent); }
.stat-inner { display: flex; align-items: center; gap: 14px; }
.stat-info { display: flex; flex-direction: column; gap: 2px; }
.stat-value { font-size: 28px; font-weight: 700; color: #1a2332; line-height: 1.1; }
.stat-label { font-size: 13px; color: #909399; }
.todo-card { border-radius: 12px; border: 1px solid #e8e8e8; margin-bottom: 20px; }
.todo-header { display: flex; align-items: center; gap: 8px; }
.todo-title { font-size: 16px; font-weight: 600; color: #1a2332; flex: 1; }
.todo-list { display: flex; flex-direction: column; }
.todo-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 4px; border-bottom: 1px solid #f0f2f5; }
.todo-item:last-child { border-bottom: none; }
.todo-item:hover { background: #fafbfc; border-radius: 8px; }
.todo-item.todo-done { opacity: 0.45; }
.todo-left { display: flex; align-items: center; gap: 10px; }
.todo-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.todo-text { font-size: 14px; color: #303133; }
.todo-text strong { font-size: 16px; font-weight: 700; margin-right: 2px; }
.todo-none { font-size: 13px; color: #c0c4cc; }
.todo-empty { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 24px 0; color: #909399; }
.todo-empty p { margin: 0; font-size: 14px; }
.chart-card { border-radius: 12px; border: 1px solid #e8e8e8; margin-bottom: 20px; }
.chart-header { display: flex; align-items: center; gap: 8px; }
.chart-title { font-size: 16px; font-weight: 600; color: #1a2332; }
.chart-container { width: 100%; height: 300px; }
.welcome-card { display: flex; align-items: center; gap: 14px; border-radius: 12px; border: 1px solid #e8e8e8; background: linear-gradient(135deg, #f0f9ff 0%, #e8f4fd 100%); }
.welcome-card :deep(.el-card__body) { display: flex; align-items: center; gap: 14px; }
.welcome-text { flex: 1; }
.welcome-text h4 { margin: 0 0 4px; font-size: 16px; color: #1a2332; }
.welcome-text p { margin: 0; font-size: 13px; color: #909399; }
.welcome-logo { width: 42px; height: 42px; border-radius: 10px; flex-shrink: 0; object-fit: cover; }
</style>
