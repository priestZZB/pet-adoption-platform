<template>
  <div class="favorites-page">
    <div class="page-header">
      <h3 class="page-title">我的收藏</h3>
      <el-button size="small" type="primary" plain @click="showCreateDialog = true">
        + 新建收藏夹
      </el-button>
    </div>

    <div class="folder-bar" v-if="!loading">
      <div class="folder-tabs">
        <div v-for="f in folderList" :key="f.id" class="folder-tab" :class="{ active: currentFolderId === f.id }">
          <span class="folder-tab-label" @click="switchFolder(f.id)">
            <span class="folder-name">{{ f.name }}</span>
            <span class="folder-count">{{ countFor(f.id) }}</span>
          </span>
          <el-dropdown v-if="f.id !== -1" trigger="click" @command="(cmd) => handleFolderCmd(cmd, f)">
            <span class="folder-more" @click.stop>...</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除收藏夹</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <div v-if="!loading && list.length === 0" class="empty-tip">
      <el-empty :description="currentFolderId === -1 ? '还没有收藏的宠物' : '这个收藏夹还没有宠物'" />
    </div>

    <div v-if="!loading && list.length > 0" class="pet-grid">
      <el-card v-for="pet in list" :key="pet.id" :body-style="{ padding: '0' }" shadow="hover" class="pet-card">
        <el-dropdown trigger="click" class="card-menu" @command="(cmd) => handleCardCmd(cmd, pet)">
          <span class="card-menu-btn" @click.stop>...</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="move" v-if="currentFolderId !== -1">移出此收藏夹</el-dropdown-item>
              <el-dropdown-item command="move-to">移至收藏夹 ></el-dropdown-item>
              <el-dropdown-item command="unfav" divided>取消收藏</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div class="pet-cover" @click="goDetail(pet.id)">
          <el-image :src="pet.coverImage" fit="cover" style="width:100%;height:200px">
            <template #error><div class="img-placeholder">暂无图片</div></template>
          </el-image>
          <el-tag :type="PET_STATUS[pet.status]?.type || 'info'" size="small" class="status-tag">
            {{ PET_STATUS[pet.status]?.label || pet.status }}
          </el-tag>
        </div>
        <div class="pet-info">
          <h4 class="pet-name">{{ pet.name }}</h4>
          <div class="pet-meta">
            <span>{{ pet.categoryName }}</span><span class="dot">.</span>
            <span>{{ pet.age }}</span><span class="dot">.</span>
            <span>{{ GENDER_MAP[pet.gender] || pet.gender }}</span>
          </div>
        </div>
      </el-card>

      <el-card v-if="currentFolderId !== -1" :body-style="{ padding: '0' }" shadow="hover" class="pet-card add-card" @click="openBatchAdd(currentFolderId)">
        <div class="add-card-inner">
          <span class="add-icon">+</span>
          <span class="add-text">批量加入</span>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showCreateDialog" title="新建收藏夹" width="360px" :close-on-click-modal="false">
      <el-input v-model="newFolderName" placeholder="输入收藏夹名称" maxlength="20" show-word-limit />
      <div style="text-align:right;padding-top:16px;border-top:1px solid #eee;margin-top:16px">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="showRenameDialog" title="重命名收藏夹" width="360px" :close-on-click-modal="false">
      <el-input v-model="renameName" placeholder="输入新名称" maxlength="20" show-word-limit />
      <div style="text-align:right;padding-top:16px;border-top:1px solid #eee;margin-top:16px">
        <el-button @click="showRenameDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRename">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="showMoveDialog" title="移至收藏夹" width="360px" :close-on-click-modal="false">
      <div class="folder-pick-list">
        <div v-for="f in folders" :key="f.id" class="folder-pick-item" :class="{ active: moveTargetFolder === f.id }" @click="moveTargetFolder = f.id">
          <span>{{ f.name }}</span>
        </div>
        <div class="folder-pick-item" :class="{ active: moveTargetFolder === -1 }" @click="moveTargetFolder = -1">
          <span>不放入收藏夹</span>
        </div>
      </div>
      <div style="text-align:right;padding-top:16px;border-top:1px solid #eee;margin-top:16px">
        <el-button @click="showMoveDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmMove">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="showBatchDialog" :title="'加入收藏夹 - ' + batchFolderName" width="480px" :close-on-click-modal="false">
      <p style="margin:0 0 12px;font-size:13px;color:#999">选择要加入的宠物：</p>
      <div v-if="batchCandidates.length === 0" class="empty-tip"><el-empty description="没有可加入的宠物" /></div>
      <div v-else class="batch-list">
        <el-checkbox-group v-model="batchSelected">
          <div v-for="pet in batchCandidates" :key="pet.id" class="batch-item">
            <el-checkbox :label="pet.id">
              <span>{{ pet.name }}</span>
              <span class="batch-meta">{{ pet.categoryName }} - {{ pet.age }}</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <div style="text-align:right;padding-top:16px;border-top:1px solid #eee;margin-top:16px">
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button type="primary" :disabled="batchSelected.length === 0" @click="confirmBatch">加入 ({{ batchSelected.length }})</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getFavorites, getFavoritesByFolder, unfavorite, moveFavorite, getFavoritesWithCounts, getFolders, createFolder, renameFolder, deleteFolder } from '@/api/pet'
import { GENDER_MAP, PET_STATUS } from '@/utils/constants'

