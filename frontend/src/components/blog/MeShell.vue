<template>
  <BlogShell>
    <main class="me-page">
      <h1 class="me-title">我的博客</h1>
      <div class="me-tabs">
        <router-link to="/me/posts" :class="{ active: isActive('posts') }">我的文章</router-link>
        <router-link to="/me/media" :class="{ active: isActive('media') }">我的媒体</router-link>
        <router-link to="/me/settings" :class="{ active: isActive('settings') }">个人设置</router-link>
      </div>
      <router-view />
    </main>
  </BlogShell>
</template>

<script setup>
import { useRoute } from 'vue-router'
import BlogShell from './BlogShell.vue'

const route = useRoute()
const isActive = (name) => route.path.includes(`/me/${name}`)
</script>
