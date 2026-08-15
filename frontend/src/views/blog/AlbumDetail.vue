<template>
  <BlogShell>
    <main class="simple-page" v-if="group">
      <h1 class="simple-title">{{ group.name }}</h1>
      <p class="simple-sub">
        <router-link :to="`/u/${group.author?.username}`">{{ group.author?.nickname }}</router-link>
        的相册 · {{ photos.length }} 个内容
      </p>

      <div v-if="photos.length" class="album-photo-grid">
        <div v-for="p in photos" :key="p.id" class="album-photo-item">
          <video v-if="p.mediaType === 'video'" :src="p.url" controls preload="metadata" class="album-video"></video>
          <el-image
            v-else
            :src="p.url"
            :preview-src-list="imageList"
            :initial-index="imageIndex(p)"
            fit="cover"
            preview-teleported
            lazy
            class="album-img"
          />
          <div v-if="p.description" class="album-photo-desc">{{ p.description }}</div>
        </div>
      </div>
      <div v-else class="empty">这个相册还是空的</div>

      <div class="album-back">
        <router-link to="/album">← 返回相册大厅</router-link>
      </div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getAlbumDetail } from '../../api'

const route = useRoute()
const group = ref(null)
const photos = ref([])

const imageList = computed(() => photos.value.filter((p) => p.mediaType !== 'video').map((p) => p.url))

const imageIndex = (photo) => {
  let idx = 0
  for (const p of photos.value) {
    if (p.id === photo.id) return idx
    if (p.mediaType !== 'video') idx++
  }
  return 0
}

const load = async () => {
  try {
    const data = await getAlbumDetail(route.params.id)
    group.value = data.group
    photos.value = data.photos
    document.title = `${group.value.name} - 相册`
  } catch (e) {
    group.value = null
    photos.value = []
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>
