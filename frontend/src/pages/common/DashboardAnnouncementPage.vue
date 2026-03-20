<template>
  <div class="announcement-page">
    <div class="page-header">
      <h1>공지사항</h1>
      <button v-if="isAdmin" class="btn btn-primary btn-md" @click="openCreateModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        공지사항 등록
      </button>
    </div>

    <!-- 필터 -->
    <div class="filter-bar">
      <input v-model="search" class="form-control ann-search-input" placeholder="제목 검색..." />
      <div class="filter-tabs">
        <button
          v-for="tab in tabs" :key="tab.value"
          :class="['tab-btn', { active: filterType === tab.value }]"
          @click="filterType = tab.value"
        >{{ tab.label }}</button>
      </div>
    </div>

    <!-- 공지 목록 -->
    <div class="announcement-list">
      <div
        v-for="item in announcements" :key="item.id"
        class="announcement-item"
        :class="{ important: item.isImportant }"
      >
        <div class="announcement-item__left" @click="openDetail(item)">
          <span v-if="item.isImportant" class="badge badge--important">중요</span>
          <span class="badge badge--type">{{ typeLabel(item.targetRole) }}</span>
          <span class="announcement-item__title">{{ item.title }}</span>
          <span v-if="isNew(item.createdAt)" class="badge badge--new">NEW</span>
        </div>
        <div class="announcement-item__right">
          <span class="announcement-item__date">{{ formatDate(item.createdAt) }}</span>
          <span class="announcement-item__views">조회 {{ item.viewCount }}</span>
          <template v-if="isAdmin">
            <button class="btn btn-ghost btn-xs" @click.stop="openEditModal(item)">수정</button>
            <button class="btn btn-danger btn-xs" @click.stop="handleDelete(item)">삭제</button>
          </template>
        </div>
      </div>

      <div v-if="announcements.length === 0" class="empty-state">
        <p>공지사항이 없습니다.</p>
      </div>
    </div>

    <AppPagination :current-page="page" :total-pages="totalPages" :total-elements="totalElements" @page-change="page = $event" />

    <!-- 상세 모달 -->
    <div v-if="selected" class="modal-overlay" @click.self="selected = null">
      <div class="modal">
        <div class="modal__header">
          <div class="modal__header-top">
            <div class="modal__badges">
              <span v-if="selected.isImportant" class="badge badge--important">중요</span>
              <span class="badge badge--type">{{ typeLabel(selected.targetRole) }}</span>
            </div>
            <button class="modal__close" @click="selected = null" aria-label="닫기">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M15 5L5 15M5 5l10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <h2 class="modal__title">{{ selected.title }}</h2>
          <div class="modal__meta">
            <span>{{ formatDate(selected.createdAt) }}</span>
            <span>조회 {{ selected.viewCount }}</span>
          </div>
        </div>
        <div class="modal__body" v-html="selected.content" />
      </div>
    </div>

    <!-- 등록/수정 모달 -->
    <div v-if="showEditor" class="modal-overlay" @click.self="showEditor = false">
      <div class="editor-modal">
        <div class="editor-modal__header">
          <h2>{{ editingId ? '공지사항 수정' : '공지사항 등록' }}</h2>
          <button class="modal__close" @click="showEditor = false" aria-label="닫기">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M15 5L5 15M5 5l10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <div class="editor-modal__body">
          <!-- 제목 -->
          <div class="form-group">
            <label>제목 <span class="required">*</span></label>
            <input v-model="form.title" class="form-control" placeholder="공지사항 제목을 입력하세요" />
          </div>

          <!-- 대상 / 중요 -->
          <div class="form-row">
            <div class="form-group">
              <label>대상</label>
              <div class="select-group">
                <button
                  v-for="opt in roleOptions" :key="opt.value"
                  :class="['role-btn', { active: form.targetRole === opt.value }]"
                  @click="form.targetRole = opt.value"
                >{{ opt.label }}</button>
              </div>
            </div>
            <div class="form-group">
              <label>중요 공지</label>
              <label class="toggle">
                <input type="checkbox" v-model="form.isImportant" />
                <span class="toggle__track">
                  <span class="toggle__thumb" />
                </span>
                <span class="toggle__label">{{ form.isImportant ? '중요' : '일반' }}</span>
              </label>
            </div>
          </div>

          <!-- 게시 기간 -->
          <div class="form-row">
            <div class="form-group">
              <label>게시 시작일</label>
              <input v-model="form.startAt" type="datetime-local" class="form-control" />
            </div>
            <div class="form-group">
              <label>게시 종료일</label>
              <input v-model="form.endAt" type="datetime-local" class="form-control" />
            </div>
          </div>

          <!-- 에디터 -->
          <div class="form-group">
            <label>내용 <span class="required">*</span></label>
            <div class="rich-editor">
              <!-- 툴바 -->
              <div class="rich-editor__toolbar">
                <button type="button" class="tool-btn" title="굵게" @mousedown.prevent="exec('bold')">
                  <b>B</b>
                </button>
                <button type="button" class="tool-btn" title="기울임" @mousedown.prevent="exec('italic')">
                  <i>I</i>
                </button>
                <button type="button" class="tool-btn" title="밑줄" @mousedown.prevent="exec('underline')">
                  <u>U</u>
                </button>
                <div class="tool-sep" />
                <button type="button" class="tool-btn" title="제목1" @mousedown.prevent="exec('formatBlock', 'h2')">H2</button>
                <button type="button" class="tool-btn" title="제목2" @mousedown.prevent="exec('formatBlock', 'h3')">H3</button>
                <button type="button" class="tool-btn" title="본문" @mousedown.prevent="exec('formatBlock', 'p')">P</button>
                <div class="tool-sep" />
                <button type="button" class="tool-btn" title="순서 없는 목록" @mousedown.prevent="exec('insertUnorderedList')">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="9" y1="6" x2="20" y2="6"/><line x1="9" y1="12" x2="20" y2="12"/><line x1="9" y1="18" x2="20" y2="18"/><circle cx="4" cy="6" r="1" fill="currentColor"/><circle cx="4" cy="12" r="1" fill="currentColor"/><circle cx="4" cy="18" r="1" fill="currentColor"/></svg>
                </button>
                <button type="button" class="tool-btn" title="순서 있는 목록" @mousedown.prevent="exec('insertOrderedList')">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="10" y1="6" x2="21" y2="6"/><line x1="10" y1="12" x2="21" y2="12"/><line x1="10" y1="18" x2="21" y2="18"/><path d="M4 6h1v4" stroke="currentColor"/><path d="M4 10H6" stroke="currentColor"/><path d="M6 18H4c0-1 2-2 2-3s-1-1.5-2-1.5" stroke="currentColor"/></svg>
                </button>
                <div class="tool-sep" />
                <button type="button" class="tool-btn" title="가운데 정렬" @mousedown.prevent="exec('justifyCenter')">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="6" y1="12" x2="18" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
                </button>
                <button type="button" class="tool-btn" title="인용" @mousedown.prevent="exec('formatBlock', 'blockquote')">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V20c0 1 0 1 1 1z"/><path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2h.75c0 2.25.25 4-2.75 4v3c0 1 0 1 1 1z"/></svg>
                </button>
                <div class="tool-sep" />
                <button type="button" class="tool-btn" title="링크 삽입" @mousedown.prevent="insertLink">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
                </button>
                <button type="button" class="tool-btn tool-btn--danger" title="서식 제거" @mousedown.prevent="exec('removeFormat')">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3H7L3 9l9 13 9-13-4-6z"/><line x1="3" y1="9" x2="21" y2="9"/></svg>
                </button>
              </div>
              <!-- 편집 영역 -->
              <div
                ref="editorRef"
                class="rich-editor__content"
                contenteditable="true"
                @input="onEditorInput"
              />
            </div>
          </div>
        </div>

        <div class="editor-modal__footer">
          <button class="btn btn-secondary" @click="showEditor = false">취소</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveAnnouncement">
            {{ saving ? '저장 중...' : '저장' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { useAuthStore } from '@/store/auth'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const authStore = useAuthStore()
const { success, error } = useToast()
const dialog = useDialog()

const isAdmin = computed(() => authStore.user?.role === 'ADMIN')

const search = ref('')
const filterType = ref('ALL')
const page = ref(1)
const selected = ref(null)
const pageSize = 10
const totalPages = ref(1)
const totalElements = ref(0)
const announcements = ref([])

// 에디터 상태
const showEditor = ref(false)
const editingId = ref(null)
const saving = ref(false)
const editorRef = ref(null)
const form = ref({
  title: '',
  targetRole: 'ALL',
  isImportant: false,
  startAt: '',
  endAt: '',
  content: ''
})

const tabs = [
  { value: 'ALL', label: '전체' },
  { value: 'STUDENT', label: '학생' },
  { value: 'TEACHER', label: '교사' }
]

const roleOptions = [
  { value: 'ALL', label: '전체' },
  { value: 'STUDENT', label: '학생' },
  { value: 'TEACHER', label: '교사' }
]

async function fetchAnnouncements() {
  try {
    const params = { page: page.value - 1, size: pageSize }
    if (search.value) params.keyword = search.value
    if (filterType.value !== 'ALL') params.targetRole = filterType.value
    const res = await api.get('/announcements', { params })
    announcements.value = (res.data?.content || res.data || []).map(a => ({
      id: a.announcementId, title: a.title, content: a.content,
      targetRole: a.targetRole || 'ALL', isImportant: a.isImportant || false,
      viewCount: a.viewCount || 0, createdAt: a.createdAt?.slice(0, 10),
      startAt: a.startAt, endAt: a.endAt
    }))
    totalPages.value = res.data?.totalPages || 1
    totalElements.value = res.data?.totalElements || announcements.value.length
  } catch {}
}

watch([search, filterType], () => { page.value = 1; fetchAnnouncements() })
watch(page, fetchAnnouncements)
onMounted(fetchAnnouncements)

function typeLabel(role) {
  return { ALL: '전체', STUDENT: '학생', TEACHER: '교사' }[role] || role
}
function formatDate(d) { return d?.slice(0, 10) || '' }
function isNew(date) {
  const diff = (new Date() - new Date(date)) / (1000 * 60 * 60 * 24)
  return diff <= 7
}
function openDetail(item) {
  item.viewCount++
  selected.value = item
}

// 에디터 열기
function openCreateModal() {
  editingId.value = null
  form.value = { title: '', targetRole: 'ALL', isImportant: false, startAt: '', endAt: '', content: '' }
  showEditor.value = true
  nextTick(() => { if (editorRef.value) editorRef.value.innerHTML = '' })
}

function openEditModal(item) {
  editingId.value = item.id
  const toLocalInput = (iso) => iso ? iso.replace(' ', 'T').slice(0, 16) : ''
  form.value = {
    title: item.title,
    targetRole: item.targetRole,
    isImportant: item.isImportant,
    startAt: toLocalInput(item.startAt),
    endAt: toLocalInput(item.endAt),
    content: item.content || ''
  }
  showEditor.value = true
  nextTick(() => { if (editorRef.value) editorRef.value.innerHTML = item.content || '' })
}

// 에디터 커맨드
function exec(cmd, value = null) {
  editorRef.value?.focus()
  document.execCommand(cmd, false, value)
}

function onEditorInput() {
  form.value.content = editorRef.value?.innerHTML || ''
}

function insertLink() {
  const url = prompt('링크 URL을 입력하세요:')
  if (url) exec('createLink', url)
}

// 저장
async function saveAnnouncement() {
  form.value.content = editorRef.value?.innerHTML || ''
  if (!form.value.title.trim()) { error('제목을 입력하세요.'); return }
  if (!form.value.content.trim()) { error('내용을 입력하세요.'); return }

  saving.value = true
  try {
    const payload = {
      title: form.value.title,
      content: form.value.content,
      targetRole: form.value.targetRole,
      isImportant: form.value.isImportant,
      startAt: form.value.startAt ? form.value.startAt + ':00' : null,
      endAt: form.value.endAt ? form.value.endAt + ':00' : null
    }
    if (editingId.value) {
      await api.put(`/announcements/${editingId.value}`, payload)
      success('공지사항을 수정했습니다.')
    } else {
      await api.post('/announcements', payload)
      success('공지사항을 등록했습니다.')
    }
    showEditor.value = false
    await fetchAnnouncements()
  } catch { error('저장에 실패했습니다.') } finally { saving.value = false }
}

// 삭제
async function handleDelete(item) {
  const ok = await dialog.confirm(`"${item.title}" 공지사항을 삭제하시겠습니까?`, { type: 'danger', confirmText: '삭제' })
  if (!ok) return
  try {
    await api.delete(`/announcements/${item.id}`)
    success('삭제했습니다.')
    await fetchAnnouncements()
  } catch { error('삭제에 실패했습니다.') }
}
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-5;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
  flex-wrap: wrap;
}

.ann-search-input {
  width: 240px;

  @media (max-width: $bp-mobile) {
    width: 100%;
  }
}

.filter-tabs {
  display: flex;
  gap: $spacing-1;
  background: $bg-light;
  padding: 3px;
  border-radius: $radius-md;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  flex: 1;
}

.tab-btn {
  padding: 5px 14px;
  border: none;
  background: transparent;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
  flex: 1;
  text-align: center;

  &.active {
    background: white;
    color: $primary;
    font-weight: 600;
    box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  }
}

.announcement-list {
  background: white;
  border-radius: $radius-lg;
  border: 1px solid $border;
  overflow: hidden;
  margin-bottom: $spacing-5;
}

.announcement-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4 $spacing-5;
  border-bottom: 1px solid $border;
  transition: background 0.15s;
  gap: $spacing-3;

  &:last-child { border-bottom: none; }
  &:hover { background: $bg-light; }

  &.important {
    background: #fffbeb;
    &:hover { background: #fef3c7; }
  }

  @media (max-width: $bp-mobile) {
    flex-direction: column;
    align-items: flex-start;
    padding: $spacing-3 $spacing-4;
    gap: $spacing-2;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    flex: 1;
    min-width: 0;
    cursor: pointer;

    @media (max-width: $bp-mobile) {
      flex-wrap: wrap;
      width: 100%;
    }
  }

  &__title {
    font-size: $font-size-sm;
    color: $text-primary;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    @media (max-width: $bp-mobile) {
      white-space: normal;
      overflow: visible;
      word-break: break-word;
    }
  }

  &__right {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-shrink: 0;

    @media (max-width: $bp-mobile) {
      width: 100%;
      flex-wrap: wrap;
      gap: $spacing-2;
    }
  }

  &__date, &__views {
    font-size: $font-size-xs;
    color: $text-muted;
    white-space: nowrap;
  }
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 7px;
  border-radius: $radius-full;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;

  &--important { background: #FEE2E2; color: #991B1B; }
  &--type { background: $primary-bg; color: $primary; }
  &--new { background: #D1FAE5; color: #065F46; }
}

.empty-state {
  padding: $spacing-12;
  text-align: center;
  color: $text-muted;
  font-size: $font-size-sm;
}

.btn-xs {
  padding: 2px 8px;
  font-size: 11px;
  height: 24px;
}

// 상세 모달
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  z-index: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-4;
}

.modal {
  background: white;
  border-radius: $radius-xl;
  width: 100%;
  max-width: 640px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);

  &__header {
    padding: $spacing-6;
    border-bottom: 1px solid $border;
  }

  &__header-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: $spacing-3;
  }

  &__badges { display: flex; gap: $spacing-2; }

  &__close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    border-radius: $radius-md;
    color: $text-muted;
    cursor: pointer;
    transition: background 0.15s, color 0.15s;
    flex-shrink: 0;
    margin: -4px -4px 0 0;

    &:hover { background: $bg-light; color: $text-primary; }
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: $spacing-2;
  }

  &__meta {
    display: flex;
    gap: $spacing-4;
    font-size: $font-size-xs;
    color: $text-muted;
  }

  &__body {
    padding: $spacing-6;
    overflow-y: auto;
    flex: 1;
    font-size: $font-size-sm;
    line-height: 1.8;
    color: $text-primary;
  }
}

