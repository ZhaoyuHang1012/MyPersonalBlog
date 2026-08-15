<template>
  <div>
    <h2 class="page-title">媒体库</h2>
    <el-card shadow="never">
      <div class="toolbar">
        <el-upload :show-file-list="false" :http-request="doUpload" accept="image/*,video/mp4,video/webm,video/quicktime">
          <el-button type="primary">上传文件</el-button>
        </el-upload>
        <el-select
          v-if="userStore.isAdmin"
          v-model="filterUser"
          clearable
          placeholder="全部用户"
          style="width: 200px"
          @change="reload"
        >
          <el-option
            v-for="u in users"
            :key="u.id"
            :label="`${u.nickname} (@${u.username})`"
            :value="u.username"
          />
        </el-select>
        <span class="upload-tip">图片 ≤100MB，视频 ≤2GB</span>
        <div class="spacer"></div>
        <el-button @click="load">刷新</el-button>
      </div>

      <div v-loading="loading" class="media-grid">
        <div v-for="(f, idx) in files" :key="f.name" class="media-item">
          <!-- 视频：直接内嵌播放器预览 -->
          <video
            v-if="f.mediaType === 'video'"
            class="media-thumb media-video"
            :src="f.url"
            controls
            preload="metadata"
          ></video>
          <!-- 图片：点击放大预览 -->
          <el-image
            v-else
            class="media-thumb"
            :src="f.url"
            :preview-src-list="previewList"
            :initial-index="imageIndex(f)"
            fit="cover"
            preview-teleported
            lazy
          />
          <div class="media-info">
            <span class="media-size">
              {{ f.mediaType === 'video' ? '🎬 ' : '🖼 ' }}{{ formatSize(f.size) }}
              <span v-if="userStore.isAdmin && !filterUser" class="media-owner">
                · {{ f.name.split('/')[0] }}
              </span>
            </span>
            <div class="media-actions">
              <el-button link type="primary" size="small" @click="copyUrl(f)">复制链接</el-button>
              <el-button link type="danger" size="small" @click="remove(f)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty
          v-if="!files.length && !loading"
          description="暂无文件，点击左上角「上传图片」"
          style="grid-column: 1 / -1"
        />
      </div>

      <div class="pager-box">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="size"
          v-model:current-page="page"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListFiles, adminDeleteFile, uploadFile, adminListUsers } from '../../api'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const users = ref([])
const filterUser = ref(null)
const files = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const loading = ref(false)

const previewList = computed(() =>
  files.value.filter((f) => f.mediaType !== 'video').map((f) => f.url)
)

// 图片在预览列表中的索引（视频不参与图片预览）
const imageIndex = (f) =>
  files.value.filter((x) => x.mediaType !== 'video').findIndex((x) => x.name === f.name)

const load = async () => {
  loading.value = true
  try {
    const data = await adminListFiles({
      page: page.value,
      size: size.value,
      username: filterUser.value || undefined
    })
    files.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  load()
}

const doUpload = async ({ file, onSuccess, onError }) => {
  try {
    await uploadFile(file)
    ElMessage.success('上传成功')
    onSuccess()
    load()
  } catch (e) {
    onError(e)
  }
}

const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

const copyUrl = async (f) => {
  const url = window.location.origin + f.url
  try {
    await navigator.clipboard.writeText(url)
  } catch (e) {
    const ta = document.createElement('textarea')
    ta.value = url
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
  ElMessage.success('链接已复制：' + url)
}

const remove = async (f) => {
  try {
    await ElMessageBox.confirm('确定删除该图片吗？已引用它的文章图片将失效。', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteFile(f.name)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  if (userStore.isAdmin) {
    adminListUsers().then((list) => (users.value = list)).catch(() => {})
  }
  load()
})
</script>

<style scoped>
.upload-tip {
  font-size: 13px;
  color: #909399;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
  min-height: 120px;
}

.media-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.media-thumb {
  width: 100%;
  height: 130px;
  display: block;
  cursor: zoom-in;
  background: #f3f4f6;
}

.media-video {
  cursor: pointer;
  background: #000;
  object-fit: contain;
}

.media-info {
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.media-size {
  font-size: 12px;
  color: #909399;
}

.media-owner {
  color: #3a7afe;
}

.media-actions {
  display: flex;
  justify-content: space-between;
}
</style>
