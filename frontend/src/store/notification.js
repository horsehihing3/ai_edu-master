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
      const raw = Array.isArray(data) ? data : (data?.content || [])
      // [2026-04-01] API 필드 정규화 (notificationId→id, isRead→read, title+content→message)
      notifications.value = raw.map(n => ({
        id: n.notificationId ?? n.id,
        message: n.title ? `${n.title}: ${n.content}` : (n.content ?? n.message ?? ''),
        linkUrl: n.linkUrl,
        read: n.isRead ?? n.read ?? false,
        createdAt: n.createdAt,
        notiType: n.notiType
      }))
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
