<template>
  <div class="settings-page">
    <div class="page-header"><h1>설정</h1></div>

    <div class="settings-layout">
      <!-- 탭 사이드바 -->
      <nav class="settings-nav">
        <button
          v-for="t in tabs"
          :key="t.value"
          :class="['settings-tab', { active: activeTab === t.value }]"
          @click="activeTab = t.value"
        >
          <span v-html="t.icon" />
          {{ t.label }}
        </button>
      </nav>

      <!-- 탭 컨텐츠 -->
      <div class="settings-content">
        <!-- 프로필 수정 -->
        <div v-if="activeTab === 'profile'" class="card">
          <h3>프로필 수정</h3>
          <div class="avatar-section">
            <div class="avatar-large">
              <img v-if="profileImg" :src="profileImg" alt="프로필" style="width:100%;height:100%;object-fit:cover;border-radius:50%;" />
              <span v-else>{{ user?.name?.charAt(0) }}</span>
            </div>
            <div>
              <button class="btn btn-secondary btn-sm" @click="$refs.fileInput.click()">사진 변경</button>
              <input ref="fileInput" type="file" accept="image/*" style="display:none;" @change="onFileChange" />
              <p class="avatar-hint">JPG, PNG 최대 2MB</p>
            </div>
          </div>
          <AppInput v-model="profile.name" label="이름" placeholder="이름" />
          <AppInput v-model="profile.email" label="이메일" placeholder="이메일" type="email" disabled />
          <AppInput v-model="profile.phone" label="전화번호" placeholder="010-0000-0000" />
          <div class="form-actions">
            <AppButton :loading="saving" @click="saveProfile">변경 저장</AppButton>
          </div>
        </div>

        <!-- 비밀번호 변경 -->
        <div v-if="activeTab === 'password'" class="card">
          <h3>비밀번호 변경</h3>
          <AppInput v-model="pw.current" label="현재 비밀번호" type="password" placeholder="현재 비밀번호" />
          <AppInput v-model="pw.newPw" label="새 비밀번호" type="password" placeholder="8자 이상" />
          <AppInput v-model="pw.confirm" label="새 비밀번호 확인" type="password" placeholder="비밀번호 재입력" :error="pw.confirm && pw.newPw !== pw.confirm ? '비밀번호가 일치하지 않습니다.' : ''" />
          <div class="form-actions">
            <AppButton :loading="saving" @click="changePassword">비밀번호 변경</AppButton>
          </div>
        </div>

        <!-- 알림 설정 -->
        <div v-if="activeTab === 'notifications'" class="card">
          <h3>알림 설정</h3>
          <div class="notify-list">
            <div v-for="n in notifications" :key="n.key" class="notify-row">
              <div>
                <p class="notify-title">{{ n.title }}</p>
                <p class="notify-desc">{{ n.desc }}</p>
              </div>
              <div class="toggle" :class="{ on: n.enabled }" @click="n.enabled = !n.enabled">
                <div class="toggle-knob" />
              </div>
            </div>
          </div>
          <div class="form-actions">
            <AppButton :loading="saving" @click="saveNotifications">저장</AppButton>
          </div>
        </div>

        <!-- 계정 탈퇴 -->
        <div v-if="activeTab === 'delete'" class="card danger-zone">
          <h3>계정 탈퇴</h3>
          <p>계정을 탈퇴하면 모든 데이터가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.</p>
          <div class="form-actions">
            <AppButton variant="danger" @click="deleteAccount">계정 탈퇴</AppButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import AppInput from '@/components/common/AppInput.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useAuthStore } from '@/store/auth'
import { useToast } from '@/composables/useToast'
import { useAuth } from '@/composables/useAuth'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const authStore = useAuthStore()
const { logout } = useAuth()
const { success, error } = useToast()
const dialog = useDialog()
const user = computed(() => authStore.user)
const activeTab = ref('profile')
const saving = ref(false)
const profileImg = ref(user.value?.profileImgUrl || null)

function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) { error('파일 크기는 2MB 이하여야 합니다.'); return }
  const reader = new FileReader()
  reader.onload = ev => { profileImg.value = ev.target.result }
  reader.readAsDataURL(file)
}

const tabs = [
  { value: 'profile', label: '프로필', icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>` },
  { value: 'password', label: '비밀번호', icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>` },
  { value: 'notifications', label: '알림', icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>` },
  { value: 'delete', label: '계정 탈퇴', icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>` }
]

const profile = reactive({
  name: user.value?.name || '',
  email: user.value?.email || '',
  phone: user.value?.phone || ''
})

