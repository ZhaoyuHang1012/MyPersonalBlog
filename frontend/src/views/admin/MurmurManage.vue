<template>
  <div>
    <h2 class="page-title">说说管理</h2>
    <el-card shadow="never">
      <div class="murmur-publish">
        <el-input
          v-model="content"
          type="textarea"
          :rows="3"
          maxlength="2000"
          show-word-limit
          placeholder="此刻的想法…（支持 emoji）"
        />
        <div class="murmur-actions">
          <span class="tip">说说的发布展示在首页导航「说说」页面</span>
          <el-button type="primary" :loading="publishing" @click="publish">发布说说</el-button>
        </div>
      </div>

      <el-table :data="murmurs" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="内容" min-width="300">
          <template #default="{ row }">
            <span class="murmur-text">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListMurmurs, adminCreateMurmur, adminDeleteMurmur } from '../../api'

const murmurs = ref([])
const content = ref('')
const loading = ref(false)
const publishing = ref(false)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const load = async () => {
  loading.value = true
  try {
    murmurs.value = await adminListMurmurs()
  } finally {
    loading.value = false
  }
}

const publish = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  publishing.value = true
  try {
    await adminCreateMurmur({ content: content.value.trim() })
    ElMessage.success('发布成功')
    content.value = ''
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    publishing.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这条说说吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteMurmur(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.murmur-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.tip {
  font-size: 13px;
  color: #909399;
}

.murmur-text {
  white-space: pre-wrap;
}
</style>
