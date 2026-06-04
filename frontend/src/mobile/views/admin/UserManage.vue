<template>
  <div class="page">
    <div class="search-bar"><el-input v-model="keyword" placeholder="搜索用户" clearable @keyup.enter="search" @clear="search" /></div>
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无用户" />
    <div v-else class="list">
      <MobileCard v-for="u in list" :key="u.id" class="item">
        <div class="row"><span class="name">{{ u.nickname || u.username }}</span><span :class="['status', u.status === 1 ? 'on' : 'off']">{{ u.status === 1 ? '正常' : '禁用' }}</span></div>
        <div class="meta">{{ u.username }} | {{ u.phone || '-' }} | {{ u.roleNames?.join(', ') || '-' }}</div>
        <div class="acts"><button @click="toggleStatus(u)">{{ u.status === 1 ? '禁用' : '启用' }}</button></div>
      </MobileCard>
    </div>
    <div class="more" v-if="list.length < total"><button @click="loadMore">加载更多</button></div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, toggleUserStatus } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const list = ref([]); const total = ref(0); const loading = ref(true); const keyword = ref(''); const page = ref(1)

async function loadData(reset = false) {
  if (reset) { page.value = 1; loading.value = true }
  try { const res = await getUserList({ page: page.value, size: 10, keyword: keyword.value }); if (reset) list.value = res.records || []; else list.value.push(...(res.records || [])); total.value = res.total || 0 } catch {} finally { loading.value = false }
}
function search() { loadData(true) }
async function loadMore() { page.value++; await loadData(false) }
async function toggleStatus(u) {
  try { await toggleUserStatus(u.id); u.status = u.status === 1 ? 0 : 1; ElMessage.success('已更新') } catch {}
}

import { onMounted } from 'vue'
onMounted(() => loadData(true))
</script>

<style scoped>
.page { padding: 12px; }
.search-bar { margin-bottom: 10px; }
.search-bar :deep(.el-input__wrapper) { height: 40px; border-radius: 8px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.name { font-size: 15px; font-weight: 600; color: #303133; }
.status { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.status.on { background: rgba(103,194,58,0.1); color: #67c23a; }
.status.off { background: rgba(144,147,153,0.1); color: #909399; }
.meta { font-size: 12px; color: #909399; margin-bottom: 8px; }
.acts button { height: 28px; padding: 0 12px; border: 1px solid #dcdfe6; border-radius: 6px; background: #fff; font-size: 12px; cursor: pointer; }
.more { text-align: center; padding: 16px 0; }
.more button { height: 36px; padding: 0 24px; border: 1px solid #dcdfe6; border-radius: 8px; background: #fff; color: #606266; cursor: pointer; }
</style>
