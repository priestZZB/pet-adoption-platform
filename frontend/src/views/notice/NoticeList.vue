<template>
  <div class="notice-page">
    <h3 class="page-title">平台公告</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无公告" />
      </div>

      <div v-else class="notice-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="notice-item"
          @click="$router.push('/notices/' + item.id)"
        >
          <div class="notice-content">
            <h4 class="notice-title">{{ item.title }}</h4>
            <p class="notice-summary">{{ item.content }}</p>
          </div>
          <span class="notice-time">{{ item.createdAt }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getNoticeList } from '@/api/notice'

const list = ref([])
const loading = ref(true)

async function loadList() {
  loading.value = true
  try {
    list.value = await getNoticeList()
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.notice-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  cursor: pointer;
  transition: all 0.2s;
  gap: 20px;
  background: var(--yc-bg-card);
}
.notice-item:hover {
  border-color: var(--yc-accent);
  background: var(--yc-bg-page);
}
.notice-content {
  flex: 1;
  min-width: 0;
}
.notice-title {
  margin: 0 0 6px;
  font-size: 16px;
  color: var(--yc-text-primary);
}
.notice-summary {
  margin: 0;
  font-size: 14px;
  color: var(--yc-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
</style>
