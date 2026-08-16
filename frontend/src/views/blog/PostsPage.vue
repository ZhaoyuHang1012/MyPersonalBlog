<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">📄 我的文章</h1>
      <p class="simple-sub">管理自己发布的文章（新建、编辑、删除）</p>
      <template v-if="isLoggedIn">
        <PostList />
      </template>
      <div v-else class="empty">
        登录后查看和发布你的文章，
        <router-link to="/admin/login">去登录</router-link>
      </div>
    </main>
  </BlogShell>
</template>

<script setup>
import { computed } from 'vue'
import BlogShell from '../../components/blog/BlogShell.vue'
import PostList from '../admin/PostList.vue'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
</script>
