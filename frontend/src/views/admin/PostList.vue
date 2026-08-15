<template>
  <div>
    <h2 class="page-title">文章管理</h2>
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索标题"
        clearable
        style="width: 240px"
        @keyup.enter="reload"
        @clear="reload"
      />
      <el-select v-model="status" placeholder="全部状态" clearable style="width: 140px" @change="reload">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button @click="reload">查询</el-button>
      <div class="spacer"></div>
      <el-button type="primary" @click="$router.push('/admin/posts/new')">新建文章</el-button>
    </div>

    <el-table :data="posts" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="标题" min-width="260">
        <template #default="{ row }">
          <span class="post-title-link" @click="$router.push(`/admin/posts/${row.id}/edit`)">
            {{ row.title }}
          </span>
          <el-tag v-if="row.isTop === 1" size="small" type="warning" class="mr4">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="130">
        <template #default="{ row }">{{ row.categoryName || '未分类' }}</template>
      </el-table-column>
      <el-table-column label="标签" min-width="150">
        <template #default="{ row }">
          <el-tag v-for="t in row.tags" :key="t.id" size="small" type="info" class="mr4">
            {{ t.name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/admin/posts/${row.id}/edit`)">
            编辑
          </el-button>
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
import { adminListPosts, adminDeletePost } from '../../api'

const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref(null)
const loading = ref(false)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  loading.value = true
  try {
    const data = await adminListPosts({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      status: status.value ?? undefined
    })
    posts.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？此操作不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeletePost(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
