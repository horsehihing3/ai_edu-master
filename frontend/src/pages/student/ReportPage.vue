<template>
  <div class="report-page">
    <div class="page-header">
      <h1>학습 리포트</h1>
      <p>나의 학습 현황과 성취도를 확인하세요</p>
    </div>

    <!-- 종합 성취도 -->
    <div class="report-grid">
      <div class="card achievement-card">
        <h3>종합 성취도</h3>
        <div class="donut-wrap">
          <svg width="160" height="160" viewBox="0 0 160 160">
            <circle cx="80" cy="80" r="60" fill="none" stroke="#E5E7EB" stroke-width="16"/>
            <circle cx="80" cy="80" r="60" fill="none" stroke="#3B82F6" stroke-width="16"
              :stroke-dasharray="`${2 * Math.PI * 60 * stats.accuracy / 100} ${2 * Math.PI * 60}`"
              stroke-dashoffset="94.25"
              stroke-linecap="round"
            />
            <text x="80" y="76" text-anchor="middle" font-size="26" font-weight="700" fill="#1F2937">{{ stats.accuracy }}%</text>
            <text x="80" y="96" text-anchor="middle" font-size="12" fill="#6B7280">정답률</text>
          </svg>
        </div>
        <div class="achievement-stats">
          <div class="ach-stat">
            <strong>{{ stats.totalSolved }}</strong>
            <span>총 풀이</span>
          </div>
          <div class="ach-stat">
            <strong>{{ stats.totalCorrect }}</strong>
            <span>정답</span>
          </div>
          <div class="ach-stat">
            <strong>{{ stats.studyDays }}일</strong>
            <span>학습일</span>
          </div>
        </div>
      </div>

      <!-- 레벨별 문제 수 -->
      <div class="card">
        <h3>레벨별 학습 현황</h3>
        <div class="level-bars">
          <div v-for="l in levelData" :key="l.level" class="level-bar-item">
            <div class="level-bar-item__header">
              <AppBadge :type="l.level" />
              <span>{{ l.count }}문제</span>
            </div>
            <div class="progress-bar" style="height: 10px;">
              <div class="progress-bar__fill" :style="{ width: l.pct + '%', background: l.color }" />
            </div>
          </div>
        </div>
      </div>

      <!-- AI 코멘트 -->
      <div class="card ai-report-card">
        <div class="ai-card-header">
          <span class="ai-badge">AI</span>
          <h3>AI 학습 분석</h3>
        </div>
        <p>{{ aiComment }}</p>
        <div class="ai-tags">
          <span v-for="tag in aiTags" :key="tag" class="ai-tag">{{ tag }}</span>
        </div>
      </div>
    </div>

    <!-- ───────────── 차트 섹션 ───────────── -->
    <!-- [2026-03-21] 학습 이력 기반 3개 차트 추가 -->
    <div class="charts-row">

      <!-- Chart 1: 최근 정답률 추이 (꺾은선) -->
      <div class="card chart-card">
        <h3>최근 정답률 추이</h3>
        <p class="chart-sub">최근 {{ chartAccuracyData.length }}회 세션</p>
        <div v-if="chartAccuracyData.length < 2" class="chart-empty">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          <p>2회 이상 학습 후 확인할 수 있습니다</p>
        </div>
        <svg v-else :viewBox="`0 0 ${SVG_W} ${SVG_H}`" class="chart-svg">
          <!-- Y 그리드 -->
          <line v-for="pct in [0, 50, 100]" :key="'yg'+pct"
            :x1="PAD_L" :x2="SVG_W - PAD_R"
            :y1="yVal(pct)" :y2="yVal(pct)"
            stroke="#F3F4F6" stroke-width="1" />
          <text v-for="pct in [0, 50, 100]" :key="'yt'+pct"
            :x="PAD_L - 6" :y="yVal(pct) + 4"
            text-anchor="end" font-size="11" fill="#9CA3AF">{{ pct }}%</text>

          <!-- 그라디언트 채우기 영역 -->
          <defs>
            <linearGradient id="lineGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#3B82F6" stop-opacity="0.18"/>
              <stop offset="100%" stop-color="#3B82F6" stop-opacity="0"/>
            </linearGradient>
          </defs>
          <polygon v-if="lineFillPath" :points="lineFillPath" fill="url(#lineGrad)" />

          <!-- 꺾은선 -->
          <polyline :points="linePolyline"
            fill="none" stroke="#3B82F6" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round" />

          <!-- 데이터 점 + 날짜 레이블 -->
          <g v-for="(dot, i) in lineDots" :key="'dot'+i">
            <circle :cx="dot.cx" :cy="dot.cy" r="4.5" fill="white" stroke="#3B82F6" stroke-width="2" />
            <text :x="dot.cx" :y="SVG_H - PAD_B + 16"
              text-anchor="middle" font-size="10" fill="#9CA3AF">{{ dot.label }}</text>
            <text v-if="i === lineDots.length - 1 || i === 0"
              :x="dot.cx" :y="dot.cy - 10"
              text-anchor="middle" font-size="11" font-weight="700" fill="#3B82F6">{{ dot.acc }}%</text>
          </g>
        </svg>
      </div>

      <!-- Chart 2: 일별 풀이량 (막대) -->
      <div class="card chart-card">
        <h3>일별 풀이량</h3>
        <p class="chart-sub">최근 {{ chartSolvedData.length }}회 세션</p>
        <div v-if="!chartSolvedData.length" class="chart-empty">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5"><rect x="2" y="3" width="4" height="18" rx="1"/><rect x="9" y="8" width="4" height="13" rx="1"/><rect x="16" y="5" width="4" height="16" rx="1"/></svg>
          <p>학습 데이터가 없습니다</p>
        </div>
        <svg v-else :viewBox="`0 0 ${SVG_W} ${SVG_H}`" class="chart-svg">
          <!-- 베이스라인 -->
          <line :x1="PAD_L" :x2="SVG_W - PAD_R"
            :y1="PAD_T + IH" :y2="PAD_T + IH"
            stroke="#E5E7EB" stroke-width="1" />

          <!-- 막대 -->
          <g v-for="(bar, i) in barRects" :key="'bar'+i">
            <rect :x="bar.x" :y="bar.y" :width="bar.w" :height="Math.max(bar.h, 2)"
              :fill="bar.h > IH * 0.5 ? '#3B82F6' : '#93C5FD'" rx="3" />
            <text :x="bar.cx" :y="SVG_H - PAD_B + 16"
              text-anchor="middle" font-size="10" fill="#9CA3AF">{{ bar.label }}</text>
            <text v-if="bar.count > 0" :x="bar.cx" :y="bar.y - 5"
              text-anchor="middle" font-size="11" font-weight="600" fill="#374151">{{ bar.count }}</text>
          </g>

          <!-- Y 레이블 (최대값) -->
          <text :x="PAD_L - 6" :y="PAD_T + 4"
            text-anchor="end" font-size="11" fill="#9CA3AF">{{ barMaxSolved }}</text>
          <text :x="PAD_L - 6" :y="PAD_T + IH + 4"
            text-anchor="end" font-size="11" fill="#9CA3AF">0</text>
        </svg>
      </div>
    </div>

    <!-- Chart 3: 이번달 vs 지난달 비교 -->
    <div class="card chart-card" style="margin-top: 20px;">
      <h3>이번달 vs 지난달 비교</h3>
      <p class="chart-sub">정답률 및 풀이량 월별 비교</p>
      <div v-if="monthComparison.thisMonth.sessions === 0 && monthComparison.lastMonth.sessions === 0" class="chart-empty">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5"><path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/></svg>
        <p>비교할 학습 이력이 없습니다</p>
      </div>
      <div v-else class="month-compare">
        <div class="month-compare__chart">
          <svg viewBox="0 0 320 160" class="chart-svg-sm">
            <!-- 배경 그리드 -->
            <line v-for="pct in [0, 50, 100]" :key="'mg'+pct"
              x1="60" x2="300"
              :y1="16 + (1 - pct / 100) * 108" :y2="16 + (1 - pct / 100) * 108"
              stroke="#F3F4F6" stroke-width="1" />
            <text v-for="pct in [0, 50, 100]" :key="'mt'+pct"
              x="54" :y="16 + (1 - pct / 100) * 108 + 4"
              text-anchor="end" font-size="11" fill="#9CA3AF">{{ pct }}%</text>

            <!-- 지난달 막대 -->
            <rect x="90" :y="16 + (1 - monthComparison.lastMonth.accuracy / 100) * 108"
              width="60" :height="monthComparison.lastMonth.accuracy / 100 * 108"
              fill="#D1D5DB" rx="4" />
            <text x="120" y="140" text-anchor="middle" font-size="11" fill="#9CA3AF">지난달</text>
            <text x="120"
              :y="16 + (1 - monthComparison.lastMonth.accuracy / 100) * 108 - 6"
              text-anchor="middle" font-size="12" font-weight="700" fill="#6B7280">
              {{ monthComparison.lastMonth.accuracy }}%
            </text>

            <!-- 이번달 막대 -->
            <rect x="170" :y="16 + (1 - monthComparison.thisMonth.accuracy / 100) * 108"
              width="60" :height="monthComparison.thisMonth.accuracy / 100 * 108"
              fill="#3B82F6" rx="4" />
            <text x="200" y="140" text-anchor="middle" font-size="11" fill="#9CA3AF">이번달</text>
            <text x="200"
              :y="16 + (1 - monthComparison.thisMonth.accuracy / 100) * 108 - 6"
              text-anchor="middle" font-size="12" font-weight="700" fill="#3B82F6">
              {{ monthComparison.thisMonth.accuracy }}%
            </text>
          </svg>
        </div>
        <div class="month-compare__stats">
          <div class="mc-stat">
            <span class="mc-label">이번달 풀이</span>
            <strong class="mc-value">{{ monthComparison.thisMonth.solved }}문제</strong>
          </div>
          <div class="mc-stat">
            <span class="mc-label">지난달 풀이</span>
            <strong class="mc-value secondary">{{ monthComparison.lastMonth.solved }}문제</strong>
          </div>
          <div class="mc-stat mc-stat--diff">
            <span class="mc-label">정답률 변화</span>
            <strong :class="['mc-value', accuracyDiff >= 0 ? 'up' : 'down']">
              {{ accuracyDiff >= 0 ? '▲' : '▼' }} {{ Math.abs(accuracyDiff) }}%
            </strong>
          </div>
        </div>
      </div>
    </div>
    <!-- ──────────────────────────────────── -->

    <!-- [2026-04-03] 등급 변화 타임라인 -->
    <div class="card" style="margin-top: 24px;">
      <h3 style="margin-bottom: 20px;">등급 변화 타임라인</h3>
      <div v-if="!levelHistory.length" class="chart-empty">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 8v4l3 3"/></svg>
        <p>진단 테스트를 완료하면 등급 변화가 기록됩니다.</p>
      </div>
      <div v-else class="level-timeline">
        <div v-for="(h, i) in levelHistory" :key="i" class="tl-item">
          <div class="tl-line-wrap">
            <div class="tl-dot" :style="{ background: LEVEL_COLORS[h.level] || '#9CA3AF' }">
              {{ h.level }}
            </div>
            <div v-if="i < levelHistory.length - 1" class="tl-connector" />
          </div>
          <div class="tl-info">
            <p class="tl-date">{{ h.date }}</p>
            <p class="tl-detail">
              정답률 <strong>{{ h.scoreRate }}%</strong>
              <span v-if="i > 0" :class="['tl-change', levelHistory[i].level < levelHistory[i-1].level ? 'up' : levelHistory[i].level > levelHistory[i-1].level ? 'down' : 'same']">
                {{ levelHistory[i].level < levelHistory[i-1].level ? '▲ 등급 상승' : levelHistory[i].level > levelHistory[i-1].level ? '▼ 등급 하락' : '— 유지' }}
              </span>
            </p>
          </div>
        </div>
        <div class="tl-item tl-item--current">
          <div class="tl-line-wrap">
            <div class="tl-dot tl-dot--current" :style="{ background: LEVEL_COLORS[currentLevel] || '#9CA3AF' }">
              {{ currentLevel }}
            </div>
          </div>
          <div class="tl-info">
            <p class="tl-date">현재</p>
            <p class="tl-detail"><strong>{{ currentLevel }}레벨</strong> 학습 중</p>
          </div>
        </div>
      </div>
    </div>

    <!-- [2026-04-03] 단원별 취약 분석 -->
    <div class="card" style="margin-top: 24px;">
      <h3 style="margin-bottom: 20px;">단원별 취약 분석</h3>
      <AppEmpty v-if="!weakUnits.length" message="데이터가 없습니다." description="3문제 이상 풀면 단원별 분석이 표시됩니다." />
      <div v-else class="unit-weak-list">
        <div v-for="(w, i) in weakUnits" :key="w.unitName" class="unit-weak-item">
          <div class="unit-weak-header">
            <div>
              <span class="rank">{{ i + 1 }}</span>
              <span class="unit-name">{{ w.unitName }}</span>
              <span class="unit-subject">{{ w.subject }}</span>
            </div>
            <span :class="['unit-accuracy', w.accuracy < 50 ? 'danger' : 'warning']">{{ w.accuracy }}%</span>
          </div>
          <div class="unit-bar-wrap">
            <div class="unit-bar-track">
              <div class="unit-bar-fill"
                :style="{ width: w.accuracy + '%', background: w.accuracy < 50 ? '#EF4444' : '#F59E0B' }" />
            </div>
            <span class="unit-bar-label">{{ w.totalAttempts }}문제 시도</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 학습 캘린더 -->
    <div class="card" style="margin-top: 24px;">
      <div class="card__header">
        <h3>학습 캘린더</h3>
        <div style="display:flex;align-items:center;gap:16px;">
          <div class="cal-legend">
            <span><i class="legend-dot high"></i>80%↑</span>
            <span><i class="legend-dot mid"></i>50~79%</span>
            <span><i class="legend-dot low"></i>49%↓</span>
          </div>
          <div class="calendar-nav">
            <button @click="prevMonth">&lt;</button>
            <span>{{ calYear }}년 {{ calMonth }}월</span>
            <button @click="nextMonth">&gt;</button>
          </div>
        </div>
      </div>
      <div class="calendar">
        <div v-for="day in weekDays" :key="day" class="cal-weekday">{{ day }}</div>
        <div
          v-for="(cell, i) in calCells"
          :key="i"
          :class="['cal-cell', {
            'other-month': !cell.inMonth,
            'has-activity': cell.activity > 0,
            'today': cell.isToday,
            'is-sun': cell.dow === 0,
            'is-sat': cell.dow === 6,
            'accuracy-high': cell.activity > 0 && cell.accuracyLevel === 'high',
            'accuracy-mid': cell.activity > 0 && cell.accuracyLevel === 'mid',
            'accuracy-low': cell.activity > 0 && cell.accuracyLevel === 'low'
          }]"
        >
          <span class="cal-date">{{ cell.date }}</span>
          <div v-if="cell.activity > 0" class="cal-activity">
            <span class="cal-dot" />
            <span class="cal-count">{{ cell.activity }}문제</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import api from '@/utils/api'

