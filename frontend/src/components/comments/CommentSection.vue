<template>
  <div class="comment-section">
    <h3 class="comment-title">💬 评论（{{ total }}）</h3>

    <!-- 评论列表 -->
    <div v-for="c in comments" :key="c.id" class="comment-item">
      <router-link
        v-if="c.username"
        :to="`/u/${c.username}`"
        class="comment-avatar-link"
        :title="`访问 ${c.nickname} 的主页`"
      >
        <el-avatar class="comment-avatar" :size="38" :src="c.avatar || undefined">
          {{ (c.nickname || '?')[0] }}
        </el-avatar>
      </router-link>
      <el-avatar v-else class="comment-avatar" :size="38" :src="c.avatar || undefined">
        {{ (c.nickname || '?')[0] }}
      </el-avatar>
      <div class="comment-body">
        <div class="comment-head">
          <router-link v-if="c.username" :to="`/u/${c.username}`" class="comment-nickname link">
            {{ c.nickname }}
          </router-link>
          <span v-else class="comment-nickname">{{ c.nickname }}</span>
          <span v-if="isAuthor(c)" class="up-badge">UP</span>
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
            <router-link
              v-if="child.username"
              :to="`/u/${child.username}`"
              class="comment-avatar-link"
              :title="`访问 ${child.nickname} 的主页`"
            >
              <el-avatar class="comment-avatar small" :size="30" :src="child.avatar || undefined">
                {{ (child.nickname || '?')[0] }}
              </el-avatar>
            </router-link>
            <el-avatar v-else class="comment-avatar small" :size="30" :src="child.avatar || undefined">
              {{ (child.nickname || '?')[0] }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-head">
                <router-link v-if="child.username" :to="`/u/${child.username}`" class="comment-nickname link">
                  {{ child.nickname }}
                </router-link>
                <span v-else class="comment-nickname">{{ child.nickname }}</span>
                <span v-if="isAuthor(child)" class="up-badge">UP</span>
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

    <!-- 未登录：引导登录 -->
    <div v-if="allowComments && !isLoggedIn" class="comment-login-tip" @click="goLogin">
      🔒 登录后参与评论
    </div>

    <!-- 发表表单（仅登录用户，身份自动识别） -->
    <div v-else-if="allowComments" class="comment-form">
      <h4 v-if="replyTarget" class="reply-tip">
        回复 @{{ replyTarget.nickname }}
        <a class="cancel-reply" @click="replyTarget = null">取消回复</a>
      </h4>
      <div class="login-identity">
        🧑 以「{{ userStore.user?.nickname || '博主' }}」身份评论
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import EmojiPanel from './EmojiPanel.vue'
import { getPostComments, submitComment, getMurmurComments, submitMurmurComment, getSite } from '../../api'
import { useUserStore } from '../../store/user'

const props = defineProps({
  postId: { type: [String, Number], default: null },
  /** 说说评论：传入 murmurId 时进入说说评论模式（与 postId 二选一） */
  murmurId: { type: [String, Number], default: null },
  authorId: { type: [String, Number], default: null }
})

const isMurmur = computed(() => props.murmurId != null)

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const comments = ref([])
const total = ref(0)
const replyTarget = ref(null)
const submitting = ref(false)
const allowComments = ref(true)
const form = ref({ content: '' })
const textareaRef = ref(null)

const isLoggedIn = computed(() => !!userStore.token)

/** 判断评论是否由文章作者发布 */
const isAuthor = (comment) => {
  return props.authorId != null && comment.userId != null &&
    String(comment.userId) === String(props.authorId)
}

/** 未登录点击 → 跳转登录页，登录后返回当前文章 */
const goLogin = () => {
  router.push({ path: '/admin/login', query: { redirect: route.fullPath } })
}

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  const list = isMurmur.value
    ? await getMurmurComments(props.murmurId)
    : await getPostComments(props.postId)
  comments.value = list
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
  submitting.value = true
  try {
    const payload = {
      content: form.value.content.trim(),
      parentId: replyTarget.value?.id || null
    }
    if (isMurmur.value) {
      await submitMurmurComment(props.murmurId, payload)
    } else {
      await submitComment(props.postId, payload)
    }
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
watch(() => [props.postId, props.murmurId], load)
</script>
