<template>
  <BlogShell>
    <main class="post-detail post-detail-with-toc" v-if="post">
      <!-- 左侧目录导航 -->
      <aside v-if="toc.length" class="post-toc">
        <div class="toc-title">📑 目录</div>
        <nav class="toc-nav">
          <a
            v-for="t in toc"
            :key="t.id"
            :class="['toc-link', 'level-' + t.level, { active: activeId === t.id }]"
            :href="'#' + t.id"
            @click.prevent="scrollToHeading(t.id)"
          >{{ t.text }}</a>
        </nav>
      </aside>

      <!-- 正文 -->
      <div class="post-main">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span v-if="post.author" class="author-link" @click="$router.push(`/u/${post.author.username}`)">
            <el-avatar
              :size="24"
              :src="post.author.avatar || undefined"
              style="background: #3a7afe; vertical-align: middle"
            >{{ (post.author.nickname || 'A')[0] }}</el-avatar>
            {{ post.author.nickname }}
          </span>
          <span>📅 {{ formatDate(post.publishedAt) }}</span>
          <span v-if="post.categoryName">📁 {{ post.categoryName }}</span>
          <span v-for="t in post.tags" :key="t.id">🏷 {{ t.name }}</span>
          <span>👁 {{ post.viewCount }} 阅读</span>
        </div>
        <div class="post-content markdown-body" v-html="post.contentHtml"></div>
        <div class="post-like-bar">
          <LikeButton :target-type="'post'" :target-id="post.id" @changed="refreshLikers" />
          <div v-if="likers.length" class="liker-avatars">
            <el-tooltip v-for="l in likers" :key="l.id" :content="l.nickname" placement="top">
              <el-avatar :size="30" :src="l.avatar || undefined" style="background: #3a7afe">
                {{ (l.nickname || 'U')[0] }}
              </el-avatar>
            </el-tooltip>
            <span class="liker-tip">等 {{ likers.length }} 人点赞</span>
          </div>
        </div>
        <div class="post-nav">
          <router-link v-if="post.prev" :to="`/post/${post.prev.id}`">← {{ post.prev.title }}</router-link>
          <span v-else></span>
          <router-link v-if="post.next" :to="`/post/${post.next.id}`">{{ post.next.title }} →</router-link>
        </div>
        <CommentSection v-if="post" :post-id="post.id" :author-id="post.userId" />
      </div>
    </main>
    <main class="empty" v-else-if="notFound">文章不存在或已删除</main>
  </BlogShell>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import hljs from 'highlight.js'
import BlogShell from '../../components/blog/BlogShell.vue'
import CommentSection from '../../components/comments/CommentSection.vue'
import LikeButton from '../../components/common/LikeButton.vue'
import { getPost, getLikers } from '../../api'
import { useUserStore } from '../../store/user'

const route = useRoute()
const userStore = useUserStore()
const post = ref(null)
const notFound = ref(false)
const likers = ref([])
const toc = ref([])
const activeId = ref('')

/** 从渲染后的正文提取 h1-h3 生成目录 */
const buildToc = () => {
  toc.value = []
  const headings = document.querySelectorAll('.post-content h1, .post-content h2, .post-content h3')
  let index = 0
  headings.forEach((h) => {
    const id = 'heading-' + (++index)
    h.id = id
    toc.value.push({ id, text: h.textContent.trim(), level: Number(h.tagName[1]) })
  })
}

/** 点击目录项平滑滚动到对应标题 */
const scrollToHeading = (id) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeId.value = id
  }
}

/** 滚动监听：高亮当前阅读位置的目录项 */
const onScroll = () => {
  if (!toc.value.length) return
  let current = toc.value[0].id
  for (const t of toc.value) {
    const el = document.getElementById(t.id)
    if (el && el.getBoundingClientRect().top <= 120) {
      current = t.id
    }
  }
  activeId.value = current
}

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  post.value = null
  notFound.value = false
  likers.value = []
  try {
    post.value = await getPost(route.params.id)
    document.title = `${post.value.title} - 博客`
    await nextTick()
    document.querySelectorAll('.post-content pre code').forEach((el) => {
      hljs.highlightElement(el)
    })
    buildToc()
    try {
      likers.value = await getLikers('post', route.params.id)
    } catch (e) {
      /* ignore */
    }
  } catch (e) {
    notFound.value = true
    document.title = '文章不存在 - 博客'
  }
}

/** 点赞/取消后实时更新「等 N 人点赞」：先本地乐观更新，再后台校准 */
const refreshLikers = async ({ liked }) => {
  if (!post.value) return
  const me = userStore.user
  if (me) {
    if (liked) {
      // 立即把自己加入点赞人列表（无需等待网络）
      if (!likers.value.some((l) => l.id === me.id)) {
        likers.value = [...likers.value, { id: me.id, nickname: me.nickname, avatar: me.avatar }]
      }
    } else {
      likers.value = likers.value.filter((l) => l.id !== me.id)
    }
  }
  // 后台静默校准，保证与其他用户数据一致
  try {
    likers.value = await getLikers('post', post.value.id)
  } catch (e) {
    /* ignore */
  }
}

watch(() => route.params.id, load)
onMounted(() => {
  load()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>
