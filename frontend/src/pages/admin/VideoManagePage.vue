<template>
  <div class="video-manage-page">
    <div class="page-header">
      <h1>동영상 관리</h1>
      <button class="btn btn-primary btn-md" @click="openAddModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        동영상 추가
      </button>
    </div>

    <AppTable :columns="columns" :data="videos" :loading="loading">
      <template #cell-title="{ value, row }">
        <span class="video-title-link" @click="openDetail(row.id)">{{ value }}</span>
      </template>
      <template #cell-level="{ value }">
        <AppBadge :type="value" />
      </template>
      <template #cell-status="{ value }">
        <span :class="['mini-badge', value]">{{ value === 'active' ? '활성' : '비활성' }}</span>
      </template>
      <template #cell-actions="{ row }">
        <button class="btn btn-ghost btn-sm" @click="openDetail(row.id)">상세</button>
      </template>
    </AppTable>

    <!-- 동영상 추가 모달 -->
    <div v-if="addModal.open" class="modal-overlay" @click.self="addModal.open = false">
      <div class="modal">
        <div class="modal__header">
          <h3>동영상 추가</h3>
          <button class="modal-close" @click="addModal.open = false">✕</button>
        </div>
        <div class="modal__body">
          <VideoFormFields :form="form" :level-options="levelOptions" :subject-options="subjectOptions" @upload="onUpload(form, $event)" />
        </div>
        <div class="modal__footer">
          <button class="btn btn-ghost btn-sm" @click="addModal.open = false">취소</button>
          <button class="btn btn-primary btn-sm" :disabled="addModal.submitting" @click="addVideo">
            {{ addModal.submitting ? '추가 중...' : '추가' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 상세/수정 모달 -->
    <div v-if="detail.open" class="modal-overlay" @click.self="detail.open = false">
      <div class="modal">
        <div class="modal__header">
          <h3>{{ detail.editing ? '동영상 수정' : '동영상 상세' }}</h3>
          <button class="modal-close" @click="detail.open = false">✕</button>
        </div>
        <div v-if="detail.loading" class="modal__body modal__body--center">
          <span style="color:#9CA3AF;font-size:14px;">불러오는 중...</span>
        </div>
        <div v-else-if="detail.data" class="modal__body">

          <!-- 보기 모드 -->
          <template v-if="!detail.editing">
            <div class="detail-grid">
              <div class="detail-row"><span class="detail-label">제목</span><span class="detail-value detail-value--bold">{{ detail.data.title }}</span></div>
              <div class="detail-row"><span class="detail-label">레벨</span><span class="detail-value">{{ detail.data.level || '-' }}</span></div>
              <div class="detail-row"><span class="detail-label">단원</span><span class="detail-value">{{ detail.data.unit || '-' }}</span></div>
              <div class="detail-row"><span class="detail-label">조회수</span><span class="detail-value">{{ (detail.data.views || 0).toLocaleString() }}회</span></div>
              <div class="detail-row"><span class="detail-label">재생 시간</span><span class="detail-value">{{ detail.data.durationSec ? formatDuration(detail.data.durationSec) : '-' }}</span></div>
              <div class="detail-row"><span class="detail-label">파일 크기</span><span class="detail-value">{{ detail.data.fileSizeMb ? `${detail.data.fileSizeMb} MB` : '-' }}</span></div>
              <div class="detail-row"><span class="detail-label">상태</span><span :class="['mini-badge', detail.data.status]">{{ detail.data.status === 'active' ? '활성' : '비활성' }}</span></div>
              <div class="detail-row"><span class="detail-label">등록일</span><span class="detail-value">{{ detail.data.createdAt?.slice(0, 10) || '-' }}</span></div>
            </div>
            <div v-if="detail.data.videoUrl || detail.data.cdnUrl" class="detail-url">
              <span class="detail-label">동영상 URL</span>
              <a :href="detail.data.videoUrl || detail.data.cdnUrl" target="_blank" rel="noopener" class="url-link">{{ detail.data.videoUrl || detail.data.cdnUrl }}</a>
            </div>
            <div v-if="detail.data.thumbnailUrl" class="detail-thumb">
              <img :src="detail.data.thumbnailUrl" alt="썸네일" />
            </div>
            <div class="detail-actions">
              <button class="btn btn-ghost btn-sm" @click="startEdit">수정</button>
              <button class="btn btn-danger btn-sm" @click="deleteVideoFromDetail">삭제</button>
            </div>
          </template>

          <!-- 수정 모드 -->
          <template v-else>
            <VideoFormFields :form="editForm" :level-options="levelOptions" :subject-options="subjectOptions" @upload="onUpload(editForm, $event)" />
            <div class="detail-actions">
              <button class="btn btn-ghost btn-sm" @click="detail.editing = false">취소</button>
              <button class="btn btn-primary btn-sm" :disabled="detail.saving" @click="saveEdit">
                {{ detail.saving ? '저장 중...' : '저장' }}
              </button>
            </div>
          </template>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import AppTable from '@/components/common/AppTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import VideoFormFields from '@/components/admin/VideoFormFields.vue'
import { useToast } from '@/composables/useToast'
import { useDialog } from '@/composables/useDialog'
import api from '@/utils/api'

const { success, error } = useToast()
const dialog = useDialog()
const loading = ref(false)

const subjectOptions = ['수학','영어','국어','과학','사회'].map(v => ({ value: v, label: v }))
const levelOptions = ['A','B','C'].map(v => ({ value: v, label: `${v} 레벨` }))

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: '제목', sortable: true },
  { key: 'subject', label: '과목' },
  { key: 'level', label: '레벨' },
  { key: 'unit', label: '단원' },
  { key: 'views', label: '조회수', sortable: true },
  { key: 'status', label: '상태' },
  { key: 'actions', label: '' }
]

