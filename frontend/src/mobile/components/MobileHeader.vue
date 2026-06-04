<template>
  <header class="mobile-header">
    <button v-if="showBack" class="header-back" @click="handleBack">
      <i class="fas fa-chevron-left"></i>
    </button>
    <h1 class="header-title">{{ title }}</h1>
    <div class="header-actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps({
  title: { type: String, default: '有宠' },
})

const emit = defineEmits(['back'])

const router = useRouter()
const route = useRoute()

const showBack = computed(() => {
  return route.path !== '/'
})

function handleBack() {
  emit('back')
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.mobile-header {
  position: sticky;
  top: 0;
  z-index: 998;
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 16px;
  background: rgba(254, 250, 245, 0.96);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(209, 231, 221, 0.5);
  flex-shrink: 0;
}
.header-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  margin-right: 8px;
  border: none;
  background: none;
  color: #5a4a42;
  font-size: 18px;
  cursor: pointer;
  border-radius: 8px;
  flex-shrink: 0;
}
.header-back:active {
  background: rgba(0, 0, 0, 0.04);
}
.header-title {
  flex: 1;
  font-size: 17px;
  font-weight: 600;
  color: #5a4a42;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
</style>
