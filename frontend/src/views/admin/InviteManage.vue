<template>
  <div>
    <h2 class="page-title">邀请码管理</h2>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input-number v-model="count" :min="1" :max="50" style="width: 140px" />
        <el-button type="primary" :loading="generating" @click="generate">生成邀请码</el-button>
        <span class="tip">每个邀请码只能使用一次，注册用户默认 1GB 存储空间</span>
      </div>

      <el-table :data="invites" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="邀请码" min-width="180">
          <template #default="{ row }">
            <span class="code-text">{{ row.code }}</span>
            <el-button v-if="row.used === 0" link type="primary" size="small" @click="copyCode(row.code)">
              复制
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.used === 1 ? 'info' : 'success'" size="small">
              {{ row.used === 1 ? '已使用' : '未使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usedBy" label="使用者" width="140">
          <template #default="{ row }">{{ row.usedBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="使用时间" width="170">
          <template #default="{ row }">{{ row.usedAt ? formatDate(row.usedAt) : '—' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { adminListInvites, adminGenerateInvites } from '../../api'

const invites = ref([])
const count = ref(5)
const loading = ref(false)
const generating = ref(false)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  loading.value = true
  try {
    invites.value = await adminListInvites()
  } finally {
    loading.value = false
  }
}

const generate = async () => {
  generating.value = true
  try {
    await adminGenerateInvites(count.value)
    ElMessage.success(`已生成 ${count.value} 个邀请码`)
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    generating.value = false
  }
}

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
  } catch (e) {
    const ta = document.createElement('textarea')
    ta.value = code
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
  ElMessage.success('邀请码已复制：' + code)
}

onMounted(load)
</script>

<style scoped>
.tip {
  font-size: 13px;
  color: #909399;
}

.code-text {
  font-family: Consolas, monospace;
  letter-spacing: 1px;
  font-size: 14px;
  margin-right: 8px;
}
</style>
