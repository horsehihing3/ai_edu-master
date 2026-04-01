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
          <button class="btn btn-ghost btn-sm" @click="resetPassword(row)">비밀번호 초기화</button>
          <button
            :class="['btn btn-sm', row.status === 'ACTIVE' ? 'btn-danger' : 'btn-secondary']"
            @click="toggleStatus(row)"
          >{{ row.status === 'ACTIVE' ? '비활성화' : '활성화' }}</button>
        </div>
      </template>
    </AppTable>

    <AppPagination :current-page="page" :total-pages="totalPages" :total-elements="totalElements" @page-change="page = $event" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import AppSelect from '@/components/common/AppSelect.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const { success } = useToast()
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
</style>
