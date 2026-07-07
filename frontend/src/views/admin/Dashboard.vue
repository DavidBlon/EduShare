<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div>
        <h2>控制台</h2>
        <p class="page-desc">欢迎回来，{{ adminInfo?.nickname || adminInfo?.username || '管理员' }}</p>
      </div>
      <n-button type="primary" @click="$router.push('/admin/resource')">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新增资源
      </n-button>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="stat-card" :style="{ '--card-accent': stat.color }">
        <div class="stat-icon-wrap" :style="{ background: stat.bg }">
          <n-icon :size="24"><component :is="stat.icon" /></n-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
        <div class="stat-accent"></div>
      </div>
    </div>

    <!-- Charts Row: side by side -->
    <div class="charts-row">
      <!-- Left: Donut + Legend -->
      <n-card :bordered="false" class="chart-card chart-card-left">
        <template #header>
          <span class="card-header-title">
            <n-icon><PulseOutline /></n-icon> 下载统计排行
          </span>
        </template>
        <div class="donut-body">
          <div class="donut-area">
            <div v-if="chartLoading" class="chart-skeleton-wrap">
              <div class="circle-skeleton">
                <n-skeleton circle style="width:160px;height:160px" />
              </div>
            </div>
            <div v-else-if="noData" class="chart-empty-wrap">
              <n-empty description="暂无下载数据">
                <template #extra>
                  <n-button size="small" @click="fetchStats">刷新</n-button>
                </template>
              </n-empty>
            </div>
            <template v-else>
              <div class="donut-chart" :style="donutStyle">
                <div class="donut-center">
                  <template v-if="centerInfo.hover">
                    <span class="donut-hover-title" :title="centerInfo.title">{{ centerInfo.title }}</span>
                    <span class="donut-hover-count">{{ centerInfo.count }} 次下载</span>
                    <span class="donut-hover-pct">{{ centerInfo.pct }}</span>
                  </template>
                  <template v-else>
                    <span class="donut-total">{{ totalDownloads }}</span>
                    <span class="donut-label">总下载</span>
                  </template>
                </div>
              </div>
            </template>
          </div>

          <div class="legend-area">
            <div v-if="chartLoading" class="legend-loading">
              <n-skeleton text :repeat="8" />
            </div>
            <div v-else-if="noData" class="legend-empty">暂无数据</div>
            <div v-else class="legend-list">
              <div
                v-for="(item, index) in downloadStats"
                :key="item.resourceId"
                class="legend-item"
                :class="{ 'legend-item-active': hoveredIndex === index }"
                @mouseenter="hoveredIndex = index"
                @mouseleave="hoveredIndex = -1"
              >
                <span class="legend-index" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
                <span class="legend-dot" :style="{ background: colors[index % colors.length] }"></span>
                <span class="legend-id">#{{ item.resourceId }}</span>
                <span class="legend-title" :title="item.resourceTitle">{{ item.resourceTitle }}</span>
                <span class="legend-count">{{ item.downloadCount }}</span>
                <span class="legend-pct">{{ pct(item.downloadCount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </n-card>

      <!-- Right: Line Chart -->
      <n-card :bordered="false" class="chart-card chart-card-right">
        <template #header>
          <span class="card-header-title">
            <n-icon><TrendingUpOutline /></n-icon> 每日下载量（近15天）
          </span>
        </template>
        <div class="line-chart-wrapper" ref="chartWrapperRef">
          <div v-if="dailyLoading" class="line-chart-skeleton">
            <n-skeleton height="180" />
          </div>
          <div v-else-if="dailyZero" class="line-chart-empty">
            <n-empty description="近期暂无下载数据" />
          </div>
          <svg v-else :viewBox="`0 0 ${SVG_W} ${SVG_H}`" class="line-chart-svg" preserveAspectRatio="xMidYMid meet">
            <line
              v-for="g in gridLines" :key="g.y"
              :x1="g.x1" :y1="g.y" :x2="g.x2" :y2="g.y"
              class="grid-line"
            />
            <text
              v-for="g in gridLines" :key="'yl'+g.y"
              :x="g.tx" :y="g.y + 4"
              class="y-label"
            >{{ g.label }}</text>
            <!-- Area fill -->
            <path :d="areaPath" fill="url(#areaGrad)" opacity="0.08" />
            <!-- Line -->
            <path :d="linePath" fill="none" class="chart-line" />
            <!-- Data points -->
            <circle
              v-for="(d, i) in dailyData" :key="'pt'+i"
              :cx="xPos(i)" :cy="yPos(d.count)" r="2.5"
              class="data-point"
              :class="{ 'data-point-active': dayHoveredIndex === i }"
            />
            <circle
              v-for="(d, i) in dailyData" :key="'ht'+i"
              :cx="xPos(i)" :cy="yPos(d.count)" r="8"
              class="hit-area"
              @mouseenter="onDayPointEnter($event, i)"
              @mouseleave="onDayPointLeave"
            />
            <text
              v-for="(d, i) in dailyData" :key="'xl'+i"
              :x="xPos(i)" :y="SVG_H - 6"
              class="x-label"
              :class="{ 'x-label-hide': i % 3 !== 0 }"
            >{{ fmtDate(d.date) }}</text>
          </svg>
          <svg width="0" height="0">
            <defs>
              <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#6366f1" />
                <stop offset="100%" stop-color="#6366f1" stop-opacity="0" />
              </linearGradient>
            </defs>
          </svg>
          <!-- Tooltip -->
          <transition name="tooltip-fade">
            <div v-if="dayTooltip.show" class="line-tooltip" :style="dayTooltipStyle">
              <div class="lt-date">{{ dayTooltip.date }}</div>
              <div class="lt-count">下载量：<strong>{{ dayTooltip.count }}</strong> 次</div>
              <div v-if="dayTooltip.top.length > 0" class="lt-top">
                <div class="lt-top-title">最多下载：</div>
                <div v-for="(item, idx) in dayTooltip.top" :key="idx" class="lt-top-item">
                  <span class="lt-top-idx">{{ idx + 1 }}</span>
                  <span class="lt-top-name">{{ item.resourceTitle }}</span>
                  <span class="lt-top-num">{{ item.downloadCount }}次</span>
                </div>
              </div>
              <div v-if="dayTopLoading" class="lt-loading">加载中…</div>
              <div class="lt-arrow"></div>
            </div>
          </transition>
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import {
  AddOutline, MenuOutline, PricetagsOutline,
  ListOutline, DocumentTextOutline, PulseOutline, TrendingUpOutline
} from '@vicons/ionicons5'
import { getDownloadStats, getDailyDownloadStats, getDailyTopResources } from '@/api/downloadLog'

const adminInfo = ref({})
try { adminInfo.value = JSON.parse(localStorage.getItem('adminInfo') || '{}') } catch { /* noop */ }

const colors = [
  '#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#ef4444',
  '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#84cc16'
]

// --- Stats cards ---
const stats = ref([
  { label: '资源总数', value: '—', icon: markRaw(DocumentTextOutline), bg: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#6366f1' },
  { label: '分类数量', value: '—', icon: markRaw(MenuOutline), bg: 'linear-gradient(135deg,#10b981,#059669)', color: '#10b981' },
  { label: '标签数量', value: '—', icon: markRaw(PricetagsOutline), bg: 'linear-gradient(135deg,#f59e0b,#d97706)', color: '#f59e0b' },
  { label: '下载日志', value: '—', icon: markRaw(ListOutline), bg: 'linear-gradient(135deg,#6b7280,#4b5563)', color: '#6b7280' }
])

// --- Donut chart ---
const downloadStats = ref([])
const chartLoading = ref(true)
const hoveredIndex = ref(-1)

const totalDownloads = computed(() =>
  downloadStats.value.reduce((sum, item) => sum + item.downloadCount, 0)
)

const noData = computed(() =>
  !chartLoading.value && downloadStats.value.length === 0
)

function pct(count) {
  const total = totalDownloads.value
  if (!total) return '0%'
  return (count / total * 100).toFixed(1) + '%'
}

function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r},${g},${b},${alpha})`
}

const centerInfo = computed(() => {
  if (hoveredIndex.value >= 0 && hoveredIndex.value < downloadStats.value.length) {
    const item = downloadStats.value[hoveredIndex.value]
    return {
      hover: true,
      title: item.resourceTitle,
      count: item.downloadCount,
      pct: pct(item.downloadCount)
    }
  }
  return { hover: false }
})

const donutStyle = computed(() => {
  const items = downloadStats.value
  if (!items.length || totalDownloads.value === 0) return {}
  let cumAngle = 0
  const isHovering = hoveredIndex.value >= 0
  const stops = items.map((item, i) => {
    const angle = (item.downloadCount / totalDownloads.value) * 360
    if (angle === 0) return null
    const start = cumAngle
    cumAngle += angle
    const color = colors[i % colors.length]
    // Dim non-hovered segments when hovering
    if (isHovering && hoveredIndex.value !== i) {
      return `${hexToRgba(color, 0.1)} ${start}deg ${cumAngle}deg`
    }
    return `${color} ${start}deg ${cumAngle}deg`
  }).filter(Boolean)
  return { background: `conic-gradient(${stops.join(', ')})` }
})

async function fetchStats() {
  chartLoading.value = true
  try {
    const res = await getDownloadStats(10)
    downloadStats.value = (res.data || []).filter(item => item.downloadCount > 0)
  } catch {
    downloadStats.value = []
  } finally {
    chartLoading.value = false
  }
}

// --- Line chart ---
const SVG_W = 560
const SVG_H = 190
const ML = 36
const MR = 10
const MT = 14
const MB = 28
const CHART_W = SVG_W - ML - MR
const CHART_H = SVG_H - MT - MB
const CHART_BOT = SVG_H - MB

const dailyData = ref([])
const dailyLoading = ref(true)
const chartWrapperRef = ref(null)
const dayHoveredIndex = ref(-1)
const dayTopCache = ref({})
const dayTopLoading = ref(false)
const dayTooltip = ref({ show: false, date: '', count: 0, top: [], px: 0, py: 0 })

const dailyZero = computed(() =>
  !dailyLoading.value && dailyData.value.every(d => d.count === 0)
)

function fillDailyData(rawData) {
  const result = []
  for (let i = 14; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = d.toISOString().slice(0, 10)
    const found = rawData.find(r => r.date === key)
    result.push({ date: key, count: found ? found.count : 0 })
  }
  return result
}

const maxCount = computed(() => Math.max(1, ...dailyData.value.map(d => d.count)))

const gridLines = computed(() => {
  const lines = []
  const steps = 4
  for (let i = 0; i <= steps; i++) {
    const y = CHART_BOT - (i / steps) * CHART_H
    const label = Math.round((i / steps) * maxCount.value)
    lines.push({ y: Math.round(y), label: String(label), x1: ML, x2: SVG_W - MR, tx: ML - 6 })
  }
  return lines
})

function xPos(i) {
  const len = dailyData.value.length
  if (len <= 1) return ML + CHART_W / 2
  return ML + (i / (len - 1)) * CHART_W
}

function yPos(count) {
  return CHART_BOT - (count / maxCount.value) * CHART_H
}

const linePath = computed(() => {
  return dailyData.value.map((d, i) => {
    const x = xPos(i)
    const y = yPos(d.count)
    return `${i === 0 ? 'M' : 'L'} ${x} ${y}`
  }).join(' ')
})

const areaPath = computed(() => {
  const items = dailyData.value
  if (!items.length) return ''
  const top = items.map((d, i) => `${i === 0 ? 'M' : 'L'} ${xPos(i)} ${yPos(d.count)}`).join(' ')
  return `${top} L ${xPos(items.length - 1)} ${CHART_BOT} L ${xPos(0)} ${CHART_BOT} Z`
})

function fmtDate(dateStr) {
  dateStr = String(dateStr)
  const month = parseInt(dateStr.slice(5, 7), 10)
  const day = parseInt(dateStr.slice(8, 10), 10)
  return `${month}/${day}`
}

async function fetchDailyStats() {
  dailyLoading.value = true
  try {
    const res = await getDailyDownloadStats(15)
    dailyData.value = fillDailyData(res.data || [])
  } catch {
    dailyData.value = fillDailyData([])
  } finally {
    dailyLoading.value = false
  }
}

// --- Line chart hover ---
const dayTooltipStyle = computed(() => {
  if (!dayTooltip.value.show) return { display: 'none' }
  return {
    left: `${dayTooltip.value.px}px`,
    top: `${dayTooltip.value.py}px`
  }
})

function onDayPointEnter(event, index) {
  const d = dailyData.value[index]
  if (!d) return
  dayHoveredIndex.value = index

  const svgEl = event.currentTarget.closest('svg')
  const wrapper = chartWrapperRef.value
  if (!svgEl || !wrapper) return

  const svgRect = svgEl.getBoundingClientRect()
  const wrapperRect = wrapper.getBoundingClientRect()
  const scaleX = svgRect.width / SVG_W
  const scaleY = svgRect.height / SVG_H

  // Exact data point position relative to wrapper
  const px = svgRect.left - wrapperRect.left + xPos(index) * scaleX
  const py = svgRect.top - wrapperRect.top + yPos(d.count) * scaleY

  // Show tooltip immediately
  dayTooltip.value = {
    show: true,
    date: d.date,
    count: d.count,
    top: dayTopCache.value[d.date] || [],
    px,
    py
  }

  // Fetch top data if not cached and day has downloads
  if (!(d.date in dayTopCache.value) && d.count > 0) {
    dayTopLoading.value = true
    getDailyTopResources(d.date, 3).then(res => {
      const top = (res.data || []).filter(item => item.downloadCount > 0)
      dayTopCache.value[d.date] = top
      if (dayHoveredIndex.value === index) {
        dayTooltip.value = { ...dayTooltip.value, top }
      }
    }).catch(() => {
      dayTopCache.value[d.date] = []
    }).finally(() => {
      dayTopLoading.value = false
    })
  }
}

function onDayPointLeave() {
  dayHoveredIndex.value = -1
  dayTooltip.value = { show: false, date: '', count: 0, top: [], px: 0, py: 0 }
}

// --- Init ---
onMounted(async () => {
  try {
    const [catRes, tagRes, resourceRes, logRes] = await Promise.all([
      import('@/api/category').then(m => m.getAllCategories()),
      import('@/api/tag').then(m => m.getAdminTagList()),
      import('@/api/resource').then(m => m.getAdminResourcePage({ page: 1, pageSize: 1 })),
      import('@/api/downloadLog').then(m => m.getDownloadLogList({ page: 1, pageSize: 1 }))
    ])
    stats.value[0].value = resourceRes.data?.total || 0
    stats.value[1].value = (catRes.data || []).length
    stats.value[2].value = (tagRes.data || []).length
    stats.value[3].value = logRes.data?.total || 0
  } catch { /* noop */ }

  await Promise.all([fetchStats(), fetchDailyStats()])
})
</script>

<style scoped>
.page-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 4px 0 0;
}

/* ---- Stats Grid ---- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  box-shadow: var(--shadow);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.3s ease;
  overflow: hidden;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.stat-accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--card-accent);
  border-radius: 0 2px 2px 0;
}

.stat-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* ---- Charts Row (side by side) ---- */
.charts-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  align-items: stretch;
}

.chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chart-card :deep(.n-card__content) {
  flex: 1;
}

.chart-card-left {
  min-width: 0;
  width: 42%;
}

.chart-card-right {
  min-width: 0;
  width: 58%;
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

/* ---- Donut Body ---- */
.donut-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  padding: 4px 0;
}

.donut-area {
  flex-shrink: 0;
  width: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-skeleton-wrap {
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-empty-wrap {
  width: 160px;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.donut-chart {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.donut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s ease;
}

.donut-total {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.1;
}

.donut-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* Donut center hover content */
.donut-hover-title {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 76px;
}
.donut-hover-count {
  font-size: 14px;
  font-weight: 700;
  color: #6366f1;
  line-height: 1.1;
  margin-top: 2px;
}
.donut-hover-pct {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 1px;
}

/* Legend Area */
.legend-area {
  flex: 1;
  min-width: 0;
}

.legend-loading {
  padding: 4px 0;
}

.legend-empty {
  text-align: center;
  padding: 30px 0;
  font-size: 14px;
  color: var(--text-placeholder);
}

.legend-list {
  display: flex;
  flex-direction: column;
  max-height: 280px;
  overflow-y: auto;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 6px;
  border-radius: 6px;
  transition: background 0.2s;
}
.legend-item:hover {
  background: var(--bg);
}
.legend-item-active {
  background: rgba(99, 102, 241, 0.06) !important;
}
.legend-item-active .legend-title {
  color: #6366f1;
}
.legend-item-active .legend-count {
  color: #6366f1;
}

.legend-index {
  width: 20px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-placeholder);
  text-align: center;
  flex-shrink: 0;
}
.legend-index.top3 {
  color: var(--text-primary);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-id {
  font-size: 12px;
  color: var(--text-secondary);
  flex-shrink: 0;
  font-family: monospace;
  min-width: 38px;
}

.legend-title {
  flex: 1;
  font-size: 12px;
  color: var(--text-regular);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.legend-count {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  flex-shrink: 0;
  min-width: 24px;
  text-align: right;
}

.legend-pct {
  font-size: 11px;
  color: var(--text-placeholder);
  flex-shrink: 0;
  min-width: 38px;
  text-align: right;
}

/* ---- SVG Line Chart ---- */
.line-chart-wrapper {
  padding: 6px 0;
  width: 100%;
  position: relative;
}

.line-chart-skeleton {
  padding: 6px 0;
}

.line-chart-empty {
  padding: 30px 0;
  display: flex;
  justify-content: center;
}

.line-chart-svg {
  width: 100%;
  height: auto;
  display: block;
  overflow: visible;
}

.grid-line {
  stroke: #e8e8ef;
  stroke-width: 0.5;
  stroke-dasharray: 3 3;
}

.y-label {
  fill: #94a3b8;
  font-size: 10px;
  text-anchor: end;
  font-family: inherit;
}

.chart-line {
  stroke: #6366f1;
  stroke-width: 1.5;
  stroke-linejoin: round;
  stroke-linecap: round;
}

.data-point {
  fill: #6366f1;
  stroke: white;
  stroke-width: 1;
  transition: r 0.15s;
}
.data-point-active {
  r: 4.5;
}

.hit-area {
  fill: transparent;
  cursor: pointer;
}

.x-label {
  fill: #94a3b8;
  font-size: 9px;
  text-anchor: middle;
  font-family: inherit;
}

.x-label-hide {
  display: none;
}

/* ---- Line Chart Tooltip ---- */
.line-tooltip {
  position: absolute;
  z-index: 10;
  background: rgba(30, 30, 50, 0.94);
  backdrop-filter: blur(8px);
  color: #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 12px;
  line-height: 1.5;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
  min-width: 140px;
  max-width: 200px;
  pointer-events: none;
  transform: translateX(-50%) translateY(-100%);
}
.lt-date {
  font-weight: 700;
  font-size: 13px;
  color: #a5b4fc;
  margin-bottom: 2px;
}
.lt-count {
  font-size: 12px;
  color: #cbd5e1;
  margin-bottom: 6px;
}
.lt-count strong {
  color: #f1f5f9;
  font-weight: 700;
}
.lt-top {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 6px;
}
.lt-top-title {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
}
.lt-top-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 2px 0;
  font-size: 11px;
}
.lt-top-idx {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  text-align: center;
  line-height: 14px;
  font-size: 10px;
  font-weight: 700;
  flex-shrink: 0;
}
.lt-top-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #e2e8f0;
  min-width: 0;
}
.lt-top-num {
  flex-shrink: 0;
  color: #a5b4fc;
  font-weight: 600;
}
.lt-loading {
  font-size: 11px;
  color: #64748b;
  text-align: center;
  padding: 4px 0;
}
.lt-arrow {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 6px solid rgba(30, 30, 50, 0.94);
}

/* Tooltip fade transition */
.tooltip-fade-enter-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.tooltip-fade-leave-active {
  transition: opacity 0.1s ease;
}
.tooltip-fade-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(calc(-100% + 4px));
}
.tooltip-fade-leave-to {
  opacity: 0;
}

/* ---- Responsive ---- */
@media (max-width: 900px) {
  .charts-row {
    flex-direction: column;
  }
  .chart-card-left,
  .chart-card-right {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .stat-card { padding: 16px; }
  .stat-icon-wrap { width: 44px; height: 44px; border-radius: 10px; }
  .stat-value { font-size: 24px; }
  .donut-body {
    flex-direction: column;
    align-items: center;
  }
}
</style>
