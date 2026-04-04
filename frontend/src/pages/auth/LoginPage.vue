<template>
  <div class="login-page">
    <h2>로그인</h2>
    <p class="subtitle">계정에 로그인하여 학습을 시작하세요</p>

    <form @submit.prevent="handleLogin" class="login-form">
      <AppInput
        v-model="form.email"
        label="이메일"
        type="email"
        placeholder="이메일을 입력하세요"
        :error="errors.email"
        required
      />
      <AppInput
        v-model="form.password"
        label="비밀번호"
        type="password"
        placeholder="비밀번호를 입력하세요"
        :error="errors.password"
        required
      />

      <div class="login-options">
        <label class="form-checkbox">
          <input type="checkbox" v-model="form.remember" />
          <span>로그인 상태 유지</span>
        </label>
        <RouterLink to="/password/forgot" class="forgot-link">비밀번호 찾기</RouterLink>
      </div>

      <p v-if="errors.general" class="error-msg">{{ errors.general }}</p>
      <AppButton type="submit" :loading="loading" block>로그인</AppButton>
    </form>

    <div class="divider-text"><span>또는</span></div>

    <!-- 소셜 로그인 -->
    <div class="social-btns">
      <button class="social-btn kakao" @click="socialLogin('kakao')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 3C6.48 3 2 6.48 2 10.8c0 2.7 1.6 5.1 4 6.6l-1 3.7 4.3-2.8c.9.2 1.8.3 2.7.3 5.52 0 10-3.48 10-7.8C22 6.48 17.52 3 12 3z"/></svg>
        카카오로 로그인
      </button>
      <button class="social-btn google" @click="socialLogin('google')">
        <svg width="20" height="20" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
        Google로 로그인
      </button>
    </div>

    <p class="signup-link">
      계정이 없으신가요?
      <RouterLink to="/register">회원가입</RouterLink>
    </p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppInput from '@/components/common/AppInput.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useAuth } from '@/composables/useAuth'

const { login } = useAuth()
const route = useRoute()

const loading = ref(false)
const form = reactive({ email: '', password: '', remember: false })
const errors = reactive({ email: '', password: '', general: '' })

// [2026-04-04] OAuth2 실패 시 URL에서 에러 메시지 표시
onMounted(() => {
  if (route.query.error) {
    errors.general = decodeURIComponent(route.query.error)
  }
})

function validate() {
  errors.email = ''
  errors.password = ''
  let ok = true
  if (!form.email) { errors.email = '이메일을 입력하세요.'; ok = false }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) { errors.email = '올바른 이메일 형식이 아닙니다.'; ok = false }
  if (!form.password) { errors.password = '비밀번호를 입력하세요.'; ok = false }
  return ok
}

async function handleLogin() {
  if (!validate()) return
  loading.value = true
  errors.general = ''
  try {
    await login({ email: form.email, password: form.password })
  } catch (e) {
    errors.general = e.message || '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

// [2026-04-04] 백엔드 OAuth2 인가 엔드포인트로 리다이렉트
function socialLogin(provider) {
  window.location.href = `http://localhost:7000/api/oauth2/authorization/${provider}`
}
</script>

<style scoped lang="scss">
.login-page {
  flex: 1;
  display: flex;
  flex-direction: column;

  h2 {
    font-size: $font-size-2xl;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: $spacing-2;
  }

  .subtitle {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-6;
  }
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-5;

  .forgot-link {
    font-size: $font-size-sm;
    color: $primary-light;

    &:hover { text-decoration: underline; }
  }
}

.divider-text {
  text-align: center;
  position: relative;
  margin: $spacing-6 0;
  font-size: $font-size-sm;
  color: $text-muted;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: $border;
  }

  span {
    position: relative;
    background: $bg-white;
    padding: 0 $spacing-3;
  }
}

.social-btns {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  margin-bottom: $spacing-6;
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-3;
  width: 100%;
  padding: $spacing-3;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
  cursor: pointer;
  transition: opacity $transition-fast;

  &:hover { opacity: 0.9; }

  &.kakao {
    background: #FEE500;
    color: #3A1D1D;
    border: none;
  }

  &.naver {
    background: #03C75A;
    color: white;
    border: none;
  }

  &.google {
    background: white;
    color: #3C4043;
    border: 1px solid #DADCE0;
  }

  .naver-n {
    font-weight: 900;
    font-size: $font-size-base;
  }
}

.error-msg {
  color: $danger;
  font-size: $font-size-sm;
  font-weight: 500;
  margin-bottom: $spacing-3;
}

.signup-link {
  margin-top: auto;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-secondary;

  a {
    color: $primary-light;
    font-weight: 600;

    &:hover { text-decoration: underline; }
  }
}
</style>
