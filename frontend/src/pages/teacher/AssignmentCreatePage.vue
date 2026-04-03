<template>
  <div class="assignment-create">
    <div class="page-header"><h1>과제 생성</h1></div>

    <div class="create-layout">
      <!-- 좌측: 과제 정보 -->
      <div class="create-main">
        <div class="card">
          <h3 class="section-title">기본 정보</h3>
          <AppInput v-model="form.title" label="과제 제목" placeholder="과제 제목을 입력하세요" :error="errors.title" required />
          <AppSelect v-model="form.subject" label="과목" :options="subjectOptions" placeholder="과목 선택" />
          <div class="form-group">
            <label>마감일 <span class="required">*</span></label>
            <input type="date" v-model="form.dueDate" class="form-control" @click="$event.target.showPicker()" />
          </div>
        </div>

        <!-- 문제 선택 -->
        <div class="card">
          <div class="card__header">
            <h3>문제 선택 <span class="count-badge">{{ selectedProblems.length }}개 선택</span></h3>
            <button v-if="selectedProblems.length" class="btn btn-ghost btn-sm" @click="openOrderModal">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 5h10"/><path d="M11 9h7"/><path d="M11 13h4"/><circle cx="4" cy="5" r="2"/><circle cx="4" cy="9" r="2"/><circle cx="4" cy="13" r="2"/></svg>
              순서 편집
            </button>
          </div>
          <div class="problem-search-row">
            <input v-model="problemSearch" class="form-control" placeholder="문제 검색..." />
          </div>
          <div v-if="!form.subject" class="problem-empty-hint">
            과목을 먼저 선택하세요.
          </div>
          <div v-else class="problem-select-list">
            <div
              v-for="p in filteredProblems"
              :key="p.id"
              :class="['problem-select-item', { selected: isSelected(p.id) }]"
              @click="toggleProblem(p)"
            >
              <input type="checkbox" :checked="isSelected(p.id)" @click.prevent />
              <span v-if="isSelected(p.id)" class="order-num">{{ getOrder(p.id) }}</span>
              <div class="problem-info">
                <div class="meta">
                  <span class="subject-tag">{{ p.subject }}</span>
                  <AppBadge :type="p.level" />
                  <span class="unit-text">{{ p.unit }}</span>
                </div>
                <p class="question-preview">{{ p.questionText }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 우측: 배정 대상 -->
      <div class="create-side" ref="sideRef">
        <div class="card">
          <h3 class="section-title">배정 대상</h3>

          <!-- [2026-04-03] 학급별/개인별 탭 -->
          <div class="target-mode-tabs">
            <button :class="['mode-tab', { active: targetMode === 'class' }]" @click="targetMode = 'class'">
              학급별
            </button>
            <button :class="['mode-tab', { active: targetMode === 'individual' }]" @click="targetMode = 'individual'">
              개인별
            </button>
          </div>

          <!-- 학급별 선택 -->
          <template v-if="targetMode === 'class'">
            <p class="hint">과제를 배정할 학급을 선택하세요</p>
            <div v-if="classes.length === 0" class="problem-empty-hint">
              등록된 학급이 없습니다.<br>학급 관리 페이지에서 먼저 학급을 만들어 주세요.
            </div>
            <div v-else class="class-select-list">
              <label v-for="c in classes" :key="c.id" :class="['class-select-item', { selected: form.targetClasses.includes(c.id) }]">
                <input type="checkbox" :value="c.id" v-model="form.targetClasses" />
                <div class="class-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                </div>
                <div class="class-info">
                  <p class="class-name">{{ c.name }}</p>
                  <span class="class-meta">{{ c.grade }} · {{ c.memberCount }}명</span>
                </div>
              </label>
            </div>
            <div class="selected-summary">
              <span>{{ form.targetClasses.length }}개 학급 선택 (약 {{ selectedClassMemberCount }}명)</span>
              <button class="btn btn-ghost btn-sm" @click="selectAllClasses">전체 선택</button>
            </div>
          </template>

          <!-- 개인별 선택 -->
          <template v-else>
            <p class="hint">과제를 받을 학생을 선택하세요</p>
            <div class="student-select-list">
              <label v-for="s in students" :key="s.id" class="student-select-item">
                <input type="checkbox" :value="s.id" v-model="form.targetStudents" />
                <div class="avatar-sm">{{ s.name.charAt(0) }}</div>
                <div>
                  <p>{{ s.name }}</p>
                  <AppBadge :type="s.level" />
                </div>
              </label>
            </div>
            <div class="selected-summary">
              <span>{{ form.targetStudents.length }}명 선택됨</span>
              <button class="btn btn-ghost btn-sm" @click="selectAll">전체 선택</button>
            </div>
          </template>
        </div>

        <!-- [2026-04-03] 등급별 자동 배정 옵션 -->
        <div class="card">
          <h3 class="section-title">배정 방식</h3>
          <label class="auto-assign-toggle">
            <div class="toggle-switch" :class="{ on: autoAssign }" @click="autoAssign = !autoAssign">
              <div class="toggle-knob" />
            </div>
            <div class="toggle-label">
              <p>등급별 자동 분리 배정</p>
              <span>A문제→A학생, B문제→B학생, C문제→C학생으로 자동 분리</span>
            </div>
          </label>
          <div v-if="autoAssign && levelGroups.length" class="level-preview">
            <div v-for="g in levelGroups" :key="g.level" class="level-preview-row">
              <AppBadge :type="g.level" />
              <span>{{ g.count }}문제</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
              <span class="muted">{{ g.level }}등급 학생에게 배정</span>
            </div>
            <p v-if="noLevelProblems > 0" class="level-preview-warn">
              ⚠ 등급 미지정 문제 {{ noLevelProblems }}개는 배정에서 제외됩니다.
            </p>
          </div>
          <p v-else-if="autoAssign" class="hint">문제를 선택하면 등급별 분리 현황이 표시됩니다.</p>
        </div>

        <div class="card">
          <h3 class="section-title">요약</h3>
          <div class="summary-list">
            <div class="summary-item"><span>과제 제목</span><strong>{{ form.title || '-' }}</strong></div>
            <div class="summary-item"><span>과목</span><strong>{{ form.subject || '-' }}</strong></div>
            <div class="summary-item"><span>문제 수</span><strong>{{ selectedProblems.length }}문제</strong></div>
            <div class="summary-item">
              <span>배정 방식</span>
              <strong>{{ targetMode === 'class' ? '학급별' : '개인별' }} {{ autoAssign ? '· 등급 자동 분리' : '' }}</strong>
            </div>
            <div class="summary-item">
              <span>대상</span>
              <strong v-if="targetMode === 'class'">{{ form.targetClasses.length }}개 학급</strong>
              <strong v-else>{{ form.targetStudents.length }}명</strong>
            </div>
            <div class="summary-item"><span>마감일</span><strong>{{ form.dueDate || '-' }}</strong></div>
          </div>
          <div class="create-btns">
            <AppButton :loading="saving" block @click="autoAssign ? openPreview() : saveAssignment()">과제 생성</AppButton>
            <AppButton variant="secondary" block @click="$router.back()">취소</AppButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 순서 편집 모달 -->
    <Teleport to="body">
      <div v-if="orderModalOpen" class="modal-overlay" @click.self="orderModalOpen = false">
        <div class="modal-box order-modal">
          <div class="modal-box__header">
            <h2>문제 순서 편집</h2>
            <button class="btn-close" @click="orderModalOpen = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <p class="modal-hint">드래그하여 문제 순서를 변경하세요.</p>
          <div class="order-list" ref="orderListRef">
            <div
              v-for="(p, idx) in tempOrder"
              :key="p.id"
              class="order-item"
              draggable="true"
              @dragstart="dragStart(idx, $event)"
              @dragover.prevent="dragOver(idx, $event)"
              @dragenter.prevent
              @drop="drop(idx)"
              @dragend="dragEnd"
              :class="{ dragging: dragIdx === idx, 'drag-over': dragOverIdx === idx && dragIdx !== idx }"
            >
              <div class="order-item__handle">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="5" r="1"/><circle cx="15" cy="5" r="1"/><circle cx="9" cy="12" r="1"/><circle cx="15" cy="12" r="1"/><circle cx="9" cy="19" r="1"/><circle cx="15" cy="19" r="1"/></svg>
              </div>
              <span class="order-item__no">{{ idx + 1 }}</span>
              <div class="order-item__info">
                <div class="order-item__meta">
                  <span class="subject-tag">{{ p.subject }}</span>
                  <AppBadge :type="p.level" />
                  <span class="unit-text">{{ p.unit }}</span>
                </div>
                <p class="order-item__text">{{ p.questionText }}</p>
              </div>
              <div class="order-item__arrows">
                <button class="arrow-btn" :disabled="idx === 0" @click="moveUp(idx)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg>
                </button>
                <button class="arrow-btn" :disabled="idx === tempOrder.length - 1" @click="moveDown(idx)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
                </button>
              </div>
            </div>
          </div>
          <div class="modal-box__footer">
            <button class="btn btn-secondary" @click="orderModalOpen = false">취소</button>
            <button class="btn btn-primary" @click="applyOrder">적용</button>
          </div>
        </div>
      </div>
    </Teleport>
    <!-- [2026-04-03] 등급별 자동 배정 미리보기 모달 -->
    <Teleport to="body">
      <div v-if="previewModalOpen" class="modal-overlay" @click.self="previewModalOpen = false">
        <div class="modal-box preview-modal">
          <div class="modal-box__header">
            <h2>등급별 자동 배정 미리보기</h2>
            <button class="btn-close" @click="previewModalOpen = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="preview-body">
            <p class="preview-desc">선택한 문제가 등급별로 분리되어 각각 별도 과제로 생성됩니다.</p>
            <div v-if="levelGroups.length" class="preview-level-list">
              <div v-for="g in levelGroups" :key="g.level" class="preview-level-row">
                <div class="preview-level-badge">
                  <AppBadge :type="g.level" />
                </div>
                <div class="preview-level-info">
                  <p><strong>"{{ form.title }} ({{ g.level }}레벨)"</strong></p>
                  <span>문제 {{ g.count }}개 → {{ g.level }}등급 학생에게 배정</span>
                </div>
              </div>
            </div>
            <p v-if="noLevelProblems > 0" class="preview-warn">
              ⚠ 등급 미지정 문제 {{ noLevelProblems }}개는 배정에서 제외됩니다.
            </p>
            <div class="preview-target-info">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <span>{{ targetMode === 'class' ? form.targetClasses.length + '개 학급 내 등급별 학생' : form.targetStudents.length + '명 중 등급별 학생' }}에게 각 과제가 배정됩니다.</span>
            </div>
          </div>
          <div class="modal-box__footer">
            <button class="btn btn-secondary" @click="previewModalOpen = false">취소</button>
            <button class="btn btn-primary" :disabled="saving" @click="saveAssignment">
              {{ saving ? '생성 중...' : '과제 생성' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import AppInput from '@/components/common/AppInput.vue'
import AppSelect from '@/components/common/AppSelect.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const router = useRouter()
const { success, error } = useToast()
const saving = ref(false)
const problemSearch = ref('')
const sideRef = ref(null)

// [2026-04-03] 배정 모드: 학급별 / 개인별
const targetMode = ref('class')
// [2026-04-03] 등급별 자동 배정
const autoAssign = ref(false)
const previewModalOpen = ref(false)

const form = reactive({
  title: '',
  subject: '',
  dueDate: '',
  targetStudents: [],
  targetClasses: []
})

const errors = reactive({ title: '' })

const subjectOptions = ref([])

const problems = ref([])
const selectedProblems = ref([])
const students = ref([])
const classes = ref([]) // [2026-04-03] 학급 목록

// 순서 편집 모달
const orderModalOpen = ref(false)
const tempOrder = ref([])
const orderListRef = ref(null)
const dragIdx = ref(null)
const dragOverIdx = ref(null)

async function fetchSubjects() {
  try {
    const res = await api.get('/codes', { params: { group: 'SUBJECT' } })
    const list = res.data?.data || res.data || []
    subjectOptions.value = list.map(c => ({ value: c.codeName, label: c.codeName }))
  } catch {
    subjectOptions.value = ['수학','영어','국어','과학','사회'].map(v => ({ value: v, label: v }))
  }
}

async function fetchProblems() {
  try {
    const params = { size: 100 }
    const res = await api.get('/problems', { params })
    const list = res.data?.content || (Array.isArray(res.data) ? res.data : [])
    problems.value = list.map(p => ({
      id: p.problemId,
      subject: p.subject,
      level: p.level,
      unit: p.unitName,
      questionText: p.questionText
    }))
  } catch (e) {
    error('문제 목록을 불러오지 못했습니다.')
  }
}

async function fetchStudents() {
  try {
    const res = await api.get('/teacher/students', { params: { size: 200 } })
    const list = res.data?.content || (Array.isArray(res.data) ? res.data : [])
    students.value = list.map(s => ({
      id: s.studentId,
      name: s.name,
      level: s.studentLevel || s.level
    }))
  } catch (e) {
    error('학생 목록을 불러오지 못했습니다.')
  }
}

// [2026-04-03] 학급 목록 조회
async function fetchClasses() {
  try {
    const res = await api.get('/teacher/classes')
    const list = res.data?.data || res.data || []
    classes.value = list.map(c => ({
      id: c.classId,
      name: c.className,
      grade: c.grade ? c.grade.replace('GRADE_', '중') + '학년' : '-',
      memberCount: c.studentCount || 0
    }))
  } catch (e) {
    error('학급 목록을 불러오지 못했습니다.')
  }
}

// [2026-04-03] 선택한 문제의 등급별 그룹 (자동 배정 미리보기용)
const levelGroups = computed(() => {
  const map = {}
  for (const p of selectedProblems.value) {
    if (!p.level) continue
    if (!map[p.level]) map[p.level] = 0
    map[p.level]++
  }
  return ['A', 'B', 'C']
    .filter(lv => map[lv] > 0)
    .map(lv => ({ level: lv, count: map[lv] }))
})

const noLevelProblems = computed(() =>
  selectedProblems.value.filter(p => !p.level).length
)

function openPreview() {
  errors.title = ''
  if (!form.title) { errors.title = '과제 제목을 입력하세요.'; return }
  if (!selectedProblems.value.length) { error('문제를 1개 이상 선택하세요.'); return }
  if (targetMode.value === 'class' && !form.targetClasses.length) { error('배정할 학급을 선택하세요.'); return }
  if (targetMode.value === 'individual' && !form.targetStudents.length) { error('대상 학생을 선택하세요.'); return }
  previewModalOpen.value = true
}

// [2026-04-03] 선택된 학급의 총 예상 학생 수
const selectedClassMemberCount = computed(() => {
  return classes.value
    .filter(c => form.targetClasses.includes(c.id))
    .reduce((sum, c) => sum + c.memberCount, 0)
})

watch(() => form.subject, () => {
  selectedProblems.value = []
})

onMounted(async () => {
  fetchSubjects()
  fetchProblems()
  fetchClasses() // [2026-04-03]
  await fetchStudents()
})

const filteredProblems = computed(() => {
  let list = problems.value
  if (form.subject) list = list.filter(p => p.subject === form.subject)
  if (problemSearch.value) list = list.filter(p =>
    p.questionText?.includes(problemSearch.value) || p.unit?.includes(problemSearch.value)
  )
  return list
})

function isSelected(id) {
  return selectedProblems.value.some(p => p.id === id)
}

function getOrder(id) {
  const idx = selectedProblems.value.findIndex(p => p.id === id)
  return idx >= 0 ? idx + 1 : ''
}

function toggleProblem(p) {
  const idx = selectedProblems.value.findIndex(sp => sp.id === p.id)
  if (idx === -1) selectedProblems.value.push(p)
  else selectedProblems.value.splice(idx, 1)
}

function selectAll() {
  form.targetStudents = students.value.map(s => s.id)
}

// [2026-04-03]
function selectAllClasses() {
  form.targetClasses = classes.value.map(c => c.id)
}

// 순서 편집
function openOrderModal() {
  tempOrder.value = [...selectedProblems.value]
  orderModalOpen.value = true
}

function moveUp(idx) {
  if (idx <= 0) return
  const arr = tempOrder.value
  ;[arr[idx - 1], arr[idx]] = [arr[idx], arr[idx - 1]]
}

function moveDown(idx) {
  if (idx >= tempOrder.value.length - 1) return
  const arr = tempOrder.value
  ;[arr[idx], arr[idx + 1]] = [arr[idx + 1], arr[idx]]
}

function dragStart(idx, e) {
  dragIdx.value = idx
  e.dataTransfer.effectAllowed = 'move'
}

function dragOver(idx) {
  dragOverIdx.value = idx
}

function drop(idx) {
  if (dragIdx.value === null || dragIdx.value === idx) return
  const arr = tempOrder.value
  const item = arr.splice(dragIdx.value, 1)[0]
  arr.splice(idx, 0, item)
  dragIdx.value = null
  dragOverIdx.value = null
}

function dragEnd() {
  dragIdx.value = null
  dragOverIdx.value = null
}

function applyOrder() {
  selectedProblems.value = [...tempOrder.value]
  orderModalOpen.value = false
}

// [2026-04-03] 학급별/개인별 분기 처리
async function saveAssignment() {
  errors.title = ''
  if (!form.title) { errors.title = '과제 제목을 입력하세요.'; return }
  if (!selectedProblems.value.length) { error('문제를 1개 이상 선택하세요.'); return }

  if (targetMode.value === 'class' && !form.targetClasses.length) {
    error('배정할 학급을 선택하세요.'); return
  }
  if (targetMode.value === 'individual' && !form.targetStudents.length) {
    error('대상 학생을 선택하세요.'); return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title,
      description: '',
      problemIds: selectedProblems.value.map(p => p.id),
      dueDate: form.dueDate ? form.dueDate + 'T23:59:59' : null,
      notifyEmail: false,
      isAutoAssign: autoAssign.value
    }

    if (targetMode.value === 'class') {
      payload.targetType = 'CLASS'
      payload.classIds = form.targetClasses
    } else {
      payload.targetType = 'INDIVIDUAL'
      payload.studentIds = form.targetStudents
    }

    await api.post('/teacher/assignments', payload)
    previewModalOpen.value = false
    success(autoAssign.value ? '등급별 과제가 자동 생성됐습니다.' : '과제를 생성했습니다.')
    router.push('/teacher/assignments')
  } catch (e) {
    error('과제 생성에 실패했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.create-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }
}

.create-main {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

.create-side {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 700;
  margin-bottom: $spacing-5;
}

.count-badge {
  background: $primary-bg;
  color: $primary;
  padding: 2px $spacing-3;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;
  margin-left: $spacing-2;
}

.card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-4;
  h3 { margin: 0; font-size: $font-size-lg; font-weight: 700; }
}

.problem-search-row {
  margin-bottom: $spacing-4;
  .form-control { width: 100%; }
}

.problem-empty-hint {
  color: $text-muted;
  font-size: $font-size-sm;
  text-align: center;
  padding: $spacing-8 0;
}

.problem-select-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  max-height: 420px;
  overflow-y: auto;
}

.problem-select-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-3;
  padding: $spacing-4;
  border: 1.5px solid $border;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover { border-color: $primary-light; background: $primary-bg; }
  &.selected { border-color: $primary-light; background: $primary-bg; }
}

.order-num {
  width: 22px; height: 22px; border-radius: 50%;
  background: $primary; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; flex-shrink: 0; margin-top: 2px;
}

.problem-info {
  flex: 1; min-width: 0;
  .meta {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    margin-bottom: $spacing-2;
  }

  .unit-text {
    font-size: $font-size-xs;
    color: $text-muted;
  }

  .question-preview {
    font-size: $font-size-sm;
    color: $text-primary;
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }
}

// [2026-04-03] 학급별/개인별 탭
.target-mode-tabs {
  display: flex;
  gap: 0;
  margin-bottom: $spacing-4;
  border: 1px solid $border;
  border-radius: $radius-md;
  overflow: hidden;
}

.mode-tab {
  flex: 1;
  padding: $spacing-2 $spacing-4;
  font-size: $font-size-sm;
  font-weight: 600;
  background: white;
  border: none;
  cursor: pointer;
  color: $text-secondary;
  transition: all $transition-fast;

  &.active {
    background: $primary;
    color: white;
  }

  &:not(.active):hover {
    background: $bg-light;
  }
}

.class-select-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  margin-bottom: $spacing-4;
  max-height: 320px;
  overflow-y: auto;
}

.class-select-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3;
  border-radius: $radius-md;
  cursor: pointer;
  border: 1.5px solid $border;
  transition: all $transition-fast;

  &:hover { background: $bg-light; border-color: $primary-light; }
  &.selected { background: $primary-bg; border-color: $primary-light; }

  .class-icon {
    width: 36px; height: 36px; border-radius: $radius-md;
    background: $primary-bg; color: $primary;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0;
  }

  .class-info {
    flex: 1;
    .class-name { font-size: $font-size-sm; font-weight: 600; margin-bottom: 2px; }
    .class-meta { font-size: $font-size-xs; color: $text-muted; }
  }
}

