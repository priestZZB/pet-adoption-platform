<template>
  <div class="chat-page">
    <h3 class="page-title">💬 消息</h3>

    <div v-if="loading" class="loading-center"><el-icon class="is-loading" :size="32"><Loading /></el-icon></div>

    <template v-else>
      <div v-if="list.length === 0" class="empty-tip"><el-empty description="暂无消息" /></div>

      <div v-else class="conversation-list">
        <div v-for="item in list" :key="item.id" class="conv-item" @click="openChat(item)">
          <div class="conv-left">
            <el-avatar :size="44" :src="item.otherAvatar">{{ getOtherName(item)[0] }}</el-avatar>
          </div>
          <div class="conv-body">
            <div class="conv-header">
              <span class="conv-name">{{ getOtherName(item) }}</span>
              <span class="conv-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div class="conv-pet">关于：{{ item.petName }}</div>
            <div class="conv-preview">{{ item.content }}</div>
          </div>
          <div v-if="isUnread(item)" class="conv-unread-badge"></div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { getConversations } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const list = ref([])
const loading = ref(true)

async function loadList() {
  loading.value = true
  try {
    const data = await getConversations()
    const enriched = await Promise.all((data || []).map(async (item) => {
      const otherId = item.senderId === userStore.userInfo?.id ? item.receiverId : item.senderId
      try {
        const pet = await request.get('/pets/' + item.petId)
        item.petName = pet?.name || '宠物#' + item.petId
      } catch { item.petName = '宠物#' + item.petId }
      try {
        const user = await request.get('/user/public/' + otherId)
        item.otherName = user?.nickname || user?.username || '用户#' + otherId
        item.otherAvatar = user?.avatar || ''
      } catch { item.otherName = '用户#' + otherId; item.otherAvatar = '' }
      item.otherId = otherId
      return item
    }))
    list.value = enriched
  } catch { list.value = [] }
  finally { loading.value = false }
}

function getOtherName(item) {
  return item.otherName || '用户'
}

function isUnread(item) {
  return item.receiverId === userStore.userInfo?.id && item.isRead === 0
}

function openChat(item) {
  router.push('/user/chat?petId=' + item.petId + '&otherUserId=' + item.otherId + '&petName=' + encodeURIComponent(item.petName || '') + '&otherName=' + encodeURIComponent(item.otherName || ''))
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}

onMounted(loadList)
</script>

<style scoped>
.chat-page { max-width: 700px; margin: 0 auto; padding: 24px 0 40px; }
.page-title { font-size: 20px; color: var(--yc-text-primary); margin: 0 0 20px; }
.loading-center { display: flex; justify-content: center; padding: 80px 0; }
.empty-tip { padding: 60px 0; }

.conversation-list { display: flex; flex-direction: column; }
.conv-item {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 16px; cursor: pointer; position: relative;
  border-bottom: 1px solid var(--yc-border);
  background: var(--yc-bg-card);
}
.conv-item:hover { background: var(--yc-bg-page); }
.conv-item:first-child { border-radius: var(--yc-radius-card) var(--yc-radius-card) 0 0; }
.conv-item:last-child { border-radius: 0 0 var(--yc-radius-card) var(--yc-radius-card); border-bottom: none; }

.conv-body { flex: 1; min-width: 0; }
.conv-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2px; }
.conv-name { font-size: 15px; color: var(--yc-text-primary); font-weight: 500; }
.conv-time { font-size: 12px; color: var(--yc-text-tertiary); }
.conv-pet { font-size: 12px; color: var(--yc-accent); margin-bottom: 2px; }
.conv-preview { font-size: 13px; color: var(--yc-text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-unread-badge {
  width: 8px; height: 8px; border-radius: 50%;
  background: #F56C6C; flex-shrink: 0;
}
</style>
