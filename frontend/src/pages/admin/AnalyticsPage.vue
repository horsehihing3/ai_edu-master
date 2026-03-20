<template>
  <div class="analytics-page">
    <div class="page-header"><h1>시스템 분석</h1></div>

    <!-- DAU/MAU 차트 -->
    <div class="card">
      <div class="card__header">
        <h3>DAU / MAU 추이 (최근 30일)</h3>
        <span style="font-size:12px;color:#6B7280;">일별 접속 학생 수</span>
      </div>
      <div class="line-chart">
        <div v-if="!dauData.length" style="height:200px;display:flex;align-items:center;justify-content:center;color:#9CA3AF;font-size:14px;">
          데이터가 없습니다
        </div>
        <template v-else>
          <div class="chart-area">
            <svg viewBox="0 0 700 200" preserveAspectRatio="none" class="chart-svg">
              <polyline :points="dauAreaPoints" fill="rgba(59,130,246,0.1)" stroke="none" />
              <polyline :points="dauPoints" fill="none" stroke="#3B82F6" stroke-width="2" />
            </svg>
          </div>
          <div class="chart-labels">
            <span v-for="(d, i) in dauData.filter((_, i) => i % 5 === 0)" :key="i">{{ d.date }}</span>
          </div>
        </template>
      </div>
    </div>

    <div class="analytics-grid">
      <!-- 레벨별 학생 분포 도넛 차트 -->
      <div class="card level-card">
        <h3>레벨별 학생 분포</h3>
        <div v-if="!levelDist.length" style="flex:1;display:flex;align-items:center;justify-content:center;color:#9CA3AF;font-size:14px;">
          데이터가 없습니다
        </div>
        <div v-else class="donut-wrap">
          <div class="donut-chart">
            <svg viewBox="0 0 240 240" class="donut-svg">
              <path v-for="s in pieSlices" :key="s.level" :d="s.path" :fill="s.color" />
              <text x="120" y="112" text-anchor="middle" class="donut-total-num">{{ totalStudents }}</text>
              <text x="120" y="132" text-anchor="middle" class="donut-total-label">전체 학생</text>
            </svg>
          </div>
          <div class="donut-legend">
            <div v-for="l in levelDist" :key="l.level" class="legend-item">
              <div class="legend-dot" :style="{ background: l.color }" />
              <span class="legend-name">{{ l.level }} 레벨</span>
              <span class="legend-count">{{ l.count }}명</span>
              <strong class="legend-pct" :style="{ color: l.color }">{{ l.pct }}%</strong>
            </div>
          </div>
        </div>
      </div>


      <!-- 단원별 취약점 -->
      <div class="card">
        <h3>취약 단원 분석 (전체 평균)</h3>
        <div v-if="!weakUnits.length" style="flex:1;display:flex;align-items:center;justify-content:center;color:#9CA3AF;font-size:14px;">
          풀이 데이터가 없습니다
        </div>
        <div v-else class="weak-chart">
          <div v-for="w in weakUnits" :key="w.unit" class="weak-row">
            <span class="unit-label">{{ w.unit }}</span>
            <div class="weak-bar">
              <div class="weak-bar__fill" :style="{ width: w.accuracy + '%', background: w.accuracy < 60 ? '#EF4444' : '#F59E0B' }" />
            </div>
            <span :style="{ color: w.accuracy < 60 ? '#EF4444' : '#F59E0B', fontWeight: '600', fontSize: '13px', minWidth: '38px', textAlign: 'right' }">{{ w.accuracy }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/utils/api'

const dauData = ref([])
const levelDist = ref([])
const weakUnits = ref([])

const LEVEL_COLORS = { A: '#3B82F6', B: '#10B981', C: '#F59E0B' }

onMounted(async () => {
  const [dauRes, levelRes, weakRes] = await Promise.allSettled([
    api.get('/admin/analytics/dau'),
    api.get('/admin/analytics/level-dist'),
    api.get('/admin/analytics/weak-units')
  ])
  if (dauRes.status === 'fulfilled') {
    dauData.value = (dauRes.value.data || []).map(d => ({ date: d.date, value: d.count || d.value || 0 }))
  }
  if (levelRes.status === 'fulfilled') {
    levelDist.value = (levelRes.value.data || []).map(l => ({
      level: l.level, count: Number(l.count) || 0, pct: Number(l.pct) || 0,
      color: LEVEL_COLORS[l.level] || '#3B82F6'
    }))
  }
  if (weakRes.status === 'fulfilled') {
    weakUnits.value = (weakRes.value.data || []).map(w => ({ unit: w.unitName || w.unit, accuracy: Number(w.accuracy) || 0 }))
  }
})

// DAU 차트
const dauMax = computed(() => Math.ceil(Math.max(...dauData.value.map(d => d.value), 1) * 1.2))

const dauPoints = computed(() => {
  if (!dauData.value.length) return ''
  const len = dauData.value.length
  return dauData.value.map((d, i) => {
    const x = len === 1 ? 350 : (i / (len - 1)) * 700
    const y = 200 - (d.value / dauMax.value * 180)
    return `${x},${y}`
  }).join(' ')
})

const dauAreaPoints = computed(() => {
  if (!dauData.value.length) return ''
  const len = dauData.value.length
  const line = dauData.value.map((d, i) => {
    const x = len === 1 ? 350 : (i / (len - 1)) * 700
    const y = 200 - (d.value / dauMax.value * 180)
    return `${x},${y}`
  }).join(' ')
  return `0,200 ${line} 700,200`
})

// 도넛 차트
const totalStudents = computed(() => levelDist.value.reduce((s, l) => s + l.count, 0))

const pieSlices = computed(() => {
  const total = totalStudents.value
  if (!total) return []
  const cx = 120, cy = 120, r = 100, ri = 62
  let angle = -Math.PI / 2
  return levelDist.value.map(l => {
    const sweep = (l.count / total) * 2 * Math.PI
    const x1 = cx + r * Math.cos(angle)
    const y1 = cy + r * Math.sin(angle)
    const x2 = cx + r * Math.cos(angle + sweep)
    const y2 = cy + r * Math.sin(angle + sweep)
    const ix1 = cx + ri * Math.cos(angle)
    const iy1 = cy + ri * Math.sin(angle)
    const ix2 = cx + ri * Math.cos(angle + sweep)
    const iy2 = cy + ri * Math.sin(angle + sweep)
    const large = sweep > Math.PI ? 1 : 0
    const path = `M ${x1} ${y1} A ${r} ${r} 0 ${large} 1 ${x2} ${y2} L ${ix2} ${iy2} A ${ri} ${ri} 0 ${large} 0 ${ix1} ${iy1} Z`
    angle += sweep
    return { path, color: l.color, level: l.level }
  })
})
</script>

<style scoped lang="scss">
.analytics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-5;
  margin-top: $spacing-5;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }

  h3 { font-size: $font-size-base; font-weight: 700; margin-bottom: $spacing-5; }
}

