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
      <el-button type="primary" @click="openPublish">➕ 发布说说</el-button>
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
      <div v-if="pubHint" class="autosave-hint">💾 {{ pubHint }}</div>
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
      <div class="murmur-foot">
        <span class="murmur-comment-toggle" @click="toggleComments(m.id)">
          💬 评论（{{ m.commentCount || 0 }}）{{ expandedId === m.id ? '▲' : '▼' }}
        </span>
      </div>
      <div v-if="expandedId === m.id" class="murmur-comment-box">
        <CommentSection :murmur-id="m.id" :author-id="m.userId" />
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
      <div v-if="editHint" class="autosave-hint">💾 {{ editHint }}</div>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentSection from '../../components/comments/CommentSection.vue'
import { adminListMurmurs, adminCreateMurmur, adminUpdateMurmur, adminDeleteMurmur, uploadFile, adminListUsers } from '../../api'
import { useUserStore } from '../../store/user'
import { saveDraft, loadDraft, clearDraft } from '../../utils/draft'

const userStore = useUserStore()
const users = ref([])
const authorId = ref(null)
const expandedId = ref(null)

const toggleComments = (id) => {
  expandedId.value = expandedId.value === id ? null : id
}

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

// ==================== 自动保存草稿 ====================
const pubHint = ref('')
const editHint = ref('')
let pubTimer = null
let editTimer = null

/** 发布弹窗：打开前检查草稿 */
const openPublish = async () => {
  const draft = loadDraft('murmur-new')
  if (draft) {
    try {
      await ElMessageBox.confirm(
        `检测到 ${dayjs(draft.at).format('MM-DD HH:mm')} 未完成的说说，是否恢复继续编辑？`,
        '恢复草稿',
        { confirmButtonText: '恢复继续', cancelButtonText: '丢弃草稿', type: 'info' }
      )
      content.value = draft.value.content || ''
      pickedImages.value = draft.value.images || []
      visibility.value = draft.value.visibility ?? 1
      pubHint.value = '已恢复上次的编辑内容'
    } catch (e) {
      clearDraft('murmur-new')
    }
  }
  publishVisible.value = true
}

watch([content, pickedImages, visibility], () => {
  if (!publishVisible.value) return
  clearTimeout(pubTimer)
  pubTimer = setTimeout(() => {
    // 内容与配图全部清空时不再保留草稿
    if (!content.value.trim() && !pickedImages.value.length) {
      clearDraft('murmur-new')
      pubHint.value = ''
      return
    }
    if (saveDraft('murmur-new', {
      content: content.value,
      images: [...pickedImages.value],
      visibility: visibility.value
    })) {
      pubHint.value = `已自动保存 ${dayjs().format('HH:mm:ss')}`
    }
  }, 2000)
}, { deep: true })

/** 编辑弹窗：打开前检查该说说的草稿 */
const openEdit = async (m) => {
  editForm.value = { id: m.id, content: m.content, visibility: m.visibility, images: m.images || [] }
  const draft = loadDraft(`murmur-edit-${m.id}`)
  if (draft) {
    try {
      await ElMessageBox.confirm(
        `检测到 ${dayjs(draft.at).format('MM-DD HH:mm')} 对这条说说的未完成编辑，是否恢复？`,
        '恢复草稿',
        { confirmButtonText: '恢复继续', cancelButtonText: '丢弃草稿', type: 'info' }
      )
      editForm.value.content = draft.value.content ?? editForm.value.content
      editForm.value.visibility = draft.value.visibility ?? editForm.value.visibility
      editHint.value = '已恢复上次的编辑内容'
    } catch (e) {
      clearDraft(`murmur-edit-${m.id}`)
    }
  }
  editVisible.value = true
}

watch(editForm, () => {
  if (!editVisible.value || !editForm.value.id) return
  clearTimeout(editTimer)
  editTimer = setTimeout(() => {
    // 内容清空时不再保留草稿
    if (!editForm.value.content.trim()) {
      clearDraft(`murmur-edit-${editForm.value.id}`)
      editHint.value = ''
      return
    }
    if (saveDraft(`murmur-edit-${editForm.value.id}`, {
      content: editForm.value.content,
      visibility: editForm.value.visibility
    })) {
      editHint.value = `已自动保存 ${dayjs().format('HH:mm:ss')}`
    }
  }, 2000)
}, { deep: true })

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
    clearDraft(`murmur-edit-${editForm.value.id}`)
    editHint.value = ''
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
    clearDraft('murmur-new')
    pubHint.value = ''
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

onBeforeUnmount(() => {
  clearTimeout(pubTimer)
  clearTimeout(editTimer)
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
