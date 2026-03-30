<template>
  <div class="problem-solve">
    <div class="solve-header">
      <button class="btn btn-ghost btn-sm" @click="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        목록으로
      </button>
      <div class="solve-progress">
        <span>{{ currentIdx + 1 }} / {{ problems.length }}</span>
        <div class="progress-bar" style="width: 200px;">
          <div class="progress-bar__fill" :style="{ width: ((currentIdx + 1) / problems.length * 100) + '%' }" />
        </div>
      </div>
      <div class="solve-timer">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        {{ formatTime(elapsed) }}
      </div>
    </div>

    <div v-if="!currentP" class="loading-wrap" style="padding: 80px; text-align:center; color: #9CA3AF;">
      문제를 불러오는 중...
    </div>

    <div v-else class="solve-body">
      <!-- 문제 본문 -->
      <div class="question-card card">
        <div class="question-card__meta">
          <span class="subject-tag">{{ currentP.subject }}</span>
          <AppBadge :type="currentP.level" />
          <span class="unit-info">{{ currentP.unit }}</span>
        </div>

        <div class="question-text">
          <!-- [2026-03-21] KaTeX 수식 렌더링 적용 -->
          <p v-if="currentP.questionText"><MathText :text="currentP.questionText" /></p>
          <img v-if="currentP.imageUrl" :src="currentP.imageUrl" class="question-img" alt="문제 이미지" />
          <!-- [2026-03-27] 보기(passage) 표시 -->
          <div v-if="currentP.passage" class="passage-box">
            <MathText :text="currentP.passage" />
          </div>
        </div>

        <!-- 선택지 (객관식) -->
        <div v-if="!submitted && currentP.problemType !== 'SHORT_ANSWER'" class="choices">
          <button
            v-for="(choice, i) in currentP.choices"
            :key="i"
            :class="['choice-btn', { selected: selected === i }]"
            @click="selected = i"
          >
            <span class="choice-num">{{ i + 1 }}</span>
            <!-- [2026-03-21] KaTeX 수식 렌더링 적용 -->
            <MathText :text="choice" />
          </button>
        </div>

        <!-- 입력창 (단답형) -->
        <div v-if="!submitted && currentP.problemType === 'SHORT_ANSWER'" class="short-answer-wrap">
          <input
            v-model="userAnswer"
            type="text"
            class="short-answer-input"
            placeholder="답을 입력하세요"
            @keyup.enter="userAnswer.trim() && !submitted ? submit() : null"
          />
        </div>

        <!-- 정답/오답 결과 -->
        <div v-if="submitted" class="answer-result">
          <!-- 제출 후: 객관식 선택지 결과 -->
          <div v-if="currentP.problemType !== 'SHORT_ANSWER'" class="choices">
            <button
              v-for="(choice, i) in currentP.choices"
              :key="i"
              :class="[
                'choice-btn',
                {
                  correct: i === currentP.answer,
                  wrong: selected === i && i !== currentP.answer,
                  selected: selected === i
                }
              ]"
              disabled
            >
              <span class="choice-num">{{ i + 1 }}</span>
              <!-- [2026-03-21] KaTeX 수식 렌더링 적용 -->
              <MathText :text="choice" />
              <svg v-if="i === currentP.answer" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5" class="ml-auto"><polyline points="20 6 9 17 4 12"/></svg>
              <svg v-if="selected === i && i !== currentP.answer" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2.5" class="ml-auto"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <!-- 제출 후: 단답형 결과 -->
          <div v-if="currentP.problemType === 'SHORT_ANSWER'" class="short-answer-result">
            <p>내 답: <strong>{{ userAnswer }}</strong></p>
            <p>정답: <strong>{{ currentP.answer }}</strong></p>
          </div>

          <!-- 정오 배너 -->
          <div :class="['result-banner', isCorrect ? 'correct' : 'wrong']">
            <span v-if="isCorrect">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              정답입니다!
            </span>
            <span v-else>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              <!-- 객관식: "X번" / 단답형: 정답 문자열 그대로 -->
              오답입니다. 정답:
              {{ currentP.problemType === 'SHORT_ANSWER' ? currentP.answer : (currentP.answer + 1) + '번' }}
            </span>
          </div>

          <!-- 해설 -->
          <div v-if="showExplanation" class="explanation">
            <h4>해설</h4>
            <!-- [2026-03-21] KaTeX 수식 렌더링 적용 -->
            <p><MathText :text="currentP.explanation" /></p>
          </div>

          <!-- 액션 버튼 -->
          <!-- [2026-03-20] 북마크 버튼 제거 — 오답 시 wrong_notes 자동저장으로 대체됨 -->
          <div class="answer-actions">
            <!-- [2026-03-21] SINGLE 세션(오답노트 재도전) + 정답 시 복귀 버튼 노출 -->
            <button v-if="fromWrongNotes && isCorrect" class="return-wrong-note-btn" @click="returnToWrongNotes">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 14 4 9 9 4"/><path d="M20 20v-7a4 4 0 0 0-4-4H4"/></svg>
              오답노트 복귀
            </button>
          </div>

          <!-- [2026-03-30] 오답 시 연결 영상 표시 -->
          <div v-if="isWrong" class="video-section">
            <div v-if="relatedVideo" class="video-btn-wrap">
              <button class="btn-watch-video" @click="showVideoModal = true">
                ▶ 풀이 영상 보기
              </button>
              <span class="video-title">{{ relatedVideo.title }}</span>
            </div>
            <div v-else-if="relatedVideo !== undefined" class="no-video">
              등록된 풀이 영상이 없습니다
            </div>
          </div>
        </div>

        <!-- 제출/다음 버튼 -->
        <div class="solve-footer">
          <AppButton v-if="!submitted"
            :disabled="currentP.problemType === 'SHORT_ANSWER' ? !userAnswer.trim() : selected === null"
            @click="submit">
            정답 제출
          </AppButton>
          <div v-else class="next-actions">
            <AppButton variant="secondary" @click="showExplanation = !showExplanation">
              {{ showExplanation ? '해설 닫기' : '해설 보기' }}
            </AppButton>
            <AppButton v-if="currentIdx < problems.length - 1" @click="nextProblem">
              다음 문제
            </AppButton>
            <AppButton v-else @click="finishSession">
              학습 완료
            </AppButton>
          </div>
        </div>
      </div>

      <!-- 사이드 패널 (문제 목록) -->
      <div class="solve-sidebar">
        <div class="card">
          <h4 class="sidebar-title">문제 목록</h4>
          <div class="problem-dots">
            <button
              v-for="(p, i) in problems"
              :key="i"
              :class="['dot', {
                active: i === currentIdx,
                correct: results[i] === true,
                wrong: results[i] === false
              }]"
              @click="jumpTo(i)"
            >{{ i + 1 }}</button>
          </div>
        </div>
      </div>
    </div>
  <!-- [2026-03-30] 풀이 영상 모달 -->
  <div v-if="showVideoModal" class="video-modal-overlay" @click.self="showVideoModal = false">
    <div class="video-modal">
      <div class="video-modal-header">
        <span>{{ relatedVideo?.title }}</span>
        <button @click="showVideoModal = false">✕</button>
      </div>
      <div class="video-modal-body">
        <template v-if="relatedVideo">
          <!-- YOUTUBE 또는 VIMEO 타입 -->
          <iframe
            v-if="relatedVideo.videoType === 'YOUTUBE' || relatedVideo.videoType === 'VIMEO' || relatedVideo.externalUrl"
            :src="relatedVideo.videoUrl || relatedVideo.externalUrl"
            style="width:100%; height:360px; border:none"
            allowfullscreen
          ></iframe>
          <!-- 직접 업로드 파일 -->
          <video
            v-else
            :src="relatedVideo.cdnUrl || relatedVideo.videoUrl"
            controls
            style="width:100%"
          ></video>
        </template>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppBadge from '@/components/common/AppBadge.vue'
