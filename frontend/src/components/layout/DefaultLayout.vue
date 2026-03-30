<template>
  <div class="app-layout">
    <AppHeader />
    <AppSidebar />
    <div
      v-if="uiStore.sidebarOpen"
      class="sidebar-overlay show"
      @click="uiStore.closeSidebar()"
    />
    <main class="app-content" :class="{ 'has-bottom-nav': isStudent }">
      <RouterView />
    </main>
    <BottomNavBar v-if="isStudent" />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import BottomNavBar from './BottomNavBar.vue'
import { useUiStore } from '@/store/ui'
import { useNotificationStore } from '@/store/notification'
import { useAuthStore } from '@/store/auth'

const uiStore = useUiStore()
const notifStore = useNotificationStore()
const authStore = useAuthStore()

const isStudent = computed(() => authStore.user?.role === 'STUDENT')

onMounted(() => {
  if (authStore.isAuthenticated) {
    notifStore.fetchNotifications()
  }
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

@media (max-width: $bp-mobile) {
  .has-bottom-nav {
    padding-bottom: 60px;
  }
}
</style>
