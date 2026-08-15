<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">📷 相册</h1>
      <p class="simple-sub">记录生活的瞬间</p>

      <div class="album-grid">
        <div v-for="(a, idx) in albums" :key="a.id" class="album-item">
          <el-image
            :src="a.url"
            :preview-src-list="albums.map((x) => x.url)"
            :initial-index="idx"
            fit="cover"
            preview-teleported
            lazy
          />
          <div v-if="a.description" class="album-desc">{{ a.description }}</div>
        </div>
      </div>
      <div v-if="!albums.length" class="empty">相册还是空的</div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getAlbums } from '../../api'

const albums = ref([])

onMounted(async () => {
  albums.value = await getAlbums()
})
</script>
