<template>
  <header class="app-header">
    <div class="app-header__inner">
      <div class="app-header__left">
        <button class="hamburger" @click="uiStore.toggleSidebar()">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="12" x2="21" y2="12"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>
        <RouterLink :to="homeRoute" class="logo">
          <span class="logo__icon">AI</span>
          <span class="logo__text">EDU</span>
        </RouterLink>
      </div>
      <div class="app-header__right">
        <!-- 알림 -->
        <div class="header-notify" @click="toggleNotify" ref="notifyRef">
          <button class="icon-btn">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="unreadCount > 0" class="notify-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
          </button>
          <!-- 알림 드롭다운 -->
          <div v-if="notifyOpen" class="notify-dropdown">
            <div class="notify-dropdown__header">
              <span>알림</span>
              <button class="btn btn-ghost btn-sm" @click="markAllRead">모두 읽음</button>
            </div>
            <div v-if="notifications.length" class="notify-list">
              <div
                v-for="n in notifications.slice(0, 5)"
                :key="n.id"
                :class="['notify-item', { unread: !n.read }]"
                @click="markAsRead(n.id)"
              >
                <p class="notify-item__msg">{{ n.message }}</p>
                <span class="notify-item__time">{{ formatTime(n.createdAt) }}</span>
              </div>
            </div>
            <div v-else class="notify-empty">새 알림이 없습니다.</div>
          </div>
        </div>

        <!-- 사용자 드롭다운 -->
        <div class="user-menu" @click="toggleUserMenu" ref="userMenuRef">
          <div class="user-avatar">
            <img v-if="user?.profileImgUrl" :src="user.profileImgUrl" alt="프로필" style="width:100%;height:100%;object-fit:cover;border-radius:50%;" />
            <span v-else>{{ userInitial }}</span>
          </div>
          <div v-if="userMenuOpen" class="user-dropdown">
            <div class="user-dropdown__info">
              <p class="name">{{ user?.name }}</p>
              <p class="email">{{ user?.email }}</p>
            </div>
            <hr />
            <RouterLink to="/settings" class="user-dropdown__item" @click="userMenuOpen = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 0-14.14 0M4.93 19.07a10 10 0 0 0 14.14 0"/></svg>
              설정
            </RouterLink>
            <button class="user-dropdown__item danger" @click="handleLogout">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              로그아웃
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onClickOutside } from '@vueuse/core'
import { useAuthStore } from '@/store/auth'
import { useUiStore } from '@/store/ui'
import { useNotificationStore } from '@/store/notification'
import { useAuth } from '@/composables/useAuth'

const authStore = useAuthStore()
const uiStore = useUiStore()
const notifStore = useNotificationStore()
const { logout } = useAuth()

const user = computed(() => authStore.user)
const userInitial = computed(() => user.value?.name?.charAt(0) || 'U')
const homeRoute = computed(() => {
  const role = user.value?.role
  if (role === 'TEACHER') return '/teacher/home'
  if (role === 'ADMIN') return '/admin/dashboard'
  return '/student/home'
})
const notifications = computed(() => notifStore.notifications)
const unreadCount = computed(() => notifStore.unreadCount)

const notifyOpen = ref(false)
const userMenuOpen = ref(false)
const notifyRef = ref(null)
const userMenuRef = ref(null)

onClickOutside(notifyRef, () => { notifyOpen.value = false })
onClickOutside(userMenuRef, () => { userMenuOpen.value = false })

function toggleNotify() { notifyOpen.value = !notifyOpen.value }
function toggleUserMenu() { userMenuOpen.value = !userMenuOpen.value }

function markAsRead(id) { notifStore.markAsRead(id) }
function markAllRead() { notifStore.markAllRead() }

async function handleLogout() {
  userMenuOpen.value = false
  await logout()
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '방금 전'
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`
  return `${Math.floor(diff / 86400)}일 전`
}
</script>

<style scoped lang="scss">
.hamburger {
  display: none;
  color: $text-secondary;
  padding: $spacing-2;
  border-radius: $radius-sm;

  &:hover { background: $bg-light; }

  @media (max-width: $bp-tablet) {
    display: flex;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  text-decoration: none;

  &__icon {
    background: $primary;
    color: white;
    padding: 4px 8px;
    border-radius: $radius-sm;
    font-size: $font-size-sm;
    font-weight: 900;
  }

  &__text {
    font-size: $font-size-xl;
    font-weight: 700;
    color: $primary;
  }
}

.icon-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: $radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-secondary;

  &:hover { background: $bg-light; }
}

.notify-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: $danger;
  color: white;
  border-radius: $radius-full;
  font-size: 10px;
  font-weight: 700;
  min-width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
}

.header-notify {
  position: relative;
}

.notify-dropdown {
  position: absolute;
  top: calc(100% + $spacing-2);
  right: 0;
  width: 320px;
  background: $bg-white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  box-shadow: $shadow-lg;
  z-index: 200;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-4 $spacing-5;
    border-bottom: 1px solid $border;
    font-weight: 600;
    font-size: $font-size-sm;
  }
}

.notify-list {
  max-height: 300px;
  overflow-y: auto;
}

.notify-item {
  padding: $spacing-3 $spacing-5;
  border-bottom: 1px solid $border;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover { background: $bg-light; }
  &.unread { background: $primary-bg; }

  &__msg {
    font-size: $font-size-sm;
    color: $text-primary;
    margin-bottom: 2px;
  }

  &__time {
    font-size: $font-size-xs;
    color: $text-muted;
  }
}

.notify-empty {
  padding: $spacing-6;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-muted;
}

.user-menu {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: $primary;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: $font-size-sm;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + $spacing-2);
  right: 0;
  width: 200px;
  background: $bg-white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  box-shadow: $shadow-lg;
  z-index: 200;
  overflow: hidden;

  &__info {
    padding: $spacing-4 $spacing-5;

    .name {
      font-weight: 700;
      font-size: $font-size-sm;
      color: $text-primary;
    }

    .email {
      font-size: $font-size-xs;
      color: $text-muted;
      margin-top: 2px;
    }
  }

  hr { border: none; border-top: 1px solid $border; margin: 0; }

  &__item {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    width: 100%;
    padding: $spacing-3 $spacing-5;
    font-size: $font-size-sm;
    color: $text-secondary;
    text-decoration: none;
    transition: background $transition-fast;

    &:hover { background: $bg-light; color: $text-primary; }
    &.danger { color: $danger; &:hover { background: #FEF2F2; } }
  }
}
</style>
