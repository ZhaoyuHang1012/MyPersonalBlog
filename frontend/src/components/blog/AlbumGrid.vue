<template>
  <div>
    <div v-if="groups.length" class="album-group-grid">
      <router-link v-for="g in groups" :key="g.id" :to="`/album/${g.id}`" class="album-group-card">
        <img v-if="g.cover" :src="g.cover" :alt="g.name" loading="lazy" />
        <div v-else class="album-no-cover">📷</div>
        <div class="album-group-info">
          <div class="album-group-name">{{ g.name }}</div>
          <div class="album-group-meta">{{ g.author?.nickname }} · {{ g.photoCount }} 个内容</div>
          <FavoriteButton :target-type="'album'" :target-id="g.id" />
        </div>
      </router-link>
    </div>
    <div v-else class="empty">还没有公开相册</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import FavoriteButton from '../common/FavoriteButton.vue'
import { getAlbums } from '../../api'

const groups = ref([])

onMounted(async () => {
  groups.value = await getAlbums()
})
</script>
