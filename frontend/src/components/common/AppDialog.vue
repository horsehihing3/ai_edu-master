<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="state.visible" class="dialog-overlay" @click.self="_cancel">
        <div class="dialog">
          <div class="dialog__icon" :class="state.type">
            <svg v-if="state.type === 'danger'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <p class="dialog__message">{{ state.message }}</p>
          <div class="dialog__actions">
            <button class="btn btn-secondary btn-sm" @click="_cancel">{{ state.cancelText }}</button>
            <button :class="['btn', 'btn-sm', state.type === 'danger' ? 'btn-danger' : 'btn-primary']" @click="_confirm">{{ state.confirmText }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useDialog } from '@/composables/useDialog'
const { state, _confirm, _cancel } = useDialog()
</script>

<style scoped lang="scss">
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-4;
}

.dialog {
  background: white;
  border-radius: $radius-xl;
  padding: $spacing-8 $spacing-6;
  width: 100%;
  max-width: 380px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-4;
  text-align: center;

  &__icon {
    width: 52px;
    height: 52px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    &.danger {
      background: #FEE2E2;
      color: $danger;
    }

    &.default {
      background: $primary-bg;
      color: $primary;
    }
  }

  &__message {
    font-size: $font-size-base;
    color: $text-primary;
    line-height: 1.6;
    white-space: pre-wrap;
  }

  &__actions {
    display: flex;
    gap: $spacing-3;
    margin-top: $spacing-2;
  }
}

.btn-danger {
  background: $danger;
  color: white;
  border-color: $danger;

  &:hover {
    background: darken(#EF4444, 8%);
    border-color: darken(#EF4444, 8%);
  }
}

.dialog-enter-active,
.dialog-leave-active {
  transition: all 0.2s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;

  .dialog {
    transform: scale(0.95) translateY(-8px);
  }
}
</style>
