<template>
  <nav class="breadcrumb">
    <RouterLink
      v-for="(item, idx) in items"
      :key="idx"
      :to="item.to || ''"
      :class="['breadcrumb__item', { active: idx === items.length - 1 }]"
    >
      {{ item.label }}
      <span v-if="idx < items.length - 1" class="breadcrumb__sep">/</span>
    </RouterLink>
  </nav>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})
</script>

<style scoped lang="scss">
.breadcrumb {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  margin-bottom: $spacing-3;

  &__item {
    font-size: $font-size-sm;
    color: $text-secondary;
    display: flex;
    align-items: center;
    gap: $spacing-1;

    &:hover:not(.active) {
      color: $primary-light;
    }

    &.active {
      color: $text-primary;
      font-weight: 500;
      pointer-events: none;
    }
  }

  &__sep {
    color: $text-muted;
    margin-left: $spacing-1;
  }
}
</style>
