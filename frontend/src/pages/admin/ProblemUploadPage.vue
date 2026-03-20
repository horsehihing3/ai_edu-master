<template>
  <div class="problem-upload-page">
    <div class="page-header"><h1>문제 파일 업로드</h1></div>

    <!-- 드래그앤드롭 업로드 -->
    <div
      v-if="step === 'upload'"
      :class="['upload-zone', { dragover }]"
      @dragover.prevent="dragover = true"
      @dragleave="dragover = false"
      @drop.prevent="handleDrop"
    >
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="1.5">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
        <polyline points="17 8 12 3 7 8"/>
        <line x1="12" y1="3" x2="12" y2="15"/>
      </svg>
      <p>파일을 드래그하거나 <label for="fileInput" class="file-link">클릭하여 선택</label>하세요</p>
      <p class="hint">지원 형식: Excel (.xlsx), CSV (.csv), JSON (.json)</p>
      <input id="fileInput" type="file" accept=".xlsx,.csv,.json" style="display:none" @change="handleFileChange" />
    </div>

    <!-- AI 파싱 진행 상태 -->
    <div v-if="step === 'parsing'" class="parsing-section card">
      <div class="parsing-header">
        <div class="ai-badge-lg">AI</div>
        <div>
          <h3>AI가 파일을 분석하고 있습니다</h3>
          <p>{{ selectedFile?.name }}</p>
        </div>
      </div>
      <div class="parsing-steps">
        <div v-for="(s, i) in parsingSteps" :key="i" :class="['parse-step', { done: i < currentStep, active: i === currentStep }]">
          <div class="step-icon">
            <svg v-if="i < currentStep" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            <svg v-else-if="i === currentStep" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <span>{{ s }}</span>
        </div>
      </div>
      <div class="progress-bar" style="height: 12px; margin-top: 20px;">
        <div class="progress-bar__fill" :style="{ width: (currentStep / (parsingSteps.length - 1) * 100) + '%' }" />
      </div>
    </div>

    <!-- 결과 검토 -->
    <div v-if="step === 'review'" class="review-section">
      <div class="review-header">
        <div class="review-stats">
          <div class="stat"><strong>{{ parsedProblems.length }}</strong><span>파싱 완료</span></div>
          <div class="stat"><strong class="text-success">{{ parsedProblems.filter(p=>p.status==='ok').length }}</strong><span>정상</span></div>
          <div class="stat"><strong class="text-danger">{{ parsedProblems.filter(p=>p.status==='error').length }}</strong><span>오류</span></div>
        </div>
        <div style="display:flex;gap:12px;">
          <button class="btn btn-secondary btn-md" @click="step = 'upload'">다시 업로드</button>
          <AppButton :loading="saving" @click="confirmUpload">업로드 확정</AppButton>
        </div>
      </div>

      <AppTable :columns="reviewColumns" :data="parsedProblems">
        <template #cell-level="{ value }">
          <AppBadge :type="value" />
        </template>
        <template #cell-status="{ value }">
          <span :class="['status-icon', value]">
            {{ value === 'ok' ? '✓ 정상' : '✗ 오류' }}
          </span>
        </template>
        <template #cell-errorMsg="{ value }">
          <span v-if="value" style="color:#EF4444; font-size:12px;">{{ value }}</span>
          <span v-else>-</span>
        </template>
      </AppTable>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppTable from '@/components/common/AppTable.vue'
import AppBadge from '@/components/common/AppBadge.vue'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { success, error } = useToast()
const step = ref('upload')
const dragover = ref(false)
const selectedFile = ref(null)
const currentStep = ref(0)
const saving = ref(false)

const parsingSteps = ['파일 읽기', '형식 검증', 'AI 문제 파싱', '레벨 자동 분류', '최종 검토']

const reviewColumns = [
  { key: 'row', label: 'Row' },
  { key: 'subject', label: '과목' },
  { key: 'level', label: '레벨' },
  { key: 'unit', label: '단원' },
  { key: 'questionText', label: '문제 내용' },
  { key: 'status', label: '상태' },
  { key: 'errorMsg', label: '오류' }
]

const parsedProblems = ref([
  { row: 1, subject: '수학', level: 'B', unit: '미적분', questionText: 'f(x) = x²을 미분하시오.', status: 'ok', errorMsg: null },
  { row: 2, subject: '영어', level: 'B', unit: '독해', questionText: 'Choose the correct word.', status: 'ok', errorMsg: null },
  { row: 3, subject: '국어', level: '', unit: '', questionText: '다음 글을 읽고...', status: 'error', errorMsg: '레벨 정보 없음' }
])

function handleDrop(e) {
  dragover.value = false
  const file = e.dataTransfer.files[0]
  if (file) startParsing(file)
}

function handleFileChange(e) {
  const file = e.target.files[0]
  if (file) startParsing(file)
}

function startParsing(file) {
  selectedFile.value = file
  step.value = 'parsing'
  currentStep.value = 0

  const interval = setInterval(() => {
    currentStep.value++
    if (currentStep.value >= parsingSteps.length - 1) {
      clearInterval(interval)
      setTimeout(() => { step.value = 'review' }, 500)
    }
  }, 800)
}

async function confirmUpload() {
  const validProblems = parsedProblems.value.filter(p => p.status === 'ok')
  saving.value = true
  try {
    await new Promise(r => setTimeout(r, 1000))
    success(`${validProblems.length}개 문제를 업로드했습니다.`)
    router.push('/admin/problems')
  } catch {
    error('업로드에 실패했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.upload-zone {
  border: 2px dashed $border;
  border-radius: $radius-lg;
  padding: $spacing-16 $spacing-8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-4;
  text-align: center;
  cursor: pointer;
  transition: all $transition-base;

  &:hover, &.dragover {
    border-color: $primary-light;
    background: $primary-bg;
  }

  p {
    font-size: $font-size-base;
    color: $text-secondary;
  }

  .hint {
    font-size: $font-size-sm;
    color: $text-muted;
  }
}

.file-link {
  color: $primary-light;
  cursor: pointer;
  font-weight: 600;

  &:hover { text-decoration: underline; }
}

.parsing-header {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  margin-bottom: $spacing-8;

  h3 { font-size: $font-size-lg; font-weight: 700; }
  p { font-size: $font-size-sm; color: $text-secondary; }
}

.ai-badge-lg {
  background: $primary;
  color: white;
  padding: 8px 14px;
  border-radius: $radius-md;
  font-size: $font-size-lg;
  font-weight: 900;
}

.parsing-steps {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.parse-step {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  font-size: $font-size-sm;
  color: $text-muted;

  &.done { color: $success; }
  &.active { color: $primary-light; font-weight: 600; }
}

.step-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid currentColor;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xs;
  font-weight: 700;
  flex-shrink: 0;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-5;
  flex-wrap: wrap;
  gap: $spacing-4;
}

.review-stats {
  display: flex;
  gap: $spacing-6;

  .stat {
    text-align: center;

    strong {
      display: block;
      font-size: $font-size-2xl;
      font-weight: 700;
      color: $text-primary;
    }

    span {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
}

.status-icon {
  font-size: $font-size-sm;
  font-weight: 600;

  &.ok { color: $success; }
  &.error { color: $danger; }
}

.text-success { color: $success !important; }
.text-danger { color: $danger !important; }
</style>
