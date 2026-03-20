<template>
  <div class="inquiry-page">
    <div class="page-header" style="display:flex; align-items:center; justify-content:space-between;">
      <h1>1:1 문의</h1>
      <button class="btn btn-primary" @click="showForm = true">문의하기</button>
    </div>

    <!-- 필터 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <button
          v-for="tab in statusTabs" :key="tab.value"
          :class="['tab-btn', { active: filterStatus === tab.value }]"
          @click="filterStatus = tab.value"
        >{{ tab.label }}</button>
      </div>
    </div>

    <!-- 문의 목록 -->
    <div class="inquiry-list">
      <div
        v-for="item in filteredList" :key="item.id"
        class="inquiry-item"
        @click="openDetail(item)"
      >
        <div class="inquiry-item__left">
          <span :class="['status-badge', item.status.toLowerCase()]">{{ statusLabel(item.status) }}</span>
          <span class="inquiry-item__category">{{ categoryLabel(item.category) }}</span>
          <span class="inquiry-item__title">{{ item.title }}</span>
          <span v-if="item.isSecret" class="lock-icon">🔒</span>
        </div>
        <div class="inquiry-item__right">
          <span class="inquiry-item__date">{{ formatDate(item.createdAt) }}</span>
        </div>
      </div>

      <div v-if="filteredList.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <p>문의 내역이 없습니다.</p>
        <button class="btn btn-primary btn-sm" @click="showForm = true">첫 문의 남기기</button>
      </div>
    </div>

    <AppPagination :current-page="page" :total-pages="totalPages" :total-elements="filteredList.length" @page-change="page = $event" />

    <!-- 문의 작성 모달 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal">
        <div class="modal__header">
          <h2>문의하기</h2>
          <button class="modal__close" @click="showForm = false">✕</button>
        </div>
        <div class="modal__body">
          <div class="form-group">
            <label>카테고리</label>
            <select v-model="form.category" class="form-control">
              <option value="LEARNING">학습 관련</option>
              <option value="ACCOUNT">계정 관련</option>
              <option value="PAYMENT">결제 관련</option>
              <option value="ETC">기타</option>
            </select>
          </div>
          <div class="form-group">
            <label>제목</label>
            <input v-model="form.title" class="form-control" placeholder="제목을 입력하세요" style="height:36px;" />
          </div>
          <div class="form-group">
            <label>내용</label>
            <textarea v-model="form.content" class="form-control" placeholder="문의 내용을 입력하세요" rows="5" />
          </div>
          <label class="form-checkbox">
            <input type="checkbox" v-model="form.isSecret" />
            <span>비밀 문의 (작성자와 관리자만 볼 수 있습니다)</span>
          </label>
        </div>
        <div class="modal__footer">
          <button class="btn btn-secondary" @click="showForm = false">취소</button>
          <button class="btn btn-primary" @click="submitInquiry">제출하기</button>
        </div>
      </div>
    </div>

    <!-- 상세 모달 -->
    <div v-if="selected" class="modal-overlay" @click.self="selected = null">
      <div class="modal modal--large">
        <div class="modal__header">
          <div style="display:flex; align-items:center; gap:8px; margin-bottom:8px;">
            <span :class="['status-badge', selected.status.toLowerCase()]">{{ statusLabel(selected.status) }}</span>
            <span class="inquiry-item__category">{{ categoryLabel(selected.category) }}</span>
          </div>
          <h2 class="modal__title">{{ selected.title }}</h2>
          <div class="modal__meta">
            <span>{{ formatDate(selected.createdAt) }}</span>
          </div>
          <button class="modal__close" @click="selected = null">✕</button>
        </div>
        <div class="modal__body">
          <div class="inquiry-content">{{ selected.content }}</div>

          <!-- 답변 -->
          <div v-if="selected.reply" class="reply-box">
            <div class="reply-box__header">
              <span class="reply-box__label">관리자 답변</span>
              <span class="reply-box__date">{{ formatDate(selected.replyAt) }}</span>
            </div>
            <div class="reply-box__content">{{ selected.reply }}</div>
          </div>
          <div v-else class="reply-pending">
            <p>답변 대기 중입니다. 영업일 기준 1~2일 내 답변드립니다.</p>
          </div>
        </div>
        <div class="modal__footer">
          <button class="btn btn-secondary" @click="selected = null">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { useToast } from '@/composables/useToast'

const { success } = useToast()
const filterStatus = ref('ALL')
const page = ref(1)
const pageSize = 10
const showForm = ref(false)
const selected = ref(null)

const statusTabs = [
  { value: 'ALL', label: '전체' },
  { value: 'PENDING', label: '답변 대기' },
  { value: 'IN_PROGRESS', label: '처리 중' },
  { value: 'COMPLETED', label: '답변 완료' }
]

const form = ref({ category: 'LEARNING', title: '', content: '', isSecret: false })

