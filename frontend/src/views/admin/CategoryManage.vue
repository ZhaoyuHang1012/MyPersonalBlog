<template>
  <div>
    <h2 class="page-title">分类管理</h2>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="name" placeholder="新分类名" style="width: 220px" maxlength="50" />
        <el-input-number v-model="sort" :min="0" style="width: 150px" />
        <el-button type="primary" @click="add">新增分类</el-button>
      </div>
      <el-table :data="categories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="220" />
        <el-table-column prop="postCount" label="文章数" width="100" />
        <el-table-column prop="sort" label="排序" width="100" />
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
import { getCategories, adminCreateCategory, adminUpdateCategory, adminDeleteCategory } from '../../api'

const categories = ref([])
const name = ref('')
const sort = ref(0)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    categories.value = await getCategories()
  } finally {
    loading.value = false
  }
}

const add = async () => {
  if (!name.value.trim()) {
    ElMessage.warning('请输入分类名')
    return
  }
  await adminCreateCategory({ name: name.value.trim(), sort: sort.value })
  ElMessage.success('新增成功')
  name.value = ''
  sort.value = 0
  load()
}

const edit = async (row) => {
  let value
  try {
    const r = await ElMessageBox.prompt('请输入新的分类名', '编辑分类', {
      inputValue: row.name,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    value = r.value
  } catch (e) {
    return
  }
  if (!value.trim()) return
  await adminUpdateCategory(row.id, { name: value.trim(), sort: row.sort })
  ElMessage.success('修改成功')
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」吗？该分类下的文章将变为未分类。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  await adminDeleteCategory(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
