<template>
  <div class="mobile-card" :class="{ clickable: !!$attrs.onClick || !!$attrs.clickable }" v-bind="$attrs">
    <div v-if="$slots.image" class="card-image">
      <slot name="image" />
    </div>
    <div class="card-body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
// 通用卡片容器
// 用法：
// <MobileCard>
//   <template #image><img src="..." /></template>
//   <h3>标题</h3>
//   <p>描述</p>
//   <template #footer><button>操作</button></template>
// </MobileCard>
</script>

<style scoped>
.mobile-card {
  background: rgba(254, 250, 245, 0.97);
  border: 1px solid rgba(210, 195, 175, 0.4);
  border-radius: 16px;
  overflow: hidden;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 8px 24px rgba(0, 0, 0, 0.06);
}
.mobile-card.clickable {
  cursor: pointer;
  transition: transform 0.15s;
}
.mobile-card.clickable:active {
  transform: scale(0.98);
}
.card-image {
  width: 100%;
  overflow: hidden;
}
.card-image :deep(img) {
  width: 100%;
  display: block;
}
.card-body {
  padding: 14px 16px;
}
.card-body :deep(h3) {
  font-size: 16px;
  font-weight: 600;
  color: #5a4a42;
  margin: 0 0 4px;
}
.card-body :deep(p) {
  font-size: 13px;
  color: #a09080;
  margin: 0;
  line-height: 1.5;
}
.card-footer {
  padding: 0 16px 14px;
  display: flex;
  gap: 8px;
}
</style>
