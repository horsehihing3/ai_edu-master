<template>
  <div class="student-list-page">
    <div class="page-header"><h1>학생 관리</h1></div>

    <div class="filter-bar">
      <input v-model="search" class="form-control" placeholder="학생 이름 검색..." style="width:220px;" />
      <AppSelect v-model="filterGrade" :options="gradeOptions" placeholder="전체 학년" style="width:160px; margin:0;" />
      <AppSelect v-model="filterLevel" :options="levelOptions" placeholder="전체 레벨" style="width:140px; margin:0;" />
    </div>

    <AppTable :columns="columns" :data="filteredStudents" :loading="loading">
      <template #cell-name="{ row }">
        <RouterLink :to="`/teacher/students/${row.id}`" class="student-name-link">
          <div class="avatar-sm">{{ row.name.charAt(0) }}</div>
          {{ row.name }}
        </RouterLink>
      </template>
      <template #cell-level="{ value }">
        <AppBadge :type="value" />
      </template>
      <template #cell-completionRate="{ value }">
        <div style="display:flex; align-items:center; gap:8px;">
          <div class="progress-bar" style="width:80px;">
            <div class="progress-bar__fill" :style="{ width: value + '%' }" />
          </div>
          <span>{{ value }}%</span>
        </div>
      </template>
      <template #cell-actions="{ row }">
        <RouterLink :to="`/teacher/students/${row.id}`" class="btn btn-ghost btn-sm">상세보기</RouterLink>
      </template>
    </AppTable>

    <AppPagination :current-page="page" :total-pages="totalPages" :total-elements="totalElements" @page-change="page = $event" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppSelect from '@/components/common/AppSelect.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import api from '@/utils/api'

const search = ref('')
const filterGrade = ref('')
const filterLevel = ref('')
const loading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)

const gradeOptions = ['고1','고2','고3','중1','중2','중3'].map(v => ({ value: v, label: v }))
const levelOptions = ['A','B','C'].map(v => ({ value: v, label: `${v} 레벨` }))

const columns = [
  { key: 'name', label: '이름', sortable: true },
  { key: 'grade', label: '학년', sortable: true },
  { key: 'level', label: '레벨' },
  { key: 'completionRate', label: '완료율', sortable: true },
  { key: 'accuracy', label: '정답률', sortable: true },
  { key: 'lastStudy', label: '마지막 학습' },
  { key: 'actions', label: '' }
]

const students = ref([])

const filteredStudents = computed(() => students.value)

async function fetchStudents() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 10 }
    if (search.value) params.name = search.value
    if (filterGrade.value) params.grade = filterGrade.value
    if (filterLevel.value) params.level = filterLevel.value
    const res = await api.get('/teacher/students', { params })
    const data = res.data?.content || res.data || []
    students.value = data.map(s => ({
      id: s.studentId, name: s.name, grade: s.grade, level: s.studentLevel || s.level,
      completionRate: s.completionRate || 0, accuracy: s.accuracy || 0, lastStudy: s.lastStudy || '-'
    }))
    totalPages.value = res.data?.totalPages || 1
    totalElements.value = res.data?.totalElements || students.value.length
  } catch {} finally { loading.value = false }
}

watch([search, filterGrade, filterLevel], () => { page.value = 1; fetchStudents() })
watch(page, fetchStudents)
onMounted(fetchStudents)
</script>

<style scoped lang="scss">
.filter-bar {
  display: flex;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
  flex-wrap: wrap;
}

.student-name-link {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  text-decoration: none;
  color: $text-primary;
  font-weight: 500;

  &:hover { color: $primary-light; }
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
</style>
