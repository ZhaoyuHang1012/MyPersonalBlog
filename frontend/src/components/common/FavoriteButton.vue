<template>
  <button
    v-if="loggedIn"
    class="favorite-btn"
    :class="{ favorited: favorited }"
    :title="favorited ? '取消收藏' : '收藏'"
    @click.stop="toggle"
  >
    {{ favorited ? '⭐ 已收藏' : '☆ 收藏' }}
  </button>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavoriteStatus, addFavorite, removeFavorite } from '../../api'
import { useUserStore } from '../../store/user'

const props = defineProps({
  targetType: { type: String, required: true },
  targetId: { type: [Number, String], required: true }
})

const userStore = useUserStore()
const loggedIn = computed(() => !!userStore.token)
const favorited = ref(false)

const toggle = async () => {
  try {
    if (favorited.value) {
      await removeFavorite(props.targetType, props.targetId)
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite({ targetType: props.targetType, targetId: props.targetId })
      ElMessage.success('已收藏')
    }
    favorited.value = !favorited.value
  } catch (e) {
    /* 拦截器已提示 */
  }
}

onMounted(async () => {
  if (!loggedIn.value) return
  try {
    const data = await getFavoriteStatus(props.targetType, props.targetId)
    favorited.value = data.archived
  } catch (e) {
    /* ignore */
  }
})
</script>

<style scoped>
.favorite-btn {
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-secondary);
  border-radius: 999px;
  padding: 3px 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-btn:hover {
  border-color: #f5a623;
  color: #f5a623;
}

.favorite-btn.favorited {
  background: #fff7e6;
  border-color: #f5a623;
  color: #f5a623;
}
</style>
