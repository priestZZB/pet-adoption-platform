<template>
  <div class="chat-page">
    <h3 class="page-title">💬 消息</h3>
    <ConversationList
      size="normal"
      empty-text="暂无消息"
      :show-delete="true"
      @select="openChat"
    />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import ConversationList from './ConversationList.vue'

const router = useRouter()

function openChat(item) {
  router.push('/user/chat?petId=' + item.petId
    + '&otherUserId=' + item.otherId
    + '&petName=' + encodeURIComponent(item.petName || '')
    + '&otherName=' + encodeURIComponent(item.otherName || ''))
}
</script>

<style scoped>
.chat-page { max-width: 700px; margin: 0 auto; padding: 24px 0 40px; }
.page-title { font-size: 20px; color: var(--yc-text-primary); margin: 0 0 20px; }
</style>
