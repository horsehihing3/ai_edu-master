<template>
  <header class="lnb-header" :class="headerClass">
    <div class="lnb-header__inner">
      <RouterLink to="/" class="lnb-logo">
        <span class="lnb-logo__icon">AI</span>
        <span class="lnb-logo__text">EDU</span>
      </RouterLink>

      <nav class="lnb-nav">
        <ul class="lnb-nav__list">
          <li
            v-for="menu in navMenus"
            :key="menu.label"
            class="lnb-nav__item"
            @mouseenter="openDrop(menu.label)"
            @mouseleave="closeDrop"
          >
            <a :href="menu.anchor || '#'" class="lnb-nav__link">
              {{ menu.label }}
              <svg v-if="menu.children" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
            </a>
            <ul v-if="menu.children" class="lnb-nav__drop" :class="{ show: activeDrop === menu.label }">
              <li v-for="child in menu.children" :key="child.label">
                <RouterLink v-if="child.to" :to="child.to" class="lnb-nav__drop-link">{{ child.label }}</RouterLink>
                <a v-else :href="child.anchor || '#'" class="lnb-nav__drop-link">{{ child.label }}</a>
              </li>
            </ul>
          </li>
        </ul>
      </nav>

      <div class="lnb-header__actions">
        <template v-if="isLoggedIn">
          <RouterLink :to="homeRoute" class="lnb-btn-trial">대시보드</RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/login" class="lnb-btn-login">로그인</RouterLink>
          <RouterLink to="/register" class="lnb-btn-trial">무료 체험 시작</RouterLink>
        </template>
        <button class="lnb-ham" @click="mobileNavOpen = !mobileNavOpen">
          <span /><span /><span />
        </button>
      </div>
    </div>

    <div class="lnb-mobile-nav" :class="{ open: mobileNavOpen }">
      <ul>
        <li v-for="menu in navMenus" :key="menu.label">
          <button class="lnb-mobile-nav__title" @click="toggleMobileDrop(menu.label)">
            {{ menu.label }}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
          </button>
          <ul v-if="menu.children && mobileDrop === menu.label" class="lnb-mobile-nav__sub">
            <li v-for="child in menu.children" :key="child.label">
              <RouterLink v-if="child.to" :to="child.to" @click="mobileNavOpen = false">{{ child.label }}</RouterLink>
              <a v-else :href="child.anchor || '#'" @click="mobileNavOpen = false">{{ child.label }}</a>
            </li>
          </ul>
        </li>
      </ul>
      <div class="lnb-mobile-nav__btns">
        <template v-if="isLoggedIn">
          <RouterLink :to="homeRoute" class="lnb-btn-trial" @click="mobileNavOpen = false">대시보드</RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/login" class="lnb-btn-login" @click="mobileNavOpen = false">로그인</RouterLink>
          <RouterLink to="/register" class="lnb-btn-trial" @click="mobileNavOpen = false">무료 체험 시작</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isAuthenticated)
const homeRoute = computed(() => {
  const role = authStore.user?.role
  if (role === 'TEACHER') return '/teacher/home'
  if (role === 'ADMIN') return '/admin/dashboard'
  return '/student/home'
})

const props = defineProps({
  transparent: { type: Boolean, default: false }
})

const isScrolled = ref(false)
function onScroll() { isScrolled.value = window.scrollY > 60 }
onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const headerClass = computed(() => {
  if (props.transparent) {
    return { scrolled: isScrolled.value, 'on-video': !isScrolled.value, 'is-fixed': true }
  }
  return { scrolled: true, 'on-video': false, 'is-fixed': false }
})

const activeDrop = ref(null)
let closeTimer = null
function openDrop(label) {
  if (closeTimer) { clearTimeout(closeTimer); closeTimer = null }
  activeDrop.value = label
}
function closeDrop() {
  closeTimer = setTimeout(() => { activeDrop.value = null }, 150)
}

const mobileNavOpen = ref(false)
const mobileDrop = ref(null)
function toggleMobileDrop(label) { mobileDrop.value = mobileDrop.value === label ? null : label }

const navMenus = [
  {
    label: '플랫폼 소개',
    anchor: '/#intro',
    children: [
      { label: '서비스 소개', anchor: '/#intro' },
      { label: '강사진 소개', anchor: '/#teachers' },
      { label: '이용 대상', anchor: '/#guide' }
    ]
  },
  {
    label: '기능 & 후기',
    anchor: '/#features',
    children: [
      { label: '주요 기능', anchor: '/#features' },
      { label: '수강 후기', anchor: '/#reviews' }
    ]
  },
  {
    label: '커리큘럼',
    anchor: '/#curriculum',
    children: [
      { label: '레벨 시스템', anchor: '/#curriculum' }
    ]
  },
  {
    label: '이용 안내',
    anchor: '/#guide',
    children: [
      { label: '이용 가이드', anchor: '/#guide' },
      { label: 'FAQ', anchor: '/#faq' }
    ]
  },
  {
    label: '고객센터',
    anchor: '#',
    children: [
      { label: '공지사항', to: '/announcements' }
    ]
  }
]
</script>

