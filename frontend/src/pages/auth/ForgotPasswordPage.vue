<template>
  <div class="forgot-page">
    <h2>비밀번호 찾기</h2>
    <p class="subtitle">가입 시 사용한 이메일을 입력하세요</p>

    <div v-if="!sent">
      <form @submit.prevent="handleSubmit">
        <AppInput
          v-model="email"
          label="이메일"
          type="email"
          placeholder="이메일을 입력하세요"
          :error="emailError"
          required
        />
        <AppButton type="submit" :loading="loading" block>재설정 링크 전송</AppButton>
      </form>
    </div>

    <div v-else class="sent-msg">
      <div class="sent-icon">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="1.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
      </div>
      <h3>이메일을 확인해주세요</h3>
      <p>
        <strong>{{ email }}</strong>로 비밀번호 재설정 링크를 발송했습니다.<br/>
        이메일을 확인하여 비밀번호를 재설정해주세요.
      </p>
      <AppButton variant="secondary" block @click="sent = false">다시 시도</AppButton>
    </div>

    <RouterLink to="/login" class="back-link">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      로그인으로 돌아가기
    </RouterLink>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppInput from '@/components/common/AppInput.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const { error } = useToast()
const email = ref('')
const emailError = ref('')
const loading = ref(false)
const sent = ref(false)

async function handleSubmit() {
  emailError.value = ''
  if (!email.value) { emailError.value = '이메일을 입력하세요.'; return }
  loading.value = true
  try {
    await api.post('/auth/password/forgot', { email: email.value })
    sent.value = true
  } catch (e) {
    error(e.message || '오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.forgot-page {
  h2 {
    font-size: $font-size-2xl;
    font-weight: 700;
    margin-bottom: $spacing-2;
  }

  .subtitle {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-6;
  }
}

.sent-msg {
  text-align: center;
  padding: $spacing-4 0;

  .sent-icon { margin-bottom: $spacing-5; }

  h3 {
    font-size: $font-size-xl;
    font-weight: 700;
    margin-bottom: $spacing-3;
  }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: 1.7;
    margin-bottom: $spacing-6;
  }
}

.back-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  margin-top: $spacing-5;
  font-size: $font-size-sm;
  color: $text-secondary;

  &:hover { color: $primary-light; }
}
</style>