const stats = ref({ accuracy: 0, totalSolved: 0, totalCorrect: 0, studyDays: 0 })
const levelData = ref([])
const weakUnits = ref([])
const aiComment = ref('')
const aiTags = ref([])
// [2026-03-21] 학습 이력 차트용 데이터
const historyData = ref([])
// [2026-04-03] 등급 변화 타임라인
const levelHistory = ref([])
const currentLevel = ref('N/A')

const LEVEL_COLORS = { A: '#3B82F6', B: '#10B981', C: '#F59E0B' }

onMounted(async () => {
  try {
    const [reportRes, aiRes, historyRes] = await Promise.all([
      api.get('/student/report'),
      api.get('/student/ai-comment'),
      api.get('/student/history', { params: { size: 30 } })
    ])
    const d = reportRes.data || {}
    stats.value = {
      accuracy: Math.min(Math.round(d.accuracy || 0), 100),
      totalSolved: Math.min(d.totalSolved || 0, 999999),
      totalCorrect: Math.min(d.totalCorrect || 0, 999999),
      studyDays: Math.min(d.studyDays || 0, 9999)
    }
    levelData.value = (d.levelData || []).map(l => ({
      level: l.level, count: l.count, pct: l.pct, color: LEVEL_COLORS[l.level] || '#3B82F6'
    }))
    weakUnits.value = (d.weakUnits || []).map(w => ({
      unitName: w.unitName || w.unit, subject: w.subject, accuracy: w.accuracy, totalAttempts: w.totalAttempts || 0
    }))
    levelHistory.value = d.levelHistory || []
    currentLevel.value = d.studentLevel || 'N/A'
    const ai = aiRes.data || {}
    aiComment.value = ai.comment || ''
    aiTags.value = ai.tags || []

    historyData.value = Array.isArray(historyRes.data) ? historyRes.data : (historyRes.data?.content || [])
  } catch {}
})

