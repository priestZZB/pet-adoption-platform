<template>
  <el-pagination
    :current-page="current"
    :page-size="currentSize"
    :total="total"
    :page-sizes="[10, 20, 50]"
    layout="total, sizes, prev, pager, next, jumper"
    background
    style="margin-top:20px;justify-content:center"
    @current-change="onCurrentChange"
    @size-change="onSizeChange"
  />
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 }
})
const emit = defineEmits(['change'])

// 内部状态，同步 props
const current = ref(props.page)
const currentSize = ref(props.size)

watch(() => props.page, (v) => { current.value = v })
watch(() => props.size, (v) => { currentSize.value = v })

function onCurrentChange(val) {
  current.value = val
  emit('change', { page: val, size: currentSize.value })
}

function onSizeChange(val) {
  currentSize.value = val
  current.value = 1  // 切换每页条数时回到第一页
  emit('change', { page: 1, size: val })
}
</script>
