<template>
  <div class="class-manage-page">
    <div class="page-header">
      <h1>학급 관리</h1>
      <button class="btn btn-primary btn-md" @click="openCreateModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        학급 생성
      </button>
    </div>

    <!-- 학급 카드 목록 -->
    <div v-if="loading" class="empty-state">
      <div class="spinner" />
    </div>
    <div v-else-if="!classes.length" class="empty-state">
      <p class="empty-text">등록된 학급이 없습니다.</p>
      <button class="btn btn-primary btn-md" @click="openCreateModal">첫 학급 만들기</button>
    </div>
    <div v-else class="class-grid">
      <div v-for="cls in classes" :key="cls.classId" class="class-card">
        <div class="class-card__header">
          <div>
            <h3 class="class-name">{{ cls.className }}</h3>
            <div class="class-meta">
              <span v-if="cls.grade" class="meta-badge">{{ gradeLabel(cls.grade) }}</span>
              <span class="meta-badge level">{{ levelLabel(cls.levelFilter) }}</span>
            </div>
          </div>
          <div class="card-actions">
            <button class="btn btn-ghost btn-sm" @click="openEditModal(cls)">수정</button>
            <button class="btn btn-secondary btn-sm" @click="deleteClass(cls)">삭제</button>
          </div>
        </div>
        <!-- [2026-04-03] 초대코드 표시 -->
        <div class="class-card__invite">
          <span class="invite-label">초대코드</span>
          <span class="invite-code">{{ cls.inviteCode || '—' }}</span>
          <button class="invite-copy-btn" :title="'코드 복사'" @click.stop="copyCode(cls.inviteCode)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
          </button>
        </div>
        <div class="class-card__body" @click="openMembersModal(cls)">
          <div class="student-count">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            <span><strong>{{ cls.studentCount }}</strong>명</span>
          </div>
          <span class="view-members">학생 목록 보기 →</span>
        </div>
      </div>
    </div>

    <!-- 학급 생성/수정 모달 -->
    <Teleport to="body">
      <div v-if="formModal.open" class="modal-backdrop" @click.self="closeFormModal">
        <div class="modal-box">
          <div class="modal-header">
            <h2>{{ formModal.isEdit ? '학급 수정' : '학급 생성' }}</h2>
            <button class="modal-close" @click="closeFormModal">✕</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">학급 이름 <span class="required">*</span></label>
              <input v-model="formModal.className" class="form-input" placeholder="예: 1반 수학 심화" />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">학년</label>
                <select v-model="formModal.grade" class="form-select">
                  <option value="">선택 안 함</option>
                  <option value="GRADE_1">1학년</option>
                  <option value="GRADE_2">2학년</option>
                  <option value="GRADE_3">3학년</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">기본 난이도</label>
                <select v-model="formModal.levelFilter" class="form-select">
                  <option value="ALL">전체</option>
                  <option value="A">A (심화)</option>
                  <option value="B">B (표준)</option>
                  <option value="C">C (기초)</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-md" @click="closeFormModal">취소</button>
            <button class="btn btn-primary btn-md" :disabled="!formModal.className.trim()" @click="submitForm">
              {{ formModal.isEdit ? '저장' : '생성' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 학생 목록 모달 -->
    <Teleport to="body">
      <div v-if="membersModal.open" class="modal-backdrop" @click.self="closeMembersModal">
        <div class="modal-box modal-box--wide">
          <div class="modal-header">
            <h2>{{ membersModal.className }} · 학생 목록</h2>
            <button class="modal-close" @click="closeMembersModal">✕</button>
          </div>
          <div class="modal-body">
            <!-- 학생 추가 -->
            <div class="add-member-row">
              <input
                v-model="membersModal.searchInput"
                class="form-input"
                placeholder="학생 이름 검색 후 선택"
                @input="searchStudents"
              />
              <button class="btn btn-primary btn-sm" :disabled="!membersModal.selectedToAdd.length" @click="addMembers">
                추가 ({{ membersModal.selectedToAdd.length }})
              </button>
            </div>
            <div v-if="membersModal.searchResults.length" class="search-results">
              <div
                v-for="s in membersModal.searchResults"
                :key="s.studentId"
                :class="['search-item', { selected: membersModal.selectedToAdd.includes(s.studentId) }]"
                @click="toggleAddStudent(s.studentId)"
              >
                <span>{{ s.name }}</span>
                <span class="text-muted">{{ s.email }}</span>
                <span :class="['level-badge', 'level-' + s.studentLevel]">{{ s.studentLevel }}</span>
              </div>
            </div>

            <div class="members-divider" />

            <!-- 현재 학생 목록 -->
            <div v-if="membersModal.loading" class="loading-row"><div class="spinner-sm" /></div>
            <div v-else-if="!membersModal.members.length" class="empty-members">
              등록된 학생이 없습니다.
            </div>
            <table v-else class="members-table">
              <thead>
                <tr>
                  <th>이름</th><th>이메일</th><th>학년</th><th>등급</th><th>가입일</th><th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="m in membersModal.members" :key="m.studentId">
                  <td class="fw-600">{{ m.studentName }}</td>
                  <td class="text-muted">{{ m.email }}</td>
                  <td>{{ gradeLabel(m.grade) }}</td>
                  <td><span :class="['level-badge', 'level-' + m.studentLevel]">{{ m.studentLevel }}</span></td>
                  <td class="text-muted">{{ m.joinedAt?.slice(0, 10) }}</td>
                  <td>
                    <button class="btn btn-ghost btn-sm danger" @click="removeMember(m.studentId)">제거</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-md" @click="closeMembersModal">닫기</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
// [2026-03-21] 교사 학급 관리 페이지
import { ref, reactive } from 'vue'
import api from '@/utils/api'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'

const { success, error } = useToast()
const dialog = useDialog()

const classes = ref([])
const loading = ref(false)
let allStudents = []

// ── 초대코드 복사 ─────────────────────────────────
async function copyCode(code) {
  if (!code) return
  try {
    await navigator.clipboard.writeText(code)
    success(`초대코드 ${code} 복사됨`)
  } catch {
    error('복사에 실패했습니다.')
  }
}

// ── 헬퍼 ────────────────────────────────────────
function gradeLabel(g) {
  return { GRADE_1: '1학년', GRADE_2: '2학년', GRADE_3: '3학년' }[g] || g || ''
}
function levelLabel(l) {
  return { ALL: '전체', A: 'A 심화', B: 'B 표준', C: 'C 기초' }[l] || l || 'ALL'
}

// ── 학급 목록 로드 ────────────────────────────────
async function loadClasses() {
  loading.value = true
  try {
    const res = await api.get('/teacher/classes')
    classes.value = res.data?.data || res.data || []
  } catch { error('학급 목록을 불러오지 못했습니다.') }
  finally { loading.value = false }
}

// ── 생성/수정 모달 ────────────────────────────────
const formModal = reactive({
  open: false, isEdit: false, classId: null,
  className: '', grade: '', levelFilter: 'ALL'
})

function openCreateModal() {
  Object.assign(formModal, { open: true, isEdit: false, classId: null, className: '', grade: '', levelFilter: 'ALL' })
}
function openEditModal(cls) {
  Object.assign(formModal, { open: true, isEdit: true, classId: cls.classId, className: cls.className, grade: cls.grade || '', levelFilter: cls.levelFilter || 'ALL' })
}
function closeFormModal() { formModal.open = false }

async function submitForm() {
  if (!formModal.className.trim()) return
  const body = { className: formModal.className, grade: formModal.grade || null, levelFilter: formModal.levelFilter }
  try {
    if (formModal.isEdit) {
      await api.put(`/teacher/classes/${formModal.classId}`, body)
      success('학급이 수정되었습니다.')
    } else {
      await api.post('/teacher/classes', body)
      success('학급이 생성되었습니다.')
    }
    closeFormModal()
    loadClasses()
  } catch { error('저장에 실패했습니다.') }
}

async function deleteClass(cls) {
  const ok = await dialog.confirm(`'${cls.className}' 학급을 삭제하시겠습니까?`, { type: 'danger', confirmText: '삭제' })
  if (!ok) return
  try {
    await api.delete(`/teacher/classes/${cls.classId}`)
    success('학급이 삭제되었습니다.')
    loadClasses()
  } catch { error('삭제에 실패했습니다.') }
}

// ── 학생 목록 모달 ────────────────────────────────
const membersModal = reactive({
  open: false, classId: null, className: '',
  members: [], loading: false,
  searchInput: '', searchResults: [], selectedToAdd: []
})

async function openMembersModal(cls) {
  Object.assign(membersModal, { open: true, classId: cls.classId, className: cls.className, members: [], loading: true, searchInput: '', searchResults: [], selectedToAdd: [] })
  await loadMembers()
  if (!allStudents.length) await loadAllStudents()
}
function closeMembersModal() { membersModal.open = false }

async function loadMembers() {
  membersModal.loading = true
  try {
    const res = await api.get(`/teacher/classes/${membersModal.classId}/members`)
    membersModal.members = res.data?.data || res.data || []
  } catch { error('학생 목록을 불러오지 못했습니다.') }
  finally { membersModal.loading = false }
}

async function loadAllStudents() {
  try {
    const res = await api.get('/teacher/students?page=0&size=200')
    const list = res.data?.data?.content || res.data?.content || []
    allStudents = list.map(s => ({ studentId: s.studentId, name: s.name, email: s.email, studentLevel: s.studentLevel }))
  } catch { /* 검색 기능만 비활성 */ }
}

function searchStudents() {
  const q = membersModal.searchInput.trim().toLowerCase()
  if (!q) { membersModal.searchResults = []; return }
  const memberIds = new Set(membersModal.members.map(m => m.studentId))
  membersModal.searchResults = allStudents
    .filter(s => !memberIds.has(s.studentId) && (s.name?.toLowerCase().includes(q) || s.email?.toLowerCase().includes(q)))
    .slice(0, 10)
}

function toggleAddStudent(id) {
  const idx = membersModal.selectedToAdd.indexOf(id)
  idx === -1 ? membersModal.selectedToAdd.push(id) : membersModal.selectedToAdd.splice(idx, 1)
}

async function addMembers() {
  if (!membersModal.selectedToAdd.length) return
  try {
    await api.post(`/teacher/classes/${membersModal.classId}/members`, { studentIds: membersModal.selectedToAdd })
    success(`${membersModal.selectedToAdd.length}명이 추가되었습니다.`)
    membersModal.selectedToAdd = []
    membersModal.searchInput = ''
    membersModal.searchResults = []
    await loadMembers()
    loadClasses()
  } catch { error('학생 추가에 실패했습니다.') }
}

async function removeMember(studentId) {
  const ok = await dialog.confirm('학급에서 학생을 제거하시겠습니까?', { type: 'danger', confirmText: '제거' })
  if (!ok) return
  try {
    await api.delete(`/teacher/classes/${membersModal.classId}/members/${studentId}`)
    success('학생이 제거되었습니다.')
    await loadMembers()
    loadClasses()
  } catch { error('제거에 실패했습니다.') }
}

loadClasses()
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-6;

  h1 { font-size: $font-size-xl; font-weight: 700; }

  .btn { display: flex; align-items: center; gap: $spacing-2; }
}

// ── 학급 카드 그리드 ──────────────────────────────
.class-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-5;
}

