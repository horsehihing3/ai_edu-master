<template>
  <div class="assignment-detail">
    <AppBreadcrumb :items="[{ label: '과제 관리', to: '/teacher/assignments' }, { label: assignment.title }]" />
    <div class="detail-header">
      <div>
        <h1>{{ assignment.title }}</h1>
        <span :class="['status-pill', assignment.status]">{{ statusLabel(assignment.status) }}</span>
      </div>
      <RouterLink to="/teacher/assignments" class="btn btn-secondary btn-sm">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        목록으로
      </RouterLink>
    </div>

    <div class="detail-grid">
      <!-- 좌측: 과제 정보 -->
      <div class="card">
        <h3>과제 정보</h3>
        <div class="info-list">
          <div class="info-item"><span>과목</span><strong>{{ assignment.subject }}</strong></div>
          <div class="info-item"><span>문제 수</span><strong>{{ assignment.problemCount }}문제</strong></div>
          <div class="info-item"><span>대상 학생</span><strong>{{ assignment.targetCount }}명</strong></div>
          <div class="info-item"><span>마감일</span><strong>{{ assignment.dueDate }}</strong></div>
          <div class="info-item"><span>완료율</span>
            <div style="display:flex;align-items:center;gap:8px;flex:1;">
              <div class="progress-bar" style="max-width:120px;flex:1;"><div class="progress-bar__fill" :style="{width:assignment.completionRate+'%'}" /></div>
              <strong>{{ assignment.completionRate }}%</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- 우측 -->
      <div class="detail-right">
        <!-- 출제 문제 -->
        <div class="card">
          <h3>출제 문제 <span class="count-badge">{{ problems.length }}문제</span></h3>
          <div class="problem-list">
            <div v-for="(p, idx) in problems" :key="p.problemId" class="problem-item">
              <span class="problem-no">{{ idx + 1 }}</span>
              <div class="problem-info">
                <div class="problem-meta">
                  <span class="subject-tag">{{ p.subject }}</span>
                  <AppBadge :type="p.level" />
                  <span class="unit-text">{{ p.unitName }}</span>
                </div>
                <p class="problem-text">{{ p.questionText }}</p>
              </div>
            </div>
            <p v-if="!problems.length" class="empty-text">등록된 문제가 없습니다.</p>
          </div>
        </div>

        <!-- 학생별 현황 (클릭 가능) -->
        <div class="card">
          <h3>학생별 현황</h3>
          <div class="student-list">
            <div
              v-for="s in studentProgress" :key="s.studentId"
              :class="['student-row', { active: selectedStudent?.studentId === s.studentId }]"
              @click="selectStudent(s)"
            >
              <div class="student-row__info">
                <div class="avatar-sm">{{ s.name.charAt(0) }}</div>
                <div>
                  <p class="student-name">{{ s.name }}</p>
                  <p class="student-grade">{{ s.grade }}</p>
                </div>
              </div>
              <div class="student-row__stats">
                <span :class="['mini-badge', s.status]">{{ s.status === 'done' ? '완료' : '미완료' }}</span>
                <span v-if="s.accuracy !== null" class="accuracy-text">{{ s.accuracy }}%</span>
                <span v-else class="accuracy-text muted">-</span>
              </div>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
            </div>
          </div>
          <p v-if="!studentProgress.length" class="empty-text">배정된 학생이 없습니다.</p>
        </div>
      </div>
    </div>

    <!-- 학생 상세 패널 (학생 클릭 시 표시) -->
    <div v-if="selectedStudent" class="student-detail-panel">
      <div class="panel-overlay" @click="selectedStudent = null" />
      <div class="panel-content">
        <div class="panel-header">
          <div class="panel-header__left">
            <div class="avatar-md">{{ selectedStudent.name.charAt(0) }}</div>
            <div>
              <h2>{{ selectedStudent.name }}</h2>
              <p>{{ selectedStudent.grade }} · 정답률 {{ selectedStudent.accuracy ?? '-' }}%</p>
            </div>
          </div>
          <button class="btn-close" @click="selectedStudent = null">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <!-- 탭 -->
        <div class="panel-tabs">
          <button :class="['panel-tab', { active: panelTab === 'attempts' }]" @click="panelTab = 'attempts'">문제 풀이 상세</button>
          <button :class="['panel-tab', { active: panelTab === 'feedback' }]" @click="panelTab = 'feedback'">전체 피드백</button>
        </div>

        <!-- 문제 풀이 상세 탭 -->
        <div v-if="panelTab === 'attempts'" class="panel-body">
          <div v-if="!attempts.length" class="empty-text">풀이 기록이 없습니다.</div>
          <div v-for="(a, idx) in attempts" :key="a.attemptId" class="attempt-card">
            <div class="attempt-header">
              <span class="attempt-no">{{ idx + 1 }}</span>
              <span :class="['result-badge', a.isCorrect ? 'correct' : 'wrong']">
                {{ a.isCorrect ? '정답' : '오답' }}
              </span>
              <span class="attempt-subject">{{ a.subject }}</span>
              <span class="attempt-level">Lv.{{ a.level }}</span>
            </div>
            <p class="attempt-question">{{ a.questionText }}</p>
            <div class="attempt-answers">
              <div class="answer-item">
                <span class="answer-label">제출 답안</span>
                <span :class="['answer-value', a.isCorrect ? '' : 'wrong']">{{ a.submittedAnswer }}번</span>
              </div>
              <div class="answer-item">
                <span class="answer-label">정답</span>
                <span class="answer-value correct">{{ a.correctAnswer }}번</span>
              </div>
            </div>

            <!-- 오답인 경우 피드백 영역 -->
            <div v-if="!a.isCorrect" class="feedback-area">
              <div v-if="a.feedbackComment || a.feedbackDrawingUrl" class="existing-feedback">
                <p class="feedback-label">작성된 피드백</p>
                <p v-if="a.feedbackComment" class="feedback-text">{{ a.feedbackComment }}</p>
                <img v-if="a.feedbackDrawingUrl" :src="a.feedbackDrawingUrl" class="feedback-drawing" alt="피드백 드로잉" />
              </div>
              <div class="feedback-input-area">
                <textarea
                  v-model="a.newComment"
                  :placeholder="a.feedbackComment ? '피드백 수정...' : '이 문제에 대한 피드백을 입력하세요...'"
                  rows="2"
                  class="feedback-textarea"
                />
                <div class="feedback-tools">
                  <button class="btn-draw" @click="openDrawing(a)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                    펜 입력
                  </button>
                  <button class="btn btn-primary btn-sm" @click="saveAttemptFeedback(a)" :disabled="!a.newComment?.trim() && !a.feedbackDrawingUrl">
                    {{ a.feedbackComment ? '수정' : '저장' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 전체 피드백 탭 -->
        <div v-if="panelTab === 'feedback'" class="panel-body">
          <div v-if="overallFeedback" class="existing-feedback overall">
            <p class="feedback-label">기존 피드백 ({{ formatDateTime(overallFeedback.updatedAt || overallFeedback.createdAt) }})</p>
            <p v-if="overallFeedback.comment" class="feedback-text">{{ overallFeedback.comment }}</p>
            <img v-if="overallFeedback.drawingUrl" :src="overallFeedback.drawingUrl" class="feedback-drawing" alt="피드백 드로잉" />
          </div>
          <div class="feedback-input-area overall">
            <label class="feedback-label">{{ overallFeedback ? '피드백 수정' : '전체 피드백 작성' }}</label>
            <textarea
              v-model="overallComment"
              placeholder="이 과제에 대한 전체적인 피드백을 입력하세요..."
              rows="4"
              class="feedback-textarea"
            />
            <div class="feedback-tools">
              <button class="btn-draw" @click="openDrawingOverall()">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                펜 입력
              </button>
              <button class="btn btn-primary btn-sm" @click="saveOverallFeedback" :disabled="!overallComment?.trim() && !overallDrawingUrl">
                {{ overallFeedback ? '수정' : '저장' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 드로잉 모달 -->
        <div v-if="drawingOpen" class="drawing-modal">
          <div class="drawing-modal__header">
            <h3>펜 입력</h3>
            <div class="drawing-tools">
              <input type="color" v-model="penColor" class="color-picker" :disabled="eraserMode" />
              <select v-model="penSize" class="pen-size-select">
                <option :value="2">얇게</option>
                <option :value="4">보통</option>
                <option :value="8">굵게</option>
              </select>
              <button :class="['btn btn-sm', eraserMode ? 'btn-primary' : 'btn-ghost']" @click="eraserMode = !eraserMode">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 20H7L3 16l10-10 7 7-2.5 2.5"/><path d="M6.5 17.5l5-5"/></svg>
                지우개
              </button>
              <select v-if="eraserMode" v-model="eraserSize" class="pen-size-select">
                <option :value="8">작게</option>
                <option :value="18">보통</option>
                <option :value="36">크게</option>
              </select>
              <button class="btn btn-ghost btn-sm" @click="undoCanvas">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7v6h6"/><path d="M21 17a9 9 0 0 0-9-9 9 9 0 0 0-6 2.3L3 13"/></svg>
                되돌리기
              </button>
              <button class="btn btn-ghost btn-sm" @click="clearCanvas">전체 지우기</button>
            </div>
            <button class="btn-close" @click="drawingOpen = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <canvas
            ref="canvasRef"
            class="drawing-canvas"
            @pointerdown="startDraw"
            @pointermove="draw"
            @pointerup="endDraw"
            @pointerleave="endDraw"
          />
          <div class="drawing-modal__footer">
            <button class="btn btn-secondary" @click="drawingOpen = false">취소</button>
            <button class="btn btn-primary" @click="confirmDrawing">확인</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import AppBreadcrumb from '@/components/common/AppBreadcrumb.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const route = useRoute()
const { success, error } = useToast()
const assignmentId = route.params.id

const assignment = ref({ title: '', subject: '', problemCount: 0, targetCount: 0, dueDate: '', completionRate: 0, status: '' })
const problems = ref([])
const studentProgress = ref([])

// 학생 상세 패널
const selectedStudent = ref(null)
const panelTab = ref('attempts')
const attempts = ref([])
const overallFeedback = ref(null)
const overallComment = ref('')
const overallDrawingUrl = ref(null)

// 드로잉
const drawingOpen = ref(false)
const canvasRef = ref(null)
const penColor = ref('#EF4444')
const penSize = ref(4)
const eraserMode = ref(false)
const eraserSize = ref(18)
let isDrawing = false
let hasDrawn = false
let drawingTarget = null // 'attempt' or 'overall'
let drawingAttempt = null
let ctx = null
let history = []

onMounted(async () => {
  try {
    const [aRes, pRes, probRes] = await Promise.all([
      api.get(`/teacher/assignments/${assignmentId}`),
      api.get(`/teacher/assignments/${assignmentId}/progress`),
      api.get(`/teacher/assignments/${assignmentId}/problems`)
    ])
    const a = aRes.data || {}
    assignment.value = {
      title: a.title, subject: a.subject, problemCount: a.problemCount,
      targetCount: a.targetCount, dueDate: a.dueDate?.slice(0, 10),
      completionRate: a.completionRate || 0, status: a.status?.toLowerCase()
    }
    problems.value = probRes.data || []
    studentProgress.value = (pRes.data || []).map(p => ({
      studentId: p.studentId,
      name: p.studentName, grade: p.grade,
      status: p.completed ? 'done' : 'pending',
      accuracy: p.accuracy ?? null,
      completedAt: p.completedAt?.slice(5, 10)?.replace('-', '/') || '-'
    }))
  } catch {}
})

async function selectStudent(s) {
  selectedStudent.value = s
  panelTab.value = 'attempts'
  attempts.value = []
  overallFeedback.value = null
  overallComment.value = ''

  try {
    const [attRes, fbRes] = await Promise.all([
      api.get(`/teacher/assignments/${assignmentId}/students/${s.studentId}/attempts`),
      api.get(`/teacher/assignments/${assignmentId}/students/${s.studentId}/feedback`)
    ])
    attempts.value = (attRes.data || []).map(a => ({
      ...a,
      isCorrect: a.isCorrect === 1 || a.isCorrect === true,
      newComment: a.feedbackComment || ''
    }))
    overallFeedback.value = fbRes.data || null
    overallComment.value = overallFeedback.value?.comment || ''
    overallDrawingUrl.value = overallFeedback.value?.drawingUrl || null
  } catch {}
}

async function saveAttemptFeedback(a) {
  try {
    await api.post(`/teacher/attempts/${a.attemptId}/feedback`, {
      comment: a.newComment,
      drawingUrl: a.feedbackDrawingUrl || null
    })
    a.feedbackComment = a.newComment
    success('피드백을 저장했습니다.')
  } catch { error('피드백 저장에 실패했습니다.') }
}

async function saveOverallFeedback() {
  try {
    const res = await api.post(`/teacher/assignments/${assignmentId}/students/${selectedStudent.value.studentId}/feedback`, {
      comment: overallComment.value,
      drawingUrl: overallDrawingUrl.value
    })
    overallFeedback.value = res.data || { comment: overallComment.value, drawingUrl: overallDrawingUrl.value }
    success('전체 피드백을 저장했습니다.')
  } catch { error('피드백 저장에 실패했습니다.') }
}

// 드로잉
function openDrawing(a) {
  drawingTarget = 'attempt'
  drawingAttempt = a
  eraserMode.value = false
  drawingOpen.value = true
  nextTick(initCanvas)
}

function openDrawingOverall() {
  drawingTarget = 'overall'
  drawingAttempt = null
  eraserMode.value = false
  drawingOpen.value = true
  nextTick(initCanvas)
}

function saveHistory() {
  if (!ctx) return
  const canvas = canvasRef.value
  history.push(ctx.getImageData(0, 0, canvas.width, canvas.height))
  if (history.length > 30) history.shift() // 최대 30단계
}

function initCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return
  history = []
  // CSS 실제 크기와 내부 버퍼 크기를 일치시켜 좌표 어긋남 방지
  const cssW = canvas.clientWidth
  const cssH = canvas.clientHeight
  if (cssW > 0) canvas.width = cssW
  if (cssH > 0) canvas.height = cssH
  ctx = canvas.getContext('2d')
  ctx.fillStyle = '#FFFFFF'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  hasDrawn = false

  // 기존 드로잉이 있으면 불러오기
  const existingUrl = drawingTarget === 'attempt'
    ? drawingAttempt?.feedbackDrawingUrl
    : overallDrawingUrl.value
  if (existingUrl) {
    const img = new Image()
    img.onload = () => {
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
      saveHistory()
    }
    img.src = existingUrl
    hasDrawn = true
  } else {
    saveHistory()
  }
}

function startDraw(e) {
  isDrawing = true
  hasDrawn = true
  ctx.beginPath()
  ctx.strokeStyle = eraserMode.value ? '#FFFFFF' : penColor.value
  ctx.lineWidth = eraserMode.value ? eraserSize.value : penSize.value
  const rect = canvasRef.value.getBoundingClientRect()
  ctx.moveTo(e.clientX - rect.left, e.clientY - rect.top)
}

function draw(e) {
  if (!isDrawing) return
  const rect = canvasRef.value.getBoundingClientRect()
  ctx.lineTo(e.clientX - rect.left, e.clientY - rect.top)
  ctx.stroke()
}

function endDraw() {
  if (!isDrawing) return
  isDrawing = false
  saveHistory()
}

function undoCanvas() {
  if (history.length <= 1) return
  history.pop()
  ctx.putImageData(history[history.length - 1], 0, 0)
  hasDrawn = history.length > 1
}

function clearCanvas() {
  if (!ctx) return
  saveHistory()
  ctx.fillStyle = '#FFFFFF'
  ctx.fillRect(0, 0, canvasRef.value.width, canvasRef.value.height)
  saveHistory()
  hasDrawn = false
}

function confirmDrawing() {
  if (!hasDrawn) {
    drawingOpen.value = false
    return
  }
  const dataUrl = canvasRef.value.toDataURL('image/png')
  if (drawingTarget === 'attempt' && drawingAttempt) {
    drawingAttempt.feedbackDrawingUrl = dataUrl
  } else if (drawingTarget === 'overall') {
    overallDrawingUrl.value = dataUrl
  }
  drawingOpen.value = false
}

function statusLabel(v) {
  return { active: '진행 중', done: '완료', draft: '초안' }[v] || v
}

function formatDateTime(dt) {
  if (!dt) return ''
  return dt.slice(0, 16).replace('T', ' ')
}
</script>

<style scoped lang="scss">
.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: $spacing-6;
  h1 { font-size: $font-size-2xl; font-weight: 700; margin-bottom: $spacing-2; }
}

.detail-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: $spacing-6;
  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }
  h3 { font-size: $font-size-lg; font-weight: 700; margin-bottom: $spacing-5; }
}

