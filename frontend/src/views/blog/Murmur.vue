<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">💭 说说</h1>
      <p class="simple-sub">记录一些零碎的想法</p>

      <div class="murmur-list">
        <div v-for="m in murmurs" :key="m.id" class="murmur-item">
          <div class="murmur-content">{{ m.content }}</div>
          <div class="murmur-time">{{ formatDate(m.createdAt) }}</div>
        </div>
      </div>
      <div v-if="!murmurs.length" class="empty">还没有说说</div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getMurmurs } from '../../api'

const murmurs = ref([])

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

onMounted(async () => {
  murmurs.value = await getMurmurs()
})
</script>
