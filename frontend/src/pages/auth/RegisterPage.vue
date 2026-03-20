<template>
  <div class="register-page">
    <h2>회원가입</h2>
    <p class="subtitle">역할을 선택하여 시작하세요</p>

    <!-- 역할 탭 -->
    <div class="role-tabs">
      <button
        :class="['role-tab', { active: role === 'STUDENT' }]"
        @click="role = 'STUDENT'"
      >학생</button>
      <button
        :class="['role-tab', { active: role === 'TEACHER' }]"
        @click="role = 'TEACHER'"
      >교사</button>
    </div>

    <!-- 단계 표시 -->
    <div class="step-indicator">
      <div v-for="i in 2" :key="i" :class="['step', { active: step >= i, done: step > i }]">
        <span class="step-num">{{ i }}</span>
        <span class="step-label">{{ i === 1 ? '기본 정보' : '이메일 인증' }}</span>
      </div>
    </div>

    <!-- Step 1: 기본 정보 -->
    <form v-if="step === 1" @submit.prevent="goStep2">
      <AppInput v-model="form.name" label="이름" placeholder="이름을 입력하세요" :error="errors.name" required />
      <AppInput v-model="form.email" label="이메일" type="email" placeholder="이메일을 입력하세요" :error="errors.email" required />
      <AppInput v-model="form.password" label="비밀번호" type="password" placeholder="8자 이상 입력하세요" :error="errors.password" required />
      <AppInput v-model="form.confirmPassword" label="비밀번호 확인" type="password" placeholder="비밀번호를 다시 입력하세요" :error="errors.confirmPassword" required />

      <div v-if="role === 'STUDENT'">
        <AppSelect
          v-model="form.grade"
          label="학년"
          :options="gradeOptions"
          placeholder="학년을 선택하세요"
        />
      </div>
      <div v-else>
        <AppInput v-model="form.schoolName" label="소속 학교" placeholder="소속 학교명을 입력하세요" />
      </div>

      <label class="form-checkbox" style="margin-bottom: 16px;">
        <input type="checkbox" v-model="form.agree" />
        <span>이용약관 및 개인정보처리방침에 동의합니다 <span style="color:#EF4444">*</span></span>
      </label>

      <AppButton type="submit" block>다음 단계</AppButton>
    </form>

    <!-- Step 2: 이메일 인증 -->
    <div v-if="step === 2" class="verify-step">
      <div class="verify-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.5"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
      </div>
      <h3>이메일을 확인해주세요</h3>
      <p><strong>{{ form.email }}</strong>로 인증 코드를 발송했습니다.</p>

      <div class="verify-input">
        <AppInput v-model="verifyCode" label="인증 코드" placeholder="6자리 코드를 입력하세요" :error="verifyError" />
        <AppButton :loading="verifying" block @click="handleVerify">인증 완료</AppButton>
      </div>

      <button class="resend-btn" @click="resendCode">인증 코드 재발송</button>
    </div>

    <p class="login-link">
      이미 계정이 있으신가요?
      <RouterLink to="/login">로그인</RouterLink>
    </p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppInput from '@/components/common/AppInput.vue'
import AppSelect from '@/components/common/AppSelect.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const { success, error } = useToast()

const role = ref(route.query.role === 'TEACHER' ? 'TEACHER' : 'STUDENT')
const step = ref(1)
const verifyCode = ref('')
const verifyError = ref('')
const verifying = ref(false)

const form = reactive({
  name: '', email: '', password: '', confirmPassword: '',
  grade: '', schoolName: '', agree: false
})
const errors = reactive({ name: '', email: '', password: '', confirmPassword: '' })

const gradeOptions = [
  { value: '중1', label: '중학교 1학년' },
  { value: '중2', label: '중학교 2학년' },
  { value: '중3', label: '중학교 3학년' },
  { value: '고1', label: '고등학교 1학년' },
  { value: '고2', label: '고등학교 2학년' },
  { value: '고3', label: '고등학교 3학년' }
]

function validate() {
  let ok = true
  Object.keys(errors).forEach(k => errors[k] = '')
  if (!form.name) { errors.name = '이름을 입력하세요.'; ok = false }
  if (!form.email) { errors.email = '이메일을 입력하세요.'; ok = false }
  if (!form.password || form.password.length < 8) { errors.password = '8자 이상 입력하세요.'; ok = false }
  if (form.password !== form.confirmPassword) { errors.confirmPassword = '비밀번호가 일치하지 않습니다.'; ok = false }
  if (!form.agree) { error('이용약관에 동의해주세요.'); ok = false }
  return ok
}

async function goStep2() {
  if (!validate()) return
  try {
    await api.post('/auth/send-verification', { email: form.email })
    step.value = 2
  } catch (e) {
    error(e.message || '오류가 발생했습니다.')
  }
}

async function handleVerify() {
  if (!verifyCode.value) { verifyError.value = '인증 코드를 입력하세요.'; return }
  verifying.value = true
  try {
    await api.post('/auth/register', {
      ...form,
      role: role.value,
      verificationCode: verifyCode.value
    })
    success('회원가입이 완료되었습니다!')
    router.push('/login')
  } catch (e) {
    verifyError.value = e.message || '인증 코드가 올바르지 않습니다.'
  } finally {
    verifying.value = false
  }
}

async function resendCode() {
  try {
    await api.post('/auth/send-verification', { email: form.email })
    success('인증 코드를 재발송했습니다.')
  } catch (e) {
    error('재발송에 실패했습니다.')
  }
}
</script>

<style scoped lang="scss">
.register-page {
  flex: 1;
  display: flex;
  flex-direction: column;

  h2 {
    font-size: $font-size-2xl;
    font-weight: 700;
    margin-bottom: $spacing-2;
  }

  .subtitle {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-5;
  }
}

.role-tabs {
  display: flex;
  background: $bg-light;
  border-radius: $radius-md;
  padding: 4px;
  margin-bottom: $spacing-6;
}

.role-tab {
  flex: 1;
  padding: $spacing-2 $spacing-4;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-secondary;
  transition: all $transition-fast;

  &.active {
    background: $bg-white;
    color: $primary;
    box-shadow: $shadow-sm;
    font-weight: 600;
  }
}

.step-indicator {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-6;
  gap: $spacing-4;

  .step {
    display: flex;
    align-items: center;
    gap: $spacing-2;

    .step-num {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: $border;
      color: $text-muted;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: $font-size-sm;
      font-weight: 700;
    }

    .step-label {
      font-size: $font-size-sm;
      color: $text-muted;
    }

    &.active .step-num {
      background: $primary-light;
      color: white;
    }

    &.active .step-label {
      color: $primary;
      font-weight: 600;
    }

    &.done .step-num {
      background: $success;
      color: white;
    }
  }
}

.verify-step {
  text-align: center;
  padding: $spacing-4 0;

  .verify-icon {
    margin-bottom: $spacing-5;
  }

  h3 {
    font-size: $font-size-xl;
    font-weight: 700;
    margin-bottom: $spacing-2;
  }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-6;
  }
}

.verify-input {
  text-align: left;
}

.resend-btn {
  margin-top: $spacing-4;
  font-size: $font-size-sm;
  color: $primary-light;
  cursor: pointer;

  &:hover { text-decoration: underline; }
}

.login-link {
  margin-top: $spacing-6;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-secondary;

  a { color: $primary-light; font-weight: 600; &:hover { text-decoration: underline; } }
}
</style>
