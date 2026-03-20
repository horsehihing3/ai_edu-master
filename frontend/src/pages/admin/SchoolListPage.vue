<template>
  <div class="school-list-page">
    <div class="page-header">
      <h1>학원/학교 관리</h1>
      <button class="btn btn-primary btn-md" @click="openCreateModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        기관 추가
      </button>
    </div>

    <AppTable :columns="columns" :data="schools" :loading="loading">
      <template #cell-type="{ value }">
        <span :class="['type-badge', value]">{{ value === 'academy' ? '학원' : '학교' }}</span>
      </template>
      <template #cell-actions="{ row }">
        <div style="display:flex;gap:6px;">
          <button class="btn btn-ghost btn-sm" @click="editSchool(row)">수정</button>
          <button class="btn btn-danger btn-sm" @click="deleteSchool(row.id)">삭제</button>
        </div>
      </template>
    </AppTable>

    <AppModal v-model="showModal" :title="editingSchool ? '기관 수정' : '기관 추가'" size="sm">
      <AppInput v-model="form.name" label="기관명" placeholder="기관명을 입력하세요" required />
      <AppSelect v-model="form.type" label="유형" :options="typeOptions" placeholder="유형 선택" />
      <AppInput v-model="form.address" label="주소" placeholder="주소를 입력하세요" />
      <template #footer>
        <AppButton variant="secondary" @click="showModal = false">취소</AppButton>
        <AppButton @click="saveSchool">저장</AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import AppModal from '@/components/common/AppModal.vue'
import AppInput from '@/components/common/AppInput.vue'
import AppSelect from '@/components/common/AppSelect.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const { success, error } = useToast()
const dialog = useDialog()
const loading = ref(false)
const showModal = ref(false)
const editingSchool = ref(null)

const form = reactive({ name: '', type: '', address: '' })
const typeOptions = [{ value: 'academy', label: '학원' }, { value: 'school', label: '학교' }]

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: '기관명', sortable: true },
  { key: 'type', label: '유형' },
  { key: 'teacherCount', label: '교사 수' },
  { key: 'studentCount', label: '학생 수' },
  { key: 'address', label: '주소' },
  { key: 'actions', label: '' }
]

const schools = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/schools')
    schools.value = (res.data?.content || res.data || []).map(s => ({
      id: s.schoolId, name: s.name, type: s.schoolType?.toLowerCase() || 'academy',
      teacherCount: s.teacherCount || 0, studentCount: s.studentCount || 0, address: s.address || ''
    }))
  } catch {} finally { loading.value = false }
})

function openCreateModal() {
  editingSchool.value = null
  Object.assign(form, { name: '', type: '', address: '' })
  showModal.value = true
}

function editSchool(school) {
  editingSchool.value = school
  Object.assign(form, { name: school.name, type: school.type, address: school.address })
  showModal.value = true
}

async function saveSchool() {
  try {
    if (editingSchool.value) {
      await api.put(`/admin/schools/${editingSchool.value.id}`, form)
      Object.assign(editingSchool.value, form)
      success('기관 정보를 수정했습니다.')
    } else {
      const res = await api.post('/admin/schools', form)
      const s = res.data || {}
      schools.value.push({ id: s.schoolId || Date.now(), ...form, teacherCount: 0, studentCount: 0 })
      success('기관을 추가했습니다.')
    }
    showModal.value = false
  } catch { error('저장에 실패했습니다.') }
}

async function deleteSchool(id) {
  const ok = await dialog.confirm('기관을 삭제하시겠습니까?', { type: 'danger', confirmText: '삭제' })
  if (ok) {
    try {
      await api.delete(`/admin/schools/${id}`)
      schools.value = schools.value.filter(s => s.id !== id)
      success('기관을 삭제했습니다.')
    } catch { error('삭제에 실패했습니다.') }
  }
}
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.type-badge {
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;

  &.academy { background: #DBEAFE; color: #1E40AF; }
  &.school { background: #D1FAE5; color: #065F46; }
}
</style>
