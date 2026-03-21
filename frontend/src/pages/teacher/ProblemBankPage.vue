<template>
  <div class="problem-bank-page">
    <!-- 헤더 -->
    <div class="page-header">
      <h1>문제 은행</h1>
    </div>

    <!-- 필터 -->
    <div class="filter-card">
      <div class="filter-row">
        <!-- 난이도 멀티선택 -->
        <div class="filter-group">
          <label class="filter-label">난이도</label>
          <div class="toggle-group">
            <button
              v-for="lv in LEVELS"
              :key="lv.value"
              :class="['toggle-btn', { active: filters.levels.includes(lv.value) }]"
              @click="toggleLevel(lv.value)"
            >{{ lv.label }}</button>
          </div>
        </div>

        <!-- 학년 -->
        <div class="filter-group">
          <label class="filter-label">학년</label>
          <select v-model="filters.grade" class="filter-select">
            <option value="">전체</option>
            <option v-for="g in GRADES" :key="g.value" :value="g.value">{{ g.label }}</option>
          </select>
        </div>

        <!-- 단원명 -->
        <div class="filter-group">
          <label class="filter-label">단원명</label>
          <input v-model="filters.unitName" class="filter-input" placeholder="단원명 입력" @keyup.enter="search(0)" />
        </div>

        <!-- 키워드 -->
        <div class="filter-group">
          <label class="filter-label">키워드</label>
          <input v-model="filters.keyword" class="filter-input" placeholder="문제 내용 검색" @keyup.enter="search(0)" />
        </div>

        <button class="btn btn-primary btn-md filter-search-btn" @click="search(0)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          검색
        </button>
      </div>
    </div>

    <!-- 결과 카운트 -->
    <div class="result-meta">
      <span v-if="!loading">총 <strong>{{ totalCount }}</strong>개</span>
    </div>

    <!-- 테이블 -->
    <div class="table-wrap">
      <div v-if="loading" class="table-loading">
        <div class="spinner" />
        <p>검색 중...</p>
      </div>
      <div v-else-if="!problems.length" class="table-empty">
        <p>검색 결과가 없습니다.</p>
      </div>
      <table v-else class="app-table">
        <thead>
          <tr>
            <th class="col-check">
              <input type="checkbox" :checked="isAllChecked" :indeterminate.prop="isIndeterminate" @change="toggleAll" />
            </th>
            <th class="col-no">번호</th>
            <th>단원명</th>
            <th class="col-level">난이도</th>
            <th class="col-grade">학년</th>
            <th>문제 미리보기</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(p, idx) in problems"
            :key="p.problemId"
            class="data-row"
            @click.stop="openDetail(p)"
          >
            <td class="col-check" @click.stop>
              <input type="checkbox" :value="p.problemId" v-model="selected" />
            </td>
            <td class="col-no text-muted">{{ (currentPage * PAGE_SIZE) + idx + 1 }}</td>
            <td class="col-unit">{{ p.unitName || '-' }}</td>
            <td class="col-level"><span :class="['level-badge', 'level-' + p.level]">{{ p.level || '-' }}</span></td>
            <td class="col-grade">{{ gradeLabel(p.grade) }}</td>
            <td class="col-preview">{{ preview(p.questionText) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="currentPage === 0" @click="goPage(currentPage - 1)">‹</button>
      <button
        v-for="n in pageNumbers"
        :key="n"
        :class="['page-btn', { active: n === currentPage }]"
        @click="goPage(n)"
      >{{ n + 1 }}</button>
      <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="goPage(currentPage + 1)">›</button>
    </div>

    <!-- 문제 상세 모달 -->
    <Teleport to="body">
      <div v-if="detailProblem" class="modal-backdrop" @click.self="closeDetail">
        <div class="modal-box">
          <div class="modal-header">
            <h2>문제 상세</h2>
            <button class="modal-close" @click="closeDetail">✕</button>
          </div>
          <div class="modal-body">
            <div class="detail-meta">
              <span :class="['level-badge', 'level-' + detailProblem.level]">{{ detailProblem.level }}</span>
              <span class="meta-tag">{{ gradeLabel(detailProblem.grade) }}</span>
              <span class="meta-tag">{{ detailProblem.unitName }}</span>
            </div>

            <div class="detail-section">
              <p class="section-label">문제</p>
              <MathText :text="detailProblem.questionText" class="question-text" />
            </div>

            <div v-if="detailProblem.options && detailProblem.options.length" class="detail-section">
              <p class="section-label">선택지</p>
              <ol class="options-list">
                <li
                  v-for="opt in detailProblem.options"
                  :key="opt.optionNo"
                  :class="{ 'correct-option': String(opt.optionNo) === String(detailProblem.answer) }"
                >
                  <MathText :text="opt.optionText" />
                </li>
              </ol>
            </div>

            <div class="detail-section">
              <p class="section-label">정답</p>
              <strong class="answer-text">{{ detailProblem.answer }}</strong>
            </div>

            <div v-if="detailProblem.explanation" class="detail-section">
              <p class="section-label">해설</p>
              <MathText :text="detailProblem.explanation" class="explanation-text" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-md" @click="closeDetail">닫기</button>
            <button
              class="btn btn-primary btn-md"
              @click="selectAndClose(detailProblem.problemId)"
            >
              {{ selected.includes(detailProblem.problemId) ? '선택 해제' : '선택 추가' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 하단 고정 선택 바 -->
    <Transition name="slide-up">
      <div v-if="selected.length" class="selection-bar">
        <span class="selection-count"><strong>{{ selected.length }}</strong>개 선택됨</span>
        <div class="selection-actions">
          <button class="btn btn-ghost btn-md" @click="selected = []">선택 취소</button>
          <button class="btn btn-primary btn-md" @click="goCreateAssignment">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            과제 만들기
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
// [2026-03-21] 교사용 문제 은행 페이지 - 문제 탐색, 체크 선택 후 과제 생성
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'
import MathText from '@/components/common/MathText.vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { error } = useToast()

const PAGE_SIZE = 20

const LEVELS = [
  { value: 'A', label: 'A (기초)' },
  { value: 'B', label: 'B (중급)' },
  { value: 'C', label: 'C (심화)' },
]
const GRADES = [
  { value: 'GRADE_1', label: '1학년' },
  { value: 'GRADE_2', label: '2학년' },
  { value: 'GRADE_3', label: '3학년' },
]

const filters = ref({ levels: [], grade: '', unitName: '', keyword: '' })
const problems = ref([])
const totalCount = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)

const selected = ref([])
const detailProblem = ref(null)

// ── 필터 헬퍼 ──────────────────────────────────
function toggleLevel(lv) {
  const idx = filters.value.levels.indexOf(lv)
  idx === -1 ? filters.value.levels.push(lv) : filters.value.levels.splice(idx, 1)
}

function gradeLabel(g) {
  return { GRADE_1: '1학년', GRADE_2: '2학년', GRADE_3: '3학년' }[g] || g || '-'
}

function preview(text) {
  if (!text) return '-'
  const plain = text.replace(/\$\$?[\s\S]*?\$\$?|\\\([\s\S]*?\\\)|\\\[[\s\S]*?\\\]/g, '[수식]')
  return plain.length > 30 ? plain.slice(0, 30) + '…' : plain
}

// ── API ────────────────────────────────────────
async function search(page = 0) {
  loading.value = true
  currentPage.value = page
  try {
    const params = new URLSearchParams()
    filters.value.levels.forEach(lv => params.append('level', lv))
    if (filters.value.grade) params.set('grade', filters.value.grade)
    if (filters.value.unitName.trim()) params.set('unitName', filters.value.unitName.trim())
    if (filters.value.keyword.trim()) params.set('keyword', filters.value.keyword.trim())
    params.set('page', page)
    params.set('size', PAGE_SIZE)

    const res = await api.get(`/problems?${params.toString()}`)
    const data = res.data?.data || res.data || {}
    problems.value = data.content || []
    totalCount.value = data.totalElements ?? problems.value.length
    totalPages.value = data.totalPages ?? 1
  } catch (e) {
    error('문제 목록을 불러오지 못했습니다.')
    problems.value = []
  } finally {
    loading.value = false
  }
}

async function fetchDetail(problemId) {
  try {
    const res = await api.get(`/problems/${problemId}`)
    return res.data?.data || res.data
  } catch {
    return null
  }
}

// ── 모달 ────────────────────────────────────────
async function openDetail(p) {
  const detail = await fetchDetail(p.problemId)
  detailProblem.value = detail || p
}

function closeDetail() {
  detailProblem.value = null
}

function selectAndClose(id) {
  const idx = selected.value.indexOf(id)
  idx === -1 ? selected.value.push(id) : selected.value.splice(idx, 1)
  closeDetail()
}

// ── 체크박스 ────────────────────────────────────
const isAllChecked = computed(() =>
  problems.value.length > 0 && problems.value.every(p => selected.value.includes(p.problemId))
)
const isIndeterminate = computed(() =>
  problems.value.some(p => selected.value.includes(p.problemId)) && !isAllChecked.value
)

function toggleAll(e) {
  if (e.target.checked) {
    problems.value.forEach(p => {
      if (!selected.value.includes(p.problemId)) selected.value.push(p.problemId)
    })
  } else {
    const pageIds = problems.value.map(p => p.problemId)
    selected.value = selected.value.filter(id => !pageIds.includes(id))
  }
}

// ── 페이지네이션 ────────────────────────────────
const pageNumbers = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  const start = Math.max(0, cur - 2)
  const end = Math.min(total - 1, cur + 2)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

function goPage(n) {
  search(n)
}

// ── 과제 만들기 ────────────────────────────────
function goCreateAssignment() {
  router.push({ path: '/teacher/assignments/create', query: { problemIds: selected.value.join(',') } })
}

onMounted(() => search(0))
</script>

<style scoped lang="scss">
.problem-bank-page {
  padding-bottom: 80px; // 하단 바 공간 확보
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-5;

  h1 { font-size: $font-size-xl; font-weight: 700; }
}

// ── 필터 카드 ──────────────────────────────────
.filter-card {
  background: $bg-white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  padding: $spacing-5;
  margin-bottom: $spacing-4;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-4;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
  min-width: 0;
}

.filter-label {
  font-size: $font-size-xs;
  font-weight: 600;
  color: $text-secondary;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.filter-select,
.filter-input {
  height: 36px;
  padding: 0 $spacing-3;
  border: 1px solid $border;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  background: $bg-light;
  color: $text-primary;
  outline: none;
  min-width: 140px;

  &:focus { border-color: $primary; }
}

.toggle-group {
  display: flex;
  gap: $spacing-1;
}

.toggle-btn {
  padding: 6px 12px;
  border-radius: $radius-md;
  font-size: $font-size-xs;
  font-weight: 600;
  border: 1px solid $border;
  background: $bg-light;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  white-space: nowrap;

  &.active {
    background: $primary;
    color: #fff;
    border-color: $primary;
  }

  &:hover:not(.active) {
    border-color: $primary;
    color: $primary;
  }
}

.filter-search-btn {
  align-self: flex-end;
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

// ── 결과 메타 ──────────────────────────────────
.result-meta {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-3;
  min-height: 20px;
}

// ── 테이블 ────────────────────────────────────
.table-wrap {
  background: $bg-white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  overflow: hidden;
}

.table-loading,
.table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-10;
  color: $text-muted;
  gap: $spacing-3;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid $border;
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.app-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;

  thead tr {
    background: $bg-light;
    border-bottom: 2px solid $border;
  }

  th {
    padding: $spacing-3 $spacing-4;
    text-align: left;
    font-size: $font-size-xs;
    font-weight: 600;
    color: $text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    white-space: nowrap;
  }

  td {
    padding: $spacing-3 $spacing-4;
    border-bottom: 1px solid $border;
    color: $text-primary;
  }

  .data-row {
    cursor: pointer;
    transition: background $transition-fast;

    &:hover { background: $bg-light; }
    &:last-child td { border-bottom: none; }
  }
}

.col-check { width: 40px; }
.col-no    { width: 56px; }
.col-level { width: 90px; }
.col-grade { width: 80px; }
.col-preview { color: $text-secondary; }

.text-muted { color: $text-muted; font-size: $font-size-xs; }

// ── 난이도 배지 ────────────────────────────────
.level-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 700;

  &.level-A { background: #D1FAE5; color: #065F46; }
  &.level-B { background: #DBEAFE; color: #1E40AF; }
  &.level-C { background: #FEE2E2; color: #991B1B; }
}

// ── 페이지네이션 ────────────────────────────────
.pagination {
  display: flex;
  justify-content: center;
  gap: $spacing-1;
  margin-top: $spacing-5;
}

.page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 $spacing-2;
  border: 1px solid $border;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  background: $bg-white;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover:not(:disabled):not(.active) { border-color: $primary; color: $primary; }
  &.active { background: $primary; color: #fff; border-color: $primary; font-weight: 700; }
  &:disabled { opacity: 0.35; cursor: default; }
}

// ── 모달 ──────────────────────────────────────
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: $spacing-4;
}

.modal-box {
  background: $bg-white;
  border-radius: $radius-xl;
  width: 100%;
  max-width: 640px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-5 $spacing-6;
  border-bottom: 1px solid $border;

  h2 { font-size: $font-size-lg; font-weight: 700; }
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: $radius-md;
  font-size: 16px;
  color: $text-muted;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background $transition-fast;

  &:hover { background: $bg-light; color: $text-primary; }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-5 $spacing-6;
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-3;
  padding: $spacing-4 $spacing-6;
  border-top: 1px solid $border;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-2;
}

.meta-tag {
  padding: 2px 10px;
  background: $bg-light;
  border: 1px solid $border;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  color: $text-secondary;
  font-weight: 500;
}

.detail-section { display: flex; flex-direction: column; gap: $spacing-2; }

.section-label {
  font-size: $font-size-xs;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: $text-muted;
}

.question-text { font-size: $font-size-base; line-height: 1.7; }

.options-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  padding: 0;

  li {
    padding: $spacing-3 $spacing-4;
    border: 1px solid $border;
    border-radius: $radius-md;
    font-size: $font-size-sm;
    display: flex;
    align-items: center;
    gap: $spacing-2;
    counter-increment: option;

    &::before {
      content: counter(option) ".";
      font-weight: 700;
      color: $text-muted;
      min-width: 18px;
    }

    &.correct-option {
      border-color: #10B981;
      background: #D1FAE5;
      color: #065F46;

      &::before { color: #065F46; }
    }
  }

  counter-reset: option;
}

.answer-text {
  font-size: $font-size-base;
  color: $primary;
}

.explanation-text {
  font-size: $font-size-sm;
  line-height: 1.7;
  color: $text-secondary;
}

// ── 하단 고정 선택 바 ──────────────────────────
.selection-bar {
  position: fixed;
  bottom: $spacing-5;
  left: 50%;
  transform: translateX(-50%);
  background: $text-primary;
  color: #fff;
  border-radius: $radius-xl;
  padding: $spacing-3 $spacing-5;
  display: flex;
  align-items: center;
  gap: $spacing-6;
  box-shadow: 0 8px 32px rgba(0,0,0,0.25);
  z-index: 500;
  white-space: nowrap;
}

.selection-count {
  font-size: $font-size-sm;

  strong { font-size: $font-size-base; }
}

.selection-actions {
  display: flex;
  gap: $spacing-2;

  .btn-ghost {
    color: rgba(255,255,255,0.75);
    &:hover { color: #fff; background: rgba(255,255,255,0.1); }
  }
}

// ── 트랜지션 ────────────────────────────────────
.slide-up-enter-active,
.slide-up-leave-active { transition: all 0.25s ease; }
.slide-up-enter-from,
.slide-up-leave-to { opacity: 0; transform: translateX(-50%) translateY(20px); }
</style>