import AppButton from '@/components/common/AppButton.vue'
import MathText from '@/components/common/MathText.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const { success, error } = useToast()

// [2026-03-21] 오답노트 재도전 세션 여부
const fromWrongNotes = computed(() => route.query.from === 'wrong-notes')

// [2026-03-30] sessionId ref — 임시저장/이어풀기에서 공통 참조
const sessionId = computed(() => route.params.sessionId || route.params.id)
const isCompleted = ref(false)

const currentIdx = ref(0)
const selected = ref(null)        // 객관식 선택 인덱스
const userAnswer = ref('')        // 단답형 입력값
const submitted = ref(false)
const showExplanation = ref(false)
// [2026-03-20] 북마크 버튼 제거 — 오답 시 wrong_notes 자동저장으로 대체됨
const results = ref([])
const elapsed = ref(0)
// [2026-03-30] 오답 시 연결 영상 연동
const relatedVideo = ref(undefined)   // undefined: 미조회, null: 없음, object: 있음
const showVideoModal = ref(false)
let timer = null
// [2026-03-21] 문제별 소요 시간 측정
let problemStartTime = Date.now()

const problems = ref([])

const currentP = computed(() => problems.value[currentIdx.value])


const isCorrect = computed(() => {
  if (!currentP.value) return false
  if (currentP.value.problemType === 'SHORT_ANSWER') {
    // 단답형: 입력 문자열과 정답 문자열 비교 (answer는 텍스트)
    return userAnswer.value.trim() === String(currentP.value.answer).trim()
  }
  // 객관식: 선택 인덱스와 정답 인덱스 비교
  return selected.value === currentP.value.answer
})
// [2026-03-30] 오답 여부 — 제출 후 오답일 때 true
const isWrong = computed(() => submitted.value && !isCorrect.value)