.class-card {
  background: $bg-white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  overflow: hidden;
  transition: box-shadow $transition-fast;

  &:hover { box-shadow: $shadow-md; }
}

.class-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: $spacing-5;
  border-bottom: 1px solid $border;
}

.class-name {
  font-size: $font-size-base;
  font-weight: 700;
  margin-bottom: $spacing-2;
}

.class-meta {
  display: flex;
  gap: $spacing-1;
  flex-wrap: wrap;
}

.meta-badge {
  padding: 2px 8px;
  background: $bg-light;
  border: 1px solid $border;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  color: $text-secondary;
  font-weight: 500;

  &.level { background: $primary-bg; color: $primary; border-color: transparent; }
}

.card-actions { display: flex; gap: $spacing-2; flex-shrink: 0; }

// ── 초대코드 ──────────────────────────────────
.class-card__invite {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-5;
  background: $bg-light;
  border-bottom: 1px solid $border;
}

.invite-label {
  font-size: $font-size-xs;
  color: $text-muted;
  flex-shrink: 0;
}

.invite-code {
  font-family: 'Courier New', monospace;
  font-weight: 700;
  font-size: $font-size-sm;
  letter-spacing: 0.15em;
  color: $primary;
  flex: 1;
}

.invite-copy-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: 1px solid $border;
  border-radius: $radius-md;
  background: $bg-white;
  color: $text-muted;
  cursor: pointer;
  transition: all $transition-fast;
  flex-shrink: 0;

  &:hover { border-color: $primary; color: $primary; background: $primary-bg; }
}