// ── SVG 차트 공통 상수 ──────────────────────────────
const SVG_W = 560, SVG_H = 180
const PAD_L = 44, PAD_R = 16, PAD_T = 16, PAD_B = 36
const IW = SVG_W - PAD_L - PAD_R
const IH = SVG_H - PAD_T - PAD_B

// y값 → SVG y좌표 (pct: 0~100)
function yVal(pct) {
  return PAD_T + IH - Math.max(0, Math.min(pct, 100)) / 100 * IH
}
// x 인덱스 → SVG x좌표
function xLine(i, total) {
  return PAD_L + (total <= 1 ? IW / 2 : (i / (total - 1)) * IW)
}

// ── Chart 1: 정답률 추이 (꺾은선) ──────────────────
const chartAccuracyData = computed(() => historyData.value.slice(0, 10).reverse())

const linePolyline = computed(() => {
  const data = chartAccuracyData.value
  if (data.length < 2) return ''
  return data.map((d, i) =>
    `${xLine(i, data.length).toFixed(1)},${yVal(d.accuracy).toFixed(1)}`
  ).join(' ')
})

const lineFillPath = computed(() => {
  const data = chartAccuracyData.value
  if (data.length < 2) return ''
  const pts = data.map((d, i) => `${xLine(i, data.length).toFixed(1)},${yVal(d.accuracy).toFixed(1)}`)
  const last = `${xLine(data.length - 1, data.length).toFixed(1)},${(PAD_T + IH).toFixed(1)}`
  const first = `${xLine(0, data.length).toFixed(1)},${(PAD_T + IH).toFixed(1)}`
  return [...pts, last, first].join(' ')
})

