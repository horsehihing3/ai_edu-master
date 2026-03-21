<template>
  <div class="wrong-notes-page">
    <div class="page-header">
      <h1>오답노트</h1>
      <p>틀린 문제를 다시 확인하고 복습하세요</p>
    </div>

    <div class="filter-bar">
      <select v-model="filterSubject" class="filter-select">
        <option value="">전체 과목</option>
        <option v-for="o in subjectOptions" :key="o" :value="o">{{ o }}</option>
      </select>
      <select v-model="filterUnit" class="filter-select">
        <option value="">전체 단원</option>
        <option v-for="u in unitOptions" :key="u" :value="u">{{ u }}</option>
      </select>
      <div class="level-btns">
        <button
          v-for="lv in ['ALL','A','B','C']"
          :key="lv"
          :class="['level-btn', `level-btn--${lv}`, { active: filterLevel === (lv === 'ALL' ? '' : lv) }]"
          @click="filterLevel = lv === 'ALL' ? '' : lv; currentPage = 1"
        >{{ lv }}</button>
      </div>
      <button class="btn btn-danger btn-sm delete-all-btn" @click="deleteAll">전체 삭제</button>
    </div>

    <div v-if="pagedItems.length" class="wrong-note-list">
      <div v-for="item in pagedItems" :key="item.id" class="wrong-note-item card">
        <div class="wn-left">
          <div class="wn-meta">
            <span class="subject-tag">{{ item.subject }}</span>
            <AppBadge :type="item.level" />
            <span class="unit-info">{{ item.unitName }}</span>
          </div>
          <!-- [2026-03-21] KaTeX 수식 렌더링 적용 -->
          <p class="question-preview"><MathText :text="item.questionText" /></p>
          <div class="wn-footer">
            <span class="created-date">오답일: {{ formatDate(item.createdAt) }}</span>
            <span :class="['resolved-badge', item.isResolved ? 'resolved-badge--done' : 'resolved-badge--pending']">
              {{ item.isResolved ? '해결됨' : '미해결' }}
            </span>
          </div>
        </div>
        <div class="wn-right">
          <!-- [2026-03-21] 재도전 버튼 — SINGLE 세션 시작 후 문제풀이 화면으로 이동 -->
          <button class="btn btn-primary btn-sm" @click="retryProblem(item)">
            🔁 재도전
          </button>
          <button
            :class="['btn', 'btn-sm', item.isResolved ? 'btn-success' : 'btn-secondary']"
            @click="toggleResolve(item)"
          >{{ item.isResolved ? '해결됨' : '미해결' }}</button>
          <button class="btn btn-secondary btn-sm" @click="deleteItem(item.id)">삭제</button>
          <button class="video-link-btn" @click="goToVideo(item.problemId)">
            🎬 관련 영상 보기
          </button>
        </div>
      </div>
    </div>
    <AppEmpty v-else message="오답노트가 비어있습니다." description="문제를 풀고 오답이 발생하면 자동으로 기록됩니다." />

    <AppPagination
      :current-page="currentPage"
      :total-pages="totalPages"
      :total-elements="filteredItems.length"
      @page-change="currentPage = $event"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppBadge from '@/components/common/AppBadge.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import MathText from '@/components/common/MathText.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const router = useRouter()
const { success, error } = useToast()

const items = ref([])
const filterSubject = ref('')
const filterUnit = ref('')
const filterLevel = ref('')
const currentPage = ref(1)
const pageSize = 10

const subjectOptions = computed(() => [...new Set(items.value.map(i => i.subject).filter(Boolean))])
const unitOptions = computed(() => {
  const base = filterSubject.value
    ? items.value.filter(i => i.subject === filterSubject.value)
    : items.value
  return [...new Set(base.map(i => i.unitName).filter(Boolean))]
})

const filteredItems = computed(() => {
  return items.value.filter(i => {
    if (filterSubject.value && i.subject !== filterSubject.value) return false
    if (filterUnit.value && i.unitName !== filterUnit.value) return false
    if (filterLevel.value && i.level !== filterLevel.value) return false
    return true
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / pageSize)))

const pagedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredItems.value.slice(start, start + pageSize)
})

