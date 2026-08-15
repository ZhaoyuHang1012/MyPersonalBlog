<template>
  <BlogShell>
    <main class="post-detail" v-if="post">
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-meta">
        <span>📅 {{ formatDate(post.publishedAt) }}</span>
        <span v-if="post.categoryName">📁 {{ post.categoryName }}</span>
        <span v-for="t in post.tags" :key="t.id">🏷 {{ t.name }}</span>
        <span>👁 {{ post.viewCount }} 阅读</span>
      </div>
      <div class="post-content markdown-body" v-html="post.contentHtml"></div>
      <div class="post-nav">
        <router-link v-if="post.prev" :to="`/post/${post.prev.id}`">← {{ post.prev.title }}</router-link>
        <span v-else></span>
        <router-link v-if="post.next" :to="`/post/${post.next.id}`">{{ post.next.title }} →</router-link>
      </div>
      <CommentSection v-if="post" :post-id="post.id" />
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
import { getPost } from '../../api'

const route = useRoute()
const post = ref(null)
const notFound = ref(false)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  post.value = null
  notFound.value = false
  try {
    post.value = await getPost(route.params.id)
    document.title = `${post.value.title} - 博客`
    await nextTick()
    document.querySelectorAll('.post-content pre code').forEach((el) => {
      hljs.highlightElement(el)
    })
  } catch (e) {
    notFound.value = true
    document.title = '文章不存在 - 博客'
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>
