<template>
  <div class="inquiry-page">
    <div class="page-header"><h1>1:1 문의 관리</h1></div>

    <!-- 상태 필터 -->
    <div class="filter-bar">
      <button
        v-for="f in filters" :key="f.value"
        :class="['filter-btn', { active: statusFilter === f.value }]"
        @click="setFilter(f.value)"
      >{{ f.label }}</button>
    </div>

    <!-- 목록 -->
    <div class="card">
      <table class="inquiry-table">
        <thead>
          <tr>
            <th>No</th>
            <th>작성자</th>
            <th>제목</th>
            <th>카테고리</th>
            <th>작성일</th>
            <th>상태</th>
            <th>답변</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!items.length">
            <td colspan="7" class="empty-row">문의 내역이 없습니다</td>
          </tr>
          <tr v-for="item in items" :key="item.inquiryId" class="inquiry-row">
            <td>{{ item.inquiryId }}</td>
            <td>{{ item.userName }}</td>
            <td class="title-cell" @click="openDetail(item.inquiryId)">{{ item.title }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.createdAt?.slice(0, 16)?.replace('T', ' ') }}</td>
            <td>
              <span :class="['status-badge', item.status?.toLowerCase()]">
                {{ item.status === 'ANSWERED' ? '답변완료' : '미답변' }}
              </span>
            </td>
            <td>
              <button class="btn btn-sm btn-primary" @click="openDetail(item.inquiryId)">
                {{ item.status === 'ANSWERED' ? '보기' : '답변하기' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="page === 0" class="btn btn-ghost btn-sm" @click="changePage(page - 1)">이전</button>
        <span class="page-info">{{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" class="btn btn-ghost btn-sm" @click="changePage(page + 1)">다음</button>
      </div>
    </div>

    <!-- 상세/답변 모달 -->
    <div v-if="modal.open" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal__header">
          <h3>{{ modal.inquiry?.title }}</h3>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>
        <div class="modal__body">
          <div class="inquiry-meta">
            <span>작성자: <strong>{{ modal.inquiry?.userName }}</strong></span>
            <span>카테고리: {{ modal.inquiry?.category }}</span>
            <span>{{ modal.inquiry?.createdAt?.slice(0, 16)?.replace('T', ' ') }}</span>
          </div>
          <div class="inquiry-content">{{ modal.inquiry?.content }}</div>

          <!-- 기존 답변 -->
          <div v-if="modal.replies?.length" class="replies-section">
            <h4>답변 내역</h4>
            <div v-for="r in modal.replies" :key="r.replyId" class="reply-item">
              <span class="reply-label">관리자 답변</span>
              <p class="reply-content">{{ r.content }}</p>
              <span class="reply-date">{{ r.createdAt?.slice(0, 16)?.replace('T', ' ') }}</span>
            </div>
          </div>

          <!-- 답변 작성 -->
          <div class="reply-form">
            <h4>{{ modal.replies?.length ? '추가 답변' : '답변 작성' }}</h4>
            <textarea
              v-model="replyContent"
              class="reply-textarea"
              placeholder="답변 내용을 입력하세요..."
              rows="5"
            />
            <div class="reply-form__actions">
              <button class="btn btn-ghost btn-sm" @click="closeModal">취소</button>
              <button class="btn btn-primary btn-sm" :disabled="!replyContent.trim() || submitting" @click="submitReply">
                {{ submitting ? '등록 중...' : '답변 등록' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/api'

const filters = [
  { label: '전체', value: '' },
  { label: '미답변', value: 'PENDING' },
  { label: '답변완료', value: 'ANSWERED' }
]

const statusFilter = ref('')
const items = ref([])
const page = ref(0)
const totalPages = ref(1)
const modal = ref({ open: false, inquiry: null, replies: [] })
const replyContent = ref('')
const submitting = ref(false)

async function load() {
  const params = { page: page.value, size: 20 }
  if (statusFilter.value) params.status = statusFilter.value
  const res = await api.get('/admin/inquiries', { params })
  const data = res.data || {}
  items.value = data.content || []
  totalPages.value = data.totalPages || 1
}

function setFilter(val) {
  statusFilter.value = val
  page.value = 0
  load()
}

function changePage(p) {
  page.value = p
  load()
}

async function openDetail(inquiryId) {
  const res = await api.get(`/admin/inquiries/${inquiryId}`)
  const data = res.data || {}
  modal.value = { open: true, inquiry: data, replies: data.replies || [] }
  replyContent.value = ''
}

function closeModal() {
  modal.value = { open: false, inquiry: null, replies: [] }
  replyContent.value = ''
}

async function submitReply() {
  if (!replyContent.value.trim()) return
  submitting.value = true
  try {
    await api.post(`/admin/inquiries/${modal.value.inquiry.inquiryId}/reply`, {
      content: replyContent.value.trim()
    })
    // Refresh the item status in the list
    const idx = items.value.findIndex(i => i.inquiryId === modal.value.inquiry.inquiryId)
    if (idx !== -1) items.value[idx].status = 'ANSWERED'
    closeModal()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.filter-bar {
  display: flex;
  gap: $spacing-2;
  margin-bottom: $spacing-4;
}

.filter-btn {
  padding: $spacing-2 $spacing-4;
  border: 1px solid $border;
  border-radius: $radius-full;
  background: white;
  font-size: $font-size-sm;
  cursor: pointer;
  color: $text-secondary;
  transition: all $transition-fast;

  &:hover { border-color: $primary; color: $primary; }
  &.active { background: $primary; color: white; border-color: $primary; }
}

.inquiry-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;

  th {
    text-align: left;
    padding: $spacing-3 $spacing-4;
    color: $text-muted;
    font-weight: 600;
    font-size: $font-size-xs;
    border-bottom: 1px solid $border;
  }

  td {
    padding: $spacing-3 $spacing-4;
    border-bottom: 1px solid $border;
    color: $text-secondary;
  }

  tr:last-child td { border-bottom: none; }
}

.empty-row {
  text-align: center;
  color: $text-muted;
  padding: $spacing-10 !important;
}

.title-cell {
  cursor: pointer;
  color: $text-primary;
  font-weight: 500;
  &:hover { color: $primary; }
}

.status-badge {
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: 11px;
  font-weight: 600;

  &.answered { background: #D1FAE5; color: #065F46; }
  &.pending   { background: #FEF3C7; color: #92400E; }
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-4;
  padding: $spacing-4 0 0;
}

.page-info { font-size: $font-size-sm; color: $text-muted; }

// 모달
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: $radius-lg;
  width: 600px;
  max-width: 90vw;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: $shadow-lg;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-5 $spacing-6;
    border-bottom: 1px solid $border;

    h3 { font-size: $font-size-lg; font-weight: 700; color: $text-primary; }
  }

  &__body { padding: $spacing-6; }
}

.modal-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: $text-muted;
  &:hover { color: $text-primary; }
}

.inquiry-meta {
  display: flex;
  gap: $spacing-4;
  flex-wrap: wrap;
  font-size: $font-size-xs;
  color: $text-muted;
  margin-bottom: $spacing-4;
}

.inquiry-content {
  background: $bg-light;
  border-radius: $radius-md;
  padding: $spacing-4;
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.7;
  white-space: pre-wrap;
  margin-bottom: $spacing-5;
}

.replies-section {
  margin-bottom: $spacing-5;

  h4 { font-size: $font-size-sm; font-weight: 700; margin-bottom: $spacing-3; color: $text-primary; }
}

.reply-item {
  background: #EFF6FF;
  border-radius: $radius-md;
  padding: $spacing-4;
  margin-bottom: $spacing-3;
}

.reply-label {
  font-size: $font-size-xs;
  font-weight: 700;
  color: $primary;
}

.reply-content {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin: $spacing-2 0;
  white-space: pre-wrap;
  line-height: 1.6;
}

.reply-date {
  font-size: $font-size-xs;
  color: $text-muted;
}

.reply-form {
  h4 { font-size: $font-size-sm; font-weight: 700; margin-bottom: $spacing-3; color: $text-primary; }

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-2;
    margin-top: $spacing-3;
  }
}

.reply-textarea {
  width: 100%;
  border: 1px solid $border;
  border-radius: $radius-md;
  padding: $spacing-3;
  font-size: $font-size-sm;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: $primary-light;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}
</style>
