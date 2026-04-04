<template>
  <div class="verify-page">
    <!-- 토큰 처리 중 -->
    <template v-if="status === 'verifying'">
      <div class="verify-icon">⏳</div>
      <h2>인증 중...</h2>
      <p class="subtitle">잠시 기다려 주세요.</p>
    </template>

    <!-- 인증 성공 -->
    <template v-else-if="status === 'success'">
      <div class="verify-icon success">✓</div>
      <h2>이메일 인증 완료!</h2>
      <p class="subtitle">인증이 완료되었습니다. 로그인하여 시작하세요.</p>
      <AppButton @click="router.push('/login')" block>로그인하기</AppButton>
    </template>

    <!-- 토큰 없음 — 이메일 확인 안내 -->
    <template v-else-if="status === 'pending'">
      <div class="verify-icon pending">✉</div>
      <h2>이메일을 확인해 주세요</h2>
      <p class="subtitle">
        <strong>{{ email }}</strong>으로 인증 링크를 발송했습니다.<br/>
        이메일의 <strong>이메일 인증하기</strong> 버튼을 클릭하면 가입이 완료됩니다.
      </p>
      <p class="hint">이메일이 보이지 않으면 스팸 폴더를 확인해 주세요.</p>
      <AppButton :loading="resending" variant="secondary" @click="resend" block>인증 이메일 재발송</AppButton>
      <p class="login-link">
        이미 인증하셨나요? <RouterLink to="/login">로그인</RouterLink>
      </p>
    </template>

    <!-- 토큰 오류 -->
    <template v-else-if="status === 'error'">
      <div class="verify-icon error">✕</div>
      <h2>인증 링크가 유효하지 않습니다</h2>
      <p class="subtitle">{{ errorMessage }}</p>
      <AppButton :loading="resending" @click="resend" block>인증 이메일 재발송</AppButton>
      <p class="login-link">
        <RouterLink to="/login">로그인으로 돌아가기</RouterLink>
      </p>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const { success, error } = useToast()

const status = ref('pending')  // pending | verifying | success | error
const errorMessage = ref('')
const resending = ref(false)
const email = ref(route.query.email || '')

onMounted(async () => {
  const token = route.query.token
  if (!token) return  // 토큰 없으면 pending 안내 화면

  status.value = 'verifying'
  try {
    await api.post('/auth/email/verify', { token })
    status.value = 'success'
  } catch (e) {
    status.value = 'error'
    errorMessage.value = e.message || '링크가 만료되었거나 이미 사용된 링크입니다.'
  }
})

async function resend() {
  if (!email.value) {
    error('이메일 주소를 알 수 없습니다. 다시 회원가입 해주세요.')
    return
  }
  resending.value = true
  try {
    await api.post('/auth/email/resend', { email: email.value })
    success('인증 이메일을 재발송했습니다.')
  } catch (e) {
    error(e.message || '재발송에 실패했습니다.')
  } finally {
    resending.value = false
  }
}
</script>

<style scoped lang="scss">
.verify-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: $spacing-8 0;

  h2 {
    font-size: $font-size-xl;
    font-weight: 700;
    margin-bottom: $spacing-3;
    color: $text-primary;
  }
}

.verify-icon {
  font-size: 48px;
  margin-bottom: $spacing-5;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-light;
  color: $text-muted;

  &.success { background: #D1FAE5; color: #059669; }
  &.pending  { background: #DBEAFE; color: #2563EB; }
  &.error    { background: #FEE2E2; color: #DC2626; }
}

.subtitle {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.7;
  margin-bottom: $spacing-6;
  max-width: 340px;
}

.hint {
  font-size: $font-size-xs;
  color: $text-muted;
  margin-bottom: $spacing-5;
}

.login-link {
  margin-top: $spacing-4;
  font-size: $font-size-sm;
  color: $text-secondary;
  a { color: $primary-light; font-weight: 600; &:hover { text-decoration: underline; } }
}
</style>
