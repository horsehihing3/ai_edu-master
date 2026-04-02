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

    <!-- 회원가입 폼 -->
    <form @submit.prevent="handleRegister">
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

      <AppButton type="submit" :loading="submitting" block>가입하기</AppButton>
    </form>

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
const submitting = ref(false)

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

// [2026-04-02] 2단계 이메일 인증 → 1단계 직접 가입으로 변경 (백엔드 미지원)
async function handleRegister() {
  if (!validate()) return
  submitting.value = true
  try {
    await api.post('/auth/register', {
      name: form.name,
      email: form.email,
      password: form.password,
      role: role.value
    })
    success('회원가입이 완료되었습니다! 로그인해주세요.')
    router.push('/login')
  } catch (e) {
    error(e.message || '회원가입에 실패했습니다.')
  } finally {
    submitting.value = false
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


.login-link {
  margin-top: $spacing-6;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-secondary;

  a { color: $primary-light; font-weight: 600; &:hover { text-decoration: underline; } }
}
</style>
