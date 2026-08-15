<template>
  <div class="login-page">
    <div class="login-card" style="width: 420px">
      <h2>注册账号</h2>
      <p class="sub">需要邀请码才能注册，可联系管理员获取</p>
      <el-form :model="form" @keyup.enter="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名（3-20 位字母/数字/下划线）" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称（展示用）" size="large">
            <template #prefix><el-icon><Postcard /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码（至少 6 位）" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.inviteCode" placeholder="邀请码" size="large">
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onSubmit">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <p class="tip">
        已有账号？
        <router-link to="/admin/login">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '', inviteCode: '' })
const loading = ref(false)

const onSubmit = async () => {
  if (!/^[a-zA-Z0-9_]{3,20}$/.test(form.username.trim())) {
    ElMessage.warning('用户名需为 3-20 位字母、数字或下划线')
    return
  }
  if (!form.nickname.trim()) {
    ElMessage.warning('请填写昵称')
    return
  }
  if (!form.password || form.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (!form.inviteCode.trim()) {
    ElMessage.warning('请填写邀请码')
    return
  }
  loading.value = true
  try {
    await userStore.register({
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      password: form.password,
      inviteCode: form.inviteCode.trim()
    })
    ElMessage.success('注册成功，欢迎加入！')
    router.push('/admin/posts')
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.tip {
  text-align: center;
  font-size: 13px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
