<template>
  <div class="app-layout">
    <AppHeader />
    <AppSidebar />
    <div
      v-if="uiStore.sidebarOpen"
      class="sidebar-overlay show"
      @click="uiStore.closeSidebar()"
    />
    <main class="app-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import { useUiStore } from '@/store/ui'
import { useNotificationStore } from '@/store/notification'
import { useAuthStore } from '@/store/auth'

const uiStore = useUiStore()
const notifStore = useNotificationStore()
const authStore = useAuthStore()

onMounted(() => {
  if (authStore.isAuthenticated) {
    notifStore.fetchNotifications()
  }
})
</script>

<style scoped lang="scss">
</style>
