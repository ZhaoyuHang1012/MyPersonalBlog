<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">💭 我的说说</h1>
      <p class="simple-sub">记录自己的日常动态（支持快捷编辑与权限设置）</p>
      <template v-if="isLoggedIn">
        <MurmurManage />
      </template>
      <div v-else class="empty">
        登录后查看和发布你的说说，
        <router-link to="/admin/login">去登录</router-link>
      </div>
    </main>
  </BlogShell>
</template>

<script setup>
import { computed } from 'vue'
import BlogShell from '../../components/blog/BlogShell.vue'
import MurmurManage from '../me/MurmurManage.vue'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
</script>
