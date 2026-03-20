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
      <button class="social-btn naver" @click="socialLogin('naver')">
        <span class="naver-n">N</span>
        네이버로 로그인
      </button>
    </div>

    <p class="signup-link">
      계정이 없으신가요?
      <RouterLink to="/register">회원가입</RouterLink>
    </p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute } from 'vue-router'
import AppInput from '@/components/common/AppInput.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useAuth } from '@/composables/useAuth'

const { login } = useAuth()
const route = useRoute()

const loading = ref(false)
const form = reactive({ email: '', password: '', remember: false })
const errors = reactive({ email: '', password: '', general: '' })

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

function socialLogin() {
  alert('준비중입니다.')
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
