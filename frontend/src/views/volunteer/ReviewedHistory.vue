<template>
  <div class="history-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange" class="page-tabs">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="审核历史" name="reviewed" />
      <el-tab-pane label="去走访" name="add" />
      <el-tab-pane label="走访记录" name="visits" />
    </el-tabs>

    <!-- 筛选 -->
    <div class="filter-bar" v-if="list.length > 0">
      <el-radio-group v-model="filterAction" size="small">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="APPROVED">通过</el-radio-button>
        <el-radio-button value="REJECTED">打回</el-radio-button>
      </el-radio-group>
      <span class="filter-count">共 {{ filteredList.length }} 条</span>
    </div>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="filteredList.length === 0" class="empty-tip">
        <el-empty :description="filterAction ? '暂无该类型的审核记录' : '暂无审核记录'" />
      </div>

      <div v-else class="review-list">
        <el-card v-for="item in filteredList" :key="item.reviewId" class="review-card">
          <div class="card-content">
            <el-image
              :src="item.coverImage" fit="cover"
              style="width:64px;height:64px;border-radius:8px;flex-shrink:0"
            >
              <template #error><div class="img-placeholder" /></template>
            </el-image>

            <div class="card-info">
              <div class="info-row">
                <span class="pet-name">{{ item.name }}</span>
                <el-tag :type="item.myAction === 'APPROVED' ? 'success' : 'danger'" size="small" effect="plain">
                  {{ item.myAction === 'APPROVED' ? '通过' : '打回' }}
                </el-tag>
              </div>
              <div class="info-row meta">
                {{ item.categoryName }} · {{ item.age }} · {{ GENDER_MAP[item.gender] || item.gender }}
              </div>
              <div class="info-row time">
                {{ item.reviewType === 'FIRST' ? '初审' : '终审' }} · {{ formatTime(item.reviewedAt) }}
              </div>
              <div v-if="item.myAction === 'REJECTED' && item.myRemark" class="info-row reason">
                ❌ {{ item.myRemark }}
              </div>
            </div>

            <el-button class="detail-btn" size="small" @click="$router.push('/volunteer/review-history/' + item.petId)">
              查看详情
            </el-button>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import request from '@/api/request'
import { GENDER_MAP } from '@/utils/constants'

const router = useRouter()
const activeTab = ref('reviewed')
const list = ref([])
const loading = ref(true)
const filterAction = ref('')

const filteredList = computed(() => {
  if (!filterAction.value) return list.value
  return list.value.filter(item => item.myAction === filterAction.value)
})

async function loadList() {
  loading.value = true
  try {
    list.value = await request.get('/volunteer/pets/reviewed')
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 19).replace('T', ' ')
}

function onTabChange(tab) {
  if (tab === 'pending') router.push('/volunteer/pending')
  else if (tab === 'add') router.push('/volunteer/visits/add')
  else if (tab === 'visits') router.push('/volunteer/visits')
}

onMounted(loadList)
</script>

<style scoped>
.history-page { max-width: 800px; margin: 0 auto; padding: 24px 0 40px; min-height: calc(100vh - 180px); }
.loading-center { display: flex; justify-content: center; padding: 80px 0; }
.empty-tip { padding: 60px 0; }

.filter-bar { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.filter-count { font-size: 13px; color: var(--yc-text-tertiary); }

.review-list { display: flex; flex-direction: column; gap: 10px; }
.card-content { display: flex; align-items: center; gap: 14px; }
.img-placeholder { width: 64px; height: 64px; background: var(--yc-bg-card); border-radius: 8px; }

.card-info { flex: 1; min-width: 0; }
.info-row { display: flex; align-items: center; gap: 6px; margin-bottom: 2px; }
.info-row:last-child { margin-bottom: 0; }
.pet-name { font-size: 15px; font-weight: 500; color: var(--yc-text-primary); }
.meta { font-size: 13px; color: var(--yc-text-secondary); }
.time { font-size: 12px; color: var(--yc-text-tertiary); }
.reason { font-size: 12px; color: #E6A23C; }

:deep(.review-card) { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-card); background: var(--yc-bg-card); }
:deep(.el-tabs__active-bar) { background: var(--yc-accent); }
:deep(.el-tabs__item.is-active) { color: var(--yc-text-primary); }
:deep(.el-tabs__item:hover) { color: var(--yc-accent); }
:deep(.detail-btn) { border: 1px solid var(--yc-border); border-radius: var(--yc-radius-btn); color: var(--yc-text-primary); }
:deep(.detail-btn:hover) { border-color: var(--yc-border-hover); color: var(--yc-accent); }
</style>
