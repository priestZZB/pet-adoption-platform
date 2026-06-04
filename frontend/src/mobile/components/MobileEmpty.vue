<template>
  <div class="mobile-empty">
    <div class="empty-icon">
      <i :class="icon"></i>
    </div>
    <p class="empty-text">{{ description }}</p>
    <button v-if="actionLabel" class="empty-action" @click="$emit('action')">
      {{ actionLabel }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'fas fa-inbox' },
  description: { type: String, default: '暂无数据' },
  actionLabel: { type: String, default: '' },
})

defineEmits(['action'])
</script>

<style scoped>
.mobile-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}
.empty-icon {
  font-size: 48px;
  color: #d1e7dd;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 14px;
  color: #a09080;
  margin: 0 0 20px;
  line-height: 1.5;
}
.empty-action {
  height: 40px;
  padding: 0 24px;
  border: 1px solid #d1e7dd;
  border-radius: 10px;
  background: #fefaf5;
  color: #5a4a42;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}
.empty-action:active {
  background: #f0e8dc;
}
</style>
