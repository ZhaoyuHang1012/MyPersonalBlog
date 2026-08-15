import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/blog/Home.vue') },
  { path: '/post/:id', component: () => import('../views/blog/PostDetail.vue') },
  { path: '/about', component: () => import('../views/blog/About.vue') },
  { path: '/archive', component: () => import('../views/blog/Archive.vue') },
  { path: '/links', component: () => import('../views/blog/Links.vue') },
  { path: '/murmur', component: () => import('../views/blog/Murmur.vue') },
  { path: '/album', component: () => import('../views/blog/Album.vue') },
  { path: '/admin/login', component: () => import('../views/admin/Login.vue') },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
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
      { path: 'murmurs', component: () => import('../views/admin/MurmurManage.vue') },
      { path: 'albums', component: () => import('../views/admin/AlbumManage.vue') },
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
})

export default router