const lineDots = computed(() =>
  chartAccuracyData.value.map((d, i) => ({
    cx: xLine(i, chartAccuracyData.value.length),
    cy: yVal(d.accuracy),
    acc: d.accuracy,
    label: (d.date || '').slice(5) // "MM-DD"
  }))
)

// ── Chart 2: 일별 풀이량 (막대) ────────────────────
const chartSolvedData = computed(() => historyData.value.slice(0, 7).reverse())
const barMaxSolved = computed(() => Math.max(...chartSolvedData.value.map(d => d.problemCount || 0), 1))

const barRects = computed(() => {
  const data = chartSolvedData.value
  if (!data.length) return []
  const barW = IW / data.length * 0.55
  return data.map((d, i) => {
    const cx = PAD_L + (i + 0.5) / data.length * IW
    const h = ((d.problemCount || 0) / barMaxSolved.value) * IH
    return {
      x: cx - barW / 2, y: PAD_T + IH - h,
      w: barW, h, cx,
      label: (d.date || '').slice(5),
      count: d.problemCount || 0
    }
  })
})

// ── Chart 3: 이번달 vs 지난달 비교 ─────────────────
const now = new Date()
const thisYM = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
const prevDate = new Date(now.getFullYear(), now.getMonth() - 1, 1)
const lastYM = `${prevDate.getFullYear()}-${String(prevDate.getMonth() + 1).padStart(2, '0')}`

