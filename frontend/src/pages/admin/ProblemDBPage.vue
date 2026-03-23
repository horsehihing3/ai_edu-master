<template>
  <div class="problem-db-page">
    <div class="page-header">
      <h1>문제 DB 관리</h1>
      <div class="page-header__actions">
        <RouterLink to="/admin/problems/upload" class="btn btn-secondary btn-md">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          파일 업로드
        </RouterLink>
        <button class="btn btn-primary btn-md" @click="openAddModal">문제 추가</button>
      </div>
    </div>

    <div class="filter-bar">
      <input v-model="search" class="form-control filter-search" placeholder="문제 검색..." />
      <div class="filter-selects">
        <AppSelect v-model="filterLevel" :options="levelOptions" placeholder="전체 레벨" style="margin:0;" />
        <!-- [2026-03-23] 검수 상태 필터 추가 -->
        <AppSelect v-model="filterApproval" :options="approvalOptions" placeholder="전체 상태" style="margin:0;" />
      </div>
    </div>

    <!-- [2026-03-23] 검수 대기 건수 배너 -->
    <div v-if="pendingCount > 0" class="pending-banner">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      검수 대기 문제가 <strong>{{ pendingCount }}건</strong> 있습니다.
      <button class="btn-link" @click="filterApproval = 'PENDING'; page = 1; fetchProblems()">바로가기</button>
    </div>

    <AppTable :columns="columns" :data="problems" :loading="loading">
      <template #cell-level="{ value }">
        <AppBadge :type="value" />
      </template>
      <template #cell-questionText="{ value }">
        <span style="max-width:260px; display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">{{ value }}</span>
      </template>
      <template #cell-approvalStatus="{ value }">
        <span :class="['approval-badge', `approval-badge--${value?.toLowerCase()}`]">
          {{ approvalLabel(value) }}
        </span>
      </template>
      <template #cell-actions="{ row }">
        <div style="display:flex;gap:6px;flex-wrap:wrap;">
          <!-- [2026-03-23] 검수 액션 버튼 -->
          <template v-if="row.approvalStatus === 'PENDING'">
            <button class="btn btn-success btn-sm" @click="approveProblem(row)">승인</button>
            <button class="btn btn-warning btn-sm" @click="openRejectModal(row)">반려</button>
          </template>
          <template v-else-if="row.approvalStatus === 'REJECTED'">
            <button class="btn btn-success btn-sm" @click="approveProblem(row)">재승인</button>
          </template>
          <button class="btn btn-ghost btn-sm" @click="editProblem(row)">수정</button>
          <button class="btn btn-danger btn-sm" @click="deleteProblem(row.id)">삭제</button>
        </div>
      </template>
    </AppTable>

    <AppPagination :current-page="page" :total-pages="totalPages" :total-elements="totalElements" @page-change="page = $event" />

    <!-- 문제 추가/수정 모달 -->
    <AppModal v-model="showModal" :title="editingProblem ? '문제 수정' : '문제 추가'" size="lg">
      <AppSelect v-model="form.subject" label="과목" :options="subjectOptions" placeholder="과목 선택" />
      <AppSelect v-model="form.level" label="레벨" :options="levelOptions" placeholder="레벨 선택" />
      <AppInput v-model="form.unit" label="단원" placeholder="단원명" />
      <div class="form-group">
        <label>문제 내용</label>
        <textarea v-model="form.questionText" class="form-control" rows="4" placeholder="문제 내용을 입력하세요" />
      </div>
      <template #footer>
        <AppButton variant="secondary" @click="showModal = false">취소</AppButton>
        <AppButton @click="saveProblem">저장</AppButton>
      </template>
    </AppModal>

    <!-- [2026-03-23] 반려 사유 입력 모달 -->
    <AppModal v-model="showRejectModal" title="반려 사유 입력" size="sm">
      <div class="form-group">
        <label>반려 사유 <span style="color:var(--color-error)">*</span></label>
        <textarea v-model="rejectReason" class="form-control" rows="3" placeholder="반려 사유를 입력하세요" />
      </div>
      <template #footer>
        <AppButton variant="secondary" @click="showRejectModal = false">취소</AppButton>
        <AppButton variant="danger" :disabled="!rejectReason.trim()" @click="confirmReject">반려 확정</AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
// [2026-03-23] 검수/승인 워크플로우 추가
import { ref, reactive, computed, onMounted, watch } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppSelect from '@/components/common/AppSelect.vue'
import AppModal from '@/components/common/AppModal.vue'
import AppInput from '@/components/common/AppInput.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const { success, error } = useToast()
const dialog = useDialog()
const search = ref('')
const filterLevel = ref('')
const filterApproval = ref('')
const loading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)
const pendingCount = ref(0)
const showModal = ref(false)
const editingProblem = ref(null)
const form = reactive({ subject: '', level: '', unit: '', questionText: '' })

// 반려 모달
const showRejectModal = ref(false)
const rejectingProblem = ref(null)
const rejectReason = ref('')

const subjectOptions = ['수학','영어','국어','과학','사회'].map(v => ({ value: v, label: v }))
const levelOptions = ['A','B','C'].map(v => ({ value: v, label: `${v} 레벨` }))
const approvalOptions = [
  { value: 'PENDING', label: '검수 대기' },
  { value: 'APPROVED', label: '승인됨' },
  { value: 'REJECTED', label: '반려됨' },
]

const columns = [
  { key: 'id', label: 'ID', sortable: true },
  { key: 'level', label: '레벨' },
  { key: 'unit', label: '단원' },
  { key: 'questionText', label: '문제', sortable: false },
  { key: 'approvalStatus', label: '상태' },
  { key: 'createdAt', label: '등록일' },
  { key: 'actions', label: '' }
]