const router = useRouter()
const list = ref([])
const loading = ref(true)
const folders = ref([])
const currentFolderId = ref(-1)
const folderCounts = ref({})
const uncounted = ref(0)
let loadSerial = 0

const folderList = computed(() => [{ id: -1, name: '全部' }, ...folders.value])

const showCreateDialog = ref(false)
const newFolderName = ref('')
const showRenameDialog = ref(false)
const renameTarget = ref(null)
const renameName = ref('')
const showMoveDialog = ref(false)
const moveTarget = ref(null)
const moveTargetFolder = ref(-1)
const showBatchDialog = ref(false)
const batchFolderId = ref(null)
const batchFolderName = ref('')
const batchCandidates = ref([])
const batchSelected = ref([])

function countFor(fid) {
  if (fid === -1) {
    if (currentFolderId.value === -1) return list.value.length
    // Not on all view: sum all folder counts
    const vals = Object.values(folderCounts.value)
    return vals.reduce((a, b) => a + b, 0) + uncounted.value
  }
  if (currentFolderId.value === fid) return list.value.length
  const v = folderCounts.value[fid]
  return v || 0
}

async function loadFolders() {
  try { folders.value = await getFolders() } catch { folders.value = [] }
}

async function refreshAll() {
  const serial = ++loadSerial
  loading.value = true
  try {
    const data = await getFavoritesWithCounts()
    if (serial !== loadSerial) return
    list.value = data.list || []
    folderCounts.value = data.counts || {}
    // Items without folder
    const all = data.list || []
    const inFolders = Object.values(data.counts || {}).reduce((a, b) => a + b, 0)
    uncounted.value = all.length - inFolders
  } catch {
    if (serial === loadSerial) { list.value = []; folderCounts.value = {} }
  } finally {
    if (serial === loadSerial) loading.value = false
  }
}

async function loadByFolder(folderId) {
  const serial = ++loadSerial
  loading.value = true
  try {
    const data = await getFavoritesByFolder(folderId)
    if (serial === loadSerial) list.value = data
  } catch {
    if (serial === loadSerial) list.value = []
  } finally {
    if (serial === loadSerial) loading.value = false
  }
}

function switchFolder(folderId) {
  currentFolderId.value = folderId
  if (folderId === -1) refreshAll()
  else loadByFolder(folderId)
}

async function handleCardCmd(cmd, pet) {
  if (cmd === 'unfav') {
    try {
      await unfavorite(pet.id)
      list.value = list.value.filter(p => p.id !== pet.id)
      ElMessage.success('已取消收藏')
    } catch {}
  } else if (cmd === 'move') {
    try {
      await moveFavorite(pet.id, null)
      list.value = list.value.filter(p => p.id !== pet.id)
      ElMessage.success('已移出收藏夹')
    } catch {}
  } else if (cmd === 'move-to') {
    moveTarget.value = pet
    moveTargetFolder.value = -1
    showMoveDialog.value = true
  }
}

async function confirmMove() {
  if (!moveTarget.value) return
  const fid = moveTargetFolder.value === -1 ? null : moveTargetFolder.value
  try {
    await moveFavorite(moveTarget.value.id, fid)
    list.value = list.value.filter(p => p.id !== moveTarget.value.id)
    ElMessage.success('已移动')
    showMoveDialog.value = false
    moveTarget.value = null
  } catch {}
}

async function handleCreate() {
  const name = newFolderName.value.trim()
  if (!name) { ElMessage.warning('请输入名称'); return }
  try {
    await createFolder(name)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    newFolderName.value = ''
    await loadFolders()
  } catch {}
}

function handleFolderCmd(cmd, folder) {
  if (cmd === 'rename') {
    renameTarget.value = folder
    renameName.value = folder.name
    showRenameDialog.value = true
  } else if (cmd === 'delete') {
    ElMessageBox.confirm('确定删除【' + folder.name + '】？收藏的宠物不会被删除', '提示')
      .then(async () => {
        await deleteFolder(folder.id)
        ElMessage.success('已删除')
        folders.value = folders.value.filter(f => f.id !== folder.id)
        if (currentFolderId.value === folder.id) {
          currentFolderId.value = -1
          refreshAll()
        }
      })
      .catch(() => {})
  }
}

