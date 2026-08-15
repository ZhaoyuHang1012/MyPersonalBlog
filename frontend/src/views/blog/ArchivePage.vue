<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">📚 我的归档</h1>
      <p class="simple-sub">你在文章、说说、相册上点过「☆归档」的内容都收在这里，可随时取消归档</p>
      <Archives />
    </main>
  </BlogShell>
</template>

<script setup>
import BlogShell from '../../components/blog/BlogShell.vue'
import Archives from '../me/Archives.vue'
</script>
