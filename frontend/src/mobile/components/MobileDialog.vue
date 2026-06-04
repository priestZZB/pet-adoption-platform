<template>
  <teleport to="body">
    <div v-if="modelValue" class="mobile-dialog-wrapper" @click.self="handleClose">
      <div class="mobile-dialog" :class="{ 'dialog-full': fullscreen }">
        <div v-if="!hideHeader" class="dialog-header">
          <h3 class="dialog-title">{{ title }}</h3>
          <button class="dialog-close" @click="handleClose">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="dialog-body">
          <slot />
        </div>
        <div v-if="$slots.footer" class="dialog-footer">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  fullscreen: { type: Boolean, default: false },
  hideHeader: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'close'])

function handleClose() {
  emit('update:modelValue', false)
  emit('close')
}
</script>

<style scoped>
.mobile-dialog-wrapper {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
}
.mobile-dialog {
  width: 100%;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  background: #fefaf5;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
  animation: slideUp 0.25s ease-out;
}
.mobile-dialog.dialog-full {
  max-height: 100vh;
  border-radius: 0;
  height: 100vh;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.dialog-header {
  display: flex;
  align-items: center;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #ece4d8;
  flex-shrink: 0;
}
.dialog-title {
  flex: 1;
  font-size: 17px;
  font-weight: 600;
  color: #5a4a42;
  margin: 0;
}
.dialog-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: #a09080;
  font-size: 16px;
  cursor: pointer;
  border-radius: 8px;
  flex-shrink: 0;
}
.dialog-close:active {
  background: rgba(0, 0, 0, 0.04);
}
.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.dialog-footer {
  padding: 12px 16px 16px;
  border-top: 1px solid #ece4d8;
  flex-shrink: 0;
}
</style>
