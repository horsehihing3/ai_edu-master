import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)

  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)
  const isStudent = computed(() => user.value?.role === 'STUDENT')
  const isTeacher = computed(() => user.value?.role === 'TEACHER' || user.value?.role === 'SUPER_USER')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(credentials) {
    const response = await api.post('/auth/login', credentials)
    const { accessToken: at, refreshToken: rt, userInfo: u } = response.data
    setTokens(at, rt)
    user.value = u
    return u
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } catch (e) {
      // ignore
    } finally {
      clearAuth()
    }
  }

  async function fetchMe() {
    if (!accessToken.value) return null
    try {
      const response = await api.get('/auth/me')
      user.value = response.data
      return response.data
    } catch (e) {
      clearAuth()
      return null
    }
  }

  async function refresh() {
    try {
      const response = await api.post('/auth/refresh', { refreshToken: refreshToken.value })
      const { accessToken: at, refreshToken: rt } = response.data
      setTokens(at, rt)
      return at
    } catch (e) {
      clearAuth()
      throw e
    }
  }

  async function updateProfile(data) {
    const response = await api.put('/auth/profile', data)
    user.value = { ...user.value, ...response.data }
    return response.data
  }

  function setTokens(at, rt) {
    accessToken.value = at
    refreshToken.value = rt
    localStorage.setItem('accessToken', at)
    if (rt) localStorage.setItem('refreshToken', rt)
  }

  function clearAuth() {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return {
    user,
    accessToken,
    refreshToken,
    isAuthenticated,
    isStudent,
    isTeacher,
    isAdmin,
    login,
    logout,
    fetchMe,
    refresh,
    updateProfile,
    setTokens,
    clearAuth
  }
})