const videos = ref([])

// ── 폼 팩토리 ──────────────────────────────────────────────────────────────────
function makeForm(defaults = {}) {
  return reactive({
    title: '', subject: '', level: '', unit: '', thumbnailUrl: '',
    isActive: true,
    videoMode: 'url', videoUrl: '',
    uploadFile: null, uploading: false, uploadProgress: 0, uploadedUrl: '',
    _touched: false,
    ...defaults
  })
}

const addModal = reactive({ open: false, submitting: false })
const form = makeForm()

const detail = reactive({ open: false, loading: false, data: null, editing: false, saving: false })
const editForm = makeForm()

// ── 파일 업로드 ────────────────────────────────────────────────────────────────
async function onUpload(target, file) {
  target.uploadFile = file
  target.uploadedUrl = ''
  target.uploading = true
  target.uploadProgress = 0

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await api.post('/admin/videos/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: e => {
        target.uploadProgress = Math.round((e.loaded / e.total) * 100)
      }
    })
    target.uploadedUrl = res.data?.fileUrl || res.data
    target.videoUrl = target.uploadedUrl
  } catch { error('파일 업로드에 실패했습니다.') } finally { target.uploading = false }
}

// ── 목록 로드 ──────────────────────────────────────────────────────────────────
onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/videos')
    videos.value = (res.data?.content || res.data || []).map(mapVideo)
  } catch {} finally { loading.value = false }
})

function mapVideo(v) {
  return {
    id: v.videoId, title: v.title, subject: v.subject, level: v.level,
    unit: v.unitName, views: v.viewCount || 0, status: v.isActive ? 'active' : 'inactive'
  }
}

// ── 추가 ───────────────────────────────────────────────────────────────────────
function openAddModal() {
  Object.assign(form, makeForm())
  addModal.open = true
}

async function addVideo() {
  form._touched = true
  if (!form.title.trim()) { error('제목을 입력하세요.'); return }
  if (!form.subject) { error('과목을 선택하세요.'); return }
  if (!form.level) { error('레벨을 선택하세요.'); return }
  if (form.uploading) { error('업로드가 진행 중입니다.'); return }
  const videoUrl = form.videoMode === 'file' ? form.uploadedUrl : form.videoUrl
  if (!videoUrl) { error('동영상 URL을 입력하거나 파일을 업로드하세요.'); return }

  addModal.submitting = true
  try {
    const res = await api.post('/admin/videos', {
      title: form.title, subject: form.subject, level: form.level,
      unitName: form.unit, videoUrl, isActive: form.isActive
    })
    const v = res.data || {}
    videos.value.unshift({
      id: v.videoId || Date.now(), title: form.title, subject: form.subject,
      level: form.level, unit: form.unit, views: 0,
      status: form.isActive ? 'active' : 'inactive'
    })
    success('동영상을 추가했습니다.')
    addModal.open = false
  } catch { error('추가에 실패했습니다.') } finally { addModal.submitting = false }
}

// ── 상세 ───────────────────────────────────────────────────────────────────────
async function openDetail(id) {
  detail.open = true
  detail.loading = true
  detail.data = null
  detail.editing = false
  try {
    const res = await api.get(`/videos/${id}`)
    const v = res.data || {}
    detail.data = {
      id, title: v.title, subject: v.subject, level: v.level, unit: v.unitName,
      views: v.viewCount || 0, status: v.isActive ? 'active' : 'inactive',
      videoUrl: v.videoUrl, cdnUrl: v.cdnUrl, thumbnailUrl: v.thumbnailUrl,
      durationSec: v.durationSec, fileSizeMb: v.fileSizeMb, createdAt: v.createdAt
    }
  } catch { error('상세 정보를 불러오지 못했습니다.') } finally { detail.loading = false }
}

