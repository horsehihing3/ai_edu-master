<template>
  <div class="bookmark-page">
    <div class="page-header">
      <h1>즐겨찾기</h1>
      <p>북마크한 문제를 다시 풀어보세요</p>
    </div>

    <div class="filter-bar">
      <select v-model="filterSubject" class="filter-select">
        <option value="">전체 과목</option>
        <option v-for="o in subjectOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
      <div class="level-btns">
        <button
          v-for="lv in ['ALL','A','B','C']"
          :key="lv"
          :class="['level-btn', `level-btn--${lv}`, { active: filterLevel === (lv === 'ALL' ? '' : lv) }]"
          @click="filterLevel = lv === 'ALL' ? '' : lv"
        >{{ lv }}</button>
      </div>
    </div>

    <div v-if="filteredBookmarks.length" class="bookmark-list">
      <div v-for="b in filteredBookmarks" :key="b.id" class="bookmark-item card">
        <div class="bookmark-item__left">
          <div class="bm-meta">
            <span class="subject-tag">{{ b.subject }}</span>
            <AppBadge :type="b.level" />
            <span class="unit-info">{{ b.unit }}</span>
          </div>
          <p class="question-preview">{{ b.questionText }}</p>
          <p class="bookmarked-date">북마크: {{ formatDate(b.bookmarkedAt) }}</p>
        </div>
        <div class="bookmark-item__right">
          <button class="btn btn-primary btn-sm" @click="startBookmarkSession(b)">다시 풀기</button>
          <button class="btn btn-secondary btn-sm" @click="removeBookmark(b.id)">삭제</button>
        </div>
      </div>
    </div>
    <AppEmpty v-else message="북마크한 문제가 없습니다." description="문제 풀기 중 오답 북마크 버튼을 클릭하면 여기에 저장됩니다." />

    <AppPagination
      :current-page="page"
      :total-pages="totalPages"
      :total-elements="totalElements"
      @page-change="page = $event"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppBadge from '@/components/common/AppBadge.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const router = useRouter()

const { success, error } = useToast()
const filterSubject = ref('')
const filterLevel = ref('')
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)

const subjectOptions = ['수학','영어','국어','과학','사회'].map(v => ({ value: v, label: v }))
const levelOptions = ['A','B','C'].map(v => ({ value: v, label: `${v} 레벨` }))

const bookmarks = ref([])
const filteredBookmarks = computed(() => bookmarks.value)

async function fetchBookmarks() {
  try {
    const params = { page: page.value - 1, size: 10 }
    if (filterSubject.value) params.subject = filterSubject.value
    if (filterLevel.value) params.level = filterLevel.value
    const res = await api.get('/student/bookmarks', { params })
    bookmarks.value = (res.data?.content || res.data || []).map(b => ({
      id: b.bookmarkId, problemId: b.problemId,
      subject: b.subject, level: b.level, unit: b.unitName || b.unit,
      questionText: b.questionText, bookmarkedAt: b.createdAt?.slice(0,10)
    }))
    totalPages.value = res.data?.totalPages || 1
    totalElements.value = res.data?.totalElements || bookmarks.value.length
  } catch {}
}

watch([filterSubject, filterLevel], () => { page.value = 1; fetchBookmarks() })
watch(page, fetchBookmarks)
onMounted(fetchBookmarks)

async function startBookmarkSession(b) {
  try {
    const res = await api.post('/student/sessions/start-bookmark', { problemIds: [b.problemId] })
    const sessionId = res.data?.sessionId
    if (sessionId) {
      router.push(`/student/learn/${sessionId}`)
    }
  } catch { error('세션 생성에 실패했습니다.') }
}

async function removeBookmark(id) {
  try {
    await api.delete(`/student/bookmarks/${id}`)
    bookmarks.value = bookmarks.value.filter(b => b.id !== id)
    success('북마크를 삭제했습니다.')
  } catch { error('삭제에 실패했습니다.') }
}

function formatDate(d) {
  const date = new Date(d)
  return `${date.getMonth()+1}/${date.getDate()}`
}
</script>

<style scoped lang="scss">
.filter-bar {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $spacing-6;
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

.bookmark-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.bookmark-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-5;

  @media (max-width: $bp-mobile) {
    flex-direction: column;
    align-items: flex-start;
  }

  &__left {
    flex: 1;
  }

  &__right {
    display: flex;
    gap: $spacing-2;
    flex-shrink: 0;
  }
}

.bm-meta {
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
}

.bookmarked-date {
  font-size: $font-size-xs;
  color: $text-muted;
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
</style>
