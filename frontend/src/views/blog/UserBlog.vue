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
            <p>@{{ profile.username }} · {{ profile.postCount }} 篇可见文章</p>
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

        <!-- 快捷切换：文章 / 说说 / 相册 -->
        <div class="hall-tabs">
          <span :class="{ active: tab === 'post' }" @click="switchTab('post')">📄 文章</span>
          <span :class="{ active: tab === 'murmur' }" @click="switchTab('murmur')">💭 说说</span>
          <span :class="{ active: tab === 'album' }" @click="switchTab('album')">📷 相册</span>
        </div>

        <!-- 文章 -->
        <template v-if="tab === 'post'">
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
          <div v-if="!posts.length" class="empty">TA 还没有可见的文章</div>
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

        <!-- 说说 -->
        <template v-else-if="tab === 'murmur'">
          <div v-for="m in murmurs" :key="m.id" class="murmur-card">
            <div class="murmur-content-text">{{ m.content }}</div>
            <div v-if="m.images && m.images.length" class="murmur-images" :class="'count-' + Math.min(m.images.length, 9)">
              <el-image
                v-for="(img, i) in m.images"
                :key="i"
                :src="img"
                :preview-src-list="m.images"
                :initial-index="i"
                fit="cover"
                preview-teleported
                lazy
                class="murmur-img"
              />
            </div>
            <div class="murmur-head">
              <div class="murmur-time">{{ formatDate(m.createdAt) }}</div>
              <div class="murmur-actions-right">
                <LikeButton :target-type="'murmur'" :target-id="m.id" />
                <FavoriteButton :target-type="'murmur'" :target-id="m.id" />
              </div>
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
          <div v-if="!murmurs.length" class="empty">TA 还没有可见的说说</div>
          <div v-if="murmurTotal > murmurSize" class="pager">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="murmurTotal"
              :page-size="murmurSize"
              v-model:current-page="murmurPage"
              @current-change="loadMurmurs"
            />
          </div>
        </template>

        <!-- 相册 -->
        <template v-else>
          <div v-if="albums.length" class="album-group-grid">
            <div v-for="g in albums" :key="g.id" class="album-group-card">
              <router-link :to="`/album/${g.id}`" class="album-group-link">
                <VideoThumb v-if="isVideoUrl(g.cover)" :src="g.cover" class="album-group-cover-video" />
                <img v-else-if="g.cover" :src="g.cover" :alt="g.name" loading="lazy" />
                <div v-else class="album-no-cover">📷</div>
                <div class="album-group-info">
                  <div class="album-group-name">{{ g.name }}</div>
                  <div class="album-group-meta">{{ g.photoCount }} 个内容</div>
                </div>
              </router-link>
              <div class="album-group-actions">
                <FavoriteButton :target-type="'album'" :target-id="g.id" />
              </div>
            </div>
          </div>
          <div v-else class="empty">TA 还没有可见的相册</div>
        </template>
      </div>

      <aside class="sidebar">
        <div class="side-card about-me">
          <h3>关于 TA</h3>
          <p v-if="profile">
            这里记录了 {{ profile.nickname }} 的文章、说说和相册。
            仅好友可见的内容需要先成为好友才能查看。
          </p>
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
import CommentSection from '../../components/comments/CommentSection.vue'
import FavoriteButton from '../../components/common/FavoriteButton.vue'
import LikeButton from '../../components/common/LikeButton.vue'
import VideoThumb from '../../components/common/VideoThumb.vue'
import {
  getUserInfo,
  getUserPosts,
  getUserMurmurs,
  getUserAlbums,
  getFriendRelation,
  sendFriendRequest
} from '../../api'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const profile = ref(null)
const tab = ref('post')

const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const murmurs = ref([])
const murmurTotal = ref(0)
const murmurPage = ref(1)
const murmurSize = ref(10)
const expandedId = ref(null)

const albums = ref([])
const relation = ref('none')

// 登录且非本人时显示好友操作
const canOperate = computed(() =>
  !!userStore.token && profile.value && userStore.user?.id !== profile.value.id
)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const toggleComments = (id) => {
  expandedId.value = expandedId.value === id ? null : id
}

const isVideoUrl = (url) => /\.(mp4|webm|mov|m4v|avi)(\?.*)?$/i.test(url || '')

const sendRequest = async () => {
  await sendFriendRequest({ toUserId: profile.value.id, message: '' })
  ElMessage.success('好友申请已发送')
  relation.value = 'requested'
}

const switchTab = (name) => {
  tab.value = name
  if (name === 'murmur' && !murmurs.value.length) {
    loadMurmurs()
  } else if (name === 'album' && !albums.value.length) {
    loadAlbums()
  }
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

const loadMurmurs = async () => {
  try {
    const data = await getUserMurmurs(route.params.username, { page: murmurPage.value, size: murmurSize.value })
    murmurs.value = data.records
    murmurTotal.value = data.total
  } catch (e) {
    /* 拦截器已提示 */
  }
}

const loadAlbums = async () => {
  try {
    albums.value = await getUserAlbums(route.params.username)
  } catch (e) {
    /* 拦截器已提示 */
  }
}

watch(() => route.params.username, () => {
  page.value = 1
  murmurPage.value = 1
  tab.value = 'post'
  murmurs.value = []
  albums.value = []
  load()
})
onMounted(load)
</script>