.class-card__body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4 $spacing-5;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover { background: $bg-light; }
}

.student-count {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  color: $text-secondary;
  font-size: $font-size-sm;

  strong { font-size: $font-size-lg; color: $text-primary; }
}

.view-members { font-size: $font-size-xs; color: $primary-light; }

// ── 빈 상태 ────────────────────────────────────
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-4;
  padding: $spacing-16;

  .empty-text { color: $text-muted; font-size: $font-size-base; }
}

.spinner {
  width: 32px; height: 32px;
  border: 3px solid $border;
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.spinner-sm {
  width: 20px; height: 20px;
  border: 2px solid $border;
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

// ── 모달 ──────────────────────────────────────
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
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
  max-width: 480px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25);

  &--wide { max-width: 700px; }
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
  width: 32px; height: 32px;
  border-radius: $radius-md;
  font-size: 16px;
  color: $text-muted;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background $transition-fast;

  &:hover { background: $bg-light; color: $text-primary; }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-5 $spacing-6;
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-3;
  padding: $spacing-4 $spacing-6;
  border-top: 1px solid $border;
}

// ── 폼 ────────────────────────────────────────
.form-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
  flex: 1;
}

.form-row {
  display: flex;
  gap: $spacing-4;
}

.form-label {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-secondary;

  .required { color: $danger; }
}

