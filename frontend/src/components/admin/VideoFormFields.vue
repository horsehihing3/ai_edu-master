<template>
  <div class="edit-form">
    <div class="form-field">
      <label class="form-label">제목 <span class="required">*</span></label>
      <input v-model="form.title" class="form-control" placeholder="동영상 제목" />
    </div>
    <div class="form-row">
      <div class="form-field">
        <label class="form-label">과목 <span class="required">*</span></label>
        <select v-model="form.subject" class="form-control" :class="{ 'is-invalid': form._touched && !form.subject }">
          <option value="">선택</option>
          <option v-for="o in subjectOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
      </div>
      <div class="form-field">
        <label class="form-label">레벨 <span class="required">*</span></label>
        <select v-model="form.level" class="form-control" :class="{ 'is-invalid': form._touched && !form.level }">
          <option value="">선택</option>
          <option v-for="o in levelOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
      </div>
    </div>
    <div class="form-field">
      <label class="form-label">단원</label>
      <input v-model="form.unit" class="form-control" placeholder="단원명 (선택)" />
    </div>

    <!-- 동영상 -->
    <div class="form-field">
      <label class="form-label">동영상 <span class="required">*</span></label>
      <div class="upload-tabs">
        <button type="button" :class="['upload-tab', { active: form.videoMode === 'url' }]" @click="form.videoMode = 'url'">URL 입력</button>
        <button type="button" :class="['upload-tab', { active: form.videoMode === 'file' }]" @click="form.videoMode = 'file'">파일 업로드</button>
      </div>
      <template v-if="form.videoMode === 'url'">
        <input v-model="form.videoUrl" class="form-control" placeholder="https://" type="url" @blur="onUrlBlur" />
      </template>
      <template v-else>
        <div class="file-drop" @click="fileInput.click()" @dragover.prevent @drop.prevent="onDrop">
          <template v-if="!form.uploadFile">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="1.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            <p>클릭하거나 파일을 드래그하세요</p>
            <span>MP4, MOV, AVI · 최대 2GB</span>
          </template>
          <template v-else>
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <p class="file-name">{{ form.uploadFile.name }}</p>
            <span>{{ (form.uploadFile.size / 1024 / 1024).toFixed(1) }} MB</span>
          </template>
        </div>
        <input ref="fileInput" type="file" accept="video/*" style="display:none" @change="onFileChange" />
        <div v-if="form.uploading" class="upload-progress">
          <div class="progress-bar"><div class="progress-fill" :style="{ width: form.uploadProgress + '%' }" /></div>
          <span>{{ form.uploadProgress }}%</span>
        </div>
        <p v-if="form.uploadedUrl" class="upload-done">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          업로드 완료
        </p>
      </template>
    </div>

    <!-- 썸네일 -->
    <div class="form-field">
      <label class="form-label">
        썸네일
        <span v-if="thumbnailGenerating" class="thumb-status generating">생성 중...</span>
        <span v-else-if="form.thumbnailUrl" class="thumb-status done">자동 생성됨</span>
      </label>
      <div class="thumb-row">
        <div v-if="form.thumbnailUrl" class="thumb-preview">
          <img :src="form.thumbnailUrl" alt="썸네일 미리보기" @error="form.thumbnailUrl = ''" />
          <button type="button" class="thumb-remove" @click="form.thumbnailUrl = ''">✕</button>
        </div>
        <input v-model="form.thumbnailUrl" class="form-control" placeholder="자동 생성 또는 URL 직접 입력" />
      </div>
    </div>

    <!-- 활성화 여부 -->
    <div class="form-field">
      <label class="form-label">활성화 여부</label>
      <label class="toggle-label">
        <input type="checkbox" v-model="form.isActive" class="toggle-input" />
        <span class="toggle-track"><span class="toggle-thumb" /></span>
        <span class="toggle-text">{{ form.isActive ? '활성' : '비활성' }}</span>
      </label>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/utils/api'

const props = defineProps(['form', 'levelOptions', 'subjectOptions'])
const emit = defineEmits(['upload'])

const fileInput = ref(null)
const thumbnailGenerating = ref(false)

// ── YouTube URL 썸네일 추출 ───────────────────────────────────────────────────
function getYoutubeThumbnail(url) {
  const match = url?.match(/(?:v=|youtu\.be\/)([^&?/\s]+)/)
  return match ? `https://img.youtube.com/vi/${match[1]}/maxresdefault.jpg` : null
}

function onUrlBlur() {
  if (props.form.thumbnailUrl) return
  const thumb = getYoutubeThumbnail(props.form.videoUrl)
  if (thumb) props.form.thumbnailUrl = thumb
}

