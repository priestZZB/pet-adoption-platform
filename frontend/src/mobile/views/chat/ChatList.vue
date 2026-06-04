<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" icon="fas fa-envelope" description="暂无会话" />
    <div v-else class="list">
      <MobileCard v-for="c in list" :key="c.id" class="item" clickable @click="goChat(c)">
        <div class="conv-row">
          <div class="conv-avatar"><i class="fas fa-user-circle"></i></div>
          <div class="conv-info">
            <div class="conv-top">
              <span class="conv-name">{{ c.otherName || c.targetName || '用户' }}</span>
              <span class="conv-time">{{ c.lastTime?.slice(0, 10) || '' }}</span>
            </div>
            <div class="conv-bottom">
              <span class="conv-msg">{{ c.lastMessage || '' }}</span>
              <span v-if="c.unread > 0" class="conv-badge">{{ c.unread > 99 ? '99+' : c.unread }}</span>
            </div>
          </div>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getConversations } from '@/api/chat'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const list = ref([]); const loading = ref(true)

onMounted(async () => { try { list.value = await getConversations() } catch {} finally { loading.value = false } })

function goChat(c) {
  const targetId = c.otherId || c.targetId
  router.push('/user/chat?targetId=' + targetId + '&targetName=' + encodeURIComponent(c.otherName || c.targetName || '用户'))
}
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.conv-row { display: flex; gap: 10px; align-items: center; }
.conv-avatar { width: 44px; height: 44px; border-radius: 50%; background: #f0e8dc; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #b5a898; flex-shrink: 0; }
.conv-info { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.conv-name { font-size: 15px; font-weight: 600; color: #5a4a42; }
.conv-time { font-size: 11px; color: #b5a898; }
.conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-msg { font-size: 13px; color: #a09080; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-badge { min-width: 18px; height: 18px; padding: 0 6px; border-radius: 9px; background: #e8564a; color: #fff; font-size: 11px; display: flex; align-items: center; justify-content: center; }
</style>
