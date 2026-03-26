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
        <AppSelect v-model="filterApproval" :options="approvalOptions" placeholder="전체 상태" style="margin:0;" />
      </div>
      <div v-if="selectedIds.size > 0" class="bulk-actions">
        <span class="bulk-count">{{ selectedIds.size }}건 선택됨</span>
        <button class="btn btn-danger btn-md" @click="deleteBulk">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
          선택 삭제
        </button>
        <button class="btn btn-ghost btn-md" @click="clearSelection">취소</button>
      </div>
    </div>

    <div v-if="pendingCount > 0" class="pending-banner">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      검수 대기 문제가 <strong>{{ pendingCount }}건</strong> 있습니다.
      <button class="btn-link" @click="filterApproval = 'PENDING_REVIEW'; page = 1; fetchProblems()">바로가기</button>
    </div>

    <!-- 전체 선택 툴바 -->
    <div class="select-toolbar">
      <label class="select-all-label">
        <input
          type="checkbox"
          :checked="isAllSelected"
          :indeterminate.prop="isIndeterminate"
          @change="toggleSelectAll"
        />
        <span>전체 선택 ({{ problems.length }}건)</span>
      </label>
    </div>

    <AppTable :columns="columns" :data="problems" :loading="loading">
      <template #cell-checkbox="{ row }">
        <input
          type="checkbox"
          :checked="selectedIds.has(row.id)"
          @change="toggleSelect(row.id)"
          @click.stop
        />
      </template>
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
          <template v-if="row.approvalStatus === 'PENDING_REVIEW'">
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
      <div class="modal-form">
        <div class="form-row">
          <AppSelect v-model="form.subject" label="과목" :options="subjectOptions" placeholder="과목 선택" />
          <AppSelect v-model="form.level" label="레벨" :options="levelOptions" placeholder="레벨 선택" />
          <AppSelect v-model="form.problemType" label="유형" :options="typeOptions" placeholder="유형 선택" />
        </div>
        <div class="form-row">
          <AppInput v-model="form.grade" label="학년" placeholder="예: 중3" />
          <AppInput v-model="form.unit" label="단원" placeholder="단원명" />
          <AppInput v-model="form.answer" label="정답" placeholder="정답 입력" />
        </div>
        <div class="form-group">
          <label>문제 내용</label>
          <textarea v-model="form.questionText" class="form-control" rows="4" placeholder="문제 내용을 입력하세요" />
        </div>

        <!-- 객관식 선지 -->
        <div v-if="form.problemType === 'MULTIPLE_CHOICE'" class="form-group">
          <div class="options-header">
            <label>선지 (보기)</label>
            <button class="btn btn-ghost btn-sm" type="button" @click="addOption">+ 선지 추가</button>
          </div>
          <div v-for="(opt, i) in form.options" :key="i" class="option-row">
            <span class="option-no">{{ '①②③④⑤⑥⑦⑧⑨⑩'[i] || (i+1) }}</span>
            <input v-model="form.options[i].optionText" class="form-control" :placeholder="`${i+1}번 선지`" />
            <button class="btn btn-ghost btn-sm option-del" type="button" @click="removeOption(i)" v-if="form.options.length > 2">✕</button>
          </div>
        </div>

        <div class="form-group">
          <label>해설</label>
          <textarea v-model="form.explanation" class="form-control" rows="3" placeholder="풀이 해설 (선택)" />
        </div>
        <div class="form-group">
          <label>문제 이미지 URL</label>
          <input v-model="form.questionImgUrl" class="form-control" placeholder="https://..." />
          <div v-if="form.questionImgUrl" class="img-preview">
            <img :src="form.questionImgUrl" alt="문제 이미지 미리보기" style="max-width:100%; max-height:200px; border:1px solid #ddd; margin-top:8px;" />
          </div>
        </div>
      </div>
      <template #footer>
        <AppButton variant="secondary" @click="showModal = false">취소</AppButton>
        <AppButton :loading="saving" @click="saveProblem">저장</AppButton>
      </template>
    </AppModal>

    <!-- 반려 사유 입력 모달 -->
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
const saving = ref(false)
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)
const pendingCount = ref(0)
const showModal = ref(false)
const editingProblem = ref(null)

const defaultForm = () => ({
  subject: '',
  level: '',
  grade: '',
  unit: '',
  questionText: '',
  problemType: 'MULTIPLE_CHOICE',
  answer: '',
  explanation: '',
  questionImgUrl: '',
  options: [
    { optionText: '', optionImgUrl: '' },
    { optionText: '', optionImgUrl: '' },
    { optionText: '', optionImgUrl: '' },
    { optionText: '', optionImgUrl: '' },
    { optionText: '', optionImgUrl: '' },
  ]
})