.student-select-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  margin-bottom: $spacing-4;
}

.student-select-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3;
  border-radius: $radius-md;
  cursor: pointer;
  border: 1px solid $border;
  transition: background $transition-fast;

  &:hover { background: $bg-light; }

  p {
    font-size: $font-size-sm;
    font-weight: 600;
    margin-bottom: 2px;
  }
}

.avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: $primary-bg;
  color: $primary;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: 700;
  flex-shrink: 0;
}

.selected-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: $font-size-sm;
  color: $text-secondary;
  padding-top: $spacing-3;
  border-top: 1px solid $border;
}

.hint {
  font-size: $font-size-sm;
  color: $text-muted;
  margin-bottom: $spacing-4;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-sm;

  span { color: $text-secondary; }
  strong { color: $text-primary; }
}

.create-btns {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.subject-tag {
  background: $primary-bg;
  color: $primary;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 600;
}

.required { color: $danger; }

// ── [2026-04-03] 등급별 자동 배정 ─────────────────
.auto-assign-toggle {
  display: flex;
  align-items: flex-start;
  gap: $spacing-4;
  cursor: pointer;
  padding: $spacing-3 0;
}

.toggle-switch {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: $border;
  position: relative;
  flex-shrink: 0;
  transition: background $transition-fast;
  cursor: pointer;

  &.on { background: $primary; }
}

.toggle-knob {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  position: absolute;
  top: 3px;
  left: 3px;
  transition: left $transition-fast;
  box-shadow: 0 1px 3px rgba(0,0,0,.2);

  .toggle-switch.on & { left: 23px; }
}

.toggle-label {
  p { font-size: $font-size-sm; font-weight: 600; color: $text-primary; margin-bottom: 2px; }
  span { font-size: $font-size-xs; color: $text-muted; }
}

.level-preview {
  margin-top: $spacing-4;
  padding: $spacing-4;
  background: $bg-light;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.level-preview-row {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  font-size: $font-size-sm;
  color: $text-secondary;

  .muted { color: $text-muted; }
}

.level-preview-warn {
  font-size: $font-size-xs;
  color: #F59E0B;
  padding-top: $spacing-2;
  border-top: 1px solid $border;
}

// ── [2026-04-03] 미리보기 모달 ───────────────────
.preview-modal {
  width: 480px;
}

.preview-body {
  padding: 0 $spacing-6 $spacing-6;
}

.preview-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-5;
}

.preview-level-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
}

