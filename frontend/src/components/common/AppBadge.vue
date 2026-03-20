<template>
  <span v-if="displayText" class="app-badge" :style="badgeStyle">{{ displayText }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: '' },
  text: { type: String, default: '' }
})

const labelMap = { A: 'A 레벨', B: 'B 레벨', C: 'C 레벨', ALL: '전체' }

const colorMap = {
  A:       { background: '#2563EB', color: '#FFFFFF' },
  B:       { background: '#16A34A', color: '#FFFFFF' },
  C:       { background: '#DC2626', color: '#FFFFFF' },
  ALL:     { background: '#6B7280', color: '#FFFFFF' },
  success: { background: '#D1FAE5', color: '#065F46' },
  warning: { background: '#FEF3C7', color: '#92400E' },
  danger:  { background: '#FEE2E2', color: '#991B1B' },
  info:    { background: '#EFF6FF', color: '#2563EB' },
}

const displayText = computed(() => {
  if (props.text) return props.text
  return labelMap[props.type] || ''
})

const badgeStyle = computed(() => {
  return colorMap[props.type] || { background: '#E5E7EB', color: '#374151' }
})
</script>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.3px;
  white-space: nowrap;
}
</style>
