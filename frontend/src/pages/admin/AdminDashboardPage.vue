<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h1>관리자 대시보드</h1>
      <p>{{ today }} 기준</p>
    </div>

    <!-- KPI 카드 -->
    <div class="kpi-grid">
      <div v-for="k in kpis" :key="k.label" class="kpi-card" :style="{ background: k.bg, borderTop: `4px solid ${k.color}` }">
        <div class="kpi-card__top">
          <p class="kpi-card__label">{{ k.label }}</p>
          <div class="kpi-icon" :style="{ background: k.bg, color: k.color }">
            <span v-html="k.icon" />
          </div>
        </div>
        <p class="kpi-card__value">{{ k.value }}</p>
        <p :class="['kpi-card__change', k.trend > 0 ? 'up' : 'down']">
          {{ k.trend > 0 ? '▲' : '▼' }} {{ Math.abs(k.trend) }}% 전일 대비
        </p>
      </div>
    </div>

    <!-- 차트 영역 (간단한 막대 시각화) -->
    <div class="chart-grid">
      <div class="card">
        <h3>일일 활성 사용자 (최근 7일)</h3>
        <div class="bar-chart">
          <div v-for="d in dauData" :key="d.date" class="bar-item">
            <span class="bar-value">{{ d.value || '' }}</span>
            <div class="bar-col">
              <div class="bar" :style="{ height: (d.value / maxDau * 100) + '%', background: '#3B82F6' }" />
            </div>
            <span class="bar-label">{{ d.date }}</span>
          </div>
        </div>
      </div>

      <div class="card">
        <h3>일일 풀이 수 (최근 7일)</h3>
        <div class="bar-chart">
          <div v-for="d in solvedData" :key="d.date" class="bar-item">
            <span class="bar-value">{{ d.value || '' }}</span>
            <div class="bar-col">
              <div class="bar" :style="{ height: (d.value / maxSolved * 100) + '%', background: '#10B981' }" />
            </div>
            <span class="bar-label">{{ d.date }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 레벨별 분포 + 문의 -->
    <div class="chart-grid">
      <div class="card">
        <h3>레벨별 학생 분포</h3>
        <div class="pie-list">
          <div v-for="l in levelDist" :key="l.level" class="pie-item">
            <div class="pie-bar" :style="{ width: l.pct + '%', background: l.color }" />
            <AppBadge :type="l.level" />
            <span>{{ l.count }}명 ({{ l.pct }}%)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 최근 문의 -->
    <div class="card">
      <div class="card__header">
        <h3>최근 1:1 문의</h3>
        <RouterLink to="/admin/inquiries" class="btn btn-ghost btn-sm">전체보기</RouterLink>
      </div>
      <AppTable :columns="inquiryColumns" :data="recentInquiries">
        <template #cell-status="{ value }">
          <span :class="['mini-badge', value === 'answered' ? 'done' : 'pending']">
            {{ value === 'answered' ? '답변완료' : '미답변' }}
          </span>
        </template>
      </AppTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import api from '@/utils/api'

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')}`
})

