<template>
  <div class="empty-state">
    <el-empty
      :image-size="imageSize"
      :description="description"
    >
      <template v-if="$slots.default" #default>
        <slot />
      </template>
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  imageSize: { type: Number, default: 80 },
  description: { type: String, default: '暂无数据' }
})
</script>

<style scoped>
.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 统一 el-empty 样式，与平台色调一致 */
:deep(.el-empty__description) {
  color: #909399;
  font-size: 14px;
  margin-top: 12px;
}
:deep(.el-empty__image) {
  opacity: 0.6;
}
</style>
