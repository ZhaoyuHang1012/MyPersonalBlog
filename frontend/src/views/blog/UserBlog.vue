<template>
  <BlogShell>
    <main class="blog-main">
      <div class="content">
        <!-- 作者信息卡 -->
        <div v-if="profile" class="user-card">
          <el-avatar :size="64" :src="profile.avatar || undefined" style="background: #3a7afe">
            {{ (profile.nickname || 'U')[0] }}
          </el-avatar>
          <div class="user-card-info">
            <h2>{{ profile.nickname }}</h2>
            <p>@{{ profile.username }} · {{ profile.postCount }} 篇公开文章</p>
          </div>
          <div class="user-card-actions" v-if="canOperate">
            <el-button v-if="relation === 'none'" type="primary" size="small" @click="sendRequest">
              ➕ 加好友
            </el-button>
            <el-button v-else-if="relation === 'requested'" size="small" disabled>已申请，等待处理</el-button>
            <el-button v-else-if="relation === 'friend'" size="small" disabled>✓ 已是好友</el-button>
            <el-button v-else-if="relation === 'pending'" type="warning" size="small" @click="router.push('/me/friends')">
              TA 申请加你为好友 →
            </el-button>
          </div>
        </div>

        <article v-for="p in posts" :key="p.id" class="post-card">
          <h2>
            <span v-if="p.isTop === 1" class="top-badge">置顶</span>
            <router-link :to="`/post/${p.id}`">{{ p.title }}</router-link>
          </h2>
          <p class="summary">{{ p.summary || '（暂无摘要）' }}</p>
          <div class="meta">
            <span>{{ formatDate(p.publishedAt) }}</span>
            <span v-if="p.categoryName" class="cat">{{ p.categoryName }}</span>
            <span v-for="t in p.tags" :key="t.id" class="tag"># {{ t.name }}</span>
            <span class="views">👁 {{ p.viewCount }} 阅读</span>
            <span class="views">💬 {{ p.commentCount }} 评论</span>
          </div>
        </article>

        <div v-if="!posts.length" class="empty">TA 还没有公开的文章</div>

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

      <aside class="sidebar">
        <div class="side-card about-me">
          <h3>关于 TA</h3>
          <p v-if="profile">这里记录了 {{ profile.nickname }} 发布的所有公开文章。</p>
          <router-link to="/">← 回到大厅</router-link>
        </div>
      </aside>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getUserInfo, getUserPosts, getFriendRelation, sendFriendRequest } from '../../api'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const profile = ref(null)
const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const relation = ref('none')

// 登录且非本人时显示好友操作
const canOperate = computed(() =>
  !!userStore.token && profile.value && userStore.user?.id !== profile.value.id
)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD') : '')

const sendRequest = async () => {
  await sendFriendRequest({ toUserId: profile.value.id, message: '' })
  ElMessage.success('好友申请已发送')
  relation.value = 'requested'
}

const load = async () => {
  try {
    profile.value = await getUserInfo(route.params.username)
    const data = await getUserPosts(route.params.username, { page: page.value, size: size.value })
    posts.value = data.records
    total.value = data.total
    document.title = `${profile.value.nickname} 的博客`
    if (canOperate.value) {
      const rel = await getFriendRelation(profile.value.id)
      relation.value = rel.relation
    }
  } catch (e) {
    /* 拦截器已提示 */
  }
}

watch(() => route.params.username, () => {
  page.value = 1
  load()
})
onMounted(load)
</script>
