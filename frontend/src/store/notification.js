import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

  async function fetchNotifications() {
    try {
      const response = await api.get('/notifications')
      const data = response.data
      notifications.value = Array.isArray(data) ? data : (data?.content || [])
    } catch (e) {
      console.error('알림 조회 실패', e)
    }
  }

  async function markAsRead(id) {
    try {
      await api.put(`/notifications/${id}/read`)
      const n = notifications.value.find(n => n.id === id)
      if (n) n.read = true
    } catch (e) {
      console.error('알림 읽음 처리 실패', e)
    }
  }

  async function markAllRead() {
    try {
      await api.put('/notifications/read-all')
      notifications.value.forEach(n => (n.read = true))
    } catch (e) {
      console.error('전체 읽음 처리 실패', e)
    }
  }

  return {
    notifications,
    unreadCount,
    fetchNotifications,
    markAsRead,
    markAllRead
  }
})
