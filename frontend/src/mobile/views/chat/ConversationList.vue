<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" icon="fas fa-comments" description="暂无消息" />
    <div v-else class="list">
      <MobileCard v-for="c in list" :key="c.id" class="item" clickable @click="$router.push('/user/chat?targetId=' + (c.otherId || c.targetId))">
        <div class="conv-row">
          <div class="conv-avatar"><i class="fas fa-user-circle"></i></div>
          <div class="conv-info">
            <span class="conv-name">{{ c.otherName || c.targetName || '用户' }}</span>
            <span class="conv-msg">{{ c.lastMessage || '' }}</span>
          </div>
          <span v-if="c.unread > 0" class="badge">{{ c.unread }}</span>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getConversations } from '@/api/chat'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const list = ref([]); const loading = ref(true)
onMounted(async () => { try { list.value = await getConversations() } catch {} finally { loading.value = false } })
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.conv-row { display: flex; gap: 10px; align-items: center; }
.conv-avatar { width: 44px; height: 44px; border-radius: 50%; background: #f0e8dc; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #b5a898; flex-shrink: 0; }
.conv-info { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.conv-name { font-size: 15px; font-weight: 600; color: #5a4a42; }
.conv-msg { font-size: 13px; color: #a09080; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.badge { min-width: 18px; height: 18px; padding: 0 6px; border-radius: 9px; background: #e8564a; color: #fff; font-size: 11px; display: flex; align-items: center; justify-content: center; }
</style>
