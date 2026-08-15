<template>
  <div>
    <h2 class="page-title">友链管理</h2>
    <div class="toolbar">
      <el-radio-group v-model="status" @change="reload">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
      </el-radio-group>
      <div class="spacer"></div>
      <el-button type="primary" @click="openDialog()">添加友链</el-button>
    </div>

    <el-table :data="links" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="站点名称" min-width="140" />
      <el-table-column label="地址" min-width="200">
        <template #default="{ row }">
          <a :href="row.url" target="_blank" class="link-url-text">{{ row.url }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
            {{ row.status === 1 ? '已通过' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" link type="success" @click="approve(row)">通过</el-button>
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
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

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑友链' : '添加友链'" width="480px">
      <el-form label-width="70px">
        <el-form-item label="名称">
          <el-input v-model="form.name" maxlength="50" placeholder="站点名称" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.url" maxlength="255" placeholder="https://example.com" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" maxlength="200" placeholder="一句话介绍（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminListLinks,
  adminCreateLink,
  adminUpdateLink,
  adminApproveLink,
  adminDeleteLink
} from '../../api'

const links = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const status = ref(null)
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const form = ref({ name: '', url: '', description: '', sort: 0 })

const load = async () => {
  loading.value = true
  try {
    const data = await adminListLinks({ page: page.value, size: size.value, status: status.value ?? undefined })
    links.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  load()
}

const openDialog = (row) => {
  editId.value = row?.id || null
  form.value = row
    ? { name: row.name, url: row.url, description: row.description || '', sort: row.sort }
    : { name: '', url: '', description: '', sort: 0 }
  dialogVisible.value = true
}

const save = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请填写站点名称')
    return
  }
  if (!/^https?:\/\//i.test(form.value.url.trim())) {
    ElMessage.warning('地址需以 http:// 或 https:// 开头')
    return
  }
  saving.value = true
  try {
    if (editId.value) {
      await adminUpdateLink(editId.value, form.value)
    } else {
      await adminCreateLink(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

const approve = async (row) => {
  await adminApproveLink(row.id)
  ElMessage.success('已通过，前台可见')
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除友链「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  await adminDeleteLink(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.link-url-text {
  font-size: 13px;
}
</style>
