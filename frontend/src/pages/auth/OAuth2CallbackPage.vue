<template>
  <div class="callback-page">
    <template v-if="error">
      <p class="error-msg">{{ error }}</p>
      <AppButton @click="router.push('/login')" block>로그인으로 돌아가기</AppButton>
    </template>
    <template v-else>
      <p class="loading-msg">로그인 중...</p>
    </template>
  </div>
</template>

<script setup>
// [2026-04-04] OAuth2 인증 성공 후 백엔드가 token 파라미터와 함께 리다이렉트하는 콜백 페이지
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '@/components/common/AppButton.vue'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()
const error = ref('')

onMounted(async () => {
  const params = new URLSearchParams(window.location.search)
  const token = params.get('token')

  if (!token) {
    error.value = '인증 토큰을 받지 못했습니다.'
    return
  }

  try {
    // 토큰 저장 후 사용자 정보 조회
    authStore.setTokens(token, null)
    const userData = await authStore.fetchMe()

    const role = userData?.role
    if (role === 'STUDENT') router.replace('/student/home')
    else if (role === 'TEACHER' || role === 'SUPER_USER') router.replace('/teacher/home')
    else if (role === 'ADMIN') router.replace('/admin/dashboard')
    else router.replace('/')
  } catch (e) {
    authStore.clearAuth()
    error.value = '로그인 처리 중 오류가 발생했습니다.'
  }
})
</script>

<style scoped lang="scss">
.callback-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-4;
}

.loading-msg {
  font-size: $font-size-base;
  color: $text-secondary;
}

.error-msg {
  font-size: $font-size-sm;
  color: $danger;
  text-align: center;
}
</style>
