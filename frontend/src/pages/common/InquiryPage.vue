<template>
  <div class="pub-page">

    <!-- 헤더 -->
    <PublicHeader />

    <!-- 히어로 -->
    <div class="pub-hero">
      <div class="pub-container">
        <p class="pub-hero__tag">고객센터</p>
        <h1 class="pub-hero__title">1:1 문의</h1>
        <p class="pub-hero__desc">궁금한 점이 있으시면 언제든지 문의해 주세요</p>
      </div>
    </div>

    <!-- 본문 -->
    <div class="pub-body">
      <div class="pub-container">

        <!-- 문의 작성 버튼 -->
        <div class="inq-top">
          <div class="inq-tabs">
            <button v-for="t in tabs" :key="t.value" :class="['inq-tab', { active: activeTab === t.value }]" @click="activeTab = t.value">
              {{ t.label }}
            </button>
          </div>
          <button class="pub-btn-write" @click="openModal">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            문의 작성
          </button>
        </div>

        <!-- 로그인 안내 -->
        <div v-if="!isLoggedIn" class="inq-login-notice">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <span>내 문의 내역을 확인하려면 <RouterLink to="/login">로그인</RouterLink>이 필요합니다.</span>
        </div>

        <!-- 목록 -->
        <div class="inq-list">
          <div
            v-for="inq in filteredInquiries"
            :key="inq.id"
            class="inq-item"
            :class="{ expanded: expandedId === inq.id }"
            @click="toggleExpand(inq.id)"
          >
            <div class="inq-item__head">
              <div class="inq-item__left">
                <span :class="['inq-status', inq.status]">{{ inq.status === 'answered' ? '답변완료' : '미답변' }}</span>
                <span class="inq-item__title">{{ inq.title }}</span>
              </div>
              <div class="inq-item__right">
                <span class="inq-item__date">{{ inq.createdAt }}</span>
                <svg class="inq-item__arrow" :class="{ open: expandedId === inq.id }" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
              </div>
            </div>
            <div v-if="expandedId === inq.id" class="inq-item__body">
              <div class="inq-question">
                <p>{{ inq.content }}</p>
              </div>
              <div v-if="inq.answer" class="inq-answer">
                <div class="inq-answer__header">
                  <span class="badge-answer">답변</span>
                  <span>{{ inq.answeredAt }}</span>
                </div>
                <p>{{ inq.answer }}</p>
              </div>
              <div v-else class="inq-pending">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                답변 대기 중입니다.
              </div>
            </div>
          </div>

          <div v-if="!filteredInquiries.length" class="inq-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <p>문의 내역이 없습니다.</p>
          </div>
        </div>

      </div>
    </div>

    <!-- 문의 작성 모달 -->
    <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
      <div class="modal-box">
        <div class="modal-box__head">
          <h3>1:1 문의 작성</h3>
          <button class="modal-box__close" @click="showModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="modal-box__body">
          <div class="form-group">
            <label>제목</label>
            <input v-model="form.title" type="text" placeholder="문의 제목을 입력하세요" />
          </div>
          <div class="form-group">
            <label>내용</label>
            <textarea v-model="form.content" rows="6" placeholder="문의 내용을 자세히 작성해주세요" />
          </div>
        </div>
        <div class="modal-box__foot">
          <button class="modal-btn-cancel" @click="showModal = false">취소</button>
          <button class="modal-btn-submit" :disabled="submitting" @click="submitInquiry">
            {{ submitting ? '등록 중...' : '등록하기' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 푸터 -->
    <PublicFooter />

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/store/auth'
import PublicHeader from '@/components/layout/PublicHeader.vue'
import PublicFooter from '@/components/layout/PublicFooter.vue'
import api from '@/utils/api'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isAuthenticated)

const activeTab = ref('all')
const showModal = ref(false)
const submitting = ref(false)
const expandedId = ref(null)
const form = reactive({ title: '', content: '' })

const tabs = [
  { label: '전체', value: 'all' },
  { label: '미답변', value: 'pending' },
  { label: '답변완료', value: 'answered' }
]

const inquiries = ref([])
const filteredInquiries = computed(() => inquiries.value)

async function fetchInquiries() {
  try {
    const params = {}
    if (activeTab.value !== 'all') params.status = activeTab.value
    const res = await api.get('/inquiries', { params })
    inquiries.value = (res.data?.content || res.data || []).map(i => ({
      id: i.inquiryId, title: i.title, content: i.content,
      status: i.status?.toLowerCase() || 'pending',
      createdAt: i.createdAt?.slice(0,10),
      answer: i.answer || null, answeredAt: i.answeredAt?.slice(0,10) || null
    }))
  } catch {}
}

watch(activeTab, fetchInquiries)
onMounted(fetchInquiries)

function toggleExpand(id) {
  expandedId.value = expandedId.value === id ? null : id
}

function openModal() {
  Object.assign(form, { title: '', content: '' })
  showModal.value = true
}

async function submitInquiry() {
  if (!form.title.trim() || !form.content.trim()) return
  submitting.value = true
  try {
    const res = await api.post('/inquiries', { title: form.title, content: form.content })
    const i = res.data || {}
    inquiries.value.unshift({
      id: i.inquiryId || Date.now(), title: form.title, content: form.content,
      status: 'pending', createdAt: new Date().toISOString().slice(0,10), answer: null, answeredAt: null
    })
    showModal.value = false
  } catch {} finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.pub-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Noto Sans KR', sans-serif;
  background: $bg-light;
}

.pub-container {
  max-width: 860px;
  margin: 0 auto;
  padding: 0 $spacing-6;
}

// ── 히어로 ───────────────────────────────────────────
.pub-hero {
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  padding: 60px 0;
  text-align: center;
  color: white;

  &__tag {
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 2px;
    text-transform: uppercase;
    color: rgba(255,255,255,0.65);
    margin-bottom: $spacing-3;
  }

  &__title {
    font-size: clamp(28px, 4vw, 40px);
    font-weight: 900;
    margin-bottom: $spacing-3;
  }

  &__desc {
    font-size: $font-size-base;
    color: rgba(255,255,255,0.75);
  }
}

// ── 본문 ────────────────────────────────────────────
.pub-body {
  flex: 1;
  padding: 48px 0 80px;
}

.inq-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-5;
  gap: $spacing-4;
}