.preview-level-row {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-4;
  border: 1px solid $border;
  border-radius: $radius-md;
  background: $bg-light;
}

.preview-level-badge { flex-shrink: 0; }

.preview-level-info {
  flex: 1;
  p { font-size: $font-size-sm; margin-bottom: 2px; }
  span { font-size: $font-size-xs; color: $text-muted; }
}

.preview-warn {
  font-size: $font-size-xs;
  color: #F59E0B;
  margin-bottom: $spacing-4;
}

.preview-target-info {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  font-size: $font-size-xs;
  color: $text-muted;
  padding: $spacing-3 $spacing-4;
  background: $bg-light;
  border-radius: $radius-md;
}

// 순서 편집 모달
.modal-overlay {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
}

.modal-box {
  background: white; border-radius: $radius-xl;
  box-shadow: $shadow-lg;
  width: 640px; max-width: 90vw; max-height: 85vh;
  display: flex; flex-direction: column;

  &__header {
    display: flex; align-items: center; justify-content: space-between;
    padding: $spacing-6 $spacing-6 $spacing-4;
    h2 { font-size: $font-size-lg; font-weight: 700; }
  }

  &__footer {
    display: flex; justify-content: flex-end; gap: $spacing-3;
    padding: $spacing-4 $spacing-6 $spacing-6;
    border-top: 1px solid $border;
  }
}

