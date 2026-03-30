<template>
  <div class="video-detail">
    <AppBreadcrumb :items="[{ label: '동영상 풀이', to: '/student/videos' }, { label: video.title }]" />

    <div class="video-layout">
      <div class="video-main">
        <!-- 동영상 플레이어 -->
        <div class="player-wrap">
          <template v-if="video.videoType === 'YOUTUBE' || video.videoType === 'VIMEO'">
            <iframe
              :src="video.videoUrl"
              class="youtube-iframe"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowfullscreen
            />
          </template>
          <template v-else>
            <video ref="videoEl" class="plyr-video" playsinline controls>
              <source :src="video.cdnUrl || video.videoUrl" :type="videoMimeType(video.videoUrl)" />
            </video>
          </template>
        </div>

        <div class="video-info card">
          <div class="video-info__header">
            <div>
              <div class="meta">
                <span v-if="video.subject" class="subject-tag">{{ video.subject }}</span>
                <AppBadge v-if="video.level" :type="video.level" />
              </div>
              <h1>{{ video.title }}</h1>
              <p v-if="video.unit" class="unit">{{ video.unit }}</p>
            </div>
          </div>

          <!-- 관련 문제: problem_id 연결된 경우만 표시 -->
          <div v-if="video.problemText" class="related-problem">
            <h3>관련 문제</h3>
            <div class="problem-box">
              <p class="problem-text">{{ video.problemText }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 사이드 -->
      <div class="video-side">
        <!-- 해설: 있을 때만 표시 -->
        <div v-if="video.explanation" class="card">
          <h3 class="side-title">해설</h3>
          <div class="explanation-content">
            <p>{{ video.explanation }}</p>
          </div>
        </div>

        <div v-if="relatedVideos.length" class="card">
          <h3 class="side-title">관련 영상</h3>
          <div class="related-list">
            <RouterLink
              v-for="r in relatedVideos"
              :key="r.id"
              :to="`/student/videos/${r.id}`"
              class="related-item"
            >
              <img :src="r.thumbnail || 'https://placehold.co/80x45?text=V'" :alt="r.title" />
              <div>
                <p>{{ r.title }}</p>
              </div>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import AppBreadcrumb from '@/components/common/AppBreadcrumb.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import api from '@/utils/api'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const authStore = useAuthStore()
const videoEl = ref(null)
let player = null

const video = ref({ id: null, title: '', subject: '', level: '', unit: '', videoUrl: '', problemText: '', problemLatex: '', explanation: '' })
const relatedVideos = ref([])

onMounted(async () => {
  try {
    const [videoRes, relatedRes] = await Promise.all([
      api.get(`/videos/${route.params.id}`),
      api.get(`/videos/${route.params.id}/related`)
    ])
    const v = videoRes.data || {}
    video.value = {
      id: v.videoId, title: v.title, subject: v.subject || '', level: v.level || '',
      unit: v.unitName || v.unit || '', videoType: v.videoType || '',
      videoUrl: v.videoUrl || v.cdnUrl || '', cdnUrl: v.cdnUrl || '',
      problemText: v.questionText || '', problemLatex: '',
      explanation: v.explanation || ''
    }
    relatedVideos.value = (relatedRes.data || []).map(r => ({
      id: r.videoId, title: r.title, duration: r.duration || '', thumbnail: r.thumbnailUrl || ''
    }))
  } catch (e) {
    console.error('영상 로드 실패:', e)
  }

  // [2026-03-30] 로그인 사용자의 DIRECT 시청 이력 저장
  if (authStore.isAuthenticated) {
    try {
      await api.post(`/videos/${route.params.id}/watch`, { source: 'DIRECT' })
    } catch {
      // 이력 저장 실패해도 영상 재생에는 영향 없음
    }
  }

  await nextTick()

  try {
    const Plyr = (await import('plyr')).default
    await import('plyr/dist/plyr.css')
    if (videoEl.value) {
      player = new Plyr(videoEl.value, {
        controls: ['play-large', 'play', 'progress', 'current-time', 'mute', 'volume', 'settings', 'fullscreen'],
        i18n: { play: '재생', pause: '일시정지', mute: '음소거', settings: '설정', fullscreen: '전체화면' }
      })
    }
  } catch (e) {
    console.log('Plyr 로드 실패:', e)
  }
})

onUnmounted(() => {
  if (player) player.destroy()
})

function isYoutube(url) {
  return url && (url.includes('youtube.com') || url.includes('youtu.be'))
}

function toYoutubeEmbed(url) {
  if (!url) return ''
  const match = url.match(/(?:v=|youtu\.be\/)([^&?/]+)/)
  const id = match ? match[1] : ''
  return `https://www.youtube.com/embed/${id}`
}

function videoMimeType(url) {
  if (!url) return 'video/mp4'
  if (url.endsWith('.webm')) return 'video/webm'
  if (url.endsWith('.ogg') || url.endsWith('.ogv')) return 'video/ogg'
  if (url.endsWith('.mov')) return 'video/mp4'
  return 'video/mp4'
}
</script>

<style scoped lang="scss">
.video-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.video-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: $spacing-6;
  align-items: start;

  @media (max-width: $bp-tablet) {
    grid-template-columns: 1fr;
  }
}

.player-wrap {
  background: #000;
  border-radius: $radius-lg;
  overflow: hidden;
  margin-bottom: $spacing-5;
  aspect-ratio: 16 / 9;

  .plyr-video {
    width: 100%;
    height: 100%;
    display: block;
  }

  .youtube-iframe {
    width: 100%;
    height: 100%;
    display: block;
  }
}

.video-info {
  &__header {
    margin-bottom: $spacing-5;

    .meta {
      display: flex;
      gap: $spacing-2;
      margin-bottom: $spacing-3;
    }

    h1 {
      font-size: $font-size-2xl;
      font-weight: 700;
      margin-bottom: $spacing-2;
    }

    .unit {
      font-size: $font-size-sm;
      color: $text-muted;
    }
  }
}

.related-problem {
  border-top: 1px solid $border;
  padding-top: $spacing-5;

  h3 {
    font-size: $font-size-base;
    font-weight: 700;
    margin-bottom: $spacing-4;
  }
}

.problem-box {
  background: $bg-light;
  border-radius: $radius-md;
  padding: $spacing-5;

  .problem-text {
    font-size: $font-size-base;
    color: $text-primary;
    line-height: 1.7;
  }

  .latex-wrap {
    margin-top: $spacing-3;
    text-align: center;
    font-size: $font-size-xl;
    color: $text-primary;
  }
}

.side-title {
  font-size: $font-size-base;
  font-weight: 700;
  margin-bottom: $spacing-4;
}

.explanation-content {
  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: 1.8;
  }
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.related-item {
  display: flex;
  gap: $spacing-3;
  text-decoration: none;
  padding: $spacing-2;
  border-radius: $radius-sm;
  transition: background $transition-fast;

  &:hover { background: $bg-light; }

  img {
    width: 80px;
    height: 45px;
    border-radius: $radius-sm;
    object-fit: cover;
    flex-shrink: 0;
    background: $border;
  }

  p {
    font-size: $font-size-sm;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: $spacing-1;
    line-height: 1.3;
  }

  span {
    font-size: $font-size-xs;
    color: $text-muted;
  }
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
