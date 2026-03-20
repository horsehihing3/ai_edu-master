<template>
  <div class="student-detail">
    <AppBreadcrumb :items="[{ label: '학생 관리', to: '/teacher/students' }, { label: student.name }]" />
    <div class="detail-header card">
      <div class="student-profile">
        <div class="avatar">{{ student.name.charAt(0) }}</div>
        <div>
          <h2>{{ student.name }}</h2>
          <p>{{ student.grade }} · {{ student.email }}</p>
        </div>
        <AppBadge :type="student.level" :text="`${student.level} 레벨`" style="font-size: 14px; padding: 4px 12px;" />
      </div>
      <div class="level-change">
        <p class="label">레벨 변경</p>
        <div class="level-btns">
          <button v-for="l in ['A','B','C']" :key="l" :class="['level-btn', { active: student.level === l }]" @click="changeLevel(l)">{{ l }}</button>
        </div>
      </div>
    </div>

    <!-- 학습 통계 -->
    <div class="stat-grid">
      <div class="kpi-card" v-for="k in kpis" :key="k.label" :style="{ background: k.bg, borderTop: '4px solid ' + k.border }">
        <p class="kpi-card__label">{{ k.label }}</p>
        <p class="kpi-card__value">{{ k.value }}</p>
      </div>
    </div>

    <!-- 취약 단원 -->
    <div class="card" v-if="weakUnits.length">
      <h3 class="card-title">취약 단원</h3>
      <div class="weak-list">
        <div v-for="w in weakUnits" :key="w.unit" class="weak-item">
          <span class="subject-tag">{{ w.subject }}</span>
          <span class="unit-name">{{ w.unit }}</span>
          <div class="progress-bar" style="width:120px;">
            <div class="progress-bar__fill" style="background:#EF4444;" :style="{ width: w.accuracy + '%' }" />
          </div>
          <span class="accuracy">{{ w.accuracy }}%</span>
        </div>
      </div>
    </div>

    <!-- 최근 학습 이력 -->
    <div class="card">
      <h3 class="card-title">학습 이력</h3>
      <AppTable :columns="historyColumns" :data="history">
        <template #cell-accuracy="{ value }">
          <span :style="{ color: value >= 80 ? '#10B981' : value >= 60 ? '#F59E0B' : '#EF4444', fontWeight: '600' }">{{ value }}%</span>
        </template>
      </AppTable>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppBreadcrumb from '@/components/common/AppBreadcrumb.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppTable from '@/components/common/AppTable.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const route = useRoute()
const { success, error } = useToast()

const student = ref({ id: route.params.id, name: '', grade: '', email: '', level: 'B' })
const kpis = ref([])
const weakUnits = ref([])
const history = ref([])

const historyColumns = [
  { key: 'date', label: '날짜' },
  { key: 'assignment', label: '과제' },
  { key: 'problemCount', label: '문제 수' },
  { key: 'accuracy', label: '정답률' },
  { key: 'time', label: '학습 시간' }
]

onMounted(async () => {
  try {
    const [infoRes, statsRes, weakRes, histRes] = await Promise.all([
      api.get(`/teacher/students/${route.params.id}`),
      api.get(`/teacher/students/${route.params.id}/stats`),
      api.get(`/teacher/students/${route.params.id}/weak-units`),
      api.get(`/teacher/students/${route.params.id}/history?size=10`)
    ])
    const s = infoRes.data || {}
    student.value = { id: s.studentId, name: s.name, grade: s.grade, email: s.email, level: s.studentLevel || 'B' }
    const st = statsRes.data || {}
    kpis.value = [
      { label: '총 풀이 수', value: `${st.totalSolved || 0}문제`, bg: '#DBEAFE', border: '#3B82F6' },
      { label: '정답률', value: `${st.accuracy || 0}%`, bg: '#D1FAE5', border: '#10B981' },
      { label: '완료율', value: `${st.completionRate || 0}%`, bg: '#FEF3C7', border: '#F59E0B' },
      { label: '학습일', value: `${st.studyDays || 0}일`, bg: '#EDE9FE', border: '#8B5CF6' }
    ]
    weakUnits.value = (weakRes.data || []).map(w => ({ subject: w.subject, unit: w.unitName, accuracy: w.accuracy }))
    history.value = (histRes.data?.content || histRes.data || []).map(h => ({
      date: h.date?.slice(0,10), assignment: h.assignmentTitle || '-',
      problemCount: h.problemCount, accuracy: h.accuracy,
      time: h.timeSpentMin ? `${h.timeSpentMin}분` : '-'
    }))
  } catch {}
})

async function changeLevel(l) {
  try {
    await api.put(`/teacher/students/${route.params.id}/level`, { level: l })
    student.value.level = l
    success(`레벨을 ${l}로 변경했습니다.`)
  } catch { error('레벨 변경에 실패했습니다.') }
}
</script>

<style scoped lang="scss">
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-5;
  flex-wrap: wrap;
  gap: $spacing-5;
}

.student-profile {
  display: flex;
  align-items: center;
  gap: $spacing-4;

  .avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: $primary;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-2xl;
    font-weight: 700;
  }

  h2 {
    font-size: $font-size-2xl;
    font-weight: 700;
  }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-top: 2px;
  }
}

.level-change {
  .label {
    font-size: $font-size-sm;
    font-weight: 600;
    color: $text-secondary;
    margin-bottom: $spacing-2;
  }
}

.level-btns {
  display: flex;
  gap: $spacing-2;
}

.level-btn {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  border: 2px solid $border;
  background: $bg-white;
  font-weight: 700;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover { border-color: $primary-light; color: $primary-light; }
  &.active { border-color: $primary-light; background: $primary-bg; color: $primary; }
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-5;
  margin-bottom: $spacing-5;

  @media (max-width: $bp-mobile) { grid-template-columns: repeat(2, 1fr); }
}

.card-title {
  font-size: $font-size-lg;
  font-weight: 700;
  margin-bottom: $spacing-5;
}

.weak-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.weak-item {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  padding: $spacing-3;
  border-radius: $radius-sm;
  border: 1px solid $border;

  .unit-name {
    flex: 1;
    font-size: $font-size-sm;
    color: $text-primary;
  }

  .accuracy {
    font-size: $font-size-sm;
    font-weight: 600;
    color: $danger;
    width: 36px;
    text-align: right;
  }
}

.subject-tag {
  background: $primary-bg;
  color: $primary;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 600;
}
</style>
