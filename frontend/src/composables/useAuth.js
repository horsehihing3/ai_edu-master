import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useToast } from './useToast'

export function useAuth() {
  const authStore = useAuthStore()
  const router = useRouter()
  const { showToast } = useToast()

  const user = computed(() => authStore.user)
  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const isStudent = computed(() => authStore.isStudent)
  const isTeacher = computed(() => authStore.isTeacher)
  const isAdmin = computed(() => authStore.isAdmin)

  async function login(credentials) {
    try {
      const user = await authStore.login(credentials)
      showToast({ type: 'success', message: `${user.name}님, 환영합니다!` })
      const role = user.role
      if (role === 'STUDENT') router.push('/student/home')
      else if (role === 'TEACHER' || role === 'SUPER_USER') router.push('/teacher/home')
      else if (role === 'ADMIN') router.push('/admin/dashboard')
    } catch (e) {
      throw e
    }
  }

  async function logout() {
    await authStore.logout()
    router.push('/')
    showToast({ type: 'info', message: '로그아웃 되었습니다.' })
  }

  function getHomeRoute() {
    if (authStore.isAdmin) return '/admin/dashboard'
    if (authStore.isTeacher) return '/teacher/home'
    if (authStore.isStudent) return '/student/home'
    return '/login'
  }

  return {
    user,
    isAuthenticated,
    isStudent,
    isTeacher,
    isAdmin,
    login,
    logout,
    getHomeRoute
  }
}
