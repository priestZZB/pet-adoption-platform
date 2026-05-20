<template>
  <div class="conv-list-wrapper">
    <div v-if="loading" class="conv-loading">
      <el-icon class="is-loading" :size="size === 'small' ? 20 : 32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="items.length === 0" class="conv-empty">{{ emptyText }}</div>

      <div v-else class="conv-list" :class="`conv-list--${size}`">
        <div
          v-for="item in items"
          :key="item.convId || item.id"
          class="conv-item"
          :class="{
            'conv-active': activeId && item.petId === activeId.petId && item.otherId === activeId.otherId,
            'conv-item--small': size === 'small'
          }"
          @click="handleSelect(item)"
        >
          <el-avatar :size="avatarSize" :src="item.otherAvatar" class="conv-avatar">
            {{ getOtherName(item)[0] }}
          </el-avatar>
          <div class="conv-body">
            <div class="conv-header">
              <span class="conv-name">{{ getOtherName(item) }}</span>
              <span class="conv-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div class="conv-pet">关于：{{ item.petName }}</div>
            <div class="conv-preview">{{ item.content }}</div>
          </div>
          <div v-if="isUnread(item)" class="conv-unread-dot"></div>
          <el-button
            v-if="showDelete"
            size="small"
            class="conv-delete-btn"
            :icon="Close"
            circle
            @click.stop="handleDelete(item)"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Close } from '@element-plus/icons-vue'
import { getConversations, deleteConversation } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { useChatSSE } from '@/composables/useChatSSE'

const props = defineProps({
  size: { type: String, default: 'normal' },       // 'normal' | 'small'
  emptyText: { type: String, default: '暂无消息' },
  activeId: { type: Object, default: null },         // { petId, otherId }
  autoLoad: { type: Boolean, default: true },        // 是否自动加载会话列表
  showDelete: { type: Boolean, default: false }       // 是否显示删除按钮
})

const emit = defineEmits(['select'])

const userStore = useUserStore()
const items = ref([])
const loading = ref(false)

const avatarSize = props.size === 'small' ? 40 : 44

async function loadConversations() {
  loading.value = true
  try {
    const data = await getConversations()
    const enriched = await Promise.all((data || []).map(async (item) => {
      const myId = userStore.userInfo?.id
      const otherId = item.senderId === myId ? item.receiverId : item.senderId
      if (!item.petName) {
        try {
          const pet = await request.get('/pets/' + item.petId)
          item.petName = pet?.name || '宠物#' + item.petId
        } catch { item.petName = '宠物#' + item.petId }
      }
      if (!item.otherName) {
        try {
          const user = await request.get('/user/public/' + otherId)
          item.otherName = user?.nickname || user?.username || '用户#' + otherId
          item.otherAvatar = user?.avatar || ''
        } catch { item.otherName = '用户#' + otherId; item.otherAvatar = '' }
      }
      item.otherId = otherId
      return item
    }))
    items.value = enriched
  } catch { items.value = [] }
  finally { loading.value = false }
}

function getOtherName(item) {
  return item.otherName || '用户'
}

function isUnread(item) {
  return item.receiverId === userStore.userInfo?.id && item.isRead === 0
}

function handleSelect(item) {
  emit('select', item)
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm('确定删除与该用户的对话记录？此操作不可恢复', '提示')
    await deleteConversation({ petId: item.petId, otherUserId: item.otherId })
    ElMessage.success('对话已删除')
    loadConversations()
  } catch {}
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}

// SSE 实时更新
const { connect: sseConnect, disconnect: sseDisconnect } = useChatSSE()

onMounted(() => {
  if (props.autoLoad) loadConversations()
  sseConnect({
    onConversationUpdate() { loadConversations() },
    onNewMessage() { loadConversations() }
  })
})

onBeforeUnmount(() => {
  sseDisconnect()
})

// 暴露刷新方法给父组件
defineExpose({ loadConversations })
</script>

<style scoped>
.conv-list-wrapper { height: 100%; display: flex; flex-direction: column; }
.conv-loading, .conv-empty {
  display: flex; justify-content: center; align-items: center;
  padding: 40px 0; color: var(--yc-text-tertiary); font-size: 13px;
}
.conv-list { display: flex; flex-direction: column; overflow-y: auto; flex: 1; }

.conv-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 16px; cursor: pointer; position: relative;
  border-bottom: 1px solid var(--yc-border);
  transition: background 0.15s;
}
.conv-item:hover { background: var(--yc-bg-page); }
.conv-item.conv-active { background: var(--yc-bg-page); border-left: 3px solid var(--yc-accent); }
.conv-item--small { padding: 10px 14px; }

.conv-avatar { flex-shrink: 0; }
.conv-body { flex: 1; min-width: 0; }
.conv-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2px; }
.conv-name { font-size: 14px; color: var(--yc-text-primary); font-weight: 500; }
.conv-time { font-size: 11px; color: var(--yc-text-tertiary); }
.conv-pet { font-size: 12px; color: var(--yc-accent); margin-bottom: 2px; }
.conv-preview {
  font-size: 13px; color: var(--yc-text-secondary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.conv-unread-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #F56C6C; flex-shrink: 0;
}
.conv-delete-btn {
  flex-shrink: 0;
  width: 24px; height: 24px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.15s;
  border: none;
  color: #909399;
}
.conv-item:hover .conv-delete-btn {
  opacity: 1;
}
.conv-delete-btn:hover {
  color: #F56C6C;
  background: rgba(245,108,108,0.1);
}
</style>
