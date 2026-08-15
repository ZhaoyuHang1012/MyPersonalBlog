<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">🗂 归档</h1>
      <p class="simple-sub">按月份归档的文章、说说和相册</p>

      <div v-for="g in groups" :key="g.year + '-' + g.month" class="archive-group">
        <h2 class="archive-month">
          {{ g.year }} 年 {{ g.month }} 月
          <span class="month-count">{{ g.items.length }} 条</span>
        </h2>
        <div v-for="item in g.items" :key="item.type + '-' + item.id" class="archive-item">
          <span class="timeline-icon">{{ typeIcon(item.type) }}</span>
          <span class="archive-date">{{ formatDay(item.createdAt) }}</span>
          <router-link v-if="item.type === 'post'" :to="`/post/${item.id}`" class="archive-link">
            {{ item.title }}
          </router-link>
          <router-link v-else-if="item.type === 'album'" :to="`/album/${item.id}`" class="archive-link">
            {{ item.title }}
          </router-link>
          <span v-else class="archive-link muted">{{ item.title }}</span>
        </div>
      </div>
      <div v-if="!groups.length" class="empty">暂无归档内容</div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getTimeline } from '../../api'

const groups = ref([])

const typeIcon = (t) => ({ post: '📄', murmur: '💭', album: '📷' }[t] || '📌')
const formatDay = (d) => (d ? dayjs(d).format('MM-DD') : '')

onMounted(async () => {
  try {
    groups.value = await getTimeline()
  } catch (e) {
    /* 拦截器已提示 */
  }
})
</script>