async function handleRename() {
  const name = renameName.value.trim()
  if (!name) { ElMessage.warning('请输入名称'); return }
  try {
    await renameFolder(renameTarget.value.id, name)
    ElMessage.success('重命名成功')
    showRenameDialog.value = false
    await loadFolders()
  } catch {}
}

function openBatchAdd(folderId) {
  batchFolderId.value = folderId
  const folder = folders.value.find(f => f.id === folderId)
  batchFolderName.value = folder ? folder.name : ''
  batchSelected.value = []
  showBatchDialog.value = true
  loadBatchCandidates(folderId)
}

async function loadBatchCandidates(folderId) {
  try {
    const all = await getFavorites()
    const inFolder = await getFavoritesByFolder(folderId)
    const inIds = new Set(inFolder.map(p => p.id))
    batchCandidates.value = all.filter(p => !inIds.has(p.id))
  } catch { batchCandidates.value = [] }
}

async function confirmBatch() {
  if (batchSelected.value.length === 0) return
  try {
    for (const petId of batchSelected.value) {
      await moveFavorite(petId, batchFolderId.value)
    }
    ElMessage.success('已将 ' + batchSelected.value.length + ' 个宠物加入收藏夹')
    showBatchDialog.value = false
    await loadFolders()
    if (currentFolderId.value === -1) refreshAll()
    else loadByFolder(currentFolderId.value)
  } catch {}
}

function goDetail(id) { router.push('/pets/' + id) }

onMounted(async () => { await loadFolders(); await refreshAll() })
</script>

<style scoped>
.favorites-page { max-width: 800px; margin: 0 auto; padding: 24px 20px 40px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { font-size: 20px; margin: 0; }
.loading-center { display: flex; justify-content: center; padding: 80px 0; }
.empty-tip { padding: 60px 0; }
.folder-bar { margin-bottom: 20px; }
.folder-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.folder-tab {
  display: flex; align-items: center;
  padding: 6px 10px 6px 14px; border-radius: 20px;
  background: #f5f5f5; border: 1px solid #e8e8e8;
  cursor: pointer; font-size: 13px; white-space: nowrap;
}
.folder-tab:hover { border-color: #409eff; color: #409eff; }
.folder-tab.active { background: #409eff; color: #fff; border-color: #409eff; }
.folder-tab-label { display: flex; align-items: center; gap: 6px; }
.folder-count { font-size: 11px; background: rgba(0,0,0,0.08); padding: 1px 6px; border-radius: 10px; }
.folder-tab.active .folder-count { background: rgba(255,255,255,0.2); }
.folder-more { font-size: 14px; font-weight: bold; padding: 0 4px; cursor: pointer; opacity: 0.4; }
.folder-more:hover { opacity: 1; }
.pet-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.pet-card { position: relative; border: 1px solid #e8e8e8; border-radius: 8px; overflow: hidden; }
.pet-card:hover { transform: translateY(-4px); }
.pet-cover { position: relative; cursor: pointer; }
.status-tag { position: absolute; top: 8px; left: 8px; }
.img-placeholder { width: 100%; height: 200px; display: flex; align-items: center; justify-content: center; background: #fafafa; color: #999; }
.pet-info { padding: 12px 16px; }
.pet-name { margin: 0 0 6px; font-size: 16px; }
.pet-meta { font-size: 13px; color: #666; }
.dot { margin: 0 4px; }
.card-menu { position: absolute; top: 6px; right: 6px; z-index: 10; }
.card-menu-btn {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 50%;
  background: rgba(0,0,0,0.35); color: #fff;
  font-size: 16px; cursor: pointer; opacity: 0;
}
.pet-card:hover .card-menu-btn { opacity: 1; }
.add-card { cursor: pointer; }
.add-card-inner {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 200px; color: #999; gap: 8px;
}
.add-icon { font-size: 40px; font-weight: 200; }
.add-text { font-size: 14px; }
.folder-pick-list { display: flex; flex-direction: column; gap: 8px; }
.folder-pick-item { padding: 10px 14px; border: 1px solid #e8e8e8; border-radius: 8px; cursor: pointer; }
.folder-pick-item.active { border-color: #409eff; background: #ecf5ff; color: #409eff; }
.batch-list { max-height: 300px; overflow-y: auto; }
.batch-item { padding: 8px 0; }
.batch-meta { font-size: 12px; color: #999; margin-left: 8px; }

/* ====== 响应式适配 ====== */
@media (max-width: 767px) {
  .favorites-page {
    padding: 12px 12px 40px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 639px) {
  .pet-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  .pet-card:hover {
    transform: none;
  }
}

@media (max-width: 420px) {
  .pet-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}
</style>
