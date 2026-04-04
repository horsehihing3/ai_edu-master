<template>
  <div class="teacher-analytics">
    <div class="page-header">
      <div class="page-header__left">
        <h1>학습 현황</h1>
        <p>{{ today }} 기준</p>
      </div>
      <!-- [2026-04-04] 학급 선택 드롭다운 -->
      <div class="class-filter">
        <select v-model="selectedClassId" @change="loadAnalytics" class="class-select">
          <option :value="null">전체 학교</option>
          <option v-for="c in classes" :key="c.classId" :value="c.classId">{{ c.className }}</option>
        </select>
      </div>
    </div>

    <!-- KPI -->
    <div class="kpi-grid">
      <div v-for="k in kpis" :key="k.label" class="kpi-card" :style="{ background: k.bg, borderTop: `4px solid ${k.color}` }">
        <div class="kpi-card__top">
          <p class="kpi-card__label">{{ k.label }}</p>
          <div class="kpi-icon" :style="{ background: k.bg, color: k.color }">
            <span v-html="k.icon" />
          </div>
        </div>
        <p class="kpi-card__value">{{ k.value }}</p>
        <p class="kpi-card__sub">{{ k.sub }}</p>
      </div>
    </div>

    <div class="analytics-grid">
      <!-- 레벨별 학생 분포 -->
      <div class="card">
        <div class="card__header"><h3>레벨별 학생 분포</h3></div>
        <div class="level-dist-wrap">
          <!-- SVG 파이차트 -->
          <div class="pie-wrap">
            <svg viewBox="0 0 100 100" width="140" height="140">
              <template v-for="(slice, i) in pieSlices" :key="i">
                <path :d="slice.d" :fill="slice.color" />
              </template>
              <circle cx="50" cy="50" r="28" fill="white" />
              <text x="50" y="47" text-anchor="middle" font-size="10" font-weight="700" fill="#1F2937">전체</text>
              <text x="50" y="59" text-anchor="middle" font-size="11" font-weight="700" fill="#1F2937">{{ totalStudentCount }}명</text>
            </svg>
            <div class="pie-legend">
              <div v-for="l in levelDist" :key="l.level" class="pie-legend-item">
                <span class="pie-dot" :style="{ background: l.color }" />
                <span>{{ l.level }}레벨</span>
              </div>
            </div>
          </div>
          <!-- 바차트 -->
          <div class="level-bars">
            <div v-for="l in levelDist" :key="l.level" class="level-bar-row">
              <span class="level-label">{{ l.level }}레벨</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: l.pct + '%', background: l.color }" />
              </div>
              <span class="bar-count">{{ l.count }}명 ({{ l.pct }}%)</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 과목별 평균 정답률 -->
      <div class="card">
        <div class="card__header"><h3>과목별 평균 정답률</h3></div>
        <div class="subject-bars">
          <div v-for="s in subjectStats" :key="s.subject" class="subject-row">
            <span class="subject-name">{{ s.subject }}</span>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: s.rate + '%', background: '#6366F1' }" />
            </div>
            <span class="rate-value">{{ s.rate }}%</span>
          </div>
        </div>
      </div>

      <!-- 주간 학습 참여율 -->
      <div class="card card--full">
        <div class="card__header"><h3>주간 학습 참여율 추이</h3></div>
        <div class="week-chart">
          <div v-for="w in weeklyData" :key="w.week" class="week-col">
            <div class="week-bar-wrap">
              <div class="week-bar" :style="{ height: w.rate + '%' }">
                <span class="week-bar-label">{{ w.rate }}%</span>
              </div>
            </div>
            <span class="week-label">{{ w.week }}</span>
          </div>
        </div>
      </div>

      <!-- [2026-04-03] 취약 단원별 학생 분석 -->
      <div class="card card--full">
        <div class="card__header"><h3>취약 단원별 분석</h3></div>
        <div v-if="!unitWeakStats.length" class="unit-empty">
          <p>단원 데이터가 없습니다. 문제에 단원 코드가 등록되면 표시됩니다.</p>
        </div>
        <div v-else>
          <div class="unit-weak-grid">
            <div v-for="(u, i) in unitWeakStats" :key="i" class="unit-weak-card">
              <div class="unit-weak-card__header">
                <div>
                  <span class="uwc-subject">{{ u.subject }}</span>
                  <p class="uwc-name">{{ u.unitName }}</p>
                </div>
                <span :class="['uwc-rate', u.avgAccuracy < 50 ? 'danger' : 'warning']">
                  {{ u.avgAccuracy }}%
                </span>
              </div>
              <div class="uwc-bar-track">
                <div class="uwc-bar-fill"
                  :style="{ width: u.avgAccuracy + '%', background: u.avgAccuracy < 50 ? '#EF4444' : '#F59E0B' }" />
              </div>
              <p class="uwc-meta">{{ u.studentCount }}명 학습</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 학생별 학습 현황 -->
      <div class="card card--full">
        <div class="card__header"><h3>학생별 학습 현황</h3></div>
        <div class="table-scroll">
        <table class="stats-table">
          <thead>
            <tr>
              <th>이름</th>
              <th>레벨</th>
              <th>이번 주 학습</th>
              <th>과제 완료율</th>
              <th>평균 정답률</th>
              <th>최근 접속</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in studentStats" :key="s.name">
              <td>{{ s.name }}</td>
              <td><span class="level-badge" :class="s.level.toLowerCase()">{{ s.level }}</span></td>
              <td>{{ s.weekStudy }}문제</td>
              <td>
                <div class="mini-bar-track">
                  <div class="mini-bar-fill" :style="{ width: s.assignRate + '%' }" />
                </div>
                <span>{{ s.assignRate }}%</span>
              </td>
              <td>{{ s.correctRate }}%</td>
              <td class="muted">{{ s.lastAccess }}</td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/utils/api'

