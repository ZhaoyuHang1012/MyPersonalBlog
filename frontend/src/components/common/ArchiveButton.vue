<template>
  <button
    v-if="loggedIn"
    class="archive-btn"
    :class="{ archived: archived }"
    :title="archived ? '取消归档' : '归档收藏'"
    @click.stop="toggle"
  >
    {{ archived ? '⭐ 已归档' : '☆ 归档' }}
  </button>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getArchiveStatus, addArchive, removeArchive } from '../../api'
import { useUserStore } from '../../store/user'

const props = defineProps({
  targetType: { type: String, required: true },
  targetId: { type: [Number, String], required: true }
})

const userStore = useUserStore()
const loggedIn = computed(() => !!userStore.token)
const archived = ref(false)

const toggle = async () => {
  try {
    if (archived.value) {
      await removeArchive(props.targetType, props.targetId)
      ElMessage.success('已取消归档')
    } else {
      await addArchive({ targetType: props.targetType, targetId: props.targetId })
      ElMessage.success('已归档')
    }
    archived.value = !archived.value
  } catch (e) {
    /* 拦截器已提示 */
  }
}

onMounted(async () => {
  if (!loggedIn.value) return
  try {
    const data = await getArchiveStatus(props.targetType, props.targetId)
    archived.value = data.archived
  } catch (e) {
    /* ignore */
  }
})
</script>

<style scoped>
.archive-btn {
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-secondary);
  border-radius: 999px;
  padding: 3px 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.archive-btn:hover {
  border-color: #f5a623;
  color: #f5a623;
}

.archive-btn.archived {
  background: #fff7e6;
  border-color: #f5a623;
  color: #f5a623;
}
</style>
