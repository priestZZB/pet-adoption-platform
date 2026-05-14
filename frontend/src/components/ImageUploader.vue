<template>
  <div>
    <el-upload
      :action="action"
      :headers="headers"
      :max-count="maxCount"
      :file-list="fileList"
      list-type="picture-card"
      :on-success="handleSuccess"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible">
      <img :src="previewUrl" style="width:100%" />
    </el-dialog>

    <!-- 当前已上传URL列表 -->
    <div v-if="urls.length" style="margin-top:8px">
      <el-tag
        v-for="(url, i) in urls"
        :key="i"
        closable
        size="small"
        style="margin-right:6px;margin-bottom:4px"
        @close="removeUrl(i)"
      >
        {{ url.split('/').pop()?.slice(0, 20) || '图片' }}
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getToken } from '@/utils/auth'

const props = defineProps({
  action: { type: String, default: '/api/file/upload' },
  maxCount: { type: Number, default: 9 },
  fileList: { type: Array, default: () => [] }
})
const emit = defineEmits(['change'])

const urls = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const headers = computed(() => ({
  Authorization: 'Bearer ' + (getToken() || '')
}))

function handleSuccess(response, file) {
  if (response && response.data?.url) {
    file.url = response.data.url
    urls.value.push(response.data.url)
    emit('change', [...urls.value])
  }
}

function handleRemove() {
  emit('change', [...urls.value])
}

function handlePreview(file) {
  previewUrl.value = file.url
  previewVisible.value = true
}

function removeUrl(index) {
  urls.value.splice(index, 1)
  emit('change', [...urls.value])
}

// 初始化已有图片
if (props.fileList.length && urls.value.length === 0) {
  urls.value = props.fileList.map(f => (typeof f === 'string' ? f : f.url))
}
</script>