<style scoped lang="scss">
.lnb-header {
  position: sticky;
  top: 0;
  z-index: 200;
  width: 100%;
  transition: background 0.4s, box-shadow 0.4s;

  &.is-fixed {
    position: fixed;
    left: 0; right: 0;
  }

  &.on-video {
    background: transparent;

    .lnb-header__inner {
      .lnb-nav__link { color: rgba(255,255,255,0.85); &:hover { color: white; background: rgba(255,255,255,0.12); } }
      .lnb-btn-login { color: rgba(255,255,255,0.85); &:hover { color: white; } }
      .lnb-logo__text { color: white; }
      .lnb-ham span { background: white; }
    }
  }

  &.scrolled {
    background: rgba(255,255,255,0.98);
    box-shadow: 0 2px 20px rgba(0,0,0,0.08);
  }

  &__inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-6;
    height: 68px;
    display: flex;
    align-items: center;
    gap: $spacing-8;
  }

  &__actions {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-shrink: 0;

    .lnb-btn-login,
    .lnb-btn-trial {
      @media (max-width: $bp-tablet) { display: none; }
    }
  }
}

.lnb-logo {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  text-decoration: none;
  flex-shrink: 0;

  &__icon {
    background: $primary;
    color: white;
    padding: 5px 10px;
    border-radius: $radius-sm;
    font-size: $font-size-sm;
    font-weight: 900;
  }

  &__text {
    font-size: $font-size-xl;
    font-weight: 800;
    color: $primary;
    transition: color 0.3s;
  }
}

.lnb-nav {
  @media (max-width: $bp-tablet) { display: none; }

  &__list { display: flex; align-items: center; gap: 4px; list-style: none; }

  &__item { position: relative; }

  &__link {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: $spacing-2 $spacing-3;
    font-size: $font-size-sm;
    font-weight: 600;
    color: $text-secondary;
    text-decoration: none;
    border-radius: $radius-sm;
    transition: color $transition-fast, background $transition-fast;
    white-space: nowrap;

    &:hover { color: $primary; background: $primary-bg; }
  }

  &__drop {
    position: absolute;
    top: 100%;
    left: 0;
    min-width: 160px;
    background: white;
    border: 1px solid $border;
    border-radius: $radius-lg;
    box-shadow: $shadow-lg;
    list-style: none;
    padding: $spacing-2;
    padding-top: calc($spacing-2 + 8px);
    margin-top: 0;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.2s;

    &::before {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 8px;
    }

    &.show { opacity: 1; pointer-events: all; }

    li + li { border-top: 1px solid $bg-light; }
  }

  &__drop-link {
    display: block;
    padding: $spacing-2 $spacing-4;
    font-size: $font-size-sm;
    color: $text-secondary;
    text-decoration: none;
    border-radius: $radius-sm;
    transition: background $transition-fast, color $transition-fast;
    white-space: nowrap;

    &:hover { background: $primary-bg; color: $primary; }
  }
}

.lnb-btn-login {
  padding: $spacing-2 $spacing-4;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-secondary;
  text-decoration: none;
  border-radius: $radius-md;
  transition: color $transition-fast;
  white-space: nowrap;

  &:hover { color: $primary; }
}

.lnb-btn-trial {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-5;
  background: $primary;
  color: white;
  font-size: $font-size-sm;
  font-weight: 700;
  border-radius: $radius-md;
  text-decoration: none;
  white-space: nowrap;
  transition: background $transition-fast;

  &:hover { background: $primary-dark; }
}

.lnb-ham {
  display: none;
  flex-direction: column;
  gap: 5px;
  padding: $spacing-2;
  background: none;
  border: none;
  cursor: pointer;

  @media (max-width: $bp-tablet) { display: flex; }

  span {
    display: block;
    width: 22px;
    height: 2px;
    background: $text-secondary;
    border-radius: 2px;
    transition: all 0.3s;
  }
}

.lnb-mobile-nav {
  display: none;
  max-height: 0;
  overflow: hidden;
  background: white;
  border-top: 1px solid $border;
  transition: max-height 0.35s ease;

  @media (max-width: $bp-tablet) { display: block; }

  &.open { max-height: 600px; }

  ul { list-style: none; padding: $spacing-3 $spacing-5; }

  &__title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: $spacing-3 0;
    font-size: $font-size-base;
    font-weight: 600;
    color: $text-primary;
    background: none;
    border: none;
    cursor: pointer;
    border-bottom: 1px solid $bg-light;
  }

  &__sub {
    padding: $spacing-2 0 $spacing-2 $spacing-5 !important;

    li a {
      display: block;
      padding: $spacing-2 0;
      font-size: $font-size-sm;
      color: $text-secondary;
      text-decoration: none;
      &:hover { color: $primary; }
    }
  }

  &__btns {
    display: flex;
    gap: $spacing-3;
    padding: $spacing-4 $spacing-5;
    border-top: 1px solid $border;

    .lnb-btn-login {
      flex: 1;
      text-align: center;
      border: 1px solid $border;
      padding: $spacing-3;
      border-radius: $radius-md;
    }

    .lnb-btn-trial {
      flex: 1;
      justify-content: center;
      padding: $spacing-3;
    }
  }
}
</style>
