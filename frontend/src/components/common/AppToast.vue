<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="['toast', `toast-${toast.type}`]"
          @click="hideToast(toast.id)"
        >
          <span class="toast__icon">
            <svg v-if="toast.type === 'success'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            <svg v-else-if="toast.type === 'error'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            <svg v-else-if="toast.type === 'warning'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </span>
          <span class="toast__message">{{ toast.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useUiStore } from '@/store/ui'

const uiStore = useUiStore()
const toasts = computed(() => uiStore.toasts)

function hideToast(id) {
  uiStore.hideToast(id)
}
</script>

<style scoped lang="scss">
.toast-container {
  position: fixed;
  top: 72px;
  right: $spacing-5;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  max-width: 360px;
  width: calc(100% - #{$spacing-10});
}

.toast {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-4 $spacing-5;
  border-radius: $radius-md;
  box-shadow: $shadow-md;
  cursor: pointer;
  border-left: 4px solid transparent;

  &-success {
    background: #ECFDF5;
    border-left-color: $success;
    color: #065F46;
  }

  &-error {
    background: #FEF2F2;
    border-left-color: $danger;
    color: #991B1B;
  }

  &-warning {
    background: #FFFBEB;
    border-left-color: $warning;
    color: #92400E;
  }

  &-info {
    background: $primary-bg;
    border-left-color: $primary-light;
    color: $primary;
  }

  &__icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }

  &__message {
    font-size: $font-size-sm;
    font-weight: 500;
    line-height: 1.4;
  }
}

// 트랜지션
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(20px);
  max-height: 0;
  margin: 0;
  padding: 0;
}
</style>
