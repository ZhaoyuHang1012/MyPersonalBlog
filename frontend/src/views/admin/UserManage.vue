<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <div class="toolbar">
      <span class="tip">管理员最高权限：可修改任意用户资料、分配角色、重置密码、管理好友关系</span>
      <div class="spacer"></div>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="32" :src="row.avatar || undefined" style="background: #3a7afe">
              {{ (row.nickname || 'U')[0] }}
            </el-avatar>
            <div>
              <div class="user-nick">{{ row.nickname }}</div>
              <div class="user-name">@{{ row.username }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="存储配额" width="110">
        <template #default="{ row }">
          {{ formatSize(row.quota) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" size="small" @click="openPassword(row)">重置密码</el-button>
          <el-button link type="primary" size="small" @click="openFriends(row)">好友关系</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="editVisible" :title="`编辑用户：${editForm.username}`" width="460px">
      <el-form label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" maxlength="50" />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="editForm.avatar" placeholder="/uploads/... 或 http(s)://..." />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="editForm.role">
            <el-radio value="USER">普通用户</el-radio>
            <el-radio value="ADMIN">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="存储配额">
          <el-input-number v-model="editForm.quotaMb" :min="1" :max="10240" :step="100" style="width: 180px" />
          <span class="tip-inline">MB</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdVisible" :title="`重置密码：${pwdUsername}`" width="400px">
      <el-input v-model="newPassword" type="password" show-password placeholder="新密码（至少 6 位）" maxlength="50" />
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="warning" :loading="saving" @click="savePassword">重置</el-button>
      </template>
    </el-dialog>

    <!-- 好友关系弹窗 -->
    <el-dialog v-model="friendsVisible" :title="`好友关系：${friendsUser?.nickname || ''}`" width="520px">
      <div v-if="friends.length" class="friend-list">
        <div v-for="f in friends" :key="f.id" class="friend-item">
          <el-avatar :size="36" :src="f.avatar || undefined" style="background: #3a7afe">
            {{ (f.nickname || 'U')[0] }}
          </el-avatar>
          <div class="friend-info">
            <div class="friend-name">{{ f.nickname }}</div>
            <div class="friend-meta">@{{ f.username }}</div>
          </div>
          <el-button link type="danger" size="small" @click="removeFriendHandler(f)">
            解除关系
          </el-button>
        </div>
      </div>
      <el-empty v-else description="该用户还没有好友" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminListUsers,
  adminUpdateUser,
  adminResetPassword,
  adminGetUserFriends,
  adminRemoveUserFriend
} from '../../api'

const users = ref([])
const loading = ref(false)
const saving = ref(false)

const editVisible = ref(false)
const editForm = ref({ id: null, username: '', nickname: '', avatar: '', role: 'USER', quotaMb: 1024 })

const pwdVisible = ref(false)
const pwdUserId = ref(null)
const pwdUsername = ref('')
const newPassword = ref('')

const friendsVisible = ref(false)
const friendsUser = ref(null)
const friends = ref([])

const formatSize = (bytes) => {
  if (!bytes) return '0 MB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(0) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}

const load = async () => {
  loading.value = true
  try {
    users.value = await adminListUsers()
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  editForm.value = {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    avatar: row.avatar || '',
    role: row.role || 'USER',
    quotaMb: Math.round((row.quota || 1073741824) / 1024 / 1024)
  }
  editVisible.value = true
}

const saveEdit = async () => {
  if (!editForm.value.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    await adminUpdateUser(editForm.value.id, {
      nickname: editForm.value.nickname.trim(),
      avatar: editForm.value.avatar.trim() || null,
      role: editForm.value.role,
      quota: editForm.value.quotaMb * 1024 * 1024
    })
    ElMessage.success('已保存')
    editVisible.value = false
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

const openPassword = (row) => {
  pwdUserId.value = row.id
  pwdUsername.value = row.nickname
  newPassword.value = ''
  pwdVisible.value = true
}

const savePassword = async () => {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  saving.value = true
  try {
    await adminResetPassword(pwdUserId.value, { newPassword: newPassword.value })
    ElMessage.success('密码已重置')
    pwdVisible.value = false
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

const openFriends = async (row) => {
  friendsUser.value = row
  friendsVisible.value = true
  friends.value = await adminGetUserFriends(row.id)
}

const removeFriendHandler = async (f) => {
  try {
    await ElMessageBox.confirm(
      `确定解除「${friendsUser.value.nickname}」与「${f.nickname}」的好友关系吗？`,
      '解除确认',
      { type: 'warning', confirmButtonText: '解除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  await adminRemoveUserFriend(friendsUser.value.id, f.id)
  ElMessage.success('已解除好友关系')
  friends.value = await adminGetUserFriends(friendsUser.value.id)
}

onMounted(load)
</script>

<style scoped>
.tip {
  font-size: 13px;
  color: #909399;
}

.tip-inline {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-nick {
  font-weight: 600;
}

.user-name {
  font-size: 12px;
  color: #909399;
}

.friend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 360px;
  overflow-y: auto;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 14px;
}

.friend-info {
  flex: 1;
}

.friend-name {
  font-weight: 600;
  font-size: 14px;
}

.friend-meta {
  font-size: 12px;
  color: #909399;
}
</style>
