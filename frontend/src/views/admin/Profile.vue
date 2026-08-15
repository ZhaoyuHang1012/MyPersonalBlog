<template>
  <div>
    <h2 class="page-title">个人设置</h2>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <h3 class="card-title">基本资料</h3>
          <el-form label-width="80px">
            <el-form-item label="头像">
              <div class="avatar-row">
                <el-avatar :size="64" :src="profile.avatar || undefined">
                  {{ (profile.nickname || 'A')[0] }}
                </el-avatar>
                <el-upload :show-file-list="false" :http-request="doUploadAvatar" accept="image/*">
                  <el-button size="small">上传新头像</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profile.nickname" maxlength="50" show-word-limit />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input :model-value="profile.username" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <h3 class="card-title">修改密码</h3>
          <el-form label-width="80px">
            <el-form-item label="原密码">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-50 位" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwd.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="changing" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMe, updateProfile, updatePassword, uploadFile } from '../../api'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

const profile = ref({ nickname: '', username: '', avatar: '' })
const pwd = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const saving = ref(false)
const changing = ref(false)

onMounted(async () => {
  const me = await getMe()
  profile.value = { nickname: me.nickname, username: me.username, avatar: me.avatar || '' }
})

const doUploadAvatar = async ({ file, onSuccess, onError }) => {
  try {
    const data = await uploadFile(file)
    profile.value.avatar = data.url
    ElMessage.success('头像已选择，点击「保存资料」生效')
    onSuccess()
  } catch (e) {
    onError(e)
  }
}

const saveProfile = async () => {
  if (!profile.value.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    const me = await updateProfile({
      nickname: profile.value.nickname.trim(),
      avatar: profile.value.avatar || null
    })
    userStore.user = me
    localStorage.setItem('blog_user', JSON.stringify(me))
    ElMessage.success('资料已保存')
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (!pwd.value.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!pwd.value.newPassword || pwd.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (pwd.value.newPassword !== pwd.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  changing.value = true
  try {
    await updatePassword({
      oldPassword: pwd.value.oldPassword,
      newPassword: pwd.value.newPassword
    })
    ElMessage.success('密码修改成功，请使用新密码重新登录')
    userStore.logout()
    router.push('/')
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    changing.value = false
  }
}
</script>

<style scoped>
.card-title {
  margin-bottom: 16px;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
}
</style>
