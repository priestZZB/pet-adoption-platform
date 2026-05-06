<template>
  <div class="admin-page">
    <h3 class="page-title">AI对话记录</h3>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="70" />
        <el-table-column prop="question" label="用户提问" min-width="200" show-overflow-tooltip />
        <el-table-column prop="answer" label="AI回答" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="提问时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAIRecords } from '@/api/admin'

const list = ref([])
const loading = ref(false)

async function loadList() {
  loading.value = true
  try { list.value = await getAIRecords() }
  catch { list.value = [] }
  finally { loading.value = false }
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 1000px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
</style>
