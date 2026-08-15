<template>
  <BlogShell>
    <main class="me-page">
      <h1 class="me-title">我的博客</h1>
      <div class="me-tabs">
        <router-link to="/me/posts" :class="{ active: isActive('posts') }">我的文章</router-link>
        <router-link to="/me/murmurs" :class="{ active: isActive('murmurs') }">我的说说</router-link>
        <router-link to="/me/albums" :class="{ active: isActive('albums') }">我的相册</router-link>
        <router-link to="/me/friends" :class="{ active: isActive('friends') }">我的好友</router-link>
        <router-link to="/me/favorites" :class="{ active: isActive('favorites') }">我的收藏</router-link>
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
// posts 前缀也匹配 posts/new、posts/:id/edit
const isActive = (name) => route.path.includes(`/me/${name}`)
</script>
