<template>
  <div class="auth-root">

    <!-- 전체 배경 -->
    <div class="auth-root__bg" />

    <!-- 헤더 -->
    <PublicHeader />

    <!-- 배경 장식 -->
    <div class="auth-root__deco auth-root__deco--tl" />
    <div class="auth-root__deco auth-root__deco--br" />
    <div class="auth-root__deco auth-root__deco--mid" />

    <!-- 카드 -->
    <div class="auth-layout">

      <!-- 좌측 브랜드 패널 (PC only) -->
      <div class="auth-brand">
        <div class="auth-brand__inner">
          <RouterLink to="/" class="auth-brand__logo">
            <span class="logo-icon">AI</span>
            <span class="logo-text">EDU</span>
          </RouterLink>
          <h2 class="auth-brand__title">
            AI가 설계하는<br />
            <em>맞춤형 학습 플랫폼</em>
          </h2>
          <p class="auth-brand__desc">
            진단 테스트부터 AI 과제 배정까지,<br />
            국어·영어·수학·과학·사회 전 교과를 지원합니다.
          </p>
          <ul class="auth-brand__features">
            <li>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              AI 기반 수준 진단 및 맞춤 과제 배정
            </li>
            <li>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              전국 기출문제 DB 50,000+ 문항
            </li>
            <li>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              학생 성취도 리포트 자동 생성
            </li>
            <li>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              공교육 농어촌 학교 특화 설계
            </li>
          </ul>
        </div>
      </div>

      <!-- 우측 폼 패널 -->
      <div class="auth-form-panel">
        <div class="auth-form-panel__inner">
          <!-- 모바일 로고 -->
          <RouterLink to="/" class="auth-mobile-logo">
            <span class="logo-icon">AI</span>
            <span class="logo-text">EDU</span>
          </RouterLink>

          <div class="auth-card">
            <RouterView />
          </div>

          <p class="auth-footer">
            &copy; 2026 AI EDU. All rights reserved.
          </p>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { onMounted, nextTick } from 'vue'
import PublicHeader from '@/components/layout/PublicHeader.vue'

onMounted(async () => {
  await nextTick()
  window.scrollTo({ top: 0, left: 0, behavior: 'instant' })
})
</script>

<style scoped lang="scss">
// 전체화면 배경
.auth-root {
  min-height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow-x: hidden;

  &__bg {
    position: fixed;
    inset: 0;
    background: linear-gradient(135deg, #e0e7ff 0%, #f0f4ff 40%, #e0f2fe 100%);
    z-index: 0;
  }

  &__deco {
    position: absolute;
    border-radius: 50%;
    pointer-events: none;

    &--tl {
      width: 600px;
      height: 600px;
      top: -200px;
      left: -200px;
      background: radial-gradient(circle, rgba(99,102,241,0.12) 0%, transparent 70%);
    }
    &--br {
      width: 500px;
      height: 500px;
      bottom: -160px;
      right: -160px;
      background: radial-gradient(circle, rgba(59,130,246,0.12) 0%, transparent 70%);
    }
    &--mid {
      width: 300px;
      height: 300px;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: radial-gradient(circle, rgba(14,165,233,0.07) 0%, transparent 70%);
    }
  }
}

// ── 카드 컨테이너 ─────────────────────────────────────
.auth-layout {
  width: calc(100% - #{$spacing-4} * 2);
  max-width: 1080px;
  display: flex;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: 0 8px 48px rgba(0,0,0,0.14);
  margin: auto;
  position: relative;
  z-index: 1;
}

// ── 좌측 브랜드 패널 ─────────────────────────────────
.auth-brand {
  display: none;

  @media (min-width: $bp-tablet) {
    display: flex;
    flex: 0 0 420px;
    align-items: center;
    justify-content: center;
    background: linear-gradient(145deg, #0f172a 0%, #1e3a8a 50%, #1d4ed8 100%);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      width: 500px; height: 500px;
      border-radius: 50%;
      background: rgba(255,255,255,0.04);
      top: -150px; left: -150px;
    }
    &::after {
      content: '';
      position: absolute;
      width: 350px; height: 350px;
      border-radius: 50%;
      background: rgba(255,255,255,0.04);
      bottom: -100px; right: -100px;
    }
  }

  &__inner {
    position: relative;
    z-index: 1;
    padding: $spacing-10 $spacing-8;
    color: white;
  }

  &__logo {
    display: inline-flex;
    align-items: center;
    gap: $spacing-2;
    text-decoration: none;
    margin-bottom: $spacing-10;

    .logo-icon {
      background: white;
      color: $primary;
      padding: 6px 10px;
      border-radius: $radius-sm;
      font-size: $font-size-lg;
      font-weight: 900;
    }
    .logo-text {
      font-size: $font-size-2xl;
      font-weight: 800;
      color: white;
    }
  }

  &__title {
    font-size: clamp(22px, 2vw, 28px);
    font-weight: 900;
    line-height: 1.35;
    margin-bottom: $spacing-4;
    color: white;

    em {
      font-style: normal;
      background: linear-gradient(90deg, #93c5fd, #a5f3fc);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }

  &__desc {
    font-size: $font-size-sm;
    color: rgba(255,255,255,0.65);
    line-height: 1.8;
    margin-bottom: $spacing-8;
  }

  &__features {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: $spacing-3;

    li {
      display: flex;
      align-items: center;
      gap: $spacing-3;
      font-size: $font-size-sm;
      color: rgba(255,255,255,0.85);

      svg { flex-shrink: 0; color: #34d399; }
    }
  }
}

// ── 우측 폼 패널 ─────────────────────────────────────
.auth-form-panel {
  flex: 1;
  display: flex;
  align-items: stretch;
  justify-content: center;
  background: white;
  padding: $spacing-8 $spacing-6;
  min-width: 0;

  @media (max-width: $bp-mobile) {
    padding: $spacing-6 $spacing-4;
  }

  &__inner {
    width: 100%;
    max-width: 420px;
    display: flex;
    flex-direction: column;
  }
}

.auth-mobile-logo {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  text-decoration: none;
  margin-bottom: $spacing-6;

  @media (min-width: $bp-tablet) { display: none; }

  .logo-icon {
    background: $primary;
    color: white;
    padding: 5px 9px;
    border-radius: $radius-sm;
    font-size: $font-size-base;
    font-weight: 900;
  }
  .logo-text {
    font-size: $font-size-xl;
    font-weight: 800;
    color: $primary;
  }
}

.auth-card {
  background: white;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.auth-footer {
  text-align: center;
  margin-top: $spacing-6;
  font-size: $font-size-xs;
  color: $text-muted;
}
</style>
