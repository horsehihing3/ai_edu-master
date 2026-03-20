<template>
  <RouterView />
  <AppToast />
  <AppDialog />

  <!-- 글로벌 로딩 오버레이 -->
  <Transition name="loading-fade">
    <div v-if="ui.isLoading" class="global-loading-overlay">
      <div class="global-loading-spinner">
        <svg viewBox="0 0 50 50" class="global-loading-svg">
          <circle cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round" />
        </svg>
        <span>로딩 중...</span>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import AppToast from '@/components/common/AppToast.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useUiStore } from '@/store/ui'

const ui = useUiStore()
</script>

<style lang="scss">
.global-loading-overlay {
  position: fixed;
  inset: 0;
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(2px);
  pointer-events: all;
}

.global-loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;

  span {
    font-size: 14px;
    color: #475569;
    font-weight: 500;
  }
}

.global-loading-svg {
  width: 44px;
  height: 44px;
  color: #3b82f6;
  animation: global-spin 1s linear infinite;

  circle {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: 0;
    animation: global-dash 1.5s ease-in-out infinite;
  }
}

@keyframes global-spin {
  100% { transform: rotate(360deg); }
}

@keyframes global-dash {
  0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; }
  50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; }
  100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; }
}

.loading-fade-enter-active,
.loading-fade-leave-active {
  transition: opacity 0.2s ease;
}
.loading-fade-enter-from,
.loading-fade-leave-to {
  opacity: 0;
}
</style>
