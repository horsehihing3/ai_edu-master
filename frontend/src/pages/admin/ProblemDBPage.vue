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
        <AppSelect v-model="filterSubject" :options="subjectOptions" placeholder="전체 과목" style="margin:0;" />
        <AppSelect v-model="filterLevel" :options="levelOptions" placeholder="전체 레벨" style="margin:0;" />
      </div>
    </div>

    <AppTable :columns="columns" :data="filteredProblems" :loading="loading">
      <template #cell-level="{ value }">
        <AppBadge :type="value" />
      </template>
      <template #cell-questionText="{ value }">
        <span style="max-width:300px; display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">{{ value }}</span>
      </template>
      <template #cell-actions="{ row }">
        <div style="display:flex;gap:6px;">
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
const filterSubject = ref('')
const filterLevel = ref('')
const loading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)
const showModal = ref(false)
const editingProblem = ref(null)
const form = reactive({ subject: '', level: '', unit: '', questionText: '' })

const subjectOptions = ['수학','영어','국어','과학','사회'].map(v => ({ value: v, label: v }))
const levelOptions = ['A','B','C'].map(v => ({ value: v, label: `${v} 레벨` }))

const columns = [
  { key: 'id', label: 'ID', sortable: true },
  { key: 'subject', label: '과목' },
  { key: 'level', label: '레벨' },
  { key: 'unit', label: '단원' },
  { key: 'questionText', label: '문제', sortable: false },
  { key: 'createdAt', label: '등록일' },
  { key: 'actions', label: '' }
]

const problems = ref([])
const filteredProblems = computed(() => problems.value)

async function fetchProblems() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 20 }
    if (search.value) params.keyword = search.value
    if (filterSubject.value) params.subject = filterSubject.value
    if (filterLevel.value) params.level = filterLevel.value
    const res = await api.get('/admin/problems', { params })
    problems.value = (res.data?.content || res.data || []).map(p => ({
      id: p.problemId, subject: p.subject, level: p.level,
      unit: p.unitName, questionText: p.questionText, createdAt: p.createdAt?.slice(0,10)
    }))
    totalPages.value = res.data?.totalPages || 1
    totalElements.value = res.data?.totalElements || problems.value.length
  } catch {} finally { loading.value = false }
}

watch([search, filterSubject, filterLevel], () => { page.value = 1; fetchProblems() })
watch(page, fetchProblems)
onMounted(fetchProblems)

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
      problems.value.unshift({ id: p.problemId || Date.now(), ...form, unit: form.unit, createdAt: new Date().toISOString().slice(0,10) })
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
</style>
