<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- 我的好友 -->
      <el-tab-pane :label="`我的好友（${friends.length}）`" name="friends">
        <div v-if="friends.length" class="friend-list">
          <div v-for="f in friends" :key="f.id" class="friend-item">
            <el-avatar :size="44" :src="f.avatar || undefined" style="background: #3a7afe">
              {{ (f.nickname || 'U')[0] }}
            </el-avatar>
            <div class="friend-info">
              <router-link :to="`/u/${f.username}`" class="friend-name">{{ f.nickname }}</router-link>
              <div class="friend-meta">@{{ f.username }}</div>
            </div>
            <el-button link type="danger" size="small" @click="removeFriendHandler(f)">删除好友</el-button>
          </div>
        </div>
        <el-empty v-else description="还没有好友，去大厅逛逛，从别人的主页添加吧" />
      </el-tab-pane>

      <!-- 收到的申请 -->
      <el-tab-pane :label="`收到的申请（${received.length}）`" name="received">
        <div v-if="received.length" class="friend-list">
          <div v-for="r in received" :key="r.id" class="friend-item">
            <el-avatar :size="44" :src="r.fromUser?.avatar || undefined" style="background: #3a7afe">
              {{ (r.fromUser?.nickname || 'U')[0] }}
            </el-avatar>
            <div class="friend-info">
              <router-link :to="`/u/${r.fromUser?.username}`" class="friend-name">
                {{ r.fromUser?.nickname }}
              </router-link>
              <div class="friend-meta">{{ r.message || '请求添加你为好友' }}</div>
            </div>
            <div class="friend-actions">
              <el-button type="primary" size="small" @click="approve(r)">同意</el-button>
              <el-button size="small" @click="rejectHandler(r)">拒绝</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无收到的申请" />
      </el-tab-pane>

      <!-- 发出的申请 -->
      <el-tab-pane :label="`发出的申请（${sent.length}）`" name="sent">
        <div v-if="sent.length" class="friend-list">
          <div v-for="r in sent" :key="r.id" class="friend-item">
            <el-avatar :size="44" :src="r.toUser?.avatar || undefined" style="background: #3a7afe">
              {{ (r.toUser?.nickname || 'U')[0] }}
            </el-avatar>
            <div class="friend-info">
              <router-link :to="`/u/${r.toUser?.username}`" class="friend-name">
                {{ r.toUser?.nickname }}
              </router-link>
              <div class="friend-meta">{{ r.message || '等待对方处理' }}</div>
            </div>
            <el-tag :type="statusTagType(r.status)" size="small">{{ statusText(r.status) }}</el-tag>
          </div>
        </div>
        <el-empty v-else description="暂无发出的申请" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFriends,
  getFriendRequests,
  getSentRequests,
  approveFriendRequest,
  rejectFriendRequest,
  removeFriend
} from '../../api'

const activeTab = ref('friends')
const friends = ref([])
const received = ref([])
const sent = ref([])

const statusText = (s) => ({ 0: '待处理', 1: '已同意', 2: '已拒绝' }[s] || '未知')
const statusTagType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info' }[s] || 'info')

const load = async () => {
  friends.value = await getFriends()
  received.value = await getFriendRequests()
  sent.value = await getSentRequests()
}

const approve = async (r) => {
  await approveFriendRequest(r.id)
  ElMessage.success('已添加为好友')
  load()
}

const rejectHandler = async (r) => {
  await rejectFriendRequest(r.id)
  ElMessage.success('已拒绝')
  load()
}

const removeFriendHandler = async (f) => {
  try {
    await ElMessageBox.confirm(`确定删除好友「${f.nickname}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await removeFriend(f.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.friend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--card-bg);
  border-radius: 10px;
  box-shadow: var(--shadow);
  padding: 14px 18px;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-weight: 600;
  color: var(--text);
  font-size: 15px;
}

.friend-name:hover {
  color: var(--accent);
}

.friend-meta {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.friend-actions {
  display: flex;
  gap: 6px;
}
</style>
