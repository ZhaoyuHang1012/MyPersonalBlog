<template>
  <BlogShell>
    <main class="post-detail" v-if="post">
      <div v-if="post.author" class="post-author-row">
        <router-link :to="`/u/${post.author.username}`">
          <el-avatar :size="40" :src="post.author.avatar || undefined" style="background: #3a7afe">
            {{ (post.author.nickname || 'A')[0] }}
          </el-avatar>
        </router-link>
        <router-link :to="`/u/${post.author.username}`" class="post-author-name">
          {{ post.author.nickname }}
        </router-link>
      </div>
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-meta">
        <span>📅 {{ formatDate(post.publishedAt) }}</span>
        <span v-if="post.categoryName">📁 {{ post.categoryName }}</span>
        <span v-for="t in post.tags" :key="t.id">🏷 {{ t.name }}</span>
        <span>👁 {{ post.viewCount }} 阅读</span>
      </div>
      <div class="post-content markdown-body" v-html="post.contentHtml"></div>
      <div class="post-like-bar">
        <LikeButton :target-type="'post'" :target-id="post.id" />
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
    </main>
    <main class="empty" v-else-if="notFound">文章不存在或已删除</main>
  </BlogShell>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import hljs from 'highlight.js'
import BlogShell from '../../components/blog/BlogShell.vue'
import CommentSection from '../../components/comments/CommentSection.vue'
import LikeButton from '../../components/common/LikeButton.vue'
import { getPost, getLikers } from '../../api'

const route = useRoute()
const post = ref(null)
const notFound = ref(false)
const likers = ref([])

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

watch(() => route.params.id, load)
onMounted(load)
</script>
