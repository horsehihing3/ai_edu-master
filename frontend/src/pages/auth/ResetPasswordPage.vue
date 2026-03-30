<template>
  <div class="reset-page">
    <h2>새 비밀번호 설정</h2>

    <div v-if="tokenError" class="token-error">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <h3>링크가 유효하지 않습니다</h3>
      <p>{{ tokenError }}</p>
      <RouterLink to="/password/forgot" class="btn-link">비밀번호 재설정 다시 요청하기</RouterLink>
    </div>

    <div v-else-if="done" class="done-msg">
      <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="1.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
      <h3>비밀번호가 변경되었습니다</h3>
      <p>새 비밀번호로 로그인해주세요.</p>
      <RouterLink to="/login" class="btn-primary">로그인하기</RouterLink>
    </div>

    <form v-else @submit.prevent="handleSubmit">
      <p class="subtitle">8자 이상의 새 비밀번호를 입력해주세요.</p>
      <AppInput
        v-model="newPassword"
        label="새 비밀번호"
        type="password"
        placeholder="8자 이상 입력"
        :error="passwordError"
        required
      />
      <AppInput
        v-model="confirmPassword"
        label="비밀번호 확인"
        type="password"
        placeholder="비밀번호를 다시 입력"
        :error="confirmError"
        required
      />
      <AppButton type="submit" :loading="loading" block>비밀번호 변경</AppButton>
    </form>

    <RouterLink v-if="!done" to="/login" class="back-link">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      로그인으로 돌아가기
    </RouterLink>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import AppInput from '@/components/common/AppInput.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const route = useRoute()
const { error } = useToast()

const token = route.query.token
const newPassword = ref('')
const confirmPassword = ref('')
const passwordError = ref('')
const confirmError = ref('')
const loading = ref(false)
const done = ref(false)
const tokenError = ref('')

onMounted(async () => {
  if (!token) {
    tokenError.value = '토큰이 없습니다. 이메일의 링크를 다시 클릭해주세요.'
    return
  }
  try {
    await api.get('/auth/password/validate-token', { params: { token } })
  } catch (e) {
    tokenError.value = e.response?.data?.message || '유효하지 않거나 만료된 링크입니다.'
  }
})

async function handleSubmit() {
  passwordError.value = ''
  confirmError.value = ''
  if (newPassword.value.length < 8) {
    passwordError.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    confirmError.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  loading.value = true
  try {
    await api.post('/auth/password/reset', { token, newPassword: newPassword.value })
    done.value = true
  } catch (e) {
    error(e.response?.data?.message || '비밀번호 변경에 실패했습니다.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.reset-page {
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

.token-error, .done-msg {
  text-align: center;
  padding: $spacing-6 0;

  h3 {
    font-size: $font-size-xl;
    font-weight: 700;
    margin: $spacing-4 0 $spacing-3;
    color: $text-primary;
  }
  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-5;
    line-height: 1.7;
  }
}

.btn-link, .btn-primary {
  display: inline-block;
  padding: $spacing-3 $spacing-6;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  font-weight: 600;
  text-decoration: none;
}
.btn-link { background: $bg-light; color: $text-primary; }
.btn-primary { background: $primary; color: #fff; }

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
