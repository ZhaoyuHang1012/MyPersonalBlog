<template>
  <BlogShell>
    <main class="simple-page">
      <h1 class="simple-title">🔗 友情链接</h1>
      <p class="simple-sub">欢迎互加友链，可以在下方提交申请</p>

      <div class="link-grid">
        <a v-for="l in links" :key="l.id" :href="l.url" target="_blank" rel="noopener" class="link-card">
          <div class="link-name">{{ l.name }}</div>
          <div class="link-desc">{{ l.description || '暂无描述' }}</div>
          <div class="link-url">{{ l.url }}</div>
        </a>
      </div>
      <div v-if="!links.length" class="empty">暂无友链，欢迎成为第一个！</div>

      <div class="link-apply">
        <h2>申请友链</h2>
        <p class="apply-tip">提交后需站长审核通过才会展示</p>
        <div class="apply-row">
          <input v-model="form.name" placeholder="站点名称 *" maxlength="50" />
          <input v-model="form.url" placeholder="站点地址 *（https:// 开头）" maxlength="255" />
        </div>
        <input v-model="form.description" class="apply-desc" placeholder="站点描述（可选）" maxlength="200" />
        <button class="comment-submit" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : '提交申请' }}
        </button>
      </div>
    </main>
  </BlogShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import BlogShell from '../../components/blog/BlogShell.vue'
import { getLinks, applyLink } from '../../api'

const links = ref([])
const submitting = ref(false)
const form = ref({ name: '', url: '', description: '' })

onMounted(async () => {
  links.value = await getLinks()
})

const submit = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请填写站点名称')
    return
  }
  if (!/^https?:\/\//i.test(form.value.url.trim())) {
    ElMessage.warning('站点地址需以 http:// 或 https:// 开头')
    return
  }
  submitting.value = true
  try {
    await applyLink({
      name: form.value.name.trim(),
      url: form.value.url.trim(),
      description: form.value.description.trim() || null
    })
    ElMessage.success('申请已提交，审核通过后将展示')
    form.value = { name: '', url: '', description: '' }
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    submitting.value = false
  }
}
</script>
