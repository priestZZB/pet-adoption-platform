<template>
  <div class="visits-page">
    <el-tabs v-model="activeTab" @tab-change="onTabChange" class="page-tabs">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="审核历史" name="reviewed" />
      <el-tab-pane label="去走访" name="add" />
      <el-tab-pane label="走访记录" name="visits" />
    </el-tabs>

    <!-- 走访记录列表 -->
    <el-card class="list-card">
      <template #header><span>我的走访记录</span></template>

      <div v-if="listLoading" class="loading-center">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>

      <template v-else>
        <div v-if="visitList.length === 0" class="empty-tip">
          <el-empty description="暂无走访记录" :image-size="80" />
        </div>

        <div v-else class="visit-list">
          <div
            v-for="item in visitList"
            :key="item.id"
            class="visit-item"
            @click="showDetail(item)"
          >
            <div class="visit-header">
              <span class="visit-date">{{ item.visitDate }}</span>
              <span class="visit-pet" v-if="item.petId">宠物 #{{ item.petId }}</span>
              <span class="visit-time">{{ item.createdAt }}</span>
            </div>
            <p class="visit-content">{{ item.content }}</p>
            <div v-if="item.images && item.images.length > 0" class="visit-thumbs">
              <el-image
                v-for="(img, idx) in item.images.slice(0, 4)"
                :key="idx"
                :src="img"
                fit="cover"
                class="thumb"
              />
              <span v-if="item.images.length > 4" class="more">
                +{{ item.images.length - 4 }}
              </span>
            </div>
          </div>
        </div>

        <Pagination
          :total="total"
          :page="page"
          :size="size"
          @change="onPageChange"
        />
      </template>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="走访记录详情" width="500px">
      <div v-if="currentVisit" class="detail-body">
        <div class="detail-row">
          <span class="label">走访日期</span>
          <span>{{ currentVisit.visitDate }}</span>
        </div>
        <div v-if="currentVisit.petId" class="detail-row">
          <span class="label">关联宠物</span>
          <span>宠物ID: {{ currentVisit.petId }}</span>
        </div>
        <div class="detail-row">
          <span class="label">走访内容</span>
          <p>{{ currentVisit.content }}</p>
        </div>
        <div v-if="currentVisit.images && currentVisit.images.length > 0" class="detail-images">
          <span class="label">现场图片</span>
          <div class="img-list">
            <el-image
              v-for="(img, idx) in currentVisit.images"
              :key="idx"
              :src="img"
              fit="cover"
              class="detail-img"
              :preview-src-list="currentVisit.images"
              preview-teleported
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { getMyVisits, getVisitDetail } from '@/api/volunteer'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()
const activeTab = ref('visits')
const listLoading = ref(true)
const visitList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const detailVisible = ref(false)
const currentVisit = ref(null)

async function loadList() {
  listLoading.value = true
  try {
    const res = await getMyVisits({ page: page.value, size: size.value })
    visitList.value = res.list || []
    total.value = res.total || 0
  } catch {
    visitList.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

function onPageChange({ page: p, size: s }) {
  page.value = p
  size.value = s
  loadList()
}

async function showDetail(item) {
  try {
    currentVisit.value = await getVisitDetail(item.id)
    detailVisible.value = true
  } catch {
    // 请求拦截器统一处理
  }
}

function onTabChange(tab) {
  if (tab === 'pending') router.push('/volunteer/pending')
  else if (tab === 'reviewed') router.push('/volunteer/reviewed')
  else if (tab === 'add') router.push('/volunteer/visits/add')
}

onMounted(loadList)
</script>

<style scoped>
.visits-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 0 40px;
  min-height: calc(100vh - 180px);
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.empty-tip { padding: 20px 0; }

.visit-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.visit-item {
  padding: 14px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  cursor: pointer;
  transition: border-color 0.2s;
  background: var(--yc-bg-card);
}
.visit-item:hover { border-color: var(--yc-accent); }
.visit-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.visit-date {
  font-size: 14px;
  color: var(--yc-text-primary);
  font-weight: 500;
}
.visit-pet {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.visit-time { font-size: 12px; color: var(--yc-text-tertiary); }
.visit-content {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--yc-text-secondary);
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.visit-thumbs {
  display: flex;
  gap: 6px;
  align-items: center;
}
.thumb {
  width: 50px;
  height: 50px;
  border-radius: var(--yc-radius-tag);
}
.more { font-size: 12px; color: var(--yc-text-tertiary); }

/* 卡片暖色 */
:deep(.list-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

/* Tabs 暖色 */
:deep(.el-tabs__active-bar) {
  background: var(--yc-accent);
}
:deep(.el-tabs__item.is-active) {
  color: var(--yc-text-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--yc-accent);
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-row {
  display: flex;
  gap: 12px;
}
.detail-row .label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  color: var(--yc-text-tertiary);
}
.detail-row span,
.detail-row p {
  font-size: 14px;
  color: var(--yc-text-primary);
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}
.detail-images .label {
  display: block;
  font-size: 14px;
  color: var(--yc-text-tertiary);
  margin-bottom: 8px;
}
.img-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.detail-img {
  width: 100px;
  height: 100px;
  border-radius: var(--yc-radius-tag);
  cursor: pointer;
}
</style>
