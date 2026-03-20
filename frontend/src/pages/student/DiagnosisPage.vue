<template>
  <div class="diagnosis-page">
    <div class="page-header">
      <h1>진단 테스트</h1>
    </div>

    <!-- 안내 화면 -->
    <div v-if="step === 'intro'" class="intro">
      <div class="intro__icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
      </div>
      <h1>진단 테스트</h1>
      <p>총 <strong>20문제</strong>로 현재 학습 수준을 정확하게 파악합니다.</p>
      <div class="intro-info">
        <div class="info-item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          <span>약 20~30분 소요</span>
        </div>
        <div class="info-item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
          <span>수학, 영어, 국어 영역</span>
        </div>
        <div class="info-item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          <span>A / B / C 레벨 배정</span>
        </div>
      </div>
      <AppButton size="lg" @click="startTest">테스트 시작하기</AppButton>
    </div>

    <!-- 문제 풀기 -->
    <div v-if="step === 'test'" class="test-wrap">
      <!-- 진행률 -->
      <div class="test-progress">
        <div class="test-progress__info">
          <span>{{ currentIdx + 1 }} / {{ questions.length }}</span>
          <span>{{ Math.round(elapsed / 60) }}분 경과</span>
        </div>
        <div class="progress-bar">
          <div class="progress-bar__fill" :style="{ width: ((currentIdx) / questions.length * 100) + '%' }" />
        </div>
      </div>

      <div class="test-question card">
        <div class="question-meta">
          <span class="question-num">문제 {{ currentIdx + 1 }}</span>
        </div>
        <p class="question-text" v-html="currentQ.text" />

        <div class="choices">
          <button
            v-for="choice in currentQ.choices"
            :key="choice.no"
            :class="['choice-btn', { selected: selected === choice.no }]"
            @click="selected = choice.no"
          >
            <span class="choice-num">{{ choice.no }}</span>
            <span>{{ choice.text }}</span>
          </button>
        </div>

        <div class="test-actions">
          <AppButton variant="secondary" :disabled="currentIdx === 0" @click="prev">이전</AppButton>
          <AppButton v-if="currentIdx < questions.length - 1" :disabled="selected === null" @click="next">다음</AppButton>
          <AppButton v-else :disabled="selected === null" @click="submitTest">제출하기</AppButton>
        </div>
      </div>
    </div>

    <!-- 결과 화면 -->
    <div v-if="step === 'result'" class="result">
      <div class="result__card card">
        <div class="result__header">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          <h2>진단 테스트 완료!</h2>
        </div>
        <div class="result__level">
          <p>배정된 레벨</p>
          <AppBadge :type="resultLevel" :text="`${resultLevel} 레벨`" style="font-size: 24px; padding: 8px 20px;" />
        </div>
        <div class="result__stats">
          <div class="result-stat">
            <strong>{{ correctCount }}</strong>
            <span>정답 수</span>
          </div>
          <div class="result-stat">
            <strong>{{ questions.length > 0 ? Math.round(correctCount / questions.length * 100) : 0 }}%</strong>
            <span>정답률</span>
          </div>
          <div class="result-stat">
            <strong>{{ Math.round(elapsed / 60) }}분</strong>
            <span>소요 시간</span>
          </div>
        </div>
        <div class="result__comment">
          <p>{{ levelComments[resultLevel] }}</p>
        </div>
        <div class="result__actions">
          <AppButton @click="$router.push('/student/learn')">학습 시작하기</AppButton>
          <AppButton variant="secondary" @click="$router.push('/student/home')">홈으로</AppButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import AppButton from '@/components/common/AppButton.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import api from '@/utils/api'

const step = ref('intro')
const currentIdx = ref(0)
const selected = ref(null)   // 선택한 optionNo (1~4)
const answers = ref([])      // [{ problemId, optionNo }, ...]
const elapsed = ref(0)
let timer = null

const testId = ref(null)
const questions = ref([])

const currentQ = computed(() => questions.value[currentIdx.value])
const correctCount = ref(0)
const resultLevel = ref('B')

const levelComments = {
  A: '훌륭합니다! 심화 과정으로 최상위권을 목표로 해보세요.',
  B: '좋습니다! 핵심 개념을 다지고 상위권으로 도약해봅시다.',
  C: '기초부터 차근차근 다져봅시다. 꾸준한 학습이 실력을 만듭니다.'
}

