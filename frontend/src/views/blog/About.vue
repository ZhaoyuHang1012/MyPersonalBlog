<template>
  <BlogShell>
    <main class="post-detail">
      <h1 class="post-title">关于我</h1>
      <div v-if="aboutHtml" class="post-content markdown-body" v-html="aboutHtml"></div>
      <div v-else class="post-content markdown-body">
        <p>你好，欢迎来到我的博客！</p>
        <p>这是一个基于 <strong>Spring Boot + Vue3 + MySQL</strong> 搭建的本地个人博客平台。</p>
        <blockquote>
          <p>在后台「站点设置」中编辑本页内容。</p>
        </blockquote>
      </div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import hljs from 'highlight.js'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getSite } from '../../api'

const aboutHtml = ref('')

onMounted(async () => {
  try {
    const site = await getSite()
    aboutHtml.value = site.aboutHtml || ''
    if (aboutHtml.value) {
      await nextTick()
      document.querySelectorAll('.post-content pre code').forEach((el) => {
        hljs.highlightElement(el)
      })
    }
  } catch (e) {
    /* ignore */
  }
})
</script>
