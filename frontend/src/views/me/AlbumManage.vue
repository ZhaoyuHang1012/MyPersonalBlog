<template>
  <div>
    <!-- 顶部按钮：新建弹窗 -->
    <div class="toolbar">
      <div class="spacer"></div>
      <el-button type="primary" @click="createVisible = true">➕ 新建相册</el-button>
    </div>

    <!-- 新建相册弹窗 -->
    <el-dialog v-model="createVisible" title="新建相册" width="420px">
      <el-form label-width="70px">
        <el-form-item label="名称">
          <el-input v-model="newName" maxlength="50" placeholder="如：我的旅行" />
        </el-form-item>
        <el-form-item label="权限">
          <el-radio-group v-model="newVisibility">
            <el-radio :value="1">公共</el-radio>
            <el-radio :value="2">仅好友可见</el-radio>
            <el-radio :value="0">仅自己可见</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">创建</el-button>
      </template>
    </el-dialog>

    <!-- 我的相册列表 -->
    <div v-if="groups.length" class="my-album-grid">
      <div v-for="g in groups" :key="g.id" class="my-album-card">
        <VideoThumb v-if="isVideoUrl(g.cover)" :src="g.cover" class="my-album-cover-video" />
        <img v-else-if="g.cover" :src="g.cover" :alt="g.name" />
        <div v-else class="album-no-cover">📷</div>
        <div class="my-album-info">
          <div class="my-album-name">
            {{ g.name }}
            <el-tag v-if="g.visibility === 2" size="small" type="warning">仅好友可见</el-tag>
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

    <!-- 相册编辑弹窗（名称 + 权限） -->
    <el-dialog v-model="editVisible" title="编辑相册" width="420px">
      <el-form label-width="70px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="权限">
          <el-radio-group v-model="editForm.visibility">
            <el-radio :value="1">公共</el-radio>
            <el-radio :value="2">仅好友可见</el-radio>
            <el-radio :value="0">仅自己可见</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 相册照片管理弹窗 -->
    <el-dialog v-model="dialogVisible" :title="`管理相册：${currentGroup?.name || ''}`" width="720px">
      <div class="album-manage-upload">
        <el-upload
          v-model:file-list="uploadList"
          multiple
          :http-request="doUploadPhoto"
          accept="image/*,video/mp4,video/webm,video/quicktime"
          @success="onUploadDone"
        >
          <el-button type="primary" size="small">📤 上传照片 / 视频（可多选）</el-button>
        </el-upload>
        <span class="tip">图片 ≤500MB，视频 ≤5GB，可一次选择多个文件</span>
      </div>
      <div v-if="photos.length" class="album-manage-grid">
        <div v-for="p in photos" :key="p.id" class="album-manage-item">
          <VideoThumb v-if="p.mediaType === 'video'" :src="p.url" />
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
import VideoThumb from '../../components/common/VideoThumb.vue'
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
const createVisible = ref(false)

const dialogVisible = ref(false)
const currentGroup = ref(null)
const photos = ref([])
const editVisible = ref(false)
const editForm = ref({ id: null, name: '', visibility: 1 })
const uploadList = ref([])

const isVideoUrl = (url) => /\.(mp4|webm|mov|m4v|avi)(\?.*)?$/i.test(url || '')

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
    createVisible.value = false
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    creating.value = false
  }
}

const rename = (g) => {
  editForm.value = { id: g.id, name: g.name, visibility: g.visibility }
  editVisible.value = true
}

const saveEdit = async () => {
  if (!editForm.value.name.trim()) {
    ElMessage.warning('请输入相册名称')
    return
  }
  await adminUpdateAlbum(editForm.value.id, {
    name: editForm.value.name.trim(),
    visibility: editForm.value.visibility
  })
  ElMessage.success('已保存')
  editVisible.value = false
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

const doUploadPhoto = async ({ file, onProgress, onSuccess, onError }) => {
  try {
    // 上传并回报进度（el-upload 文件列表会显示进度条）
    const data = await uploadFile(file, (percent) => onProgress && onProgress({ percent }))
    await adminAddAlbumPhoto(currentGroup.value.id, {
      url: data.url,
      mediaType: data.mediaType,
      description: null
    })
    ElMessage.success(`「${file.name}」已添加到相册`)
    onSuccess()
    loadPhotos()
    load()
  } catch (e) {
    onError(e)
  }
}

const loadPhotos = async () => {
  const detail = await getAlbumDetail(currentGroup.value.id)
  photos.value = detail.photos
}

/** 每个文件上传成功都会触发：全部完成后清空上传列表 */
const onUploadDone = () => {
  if (uploadList.value.length && uploadList.value.every((f) => f.status === 'success')) {
    uploadList.value = []
  }
}

const openManage = async (g) => {
  currentGroup.value = g
  dialogVisible.value = true
  const detail = await getAlbumDetail(g.id)
  photos.value = detail.photos
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
