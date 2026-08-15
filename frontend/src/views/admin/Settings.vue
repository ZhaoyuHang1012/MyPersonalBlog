<template>
  <div>
    <h2 class="page-title">站点设置</h2>
    <el-card shadow="never">
      <el-form label-width="90px" style="max-width: 720px" v-loading="loading">
        <el-form-item label="站点标题">
          <el-input v-model="form.title" placeholder="显示在浏览器标签和首页顶部" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" placeholder="一句简短的话介绍你的博客" />
        </el-form-item>
        <el-form-item label="站长昵称">
          <el-input v-model="form.author" />
        </el-form-item>
        <el-form-item label="站点公告">
          <el-input
            v-model="form.announcement"
            type="textarea"
            :rows="2"
            placeholder="显示在首页顶部横幅，留空则不显示"
          />
        </el-form-item>
        <el-form-item label="关于页">
          <el-input
            v-model="form.aboutMd"
            type="textarea"
            :rows="8"
            placeholder="支持 Markdown 语法，显示在「关于」页面"
          />
        </el-form-item>
        <el-form-item label="页脚文案">
          <el-input v-model="form.footer" placeholder="例如：本站内容仅供学习交流" />
        </el-form-item>
        <el-form-item label="备案号">
          <el-input v-model="form.icp" placeholder="例如：京ICP备XXXXXXXX号（可选）" />
        </el-form-item>
        <el-form-item label="允许评论">
          <el-switch v-model="form.allowComments" :active-value="1" :inactive-value="0" />
          <span class="switch-tip">{{ form.allowComments === 1 ? '访客可以提交评论（需审核）' : '前台关闭评论功能' }}</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSite, adminUpdateSite } from '../../api'

const form = ref({
  title: '',
  subtitle: '',
  author: '',
  announcement: '',
  aboutMd: '',
  footer: '',
  icp: '',
  allowComments: 1
})
const loading = ref(false)
const saving = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const site = await getSite()
    form.value = {
      title: site.title,
      subtitle: site.subtitle,
      author: site.author,
      announcement: site.announcement,
      aboutMd: site.aboutMd,
      footer: site.footer,
      icp: site.icp,
      allowComments: site.allowComments
    }
  } finally {
    loading.value = false
  }
})

const save = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('站点标题不能为空')
    return
  }
  saving.value = true
  try {
    await adminUpdateSite(form.value)
    ElMessage.success('设置已保存，前台立即生效')
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.switch-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}
</style>
