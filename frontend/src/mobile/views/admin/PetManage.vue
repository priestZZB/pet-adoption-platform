<template>
  <div class="page">
    <div class="search-bar"><el-input v-model="keyword" placeholder="搜索宠物" clearable @keyup.enter="search" @clear="search" /></div>
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无宠物" />
    <div v-else class="list">
      <MobileCard v-for="p in list" :key="p.id" class="item">
        <div class="pet-row">
          <img :src="p.coverImage" class="pet-img" />
          <div class="info"><div class="name">{{ p.name }}</div><div class="meta">{{ p.breed }} · {{ p.age }} · {{ p.status }}</div></div>
        </div>
        <div class="acts">
          <button v-if="p.status === '待审核'" @click="review(p)">审核</button>
          <button @click="toggleStatus(p)">{{ p.status === '已上架' ? '下架' : '上架' }}</button>
        </div>
      </MobileCard>
    </div>
    <div class="more" v-if="list.length < total"><button @click="loadMore">加载更多</button></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAllPets, updatePetStatus } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const list = ref([]); const total = ref(0); const loading = ref(true); const keyword = ref(''); const page = ref(1)

async function loadData(reset = false) {
  if (reset) { page.value = 1; loading.value = true }
  try { const res = await getAllPets({ page: page.value, size: 10, keyword: keyword.value }); if (reset) list.value = res.records || []; else list.value.push(...(res.records || [])); total.value = res.total || 0 } catch {} finally { loading.value = false }
}
function search() { loadData(true) }
async function loadMore() { page.value++; await loadData(false) }
function review(p) { router.push('/admin/review/' + p.id) }
async function toggleStatus(p) {
  const newStatus = p.status === '已上架' ? 'SHELVED' : 'LISTED'
  try { await updatePetStatus(p.id, newStatus); ElMessage.success('已更新'); loadData(true) } catch {}
}
onMounted(() => loadData(true))
</script>

<style scoped>
.page { padding: 12px; }
.search-bar { margin-bottom: 10px; }
.search-bar :deep(.el-input__wrapper) { height: 40px; border-radius: 8px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.pet-row { display: flex; gap: 10px; align-items: center; margin-bottom: 8px; }
.pet-img { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; }
.name { font-size: 15px; font-weight: 600; color: #303133; }
.meta { font-size: 12px; color: #909399; }
.acts { display: flex; gap: 8px; }
.acts button { height: 28px; padding: 0 12px; border: 1px solid #dcdfe6; border-radius: 6px; background: #fff; font-size: 12px; cursor: pointer; }
.more { text-align: center; padding: 16px 0; }
.more button { height: 36px; padding: 0 24px; border: 1px solid #dcdfe6; border-radius: 8px; background: #fff; color: #606266; cursor: pointer; }
</style>
