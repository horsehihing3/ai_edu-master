<template>
  <div class="code-manage-page">
    <div class="page-header">
      <h1>코드 관리</h1>
      <button class="btn btn-primary btn-md" @click="openAddModal(null)">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        코드 추가
      </button>
    </div>

    <!-- 그룹 선택 -->
    <div class="group-bar">
      <div class="group-select-wrap">
        <select v-model="selectedGroup" class="group-select">
          <option value="">전체 그룹</option>
          <option v-for="g in groups" :key="g" :value="g">{{ g }}</option>
        </select>
        <svg class="group-select__arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
      </div>
      <span class="group-count">{{ groups.length }}개 그룹 · {{ visibleCodes }}개 코드</span>
    </div>

    <!-- 코드 트리 테이블 -->
    <div class="code-table-scroll">
    <div class="code-table">
      <div class="code-table__head">
        <span style="width:80px">ID</span>
        <span style="flex:1;min-width:120px">코드명</span>
        <span style="width:160px">코드 값</span>
        <span style="width:120px">부모</span>
        <span style="width:60px">순서</span>
        <span style="width:70px">상태</span>
        <span style="width:120px"></span>
      </div>

      <template v-if="!loading">
        <template v-for="code in rootCodes" :key="code.codeId">
          <!-- 루트 코드 -->
          <div class="code-row code-row--root">
            <span style="width:80px" class="code-id">{{ code.codeId }}</span>
            <span style="flex:1;min-width:120px" class="code-name">{{ code.codeName }}</span>
            <span style="width:160px" class="code-value">{{ code.codeValue }}</span>
            <span style="width:120px" class="text-muted">-</span>
            <span style="width:60px">{{ code.sortOrder }}</span>
            <span style="width:70px">
              <span :class="['status-dot', code.isActive ? 'active' : 'inactive']" />
            </span>
            <span style="width:120px" class="actions">
              <button class="btn btn-ghost btn-xs" @click="openAddModal(code)">하위 추가</button>
              <button class="btn btn-ghost btn-xs" @click="openEditModal(code)">수정</button>
            </span>
          </div>
          <!-- 자식 코드 -->
          <template v-for="child in getChildren(code.codeId)" :key="child.codeId">
            <div class="code-row code-row--child">
              <span style="width:80px" class="code-id">{{ child.codeId }}</span>
              <span style="flex:1;min-width:120px" class="code-name">
                <span class="indent">└</span> {{ child.codeName }}
              </span>
              <span style="width:160px" class="code-value">{{ child.codeValue }}</span>
              <span style="width:120px" class="text-muted">{{ code.codeName }}</span>
              <span style="width:60px">{{ child.sortOrder }}</span>
              <span style="width:70px">
                <span :class="['status-dot', child.isActive ? 'active' : 'inactive']" />
              </span>
              <span style="width:120px" class="actions">
                <button class="btn btn-ghost btn-xs" @click="openEditModal(child)">수정</button>
                <button class="btn btn-danger btn-xs" @click="deleteCode(child)">삭제</button>
              </span>
            </div>
          </template>
        </template>
        <div v-if="rootCodes.length === 0" class="empty-state">코드가 없습니다.</div>
      </template>
      <div v-else class="empty-state">불러오는 중...</div>
    </div>
    </div>

    <!-- 추가/수정 모달 -->
    <AppModal v-model="showModal" :title="editingCode ? '코드 수정' : '코드 추가'" size="sm">
      <div class="form-group">
        <label>그룹</label>
        <input v-model="form.codeGroup" class="form-control" placeholder="예: SUBJECT, UNIT" :disabled="!!editingCode" />
      </div>
      <div class="form-group">
        <label>코드 값</label>
        <input v-model="form.codeValue" class="form-control" placeholder="예: MATH, ENG_READING" />
      </div>
      <div class="form-group">
        <label>코드명</label>
        <input v-model="form.codeName" class="form-control" placeholder="예: 수학, 읽기" />
      </div>
      <div class="form-group">
        <label>부모 코드 ID <span class="text-muted">(없으면 비워두기)</span></label>
        <input v-model.number="form.parentCodeId" type="number" class="form-control" placeholder="부모 code_id" />
      </div>
      <div class="form-group">
        <label>정렬 순서</label>
        <input v-model.number="form.sortOrder" type="number" class="form-control" value="1" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showModal = false">취소</button>
        <button class="btn btn-primary" @click="saveCode">저장</button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import AppModal from '@/components/common/AppModal.vue'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const { success, error } = useToast()
const dialog = useDialog()

const loading = ref(false)
const groups = ref([])
const selectedGroup = ref('')
const codes = ref([])
const showModal = ref(false)
const editingCode = ref(null)
const form = reactive({ codeGroup: '', codeValue: '', codeName: '', parentCodeId: null, sortOrder: 1 })

const rootCodes = computed(() => {
  const roots = codes.value.filter(c => !c.parentCodeId)
  if (!selectedGroup.value) return roots
  return roots.filter(c => c.codeGroup === selectedGroup.value)
})

const visibleCodes = computed(() => {
  if (!selectedGroup.value) return codes.value.length
  const rootIds = new Set(rootCodes.value.map(c => c.codeId))
  return codes.value.filter(c => rootIds.has(c.codeId) || rootIds.has(c.parentCodeId)).length
})
function getChildren(parentId) {
  return codes.value.filter(c => c.parentCodeId === parentId)
}

