<template>
  <div class="comment-section">
    <h3 class="comment-title">💬 评论（{{ total }}）</h3>

    <!-- 评论列表 -->
    <div v-for="c in comments" :key="c.id" class="comment-item">
      <div class="comment-avatar">{{ c.nickname[0] }}</div>
      <div class="comment-body">
        <div class="comment-head">
          <span class="comment-nickname">{{ c.nickname }}</span>
          <a v-if="c.website" class="comment-site" @click="openSite(c.website)">🔗 网站</a>
          <span class="comment-time">{{ formatDate(c.createdAt) }}</span>
        </div>
        <div class="comment-content">{{ c.content }}</div>
        <div class="comment-actions">
          <a @click="replyTo(c)">回复</a>
        </div>
        <!-- 楼中楼 -->
        <div v-if="c.children && c.children.length" class="comment-children">
          <div v-for="child in c.children" :key="child.id" class="comment-item">
            <div class="comment-avatar small">{{ child.nickname[0] }}</div>
            <div class="comment-body">
              <div class="comment-head">
                <span class="comment-nickname">{{ child.nickname }}</span>
                <span class="comment-time">{{ formatDate(child.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ child.content }}</div>
              <div class="comment-actions">
                <a @click="replyTo(c)">回复</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!comments.length" class="comment-empty">暂无评论，来抢沙发～</div>

    <!-- 提交表单 -->
    <div v-if="allowComments" class="comment-form">
      <h4 v-if="replyTarget" class="reply-tip">
        回复 @{{ replyTarget.nickname }}
        <a class="cancel-reply" @click="replyTarget = null">取消回复</a>
      </h4>
      <div v-if="isLoggedIn" class="login-identity">
        🧑 以「{{ userStore.user?.nickname || '博主' }}」身份评论
      </div>
      <div v-else class="comment-form-row">
        <input v-model="form.nickname" placeholder="昵称 *" maxlength="50" />
        <input v-model="form.email" placeholder="邮箱 *（不会公开）" maxlength="100" />
        <input v-model="form.website" placeholder="网址（可选）" maxlength="200" />
      </div>
      <div class="comment-input-wrap">
        <textarea
          ref="textareaRef"
          v-model="form.content"
          :maxlength="1000"
          placeholder="说点什么吧…"
          rows="3"
        ></textarea>
        <EmojiPanel @pick="insertEmoji" />
      </div>
      <div class="comment-form-actions">
        <span class="comment-count">{{ form.content.length }}/1000</span>
        <button class="comment-submit" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : '发表评论' }}
        </button>
      </div>
    </div>
    <div v-else class="comment-closed">评论功能已关闭</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import EmojiPanel from './EmojiPanel.vue'
import { getPostComments, submitComment, getSite } from '../../api'
import { useUserStore } from '../../store/user'

const props = defineProps({
  postId: { type: [String, Number], required: true }
})

const userStore = useUserStore()
const comments = ref([])
const total = ref(0)
const replyTarget = ref(null)
const submitting = ref(false)
const allowComments = ref(true)
const form = ref({ nickname: '', email: '', website: '', content: '' })
const textareaRef = ref(null)

const isLoggedIn = computed(() => !!userStore.token)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  comments.value = await getPostComments(props.postId)
  total.value = comments.value.reduce((n, c) => n + 1 + (c.children?.length || 0), 0)
}

const replyTo = (c) => {
  replyTarget.value = c
  nextTick(() => textareaRef.value?.focus())
}

const insertEmoji = (emoji) => {
  form.value.content += emoji
}

const openSite = (url) => {
  const u = /^https?:\/\//i.test(url) ? url : `http://${url}`
  window.open(u, '_blank')
}

const submit = async () => {
  if (!form.value.content.trim()) {
    ElMessage.warning('请填写评论内容')
    return
  }
  // 未登录访客需校验身份信息；登录用户由后端自动识别
  if (!isLoggedIn.value) {
    if (!form.value.nickname.trim()) {
      ElMessage.warning('请填写昵称')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email.trim())) {
      ElMessage.warning('请填写正确的邮箱')
      return
    }
  }
  submitting.value = true
  try {
    const payload = {
      content: form.value.content.trim(),
      parentId: replyTarget.value?.id || null
    }
    if (!isLoggedIn.value) {
      payload.nickname = form.value.nickname.trim()
      payload.email = form.value.email.trim()
      payload.website = form.value.website.trim() || null
    }
    await submitComment(props.postId, payload)
    ElMessage.success('评论成功')
    form.value.content = ''
    replyTarget.value = null
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  load()
  try {
    const site = await getSite()
    allowComments.value = site.allowComments === 1
  } catch (e) {
    /* ignore */
  }
})
watch(() => props.postId, load)
</script>
