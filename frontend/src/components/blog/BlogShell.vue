<template>
  <div class="blog-page">
    <header class="blog-header">
      <div class="header-inner">
        <div class="site-info" @click="$router.push('/')">
          <h1>{{ site.title }}</h1>
          <p>{{ site.subtitle }}</p>
        </div>
        <nav class="site-nav">
          <router-link to="/">大厅</router-link>
          <router-link to="/archive">归档</router-link>
          <router-link to="/links">友链</router-link>
          <router-link to="/murmur">说说</router-link>
          <router-link to="/album">相册</router-link>
          <router-link to="/about">关于</router-link>
          <router-link v-if="isLoggedIn" to="/admin">我的主页</router-link>
          <router-link v-else to="/register">注册</router-link>
          <a class="theme-toggle" :title="isDark ? '切换到白天模式' : '切换到黑夜模式'" @click="toggleTheme">
            {{ isDark ? '☀️' : '🌙' }}
          </a>
        </nav>
      </div>
      <div v-if="site.announcement" class="blog-announcement">
        <span>📢 {{ site.announcement }}</span>
      </div>
    </header>
    <slot />
    <footer class="blog-footer">
      <p>© {{ year }} {{ site.title }} · Powered by Spring Boot &amp; Vue3</p>
      <p v-if="site.footer" class="footer-text">{{ site.footer }}</p>
      <p v-if="site.icp" class="footer-text">{{ site.icp }}</p>
      <p class="footer-text">
        <a href="/api/rss" target="_blank">📡 RSS 订阅</a>
      </p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSite } from '../../api'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const site = ref({ title: '我的博客', subtitle: '', author: '', announcement: '', footer: '', icp: '' })
const isLoggedIn = computed(() => !!userStore.token)
const year = new Date().getFullYear()
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')

const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : '')
  localStorage.setItem('blog_theme', isDark.value ? 'dark' : 'light')
}

onMounted(async () => {
  try {
    site.value = await getSite()
    document.title = site.value.title
  } catch (e) {
    /* 站点信息获取失败时使用默认值 */
  }
})
</script>
