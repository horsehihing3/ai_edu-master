<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/>
        <polyline points="13 2 13 9 20 9"/>
      </svg>
    </div>
    <p class="empty-state__message">{{ message }}</p>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  message: { type: String, default: '데이터가 없습니다.' },
  description: { type: String, default: '' }
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-16 $spacing-8;
  text-align: center;

  &__icon {
    color: $text-muted;
    margin-bottom: $spacing-5;
    opacity: 0.5;
  }

  &__message {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $text-secondary;
    margin-bottom: $spacing-2;
  }

  &__desc {
    font-size: $font-size-sm;
    color: $text-muted;
    margin-bottom: $spacing-5;
  }
}
</style>