const kpiIcons = [
  `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>`,
  `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
  `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>`,
  `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`
]
const kpiColors = ['#3B82F6','#10B981','#F59E0B','#8B5CF6']
const kpiBgs   = ['#DBEAFE','#D1FAE5','#FEF3C7','#EDE9FE']

const kpis = ref([])
const dauData = ref([])
const solvedData = ref([])
const levelDist = ref([])
const recentInquiries = ref([])

const maxDau = computed(() => dauData.value.length ? Math.max(...dauData.value.map(d => d.value)) : 1)
const maxSolved = computed(() => solvedData.value.length ? Math.max(...solvedData.value.map(d => d.value)) : 1)

const inquiryColumns = [
  { key: 'id', label: 'No' },
  { key: 'userName', label: '작성자' },
  { key: 'title', label: '제목' },
  { key: 'createdAt', label: '작성일' },
  { key: 'status', label: '상태' }
]

onMounted(async () => {
  try {
    const [statsRes, dauRes, solvedRes, levelRes, inquiryRes] = await Promise.all([
      api.get('/admin/dashboard/stats'),
      api.get('/admin/dashboard/dau'),
      api.get('/admin/dashboard/solved'),
      api.get('/admin/dashboard/level-dist'),
      api.get('/admin/inquiries?size=5&sort=createdAt,desc')
    ])
    const s = statsRes.data || {}
    kpis.value = [
      { label: 'DAU (일일 활성 사용자)', value: (s.dau || 0).toLocaleString(), trend: s.dauTrend || 0, color: kpiColors[0], bg: kpiBgs[0], icon: kpiIcons[0] },
      { label: 'MAU (월간 활성 사용자)', value: (s.mau || 0).toLocaleString(), trend: s.mauTrend || 0, color: kpiColors[1], bg: kpiBgs[1], icon: kpiIcons[1] },
      { label: '일일 풀이 수', value: (s.dailySolved || 0).toLocaleString(), trend: s.solvedTrend || 0, color: kpiColors[2], bg: kpiBgs[2], icon: kpiIcons[2] },
      { label: '활성 구독자', value: (s.activeSubscribers || 0).toLocaleString(), trend: s.subscriberTrend || 0, color: kpiColors[3], bg: kpiBgs[3], icon: kpiIcons[3] }
    ]
    dauData.value = (dauRes.data || []).map(d => ({ date: d.date, value: d.value }))
    solvedData.value = (solvedRes.data || []).map(d => ({ date: d.date, value: d.value }))
    levelDist.value = (levelRes.data || []).map((l, i) => ({
      level: l.level, count: l.count, pct: l.pct,
      color: ['#3B82F6','#10B981','#F59E0B'][i] || '#6B7280'
    }))
    recentInquiries.value = (inquiryRes.data?.content || inquiryRes.data || []).map(q => ({
      id: q.inquiryId, userName: q.userName, title: q.title,
      createdAt: q.createdAt?.slice(0,16)?.replace('T',' '), status: q.status?.toLowerCase()
    }))
  } catch {}
})
</script>

<style scoped lang="scss">
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-5;
  margin-bottom: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: repeat(2, 1fr); }
}

.kpi-card {
  &__top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-3;
  }
  &__label { font-size: $font-size-xs; color: $text-muted; }
  &__value { font-size: $font-size-2xl; font-weight: 700; color: $text-primary; }
  &__change {
    font-size: $font-size-xs;
    margin-top: $spacing-1;
    &.up   { color: #10B981; }
    &.down { color: #EF4444; }
  }
}

.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-5;
  margin-bottom: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }

  h3 { font-size: $font-size-base; font-weight: 700; margin-bottom: $spacing-5; }
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: $spacing-2;
  height: 160px;
}

.bar-value {
  font-size: 22px;
  font-weight: 700;
  color: #3B82F6;
  min-height: 28px;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  gap: $spacing-1;

  .bar-col {
    flex: 1;
    width: 100%;
    display: flex;
    align-items: flex-end;

    .bar {
      width: 100%;
      border-radius: $radius-sm $radius-sm 0 0;
      min-height: 4px;
      transition: all $transition-base;

      &:hover { opacity: 0.8; }
    }
  }

  .bar-label {
    font-size: 10px;
    color: $text-muted;
    white-space: nowrap;
  }
}

.pie-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  padding-top: $spacing-4;
}

.pie-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  font-size: $font-size-sm;
  color: $text-secondary;

  .pie-bar {
    height: 12px;
    border-radius: $radius-full;
    max-width: 200px;
  }
}

.mini-badge {
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: 11px;
  font-weight: 600;

  &.done { background: #D1FAE5; color: #065F46; }
  &.pending { background: #FEF3C7; color: #92400E; }
}
</style>
