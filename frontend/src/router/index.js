import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

// 레이아웃
const DefaultLayout = () => import('@/components/layout/DefaultLayout.vue')
const AuthLayout = () => import('@/components/layout/AuthLayout.vue')

// 페이지
const LandingPage = () => import('@/pages/LandingPage.vue')
const LoginPage = () => import('@/pages/auth/LoginPage.vue')
const RegisterPage = () => import('@/pages/auth/RegisterPage.vue')
const ForgotPasswordPage = () => import('@/pages/auth/ForgotPasswordPage.vue')
const ResetPasswordPage = () => import('@/pages/auth/ResetPasswordPage.vue')
const VerifyEmailPage = () => import('@/pages/auth/VerifyEmailPage.vue')
const OAuth2CallbackPage = () => import('@/pages/auth/OAuth2CallbackPage.vue')

// Student
const StudentHomePage = () => import('@/pages/student/StudentHomePage.vue')
const DiagnosisPage = () => import('@/pages/student/DiagnosisPage.vue')
const ProblemListPage = () => import('@/pages/student/ProblemListPage.vue')
const ProblemSolvePage = () => import('@/pages/student/ProblemSolvePage.vue')
const VideoListPage = () => import('@/pages/student/VideoListPage.vue')
const VideoDetailPage = () => import('@/pages/student/VideoDetailPage.vue')
const BookmarkPage = () => import('@/pages/student/BookmarkPage.vue')
const WrongNotesPage = () => import('@/pages/student/WrongNotesPage.vue')
const ReportPage = () => import('@/pages/student/ReportPage.vue')
const AssignmentFeedbackPage = () => import('@/pages/student/AssignmentFeedbackPage.vue')
const StudentMyPage = () => import('@/pages/student/StudentMyPage.vue')

// Teacher
const TeacherHomePage = () => import('@/pages/teacher/TeacherHomePage.vue')
const TeacherAnalyticsPage = () => import('@/pages/teacher/TeacherAnalyticsPage.vue')
const StudentListPage = () => import('@/pages/teacher/StudentListPage.vue')
const StudentDetailPage = () => import('@/pages/teacher/StudentDetailPage.vue')
const AssignmentListPage = () => import('@/pages/teacher/AssignmentListPage.vue')
const AssignmentCreatePage = () => import('@/pages/teacher/AssignmentCreatePage.vue')
const AssignmentDetailPage = () => import('@/pages/teacher/AssignmentDetailPage.vue')
const ProblemBankPage = () => import('@/pages/teacher/ProblemBankPage.vue')
const ClassManagePage = () => import('@/pages/teacher/ClassManagePage.vue')

// Admin
const AdminDashboardPage = () => import('@/pages/admin/AdminDashboardPage.vue')
const MemberListPage = () => import('@/pages/admin/MemberListPage.vue')
const SchoolListPage = () => import('@/pages/admin/SchoolListPage.vue')
const ProblemDBPage = () => import('@/pages/admin/ProblemDBPage.vue')
const ProblemUploadPage = () => import('@/pages/admin/ProblemUploadPage.vue')
const VideoManagePage = () => import('@/pages/admin/VideoManagePage.vue')
const PaymentPage = () => import('@/pages/admin/PaymentPage.vue')
const AnalyticsPage = () => import('@/pages/admin/AnalyticsPage.vue')
const CodeManagePage = () => import('@/pages/admin/CodeManagePage.vue')
const AdminInquiryPage = () => import('@/pages/admin/AdminInquiryPage.vue')

// Common
const AnnouncementPage = () => import('@/pages/common/AnnouncementPage.vue')
const DashboardAnnouncementPage = () => import('@/pages/common/DashboardAnnouncementPage.vue')
const SettingsPage = () => import('@/pages/common/SettingsPage.vue')
const NotFoundPage = () => import('@/pages/NotFoundPage.vue')

const TermsPage = () => import('@/pages/common/TermsPage.vue')
const PrivacyPage = () => import('@/pages/common/PrivacyPage.vue')

