<template>
  <div class="student-home">
    <!-- 환영 배너 -->
    <div class="welcome-banner">
      <div class="welcome-banner__content">
        <div class="welcome-banner__text">
          <h1>안녕하세요, <strong>{{ user?.name }}</strong>님! 👋</h1>
          <p>오늘도 학습해볼까요? 꾸준한 학습이 실력을 만듭니다.</p>
        </div>
        <AppBadge :type="user?.level || 'C'" :text="`${user?.level || 'C'} 레벨`" />
      </div>
      <div class="welcome-banner__date">
        {{ today }}
      </div>
    </div>

    <!-- 학습 현황 카드 -->
    <div class="stats-grid">
      <div class="stat-card" style="border-top: 4px solid #3B82F6; background: #DBEAFE;">
        <div class="stat-card__icon" style="background: #DBEAFE; color: #1E40AF;">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
        </div>
        <div>
          <p class="stat-card__label">현재 레벨</p>
          <p class="stat-card__value">{{ user?.level || 'C' }} 레벨</p>
        </div>
      </div>
      <div class="stat-card" style="border-top: 4px solid #10B981; background: #D1FAE5;">
        <div class="stat-card__icon" style="background: #D1FAE5; color: #065F46;">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
        </div>
        <div>
          <p class="stat-card__label">총 풀이 수</p>
          <p class="stat-card__value">{{ stats.totalSolved.toLocaleString() }}문제</p>
        </div>
      </div>
      <div class="stat-card" style="border-top: 4px solid #F59E0B; background: #FEF3C7;">
        <div class="stat-card__icon" style="background: #FEF3C7; color: #92400E;">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </div>
        <div>
          <p class="stat-card__label">정답률</p>
          <p class="stat-card__value">{{ stats.accuracy }}%</p>
        </div>
      </div>
      <div class="stat-card" style="border-top: 4px solid #8B5CF6; background: #EDE9FE;">
        <div class="stat-card__icon" style="background: #EDE9FE; color: #5B21B6;">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
        </div>
        <div>
          <p class="stat-card__label">연속 학습</p>
          <p class="stat-card__value">{{ stats.streak }}일 연속</p>
        </div>
      </div>
    </div>

    <div class="home-grid">
      <!-- 진행 중인 과제 -->
      <div class="card">
        <div class="card__header">
          <h3>진행 중인 과제</h3>
          <RouterLink to="/student/learn" class="btn btn-ghost btn-sm">전체보기</RouterLink>
        </div>
        <div v-if="assignments.length" class="assignment-list">
          <div v-for="a in assignments.slice(0, 3)" :key="a.id" class="assignment-item">
            <div class="assignment-item__info">
              <span class="subject-tag">{{ a.subject }}</span>
              <h4>{{ a.title }}</h4>
              <p class="due">마감: {{ formatDate(a.dueDate) }}</p>
            </div>
            <div class="assignment-item__progress">
              <div class="progress-bar">
                <div class="progress-bar__fill" :style="{ width: a.progress + '%' }" />
              </div>
              <span class="progress-text">{{ a.progress }}%</span>
            </div>
            <RouterLink :to="`/student/learn/${a.sessionId}`" class="btn btn-primary btn-sm">풀기</RouterLink>
          </div>
        </div>
        <AppEmpty v-else message="진행 중인 과제가 없습니다." />
      </div>

      <!-- 오늘의 통계 -->
      <div class="today-stats">
        <div class="card">
          <div class="card__header">
            <h3>오늘의 학습</h3>
          </div>
          <div class="today-grid">
            <div class="today-item">
              <p class="today-item__num">{{ stats.todaySolved }}</p>
              <p class="today-item__label">오늘 풀이</p>
            </div>
            <div class="today-item">
              <p class="today-item__num">{{ stats.todayTime }}분</p>
              <p class="today-item__label">학습 시간</p>
            </div>
            <div class="today-item">
              <p class="today-item__num">{{ stats.todayAccuracy }}%</p>
              <p class="today-item__label">오늘 정답률</p>
            </div>
          </div>
          <RouterLink to="/student/diagnosis" class="btn btn-primary btn-block" style="margin-top: 16px; display: flex;">
            진단 테스트 시작
          </RouterLink>
        </div>

        <!-- AI 코멘트 -->
        <div class="ai-comment">
          <div class="ai-comment__header">
            <span class="ai-badge">AI</span>
            <span>오늘의 학습 코멘트</span>
          </div>
          <p>{{ aiComment }}</p>
        </div>
      </div>
    </div>

    <!-- 최근 학습 이력 -->
    <div class="card" style="margin-top: 24px;">
      <div class="card__header">
        <h3>최근 학습 이력</h3>
        <RouterLink to="/student/report" class="btn btn-ghost btn-sm">리포트 보기</RouterLink>
      </div>
      <AppTable
        :columns="historyColumns"
        :data="recentHistory"
        :loading="loading"
      >
        <template #cell-accuracy="{ value }">
          <span :style="{ color: value >= 80 ? '#10B981' : value >= 60 ? '#F59E0B' : '#EF4444', fontWeight: '600' }">
            {{ value }}%
          </span>
        </template>
        <template #cell-subject="{ value }">
          <span class="subject-tag">{{ value }}</span>
        </template>
      </AppTable>
    </div>

    <!-- [2026-04-03] 학급 가입 배너 -->
    <div class="join-class-banner">
      <div class="join-class-banner__text">
        <strong>학급 초대코드가 있으신가요?</strong>
        <span>선생님께 받은 초대코드를 입력하면 학급에 바로 가입할 수 있어요.</span>
      </div>
      <button class="btn btn-primary btn-md" @click="joinModal.open = true">학급 가입하기</button>
    </div>

    <!-- 학급 가입 모달 -->
    <Teleport to="body">
      <div v-if="joinModal.open" class="modal-backdrop" @click.self="closeJoinModal">
        <div class="modal-box">
          <div class="modal-header">
            <h2>학급 가입</h2>
            <button class="modal-close" @click="closeJoinModal">✕</button>
          </div>
          <div class="modal-body">
            <p class="join-desc">선생님께 받은 6자리 초대코드를 입력하세요.</p>
            <div class="form-group">
              <input
                v-model="joinModal.code"
                class="form-input invite-input"
                placeholder="예: AB3DEF7G"
                maxlength="8"
                @keyup.enter="submitJoin"
                @input="joinModal.code = joinModal.code.toUpperCase()"
              />
              <p v-if="joinModal.error" class="join-error">{{ joinModal.error }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-md" @click="closeJoinModal">취소</button>
            <button class="btn btn-primary btn-md" :disabled="joinModal.code.length < 6 || joinModal.loading" @click="submitJoin">
              {{ joinModal.loading ? '가입 중...' : '가입하기' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import AppBadge from '@/components/common/AppBadge.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppTable from '@/components/common/AppTable.vue'
import api from '@/utils/api'

const authStore = useAuthStore()
const user = computed(() => authStore.user)
const loading = ref(false)

const stats = ref({ totalSolved: 0, accuracy: 0, streak: 0, todaySolved: 0, todayTime: 0, todayAccuracy: 0 })
const assignments = ref([])
const recentHistory = ref([])

const historyColumns = [
  { key: 'date', label: '날짜' },
  { key: 'subject', label: '과목' },
  { key: 'problemCount', label: '문제 수' },
  { key: 'accuracy', label: '정답률' }
]

const aiComment = ref('')

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}년 ${d.getMonth()+1}월 ${d.getDate()}일 ${['일','월','화','수','목','금','토'][d.getDay()]}요일`
})

function formatDate(dateStr) {
  const d = new Date(dateStr)
  return `${d.getMonth()+1}월 ${d.getDate()}일`
}

// ── 학급 가입 모달 ────────────────────────────────
const joinModal = reactive({ open: false, code: '', loading: false, error: '' })

function closeJoinModal() {
  joinModal.open = false
  joinModal.code = ''
  joinModal.error = ''
}

async function submitJoin() {
  if (joinModal.code.length < 4 || joinModal.loading) return
  joinModal.loading = true
  joinModal.error = ''
  try {
    const res = await api.post('/student/classes/join', { inviteCode: joinModal.code })
    const data = res.data?.data || res.data
    closeJoinModal()
    alert(`'${data?.className || '학급'}'에 가입되었습니다!`)
  } catch (e) {
    const msg = e.response?.data?.message
    joinModal.error = msg || '유효하지 않은 초대코드이거나 이미 가입된 학급입니다.'
  } finally {
    joinModal.loading = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const [statsRes, assignRes, historyRes, aiRes] = await Promise.all([
      api.get('/student/dashboard/stats'),
      api.get('/student/assignments', { params: { size: 3 } }),
      api.get('/student/history', { params: { size: 5 } }),
      api.get('/student/ai-comment')
    ])
    const s = statsRes.data || {}
    stats.value = {
      totalSolved: s.totalSolved || 0, accuracy: s.accuracy || 0, streak: s.streak || 0,
      todaySolved: s.todaySolved || 0, todayTime: s.todayTime || 0, todayAccuracy: s.todayAccuracy || 0
    }
    assignments.value = (assignRes.data?.content || assignRes.data || [])
      .filter(a => (a.status || 'pending').toLowerCase() !== 'done')
      .map(a => ({
        id: a.assignmentId, sessionId: a.sessionId || a.assignmentId, subject: a.subject,
        title: a.title, dueDate: a.dueDate, progress: a.progress || 0
      }))
    recentHistory.value = (historyRes.data?.content || historyRes.data || []).map(h => ({
      date: h.date?.slice(0,10), subject: h.subject, problemCount: h.problemCount, accuracy: h.accuracy
    }))
    aiComment.value = aiRes.data?.comment || ''
  } catch {} finally { loading.value = false }
})
</script>

<style scoped lang="scss">
.student-home {
  display: flex;
  flex-direction: column;
  gap: $spacing-6;
}

.welcome-banner {
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  border-radius: $radius-lg;
  padding: $spacing-8;
  color: white;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;

  @media (max-width: $bp-mobile) {
    flex-direction: column;
    gap: $spacing-4;
  }

  &__content {
    display: flex;
    align-items: flex-start;
    gap: $spacing-5;
    flex-wrap: wrap;
  }

  &__text {
    h1 {
      font-size: $font-size-2xl;
      font-weight: 700;
      margin-bottom: $spacing-2;

      strong { color: #FDE68A; }
    }

    p {
      font-size: $font-size-sm;
      opacity: 0.85;
    }
  }

  &__date {
    font-size: $font-size-sm;
    opacity: 0.7;
    white-space: nowrap;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-5;

  @media (max-width: $bp-tablet) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: $bp-mobile) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  background: $bg-white;
  border-radius: $radius-lg;
  border: 1px solid $border;
  padding: $spacing-5;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  gap: $spacing-4;

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__label {
    font-size: $font-size-xs;
    color: $text-secondary;
    margin-bottom: 2px;
  }

  &__value {
    font-size: $font-size-xl;
    font-weight: 700;
    color: $text-primary;
  }
}

.home-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: $spacing-6;

  @media (max-width: $bp-tablet) {
    grid-template-columns: 1fr;
  }
}

.assignment-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.assignment-item {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  padding: $spacing-4;
  background: $bg-light;
  border-radius: $radius-md;
  border: 1px solid $border;

  @media (max-width: $bp-mobile) {
    flex-wrap: wrap;
  }

  &__info {
    flex: 1;

    h4 {
      font-size: $font-size-sm;
      font-weight: 600;
      color: $text-primary;
      margin: $spacing-1 0;
    }

    .due {
      font-size: $font-size-xs;
      color: $text-muted;
    }
  }

  &__progress {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    width: 120px;

    .progress-bar { flex: 1; }

    .progress-text {
      font-size: $font-size-xs;
      color: $text-secondary;
      width: 30px;
      text-align: right;
    }
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

.today-stats {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

.today-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-4;
  text-align: center;
}

.today-item {
  &__num {
    font-size: $font-size-2xl;
    font-weight: 700;
    color: $primary;
  }

  &__label {
    font-size: $font-size-xs;
    color: $text-secondary;
    margin-top: 2px;
  }
}

.ai-comment {
  background: linear-gradient(135deg, $primary-bg, #EEF2FF);
  border-radius: $radius-lg;
  padding: $spacing-5;
  border: 1px solid rgba(59, 130, 246, 0.2);

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    margin-bottom: $spacing-3;
    font-size: $font-size-sm;
    font-weight: 600;
    color: $primary;
  }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: 1.7;
  }
}

.ai-badge {
  background: $primary;
  color: white;
  padding: 2px 6px;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 700;
}

// ── 학급 가입 배너 ──────────────────────────────
.join-class-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-4;
  padding: $spacing-5 $spacing-6;
  background: $primary-bg;
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: $radius-lg;

  @media (max-width: $bp-mobile) {
    flex-direction: column;
    align-items: flex-start;
  }

  &__text {
    display: flex;
    flex-direction: column;
    gap: $spacing-1;

    strong {
      font-size: $font-size-base;
      color: $primary;
    }

    span {
      font-size: $font-size-sm;
      color: $text-secondary;
    }
  }
}

// ── 학급 가입 모달 ──────────────────────────────
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
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25);
  display: flex;
  flex-direction: column;
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

.join-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.form-input {
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

.invite-input {
  font-family: 'Courier New', monospace;
  font-size: $font-size-lg;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-align: center;
  text-transform: uppercase;
}

.join-error {
  font-size: $font-size-xs;
  color: $danger;
  margin-top: $spacing-1;
}
</style>
