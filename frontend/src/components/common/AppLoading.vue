<template>
  <div :class="['loading-wrap', { overlay: overlay }]">
    <div class="loading-spinner">
      <svg viewBox="0 0 50 50" class="spinner-svg">
        <circle cx="25" cy="25" r="20" fill="none" stroke-width="4" />
      </svg>
    </div>
    <p v-if="message" class="loading-message">{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({
  message: { type: String, default: '' },
  overlay: { type: Boolean, default: false }
})
</script>

<style scoped lang="scss">
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-3;
  padding: $spacing-8;

  &.overlay {
    position: fixed;
    inset: 0;
    background: rgba(255, 255, 255, 0.8);
    z-index: 999;
    padding: 0;
  }
}

.loading-spinner {
  width: 40px;
  height: 40px;
}

.spinner-svg {
  width: 100%;
  height: 100%;
  animation: rotate 1.4s linear infinite;

  circle {
    stroke: $primary-light;
    stroke-linecap: round;
    stroke-dasharray: 80, 200;
    stroke-dashoffset: 0;
    animation: dash 1.4s ease-in-out infinite;
  }
}

.loading-message {
  font-size: $font-size-sm;
  color: $text-secondary;
}

@keyframes rotate {
  100% { transform: rotate(360deg); }
}

@keyframes dash {
  0% { stroke-dasharray: 1, 200; stroke-dashoffset: 0; }
  50% { stroke-dasharray: 89, 200; stroke-dashoffset: -35; }
  100% { stroke-dasharray: 89, 200; stroke-dashoffset: -124; }
}
</style>
