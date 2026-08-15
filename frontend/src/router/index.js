import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/blog/Home.vue') },
  { path: '/post/:id', component: () => import('../views/blog/PostDetail.vue') },
  { path: '/about', component: () => import('../views/blog/About.vue') },
  { path: '/archive', component: () => import('../views/blog/ArchivePage.vue'), meta: { requiresAuth: true } },
  { path: '/links', component: () => import('../views/blog/Links.vue') },
  { path: '/murmur', component: () => import('../views/blog/Murmur.vue') },
  { path: '/album', component: () => import('../views/blog/Album.vue') },
  { path: '/album/:id', component: () => import('../views/blog/AlbumDetail.vue') },
  { path: '/u/:username', component: () => import('../views/blog/UserBlog.vue') },
  { path: '/register', component: () => import('../views/auth/Register.vue') },
  // 个人中心（前台风格，登录用户管理自己的博客）
  {
    path: '/me',
    component: () => import('../components/blog/MeShell.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/me/posts' },
      { path: 'posts', component: () => import('../views/admin/PostList.vue') },
      { path: 'posts/new', component: () => import('../views/admin/PostEdit.vue') },
      { path: 'posts/:id/edit', component: () => import('../views/admin/PostEdit.vue') },
      { path: 'murmurs', component: () => import('../views/me/MurmurManage.vue') },
      { path: 'albums', component: () => import('../views/me/AlbumManage.vue') },
      { path: 'friends', component: () => import('../views/me/Friends.vue') },
      { path: 'archives', component: () => import('../views/me/Archives.vue') },
      { path: 'media', component: () => import('../views/admin/MediaLibrary.vue') },
      { path: 'settings', component: () => import('../views/admin/Profile.vue') }
    ]
  },
  { path: '/admin/login', component: () => import('../views/admin/Login.vue') },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'posts', component: () => import('../views/admin/PostList.vue') },
      { path: 'posts/new', component: () => import('../views/admin/PostEdit.vue') },
      { path: 'posts/:id/edit', component: () => import('../views/admin/PostEdit.vue') },
      { path: 'categories', component: () => import('../views/admin/CategoryManage.vue') },
      { path: 'tags', component: () => import('../views/admin/TagManage.vue') },
      { path: 'comments', component: () => import('../views/admin/CommentManage.vue') },
      { path: 'settings', component: () => import('../views/admin/Settings.vue') },
      { path: 'media', component: () => import('../views/admin/MediaLibrary.vue') },
      { path: 'links', component: () => import('../views/admin/LinkManage.vue') },
      { path: 'murmurs', component: () => import('../views/me/MurmurManage.vue') },
      { path: 'invites', component: () => import('../views/admin/InviteManage.vue') },
      { path: 'logs', component: () => import('../views/admin/OperationLogs.vue') },
      { path: 'profile', component: () => import('../views/admin/Profile.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.matched.some((r) => r.meta.requiresAuth)) {
    const token = localStorage.getItem('blog_token')
    if (!token) {
      return { path: '/admin/login', query: { redirect: to.fullPath } }
    }
  }
  // 管理后台仅管理员可进，普通用户引导到个人中心
  if (to.matched.some((r) => r.meta.requiresAdmin)) {
    const user = JSON.parse(localStorage.getItem('blog_user') || 'null')
    if (user?.role !== 'ADMIN') {
      return '/me/posts'
    }
  }
})

export default router