const today = new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })

const kpis = ref([
  { label: '담당 학생 수',      value: '-', sub: '', color: '#3B82F6', bg: '#DBEAFE',
    icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>` },
  { label: '이번 주 평균 학습량', value: '-', sub: '', color: '#10B981', bg: '#D1FAE5',
    icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>` },
  { label: '전체 평균 정답률',   value: '-', sub: '', color: '#F59E0B', bg: '#FEF3C7',
    icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>` },
  { label: '과제 평균 완료율',   value: '-', sub: '', color: '#EF4444', bg: '#FEE2E2',
    icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>` }
])
const levelDist = ref([])

// [2026-04-01] SVG 파이차트 슬라이스 계산
const totalStudentCount = computed(() => levelDist.value.reduce((sum, l) => sum + (l.count || 0), 0))
const pieSlices = computed(() => {
  const total = totalStudentCount.value
  if (total === 0) return []
  let startAngle = -Math.PI / 2
  return levelDist.value.map(l => {
    const angle = (l.count / total) * 2 * Math.PI
    const endAngle = startAngle + angle
    const x1 = 50 + 50 * Math.cos(startAngle)
    const y1 = 50 + 50 * Math.sin(startAngle)
    const x2 = 50 + 50 * Math.cos(endAngle)
    const y2 = 50 + 50 * Math.sin(endAngle)
    const largeArc = angle > Math.PI ? 1 : 0
    const d = `M50,50 L${x1},${y1} A50,50 0 ${largeArc},1 ${x2},${y2} Z`
    startAngle = endAngle
    return { d, color: l.color }
  })
})

const subjectStats = ref([])
const weeklyData = ref([])
const studentStats = ref([])
// [2026-04-03] 단원별 취약 분석
const unitWeakStats = ref([])

// [2026-04-04] 학급 필터
const classes = ref([])
const selectedClassId = ref(null)

async function loadAnalytics() {
  try {
    const params = selectedClassId.value != null ? { classId: selectedClassId.value } : {}
    const res = await api.get('/teacher/analytics', { params })
    const d = res.data || {}
    if (d.kpis) {
      d.kpis.forEach((k, i) => {
        if (kpis.value[i]) {
          kpis.value[i].value = k.value ?? kpis.value[i].value
          kpis.value[i].sub   = k.sub   ?? kpis.value[i].sub
        }
      })
    }
    levelDist.value = (d.levelDist || []).map(l => ({
      level: l.level, count: l.count, pct: l.pct,
      color: { A: '#1e3a8a', B: '#064e3b', C: '#78350f' }[l.level] || '#1e3a8a'
    }))
    subjectStats.value = (d.subjectStats || []).map(s => ({ subject: s.subject, rate: s.rate || s.avgAccuracy || 0 }))
    weeklyData.value = (d.weeklyData || []).map(w => ({ week: w.week, rate: w.rate || w.participationRate || 0 }))
    studentStats.value = (d.studentStats || []).map(s => ({
      name: s.name, level: s.level, weekStudy: s.weekStudy || 0,
      assignRate: s.assignRate || 0, correctRate: s.correctRate || 0, lastAccess: s.lastAccess || '-'
    }))
    unitWeakStats.value = d.unitWeakStats || []
  } catch {}
}

onMounted(async () => {
  try {
    const res = await api.get('/teacher/classes')
    classes.value = res.data || []
  } catch {}
  loadAnalytics()
})
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-3;
  margin-bottom: $spacing-6;

  &__left {
    display: flex;
    align-items: baseline;
    gap: $spacing-3;
  }

  h1 { font-size: $font-size-2xl; font-weight: 700; }
  p  { font-size: $font-size-sm; color: $text-muted; }
}

// [2026-04-04] 학급 필터 드롭다운
.class-filter { flex-shrink: 0; }

