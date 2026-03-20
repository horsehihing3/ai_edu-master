<template>
  <div class="pagination" v-if="totalPages > 1">
    <button
      class="pagination__btn"
      :disabled="currentPage === 1"
      @click="$emit('page-change', currentPage - 1)"
    >
      &laquo;
    </button>
    <button
      v-for="page in visiblePages"
      :key="page"
      :class="['pagination__btn', { active: page === currentPage }]"
      @click="$emit('page-change', page)"
    >
      {{ page }}
    </button>
    <button
      class="pagination__btn"
      :disabled="currentPage === totalPages"
      @click="$emit('page-change', currentPage + 1)"
    >
      &raquo;
    </button>
    <span class="pagination__info">
      총 {{ totalElements.toLocaleString() }}건
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, default: 1 },
  totalPages: { type: Number, default: 1 },
  totalElements: { type: Number, default: 0 },
  size: { type: Number, default: 10 }
})

defineEmits(['page-change'])

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, props.currentPage - 2)
  const end = Math.min(props.totalPages, props.currentPage + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
</script>

<style scoped lang="scss">
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-1;
  margin-top: $spacing-6;
  flex-wrap: wrap;

  @media (max-width: $bp-mobile) {
    gap: 4px;
  }

  &__btn {
    min-width: 36px;
    height: 36px;
    padding: 0 $spacing-2;
    border-radius: $radius-sm;
    border: 1.5px solid $border;
    background: $bg-white;
    color: $text-secondary;
    font-size: $font-size-sm;
    font-weight: 500;
    transition: all $transition-fast;
    cursor: pointer;
    flex-shrink: 0;

    @media (max-width: $bp-mobile) {
      min-width: 32px;
      height: 32px;
      font-size: $font-size-xs;
      padding: 0 4px;

      &.active {
        background: $primary;
        border-color: $primary;
        color: #fff !important;
        font-weight: 700;
      }
    }

    &:hover:not(:disabled) {
      border-color: $primary-light;
      color: $primary-light;
    }

    &.active {
      background: $primary;
      border-color: $primary;
      color: #fff;
      font-weight: 700;
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }

  &__info {
    margin-left: $spacing-3;
    font-size: $font-size-sm;
    color: $text-muted;

    @media (max-width: $bp-mobile) {
      width: 100%;
      text-align: center;
      margin-left: 0;
      margin-top: $spacing-2;
    }
  }
}
</style>
