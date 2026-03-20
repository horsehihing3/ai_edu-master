<template>
  <div class="problem-list-page">
    <div class="page-header">
      <h1>문제 풀기</h1>
      <p>과제를 선택하여 학습을 시작하세요</p>
    </div>

    <!-- 레벨 필터 -->
    <div class="level-btns">
      <button
        v-for="lv in ['ALL','A','B','C']"
        :key="lv"
        :class="['level-btn', `level-btn--${lv}`, { active: filterLevel === (lv === 'ALL' ? '' : lv) }]"
        @click="filterLevel = lv === 'ALL' ? '' : lv"
      >{{ lv }}</button>
    </div>

    <!-- 탭 -->
    <div class="tabs">
      <button :class="['tab', { active: tab === 'pending' }]" @click="tab = 'pending'">
        미완료 <span class="tab-count">{{ pending.length }}</span>
      </button>
      <button :class="['tab', { active: tab === 'done' }]" @click="tab = 'done'">
        완료 <span class="tab-count">{{ done.length }}</span>
      </button>
    </div>

    <div v-if="loading" class="loading-wrap">
      <AppLoading />
    </div>
    <div v-else>
      <div v-if="filteredList.length" class="assignment-grid">
        <div
          v-for="a in filteredList"
          :key="a.id"
          class="assignment-card"
          @click="startAssignment(a)"
        >
          <div class="assignment-card__top">
            <span v-if="a.subject" :class="['subject-tag', `subject-tag--${a.subject}`]">{{ a.subject }}</span>
            <AppBadge v-if="a.level" :type="a.level" />
          </div>
          <h3>{{ a.title }}</h3>
          <p class="desc">{{ a.description }}</p>
          <div class="assignment-card__meta">
            <span>총 {{ a.totalCount }}문제</span>
            <span>마감: {{ formatDate(a.dueDate) }}</span>
          </div>
          <div class="assignment-card__progress">
            <div class="progress-bar">
              <div class="progress-bar__fill" :style="{ width: a.progress + '%' }" />
            </div>
            <span>{{ a.progress }}%</span>
          </div>
          <div class="assignment-card__footer">
            <span :class="['status', a.status]">
              {{ a.status === 'pending' ? '미완료' : a.status === 'in_progress' ? '진행 중' : '완료' }}
            </span>
            <button v-if="a.status === 'done'" class="btn btn-secondary btn-sm" @click.stop="viewFeedback(a)">피드백 보기</button>
            <button v-else class="btn btn-primary btn-sm">풀기 시작</button>
          </div>
        </div>
      </div>
      <AppEmpty v-else :message="tab === 'pending' ? '미완료 과제가 없습니다.' : '완료된 과제가 없습니다.'" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppBadge from '@/components/common/AppBadge.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import api from '@/utils/api'

const router = useRouter()

const filterLevel = ref('')
const tab = ref('pending')
const loading = ref(false)
const assignments = ref([])

const byLevel = computed(() =>
  filterLevel.value ? assignments.value.filter(a => a.level === filterLevel.value) : assignments.value
)
const pending = computed(() => byLevel.value.filter(a => a.status !== 'done'))
const done    = computed(() => byLevel.value.filter(a => a.status === 'done'))
const filteredList = computed(() => tab.value === 'pending' ? pending.value : done.value)

async function fetchAssignments() {
  loading.value = true
  try {
    const res = await api.get('/student/assignments')
    assignments.value = (res.data?.content || res.data || []).map(a => ({
      id: a.assignmentId, sessionId: a.sessionId || a.assignmentId,
      subject: a.subject, level: a.level, title: a.title,
      description: a.description || '', totalCount: a.totalCount || a.problemCount || 0,
      dueDate: a.dueDate, progress: a.progress || 0,
      status: a.status?.toLowerCase() || 'pending'
    }))
  } catch {} finally { loading.value = false }
}

onMounted(fetchAssignments)

async function startAssignment(a) {
  if (a.status === 'done') {
    router.push(`/student/assignments/${a.id}/feedback`)
    return
  }
  try {
    const res = await api.post('/student/sessions/start', { assignmentId: a.id })
    const sessionId = res.data?.sessionId
    if (sessionId) {
      router.push(`/student/learn/${sessionId}`)
    }
  } catch {}
}

function viewFeedback(a) {
  router.push(`/student/assignments/${a.id}/feedback`)
}

function formatDate(d) {
  const date = new Date(d)
  return `${date.getMonth()+1}/${date.getDate()}`
}
</script>

<style scoped lang="scss">
.level-btns {
  display: flex;
  gap: $spacing-1;
  margin-bottom: $spacing-4;
}

.level-btn {
  padding: 5px 16px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 700;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all $transition-fast;

  &--ALL { background: #E5E7EB; color: #374151; border-color: #D1D5DB; &.active { background: #6B7280; color: white; border-color: #6B7280; } }
  &--A   { background: #DBEAFE; color: #1D4ED8; border-color: #93C5FD; &.active { background: #2563EB; color: white; border-color: #2563EB; } }
  &--B   { background: #DCFCE7; color: #15803D; border-color: #86EFAC; &.active { background: #16A34A; color: white; border-color: #16A34A; } }
  &--C   { background: #FEE2E2; color: #B91C1C; border-color: #FCA5A5; &.active { background: #DC2626; color: white; border-color: #DC2626; } }
}

.tabs {
  display: flex;
  gap: $spacing-2;
  margin-bottom: $spacing-6;
  border-bottom: 2px solid $border;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
}

.tab {
  padding: $spacing-3 $spacing-5;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-secondary;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  gap: $spacing-2;
  white-space: nowrap;
  flex-shrink: 0;

  &.active {
    color: $primary-light;
    border-bottom-color: $primary-light;
    font-weight: 600;
  }

  &:hover { color: $primary-light; }
}

.tab-count {
  background: $border;
  color: $text-muted;
  border-radius: $radius-full;
  padding: 1px 7px;
  font-size: $font-size-xs;
  font-weight: 600;
}

.assignment-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-5;

  @media (max-width: $bp-tablet) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: $bp-mobile) { grid-template-columns: 1fr; }
}

.assignment-card {
  background: $bg-white;
  border-radius: $radius-lg;
  border: 1px solid $border;
  padding: $spacing-5;
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-3;
  }

  h3 {
    font-size: $font-size-base;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: $spacing-2;
  }

  .desc {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-4;
    line-height: 1.5;
  }

  &__meta {
    display: flex;
    justify-content: space-between;
    font-size: $font-size-xs;
    color: $text-muted;
    margin-bottom: $spacing-3;
  }

  &__progress {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    margin-bottom: $spacing-4;

    .progress-bar { flex: 1; }

    span {
      font-size: $font-size-xs;
      color: $text-secondary;
      width: 28px;
    }
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.subject-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 700;
  background: $primary-bg;
  color: $primary;

  &--A { background: #2563EB; color: white; }
  &--B { background: #16A34A; color: white; }
  &--C { background: #DC2626; color: white; }
}

.status {
  font-size: $font-size-xs;
  font-weight: 600;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;

  &.pending { background: #FEF3C7; color: #92400E; }
  &.in_progress { background: #DBEAFE; color: #1E40AF; }
  &.done { background: #D1FAE5; color: #065F46; }
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: $spacing-12;
}
</style>
