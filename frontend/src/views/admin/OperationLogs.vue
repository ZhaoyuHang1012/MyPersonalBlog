<template>
  <div>
    <h2 class="page-title">操作日志</h2>
    <div class="toolbar">
      <span class="log-tip">记录后台所有增删改操作（请求参数中的密码等敏感字段已自动打码）</span>
      <div class="spacer"></div>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="logs" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="操作人" width="100" />
      <el-table-column label="请求" width="100">
        <template #default="{ row }">
          <el-tag :type="methodTagType(row.method)" size="small">{{ row.method }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="uri" label="接口路径" min-width="200" />
      <el-table-column label="参数" min-width="220">
        <template #default="{ row }">
          <el-tooltip :content="row.params || '[]'" placement="top" :show-after="300">
            <span class="log-params">{{ row.params || '[]' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="IP" width="120" />
      <el-table-column label="耗时" width="90">
        <template #default="{ row }">{{ row.durationMs }} ms</template>
      </el-table-column>
      <el-table-column label="结果" width="80">
        <template #default="{ row }">
          <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
            {{ row.success === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
    </el-table>

    <div class="pager-box">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        v-model:current-page="page"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { adminListLogs } from '../../api'

const logs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const loading = ref(false)

const formatDate = (d) => (d ? dayjs(d).format('YYYY-MM-DD HH:mm:ss') : '')

const methodTagType = (m) =>
  ({ POST: 'success', PUT: 'warning', DELETE: 'danger' }[m] || 'info')

const load = async () => {
  loading.value = true
  try {
    const data = await adminListLogs({ page: page.value, size: size.value })
    logs.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.log-tip {
  font-size: 13px;
  color: #909399;
}

.log-params {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  color: #606266;
  cursor: default;
}
</style>
