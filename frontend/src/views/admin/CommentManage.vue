<template>
  <div>
    <h2 class="page-title">评论管理</h2>
    <div class="toolbar">
      <el-radio-group v-model="status" @change="reload">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
        <el-radio-button :value="2">垃圾</el-radio-button>
      </el-radio-group>
      <div class="spacer"></div>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="comments" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="所属文章" min-width="160">
        <template #default="{ row }">
          <span class="post-title-link" @click="openPost(row)">{{ row.postTitle }}</span>
        </template>
      </el-table-column>
      <el-table-column label="评论者" width="150">
        <template #default="{ row }">
          <div>{{ row.nickname }}</div>
          <div v-if="row.website" class="comment-meta">🔗 {{ row.website }}</div>
        </template>
      </el-table-column>
      <el-table-column label="内容" min-width="240">
        <template #default="{ row }">
          <span class="comment-content-text">{{ row.content }}</span>
          <el-tag v-if="row.parentId" size="small" type="info" class="mr4">回复</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="150">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" link type="success" @click="approve(row)">通过</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="unapprove(row)">退回</el-button>
          <el-button v-if="row.status !== 2" link type="warning" @click="reject(row)">垃圾</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminListComments,
  adminApproveComment,
  adminUnapproveComment,
  adminRejectComment,
  adminDeleteComment
} from '../../api'

const comments = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const status = ref(null)
const loading = ref(false)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '垃圾' }[s] || '未知')
const statusTagType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')

const openPost = (row) => {
  window.open(`/post/${row.postId}`, '_blank')
}

const load = async () => {
  loading.value = true
  try {
    const data = await adminListComments({
      page: page.value,
      size: size.value,
      status: status.value ?? undefined
    })
    comments.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  load()
}

const approve = async (row) => {
  await adminApproveComment(row.id)
  ElMessage.success('已通过，前台可见')
  load()
}

const unapprove = async (row) => {
  await adminUnapproveComment(row.id)
  ElMessage.success('已退回待审核，前台已隐藏')
  load()
}

const reject = async (row) => {
  await adminRejectComment(row.id)
  ElMessage.success('已标记为垃圾')
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？其下回复将保留为顶级评论。', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteComment(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.comment-meta {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}

.comment-content-text {
  white-space: pre-wrap;
}
</style>
