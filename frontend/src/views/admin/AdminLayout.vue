<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">📝 博客管理</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1f2937"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/posts">
          <el-icon><Document /></el-icon><span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Folder /></el-icon><span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon><span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon><span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/media">
          <el-icon><Picture /></el-icon><span>媒体库</span>
        </el-menu-item>
        <el-menu-item index="/admin/links">
          <el-icon><Link /></el-icon><span>友链管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/murmurs">
          <el-icon><ChatLineSquare /></el-icon><span>说说管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/logs">
          <el-icon><List /></el-icon><span>操作日志</span>
        </el-menu-item>
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon><span>站点设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="header-title">个人博客管理后台</div>
        <el-dropdown @command="onCommand">
          <span class="user-info">
            <el-avatar :size="30" :src="userStore.user?.avatar || undefined" style="background: #3a7afe">
              {{ (userStore.user?.nickname || 'A')[0] }}
            </el-avatar>
            <span>{{ userStore.user?.nickname || '管理员' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人设置</el-dropdown-item>
              <el-dropdown-item command="home">查看前台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  userStore.fetchMe().catch(() => {})
})

const onCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/admin/login')
  } else if (cmd === 'home') {
    window.open('/', '_blank')
  } else if (cmd === 'profile') {
    router.push('/admin/profile')
  }
}
</script>