.form-input, .form-select {
  height: 40px;
  padding: 0 $spacing-3;
  border: 1px solid $border;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  background: $bg-white;
  color: $text-primary;
  outline: none;
  width: 100%;

  &:focus { border-color: $primary; }
}

// ── 학생 추가 검색 ──────────────────────────────
.add-member-row {
  display: flex;
  gap: $spacing-3;

  .form-input { flex: 1; }
}

.search-results {
  border: 1px solid $border;
  border-radius: $radius-md;
  overflow: hidden;
  margin-top: -$spacing-2;
}

.search-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-2 $spacing-3;
  cursor: pointer;
  font-size: $font-size-sm;
  transition: background $transition-fast;

  &:not(:last-child) { border-bottom: 1px solid $border; }
  &:hover { background: $bg-light; }
  &.selected { background: $primary-bg; }

  .text-muted { flex: 1; color: $text-muted; font-size: $font-size-xs; }
}

.members-divider {
  border-top: 2px solid $border;
  margin: 0 (-$spacing-6);
}

// ── 학생 테이블 ────────────────────────────────
.members-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;

  th {
    padding: $spacing-2 $spacing-3;
    text-align: left;
    font-size: $font-size-xs;
    font-weight: 600;
    color: $text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    background: $bg-light;
    border-bottom: 2px solid $border;
  }

  td {
    padding: $spacing-3;
    border-bottom: 1px solid $border;
    color: $text-primary;

    &:last-child { border-bottom: none; }
  }

  tr:last-child td { border-bottom: none; }
}

.fw-600 { font-weight: 600; }
.text-muted { color: $text-muted; font-size: $font-size-xs; }

.level-badge {
  display: inline-block;
  padding: 2px 7px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 700;

  &.level-A { background: #D1FAE5; color: #065F46; }
  &.level-B { background: #DBEAFE; color: #1E40AF; }
  &.level-C { background: #FEE2E2; color: #991B1B; }
}

.loading-row {
  display: flex;
  justify-content: center;
  padding: $spacing-5;
}

.empty-members {
  text-align: center;
  padding: $spacing-8;
  color: $text-muted;
  font-size: $font-size-sm;
}

.btn.danger { color: $danger; &:hover { background: #FEE2E2; } }
</style>
