<template>
  <div class="not-found">
    <div class="not-found__inner">
      <div class="not-found__code">404</div>
      <h1>페이지를 찾을 수 없습니다</h1>
      <p>요청하신 페이지가 존재하지 않거나 이동되었습니다.</p>
      <div class="not-found__actions">
        <button class="btn btn-primary btn-lg" @click="$router.back()">이전 페이지</button>
        <RouterLink to="/" class="btn btn-secondary btn-lg">홈으로</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-bg;

  &__inner {
    text-align: center;
    padding: $spacing-8;
  }

  &__code {
    font-size: 120px;
    font-weight: 900;
    color: $primary-light;
    opacity: 0.3;
    line-height: 1;
    margin-bottom: $spacing-6;
  }

  h1 {
    font-size: $font-size-3xl;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: $spacing-4;
  }

  p {
    font-size: $font-size-lg;
    color: $text-secondary;
    margin-bottom: $spacing-8;
  }

  &__actions {
    display: flex;
    gap: $spacing-4;
    justify-content: center;
  }
}
</style>
