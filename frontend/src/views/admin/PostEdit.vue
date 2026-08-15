<template>
  <div>
    <h2 class="page-title">{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
    <el-form label-width="60px" v-loading="loading">
      <el-form-item label="标题">
        <el-input
          v-model="form.title"
          placeholder="请输入文章标题"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="2"
          placeholder="文章摘要（显示在列表页，留空将不显示）"
          maxlength="500"
        />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 280px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-select
          v-model="form.tagIds"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择标签或输入新标签后回车"
          style="width: 420px"
        >
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">草稿</el-radio>
          <el-radio :value="1">发布</el-radio>
        </el-radio-group>
        <el-checkbox
          v-model="form.isTop"
          :true-value="1"
          :false-value="0"
          style="margin-left: 24px"
        >置顶</el-checkbox>
      </el-form-item>
      <el-form-item label="可见性">
        <el-radio-group v-model="form.visibility">
          <el-radio :value="1">开放（大厅和个人主页可见）</el-radio>
          <el-radio :value="0">仅自己可见</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="内容">
        <MdEditor
          v-model="form.contentMd"
          :toolbars="toolbars"
          placeholder="开始写作…（支持 Markdown 语法，工具栏图片按钮可上传本地图片）"
          style="height: 520px; width: 100%"
          @onUploadImg="onUploadImg"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import {
  getCategories,
  getTags,
  adminGetPost,
  adminCreatePost,
  adminUpdatePost,
  adminCreateTag,
  uploadFile
} from '../../api'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const form = ref({
  title: '',
  summary: '',
  contentMd: '',
  categoryId: null,
  tagIds: [],
  status: 0,
  visibility: 1,
  isTop: 0
})
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const saving = ref(false)

const toolbars = [
  'bold', 'italic', 'strikeThrough', 'title',
  'quote', 'unorderedList', 'orderedList', 'task',
  'codeRow', 'code', 'link', 'image', 'table',
  'revoke', 'next', 'save', '-',
  'preview', 'catalog'
]

const loadOptions = async () => {
  categories.value = await getCategories()
  tags.value = await getTags()
}

const loadPost = async () => {
  loading.value = true
  try {
    const p = await adminGetPost(route.params.id)
    form.value = {
      title: p.title,
      summary: p.summary || '',
      contentMd: p.contentMd || '',
      categoryId: p.categoryId,
      tagIds: (p.tags || []).map((t) => t.id),
      status: p.status,
      visibility: p.visibility ?? 1,
      isTop: p.isTop
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOptions().catch(() => {})
  if (isEdit.value) loadPost()
})

// 编辑器图片上传：逐个上传到服务器，成功后回填 URL
const onUploadImg = async (files, callback) => {
  const urls = []
  for (const file of files) {
    try {
      const data = await uploadFile(file)
      urls.push(data.url)
    } catch (e) {
      ElMessage.error('图片上传失败，请稍后重试')
    }
  }
  callback(urls)
}

// 处理编辑器里输入的新标签：先创建再取 ID
const normalizeTagIds = async () => {
  const ids = []
  for (const v of form.value.tagIds) {
    if (typeof v === 'number') {
      ids.push(v)
      continue
    }
    const existing = tags.value.find((t) => t.name === v)
    if (existing) {
      ids.push(existing.id)
      continue
    }
    const created = await adminCreateTag({ name: v })
    tags.value.push({ id: created.id, name: created.name })
    ids.push(created.id)
  }
  return ids
}

const save = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.contentMd.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  saving.value = true
  try {
    const tagIds = await normalizeTagIds()
    const payload = { ...form.value, tagIds }
    if (isEdit.value) {
      await adminUpdatePost(route.params.id, payload)
    } else {
      await adminCreatePost(payload)
    }
    ElMessage.success('保存成功')
    router.push('/admin/posts')
  } catch (e) {
    /* 错误已由拦截器提示 */
  } finally {
    saving.value = false
  }
}
</script>