function monthStats(list) {
  if (!list.length) return { accuracy: 0, solved: 0, sessions: 0 }
  const totalAcc = list.reduce((s, d) => s + (d.accuracy || 0), 0)
  const totalSolved = list.reduce((s, d) => s + (d.problemCount || 0), 0)
  return {
    accuracy: Math.round(totalAcc / list.length),
    solved: totalSolved,
    sessions: list.length
  }
}

const monthComparison = computed(() => {
  const thisM = historyData.value.filter(d => (d.date || '').startsWith(thisYM))
  const lastM = historyData.value.filter(d => (d.date || '').startsWith(lastYM))
  return { thisMonth: monthStats(thisM), lastMonth: monthStats(lastM) }
})

const accuracyDiff = computed(() =>
  monthComparison.value.thisMonth.accuracy - monthComparison.value.lastMonth.accuracy
)

// ── 캘린더 ──────────────────────────────────────────
const today = new Date()
const calYear = ref(today.getFullYear())
const calMonth = ref(today.getMonth() + 1)
const weekDays = ['일', '월', '화', '수', '목', '금', '토']

const activities = computed(() => {
  const map = {}
  for (const h of (historyData.value || [])) {
    const [y, m, d] = h.date.split('-')
    const key = `${parseInt(y)}-${parseInt(m)}-${parseInt(d)}`
    if (!map[key]) map[key] = { count: 0, totalAccuracy: 0, sessions: 0 }
    map[key].count += h.problemCount || 0
    map[key].totalAccuracy += h.accuracy || 0
    map[key].sessions += 1
  }
  const result = {}
  for (const [key, val] of Object.entries(map)) {
    result[key] = {
      count: val.count,
      accuracy: val.sessions > 0 ? Math.round(val.totalAccuracy / val.sessions) : 0
    }
  }
  return result
})