const form = reactive(defaultForm())

// 반려 모달
const showRejectModal = ref(false)
const rejectingProblem = ref(null)
const rejectReason = ref('')

const subjectOptions = ['수학','영어','국어','과학','사회'].map(v => ({ value: v, label: v }))
const levelOptions = ['A','B','C'].map(v => ({ value: v, label: `${v} 레벨` }))
const typeOptions = [
  { value: 'MULTIPLE_CHOICE', label: '객관식' },
  { value: 'SHORT_ANSWER', label: '주관식' },
]
const approvalOptions = [
  { value: 'PENDING_REVIEW', label: '검수 대기' },
  { value: 'APPROVED', label: '승인됨' },
  { value: 'REJECTED', label: '반려됨' },
]

const columns = [
  { key: 'checkbox', label: '' },
  { key: 'id', label: 'ID', sortable: true },
  { key: 'level', label: '레벨' },
  { key: 'unit', label: '단원' },
  { key: 'questionText', label: '문제', sortable: false },
  { key: 'approvalStatus', label: '상태' },
  { key: 'createdAt', label: '등록일' },
  { key: 'actions', label: '' }
]

const problems = ref([])

// 체크박스 선택 상태
const selectedIds = ref(new Set())

const isAllSelected = computed(
  () => problems.value.length > 0 && problems.value.every(p => selectedIds.value.has(p.id))
)
const isIndeterminate = computed(
  () => selectedIds.value.size > 0 && !isAllSelected.value
)

function toggleSelect(id) {
  const next = new Set(selectedIds.value)
  next.has(id) ? next.delete(id) : next.add(id)
  selectedIds.value = next
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(problems.value.map(p => p.id))
  }
}

function clearSelection() {
  selectedIds.value = new Set()
}

async function deleteBulk() {
  if (selectedIds.value.size === 0) return
  const ids = [...selectedIds.value]
  const ok = await dialog.confirm(
    `선택한 ${ids.length}건의 문제를 삭제하시겠습니까?\n제출 이력이 있는 문제는 숨김 처리됩니다.`,
    { type: 'danger', confirmText: '일괄 삭제' }
  )
  if (!ok) return

  loading.value = true
  try {
    // 병렬 삭제 후 성공/실패 집계
    const results = await Promise.allSettled(
      ids.map(id => api.delete(`/admin/problems/${id}`))
    )
    const successCount = results.filter(r => r.status === 'fulfilled').length
    const failCount    = results.filter(r => r.status === 'rejected').length

    // 성공한 ID만 즉시 로컬 제거 (UX: 깜빡임 없이 바로 반영)
    const failedIds = new Set(
      results
        .map((r, i) => r.status === 'rejected' ? ids[i] : null)
        .filter(Boolean)
    )
    problems.value = problems.value.filter(p => !selectedIds.value.has(p.id) || failedIds.has(p.id))
    totalElements.value = Math.max(0, totalElements.value - successCount)

    clearSelection()

    if (failCount === 0) {
      success(`${successCount}건을 삭제했습니다.`)
    } else if (successCount === 0) {
      error(`삭제에 실패했습니다. (${failCount}건 오류)`)
    } else {
      success(`${successCount}건 삭제 완료`)
      error(`${failCount}건은 삭제할 수 없습니다. (제출 이력 또는 권한 문제)`)
    }

    // 현재 페이지 문제가 다 사라졌으면 이전 페이지로
    if (problems.value.length === 0 && page.value > 1) {
      page.value = page.value - 1
    } else {
      await fetchProblems()
    }
    await fetchPendingCount()
  } catch (e) {
    error('일괄 삭제 중 오류가 발생했습니다.')
    await fetchProblems()
  } finally {
    loading.value = false
  }
}

function approvalLabel(status) {
  if (status === 'APPROVED') return '승인됨'
  if (status === 'REJECTED') return '반려됨'
  if (status === 'DRAFT') return '초안'
  return '검수 대기'
}

function addOption() {
  form.options.push({ optionText: '', optionImgUrl: '' })
}

function removeOption(i) {
  form.options.splice(i, 1)
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
      approvalStatus: p.approvalStatus || 'PENDING_REVIEW',
      rejectReason: p.rejectReason,
      createdAt: p.createdAt?.slice(0, 10)
    }))
    totalPages.value = res.data?.totalPages || 1
    totalElements.value = res.data?.totalElements || problems.value.length
    clearSelection()
  } catch {} finally { loading.value = false }
}