.level-card {
  display: flex;
  flex-direction: column;

  h3 { align-self: flex-start; }
}

.line-chart { margin-top: $spacing-4; }

.chart-area {
  height: 200px;
  border-bottom: 1px solid $border;
  border-left: 1px solid $border;
}

.chart-svg { width: 100%; height: 100%; }

.chart-labels {
  display: flex;
  justify-content: space-between;
  padding-top: $spacing-2;
  font-size: 11px;
  color: $text-muted;
}

// 도넛 차트
.donut-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-5;
  width: 100%;
}

.donut-chart { flex-shrink: 0; }

.donut-svg {
  width: 280px;
  height: 280px;
}

.donut-total-num {
  font-size: 32px;
  font-weight: 700;
  fill: #1F2937;
}

.donut-total-label {
  font-size: 12px;
  fill: #9CA3AF;
}

.donut-legend {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: $spacing-6;
  width: 100%;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  font-size: $font-size-sm;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-name {
  color: $text-secondary;
}

.legend-count {
  color: $text-muted;
  font-size: $font-size-xs;
  margin-right: $spacing-1;
}

.legend-pct {
  font-weight: 700;
  font-size: $font-size-base;
  min-width: 40px;
  text-align: right;
}

// 취약 단원
.weak-chart {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.weak-row {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.unit-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  width: 160px;
  flex-shrink: 0;
}

.weak-bar {
  flex: 1;
  height: 10px;
  background: $border;
  border-radius: $radius-full;
  overflow: hidden;

  &__fill {
    height: 100%;
    border-radius: $radius-full;
    transition: width $transition-base;
  }
}
</style>
