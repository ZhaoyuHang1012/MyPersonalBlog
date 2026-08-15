<template>
  <div>
    <h2 class="page-title">相册管理</h2>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="form.url" placeholder="图片地址（/uploads/...，可在媒体库复制）" style="width: 340px" />
        <el-input v-model="form.description" placeholder="描述（可选）" style="width: 220px" maxlength="200" />
        <el-button type="primary" :loading="saving" @click="add">添加图片</el-button>
        <span class="tip">先去「媒体库」上传图片并复制链接</span>
      </div>

      <div v-loading="loading" class="album-admin-grid">
        <div v-for="a in albums" :key="a.id" class="album-admin-item">
          <img :src="a.url" :alt="a.description || a.url" loading="lazy" />
          <div class="album-admin-info">
            <span class="album-admin-desc">{{ a.description || '（无描述）' }}</span>
            <el-button link type="danger" size="small" @click="remove(a)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="!albums.length && !loading" description="相册为空，添加第一张图片吧" style="grid-column: 1 / -1" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListAlbums, adminCreateAlbum, adminDeleteAlbum } from '../../api'

const albums = ref([])
const form = ref({ url: '', description: '' })
const loading = ref(false)
const saving = ref(false)

const load = async () => {
  loading.value = true
  try {
    albums.value = await adminListAlbums()
  } finally {
    loading.value = false
  }
}

const add = async () => {
  if (!form.value.url.trim()) {
    ElMessage.warning('请填写图片地址')
    return
  }
  saving.value = true
  try {
    await adminCreateAlbum({
      url: form.value.url.trim(),
      description: form.value.description.trim() || null
    })
    ElMessage.success('添加成功')
    form.value = { url: '', description: '' }
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

const remove = async (a) => {
  try {
    await ElMessageBox.confirm('确定从相册移除该图片吗？（不会删除媒体库文件）', '删除确认', {
      type: 'warning',
      confirmButtonText: '移除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteAlbum(a.id)
  ElMessage.success('已移除')
  load()
}

onMounted(load)
</script>

<style scoped>
.tip {
  font-size: 13px;
  color: #909399;
}

.album-admin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
  margin-top: 16px;
  min-height: 100px;
}

.album-admin-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.album-admin-item img {
  width: 100%;
  height: 130px;
  object-fit: cover;
  display: block;
  background: #f3f4f6;
}

.album-admin-info {
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.album-admin-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
