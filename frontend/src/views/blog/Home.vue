<template>
  <BlogShell>
    <main class="blog-main">
      <div class="content">
        <div class="hall-tabs">
          <span :class="{ active: tab === 'post' }" @click="switchTab('post')">📄 文章</span>
          <span :class="{ active: tab === 'murmur' }" @click="switchTab('murmur')">💭 说说</span>
          <span :class="{ active: tab === 'album' }" @click="switchTab('album')">📷 相册</span>
        </div>

        <template v-if="tab === 'post'">
          <div class="search-bar">
            <input v-model="keyword" placeholder="搜索文章（标题或正文）…" @keyup.enter="search" />
            <button @click="search">搜索</button>
          </div>

          <div v-if="activeCategory || activeTags.length" class="filter-tip">
            <span>
              当前筛选：{{ activeCategory ? '分类「' + currentFilterName + '」' : '' }}
              {{ activeCategory && activeTags.length ? ' + ' : '' }}
              {{ activeTags.length ? '标签「' + activeTagNames + '」' : '' }}
              （点击已选标签可取消）
            </span>
            <a @click="resetFilter">清除全部筛选</a>
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
              <span v-for="t in p.tags" :key="t.id" class="tag" @click="toggleTag(t.id)">
                # {{ t.name }}
              </span>
              <ArchiveButton :target-type="'post'" :target-id="p.id" />
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
        </template>

        <MurmurStream v-else-if="tab === 'murmur'" />
        <AlbumGrid v-else />
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
              :class="{ active: activeTags.includes(t.id) }"
              @click="toggleTag(t.id)"
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
import MurmurStream from '../../components/blog/MurmurStream.vue'
import AlbumGrid from '../../components/blog/AlbumGrid.vue'
import ArchiveButton from '../../components/common/ArchiveButton.vue'
import { getPosts, getCategories, getTags, getSite } from '../../api'

const site = ref({ title: '我的博客', author: '博主' })
const tab = ref('post')
const posts = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const categories = ref([])
const tags = ref([])
const activeCategory = ref(null)
const activeTags = ref([])
const keyword = ref('')

const currentFilterName = computed(() => {
  const c = categories.value.find((i) => i.id === activeCategory.value)
  return c ? c.name : ''
})

const activeTagNames = computed(() =>
  activeTags.value.map((id) => tags.value.find((t) => t.id === id)?.name || '').join('、')
)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD') : '')

const loadPosts = async () => {
  const data = await getPosts({
    page: page.value,
    size: size.value,
    categoryId: activeCategory.value || undefined,
    tagIds: activeTags.value.length ? activeTags.value.join(',') : undefined,
    keyword: keyword.value || undefined
  })
  posts.value = data.records
  total.value = data.total
}

const switchTab = (name) => {
  tab.value = name
}

const filterByCategory = (id) => {
  activeCategory.value = id
  page.value = 1
  tab.value = 'post'
  loadPosts()
}

// 标签多选：再次点击已选中的标签可取消
const toggleTag = (id) => {
  const idx = activeTags.value.indexOf(id)
  if (idx >= 0) {
    activeTags.value.splice(idx, 1)
  } else {
    activeTags.value.push(id)
  }
  page.value = 1
  tab.value = 'post'
  loadPosts()
}

const resetFilter = () => {
  activeCategory.value = null
  activeTags.value = []
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
