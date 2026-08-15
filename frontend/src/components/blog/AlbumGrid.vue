<template>
  <div>
    <div v-if="groups.length" class="album-group-grid">
      <div v-for="g in groups" :key="g.id" class="album-group-card">
        <router-link :to="`/album/${g.id}`" class="album-group-link">
          <img v-if="g.cover" :src="g.cover" :alt="g.name" loading="lazy" />
          <div v-else class="album-no-cover">📷</div>
          <div class="album-group-info">
            <div class="album-group-name">{{ g.name }}</div>
            <div class="album-group-meta">
              <el-avatar
                :size="18"
                :src="g.author?.avatar || undefined"
                style="background: #3a7afe; vertical-align: middle"
              >{{ (g.author?.nickname || 'U')[0] }}</el-avatar>
              {{ g.author?.nickname }} · {{ g.photoCount }} 个内容
            </div>
          </div>
        </router-link>
        <div class="album-group-actions">
          <FavoriteButton :target-type="'album'" :target-id="g.id" />
        </div>
      </div>
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