const inquiries = ref([
  { id: 1, title: '학습 레벨 변경 요청', category: 'LEARNING', content: '현재 B레벨로 배정되어 있는데 A레벨로 변경 요청드립니다.', status: 'COMPLETED', isSecret: false, createdAt: '2026-03-10', reply: '안녕하세요. 진단 테스트를 다시 응시하시면 레벨이 자동으로 재조정됩니다.', replyAt: '2026-03-11' },
  { id: 2, title: '결제 취소 문의', category: 'PAYMENT', content: '지난달 결제 내역 취소 요청드립니다.', status: 'IN_PROGRESS', isSecret: true, createdAt: '2026-03-08', reply: null, replyAt: null },
  { id: 3, title: '로그인이 안 됩니다', category: 'ACCOUNT', content: '비밀번호를 잊어버렸습니다. 초기화 부탁드립니다.', status: 'PENDING', isSecret: false, createdAt: '2026-03-12', reply: null, replyAt: null }
])

const filteredList = computed(() => {
  return inquiries.value.filter(i => {
    if (filterStatus.value !== 'ALL' && i.status !== filterStatus.value) return false
    return true
  })
})

const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize))

function statusLabel(s) {
  return { PENDING: '답변 대기', IN_PROGRESS: '처리 중', COMPLETED: '답변 완료' }[s] || s
}

function categoryLabel(c) {
  return { LEARNING: '학습', ACCOUNT: '계정', PAYMENT: '결제', ETC: '기타' }[c] || c
}

function formatDate(d) {
  return d?.slice(0, 10) || ''
}

function openDetail(item) {
  selected.value = item
}

function submitInquiry() {
  if (!form.value.title.trim() || !form.value.content.trim()) return
  inquiries.value.unshift({
    id: Date.now(),
    title: form.value.title,
    category: form.value.category,
    content: form.value.content,
    status: 'PENDING',
    isSecret: form.value.isSecret,
    createdAt: new Date().toISOString().slice(0, 10),
    reply: null,
    replyAt: null
  })
  form.value = { category: 'LEARNING', title: '', content: '', isSecret: false }
  showForm.value = false
  success('문의가 접수되었습니다.')
}
</script>

<style scoped lang="scss">
.filter-bar {
  margin-bottom: $spacing-5;
}

.filter-tabs {
  display: flex;
  gap: $spacing-1;
  background: $bg-light;
  padding: 3px;
  border-radius: $radius-md;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
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
  flex-shrink: 0;

  &.active {
    background: white;
    color: $primary;
    font-weight: 600;
    box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  }
}

.inquiry-list {
  background: white;
  border-radius: $radius-lg;
  border: 1px solid $border;
  overflow: hidden;
  margin-bottom: $spacing-5;
}

.inquiry-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4 $spacing-5;
  border-bottom: 1px solid $border;
  cursor: pointer;
  transition: background 0.15s;
  gap: $spacing-3;

  &:last-child { border-bottom: none; }
  &:hover { background: $bg-light; }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    flex: 1;
    min-width: 0;
  }

  &__category {
    font-size: $font-size-xs;
    color: $text-muted;
    white-space: nowrap;
  }

  &__title {
    font-size: $font-size-sm;
    color: $text-primary;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__right {
    flex-shrink: 0;
  }

  &__date {
    font-size: $font-size-xs;
    color: $text-muted;
  }
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;

  &.pending    { background: #FEF3C7; color: #92400E; }
  &.in_progress { background: #DBEAFE; color: #1E40AF; }
  &.completed  { background: #D1FAE5; color: #065F46; }
}

.empty-state {
  padding: $spacing-12;
  text-align: center;
  color: $text-muted;
  font-size: $font-size-sm;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-3;
}

// 모달
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
  max-width: 560px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);

  &--large { max-width: 680px; }

  &__header {
    padding: $spacing-5 $spacing-6;
    border-bottom: 1px solid $border;
    position: relative;

    h2 { font-size: $font-size-lg; font-weight: 700; margin: 0; }
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: $spacing-1;
  }

  &__meta {
    font-size: $font-size-xs;
    color: $text-muted;
  }

  &__close {
    position: absolute;
    top: $spacing-4;
    right: $spacing-5;
    border: none;
    background: none;
    font-size: 18px;
    color: $text-muted;
    cursor: pointer;
    line-height: 1;
    &:hover { color: $text-primary; }
  }

  &__body {
    padding: $spacing-6;
    overflow-y: auto;
    flex: 1;
  }

  &__footer {
    padding: $spacing-4 $spacing-6;
    border-top: 1px solid $border;
    display: flex;
    justify-content: flex-end;
    gap: $spacing-2;
  }
}

.inquiry-content {
  font-size: $font-size-sm;
  color: $text-primary;
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: $spacing-6;
}

.reply-box {
  background: $bg-light;
  border-radius: $radius-lg;
  border-left: 3px solid $primary;
  padding: $spacing-4 $spacing-5;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-3;
  }

  &__label {
    font-size: $font-size-xs;
    font-weight: 700;
    color: $primary;
  }

  &__date {
    font-size: $font-size-xs;
    color: $text-muted;
  }

  &__content {
    font-size: $font-size-sm;
    color: $text-primary;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

.reply-pending {
  background: #FFFBEB;
  border-radius: $radius-lg;
  padding: $spacing-4 $spacing-5;

  p {
    font-size: $font-size-sm;
    color: #92400E;
    margin: 0;
  }
}

.lock-icon {
  font-size: 13px;
}
</style>
