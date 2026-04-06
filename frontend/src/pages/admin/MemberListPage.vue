<template>
  <div class="member-list-page">
    <div class="page-header"><h1>회원 관리</h1></div>

    <div class="filter-bar">
      <input v-model="search" class="form-control filter-search" placeholder="이름, 이메일 검색..." />
      <div class="filter-selects">
        <AppSelect v-model="filterRole" :options="roleOptions" placeholder="전체 역할" style="margin:0;" />
        <AppSelect v-model="filterStatus" :options="statusOptions" placeholder="전체 상태" style="margin:0;" />
      </div>
    </div>

    <AppTable :columns="columns" :data="filteredMembers" :loading="loading">
      <template #cell-name="{ row }">
        <div style="display:flex; align-items:center; gap:8px;">
          <div class="avatar-sm">{{ row.name.charAt(0) }}</div>
          {{ row.name }}
        </div>
      </template>
      <template #cell-role="{ value }">
        <span :class="['role-badge', value.toLowerCase()]">{{ roleLabel(value) }}</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['status-dot', value === 'ACTIVE' ? 'active' : 'inactive']" /> {{ value === 'ACTIVE' ? '활성' : '비활성' }}
      </template>
      <template #cell-actions="{ row }">
        <div style="display:flex;gap:6px;">
          <button v-if="row.role === 'STUDENT'" class="btn btn-ghost btn-sm" @click="openReassignModal(row)">재배정</button>
          <button class="btn btn-ghost btn-sm" @click="resetPassword(row)">비밀번호 초기화</button>
          <button
            :class="['btn btn-sm', row.status === 'ACTIVE' ? 'btn-danger' : 'btn-secondary']"
            @click="toggleStatus(row)"
          >{{ row.status === 'ACTIVE' ? '비활성화' : '활성화' }}</button>
        </div>
      </template>
    </AppTable>

    <!-- [2026-04-06] 학생 학급 재배정 모달 -->
    <Teleport to="body">
      <div v-if="reassignModal.open" class="modal-backdrop" @click.self="closeReassignModal">
        <div class="modal-box">
          <div class="modal-header">
            <h2>학급 재배정</h2>
            <button class="modal-close" @click="closeReassignModal">✕</button>
          </div>
          <div class="modal-body">
            <p class="reassign-info"><strong>{{ reassignModal.studentName }}</strong> 학생의 학급을 변경합니다.</p>
            <div class="form-group">
              <label class="form-label">현재 학급</label>
              <div v-if="!reassignModal.currentClasses.length" class="no-class-notice">
                배정된 학급이 없습니다.
              </div>
              <select v-else v-model="reassignModal.fromClassId" class="form-select">
                <option value="">현재 학급 선택</option>
                <option v-for="cls in reassignModal.currentClasses" :key="cls.classId" :value="cls.classId">
                  {{ cls.className }} ({{ cls.teacherName }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">이동할 학급</label>
              <select v-model="reassignModal.toClassId" class="form-select">
                <option value="">이동할 학급 선택</option>
                <option v-for="cls in allClasses" :key="cls.classId" :value="cls.classId">
                  {{ cls.className }} ({{ cls.teacherName }})
                </option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary btn-md" @click="closeReassignModal">취소</button>
            <button
              class="btn btn-primary btn-md"
              :disabled="!reassignModal.fromClassId || !reassignModal.toClassId"
              @click="submitReassign"
            >재배정</button>
          </div>
        </div>
      </div>
    </Teleport>

    <AppPagination :current-page="page" :total-pages="totalPages" :total-elements="totalElements" @page-change="page = $event" />
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import AppSelect from '@/components/common/AppSelect.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const { success, error } = useToast()
const dialog = useDialog()
const search = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const loading = ref(false)
const page = ref(1)

const roleOptions = [
  { value: 'STUDENT', label: '학생' },
  { value: 'TEACHER', label: '교사' },
  { value: 'ADMIN', label: '관리자' }
]

const statusOptions = [
  { value: 'ACTIVE', label: '활성' },
  { value: 'INACTIVE', label: '비활성' }
]

const columns = [
  { key: 'id', label: 'ID', sortable: true },
  { key: 'name', label: '이름', sortable: true },
  { key: 'email', label: '이메일' },
  { key: 'role', label: '역할' },
  { key: 'status', label: '상태' },
  { key: 'joinedAt', label: '가입일', sortable: true },
  { key: 'actions', label: '' }
]

const members = ref([])
const totalPages = ref(1)
const totalElements = ref(0)

const filteredMembers = computed(() => members.value)

async function fetchMembers() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 15 }
    if (search.value) params.keyword = search.value
    if (filterRole.value) params.role = filterRole.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await api.get('/admin/users', { params })
    members.value = (res.data?.content || res.data || []).map(m => ({
      id: m.userId, userId: m.userId, name: m.name, email: m.email, role: m.role,
      status: m.isActive ? 'ACTIVE' : 'INACTIVE', joinedAt: m.createdAt?.slice(0,10)
    }))
    totalPages.value = res.data?.totalPages || 1
    totalElements.value = res.data?.totalElements || members.value.length
  } catch {} finally { loading.value = false }
}