const calCells = computed(() => {
  const firstDay = new Date(calYear.value, calMonth.value - 1, 1).getDay()
  const lastDate = new Date(calYear.value, calMonth.value, 0).getDate()
  const cells = []

  for (let i = 0; i < firstDay; i++) {
    const d = new Date(calYear.value, calMonth.value - 1, -firstDay + i + 1)
    cells.push({ date: d.getDate(), inMonth: false, activity: 0, isToday: false, dow: d.getDay() })
  }

  for (let d = 1; d <= lastDate; d++) {
    const key = `${calYear.value}-${calMonth.value}-${d}`
    const isToday = calYear.value === today.getFullYear() && calMonth.value === today.getMonth() + 1 && d === today.getDate()
    const dow = new Date(calYear.value, calMonth.value - 1, d).getDay()
    const act = activities.value[key]
    const accuracyLevel = act ? (act.accuracy >= 80 ? 'high' : act.accuracy >= 50 ? 'mid' : 'low') : ''
    cells.push({ date: d, inMonth: true, activity: act?.count || 0, accuracy: act?.accuracy || 0, accuracyLevel, isToday, dow })
  }

  return cells
})

function prevMonth() {
  if (calMonth.value === 1) { calMonth.value = 12; calYear.value-- }
  else calMonth.value--
}

function nextMonth() {
  if (calMonth.value === 12) { calMonth.value = 1; calYear.value++ }
  else calMonth.value++
}
</script>

<style scoped lang="scss">
.report-grid {
  display: grid;
  grid-template-columns: 280px 1fr 1fr;
  gap: $spacing-5;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr 1fr; }
  @media (max-width: $bp-mobile) { grid-template-columns: 1fr; }
}

.achievement-card {
  h3 { margin-bottom: $spacing-4; }
}

.donut-wrap {
  display: flex;
  justify-content: center;
  margin: $spacing-5 0;
}

