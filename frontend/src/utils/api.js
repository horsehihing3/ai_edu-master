import axios from 'axios'
import { useUiStore } from '@/store/ui'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let activeRequests = 0

// 요청 인터셉터 - Authorization 헤더 자동 추가 + 로딩
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (!config._silent) {
      activeRequests++
      if (activeRequests === 1) {
        try { useUiStore().setLoading(true) } catch {}
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

function decrementLoading(config) {
  if (!config?._silent) {
    activeRequests = Math.max(0, activeRequests - 1)
    if (activeRequests === 0) {
      try { useUiStore().setLoading(false) } catch {}
    }
  }
}

// 응답 인터셉터 - 401 시 토큰 갱신
let isRefreshing = false
let failedQueue = []

function processQueue(error, token = null) {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => {
    decrementLoading(response.config)
    // ApiResponse 래퍼 { success, data, message } 자동 해제
    if (response.data && typeof response.data.success === 'boolean' && 'data' in response.data) {
      response.data = response.data.data
    }
    return response
  },
  async (error) => {
    decrementLoading(error.config)
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        }).catch(err => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        isRefreshing = false
        error.message = error.response?.data?.message || '이메일 또는 비밀번호를 확인해 주세요.'
        return Promise.reject(error)
      }

      try {
        const response = await axios.post('/api/auth/refresh', { refreshToken })
        const { accessToken } = response.data?.data || response.data

        localStorage.setItem('accessToken', accessToken)
        api.defaults.headers.common.Authorization = `Bearer ${accessToken}`
        originalRequest.headers.Authorization = `Bearer ${accessToken}`

        processQueue(null, accessToken)
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        refreshError.message = '세션이 만료되었습니다. 다시 로그인해 주세요.'
        if (localStorage.getItem('accessToken')) {
          redirectToLogin()
        }
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 에러 메시지 정규화
    if (error.response) {
      const statusMessages = {
        400: '잘못된 요청입니다.',
        401: '인증이 필요합니다. 다시 로그인해 주세요.',
        403: '접근 권한이 없습니다.',
        404: '요청한 리소스를 찾을 수 없습니다.',
        409: '이미 존재하는 데이터입니다.',
        500: '서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
      }
      const msg = error.response.data?.message || error.response.data?.error || statusMessages[error.response.status] || '오류가 발생했습니다.'
      error.message = msg
    } else if (error.request) {
      error.message = '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.'
    }

    return Promise.reject(error)
  }
)

function redirectToLogin() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  window.location.href = '/login'
}

export default api
