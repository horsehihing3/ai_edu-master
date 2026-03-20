<template>
  <div class="feedback-page">
    <div class="page-header">
      <RouterLink to="/student/learn" class="back-link">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        과제 목록
      </RouterLink>
      <h1>{{ assignment.title || '과제 피드백' }}</h1>
      <p v-if="assignment.subject">{{ assignment.subject }} · {{ assignment.problemCount }}문제</p>
    </div>

    <div v-if="loading" class="loading-wrap"><AppLoading /></div>

    <template v-else>
      <!-- 전체 피드백 -->
      <div v-if="overallFeedback" class="card overall-feedback-card">
        <h3>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          선생님 전체 피드백
        </h3>
        <p v-if="overallFeedback.comment" class="overall-comment">{{ overallFeedback.comment }}</p>
        <img v-if="overallFeedback.drawingUrl" :src="overallFeedback.drawingUrl" class="feedback-drawing" alt="피드백" />
        <p v-if="!overallFeedback.comment && !overallFeedback.drawingUrl" class="empty-text">작성된 전체 피드백이 없습니다.</p>
      </div>

      <!-- 결과 요약 -->
      <div class="result-summary">
        <div class="summary-item correct">
          <span class="summary-value">{{ correctCount }}</span>
          <span class="summary-label">정답</span>
        </div>
        <div class="summary-item wrong">
          <span class="summary-value">{{ wrongCount }}</span>
          <span class="summary-label">오답</span>
        </div>
        <div class="summary-item accuracy">
          <span class="summary-value">{{ accuracyPct }}%</span>
          <span class="summary-label">정답률</span>
        </div>
      </div>

      <!-- 문제별 결과 -->
      <div class="problem-results">
        <div v-for="(a, idx) in attempts" :key="a.attemptId" :class="['problem-card', a.isCorrect ? 'correct' : 'wrong']">
          <div class="problem-card__header">
            <span class="problem-no">{{ idx + 1 }}</span>
            <span :class="['result-badge', a.isCorrect ? 'correct' : 'wrong']">
              {{ a.isCorrect ? '정답' : '오답' }}
            </span>
            <span v-if="a.subject" class="subject-tag">{{ a.subject }}</span>
            <span v-if="a.level" class="level-tag">Lv.{{ a.level }}</span>
          </div>

          <p class="question-text">{{ a.questionText }}</p>

          <div class="answer-row">
            <div class="answer-item">
              <span class="answer-label">제출 답안</span>
              <span :class="['answer-value', a.isCorrect ? '' : 'wrong']">{{ a.submittedAnswer }}번</span>
            </div>
            <div class="answer-item">
              <span class="answer-label">정답</span>
              <span class="answer-value correct">{{ a.correctAnswer }}번</span>
            </div>
          </div>

          <div v-if="a.explanation" class="explanation">
            <span class="explanation-label">풀이</span>
            <p>{{ a.explanation }}</p>
          </div>

          <!-- 선생님 피드백 (오답인 경우) -->
          <div v-if="!a.isCorrect && (a.feedbackComment || a.feedbackDrawingUrl)" class="teacher-feedback">
            <div class="teacher-feedback__label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
              선생님 피드백
            </div>
            <p v-if="a.feedbackComment" class="teacher-feedback__text">{{ a.feedbackComment }}</p>
            <img v-if="a.feedbackDrawingUrl" :src="a.feedbackDrawingUrl" class="feedback-drawing" alt="피드백" />
          </div>

          <div v-if="!a.isCorrect && !a.feedbackComment && !a.feedbackDrawingUrl" class="no-feedback">
            아직 피드백이 작성되지 않았습니다.
          </div>
        </div>
      </div>

      <div v-if="!attempts.length" class="empty-wrap">
        <AppEmpty message="풀이 기록이 없습니다." />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import api from '@/utils/api'

const route = useRoute()
const assignmentId = route.params.id

const loading = ref(true)
const assignment = ref({})
const attempts = ref([])
const overallFeedback = ref(null)

const correctCount = computed(() => attempts.value.filter(a => a.isCorrect).length)
const wrongCount = computed(() => attempts.value.filter(a => !a.isCorrect).length)
const accuracyPct = computed(() => {
  const total = attempts.value.length
  return total > 0 ? Math.round(correctCount.value * 100 / total) : 0
})