// 에디터 모달
.editor-modal {
  background: white;
  border-radius: $radius-xl;
  width: 100%;
  max-width: 780px;
  max-height: 92vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-5 $spacing-6;
    border-bottom: 1px solid $border;

    h2 {
      font-size: $font-size-lg;
      font-weight: 700;
      color: $text-primary;
    }
  }

  &__body {
    padding: $spacing-6;
    overflow-y: auto;
    flex: 1;
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-3;
    padding: $spacing-4 $spacing-6;
    border-top: 1px solid $border;
  }
}

.form-group {
  margin-bottom: $spacing-4;

  label {
    display: block;
    font-size: $font-size-sm;
    font-weight: 500;
    margin-bottom: $spacing-2;
    color: $text-primary;
  }

  .required { color: $danger; }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-4;
  margin-bottom: 0;
}

.select-group {
  display: flex;
  gap: $spacing-1;
  background: $bg-light;
  padding: 3px;
  border-radius: $radius-md;
  width: fit-content;
}

.role-btn {
  padding: 5px 14px;
  border: none;
  background: transparent;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all 0.15s;

  &.active {
    background: white;
    color: $primary;
    font-weight: 600;
    box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  }
}

// 토글 스위치
.toggle {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  cursor: pointer;

  input { display: none; }

  &__track {
    position: relative;
    width: 40px;
    height: 22px;
    background: #D1D5DB;
    border-radius: 11px;
    transition: background 0.2s;
  }

  input:checked + .toggle__track { background: $primary; }

  &__thumb {
    position: absolute;
    top: 3px;
    left: 3px;
    width: 16px;
    height: 16px;
    background: white;
    border-radius: 50%;
    transition: left 0.2s;
    box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  }

  input:checked + .toggle__track .toggle__thumb { left: 21px; }

  &__label {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

// 리치 에디터
.rich-editor {
  border: 1px solid $border;
  border-radius: $radius-md;
  overflow: hidden;

  &__toolbar {
    display: flex;
    align-items: center;
    gap: 2px;
    padding: $spacing-2 $spacing-3;
    background: $bg-light;
    border-bottom: 1px solid $border;
    flex-wrap: wrap;
  }

  &__content {
    min-height: 240px;
    max-height: 360px;
    overflow-y: auto;
    padding: $spacing-4;
    font-size: $font-size-sm;
    line-height: 1.8;
    color: $text-primary;
    outline: none;

    &:focus { box-shadow: inset 0 0 0 2px rgba($primary, 0.15); }

    // 내부 HTML 렌더링 스타일
    :deep(h2) { font-size: 1.25em; font-weight: 700; margin: 0.75em 0 0.25em; }
    :deep(h3) { font-size: 1.1em; font-weight: 600; margin: 0.6em 0 0.2em; }
    :deep(p) { margin: 0.4em 0; }
    :deep(ul), :deep(ol) { padding-left: 1.5em; margin: 0.4em 0; }
    :deep(blockquote) {
      border-left: 3px solid $primary;
      padding-left: 1em;
      color: $text-secondary;
      margin: 0.5em 0;
    }
    :deep(a) { color: $primary; text-decoration: underline; }
  }
}

.tool-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  border: none;
  background: transparent;
  border-radius: $radius-sm;
  font-size: 13px;
  color: $text-secondary;
  cursor: pointer;
  transition: background 0.1s, color 0.1s;

  &:hover { background: $border; color: $text-primary; }
  &--danger:hover { color: $danger; }
}

.tool-sep {
  width: 1px;
  height: 18px;
  background: $border;
  margin: 0 $spacing-1;
}
</style>
