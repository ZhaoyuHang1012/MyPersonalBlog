<template>
  <BlogShell>
    <main class="blog-main">
      <div class="content">
        <div class="search-bar">
          <input v-model="keyword" placeholder="搜索文章（标题或正文）…" @keyup.enter="search" />
          <button @click="search">搜索</button>
        </div>

        <div v-if="activeCategory || activeTag" class="filter-tip">
          <span>当前筛选：{{ activeCategory ? '分类' : '标签' }}「{{ currentFilterName }}」</span>
          <a @click="resetFilter">清除筛选</a>
        </div>

        <article v-for="p in posts" :key="p.id" class="post-card">
          <h2>
            <span v-if="p.isTop === 1" class="top-badge">置顶</span>
            <router-link :to="`/post/${p.id}`">{{ p.title }}</router-link>
          </h2>
          <p class="summary">{{ p.summary || '（暂无摘要）' }}</p>
          <div class="meta">
            <span v-if="p.author" class="author-link" @click="$router.push(`/u/${p.author.username}`)">
              ✍️ {{ p.author.nickname }}
            </span>
            <span>{{ formatDate(p.publishedAt) }}</span>
            <span v-if="p.categoryName" class="cat" @click="filterByCategory(p.categoryId)">
              {{ p.categoryName }}
            </span>
            <span v-for="t in p.tags" :key="t.id" class="tag" @click="filterByTag(t.id)">
              # {{ t.name }}
            </span>
            <span class="views">👁 {{ p.viewCount }} 阅读</span>
            <span class="views">💬 {{ p.commentCount }} 评论</span>
          </div>
        </article>

        <div v-if="!posts.length" class="empty">暂无文章</div>

        <div v-if="total > size" class="pager">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            v-model:current-page="page"
            @current-change="loadPosts"
          />
        </div>
      </div>

      <aside class="sidebar">
        <div class="side-card">
          <h3>分类</h3>
          <ul>
            <li
              v-for="c in categories"
              :key="c.id"
              :class="{ active: activeCategory === c.id }"
              @click="filterByCategory(c.id)"
            >
              <span>{{ c.name }}</span>
              <span class="count">{{ c.postCount }}</span>
            </li>
          </ul>
        </div>
        <div class="side-card">
          <h3>标签</h3>
          <div class="tag-cloud">
            <span
              v-for="t in tags"
              :key="t.id"
              :class="{ active: activeTag === t.id }"
              @click="filterByTag(t.id)"
            >{{ t.name }}</span>
          </div>
        </div>
        <div class="side-card about-me">
          <h3>关于</h3>
          <p>这是一个多用户博客平台，欢迎浏览大家分享的文章。</p>
          <router-link to="/register">注册入驻 →</router-link>
        </div>
      </aside>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getPosts, getCategories, getTags, getSite } from '../../api'

const site = ref({ title: '我的博客', author: '博主' })
const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const categories = ref([])
const tags = ref([])
const activeCategory = ref(null)
const activeTag = ref(null)
const keyword = ref('')

const currentFilterName = computed(() => {
  if (activeCategory.value) {
    const c = categories.value.find((i) => i.id === activeCategory.value)
    return c ? c.name : ''
  }
  const t = tags.value.find((i) => i.id === activeTag.value)
  return t ? t.name : ''
})

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD') : '')

const loadPosts = async () => {
  const data = await getPosts({
    page: page.value,
    size: size.value,
    categoryId: activeCategory.value || undefined,
    tagId: activeTag.value || undefined,
    keyword: keyword.value || undefined
  })
  posts.value = data.records
  total.value = data.total
}

const filterByCategory = (id) => {
  activeCategory.value = id
  activeTag.value = null
  page.value = 1
  loadPosts()
}

const filterByTag = (id) => {
  activeTag.value = id
  activeCategory.value = null
  page.value = 1
  loadPosts()
}

const resetFilter = () => {
  activeCategory.value = null
  activeTag.value = null
  page.value = 1
  loadPosts()
}

const search = () => {
  page.value = 1
  loadPosts()
}

onMounted(async () => {
  try {
    site.value = await getSite()
  } catch (e) {
    /* ignore */
  }
  loadPosts()
  try {
    categories.value = await getCategories()
    tags.value = await getTags()
  } catch (e) {
    /* ignore */
  }
})
</script>