watch([search, filterRole, filterStatus], () => { page.value = 1; fetchMembers() })
watch(page, fetchMembers)
onMounted(fetchMembers)

function roleLabel(v) {
  return { STUDENT: '학생', TEACHER: '교사', ADMIN: '관리자' }[v] || v
}

async function resetPassword(member) {
  const ok = await dialog.confirm(`${member.name}의 비밀번호를 초기화하시겠습니까?\n임시 비밀번호가 이메일로 발송됩니다.`)
  if (!ok) return
  try {
    await api.post(`/admin/users/${member.userId}/password/reset`)
    success(`${member.name}의 비밀번호를 초기화했습니다. 이메일로 임시 비밀번호가 발송됩니다.`)
  } catch (e) {
    error('비밀번호 초기화에 실패했습니다.')
  }
}

function toggleStatus(member) {
  member.status = member.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  success(`${member.name}의 상태를 변경했습니다.`)
}

// [2026-04-06] 학생 학급 재배정
const allClasses = ref([])
const reassignModal = reactive({
  open: false, studentId: null, studentName: '',
  currentClasses: [], fromClassId: '', toClassId: ''
})

async function openReassignModal(member) {
  try {
    if (!allClasses.value.length) {
      const res = await api.get('/admin/classes')
      allClasses.value = res.data?.data || res.data || []
    }
  } catch {
    error('학급 목록을 불러오지 못했습니다.')
    return
  }

  let currentClasses = []
  try {
    const res = await api.get(`/admin/users/${member.userId}/classes`)
    currentClasses = res.data?.data || res.data || []
  } catch {
    // 소속 학급 없는 학생이거나 조회 실패 시 빈 목록으로 진행
  }

  Object.assign(reassignModal, {
    open: true, studentId: member.userId, studentName: member.name,
    currentClasses, fromClassId: '', toClassId: ''
  })
}

function closeReassignModal() { reassignModal.open = false }

async function submitReassign() {
  if (!reassignModal.fromClassId || !reassignModal.toClassId) return
  const ok = await dialog.confirm(`${reassignModal.studentName} 학생의 학급을 재배정하시겠습니까?`, { confirmText: '재배정' })
  if (!ok) return
  try {
    await api.post(`/admin/users/${reassignModal.studentId}/reassign`, {
      fromClassId: reassignModal.fromClassId,
      toClassId: reassignModal.toClassId
    })
    success(`${reassignModal.studentName} 학생이 재배정되었습니다.`)
    closeReassignModal()
  } catch { error('재배정에 실패했습니다.') }
}
</script>

<style scoped lang="scss">
.filter-bar {
  display: flex;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
  flex-wrap: wrap;
}

.filter-search {
  width: 240px;

  @media (max-width: $bp-mobile) {
    width: 100%;
  }
}

.filter-selects {
  display: flex;
  gap: $spacing-3;

  @media (max-width: $bp-mobile) {
    width: 100%;

    > * {
      flex: 1;
      min-width: 0;
    }
  }
}

.avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: $primary-bg;
  color: $primary;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xs;
  font-weight: 700;
}

.role-badge {
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;

  &.student { background: #DBEAFE; color: #1E40AF; }
  &.teacher { background: #D1FAE5; color: #065F46; }
  &.admin { background: #EDE9FE; color: #5B21B6; }
}

// [2026-04-06] 재배정 모달
.modal-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; padding: 16px;
}
.modal-box {
  background: #fff; border-radius: 12px; width: 100%; max-width: 440px;
  max-height: 85vh; display: flex; flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25);
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 24px; border-bottom: 1px solid $border;
  h2 { font-size: $font-size-lg; font-weight: 700; }
}
.modal-close {
  width: 32px; height: 32px; border-radius: 6px; font-size: 16px;
  color: $text-muted; background: transparent; border: none;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  &:hover { background: $bg-light; }
}
.modal-body {
  flex: 1; overflow-y: auto; padding: 20px 24px;
  display: flex; flex-direction: column; gap: 16px;
}
.modal-footer {
  display: flex; justify-content: flex-end; gap: 12px;
  padding: 16px 24px; border-top: 1px solid $border;
}
.form-group { display: flex; flex-direction: column; gap: 4px; }
.form-label { font-size: $font-size-sm; font-weight: 600; color: $text-secondary; }
.form-select {
  height: 40px; padding: 0 12px; border: 1px solid $border;
  border-radius: 6px; font-size: $font-size-sm; background: #fff;
  color: $text-primary; outline: none; width: 100%;
  &:focus { border-color: $primary; }
}
.reassign-info { font-size: $font-size-sm; color: $text-secondary; }
.no-class-notice {
  padding: 8px 12px;
  background: #FEF9C3;
  border: 1px solid #FDE68A;
  border-radius: 6px;
  font-size: $font-size-sm;
  color: #92400E;
}
.status-dot {
  display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px;
  &.active { background: #10B981; }
  &.inactive { background: #9CA3AF; }
}
</style>