const pw = reactive({ current: '', newPw: '', confirm: '' })

const notifications = ref([
  { key: 'assignment', title: '과제 알림', desc: '새 과제가 배정될 때 알림을 받습니다.', enabled: true },
  { key: 'answer', title: '문의 답변 알림', desc: '1:1 문의에 답변이 달릴 때 알림을 받습니다.', enabled: true },
  { key: 'announcement', title: '공지사항 알림', desc: '새 공지사항이 등록될 때 알림을 받습니다.', enabled: false }
])

async function saveProfile() {
  saving.value = true
  try {
    await api.put('/settings/profile', {
      name: profile.name,
      phone: profile.phone,
      profileImgUrl: profileImg.value || ''
    })
    authStore.user.name = profile.name
    authStore.user.phone = profile.phone
    authStore.user.profileImgUrl = profileImg.value || ''
    success('프로필을 저장했습니다.')
  } catch (e) {
    error(e.message)
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (!pw.current || !pw.newPw) { error('모든 항목을 입력하세요.'); return }
  if (pw.newPw !== pw.confirm) { error('비밀번호가 일치하지 않습니다.'); return }
  saving.value = true
  try {
    await api.put('/settings/password', { currentPassword: pw.current, newPassword: pw.newPw })
    success('비밀번호를 변경했습니다.')
    Object.assign(pw, { current: '', newPw: '', confirm: '' })
  } catch (e) {
    error(e.message)
  } finally {
    saving.value = false
  }
}

async function saveNotifications() {
  saving.value = true
  try {
    await api.put('/settings/notifications', { settings: notifications.value })
    success('알림 설정을 저장했습니다.')
  } catch (e) {
    error(e.message)
  } finally {
    saving.value = false
  }
}

async function deleteAccount() {
  const ok = await dialog.confirm('정말 계정을 탈퇴하시겠습니까?\n이 작업은 되돌릴 수 없습니다.', { type: 'danger', confirmText: '탈퇴하기' })
  if (ok) {
    try {
      await api.delete('/settings/account')
      logout()
    } catch (e) {
      error(e.message)
    }
  }
}
</script>

<style scoped lang="scss">
.settings-layout {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: $spacing-6;
  align-items: start;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }
}

.settings-nav {
  background: $bg-white;
  border-radius: $radius-lg;
  border: 1px solid $border;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  @media (max-width: $bp-tablet) {
    flex-direction: row;
    overflow-x: auto;
  }
}

.settings-tab {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-4 $spacing-5;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-secondary;
  border-left: 3px solid transparent;
  text-align: left;
  transition: all $transition-fast;
  white-space: nowrap;

  &:hover { background: $bg-light; color: $text-primary; }
  &.active { background: $primary-bg; color: $primary; border-left-color: $primary-light; font-weight: 600; }

  @media (max-width: $bp-tablet) {
    border-left: none;
    border-bottom: 3px solid transparent;

    &.active { border-bottom-color: $primary-light; border-left: none; }
  }
}

.settings-content {
  h3 {
    font-size: $font-size-xl;
    font-weight: 700;
    margin-bottom: $spacing-6;
  }
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: $spacing-5;
  margin-bottom: $spacing-6;
}

.avatar-hint {
  font-size: $font-size-xs;
  color: $text-muted;
  margin-top: $spacing-1;
}

.form-actions {
  margin-top: $spacing-4;
  display: flex;
  justify-content: flex-end;
}

.avatar-large {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: $primary;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-3xl;
  font-weight: 700;
}

.notify-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  margin-bottom: $spacing-6;
}

.notify-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-4;
  border: 1px solid $border;
  border-radius: $radius-md;
  gap: $spacing-4;

  .notify-title {
    font-size: $font-size-base;
    font-weight: 600;
    margin-bottom: 2px;
  }

  .notify-desc {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.toggle {
  width: 44px;
  height: 24px;
  border-radius: $radius-full;
  background: $border;
  position: relative;
  cursor: pointer;
  transition: background $transition-fast;
  flex-shrink: 0;

  &.on {
    background: $primary-light;

    .toggle-knob { transform: translateX(20px); }
  }

  .toggle-knob {
    position: absolute;
    top: 2px;
    left: 2px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: white;
    box-shadow: $shadow-sm;
    transition: transform $transition-fast;
  }
}

.danger-zone {
  border: 1px solid $danger;

  h3 { color: $danger; }

  p {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin-bottom: $spacing-5;
    line-height: 1.6;
  }
}
</style>
