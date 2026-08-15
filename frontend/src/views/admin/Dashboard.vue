<template>
  <div>
    <h2 class="page-title">仪表盘</h2>

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

    <el-card class="quick-card" shadow="never">
      <h3>快速操作</h3>
      <el-button type="primary" @click="$router.push('/admin/posts/new')">写文章</el-button>
      <el-button @click="$router.push('/admin/posts')">管理文章</el-button>
      <el-button @click="$router.push('/admin/comments')">审核评论</el-button>
      <el-button @click="$router.push('/admin/settings')">站点设置</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStats } from '../../api'

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
    xAxis: {
      type: 'category',
      data: stats.value.trend.map((t) => t.date.slice(5)),
      boundaryGap: false
    },
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

onMounted(async () => {
  stats.value = await getStats()
  await nextTick()
  trendChart = echarts.init(trendRef.value)
  topChart = echarts.init(topRef.value)
  renderTrend()
  renderTop()
  window.addEventListener('resize', resize)
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
</style>
