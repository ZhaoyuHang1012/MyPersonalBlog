<template>
  <div>
    <h2 class="page-title">标签管理</h2>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="name" placeholder="新标签名" style="width: 220px" maxlength="50" />
        <el-button type="primary" @click="add">新增标签</el-button>
      </div>
      <el-table :data="tags" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="220" />
        <el-table-column prop="postCount" label="文章数" width="100" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTags, adminCreateTag, adminUpdateTag, adminDeleteTag } from '../../api'

const tags = ref([])
const name = ref('')
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    tags.value = await getTags()
  } finally {
    loading.value = false
  }
}

const add = async () => {
  if (!name.value.trim()) {
    ElMessage.warning('请输入标签名')
    return
  }
  await adminCreateTag({ name: name.value.trim() })
  ElMessage.success('新增成功')
  name.value = ''
  load()
}

const edit = async (row) => {
  let value
  try {
    const r = await ElMessageBox.prompt('请输入新的标签名', '编辑标签', {
      inputValue: row.name,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    value = r.value
  } catch (e) {
    return
  }
  if (!value.trim()) return
  await adminUpdateTag(row.id, { name: value.trim() })
  ElMessage.success('修改成功')
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteTag(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