async function fetchGroups() {
  const res = await api.get('/codes/admin/groups')
  groups.value = res.data || []
  if (groups.value.length && !selectedGroup.value) selectedGroup.value = groups.value[0]
}

async function fetchCodes() {
  loading.value = true
  try {
    const res = await api.get('/codes/admin')
    codes.value = res.data || []
  } catch {} finally { loading.value = false }
}

onMounted(async () => { await fetchGroups(); await fetchCodes() })
watch(selectedGroup, () => {})

function openAddModal(parent) {
  editingCode.value = null
  Object.assign(form, {
    codeGroup: parent ? parent.codeGroup : (selectedGroup.value || ''),
    codeValue: '',
    codeName: '',
    parentCodeId: parent?.codeId ?? null,
    sortOrder: 1
  })
  showModal.value = true
}

function openEditModal(code) {
  editingCode.value = code
  Object.assign(form, {
    codeGroup: code.codeGroup,
    codeValue: code.codeValue,
    codeName: code.codeName,
    parentCodeId: code.parentCodeId ?? null,
    sortOrder: code.sortOrder ?? 1
  })
  showModal.value = true
}

async function saveCode() {
  try {
    const payload = {
      codeGroup: form.codeGroup,
      codeValue: form.codeValue,
      codeName: form.codeName,
      parentCodeId: form.parentCodeId || null,
      sortOrder: form.sortOrder || 1,
      isActive: true
    }
    if (editingCode.value) {
      await api.put(`/codes/admin/${editingCode.value.codeId}`, payload)
      success('코드를 수정했습니다.')
    } else {
      await api.post('/codes/admin', payload)
      success('코드를 추가했습니다.')
    }
    showModal.value = false
    await fetchCodes()
    await fetchGroups()
  } catch { error('저장에 실패했습니다.') }
}

async function deleteCode(code) {
  const ok = await dialog.confirm(`"${code.codeName}" 코드를 삭제하시겠습니까?`, { type: 'danger', confirmText: '삭제' })
  if (!ok) return
  try {
    await api.delete(`/codes/admin/${code.codeId}`)
    success('삭제했습니다.')
    await fetchCodes()
  } catch { error('삭제에 실패했습니다.') }
}
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-5;
}

.group-bar {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  margin-bottom: $spacing-5;

  @media (max-width: $bp-mobile) {
    flex-direction: column;
    align-items: stretch;
    gap: $spacing-2;
  }
}

.group-select-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;

  @media (max-width: $bp-mobile) {
    display: flex;
    width: 100%;
  }

  .group-select__arrow {
    position: absolute;
    right: 10px;
    pointer-events: none;
    color: $text-muted;
  }
}

.group-select {
  appearance: none;
  padding: $spacing-2 36px $spacing-2 $spacing-3;
  border: 1px solid $border;
  border-radius: $radius-md;
  background: white;
  font-size: $font-size-sm;
  color: $text-primary;
  cursor: pointer;
  min-width: 200px;
  outline: none;
  transition: border-color $transition-fast;

  &:focus {
    border-color: $primary-light;
  }

  @media (max-width: $bp-mobile) {
    width: 100%;
    min-width: unset;
  }
}

.group-count {
  font-size: $font-size-xs;
  color: $text-muted;
}

.code-table-scroll {
  @media (max-width: $bp-mobile) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
}

.code-table {
  background: white;
  border: 1px solid $border;
  border-radius: $radius-lg;
  overflow: hidden;
  min-width: 700px;

  &__head {
    display: flex;
    align-items: center;
    padding: $spacing-3 $spacing-5;
    background: $bg-light;
    border-bottom: 1px solid $border;
    font-size: $font-size-xs;
    font-weight: 600;
    color: $text-muted;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    gap: $spacing-3;
  }
}

.code-row {
  display: flex;
  align-items: center;
  padding: $spacing-3 $spacing-5;
  border-bottom: 1px solid $border;
  gap: $spacing-3;
  font-size: $font-size-sm;
  transition: background 0.1s;

  &:last-child { border-bottom: none; }
  &:hover { background: $bg-light; }

  &--root {
    font-weight: 500;
    background: #fafafa;
    &:hover { background: $bg-light; }
  }
  &--child {
    padding-left: $spacing-5;
  }
}

.code-id { color: $text-muted; font-size: $font-size-xs; }
.code-name { color: $text-primary; }
.code-value { font-family: monospace; font-size: $font-size-xs; color: $primary; }
.text-muted { color: $text-muted; font-size: $font-size-xs; }

.indent {
  color: $text-muted;
  margin-right: 4px;
}

.status-dot {
  display: inline-block;
  width: 8px; height: 8px;
  border-radius: 50%;
  &.active { background: #10B981; }
  &.inactive { background: #D1D5DB; }
}

.actions {
  display: flex;
  gap: 4px;
}

.empty-state {
  padding: $spacing-10;
  text-align: center;
  color: $text-muted;
  font-size: $font-size-sm;
}

.form-group {
  margin-bottom: $spacing-4;
  label {
    display: block;
    font-size: $font-size-sm;
    font-weight: 500;
    margin-bottom: $spacing-1;
    color: $text-primary;
  }
}

.btn-xs {
  padding: 2px 8px;
  font-size: 11px;
  height: 24px;
}
</style>
