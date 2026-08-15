<template>
  <div>
    <!-- 发布说说（QQ 动态风格） -->
    <div class="murmur-publish">
      <el-input
        v-model="content"
        type="textarea"
        :rows="3"
        maxlength="2000"
        show-word-limit
        placeholder="分享新鲜事…"
      />
      <div v-if="pickedImages.length" class="murmur-publish-images">
        <div v-for="(img, i) in pickedImages" :key="i" class="publish-img-item">
          <img :src="img" />
          <span class="publish-img-remove" @click="pickedImages.splice(i, 1)">✕</span>
        </div>
      </div>
      <div class="murmur-publish-actions">
        <el-upload
          :show-file-list="false"
          :http-request="doUploadImage"
          accept="image/*"
          :disabled="pickedImages.length >= 9"
        >
          <el-button size="small" :disabled="pickedImages.length >= 9">🖼 添加图片</el-button>
        </el-upload>
        <el-radio-group v-model="visibility" size="small">
          <el-radio-button :value="1">公开</el-radio-button>
          <el-radio-button :value="0">仅自己可见</el-radio-button>
        </el-radio-group>
        <el-button type="primary" :loading="publishing" @click="publish">发布</el-button>
      </div>
    </div>

    <!-- 我的说说列表 -->
    <div v-for="m in murmurs" :key="m.id" class="murmur-item">
      <div class="murmur-item-head">
        <span class="murmur-item-time">{{ formatDate(m.createdAt) }}</span>
        <el-tag v-if="m.visibility === 0" size="small" type="info">仅自己可见</el-tag>
        <el-button link type="danger" size="small" @click="remove(m)">删除</el-button>
      </div>
      <div class="murmur-item-content">{{ m.content }}</div>
      <div v-if="m.images && m.images.length" class="murmur-item-images">
        <el-image
          v-for="(img, i) in m.images"
          :key="i"
          :src="img"
          :preview-src-list="m.images"
          :initial-index="i"
          fit="cover"
          preview-teleported
          lazy
          class="murmur-item-img"
        />
      </div>
    </div>
    <el-empty v-if="!murmurs.length" description="还没有发过说说" />

    <div v-if="total > size" class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        v-model:current-page="page"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListMurmurs, adminCreateMurmur, adminDeleteMurmur, uploadFile } from '../../api'

const content = ref('')
const pickedImages = ref([])
const visibility = ref(1)
const publishing = ref(false)
const murmurs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  const data = await adminListMurmurs({ page: page.value, size: size.value })
  murmurs.value = data.records
  total.value = data.total
}

const doUploadImage = async ({ file, onSuccess, onError }) => {
  try {
    const data = await uploadFile(file)
    pickedImages.value.push(data.url)
    onSuccess()
  } catch (e) {
    onError(e)
  }
}

const publish = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  publishing.value = true
  try {
    await adminCreateMurmur({
      content: content.value.trim(),
      images: pickedImages.value,
      visibility: visibility.value
    })
    ElMessage.success('发布成功')
    content.value = ''
    pickedImages.value = []
    visibility.value = 1
    page.value = 1
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    publishing.value = false
  }
}

const remove = async (m) => {
  try {
    await ElMessageBox.confirm('确定删除这条说说吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteMurmur(m.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