async function startTest() {
  try {
    const res = await api.post('/diagnosis/start')
    const data = res.data || {}
    testId.value = data.testId
    questions.value = (data.problems || []).map(q => ({
      id: q.problemId,
      text: q.questionText,
      choices: (q.options || []).map(o => ({ no: o.optionNo, text: o.optionText }))
    }))
  } catch {
    return
  }
  answers.value = new Array(questions.value.length).fill(null)
  step.value = 'test'
  timer = setInterval(() => elapsed.value++, 1000)
}

function next() {
  answers.value[currentIdx.value] = selected.value
  currentIdx.value++
  selected.value = answers.value[currentIdx.value] ?? null
}

function prev() {
  answers.value[currentIdx.value] = selected.value
  currentIdx.value--
  selected.value = answers.value[currentIdx.value] ?? null
}

async function submitTest() {
  answers.value[currentIdx.value] = selected.value
  clearInterval(timer)
  step.value = 'result'
  try {
    // 각 답안 제출
    for (let i = 0; i < questions.value.length; i++) {
      if (answers.value[i] != null) {
        await api.post(`/diagnosis/${testId.value}/submit`, {
          problemId: questions.value[i].id,
          answer: String(answers.value[i])
        })
      }
    }
    // 완료 처리
    const res = await api.post(`/diagnosis/${testId.value}/complete`)
    const result = res.data || {}
    correctCount.value = result.correctCount || 0
    resultLevel.value = result.determinedLevel || 'B'
  } catch {}
}

onUnmounted(() => clearInterval(timer))
</script>

<style scoped lang="scss">
.diagnosis-page {
}


.intro {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: $spacing-12 0;
  gap: $spacing-6;

  &__icon { opacity: 0.9; }

  h1 {
    font-size: $font-size-3xl;
    font-weight: 700;
    color: $text-primary;
  }

  p {
    font-size: $font-size-lg;
    color: $text-secondary;

    strong { color: $primary; }
  }
}

.intro-info {
  display: flex;
  gap: $spacing-8;
  flex-wrap: wrap;
  justify-content: center;

  .info-item {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.test-progress {
  margin-bottom: $spacing-6;

  &__info {
    display: flex;
    justify-content: space-between;
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-2;
  }
}

.test-question {
  .question-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-5;

    .question-num {
      font-size: $font-size-sm;
      color: $text-muted;
    }
  }

  .question-text {
    font-size: $font-size-lg;
    font-weight: 500;
    color: $text-primary;
    line-height: 1.7;
    margin-bottom: $spacing-8;
  }
}

.choices {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  margin-bottom: $spacing-8;
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

  &:hover {
    border-color: $primary-light;
    background: $primary-bg;
  }

  &.selected {
    border-color: $primary-light;
    background: $primary-bg;
    color: $primary;
    font-weight: 600;
  }

  .choice-num {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    border: 2px solid currentColor;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-sm;
    font-weight: 700;
    flex-shrink: 0;
  }
}

.test-actions {
  display: flex;
  justify-content: space-between;
}

.result {
  display: flex;
  justify-content: center;
  padding: $spacing-8 0;

  &__card {
    max-width: 600px;
    width: 100%;
    text-align: center;
  }

  &__header {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-4;
    margin-bottom: $spacing-8;

    h2 {
      font-size: $font-size-2xl;
      font-weight: 700;
    }
  }

  &__level {
    margin-bottom: $spacing-8;

    p {
      font-size: $font-size-sm;
      color: $text-secondary;
      margin-bottom: $spacing-3;
    }
  }

  &__stats {
    display: flex;
    justify-content: center;
    gap: $spacing-8;
    margin-bottom: $spacing-6;
    padding: $spacing-5;
    background: $bg-light;
    border-radius: $radius-md;
  }

  &__comment {
    padding: $spacing-4;
    background: $primary-bg;
    border-radius: $radius-md;
    margin-bottom: $spacing-6;
    font-size: $font-size-sm;
    color: $primary;
    line-height: 1.7;
  }

  &__actions {
    display: flex;
    gap: $spacing-3;
    justify-content: center;
  }
}

.result-stat {
  display: flex;
  flex-direction: column;
  align-items: center;

  strong {
    font-size: $font-size-2xl;
    font-weight: 700;
    color: $primary;
  }

  span {
    font-size: $font-size-xs;
    color: $text-secondary;
    margin-top: 2px;
  }
}

.subject-tag {
  display: inline-block;
  background: $primary-bg;
  color: $primary;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 600;
}
</style>