.class-select {
  padding: $spacing-2 $spacing-3;
  border: 1px solid $border;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  background: white;
  color: $text-primary;
  cursor: pointer;
  min-width: 140px;

  &:focus { outline: none; border-color: $primary; }
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-4;
  margin-bottom: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: repeat(2, 1fr); }
}

.kpi-card {
  background: white;
  border-radius: $radius-lg;
  border: 1px solid $border;
  padding: $spacing-5;

  &__top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-3;
  }
  &__label { font-size: $font-size-xs; color: $text-muted; }
  &__value { font-size: $font-size-2xl; font-weight: 700; color: $text-primary; }
  &__sub   { font-size: $font-size-xs; color: $text-muted; margin-top: $spacing-1; }
}

.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.analytics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-5;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }

  .card { min-width: 0; }
}

.card--full { grid-column: 1 / -1; }

.table-scroll {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.level-dist-wrap {
  display: flex;
  align-items: center;
  gap: $spacing-6;
  padding: $spacing-2 0;

  @media (max-width: $bp-tablet) { flex-direction: column; }
}

.pie-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-3;
  flex-shrink: 0;
}

.pie-legend {
  display: flex;
  gap: $spacing-3;
}

.pie-legend-item {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  font-size: $font-size-xs;
  color: $text-secondary;
}

.pie-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.level-bars, .subject-bars {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  padding: $spacing-2 0;
}

.level-bar-row, .subject-row {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.level-label, .subject-name {
  font-size: $font-size-sm;
  font-weight: 600;
  width: 56px;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 10px;
  background: $bg-light;
  border-radius: $radius-full;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: $radius-full;
  transition: width 0.6s ease;
}

.bar-count, .rate-value {
  font-size: $font-size-xs;
  color: $text-muted;
  width: 80px;
  text-align: right;
  flex-shrink: 0;
}

// 주간 차트
// [2026-04-04] align-items: stretch — 컬럼이 140px 전체를 채워야 week-bar의 height:%가 계산됨
.week-chart {
  display: flex;
  align-items: stretch;
  gap: $spacing-4;
  height: 140px;
  padding: $spacing-2 0;
}

.week-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-2;
  flex: 1;
}

.week-bar-wrap {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
}

.week-bar {
  width: 100%;
  background: $primary;
  border-radius: $radius-sm $radius-sm 0 0;
  min-height: 4px;
  transition: height 0.6s ease;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  overflow: visible;
}

.week-bar-label {
  font-size: 10px;
  font-weight: 600;
  color: $primary;
  margin-top: -18px;
  white-space: nowrap;
}

.week-label {
  font-size: $font-size-xs;
  color: $text-muted;
}

// 테이블
.stats-table {
  width: 100%;
  min-width: 540px;
  border-collapse: collapse;

  th {
    text-align: left;
    font-size: $font-size-xs;
    font-weight: 600;
    color: $text-muted;
    padding: $spacing-2 $spacing-3;
    border-bottom: 1px solid $border;
  }

  td {
    font-size: $font-size-sm;
    padding: $spacing-3;
    border-bottom: 1px solid $border;
    vertical-align: middle;

    &:last-child { border-bottom: none; }
  }

  tr:last-child td { border-bottom: none; }
}

.level-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: $font-size-xs;
  font-weight: 700;
  color: white;

  &.a { background: #1e3a8a; }
  &.b { background: #064e3b; }
  &.c { background: #78350f; }
}

.mini-bar-track {
  display: inline-block;
  width: 60px;
  height: 6px;
  background: $bg-light;
  border-radius: $radius-full;
  overflow: hidden;
  margin-right: $spacing-2;
  vertical-align: middle;
}

.mini-bar-fill {
  height: 100%;
  background: $primary;
  border-radius: $radius-full;
}

.muted { color: $text-muted; }

// ── [2026-04-03] 취약 단원별 분석 ──────────────────
.unit-empty {
  padding: $spacing-6;
  text-align: center;
  color: $text-muted;
  font-size: $font-size-sm;
}

.unit-weak-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-4;
  padding: $spacing-2 0;
}

.unit-weak-card {
  border: 1px solid $border;
  border-radius: $radius-md;
  padding: $spacing-4;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-3;
  }
}

.uwc-subject {
  font-size: $font-size-xs;
  color: $text-muted;
  display: block;
  margin-bottom: 2px;
}

.uwc-name {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
}

.uwc-rate {
  font-size: $font-size-lg;
  font-weight: 700;
  flex-shrink: 0;

  &.danger  { color: #EF4444; }
  &.warning { color: #F59E0B; }
}

.uwc-bar-track {
  height: 6px;
  background: $bg-light;
  border-radius: $radius-full;
  overflow: hidden;
  margin-bottom: $spacing-2;
}

.uwc-bar-fill {
  height: 100%;
  border-radius: $radius-full;
  transition: width .4s ease;
}

.uwc-meta {
  font-size: $font-size-xs;
  color: $text-muted;
}
</style>
