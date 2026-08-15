<template>
  <div>
    <el-tabs v-model="activeType" @tab-change="load">
      <el-tab-pane label="📄 文章" name="post" />
      <el-tab-pane label="💭 说说" name="murmur" />
      <el-tab-pane label="📷 相册" name="album" />
    </el-tabs>

    <!-- 归档文章 -->
    <template v-if="activeType === 'post'">
      <article v-for="p in data.posts" :key="p.id" class="post-card archive-row">
        <h2>
          <router-link :to="`/post/${p.id}`">{{ p.title }}</router-link>
        </h2>
        <p class="summary">{{ p.summary || '（暂无摘要）' }}</p>
        <div class="meta">
          <span v-if="p.author" class="author-link" @click="$router.push(`/u/${p.author.username}`)">
            ✍️ {{ p.author.nickname }}
          </span>
          <span>{{ formatDate(p.publishedAt) }}</span>
          <ArchiveButton :target-type="'post'" :target-id="p.id" />
        </div>
      </article>
      <el-empty v-if="!data.posts.length" description="还没有归档文章" />
    </template>

    <!-- 归档说说 -->
    <template v-else-if="activeType === 'murmur'">
      <div v-for="m in data.murmurs" :key="m.id" class="murmur-card archive-row">
        <div class="murmur-head">
          <el-avatar :size="36" :src="m.author?.avatar || undefined" style="background: #3a7afe">
            {{ (m.author?.nickname || 'U')[0] }}
          </el-avatar>
          <div class="murmur-head-info">
            <span class="murmur-author">{{ m.author?.nickname }}</span>
            <div class="murmur-time">{{ formatDate(m.createdAt) }}</div>
          </div>
          <ArchiveButton :target-type="'murmur'" :target-id="m.id" style="margin-left: auto" />
        </div>
        <div class="murmur-content-text">{{ m.content }}</div>
      </div>
      <el-empty v-if="!data.murmurs.length" description="还没有归档说说" />
    </template>

    <!-- 归档相册 -->
    <template v-else>
      <div v-if="data.albums.length" class="album-group-grid">
        <div v-for="g in data.albums" :key="g.id" class="album-group-card-wrap">
          <router-link :to="`/album/${g.id}`" class="album-group-card">
            <img v-if="g.cover" :src="g.cover" :alt="g.name" loading="lazy" />
            <div v-else class="album-no-cover">📷</div>
            <div class="album-group-info">
              <div class="album-group-name">{{ g.name }}</div>
              <div class="album-group-meta">{{ g.author?.nickname }} · {{ g.photoCount }} 个内容</div>
            </div>
          </router-link>
          <div class="archive-btn-row">
            <ArchiveButton :target-type="'album'" :target-id="g.id" />
          </div>
        </div>
      </div>
      <el-empty v-else description="还没有归档相册" />
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import ArchiveButton from '../../components/common/ArchiveButton.vue'
import { getArchives } from '../../api'

const activeType = ref('post')
const data = reactive({ posts: [], murmurs: [], albums: [] })

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  const result = await getArchives(activeType.value)
  data.posts = result.posts || []
  data.murmurs = result.murmurs || []
  data.albums = result.albums || []
}

onMounted(load)
</script>

<style scoped>
.archive-row {
  position: relative;
}

.archive-btn-row {
  padding: 8px 12px 12px;
}
</style>
