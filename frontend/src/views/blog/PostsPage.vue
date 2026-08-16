<template>
  <BlogShell>
    <main class="blog-main posts-page">
      <div class="content">
        <div class="posts-page-head">
          <div>
            <h1 class="simple-title">📄 我的文章</h1>
            <p class="simple-sub">仅显示自己发布的文章，支持搜索与在线编辑</p>
          </div>
          <el-button v-if="isLoggedIn" type="primary" size="large" @click="$router.push('/posts/new')">
            ✍️ 写文章
          </el-button>
        </div>

        <template v-if="isLoggedIn">
          <div class="search-bar posts-toolbar">
            <input v-model="keyword" placeholder="搜索标题…" @keyup.enter="reload" />
            <button @click="reload">搜索</button>
            <el-radio-group v-model="status" size="small" class="posts-status" @change="reload">
              <el-radio-button :value="null">全部</el-radio-button>
              <el-radio-button :value="1">已发布</el-radio-button>
              <el-radio-button :value="0">草稿</el-radio-button>
            </el-radio-group>
            <div class="spacer"></div>
            <span class="posts-total">共 {{ total }} 篇</span>
          </div>

          <article v-for="p in posts" :key="p.id" class="post-card">
            <h2>
              <span v-if="p.isTop === 1" class="top-badge">置顶</span>
              <span v-if="p.status !== 1" class="draft-badge">草稿</span>
              <router-link v-if="p.status === 1" :to="`/post/${p.id}`">{{ p.title }}</router-link>
              <span v-else class="post-title-plain">{{ p.title }}</span>
            </h2>
            <p class="summary">{{ p.summary || '（暂无摘要）' }}</p>
            <div class="meta">
              <span>{{ formatDate(p.publishedAt || p.updatedAt) }}</span>
              <span v-if="p.categoryName" class="cat">{{ p.categoryName }}</span>
              <span v-for="t in p.tags" :key="t.id" class="tag"># {{ t.name }}</span>
              <span class="views">👁 {{ p.viewCount }} 阅读</span>
              <span class="views">💬 {{ p.commentCount }} 评论</span>
              <span class="views">👍 {{ p.likeCount || 0 }} 点赞</span>
              <span v-if="p.visibility === 2" class="friend-badge">👥 仅好友可见</span>
              <span v-if="p.visibility === 0" class="private-badge">🔒 仅自己可见</span>
            </div>
            <div class="card-actions">
              <el-button link type="primary" @click="$router.push(`/posts/${p.id}/edit`)">
                ✏️ 编辑
              </el-button>
              <el-button link type="danger" @click="remove(p)">🗑 删除</el-button>
            </div>
          </article>

          <div v-if="!posts.length && !loading" class="empty">暂无文章，点击右上角「写文章」开始创作</div>

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
        </template>
        <div v-else class="empty">
          登录后查看和发布你的文章，
          <router-link to="/admin/login">去登录</router-link>
        </div>
      </div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import BlogShell from '../../components/blog/BlogShell.vue'
import { adminListPosts, adminDeletePost } from '../../api'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)

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
