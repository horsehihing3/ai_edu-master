<template>
  <div class="teacher-home">
    <div class="page-header">
      <h1>교사 대시보드</h1>
      <p>{{ today }} 기준</p>
    </div>

    <!-- 요약 KPI -->
    <div class="kpi-grid">
      <div v-for="k in kpis" :key="k.label" class="kpi-card" :style="{ background: k.bg, borderTop: `4px solid ${k.color}` }">
        <div class="kpi-card__top">
          <p class="kpi-card__label">{{ k.label }}</p>
          <div class="kpi-icon" :style="{ background: k.bg, color: k.color }">
            <span v-html="k.icon" />
          </div>
        </div>
        <p class="kpi-card__value">{{ k.value }}</p>
        <p class="kpi-card__change">{{ k.change }}</p>
      </div>
    </div>

    <div class="teacher-grid">
      <!-- 최근 과제 -->
      <div class="card">
        <div class="card__header">
          <h3>최근 과제</h3>
          <RouterLink to="/teacher/assignments" class="btn btn-ghost btn-sm">전체보기</RouterLink>
        </div>
        <AppTable :columns="assignmentColumns" :data="recentAssignments">
          <template #cell-status="{ value }">
            <span :class="['status-pill', value]">{{ statusLabel(value) }}</span>
          </template>
          <template #cell-completionRate="{ value }">
            <div style="display:flex; align-items:center; gap:8px;">
              <div class="progress-bar" style="width:80px;">
                <div class="progress-bar__fill" :style="{ width: value + '%' }" />
              </div>
              <span style="font-size:12px; color:#6B7280;">{{ value }}%</span>
            </div>
          </template>
        </AppTable>
      </div>

      <!-- 미완료 학생 목록 -->
      <div class="card">
        <div class="card__header">
          <h3>미완료 학생 (상위 5명)</h3>
          <RouterLink to="/teacher/students" class="btn btn-ghost btn-sm">전체보기</RouterLink>
        </div>
        <div class="student-quick-list">
          <div v-for="s in incompleteStudents" :key="s.id" class="student-quick-item">
            <div class="avatar">{{ s.name.charAt(0) }}</div>
            <div class="info">
              <p class="name">{{ s.name }}</p>
              <p class="detail">{{ s.grade }} · 마지막 접속 {{ s.lastLogin }}</p>
            </div>
            <AppBadge :type="s.level" />
            <RouterLink :to="`/teacher/students/${s.id}`" class="btn btn-ghost btn-sm">상세</RouterLink>
          </div>
        </div>
      </div>
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

const kpis = ref([])
const recentAssignments = ref([])
const incompleteStudents = ref([])

const assignmentColumns = [
  { key: 'title', label: '과제명' },
  { key: 'subject', label: '과목' },
  { key: 'dueDate', label: '마감일' },
  { key: 'completionRate', label: '완료율' },
  { key: 'status', label: '상태' }
]

onMounted(async () => {
  try {
    const [statsRes, assignRes, studentRes] = await Promise.all([
      api.get('/teacher/dashboard/stats'),
      api.get('/teacher/assignments?size=5&sort=createdAt,desc'),
      api.get('/teacher/students/incomplete?size=5')
    ])
    const s = statsRes.data || {}
    kpis.value = [
      { label: '담당 학생 수', value: `${s.studentCount || 0}명`, change: s.studentChange || '', color: '#3B82F6', bg: '#DBEAFE', icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>` },
      { label: '활성 과제', value: `${s.activeAssignments || 0}개`, change: '진행 중', color: '#10B981', bg: '#D1FAE5', icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>` },
      { label: '평균 완료율', value: `${s.avgCompletionRate || 0}%`, change: s.completionChange || '', color: '#F59E0B', bg: '#FEF3C7', icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>` },
      { label: '미완료 학생', value: `${s.incompleteCount || 0}명`, change: '즉시 확인 필요', color: '#EF4444', bg: '#FEE2E2', icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>` }
    ]
    recentAssignments.value = (assignRes.data?.content || assignRes.data || []).map(a => ({
      title: a.title, subject: a.subject, dueDate: a.dueDate?.slice(0,10),
      completionRate: a.completionRate || 0, status: a.status?.toLowerCase()
    }))
    incompleteStudents.value = (studentRes.data || []).map(s => ({
      id: s.studentId, name: s.name, grade: s.grade, level: s.level, lastLogin: s.lastLogin || '-'
    }))
  } catch {}
})

function statusLabel(v) {
  return { active: '진행 중', done: '완료', draft: '초안' }[v] || v
}
</script>

<style scoped lang="scss">
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-5;
  margin-bottom: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: $bp-mobile) { grid-template-columns: repeat(2, 1fr); gap: $spacing-3; }
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
  &__change { font-size: $font-size-xs; color: $text-muted; margin-top: $spacing-1; }
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

.teacher-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }

  .card { min-width: 0; }
}

.status-pill {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;

  &.active { background: #DBEAFE; color: #1E40AF; }
  &.done { background: #D1FAE5; color: #065F46; }
  &.draft { background: #F3F4F6; color: #6B7280; }
}

.student-quick-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.student-quick-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3;
  border-radius: $radius-md;
  border: 1px solid $border;

  &:hover { background: $bg-light; }

  .avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: $primary-bg;
    color: $primary;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: $font-size-sm;
    flex-shrink: 0;
  }

  .info {
    flex: 1;
    min-width: 0;

    .name {
      font-size: $font-size-sm;
      font-weight: 600;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .detail {
      font-size: $font-size-xs;
      color: $text-muted;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>
