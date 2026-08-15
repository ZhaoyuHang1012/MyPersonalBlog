<template>
  <button
    class="like-btn"
    :class="{ liked: liked }"
    :title="liked ? '取消点赞' : '点赞'"
    @click.stop="toggle"
  >
    {{ liked ? '❤️' : '🤍' }} <span class="like-count">{{ count }}</span>
  </button>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLikeStatus, addLike, removeLike } from '../../api'
import { useUserStore } from '../../store/user'

const props = defineProps({
  targetType: { type: String, required: true },
  targetId: { type: [Number, String], required: true }
})

const emit = defineEmits(['changed'])

const router = useRouter()
const userStore = useUserStore()
const loggedIn = computed(() => !!userStore.token)
const liked = ref(false)
const count = ref(0)

const toggle = async () => {
  if (!loggedIn.value) {
    ElMessage.warning('请先登录后再点赞')
    router.push({ path: '/admin/login', query: { redirect: window.location.pathname } })
    return
  }
  try {
    if (liked.value) {
      await removeLike(props.targetType, props.targetId)
      count.value = Math.max(0, count.value - 1)
    } else {
      await addLike({ targetType: props.targetType, targetId: props.targetId })
      count.value += 1
    }
    liked.value = !liked.value
    emit('changed', { liked: liked.value, count: count.value })
  } catch (e) {
    /* 拦截器已提示 */
  }
}

onMounted(async () => {
  try {
    const data = await getLikeStatus(props.targetType, props.targetId)
    liked.value = data.liked
    count.value = data.count
  } catch (e) {
    /* ignore */
  }
})
</script>

<style scoped>
.like-btn {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.like-btn:hover {
  color: #e74c3c;
  background: #fdf0ef;
}

.like-btn.liked {
  color: #e74c3c;
}

.like-count {
  font-size: 13px;
}
</style>