.inq-tabs {
  display: flex;
  gap: $spacing-1;
  background: white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  padding: 4px;
}

.inq-tab {
  padding: $spacing-2 $spacing-4;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-secondary;
  border-radius: $radius-md;
  transition: all $transition-fast;
  background: none;
  border: none;
  cursor: pointer;

  &.active {
    background: $primary;
    color: white;
  }

  &:not(.active):hover { color: $primary; }
}

.pub-btn-write {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-5;
  background: $primary;
  color: white;
  font-size: $font-size-sm;
  font-weight: 700;
  border-radius: $radius-md;
  border: none;
  cursor: pointer;
  transition: background $transition-fast;
  white-space: nowrap;

  &:hover { background: $primary-dark; }
}

.inq-login-notice {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  background: $primary-bg;
  border: 1px solid rgba(59,130,246,0.25);
  border-radius: $radius-lg;
  padding: $spacing-4 $spacing-5;
  margin-bottom: $spacing-5;
  font-size: $font-size-sm;
  color: $text-secondary;

  svg { color: $primary; flex-shrink: 0; }
  a { color: $primary; font-weight: 700; text-decoration: none; &:hover { text-decoration: underline; } }
}

// ── 문의 목록 ────────────────────────────────────────
.inq-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.inq-item {
  background: white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: $shadow-md; }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-5 $spacing-6;
    gap: $spacing-4;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    min-width: 0;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: 600;
    color: $text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-shrink: 0;
  }

  &__date { font-size: $font-size-sm; color: $text-muted; }

  &__arrow {
    color: $text-muted;
    transition: transform 0.25s;
    &.open { transform: rotate(180deg); }
  }

  &__body { border-top: 1px solid $border; }
}

.inq-status {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 700;

  &.answered { background: #D1FAE5; color: #065F46; }
  &.pending { background: #FEF3C7; color: #92400E; }
}

.inq-question {
  padding: $spacing-5 $spacing-6;

  p { font-size: $font-size-sm; color: $text-primary; line-height: 1.8; }
}

.inq-answer {
  padding: $spacing-5 $spacing-6;
  background: $primary-bg;
  border-top: 1px solid rgba(59,130,246,0.15);

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    margin-bottom: $spacing-3;
    font-size: $font-size-xs;
    color: $text-muted;
  }

  p { font-size: $font-size-sm; color: $text-primary; line-height: 1.8; }
}

.inq-pending {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-4 $spacing-6;
  font-size: $font-size-sm;
  color: $text-muted;
  background: $bg-light;
}

.badge-answer {
  background: $primary;
  color: white;
  padding: 2px 7px;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 700;
}

.inq-empty {
  text-align: center;
  padding: 80px 0;
  color: $text-muted;

  svg { margin: 0 auto $spacing-4; display: block; }
  p { font-size: $font-size-sm; }
}

// ── 모달 ────────────────────────────────────────────
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-6;
}

.modal-box {
  background: white;
  border-radius: $radius-xl;
  width: 100%;
  max-width: 560px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.2);

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-6;
    border-bottom: 1px solid $border;

    h3 { font-size: $font-size-xl; font-weight: 800; color: $text-primary; }
  }

  &__close {
    background: none;
    border: none;
    cursor: pointer;
    color: $text-muted;
    padding: $spacing-1;
    border-radius: $radius-sm;
    &:hover { color: $text-primary; background: $bg-light; }
  }

  &__body {
    padding: $spacing-6;
    display: flex;
    flex-direction: column;
    gap: $spacing-5;
  }

  &__foot {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-3;
    padding: $spacing-5 $spacing-6;
    border-top: 1px solid $border;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;

  label {
    font-size: $font-size-sm;
    font-weight: 700;
    color: $text-primary;
  }

  input, textarea {
    width: 100%;
    padding: $spacing-3 $spacing-4;
    border: 1px solid $border;
    border-radius: $radius-md;
    font-size: $font-size-sm;
    font-family: inherit;
    color: $text-primary;
    outline: none;
    transition: border-color $transition-fast;
    box-sizing: border-box;
    resize: vertical;

    &:focus { border-color: $primary; }
    &::placeholder { color: $text-muted; }
  }
}

.modal-btn-cancel {
  padding: $spacing-3 $spacing-6;
  background: $bg-light;
  color: $text-secondary;
  font-size: $font-size-sm;
  font-weight: 600;
  border: 1px solid $border;
  border-radius: $radius-md;
  cursor: pointer;
  transition: background $transition-fast;
  &:hover { background: $border; }
}

.modal-btn-submit {
  padding: $spacing-3 $spacing-6;
  background: $primary;
  color: white;
  font-size: $font-size-sm;
  font-weight: 700;
  border: none;
  border-radius: $radius-md;
  cursor: pointer;
  transition: background $transition-fast;
  &:hover { background: $primary-dark; }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
}

</style>