.achievement-stats {
  display: flex;
  justify-content: space-around;
  padding-top: $spacing-4;
  border-top: 1px solid $border;
}

.ach-stat {
  text-align: center;

  strong {
    display: block;
    font-size: $font-size-xl;
    font-weight: 700;
    color: $primary;
  }

  span {
    font-size: $font-size-xs;
    color: $text-secondary;
  }
}

.level-bars {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
  margin-top: $spacing-4;
}

.level-bar-item {
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-2;
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.ai-report-card {
  .ai-card-header {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    margin-bottom: $spacing-4;

    h3 { font-size: $font-size-base; }
  }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: 1.7;
    margin-bottom: $spacing-4;
  }
}

.ai-badge {
  background: $primary;
  color: white;
  padding: 2px 7px;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 700;
}

.ai-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-2;
}

.ai-tag {
  background: $primary-bg;
  color: $primary;
  padding: 3px $spacing-3;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;
}

// ── 차트 섹션 ──────────────────────────────────────
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-5;
  margin-top: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }
}

.chart-card {
  h3 { margin-bottom: $spacing-1; }
}

.chart-sub {
  font-size: $font-size-xs;
  color: $text-muted;
  margin-bottom: $spacing-4;
}

.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-3;
  padding: $spacing-8 $spacing-4;
  color: $text-muted;

  p {
    font-size: $font-size-sm;
    color: $text-muted;
    text-align: center;
  }
}

.chart-svg {
  width: 100%;
  height: auto;
  display: block;
  overflow: visible;
}

.chart-svg-sm {
  width: 100%;
  max-width: 320px;
  height: auto;
  display: block;
  overflow: visible;
}

// Chart 3: 이번달 vs 지난달
.month-compare {
  display: flex;
  align-items: center;
  gap: $spacing-8;

  @media (max-width: $bp-mobile) { flex-direction: column; gap: $spacing-4; }

  &__chart { flex: 0 0 auto; }
  &__stats {
    display: flex;
    flex-direction: column;
    gap: $spacing-4;
    flex: 1;
  }
}

.mc-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-3 $spacing-4;
  background: $bg-light;
  border-radius: $radius-md;

  &--diff { background: transparent; border-top: 1px solid $border; padding-top: $spacing-4; }
}

.mc-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.mc-value {
  font-size: $font-size-lg;
  font-weight: 700;
  color: $primary;

  &.secondary { color: $text-muted; }
  &.up { color: #10B981; }
  &.down { color: #EF4444; }
}

// ── 등급 변화 타임라인 ─────────────────────────────
.level-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: $spacing-2 0;
}

.tl-item {
  display: flex;
  gap: $spacing-4;
  align-items: flex-start;

  &--current .tl-dot {
    box-shadow: 0 0 0 4px rgba(59,130,246,.2);
  }
}

.tl-line-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 32px;
}

.tl-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: white;
  font-size: $font-size-sm;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.tl-connector {
  width: 2px;
  height: 32px;
  background: $border;
  margin: 2px 0;
}

.tl-info {
  padding: 4px 0 28px;
}

.tl-date {
  font-size: $font-size-xs;
  color: $text-muted;
  margin-bottom: 2px;
}

.tl-detail {
  font-size: $font-size-sm;
  color: $text-secondary;

  strong { color: $text-primary; }
}