async function fetchPendingCount() {
  try {
    const res = await api.get('/admin/problems', { params: { approvalStatus: 'PENDING_REVIEW', size: 1 } })
    pendingCount.value = res.data?.totalElements || 0
  } catch {}
}

watch([search, filterLevel, filterApproval], () => { page.value = 1; fetchProblems() })
watch(page, fetchProblems)
onMounted(() => { fetchProblems(); fetchPendingCount() })

function openAddModal() {
  editingProblem.value = null
  Object.assign(form, defaultForm())
  showModal.value = true
}

async function editProblem(p) {
  editingProblem.value = p
  // 상세 조회로 선지 포함 데이터 가져오기
  try {
    const res = await api.get(`/problems/${p.id}`)
    const detail = res.data?.data || res.data || {}
    Object.assign(form, {
      subject: detail.subject || p.subject || '',
      level: detail.level || p.level || '',
      grade: detail.grade || '',
      unit: detail.unitName || p.unit || '',
      questionText: detail.questionText || p.questionText || '',
      problemType: detail.problemType || 'MULTIPLE_CHOICE',
      answer: detail.answer || '',
      explanation: detail.explanation || '',
      questionImgUrl: detail.questionImgUrl || '',
      options: detail.options?.length
        ? detail.options.map(o => ({ optionText: o.optionText || '', optionImgUrl: o.optionImgUrl || '' }))
        : defaultForm().options
    })
  } catch {
    Object.assign(form, {
      ...defaultForm(),
      subject: p.subject || '',
      level: p.level || '',
      unit: p.unit || '',
      questionText: p.questionText || '',
    })
  }
  showModal.value = true
}

async function saveProblem() {
  saving.value = true
  try {
    const payload = {
      subject: form.subject,
      level: form.level,
      grade: form.grade,
      unitName: form.unit,
      questionText: form.questionText,
      problemType: form.problemType,
      answer: form.answer,
      explanation: form.explanation,
      questionImgUrl: form.questionImgUrl || '',
      options: form.problemType === 'MULTIPLE_CHOICE'
        ? form.options.filter(o => o.optionText.trim())
        : []
    }

    if (editingProblem.value) {
      await api.put(`/admin/problems/${editingProblem.value.id}`, payload)
      Object.assign(editingProblem.value, { level: form.level, unit: form.unit, questionText: form.questionText })
      success('문제를 수정했습니다.')
    } else {
      const res = await api.post('/admin/problems', payload)
      const p = res.data?.data || res.data || {}
      problems.value.unshift({
        id: p.problemId || Date.now(),
        level: form.level,
        unit: form.unit,
        questionText: form.questionText,
        approvalStatus: 'PENDING_REVIEW',
        createdAt: new Date().toISOString().slice(0, 10)
      })
      pendingCount.value++
      success('문제를 추가했습니다.')
    }
    showModal.value = false
  } catch { error('저장에 실패했습니다.') }
  finally { saving.value = false }
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

.approval-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;

  &--pending_review { background: #fff3cd; color: #856404; }
  &--approved       { background: #d1fae5; color: #065f46; }
  &--rejected       { background: #fee2e2; color: #991b1b; }
  &--draft          { background: #e5e7eb; color: #374151; }
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-3;

  @media (max-width: $bp-mobile) {
    grid-template-columns: 1fr;
  }
}

.options-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-2;

  label { margin: 0; font-weight: 600; font-size: $font-size-sm; }
}

.option-row {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  margin-bottom: $spacing-2;
}

.option-no {
  font-size: $font-size-base;
  width: 24px;
  flex-shrink: 0;
  text-align: center;
}

.option-del {
  flex-shrink: 0;
  color: $danger;
  padding: 4px 8px;
}

.img-preview {
  margin-top: $spacing-2;
  img {
    max-width: 100%;
    max-height: 300px;
    border: 1px solid var(--color-border);
    border-radius: 8px;
  }
}

.select-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 4px;
  margin-bottom: 4px;
}

.select-all-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  user-select: none;

  input[type='checkbox'] {
    width: 16px;
    height: 16px;
    cursor: pointer;
    accent-color: var(--color-primary);
  }
}

.bulk-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;

  @media (max-width: $bp-mobile) {
    width: 100%;
    margin-left: 0;
  }
}

.bulk-count {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-light, #e8f4ff);
  padding: 4px 10px;
  border-radius: 20px;
}
</style>
