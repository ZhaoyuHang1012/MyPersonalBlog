<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">📷 我的相册</h1>
      <p class="simple-sub">创建相册、上传照片和视频（支持权限设置）</p>
      <template v-if="isLoggedIn">
        <AlbumManage />
      </template>
      <div v-else class="empty">
        登录后查看和管理你的相册，
        <router-link to="/admin/login">去登录</router-link>
      </div>
    </main>
  </BlogShell>
</template>

<script setup>
import { computed } from 'vue'
import BlogShell from '../../components/blog/BlogShell.vue'
import AlbumManage from '../me/AlbumManage.vue'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
</script>