.tl-change {
  font-size: $font-size-xs;
  font-weight: 600;
  margin-left: $spacing-2;

  &.up   { color: #10B981; }
  &.down { color: #EF4444; }
  &.same { color: $text-muted; }
}

// ── 단원별 취약 분석 ───────────────────────────────
.unit-weak-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

.unit-weak-item {
  border-bottom: 1px solid $border;
  padding-bottom: $spacing-4;

  &:last-child { border-bottom: none; padding-bottom: 0; }
}

.unit-weak-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-2;

  .rank {
    width: 22px;
    height: 22px;
    font-size: 11px;
    margin-right: $spacing-2;
  }
}

.unit-name {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
}

.unit-subject {
  font-size: $font-size-xs;
  color: $text-muted;
  margin-left: $spacing-2;
}

.unit-accuracy {
  font-size: $font-size-sm;
  font-weight: 700;

  &.danger  { color: #EF4444; }
  &.warning { color: #F59E0B; }
}

.unit-bar-wrap {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.unit-bar-track {
  flex: 1;
  height: 8px;
  background: $border;
  border-radius: $radius-full;
  overflow: hidden;
}

.unit-bar-fill {
  height: 100%;
  border-radius: $radius-full;
  transition: width .4s ease;
}

.unit-bar-label {
  font-size: $font-size-xs;
  color: $text-muted;
  white-space: nowrap;
}

// ── 취약 단원 (구 TOP 5, 하위 호환) ──────────────────────────────────────
.weak-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.weak-item {
  display: flex;
  align-items: center;
  gap: $spacing-4;
}

.rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: $primary-bg;
  color: $primary;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: 700;
  flex-shrink: 0;
}

.weak-info {
  flex: 1;

  p {
    font-size: $font-size-sm;
    font-weight: 600;
    color: $text-primary;
  }

  span {
    font-size: $font-size-xs;
    color: $text-muted;
  }
}

.weak-progress {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  width: 180px;

  .progress-bar { flex: 1; }

  span {
    font-size: $font-size-sm;
    color: $danger;
    font-weight: 600;
    width: 36px;
    text-align: right;
  }
}

.progress-danger { background: $danger !important; }

// ── 캘린더 ────────────────────────────────────────
.calendar-nav {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  font-size: $font-size-sm;
  font-weight: 600;

  button {
    width: 28px;
    height: 28px;
    border-radius: $radius-sm;
    border: 1px solid $border;
    cursor: pointer;
    font-size: $font-size-sm;

    &:hover { background: $bg-light; }
  }
}

.calendar {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-top: 1px solid $border;
  border-left: 1px solid $border;
  margin-top: $spacing-3;
}

.cal-weekday {
  text-align: center;
  font-size: $font-size-xs;
  font-weight: 600;
  color: $text-muted;
  padding: $spacing-2;
  border-right: 1px solid $border;
  border-bottom: 1px solid $border;
  background: $bg-light;

  &:first-child { color: #EF4444; }
  &:nth-child(7) { color: #3B82F6; }
}

.cal-cell {
  min-height: 56px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 4px 6px;
  border-right: 1px solid $border;
  border-bottom: 1px solid $border;
  cursor: default;
  gap: 2px;
  position: relative;

  &.other-month { background: $bg-light; }
  &.other-month .cal-date { color: $text-muted; opacity: 0.4; }

  &.today .cal-date {
    background: $primary;
    color: white;
    border-radius: 50%;
    width: 22px;
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &.is-sun .cal-date { color: #EF4444; }
  &.is-sat .cal-date { color: #3B82F6; }

  &.today.is-sun .cal-date,
  &.today.is-sat .cal-date { color: white !important; }

  &.has-activity { background: #EFF6FF; }
  &.accuracy-high { background: #F0FDF4; }
  &.accuracy-mid  { background: #FFFBEB; }
  &.accuracy-low  { background: #FFF5F5; }

  .cal-date {
    font-size: $font-size-xs;
    font-weight: 500;
    color: $text-primary;
  }

  .cal-activity {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    width: 100%;
    margin-top: auto;
  }

  .cal-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }

  .cal-count {
    font-size: 10px;
    color: $text-muted;
    white-space: nowrap;
  }
}

.accuracy-high .cal-dot { background: #22C55E; }
.accuracy-mid  .cal-dot { background: #F59E0B; }
.accuracy-low  .cal-dot { background: #EF4444; }

.cal-legend {
  display: flex;
  gap: 12px;
  font-size: $font-size-xs;
  color: $text-muted;
  align-items: center;
}

.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;

  &.high { background: #22C55E; }
  &.mid  { background: #F59E0B; }
  &.low  { background: #EF4444; }
}
</style>