// [2026-03-30] 오답 시 연결 영상 조회
async function fetchRelatedVideo(problemId) {
  try {
    const res = await api.get(`/videos/by-problem/${problemId}`)
    relatedVideo.value = res.data ?? null
  } catch {
    relatedVideo.value = null
  }
}

async function submit() {
  submitted.value = true
  results.value[currentIdx.value] = isCorrect.value
  // [2026-03-20] 정답/오답 즉시 피드백 토스트 추가
  if (isCorrect.value) {
    success('✅ 정답입니다!')
  } else {
    error('❌ 오답입니다. 해설을 확인해보세요.')
    showExplanation.value = true
    // [2026-03-30] 오답 시 연결 영상 비동기 조회
    fetchRelatedVideo(currentP.value.id)
  }
  try {
    // 단답형: 입력 텍스트 그대로 전송 / 객관식: 선택 번호(1-based) 전송
    const submittedAnswer = currentP.value.problemType === 'SHORT_ANSWER'
      ? userAnswer.value.trim()
      : String(selected.value + 1)
    // [2026-03-21] 문제별 소요 시간 계산 후 전달
    const timeSpentSec = Math.round((Date.now() - problemStartTime) / 1000)
    await api.post(`/student/sessions/${sessionId.value}/submit`, {
      problemId: currentP.value.id,
      submittedAnswer,
      timeSpentSec
    })
  } catch {}
}

// [2026-03-30] 임시저장 — currentIndex를 서버에 저장
async function saveProgress() {
  if (!sessionId.value) return
  try {
    await api.put(`/student/sessions/${sessionId.value}/save`, {
      currentIndex: currentIdx.value
    })
  } catch {}
}

function nextProblem() {
  currentIdx.value++
  selected.value = null
  userAnswer.value = ''
  submitted.value = false
  showExplanation.value = false
  // [2026-03-20] 북마크 버튼 제거 — 오답 시 wrong_notes 자동저장으로 대체됨
  problemStartTime = Date.now() // [2026-03-21] 문제 전환 시 타이머 리셋
  // [2026-03-30] 다음 문제로 넘어갈 때 영상 상태 초기화 + 임시저장
  relatedVideo.value = undefined
  showVideoModal.value = false
  saveProgress()
}

function jumpTo(i) {
  currentIdx.value = i
  selected.value = null
  submitted.value = results.value[i] !== undefined
  problemStartTime = Date.now() // [2026-03-21] 문제 전환 시 타이머 리셋
}

// [2026-03-20] 북마크 버튼 제거 — 오답 시 wrong_notes 자동저장으로 대체됨

