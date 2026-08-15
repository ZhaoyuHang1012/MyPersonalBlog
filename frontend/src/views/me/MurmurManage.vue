<template>
  <div>
    <!-- 顶部按钮：新建弹窗 -->
    <div class="toolbar">
      <el-select
        v-if="userStore.isAdmin"
        v-model="authorId"
        clearable
        placeholder="全部用户"
        style="width: 200px"
        @change="reload"
      >
        <el-option
          v-for="u in users"
          :key="u.id"
          :label="`${u.nickname} (@${u.username})`"
          :value="u.id"
        />
      </el-select>
      <div class="spacer"></div>
      <el-button type="primary" @click="publishVisible = true">➕ 发布说说</el-button>
    </div>

    <!-- 发布弹窗 -->
    <el-dialog v-model="publishVisible" title="发布说说" width="520px">
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
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
      </div>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="publish">发布</el-button>
      </template>
    </el-dialog>

    <!-- 我的说说列表 -->
    <div v-for="m in murmurs" :key="m.id" class="murmur-item">
      <div class="murmur-item-head">
        <span v-if="userStore.isAdmin && m.author" class="murmur-item-author">
          {{ m.author.nickname }}
        </span>
        <span class="murmur-item-time">{{ formatDate(m.createdAt) }}</span>
        <el-tag v-if="m.visibility === 0" size="small" type="info">仅自己可见</el-tag>
        <el-button link type="primary" size="small" @click="openEdit(m)">编辑</el-button>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑说说" width="480px">
      <el-input v-model="editForm.content" type="textarea" :rows="4" maxlength="2000" show-word-limit />
      <div class="edit-visibility">
        <span>可见性：</span>
        <el-radio-group v-model="editForm.visibility">
          <el-radio :value="1">公开</el-radio>
          <el-radio :value="0">仅自己可见</el-radio>
        </el-radio-group>
      </div>
      <p class="edit-tip">配图不可在此修改，如需调整请删除后重新发布</p>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListMurmurs, adminCreateMurmur, adminUpdateMurmur, adminDeleteMurmur, uploadFile, adminListUsers } from '../../api'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const users = ref([])
const authorId = ref(null)

const content = ref('')
const pickedImages = ref([])
const visibility = ref(1)
const publishing = ref(false)
const publishVisible = ref(false)
const murmurs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const editVisible = ref(false)
const saving = ref(false)
const editForm = ref({ id: null, content: '', visibility: 1 })

const openEdit = (m) => {
  editForm.value = { id: m.id, content: m.content, visibility: m.visibility, images: m.images || [] }
  editVisible.value = true
}

const saveEdit = async () => {
  if (!editForm.value.content.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  saving.value = true
  try {
    await adminUpdateMurmur(editForm.value.id, {
      content: editForm.value.content.trim(),
      visibility: editForm.value.visibility,
      images: editForm.value.images
    })
    ElMessage.success('已保存')
    editVisible.value = false
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  const data = await adminListMurmurs({
    page: page.value,
    size: size.value,
    authorId: authorId.value || undefined
  })
  murmurs.value = data.records
  total.value = data.total
}

const reload = () => {
  page.value = 1
  load()
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
    publishVisible.value = false
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

onMounted(() => {
  if (userStore.isAdmin) {
    adminListUsers().then((list) => (users.value = list)).catch(() => {})
  }
  load()
})
</script>

<style scoped>
.edit-visibility {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
}

.edit-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}
</style>
