<template>
  <div class="assignment-list-page">
    <div class="page-header">
      <div>
        <h1>과제 관리</h1>
      </div>
      <RouterLink to="/teacher/assignments/create" class="btn btn-primary btn-md">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        과제 생성
      </RouterLink>
    </div>

    <div class="tabs">
      <button v-for="t in tabs" :key="t.value" :class="['tab', { active: activeTab === t.value }]" @click="activeTab = t.value">
        {{ t.label }} <span class="tab-count">{{ countByStatus(t.value) }}</span>
      </button>
    </div>

    <AppTable :columns="columns" :data="filteredAssignments">
      <template #cell-title="{ row }">
        <RouterLink :to="`/teacher/assignments/${row.id}`" class="assignment-link">{{ row.title }}</RouterLink>
      </template>
      <template #cell-status="{ value }">
        <span :class="['status-pill', value]">{{ statusLabel(value) }}</span>
      </template>
      <template #cell-completionRate="{ value }">
        <div style="display:flex;align-items:center;gap:8px;">
          <div class="progress-bar" style="width:80px;"><div class="progress-bar__fill" :style="{width:value+'%'}" /></div>
          <span style="font-size:12px;">{{ value }}%</span>
        </div>
      </template>
      <template #cell-actions="{ row }">
        <div style="display:flex;gap:6px;">
          <RouterLink :to="`/teacher/assignments/${row.id}`" class="btn btn-ghost btn-sm">상세</RouterLink>
          <button class="btn btn-secondary btn-sm" @click="deleteAssignment(row.id)">삭제</button>
        </div>
      </template>
    </AppTable>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const { success, error } = useToast()
const dialog = useDialog()
const activeTab = ref('all')

const tabs = [
  { label: '전체', value: 'all' },
  { label: '진행 중', value: 'active' },
  { label: '완료', value: 'done' },
  { label: '초안', value: 'draft' }
]

const columns = [
  { key: 'title', label: '과제명', sortable: true },
  { key: 'subject', label: '과목' },
  { key: 'targetCount', label: '대상 학생' },
  { key: 'dueDate', label: '마감일', sortable: true },
  { key: 'completionRate', label: '완료율', sortable: true },
  { key: 'status', label: '상태' },
  { key: 'actions', label: '' }
]

const DUMMY = [
  { id: 1, title: '1학기 중간고사 수학 복습', subject: '수학', targetCount: '28명', dueDate: '2026-03-20', completionRate: 72, status: 'active' },
  { id: 2, title: '영어 독해 연습 - Unit 3', subject: '영어', targetCount: '25명', dueDate: '2026-03-18', completionRate: 100, status: 'done' },
  { id: 3, title: '국어 문법 기초 과제', subject: '국어', targetCount: '30명', dueDate: '2026-03-25', completionRate: 45, status: 'active' },
  { id: 4, title: '과학 실험 보고서 작성', subject: '과학', targetCount: '28명', dueDate: '2026-03-28', completionRate: 0, status: 'draft' },
  { id: 5, title: '사회 지도 읽기 연습', subject: '사회', targetCount: '30명', dueDate: '2026-03-10', completionRate: 100, status: 'done' },
  { id: 6, title: '수학 함수 심화 문제', subject: '수학', targetCount: '15명', dueDate: '2026-04-01', completionRate: 20, status: 'active' },
  { id: 7, title: '영어 문법 총정리', subject: '영어', targetCount: '25명', dueDate: '2026-04-05', completionRate: 0, status: 'draft' },
]

const assignments = ref([])

const filteredAssignments = computed(() =>
  activeTab.value === 'all' ? assignments.value : assignments.value.filter(a => a.status === activeTab.value)
)

function countByStatus(v) {
  if (v === 'all') return assignments.value.length
  return assignments.value.filter(a => a.status === v).length
}

function statusLabel(v) {
  return { active: '진행 중', done: '완료', draft: '초안' }[v] || v
}

onMounted(async () => {
  try {
    const res = await api.get('/teacher/assignments')
    const list = (res.data?.content || res.data || []).map(a => ({
      id: a.assignmentId, title: a.title, subject: a.subject,
      targetCount: `${a.targetCount || 0}명`, dueDate: a.dueDate?.slice(0,10),
      completionRate: a.completionRate || 0, status: a.status?.toLowerCase()
    }))
    assignments.value = list.length ? list : DUMMY
  } catch { assignments.value = DUMMY }
})

async function deleteAssignment(id) {
  const ok = await dialog.confirm('과제를 삭제하시겠습니까?', { type: 'danger', confirmText: '삭제' })
  if (ok) {
    try {
      await api.delete(`/teacher/assignments/${id}`)
      assignments.value = assignments.value.filter(a => a.id !== id)
      success('과제를 삭제했습니다.')
    } catch { error('삭제에 실패했습니다.') }
  }
}
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.tabs {
  display: flex;
  gap: $spacing-2;
  margin-bottom: $spacing-5;
  border-bottom: 2px solid $border;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
}

.tab {
  padding: $spacing-3 $spacing-5;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-secondary;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  gap: $spacing-2;
  white-space: nowrap;
  flex-shrink: 0;

  &.active { color: $primary-light; border-bottom-color: $primary-light; font-weight: 600; }
  &:hover { color: $primary-light; }
}

.tab-count {
  background: $border;
  color: $text-muted;
  border-radius: $radius-full;
  padding: 1px 7px;
  font-size: $font-size-xs;
  font-weight: 600;
}

.assignment-link {
  font-weight: 600;
  color: $primary;
  text-decoration: none;

  &:hover { text-decoration: underline; }
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
</style>