async function finishSession() {
  const correct = results.value.filter(r => r === true).length
  isCompleted.value = true  // [2026-03-30] 완료 플래그 — onBeforeUnmount 임시저장 방지
  try {
    await api.post(`/student/sessions/${sessionId.value}/complete`, {
      results: results.value, elapsedSeconds: elapsed.value
    })
  } catch {}
  success(`학습 완료! ${correct}/${problems.value.length} 정답`)
  // [2026-03-21] 오답노트 세션이면 오답노트로, 아니면 학습 목록으로
  router.push(fromWrongNotes.value ? '/student/wrong-notes' : '/student/learn')
}

// [2026-03-21] 오답노트 복귀 — 세션 완료 후 오답노트 페이지로 이동
async function returnToWrongNotes() {
  isCompleted.value = true
  try {
    await api.post(`/student/sessions/${sessionId.value}/complete`, {})
  } catch {}
  router.push('/student/wrong-notes')
}

function formatTime(sec) {
  const m = Math.floor(sec / 60).toString().padStart(2, '0')
  const s = (sec % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

onMounted(async () => {
  timer = setInterval(() => elapsed.value++, 1000)
  // [2026-03-30] 이어풀기: 라우터에서 currentIndex 전달받으면 복원
  if (route.query.currentIndex !== undefined) {
    currentIdx.value = Number(route.query.currentIndex)
  }
  try {
    const res = await api.get(`/student/sessions/${sessionId.value}/problems`)
    problems.value = (res.data || []).map(p => ({
      id: p.problemId, subject: p.subject, level: p.level,
      unit: p.unitName || p.unit, questionText: p.questionText,
      imageUrl: p.imageUrl || null,
      passage: p.passage || '',
      problemType: p.problemType || 'MULTIPLE_CHOICE',
      choices: p.options?.map(o => o.content) || [],
      // 객관식: 정답 선택지 인덱스(0-based) / 단답형: 정답 문자열
      // 같은 필드명 `answer`를 사용하나 타입이 다름 — problemType으로 구분
      answer: p.problemType === 'SHORT_ANSWER'
        ? (p.correctAnswer || '')
        : (p.options?.findIndex(o => o.isCorrect) ?? 0),
      explanation: p.explanation || ''
    }))
  } catch {}
})

onUnmounted(() => clearInterval(timer))

// [2026-03-30] 페이지 이탈 시 임시저장 (완료된 세션은 제외)
onBeforeUnmount(async () => {
  if (sessionId.value && !isCompleted.value) {
    await saveProgress()
  }
})
</script>

<style scoped lang="scss">
.problem-solve {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
  max-width: 1100px;
  margin: 0 auto;
}

.solve-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: $bg-white;
  padding: $spacing-4 $spacing-6;
  border-radius: $radius-lg;
  border: 1px solid $border;
  box-shadow: $shadow-sm;
}

.solve-progress {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
}

.solve-timer {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-secondary;
}

.solve-body {
  display: grid;
  grid-template-columns: 1fr 220px;
  gap: $spacing-5;
  align-items: start;

  @media (max-width: $bp-tablet) {
    grid-template-columns: 1fr;
  }
}

.question-card {
  .question-card__meta {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    margin-bottom: $spacing-5;

    .unit-info {
      font-size: $font-size-xs;
      color: $text-muted;
      margin-left: auto;
    }
  }

  .question-text {
    margin-bottom: $spacing-6;

    p {
      font-size: $font-size-lg;
      color: $text-primary;
      line-height: 1.7;
      margin-bottom: $spacing-4;
    }

.question-img {
      max-width: 100%;
      border-radius: $radius-md;
      margin: $spacing-4 0;
    }

    /* [2026-03-27] 보기 박스 스타일 */
    .passage-box {
      border: 1.5px solid #e5e7eb;
      border-radius: $radius-md;
      padding: $spacing-4 $spacing-5;
      margin: $spacing-3 0 $spacing-4;
      background: #f9fafb;
      font-size: 0.95em;
      line-height: 1.7;
      white-space: pre-line;
    }
  }
}

.choices {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
}

.choice-btn {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  padding: $spacing-4 $spacing-5;
  border: 2px solid $border;
  border-radius: $radius-md;
  background: $bg-white;
  font-size: $font-size-base;
  color: $text-primary;
  text-align: left;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    border-color: $primary-light;
    background: $primary-bg;
  }

  &.selected { border-color: $primary-light; background: $primary-bg; }
  &.correct { border-color: $success; background: #ECFDF5 !important; }
  &.wrong { border-color: $danger; background: #FEF2F2 !important; }
  &:disabled { cursor: default; }

  .choice-num {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    border: 2px solid $border;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-sm;
    font-weight: 700;
    flex-shrink: 0;
  }
}

.result-banner {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-4 $spacing-5;
  border-radius: $radius-md;
  font-weight: 700;
  font-size: $font-size-base;
  margin-bottom: $spacing-4;

  span {
    display: flex;
    align-items: center;
    gap: $spacing-2;
  }

  &.correct { background: #ECFDF5; color: #065F46; }
  &.wrong { background: #FEF2F2; color: #991B1B; }
}

.explanation {
  background: $bg-light;
  border-radius: $radius-md;
  padding: $spacing-5;
  margin-bottom: $spacing-4;
  border-left: 4px solid $primary-light;

  h4 {
    font-size: $font-size-sm;
    font-weight: 700;
    color: $primary;
    margin-bottom: $spacing-2;
  }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: 1.7;
  }
}

.answer-actions {
  display: flex;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
}

// [2026-03-20] 북마크 버튼 제거 — 오답 시 wrong_notes 자동저장으로 대체됨
// [2026-03-21] 오답노트 복귀 버튼
.return-wrong-note-btn {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-4;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  font-weight: 600;
  cursor: pointer;
  transition: all $transition-fast;
  border: 1.5px solid $success;
  background: #ECFDF5;
  color: #065F46;

  &:hover { background: #D1FAE5; }
}

.video-btn {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-4;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
  cursor: pointer;
  transition: all $transition-fast;
  border: 1.5px solid $border;
  background: $bg-white;
  color: $text-secondary;

  &:hover { border-color: $primary-light; color: $primary-light; }
}

.solve-footer {
  border-top: 1px solid $border;
  padding-top: $spacing-5;
  display: flex;
  justify-content: flex-end;
}

.next-actions {
  display: flex;
  gap: $spacing-3;
}

.sidebar-title {
  font-size: $font-size-sm;
  font-weight: 700;
  color: $text-secondary;
  margin-bottom: $spacing-4;
}

.problem-dots {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-2;
}

.dot {
  width: 32px;
  height: 32px;
  border-radius: $radius-sm;
  border: 1.5px solid $border;
  background: $bg-light;
  font-size: $font-size-xs;
  font-weight: 600;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;

  &.active { border-color: $primary-light; background: $primary-bg; color: $primary; }
  &.correct { border-color: $success; background: #ECFDF5; color: #065F46; }
  &.wrong { border-color: $danger; background: #FEF2F2; color: #991B1B; }
}

.short-answer-wrap {
  margin-bottom: $spacing-5;
}

.short-answer-input {
  width: 100%;
  padding: $spacing-4 $spacing-5;
  border: 2px solid $border;
  border-radius: $radius-md;
  font-size: $font-size-base;
  color: $text-primary;
  box-sizing: border-box;

  &:focus { outline: none; border-color: $primary-light; }
}

.short-answer-result {
  margin-bottom: $spacing-4;

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-2;
  }

  strong { color: $text-primary; font-weight: 700; }
}

.subject-tag {
  background: $primary-bg;
  color: $primary;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 600;
}

.ml-auto { margin-left: auto; }

// [2026-03-30] 오답 시 연결 영상 스타일
.video-section {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}
.video-btn-wrap {
  display: flex;
  align-items: center;
}
.btn-watch-video {
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  &:hover { background: #4f46e5; }
}
.video-title {
  font-size: 13px;
  color: #555;
  margin-left: 10px;
}
.no-video {
  font-size: 13px;
  color: #999;
}
.video-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.video-modal {
  background: #fff;
  border-radius: 12px;
  width: 680px;
  max-width: 95vw;
  overflow: hidden;
}
.video-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #eee;
  font-weight: 500;
  button {
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: #888;
  }
}
.video-modal-body {
  padding: 16px;
}
</style>