.modal-hint {
  padding: 0 $spacing-6 $spacing-4;
  font-size: $font-size-sm; color: $text-muted;
}

.btn-close {
  background: none; border: none; cursor: pointer; padding: $spacing-2;
  border-radius: $radius-md;
  &:hover { background: $bg-light; }
}

.order-list {
  flex: 1; overflow-y: auto; padding: 0 $spacing-6;
  display: flex; flex-direction: column; gap: $spacing-2;
  min-height: 0;
}

.order-item {
  display: flex; align-items: center; gap: $spacing-3;
  padding: $spacing-3 $spacing-4;
  border: 1.5px solid $border; border-radius: $radius-md;
  background: white;
  transition: all 0.15s ease;
  cursor: grab;

  &:active { cursor: grabbing; }
  &.dragging { opacity: 0.4; }
  &.drag-over { border-color: $primary; background: $primary-bg; }

  &__handle {
    flex-shrink: 0; color: $text-muted; cursor: grab;
    display: flex; align-items: center;
  }

  &__no {
    width: 26px; height: 26px; border-radius: 50%;
    background: $primary; color: white;
    display: flex; align-items: center; justify-content: center;
    font-size: $font-size-xs; font-weight: 800; flex-shrink: 0;
  }

  &__info { flex: 1; min-width: 0; }

  &__meta {
    display: flex; align-items: center; gap: $spacing-2; margin-bottom: 2px;
    .unit-text { font-size: $font-size-xs; color: $text-muted; }
  }

  &__text {
    font-size: $font-size-sm; color: $text-primary;
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }

  &__arrows {
    display: flex; flex-direction: column; gap: 2px; flex-shrink: 0;
  }
}

.arrow-btn {
  width: 24px; height: 24px; border: 1px solid $border; border-radius: $radius-sm;
  background: white; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: $text-secondary;
  &:hover:not(:disabled) { background: $bg-light; color: $primary; border-color: $primary-light; }
  &:disabled { opacity: 0.3; cursor: default; }
}
</style>
