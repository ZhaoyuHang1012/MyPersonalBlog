<template>
  <div class="login-page">
    <div class="login-card">
      <h2>欢迎登录</h2>
      <p class="sub">选择身份类型后输入账号密码</p>

      <div class="login-mode">
        <span :class="{ active: mode === 'user' }" @click="mode = 'user'">👤 普通用户</span>
        <span :class="{ active: mode === 'admin' }" @click="mode = 'admin'">🛠 管理员</span>
      </div>

      <el-form :model="form" @keyup.enter="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onSubmit">
            {{ mode === 'admin' ? '进入管理后台' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <p class="tip">
        <template v-if="mode === 'user'">
          没有账号？<router-link to="/register">使用邀请码注册</router-link>
        </template>
        <template v-else>
          默认管理员账号：admin / admin123
        </template>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 登录身份模式：user 普通用户 / admin 管理员
const mode = ref('user')
const form = reactive({ username: '', password: '' })
const loading = ref(false)

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    // 管理员模式：校验账号角色
    if (mode.value === 'admin' && userStore.user?.role !== 'ADMIN') {
      userStore.logout()
      ElMessage.error('该账号不是管理员，请使用「普通用户」登录')
      return
    }
    ElMessage.success('登录成功')
    // 有回跳地址则优先回跳；管理员进管理后台，普通用户进博客大厅
    router.push(route.query.redirect || (mode.value === 'admin' ? '/admin' : '/'))
  } catch (e) {
    /* 错误已由拦截器提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-mode {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  background: #f3f4f6;
  border-radius: 10px;
  padding: 5px;
}

.login-mode span {
  flex: 1;
  text-align: center;
  padding: 9px 0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #6b7280;
  transition: all 0.2s;
  user-select: none;
}

.login-mode span.active {
  background: #3a7afe;
  color: #fff;
  font-weight: 600;
}

.tip {
  text-align: center;
  font-size: 13px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
