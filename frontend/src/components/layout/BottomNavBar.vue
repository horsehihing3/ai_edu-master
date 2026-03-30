<template>
  <nav class="bottom-nav-bar">
    <button
      v-for="tab in tabs"
      :key="tab.to"
      :class="['bottom-nav-tab', { active: isActive(tab) }]"
      @click="navigate(tab.to)"
    >
      <span class="bottom-nav-tab__icon" v-html="tab.icon" />
      <span class="bottom-nav-tab__label">{{ tab.label }}</span>
    </button>
  </nav>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUiStore } from '@/store/ui'

const router = useRouter()
const route = useRoute()
const uiStore = useUiStore()

const tabs = [
  {
    label: '홈',
    to: '/student/home',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`
  },
  {
    label: '문제 풀기',
    to: '/student/learn',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>`
  },
  {
    label: '동영상',
    to: '/student/videos',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>`
  },
  {
    label: '오답노트',
    to: '/student/wrong-notes',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>`
  },
  {
    label: '설정',
    to: '/settings',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 0-14.14 0M4.93 19.07a10 10 0 0 0 14.14 0"/></svg>`
  }
]

function isActive(tab) {
  return route.path.startsWith(tab.to)
}

function navigate(to) {
  uiStore.closeSidebar()
  router.push(to)
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.bottom-nav-bar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: $bg-white;
  border-top: 1px solid $border;
  z-index: 100;

  @media (max-width: $bp-mobile) {
    display: flex;
  }
}

.bottom-nav-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: #888;
  padding: 0;
  transition: color $transition-fast;

  &.active {
    color: $primary;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__label {
    font-size: 10px;
    margin-top: 3px;
    line-height: 1;
  }
}
</style>
