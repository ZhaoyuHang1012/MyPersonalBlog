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
          <router-link to="/favorites">收藏</router-link>
          <router-link to="/links">友链</router-link>
          <router-link to="/murmur">说说</router-link>
          <router-link to="/album">相册</router-link>
          <router-link to="/about">关于</router-link>
          <template v-if="isLoggedIn">
            <el-dropdown class="user-menu" trigger="click" @command="onUserCommand">
              <span class="user-menu-trigger">
                <el-avatar :size="26" :src="userStore.user?.avatar || undefined" style="background: rgba(255,255,255,0.25)">
                  {{ (userStore.user?.nickname || 'U')[0] }}
                </el-avatar>
                <span class="user-menu-name">{{ userStore.user?.nickname || '用户' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="write">✍️ 写文章</el-dropdown-item>
                  <el-dropdown-item command="posts">📄 我的文章</el-dropdown-item>
                  <el-dropdown-item command="media">🖼 我的媒体</el-dropdown-item>
                  <el-dropdown-item command="settings">⚙️ 个人设置</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>🛠 管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/admin/login">登录</router-link>
            <router-link to="/register">注册</router-link>
          </template>
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
import { useRouter } from 'vue-router'
import { getSite } from '../../api'
import { useUserStore } from '../../store/user'

const router = useRouter()
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

const onUserCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/')
  } else if (cmd === 'admin') {
    router.push('/admin')
  } else {
    const map = { write: '/me/posts/new', posts: '/me/posts', media: '/me/media', settings: '/me/settings' }
    router.push(map[cmd] || '/me/posts')
  }
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
