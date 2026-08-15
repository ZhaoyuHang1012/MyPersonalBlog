<template>
  <div>
    <div v-for="m in murmurs" :key="m.id" class="murmur-card">
      <div class="murmur-head">
        <el-avatar :size="38" :src="m.author?.avatar || undefined" style="background: #3a7afe">
          {{ (m.author?.nickname || 'U')[0] }}
        </el-avatar>
        <div class="murmur-head-info">
          <router-link :to="`/u/${m.author?.username}`" class="murmur-author">
            {{ m.author?.nickname }}
          </router-link>
          <div class="murmur-time">{{ formatDate(m.createdAt) }}</div>
        </div>
        <div class="murmur-actions-right">
          <LikeButton :target-type="'murmur'" :target-id="m.id" />
          <FavoriteButton :target-type="'murmur'" :target-id="m.id" />
        </div>
      </div>
      <div class="murmur-content-text">{{ m.content }}</div>
      <div v-if="m.images && m.images.length" class="murmur-images" :class="'count-' + Math.min(m.images.length, 9)">
        <el-image
          v-for="(img, i) in m.images"
          :key="i"
          :src="img"
          :preview-src-list="m.images"
          :initial-index="i"
          fit="cover"
          preview-teleported
          lazy
          class="murmur-img"
        />
      </div>
    </div>

    <div v-if="!murmurs.length" class="empty">还没有公开说说</div>

    <div v-if="total > size" class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        v-model:current-page="page"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import FavoriteButton from '../common/FavoriteButton.vue'
import LikeButton from '../common/LikeButton.vue'
import { getMurmurs } from '../../api'

const murmurs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  const data = await getMurmurs({ page: page.value, size: size.value })
  murmurs.value = data.records
  total.value = data.total
}

onMounted(load)
</script>
