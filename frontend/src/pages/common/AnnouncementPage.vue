<template>
  <div class="pub-page">

    <!-- 헤더 -->
    <PublicHeader />

    <!-- 히어로 -->
    <div class="pub-hero">
      <div class="pub-container">
        <p class="pub-hero__tag">고객센터</p>
        <h1 class="pub-hero__title">공지사항</h1>
        <p class="pub-hero__desc">AI EDU의 새로운 소식과 업데이트를 확인하세요</p>
      </div>
    </div>

    <!-- 본문 -->
    <div class="pub-body">
      <div class="pub-container">

        <!-- 검색 -->
        <div class="pub-search">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="keyword" type="text" placeholder="공지사항 검색..." />
        </div>

        <!-- 목록 -->
        <div class="ann-list">
          <div
            v-for="ann in filteredAnnouncements"
            :key="ann.id"
            class="ann-item"
            :class="{ expanded: expandedId === ann.id, pinned: ann.pinned }"
            @click="toggleExpand(ann.id)"
          >
            <div class="ann-item__head">
              <div class="ann-item__left">
                <span v-if="ann.pinned" class="badge-pin">공지</span>
                <span v-if="ann.isNew" class="badge-new">NEW</span>
                <span class="ann-item__title">{{ ann.title }}</span>
              </div>
              <div class="ann-item__right">
                <span class="ann-item__date">{{ ann.createdAt }}</span>
                <svg class="ann-item__arrow" :class="{ open: expandedId === ann.id }" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
              </div>
            </div>
            <div v-if="expandedId === ann.id" class="ann-item__body" v-html="ann.content" />

          </div>
          <div v-if="!filteredAnnouncements.length" class="ann-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <p>검색 결과가 없습니다.</p>
          </div>
        </div>

      </div>
    </div>

    <!-- 푸터 -->
    <PublicFooter />

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PublicHeader from '@/components/layout/PublicHeader.vue'
import PublicFooter from '@/components/layout/PublicFooter.vue'
import api from '@/utils/api'

const keyword = ref('')
const expandedId = ref(null)
const announcements = ref([])

onMounted(async () => {
  try {
    const res = await api.get('/announcements')
    const now = new Date()
    announcements.value = (res.data?.content || res.data || []).map(a => ({
      id: a.announcementId, title: a.title, content: a.content,
      createdAt: a.createdAt?.slice(0,10),
      pinned: a.isImportant || a.isPinned || false,
      isNew: a.createdAt ? (now - new Date(a.createdAt)) / (1000*60*60*24) <= 7 : false
    }))
  } catch {}
})

const filteredAnnouncements = computed(() => {
  if (!keyword.value.trim()) return announcements.value
  return announcements.value.filter(a => a.title.includes(keyword.value) || a.content.includes(keyword.value))
})

function toggleExpand(id) {
  expandedId.value = expandedId.value === id ? null : id
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

.pub-search {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  background: white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  padding: $spacing-3 $spacing-5;
  margin-bottom: $spacing-6;
  box-shadow: $shadow-sm;
  color: $text-muted;

  input {
    flex: 1;
    border: none;
    outline: none;
    font-size: $font-size-sm;
    color: $text-primary;
    background: transparent;
    font-family: inherit;

    &::placeholder { color: $text-muted; }
  }
}

// ── 공지 목록 ────────────────────────────────────────
.ann-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.ann-item {
  background: white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: $shadow-md; }

  &.pinned {
    border-color: rgba(59,130,246,0.35);
    background: linear-gradient(to right, #f0f4ff, white);
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-5 $spacing-6;
    gap: $spacing-4;

    @media (max-width: $bp-mobile) {
      flex-direction: column;
      align-items: flex-start;
      padding: $spacing-4;
      gap: $spacing-2;
    }
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    min-width: 0;

    @media (max-width: $bp-mobile) {
      flex-wrap: wrap;
    }
  }

  &__title {
    font-size: $font-size-base;
    font-weight: 600;
    color: $text-primary;
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
      justify-content: space-between;
    }
  }

  &__date {
    font-size: $font-size-sm;
    color: $text-muted;
  }

  &__arrow {
    color: $text-muted;
    transition: transform 0.25s;
    &.open { transform: rotate(180deg); }
  }

  &__body {
    padding: $spacing-5 $spacing-6;
    border-top: 1px solid $border;
    background: $bg-light;

    p {
      font-size: $font-size-sm;
      color: $text-secondary;
      line-height: 1.9;
      white-space: pre-line;
    }
  }
}

.badge-pin {
  flex-shrink: 0;
  background: $primary;
  color: white;
  padding: 2px 8px;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 700;
}

.badge-new {
  flex-shrink: 0;
  background: $danger;
  color: white;
  padding: 2px 6px;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 700;
}

.ann-empty {
  text-align: center;
  padding: 80px 0;
  color: $text-muted;

  svg { margin: 0 auto $spacing-4; display: block; }
  p { font-size: $font-size-sm; }
}

</style>
