<template>
  <BlogShell>
    <main class="archive-page">
      <h1 class="archive-title">📚 文章归档</h1>
      <p class="archive-sub">共 {{ totalCount }} 篇文章</p>

      <div v-for="g in groups" :key="g.year + '-' + g.month" class="archive-group">
        <h2 class="archive-month">{{ g.year }} 年 {{ g.month }} 月 <span class="month-count">{{ g.posts.length }} 篇</span></h2>
        <div v-for="p in g.posts" :key="p.id" class="archive-item">
          <span class="archive-date">{{ dayOfMonth(p.publishedAt) }}</span>
          <router-link :to="`/post/${p.id}`" class="archive-link">{{ p.title }}</router-link>
        </div>
      </div>

      <div v-if="!groups.length" class="empty">暂无文章</div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getArchive } from '../../api'

const groups = ref([])

const totalCount = computed(() => groups.value.reduce((n, g) => n + g.posts.length, 0))

const dayOfMonth = (d) => (d ? dayjs(d).format('MM-DD') : '')

onMounted(async () => {
  groups.value = await getArchive()
})
</script>
