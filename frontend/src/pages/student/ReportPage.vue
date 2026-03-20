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

    <!-- 취약 단원 TOP 5 -->
    <div class="card" style="margin-top: 24px;">
      <h3 style="margin-bottom: 20px;">취약 단원 TOP 5</h3>
      <div class="weak-list">
        <div v-for="(w, i) in weakUnits" :key="w.unit" class="weak-item">
          <span class="rank">{{ i + 1 }}</span>
          <div class="weak-info">
            <p>{{ w.unit }}</p>
            <span>{{ w.subject }}</span>
          </div>
          <div class="weak-progress">
            <div class="progress-bar">
              <div class="progress-bar__fill progress-danger" :style="{ width: w.accuracy + '%' }" />
            </div>
            <span>{{ w.accuracy }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 학습 캘린더 -->
    <div class="card" style="margin-top: 24px;">
      <div class="card__header">
        <h3>학습 캘린더</h3>
        <div class="calendar-nav">
          <button @click="prevMonth">&lt;</button>
          <span>{{ calYear }}년 {{ calMonth }}월</span>
          <button @click="nextMonth">&gt;</button>
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
            'is-sat': cell.dow === 6
          }]"
        >
          <span class="cal-date">{{ cell.date }}</span>
          <span v-if="cell.activity > 0" class="cal-dot" :style="{ opacity: Math.min(cell.activity / 30, 1) }" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppBadge from '@/components/common/AppBadge.vue'
import api from '@/utils/api'

const stats = ref({ accuracy: 0, totalSolved: 0, totalCorrect: 0, studyDays: 0 })
const levelData = ref([])
const weakUnits = ref([])
const aiComment = ref('')
const aiTags = ref([])

const LEVEL_COLORS = { A: '#3B82F6', B: '#10B981', C: '#F59E0B' }

onMounted(async () => {
  try {
    const [reportRes, aiRes] = await Promise.all([
      api.get('/student/report'),
      api.get('/student/ai-comment')
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
      unit: w.unitName || w.unit, subject: w.subject, accuracy: w.accuracy
    }))
    const ai = aiRes.data || {}
    aiComment.value = ai.comment || ''
    aiTags.value = ai.tags || []
  } catch {}
})

// 캘린더
const today = new Date()
const calYear = ref(today.getFullYear())
const calMonth = ref(today.getMonth() + 1)
const weekDays = ['일', '월', '화', '수', '목', '금', '토']

const activities = {}

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
    cells.push({ date: d, inMonth: true, activity: activities[key] || 0, isToday, dow })
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

  &.has-activity {
    background: #EFF6FF;
  }

  .cal-date {
    font-size: $font-size-xs;
    font-weight: 500;
    color: $text-primary;
  }

  .cal-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: $primary-light;
    margin-top: auto;
  }
}
</style>