onMounted(async () => {
  try {
    const res = await api.get(`/student/assignments/${assignmentId}/feedback`)
    const data = res.data || {}
    overallFeedback.value = data.overallFeedback || null
    attempts.value = (data.attemptDetails || []).map(a => ({
      ...a,
      isCorrect: a.isCorrect === 1 || a.isCorrect === true
    }))

    // Fetch assignment info
    try {
      const aRes = await api.get('/student/assignments')
      const list = aRes.data?.content || aRes.data || []
      const found = list.find(a => String(a.assignmentId) === String(assignmentId))
      if (found) {
        assignment.value = {
          title: found.title,
          subject: found.subject,
          problemCount: found.problemCount || found.totalCount || 0
        }
      }
    } catch {}
  } catch {} finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: $spacing-6;
  h1 { font-size: $font-size-2xl; font-weight: 700; margin: $spacing-2 0 $spacing-1; }
  p { font-size: $font-size-sm; color: $text-secondary; }
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: $spacing-1;
  font-size: $font-size-sm;
  color: $text-secondary;
  text-decoration: none;
  &:hover { color: $primary; }
}

.loading-wrap { display: flex; justify-content: center; padding: $spacing-12; }

// 전체 피드백
.overall-feedback-card {
  margin-bottom: $spacing-6;
  border-left: 4px solid $primary;
  h3 {
    display: flex; align-items: center; gap: $spacing-2;
    font-size: $font-size-base; font-weight: 700; color: $primary; margin-bottom: $spacing-4;
  }
}
.overall-comment { font-size: $font-size-sm; color: $text-primary; line-height: 1.7; white-space: pre-wrap; }

// 결과 요약
.result-summary {
  display: flex; gap: $spacing-4; margin-bottom: $spacing-6;
}
.summary-item {
  flex: 1; text-align: center; padding: $spacing-5; border-radius: $radius-lg; background: $bg-light;
  &.correct { background: #ECFDF5; }
  &.wrong { background: #FEF2F2; }
  &.accuracy { background: #EFF6FF; }
}
.summary-value {
  display: block; font-size: $font-size-2xl; font-weight: 800; margin-bottom: $spacing-1;
  .correct & { color: #059669; }
  .wrong & { color: #DC2626; }
  .accuracy & { color: #2563EB; }
}
.summary-label { font-size: $font-size-xs; color: $text-secondary; font-weight: 500; }

// 문제별 카드
.problem-results { display: flex; flex-direction: column; gap: $spacing-4; }
.problem-card {
  background: $bg-white; border: 1px solid $border; border-radius: $radius-lg; padding: $spacing-5;
  &.correct { border-left: 4px solid #10B981; }
  &.wrong { border-left: 4px solid #EF4444; }

  &__header {
    display: flex; align-items: center; gap: $spacing-3; margin-bottom: $spacing-3;
  }
}

.problem-no {
  width: 26px; height: 26px; border-radius: 50%; background: $bg-light;
  display: flex; align-items: center; justify-content: center;
  font-size: $font-size-xs; font-weight: 700; color: $text-secondary;
}

.result-badge {
  padding: 2px 10px; border-radius: $radius-full; font-size: $font-size-xs; font-weight: 700;
  &.correct { background: #D1FAE5; color: #065F46; }
  &.wrong { background: #FEE2E2; color: #991B1B; }
}

.subject-tag { font-size: $font-size-xs; color: $text-muted; background: $primary-bg; padding: 2px 8px; border-radius: $radius-sm; }
.level-tag { font-size: $font-size-xs; color: $text-muted; }

.question-text { font-size: $font-size-sm; color: $text-primary; line-height: 1.7; margin-bottom: $spacing-4; }

.answer-row { display: flex; gap: $spacing-6; margin-bottom: $spacing-3; }
.answer-item { display: flex; align-items: center; gap: $spacing-2; }
.answer-label { font-size: $font-size-xs; color: $text-muted; }
.answer-value {
  font-size: $font-size-sm; font-weight: 700;
  &.correct { color: #10B981; }
  &.wrong { color: #EF4444; }
}

.explanation {
  background: $bg-light; border-radius: $radius-md; padding: $spacing-3 $spacing-4; margin-bottom: $spacing-3;
  p { font-size: $font-size-sm; color: $text-secondary; line-height: 1.6; }
}
.explanation-label { font-size: $font-size-xs; font-weight: 600; color: $text-muted; display: block; margin-bottom: $spacing-1; }

// 선생님 피드백
.teacher-feedback {
  background: #FFF7ED; border: 1px solid #FED7AA; border-radius: $radius-md; padding: $spacing-4; margin-top: $spacing-3;
  &__label {
    display: flex; align-items: center; gap: $spacing-2;
    font-size: $font-size-xs; font-weight: 700; color: #C2410C; margin-bottom: $spacing-2;
  }
  &__text { font-size: $font-size-sm; color: $text-primary; line-height: 1.7; white-space: pre-wrap; }
}

.no-feedback {
  font-size: $font-size-xs; color: $text-muted; font-style: italic; margin-top: $spacing-3;
  padding: $spacing-3; background: $bg-light; border-radius: $radius-md; text-align: center;
}

.feedback-drawing { max-width: 100%; border-radius: $radius-md; margin-top: $spacing-2; border: 1px solid $border; }
.empty-text { color: $text-muted; font-size: $font-size-sm; }
.empty-wrap { padding: $spacing-8 0; }
</style>
