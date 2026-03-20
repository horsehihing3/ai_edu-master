<template>
  <div class="video-list-page">
    <div class="page-header"><h1>동영상 풀이</h1></div>

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
    <div class="video-grid">
      <RouterLink
        v-for="v in filteredVideos"
        :key="v.id"
        :to="`/student/videos/${v.id}`"
        class="video-card"
      >
        <div class="video-card__thumb">
          <img :src="v.thumbnail || 'https://placehold.co/320x180?text=Video'" :alt="v.title" />
          <span class="duration-badge">{{ v.duration }}</span>
        </div>
        <div class="video-card__info">
          <div class="meta">
            <span v-if="v.subject" class="subject-tag">{{ v.subject }}</span>
            <AppBadge v-if="v.level" :type="v.level" />
          </div>
          <h3>{{ v.title }}</h3>
          <p>{{ v.unit }}</p>
        </div>
      </RouterLink>
    </div>
    <AppEmpty v-if="!filteredVideos.length" message="동영상이 없습니다." />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import api from '@/utils/api'

const filterSubject = ref('')
const filterLevel = ref('')

const subjectOptions = ['수학','영어','국어','과학','사회'].map(v => ({ value: v, label: v }))

const allVideos = ref([])
const filteredVideos = computed(() => {
  return allVideos.value.filter(v => {
    if (filterSubject.value && (v.subject || '') !== filterSubject.value) return false
    if (filterLevel.value && (v.level || '') !== filterLevel.value) return false
    return true
  })
})

async function fetchVideos() {
  try {
    const res = await api.get('/videos', { params: { size: 200 } })
    allVideos.value = (res.data?.content || res.data || []).map(v => ({
      id: v.videoId, subject: v.subject, level: v.level,
      title: v.title, unit: v.unitName || v.unit,
      thumbnail: v.thumbnailUrl || '', duration: v.duration || ''
    }))
  } catch {}
}

onMounted(fetchVideos)
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

.video-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-5;

  @media (max-width: $bp-tablet) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: $bp-mobile) { grid-template-columns: 1fr; }
}

.video-card {
  text-decoration: none;
  background: $bg-white;
  border-radius: $radius-lg;
  border: 1px solid $border;
  overflow: hidden;
  transition: all $transition-base;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &__thumb {
    position: relative;
    aspect-ratio: 16 / 9;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__info {
    padding: $spacing-4;

    .meta {
      display: flex;
      align-items: center;
      gap: $spacing-2;
      margin-bottom: $spacing-2;
    }

    h3 {
      font-size: $font-size-sm;
      font-weight: 700;
      color: $text-primary;
      margin-bottom: $spacing-1;
      line-height: 1.4;
    }

    p {
      font-size: $font-size-xs;
      color: $text-muted;
    }
  }
}

.duration-badge {
  position: absolute;
  bottom: $spacing-2;
  right: $spacing-2;
  background: rgba(0,0,0,0.7);
  color: white;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 600;
}

.subject-tag {
  background: $primary-bg;
  color: $primary;
  padding: 2px $spacing-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  font-weight: 600;
}
</style>