// ── 수정 ───────────────────────────────────────────────────────────────────────
function startEdit() {
  Object.assign(editForm, makeForm({
    title: detail.data.title,
    subject: detail.data.subject || '',
    level: detail.data.level || '',
    unit: detail.data.unit || '',
    thumbnailUrl: detail.data.thumbnailUrl || '',
    isActive: detail.data.status === 'active',
    videoMode: 'url',
    videoUrl: detail.data.videoUrl || ''
  }))
  detail.editing = true
}

async function saveEdit() {
  editForm._touched = true
  if (!editForm.title.trim()) { error('제목을 입력하세요.'); return }
  if (!editForm.subject) { error('과목을 선택하세요.'); return }
  if (!editForm.level) { error('레벨을 선택하세요.'); return }
  if (editForm.uploading) { error('업로드가 진행 중입니다.'); return }
  const videoUrl = editForm.videoMode === 'file' ? editForm.uploadedUrl : editForm.videoUrl
  if (!videoUrl) { error('동영상 URL을 입력하거나 파일을 업로드하세요.'); return }

  detail.saving = true
  try {
    await api.put(`/admin/videos/${detail.data.id}`, {
      title: editForm.title, subject: editForm.subject, level: editForm.level,
      unitName: editForm.unit, videoUrl, thumbnailUrl: editForm.thumbnailUrl, isActive: editForm.isActive
    })
    const nextStatus = editForm.isActive ? 'active' : 'inactive'
    Object.assign(detail.data, {
      title: editForm.title, subject: editForm.subject, level: editForm.level,
      unit: editForm.unit, videoUrl, thumbnailUrl: editForm.thumbnailUrl, status: nextStatus
    })
    const row = videos.value.find(v => v.id === detail.data.id)
    if (row) Object.assign(row, { title: editForm.title, subject: editForm.subject, level: editForm.level, unit: editForm.unit, status: nextStatus })
    detail.editing = false
    detail.open = false
    success('동영상 정보를 수정했습니다.')
  } catch { error('수정에 실패했습니다.') } finally { detail.saving = false }
}

// ── 삭제 ───────────────────────────────────────────────────────────────────────
async function deleteVideoFromDetail() {
  const ok = await dialog.confirm('동영상을 삭제하시겠습니까?', { type: 'danger', confirmText: '삭제' })
  if (ok) {
    try {
      await api.delete(`/admin/videos/${detail.data.id}`)
      videos.value = videos.value.filter(v => v.id !== detail.data.id)
      detail.open = false
      success('동영상을 삭제했습니다.')
    } catch { error('삭제에 실패했습니다.') }
  }
}

function formatDuration(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}분 ${s}초`
}
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.mini-badge {
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;
  &.active   { background: #D1FAE5; color: #065F46; }
  &.inactive { background: #F3F4F6; color: #6B7280; }
}

.video-title-link {
  cursor: pointer;
  color: $text-primary;
  font-weight: 500;
  &:hover { color: $primary; text-decoration: underline; }
}

// ── 모달 ─────────────────────────────────────────────────────────────────────
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: $radius-lg;
  width: 560px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: $shadow-lg;
  display: flex;
  flex-direction: column;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-5 $spacing-6;
    border-bottom: 1px solid $border;
    flex-shrink: 0;
    h3 { font-size: $font-size-lg; font-weight: 700; color: $text-primary; }
  }

  &__body {
    padding: $spacing-6;
    flex: 1;
    &--center { display: flex; align-items: center; justify-content: center; min-height: 120px; }
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-2;
    padding: $spacing-4 $spacing-6;
    border-top: 1px solid $border;
    flex-shrink: 0;
  }
}

.modal-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: $text-muted;
  &:hover { color: $text-primary; }
}

// ── 상세 ─────────────────────────────────────────────────────────────────────
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.detail-label {
  font-size: $font-size-xs;
  color: $text-muted;
  width: 80px;
  flex-shrink: 0;
}

.detail-value {
  font-size: $font-size-sm;
  color: $text-secondary;
  &--bold { font-weight: 600; color: $text-primary; }
}

.detail-url {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
  margin-bottom: $spacing-4;
  .detail-label { width: auto; }
}

.url-link {
  font-size: $font-size-sm;
  color: $primary;
  word-break: break-all;
  &:hover { text-decoration: underline; }
}

.detail-thumb {
  margin-top: $spacing-4;
  img { max-width: 100%; border-radius: $radius-md; border: 1px solid $border; }
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-2;
  margin-top: $spacing-6;
  padding-top: $spacing-5;
  border-top: 1px solid $border;
}
</style>
