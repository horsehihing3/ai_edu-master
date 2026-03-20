<template>
  <aside :class="['app-sidebar', { open: uiStore.sidebarOpen }]">
    <nav class="sidebar-nav">
      <div v-for="group in menuGroups" :key="group.label" class="sidebar-group">
        <p v-if="group.label" class="sidebar-group__label">{{ group.label }}</p>
        <RouterLink
          v-for="item in group.items"
          :key="item.to"
          :to="item.to"
          :class="['sidebar-link', { active: isActive(item) }]"
          @click="uiStore.closeSidebar()"
        >
          <span class="sidebar-link__icon" v-html="item.icon" />
          <span>{{ item.label }}</span>
          <span v-if="item.badge" class="sidebar-link__badge">{{ item.badge }}</span>
        </RouterLink>
      </div>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useUiStore } from '@/store/ui'

const authStore = useAuthStore()
const uiStore = useUiStore()
const route = useRoute()

const icons = {
  home: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
  book: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>`,
  video: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>`,
  bookmark: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>`,
  chart: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`,
  msg: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`,
  settings: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 0-14.14 0M4.93 19.07a10 10 0 0 0 14.14 0"/></svg>`,
  users: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
  task: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>`,
  db: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>`,
  dollar: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
  bell: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>`,
  school: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>`,
  activity: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>`,
  code: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`
}

const studentMenu = [
  { label: '홈', to: '/student/home', icon: icons.home },
  { label: '진단 테스트', to: '/student/diagnosis', icon: icons.activity },
  { label: '문제 풀기', to: '/student/learn', icon: icons.book },
  { label: '동영상 풀이', to: '/student/videos', icon: icons.video },
  { label: '오답노트', to: '/student/wrong-notes', icon: icons.bookmark },
  { label: '학습 리포트', to: '/student/report', icon: icons.chart }
]

const teacherMenu = [
  { label: '홈', to: '/teacher/home', icon: icons.home },
  { label: '학생 관리', to: '/teacher/students', icon: icons.users },
  { label: '과제 관리', to: '/teacher/assignments', icon: icons.task },
  { label: '학습 현황', to: '/teacher/analytics', icon: icons.chart }
]

const adminMenu = [
  {
    label: '관리',
    items: [
      { label: '대시보드', to: '/admin/dashboard', icon: icons.home },
      { label: '회원 관리', to: '/admin/members', icon: icons.users },
      { label: '학원/학교 관리', to: '/admin/schools', icon: icons.school },
      { label: '문제 DB', to: '/admin/problems', icon: icons.db },
      { label: '동영상 관리', to: '/admin/videos', icon: icons.video },
      { label: '결제/구독 관리', to: '/admin/payments', icon: icons.dollar },
      { label: '시스템 분석', to: '/admin/analytics', icon: icons.activity },
      { label: '1:1 문의 관리', to: '/admin/inquiries', icon: icons.msg },
      { label: '코드 관리', to: '/admin/codes', icon: icons.code }
    ]
  }
]

const menuGroups = computed(() => {
  const role = authStore.user?.role

  const common = [
    { label: '', items: [
      { label: '공지사항', to: '/notices', icon: icons.bell },
      { label: '설정', to: '/settings', icon: icons.settings }
    ]}
  ]

  if (role === 'STUDENT') {
    return [{ label: '', items: studentMenu }, ...common]
  }
  if (role === 'TEACHER' || role === 'SUPER_USER') {
    return [{ label: '', items: teacherMenu }, ...common]
  }
  if (role === 'ADMIN') {
    return [...adminMenu, ...common]
  }
  return []
})

function isActive(item) {
  return route.path.startsWith(item.to)
}
</script>

<style scoped lang="scss">
.sidebar-nav {
  padding: $spacing-4 0;
}

.sidebar-group {
  margin-bottom: $spacing-3;

  &__label {
    font-size: $font-size-xs;
    font-weight: 600;
    color: $text-muted;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    padding: $spacing-2 $spacing-5;
  }
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3 $spacing-5;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-secondary;
  text-decoration: none;
  transition: all $transition-fast;
  border-left: 3px solid transparent;
  margin: 1px 0;

  &:hover {
    background: $primary-bg;
    color: $primary;
  }

  &.active {
    background: $primary-bg;
    color: $primary;
    border-left-color: $primary-light;
    font-weight: 600;
  }

  &__icon {
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }

  &__badge {
    margin-left: auto;
    background: $danger;
    color: white;
    border-radius: $radius-full;
    font-size: 10px;
    font-weight: 700;
    min-width: 18px;
    height: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 4px;
  }
}
</style>