.info-list { display: flex; flex-direction: column; gap: $spacing-4; }
.info-item {
  display: flex; align-items: center; gap: $spacing-3; font-size: $font-size-sm;
  span { color: $text-secondary; width: 80px; flex-shrink: 0; }
  strong { color: $text-primary; }
}

.status-pill {
  padding: 3px 12px; border-radius: $radius-full; font-size: $font-size-xs; font-weight: 600;
  &.active { background: #DBEAFE; color: #1E40AF; }
  &.done { background: #D1FAE5; color: #065F46; }
  &.draft { background: #F3F4F6; color: #6B7280; }
}

.detail-right { display: flex; flex-direction: column; gap: $spacing-6; }
.count-badge { background: $primary-bg; color: $primary; padding: 2px $spacing-3; border-radius: $radius-full; font-size: $font-size-xs; font-weight: 600; margin-left: $spacing-2; }

.problem-list { display: flex; flex-direction: column; gap: $spacing-2; max-height: 280px; overflow-y: auto; }
.problem-item { display: flex; align-items: flex-start; gap: $spacing-3; padding: $spacing-3 $spacing-4; border: 1px solid $border; border-radius: $radius-md; }
.problem-no { width: 24px; height: 24px; border-radius: 50%; background: $primary-bg; color: $primary; display: flex; align-items: center; justify-content: center; font-size: $font-size-xs; font-weight: 700; flex-shrink: 0; margin-top: 2px; }
.problem-info { flex: 1; min-width: 0; }
.problem-meta { display: flex; align-items: center; gap: $spacing-2; margin-bottom: $spacing-1; }
.subject-tag { background: $primary-bg; color: $primary; padding: 2px $spacing-2; border-radius: $radius-sm; font-size: $font-size-xs; font-weight: 600; }
.unit-text { font-size: $font-size-xs; color: $text-muted; }
.problem-text { font-size: $font-size-sm; color: $text-primary; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.empty-text { color: $text-muted; font-size: $font-size-sm; text-align: center; padding: $spacing-6 0; }

// 학생 리스트
.student-list { display: flex; flex-direction: column; gap: $spacing-2; }
.student-row {
  display: flex; align-items: center; gap: $spacing-4; padding: $spacing-3 $spacing-4;
  border: 1.5px solid $border; border-radius: $radius-md; cursor: pointer; transition: all $transition-fast;
  &:hover { border-color: $primary-light; background: $primary-bg; }
  &.active { border-color: $primary; background: $primary-bg; }
  &__info { display: flex; align-items: center; gap: $spacing-3; flex: 1; }
  &__stats { display: flex; align-items: center; gap: $spacing-3; }
}
.avatar-sm { width: 32px; height: 32px; border-radius: 50%; background: $primary-bg; color: $primary; display: flex; align-items: center; justify-content: center; font-size: $font-size-sm; font-weight: 700; flex-shrink: 0; }
.avatar-md { width: 40px; height: 40px; border-radius: 50%; background: $primary-bg; color: $primary; display: flex; align-items: center; justify-content: center; font-size: $font-size-base; font-weight: 700; flex-shrink: 0; }
.student-name { font-size: $font-size-sm; font-weight: 600; color: $text-primary; }
.student-grade { font-size: $font-size-xs; color: $text-muted; }
.accuracy-text { font-size: $font-size-sm; font-weight: 600; color: $text-primary; &.muted { color: $text-muted; } }
.mini-badge {
  padding: 2px 8px; border-radius: $radius-full; font-size: 11px; font-weight: 600;
  &.done { background: #D1FAE5; color: #065F46; }
  &.pending { background: #FEF3C7; color: #92400E; }
}

// 학생 상세 패널 (슬라이드)
.student-detail-panel { position: fixed; inset: 0; z-index: 100; display: flex; justify-content: flex-end; }
.panel-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.4); }
.panel-content {
  position: relative; width: 680px; max-width: 100vw; height: 100vh; background: white;
  display: flex; flex-direction: column; box-shadow: -4px 0 24px rgba(0,0,0,0.15); overflow: hidden;
}
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: $spacing-6; border-bottom: 1px solid $border;
  &__left { display: flex; align-items: center; gap: $spacing-4; }
  h2 { font-size: $font-size-lg; font-weight: 700; }
  p { font-size: $font-size-sm; color: $text-secondary; }
}
.btn-close { background: none; border: none; cursor: pointer; padding: $spacing-2; border-radius: $radius-md; &:hover { background: $bg-light; } }

.panel-tabs { display: flex; border-bottom: 2px solid $border; padding: 0 $spacing-6; overflow-x: auto; overflow-y: hidden; -webkit-overflow-scrolling: touch; }
.panel-tab {
  padding: $spacing-3 $spacing-5; font-size: $font-size-sm; font-weight: 500; color: $text-secondary;
  border-bottom: 2px solid transparent; margin-bottom: -2px; cursor: pointer; transition: all $transition-fast;
  white-space: nowrap; flex-shrink: 0;
  &.active { color: $primary; border-bottom-color: $primary; font-weight: 600; }
  &:hover { color: $primary; }
}

.panel-body { flex: 1; overflow-y: auto; padding: $spacing-6; display: flex; flex-direction: column; gap: $spacing-4; }

// 문제 풀이 카드
.attempt-card {
  border: 1px solid $border; border-radius: $radius-lg; padding: $spacing-5;
  &:has(.result-badge.wrong) { border-left: 4px solid #EF4444; }
  &:has(.result-badge.correct) { border-left: 4px solid #10B981; }
}
.attempt-header { display: flex; align-items: center; gap: $spacing-3; margin-bottom: $spacing-3; }
.attempt-no { width: 24px; height: 24px; border-radius: 50%; background: $bg-light; display: flex; align-items: center; justify-content: center; font-size: $font-size-xs; font-weight: 700; color: $text-secondary; }
.result-badge {
  padding: 2px 10px; border-radius: $radius-full; font-size: $font-size-xs; font-weight: 700;
  &.correct { background: #D1FAE5; color: #065F46; }
  &.wrong { background: #FEE2E2; color: #991B1B; }
}
.attempt-subject { font-size: $font-size-xs; color: $text-muted; }
.attempt-level { font-size: $font-size-xs; color: $text-muted; }
.attempt-question { font-size: $font-size-sm; color: $text-primary; line-height: 1.6; margin-bottom: $spacing-3; }
.attempt-answers { display: flex; gap: $spacing-5; margin-bottom: $spacing-3; }
.answer-item { display: flex; align-items: center; gap: $spacing-2; }
.answer-label { font-size: $font-size-xs; color: $text-muted; }
.answer-value { font-size: $font-size-sm; font-weight: 700; &.correct { color: #10B981; } &.wrong { color: #EF4444; } }

// 피드백 영역
.feedback-area { border-top: 1px solid $border; padding-top: $spacing-4; margin-top: $spacing-2; }
.existing-feedback {
  background: #FFF7ED; border: 1px solid #FED7AA; border-radius: $radius-md; padding: $spacing-4; margin-bottom: $spacing-3;
  &.overall { background: #EFF6FF; border-color: #BFDBFE; }
}
.feedback-label { font-size: $font-size-xs; font-weight: 600; color: $text-secondary; margin-bottom: $spacing-2; }
.feedback-text { font-size: $font-size-sm; color: $text-primary; line-height: 1.6; }
.feedback-drawing { max-width: 100%; border-radius: $radius-md; margin-top: $spacing-2; border: 1px solid $border; }
.feedback-input-area { display: flex; flex-direction: column; gap: $spacing-2; &.overall { margin-top: $spacing-4; } }
.feedback-textarea {
  width: 100%; padding: $spacing-3; border: 1px solid $border; border-radius: $radius-md;
  font-size: $font-size-sm; resize: vertical; font-family: inherit;
  &:focus { outline: none; border-color: $primary; }
}
.feedback-tools { display: flex; align-items: center; gap: $spacing-2; justify-content: flex-end; }
.btn-draw {
  display: flex; align-items: center; gap: $spacing-2; padding: $spacing-2 $spacing-3;
  border: 1px solid $border; border-radius: $radius-md; font-size: $font-size-xs; font-weight: 500;
  cursor: pointer; background: white; color: $text-secondary; transition: all $transition-fast;
  &:hover { border-color: $primary; color: $primary; }
}

// 드로잉 모달
.drawing-modal {
  position: absolute; inset: 0; background: white; z-index: 10; display: flex; flex-direction: column;
  &__header {
    display: flex; align-items: center; gap: $spacing-4; padding: $spacing-4 $spacing-6; border-bottom: 1px solid $border;
    h3 { font-size: $font-size-base; font-weight: 700; }
  }
  &__footer { display: flex; justify-content: flex-end; gap: $spacing-3; padding: $spacing-4 $spacing-6; border-top: 1px solid $border; }
}
.drawing-tools { display: flex; align-items: center; gap: $spacing-3; flex: 1; }
.color-picker { width: 32px; height: 32px; border: none; cursor: pointer; border-radius: $radius-sm; }
.pen-size-select {
  padding: $spacing-1 28px $spacing-1 $spacing-2;
  border: 1px solid $border; border-radius: $radius-sm; font-size: $font-size-xs;
  appearance: none;
  background: white url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='6' viewBox='0 0 10 6'%3E%3Cpath d='M0 0l5 6 5-6z' fill='%236B7280'/%3E%3C/svg%3E") no-repeat right 8px center;
  cursor: pointer;
}
.drawing-canvas {
  flex: 1; border: 1px solid $border; margin: $spacing-4 $spacing-6; border-radius: $radius-md;
  cursor: crosshair; touch-action: none;
}
</style>
