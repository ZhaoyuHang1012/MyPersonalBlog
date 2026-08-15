<template>
  <div>
    <h2 class="page-title">{{ userStore.isAdmin ? '仪表盘' : '我的主页' }}</h2>

    <!-- 管理员：全局统计 -->
    <template v-if="userStore.isAdmin">
      <el-row :gutter="16">
        <el-col :span="6" v-for="card in cards" :key="card.label">
          <el-card shadow="hover">
            <div class="stat">
              <div class="stat-num" :style="{ color: card.color }">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="14">
          <el-card shadow="never">
            <h3 class="chart-title">近 7 天访问趋势</h3>
            <div ref="trendRef" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="never">
            <h3 class="chart-title">文章浏览量 Top 10</h3>
            <div ref="topRef" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 普通用户：个人概览 -->
    <template v-else>
      <el-row :gutter="16">
        <el-col :span="6" v-for="card in myCards" :key="card.label">
          <el-card shadow="hover">
            <div class="stat">
              <div class="stat-num" :style="{ color: card.color }">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-card shadow="never" style="margin-top: 16px">
        <h3 class="chart-title">我的存储空间</h3>
        <div class="storage-box">
          <el-progress
            :percentage="storagePercent"
            :stroke-width="18"
            :color="storagePercent > 80 ? '#f56c6c' : '#3a7afe'"
          />
          <p class="storage-text">
            已用 {{ formatSize(storage.usage) }} / 共 {{ formatSize(storage.quota) }}
          </p>
        </div>
      </el-card>
    </template>

    <el-card class="quick-card" shadow="never">
      <h3>快速操作</h3>
      <el-button type="primary" @click="$router.push('/admin/posts/new')">写文章</el-button>
      <el-button @click="$router.push('/admin/posts')">我的文章</el-button>
      <el-button @click="$router.push('/admin/media')">媒体库</el-button>
      <el-button @click="$router.push('/admin/profile')">个人设置</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStats, adminListPosts, getFilesUsage } from '../../api'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()

// ---- 管理员统计 ----
const stats = ref(null)
const trendRef = ref(null)
const topRef = ref(null)
let trendChart = null
let topChart = null

const cards = computed(() => [
  { label: '文章总数', value: stats.value?.postTotal ?? '-', color: '#3a7afe' },
  { label: '已发布', value: stats.value?.published ?? '-', color: '#67c23a' },
  { label: '草稿', value: stats.value?.drafts ?? '-', color: '#909399' },
  { label: '今日访问', value: stats.value?.todayVisits ?? '-', color: '#f56c6c' }
])

const renderTrend = () => {
  if (!trendChart || !stats.value) return
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: stats.value.trend.map((t) => t.date.slice(5)), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        data: stats.value.trend.map((t) => t.count),
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#3a7afe' },
        lineStyle: { width: 2.5 }
      }
    ]
  })
}

const renderTop = () => {
  if (!topChart || !stats.value) return
  const posts = [...stats.value.topPosts].reverse()
  topChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 130, right: 30, top: 10, bottom: 30 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: {
      type: 'category',
      data: posts.map((p) => (p.title.length > 12 ? p.title.slice(0, 12) + '…' : p.title))
    },
    series: [
      {
        name: '浏览量',
        type: 'bar',
        data: posts.map((p) => p.viewCount),
        itemStyle: { color: '#67c23a', borderRadius: [0, 4, 4, 0] },
        barMaxWidth: 18
      }
    ]
  })
}

const resize = () => {
  trendChart?.resize()
  topChart?.resize()
}

// ---- 普通用户概览 ----
const myCounts = ref({ total: 0, published: 0, drafts: 0 })
const storage = ref({ usage: 0, quota: 1073741824 })

const myCards = computed(() => [
  { label: '我的文章', value: myCounts.value.total, color: '#3a7afe' },
  { label: '已发布', value: myCounts.value.published, color: '#67c23a' },
  { label: '草稿', value: myCounts.value.drafts, color: '#909399' },
  { label: '总浏览量', value: '—', color: '#f56c6c' }
])

const storagePercent = computed(() => {
  if (!storage.value.quota) return 0
  return Math.min(100, Math.round((storage.value.usage / storage.value.quota) * 100))
})

const formatSize = (bytes) => {
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}

onMounted(async () => {
  if (userStore.isAdmin) {
    stats.value = await getStats()
    await nextTick()
    trendChart = echarts.init(trendRef.value)
    topChart = echarts.init(topRef.value)
    renderTrend()
    renderTop()
    window.addEventListener('resize', resize)
  } else {
    try {
      const [all, published, drafts] = await Promise.all([
        adminListPosts({ page: 1, size: 1 }),
        adminListPosts({ page: 1, size: 1, status: 1 }),
        adminListPosts({ page: 1, size: 1, status: 0 })
      ])
      myCounts.value = { total: all.total, published: published.total, drafts: drafts.total }
    } catch (e) {
      /* ignore */
    }
    try {
      storage.value = await getFilesUsage()
    } catch (e) {
      /* ignore */
    }
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  trendChart?.dispose()
  topChart?.dispose()
})
</script>

<style scoped>
.chart-title {
  margin-bottom: 12px;
  font-size: 15px;
}

.chart-box {
  height: 300px;
}

.storage-box {
  padding: 8px 4px;
}

.storage-text {
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}
</style>