const problems = ref([])

function approvalLabel(status) {
  if (status === 'APPROVED') return '승인됨'
  if (status === 'REJECTED') return '반려됨'
  return '검수 대기'
}

async function fetchProblems() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 20 }
    if (search.value) params.keyword = search.value
    if (filterLevel.value) params.level = filterLevel.value
    if (filterApproval.value) params.approvalStatus = filterApproval.value
    const res = await api.get('/admin/problems', { params })
    problems.value = (res.data?.content || res.data || []).map(p => ({
      id: p.problemId,
      level: p.level,
      unit: p.unitPath || p.unitName || '',
      questionText: p.questionText,
      approvalStatus: p.approvalStatus || 'PENDING',
      rejectReason: p.rejectReason,
      createdAt: p.createdAt?.slice(0, 10)
    }))
    totalPages.value = res.data?.totalPages || 1
    totalElements.value = res.data?.totalElements || problems.value.length
  } catch {} finally { loading.value = false }
}

async function fetchPendingCount() {
  try {
    const res = await api.get('/admin/problems', { params: { approvalStatus: 'PENDING', size: 1 } })
    pendingCount.value = res.data?.totalElements || 0
  } catch {}
}

watch([search, filterLevel, filterApproval], () => { page.value = 1; fetchProblems() })
watch(page, fetchProblems)
onMounted(() => { fetchProblems(); fetchPendingCount() })

function openAddModal() {
  editingProblem.value = null
  Object.assign(form, { subject: '', level: '', unit: '', questionText: '' })
  showModal.value = true
}

function editProblem(p) {
  editingProblem.value = p
  Object.assign(form, { subject: p.subject, level: p.level, unit: p.unit, questionText: p.questionText })
  showModal.value = true
}

async function saveProblem() {
  try {
    if (editingProblem.value) {
      await api.put(`/admin/problems/${editingProblem.value.id}`, { subject: form.subject, level: form.level, unitName: form.unit, questionText: form.questionText })
      Object.assign(editingProblem.value, form)
      success('문제를 수정했습니다.')
    } else {
      const res = await api.post('/admin/problems', { subject: form.subject, level: form.level, unitName: form.unit, questionText: form.questionText })
      const p = res.data || {}
      problems.value.unshift({ id: p.problemId || Date.now(), ...form, unit: form.unit, approvalStatus: 'PENDING', createdAt: new Date().toISOString().slice(0, 10) })
      pendingCount.value++
      success('문제를 추가했습니다.')
    }
    showModal.value = false
  } catch { error('저장에 실패했습니다.') }
}

async function deleteProblem(id) {
  const ok = await dialog.confirm('문제를 삭제하시겠습니까?', { type: 'danger', confirmText: '삭제' })
  if (ok) {
    try {
      await api.delete(`/admin/problems/${id}`)
      problems.value = problems.value.filter(p => p.id !== id)
      success('문제를 삭제했습니다.')
    } catch { error('삭제에 실패했습니다.') }
  }
}

async function approveProblem(row) {
  try {
    await api.patch(`/admin/problems/${row.id}/approve`)
    row.approvalStatus = 'APPROVED'
    row.rejectReason = null
    if (pendingCount.value > 0) pendingCount.value--
    success('문제를 승인했습니다.')
  } catch { error('승인에 실패했습니다.') }
}

function openRejectModal(row) {
  rejectingProblem.value = row
  rejectReason.value = ''
  showRejectModal.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) return
  try {
    await api.patch(`/admin/problems/${rejectingProblem.value.id}/reject`, { rejectReason: rejectReason.value })
    rejectingProblem.value.approvalStatus = 'REJECTED'
    rejectingProblem.value.rejectReason = rejectReason.value
    if (pendingCount.value > 0) pendingCount.value--
    showRejectModal.value = false
    success('문제를 반려했습니다.')
  } catch { error('반려 처리에 실패했습니다.') }
}
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;

  @media (max-width: $bp-mobile) {
    flex-direction: column;
    gap: $spacing-3;

    h1 { width: 100%; }
  }

  &__actions {
    display: flex;
    gap: 12px;

    @media (max-width: $bp-mobile) {
      width: 100%;

      > a, > button {
        flex: 1;
        text-align: center;
        justify-content: center;
      }
    }
  }
}

.filter-bar {
  display: flex;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
  flex-wrap: wrap;
}

.filter-search {
  width: 240px;

  @media (max-width: $bp-mobile) {
    width: 100%;
  }
}

.filter-selects {
  display: flex;
  gap: $spacing-3;
  flex: 1;

  @media (max-width: $bp-mobile) {
    width: 100%;

    > * {
      flex: 1;
      min-width: 0;
    }
  }
}

// [2026-03-23] 검수 대기 배너
.pending-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff8e1;
  border: 1px solid #ffc107;
  border-radius: 8px;
  margin-bottom: $spacing-4;
  font-size: 14px;
  color: #7a5800;

  svg { flex-shrink: 0; stroke: #ffc107; }

  .btn-link {
    background: none;
    border: none;
    color: #7a5800;
    text-decoration: underline;
    cursor: pointer;
    font-size: 14px;
    padding: 0;
    margin-left: 4px;
  }
}

// [2026-03-23] 승인 상태 배지
.approval-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;

  &--pending  { background: #fff3cd; color: #856404; }
  &--approved { background: #d1fae5; color: #065f46; }
  &--rejected { background: #fee2e2; color: #991b1b; }
}
</style>