watch([filterSubject, filterUnit, filterLevel], () => { currentPage.value = 1 })
watch(filterSubject, () => { filterUnit.value = '' })

async function fetchWrongNotes() {
  try {
    const res = await api.get('/student/wrong-notes', { params: { page: 0, size: 50 } })
    items.value = (res.data?.content || res.data || []).map(w => ({
      id: w.wrongNoteId ?? w.id,
      problemId: w.problemId,
      subject: w.subject,
      unitName: w.unitName,
      level: w.level,
      questionText: w.questionText,
      isResolved: w.isResolved ?? false,
      createdAt: w.createdAt
    }))
  } catch {
    error('오답노트를 불러오지 못했습니다.')
  }
}

async function toggleResolve(item) {
  try {
    await api.patch(`/student/wrong-notes/${item.id}/resolve`)
    item.isResolved = !item.isResolved
  } catch {
    error('상태 변경에 실패했습니다.')
  }
}

async function deleteItem(id) {
  try {
    await api.delete(`/student/wrong-notes/${id}`)
    items.value = items.value.filter(i => i.id !== id)
    success('삭제했습니다.')
  } catch {
    error('삭제에 실패했습니다.')
  }
}

async function deleteAll() {
  if (!window.confirm('오답노트 전체를 삭제하시겠습니까?')) return
  try {
    await api.delete('/student/wrong-notes')
    items.value = []
    currentPage.value = 1
    success('전체 삭제했습니다.')
  } catch {
    error('전체 삭제에 실패했습니다.')
  }
}

// [2026-03-21] 재도전 — SINGLE 세션 생성 후 문제풀이 화면으로 이동
async function retryProblem(item) {
  try {
    const res = await api.post('/student/sessions/start-single', { problemId: item.problemId })
    const sessionId = res.data?.sessionId
    router.push({ path: `/student/learn/${sessionId}`, query: { from: 'wrong-notes' } })
  } catch {
    error('재도전 세션 생성에 실패했습니다.')
  }
}

// [2026-03-20] 오답노트 관련 영상 바로보기 추가
function goToVideo(problemId) {
  router.push({ path: '/student/videos', query: { problemId } })
}

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
}

onMounted(fetchWrongNotes)
</script>

<style scoped lang="scss">
.filter-bar {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $spacing-6;
  flex-wrap: wrap;

  .delete-all-btn {
    margin-left: auto;
  }
}

.filter-select {
  height: 38px;
  padding: 0 24px 0 $spacing-3;
  border: 1px solid $border;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  color: $text-primary;
  background: white url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%236B7280' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E") no-repeat;
  background-position: right 8px center;
  cursor: pointer;
  width: 160px;
  appearance: none;

  &:focus { outline: none; border-color: $primary; }
}

.level-btns {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  height: 38px;
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

.wrong-note-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  margin-bottom: $spacing-6;
}

.wrong-note-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-5;

  @media (max-width: $bp-mobile) {
    flex-direction: column;
    align-items: flex-start;
  }

  .wn-left { flex: 1; min-width: 0; }
  .wn-right { display: flex; gap: $spacing-2; flex-shrink: 0; }
}

.wn-meta {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  margin-bottom: $spacing-2;
}

.question-preview {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: 500;
  margin-bottom: $spacing-2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.wn-footer {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.created-date {
  font-size: $font-size-xs;
  color: $text-muted;
}

.resolved-badge {
  font-size: $font-size-xs;
  font-weight: 600;
  padding: 2px $spacing-2;
  border-radius: $radius-full;

  &--done { background: #DCFCE7; color: #15803D; }
  &--pending { background: #FEE2E2; color: #B91C1C; }
}

.subject-tag {
  background: $primary-bg;
  color: $primary;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 600;
}

.unit-info {
  font-size: $font-size-xs;
  color: $text-muted;
}

.video-link-btn {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  padding: $spacing-1 $spacing-3;
  border-radius: $radius-md;
  font-size: $font-size-xs;
  font-weight: 500;
  cursor: pointer;
  transition: all $transition-fast;
  border: 1.5px solid $border;
  background: $bg-white;
  color: $text-secondary;
  white-space: nowrap;

  &:hover { border-color: $primary-light; color: $primary-light; }
}
</style>
