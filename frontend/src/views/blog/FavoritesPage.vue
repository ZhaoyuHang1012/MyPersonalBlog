<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">⭐ 我的收藏</h1>
      <p class="simple-sub">你在文章、说说、相册上点过「☆收藏」的内容都收在这里，可随时取消收藏</p>
      <Favorites />
    </main>
  </BlogShell>
</template>

<script setup>
import BlogShell from '../../components/blog/BlogShell.vue'
import Favorites from '../me/Favorites.vue'
</script>
