<template>
  <div>
    <!-- 创建相册 -->
    <div class="album-create">
      <el-input v-model="newName" placeholder="新相册名称，如：我的旅行" maxlength="50" style="width: 240px" />
      <el-radio-group v-model="newVisibility" size="small">
        <el-radio-button :value="1">公开</el-radio-button>
        <el-radio-button :value="0">仅自己可见</el-radio-button>
      </el-radio-group>
      <el-button type="primary" :loading="creating" @click="create">创建相册</el-button>
    </div>

    <!-- 我的相册列表 -->
    <div v-if="groups.length" class="my-album-grid">
      <div v-for="g in groups" :key="g.id" class="my-album-card">
        <img v-if="g.cover" :src="g.cover" :alt="g.name" />
        <div v-else class="album-no-cover">📷</div>
        <div class="my-album-info">
          <div class="my-album-name">
            {{ g.name }}
            <el-tag v-if="g.visibility === 0" size="small" type="info">私密</el-tag>
          </div>
          <div class="my-album-meta">{{ g.photoCount }} 个内容</div>
          <div class="my-album-actions">
            <el-button link type="primary" size="small" @click="openManage(g)">管理</el-button>
            <el-button link type="primary" size="small" @click="rename(g)">重命名</el-button>
            <el-button link type="danger" size="small" @click="removeGroup(g)">删除</el-button>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-else description="还没有相册，创建一个吧" />

    <!-- 相册照片管理弹窗 -->
    <el-dialog v-model="dialogVisible" :title="`管理相册：${currentGroup?.name || ''}`" width="720px">
      <div class="album-manage-upload">
        <el-upload
          :show-file-list="false"
          :http-request="doUploadPhoto"
          accept="image/*,video/mp4,video/webm,video/quicktime"
        >
          <el-button type="primary" size="small">上传照片 / 视频</el-button>
        </el-upload>
        <span class="tip">图片 ≤10MB，视频 ≤200MB</span>
      </div>
      <div v-if="photos.length" class="album-manage-grid">
        <div v-for="p in photos" :key="p.id" class="album-manage-item">
          <video v-if="p.mediaType === 'video'" :src="p.url" controls preload="metadata"></video>
          <el-image v-else :src="p.url" :preview-src-list="photos.filter(x => x.mediaType !== 'video').map(x => x.url)" fit="cover" preview-teleported lazy />
          <div class="album-manage-item-bar">
            <span class="media-badge">{{ p.mediaType === 'video' ? '🎬 视频' : '🖼 图片' }}</span>
            <el-button link type="danger" size="small" @click="removePhoto(p)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="相册还是空的，上传第一个内容吧" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminListAlbums,
  adminCreateAlbum,
  adminUpdateAlbum,
  adminDeleteAlbum,
  adminAddAlbumPhoto,
  adminDeleteAlbumPhoto,
  getAlbumDetail,
  uploadFile
} from '../../api'

const groups = ref([])
const newName = ref('')
const newVisibility = ref(1)
const creating = ref(false)

const dialogVisible = ref(false)
const currentGroup = ref(null)
const photos = ref([])

const load = async () => {
  groups.value = await adminListAlbums()
}

const create = async () => {
  if (!newName.value.trim()) {
    ElMessage.warning('请输入相册名称')
    return
  }
  creating.value = true
  try {
    await adminCreateAlbum({ name: newName.value.trim(), visibility: newVisibility.value })
    ElMessage.success('相册已创建')
    newName.value = ''
    newVisibility.value = 1
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    creating.value = false
  }
}

const rename = async (g) => {
  let value
  try {
    const r = await ElMessageBox.prompt('输入新的相册名称', '重命名', {
      inputValue: g.name,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    value = r.value
  } catch (e) {
    return
  }
  if (!value.trim()) return
  await adminUpdateAlbum(g.id, { name: value.trim(), visibility: g.visibility })
  ElMessage.success('已重命名')
  load()
}

const removeGroup = async (g) => {
  try {
    await ElMessageBox.confirm(`确定删除相册「${g.name}」及其全部内容记录吗？（不会删除上传的文件）`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteAlbum(g.id)
  ElMessage.success('已删除')
  load()
}

const openManage = async (g) => {
  currentGroup.value = g
  dialogVisible.value = true
  const detail = await getAlbumDetail(g.id)
  photos.value = detail.photos
}

const doUploadPhoto = async ({ file, onSuccess, onError }) => {
  try {
    const data = await uploadFile(file)
    await adminAddAlbumPhoto(currentGroup.value.id, {
      url: data.url,
      mediaType: data.mediaType,
      description: null
    })
    ElMessage.success('已添加到相册')
    onSuccess()
    const detail = await getAlbumDetail(currentGroup.value.id)
    photos.value = detail.photos
    load()
  } catch (e) {
    onError(e)
  }
}

const removePhoto = async (p) => {
  await adminDeleteAlbumPhoto(p.id)
  ElMessage.success('已移除')
  const detail = await getAlbumDetail(currentGroup.value.id)
  photos.value = detail.photos
  load()
}

onMounted(load)
</script>