const routes = [
  {
    path: '/',
    component: LandingPage,
    meta: { public: true }
  },
  {
    path: '/terms',
    component: TermsPage,
    meta: { public: true }
  },
  {
    path: '/privacy',
    component: PrivacyPage,
    meta: { public: true }
  },
  {
    path: '/login',
    component: AuthLayout,
    meta: { public: true, guestOnly: true },
    children: [{ path: '', component: LoginPage }]
  },
  {
    path: '/register',
    component: AuthLayout,
    meta: { public: true, guestOnly: true },
    children: [{ path: '', component: RegisterPage }]
  },
  {
    path: '/password/forgot',
    component: AuthLayout,
    meta: { public: true, guestOnly: true },
    children: [{ path: '', component: ForgotPasswordPage }]
  },
  {
    path: '/password/reset',
    component: AuthLayout,
    meta: { public: true },
    children: [{ path: '', component: ResetPasswordPage }]
  },
  {
    path: '/verify-email',
    component: AuthLayout,
    meta: { public: true },
    children: [{ path: '', component: VerifyEmailPage }]
  },
  {
    path: '/oauth2/callback',
    component: AuthLayout,
    meta: { public: true },
    children: [{ path: '', component: OAuth2CallbackPage }]
  },
  {
    path: '/student',
    component: DefaultLayout,
    meta: { requiresAuth: true, roles: ['STUDENT'] },
    children: [
      { path: 'home', component: StudentHomePage, meta: { title: '홈' } },
      { path: 'diagnosis', component: DiagnosisPage, meta: { title: '진단 테스트' } },
      { path: 'learn', component: ProblemListPage, meta: { title: '문제 풀기' } },
      { path: 'learn/:sessionId', component: ProblemSolvePage, meta: { title: '문제 풀기' } },
      { path: 'videos', component: VideoListPage, meta: { title: '동영상 풀이' } },
      { path: 'videos/:id', component: VideoDetailPage, meta: { title: '동영상 풀이' } },
      { path: 'bookmarks', component: BookmarkPage, meta: { title: '즐겨찾기' } },
      { path: 'wrong-notes', component: WrongNotesPage, meta: { title: '오답노트' } },
      { path: 'report', component: ReportPage, meta: { title: '학습 리포트' } },
      { path: 'assignments/:id/feedback', component: AssignmentFeedbackPage, meta: { title: '과제 피드백' } },
      { path: 'mypage', component: StudentMyPage, meta: { title: '마이페이지' } }
    ]
  },
  {
    path: '/teacher',
    component: DefaultLayout,
    meta: { requiresAuth: true, roles: ['TEACHER', 'SUPER_USER'] },
    children: [
      { path: 'home', component: TeacherHomePage, meta: { title: '대시보드' } },
      { path: 'students', component: StudentListPage, meta: { title: '학생 관리' } },
      { path: 'students/:id', component: StudentDetailPage, meta: { title: '학생 상세' } },
      { path: 'assignments', component: AssignmentListPage, meta: { title: '과제 관리' } },
      { path: 'assignments/create', component: AssignmentCreatePage, meta: { title: '과제 생성' } },
      { path: 'assignments/:id', component: AssignmentDetailPage, meta: { title: '과제 상세' } },
      { path: 'analytics', component: TeacherAnalyticsPage, meta: { title: '학습 현황' } },
      { path: 'problems', component: ProblemBankPage, meta: { title: '문제 은행' } },
      { path: 'classes', component: ClassManagePage, meta: { title: '학급 관리' } }
    ]
  },
  {
    path: '/admin',
    component: DefaultLayout,
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: 'dashboard', component: AdminDashboardPage, meta: { title: '대시보드' } },
      { path: 'members', component: MemberListPage, meta: { title: '회원 관리' } },
      { path: 'schools', component: SchoolListPage, meta: { title: '학원/학교 관리' } },
      { path: 'problems', component: ProblemDBPage, meta: { title: '문제 DB' } },
      { path: 'problems/upload', component: ProblemUploadPage, meta: { title: '문제 업로드' } },
      { path: 'videos', component: VideoManagePage, meta: { title: '동영상 관리' } },
      { path: 'payments', component: PaymentPage, meta: { title: '결제 관리' } },
      { path: 'analytics', component: AnalyticsPage, meta: { title: '시스템 분석' } },
      { path: 'codes', component: CodeManagePage, meta: { title: '코드 관리' } },
      { path: 'inquiries', component: AdminInquiryPage, meta: { title: '1:1 문의 관리' } }
    ]
  },
  {
    path: '/announcements',
    component: AnnouncementPage,
    meta: { public: true }
  },
  {
    path: '/settings',
    component: DefaultLayout,
    meta: { requiresAuth: true },
    children: [{ path: '', component: SettingsPage, meta: { title: '설정' } }]
  },
  {
    path: '/notices',
    component: DefaultLayout,
    meta: { requiresAuth: true },
    children: [{ path: '', component: DashboardAnnouncementPage, meta: { title: '공지사항' } }]
  },
  {
    path: '/404',
    component: NotFoundPage,
    meta: { public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return new Promise(resolve => {
      setTimeout(() => resolve({ top: 0, left: 0, behavior: 'instant' }), 0)
    })
  }
})

// 네비게이션 가드
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 토큰이 있지만 유저 정보가 없는 경우 유저 정보 fetch
  if (authStore.accessToken && !authStore.user) {
    await authStore.fetchMe()
  }

  const isAuthenticated = authStore.isAuthenticated
  const userRole = authStore.user?.role

  // 게스트 전용 페이지 (로그인 상태면 홈으로)
  if (to.meta.guestOnly && isAuthenticated) {
    if (userRole === 'STUDENT') return next('/student/home')
    if (userRole === 'TEACHER' || userRole === 'SUPER_USER') return next('/teacher/home')
    if (userRole === 'ADMIN') return next('/admin/dashboard')
    return next('/')
  }

  // 인증 필요 페이지
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 역할 제한 페이지
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userRole)) {
      if (userRole === 'STUDENT') return next('/student/home')
      if (userRole === 'TEACHER' || userRole === 'SUPER_USER') return next('/teacher/home')
      if (userRole === 'ADMIN') return next('/admin/dashboard')
      return next('/login')
    }
  }

  next()
})

export default router