// ── 파일에서 썸네일 프레임 추출 (Canvas) ─────────────────────────────────────
function extractFrameBlob(file) {
  return new Promise((resolve) => {
    const video = document.createElement('video')
    const objectUrl = URL.createObjectURL(file)
    video.src = objectUrl
    video.muted = true
    video.playsInline = true

    video.addEventListener('loadeddata', () => {
      video.currentTime = Math.min(2, video.duration * 0.1 || 2)
    }, { once: true })

    video.addEventListener('seeked', () => {
      const canvas = document.createElement('canvas')
      const w = Math.min(video.videoWidth || 640, 640)
      const h = video.videoHeight ? Math.round(w * video.videoHeight / video.videoWidth) : 360
      canvas.width = w
      canvas.height = h
      canvas.getContext('2d').drawImage(video, 0, 0, w, h)
      canvas.toBlob((blob) => {
        URL.revokeObjectURL(objectUrl)
        resolve(blob)
      }, 'image/jpeg', 0.85)
    }, { once: true })

    video.addEventListener('error', () => {
      URL.revokeObjectURL(objectUrl)
      resolve(null)
    }, { once: true })

    video.load()
  })
}

async function autoGenerateThumbnail(file) {
  if (props.form.thumbnailUrl) return
  thumbnailGenerating.value = true
  try {
    const blob = await extractFrameBlob(file)
    if (!blob) return
    const fd = new FormData()
    fd.append('file', blob, 'thumbnail.jpg')
    const res = await api.post('/admin/videos/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    props.form.thumbnailUrl = res.data?.fileUrl || res.data || ''
  } catch {
    // 썸네일 생성 실패는 무시 (영상 등록은 계속)
  } finally {
    thumbnailGenerating.value = false
  }
}

// ── 파일 선택 ────────────────────────────────────────────────────────────────
function onDrop(e) {
  const f = e.dataTransfer.files[0]
  if (!f) return
  emit('upload', f)
  autoGenerateThumbnail(f)
}

function onFileChange(e) {
  const f = e.target.files[0]
  if (!f) return
  emit('upload', f)
  autoGenerateThumbnail(f)
}
</script>

<style scoped lang="scss">
.edit-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-3;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.form-label {
  font-size: $font-size-xs;
  font-weight: 600;
  color: $text-muted;
  display: flex;
  align-items: center;
  gap: $spacing-2;
  .required { color: $danger; }
}

.is-invalid {
  border-color: $danger !important;
  &:focus { box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15); }
}

.thumb-status {
  font-size: $font-size-xs;
  font-weight: 400;
  &.generating { color: $text-muted; }
  &.done { color: #10B981; }
}

.thumb-row {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.thumb-preview {
  position: relative;
  width: 160px;
  border-radius: $radius-md;
  overflow: hidden;
  border: 1px solid $border;

  img {
    display: block;
    width: 100%;
    aspect-ratio: 16 / 9;
    object-fit: cover;
  }

  .thumb-remove {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 20px;
    height: 20px;
    background: rgba(0,0,0,0.55);
    color: white;
    border: none;
    border-radius: 50%;
    font-size: 10px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    &:hover { background: rgba(0,0,0,0.8); }
  }
}

.upload-tabs {
  display: flex;
  margin-bottom: $spacing-2;
  border: 1px solid $border;
  border-radius: $radius-md;
  overflow: hidden;
  width: fit-content;
}

.upload-tab {
  padding: $spacing-1 $spacing-4;
  font-size: $font-size-sm;
  border: none;
  background: white;
  cursor: pointer;
  color: $text-muted;
  transition: background $transition-fast, color $transition-fast;

  &:first-child { border-right: 1px solid $border; }
  &.active { background: $primary; color: white; }
  &:not(.active):hover { background: $bg-light; }
}

.file-drop {
  border: 2px dashed $border;
  border-radius: $radius-md;
  padding: $spacing-6 $spacing-4;
  text-align: center;
  cursor: pointer;
  transition: border-color $transition-fast, background $transition-fast;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-2;

  &:hover { border-color: $primary; background: #f0f4ff; }

  p { font-size: $font-size-sm; color: $text-secondary; margin: 0; }
  .file-name { color: $primary; font-weight: 500; }
  span { font-size: $font-size-xs; color: $text-muted; }
}

.upload-progress {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-top: $spacing-2;

  span { font-size: $font-size-xs; color: $text-muted; min-width: 36px; text-align: right; }
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: $border;
  border-radius: $radius-full;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $primary;
  border-radius: $radius-full;
  transition: width 0.2s;
}

.upload-done {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  font-size: $font-size-xs;
  color: #10B981;
  margin-top: $spacing-1;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  cursor: pointer;
  user-select: none;
}

.toggle-input { display: none; }

.toggle-track {
  position: relative;
  width: 40px;
  height: 22px;
  background: $border;
  border-radius: $radius-full;
  transition: background $transition-fast;

  .toggle-input:checked + & { background: $primary; }
}

.toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  transition: transform $transition-fast;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.toggle-input:checked + .toggle-track .toggle-thumb {
  transform: translateX(18px);
}

.toggle-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}
</style>
