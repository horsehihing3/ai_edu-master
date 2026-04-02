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
      @click="triggerFileInput"
    >
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="1.5">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
        <polyline points="17 8 12 3 7 8"/>
        <line x1="12" y1="3" x2="12" y2="15"/>
      </svg>
      <p>파일을 드래그하거나 <span class="file-link">클릭하여 선택</span>하세요</p>
      <p class="hint">지원 형식: PDF (.pdf), Excel (.xlsx), CSV (.csv), JSON (.json)</p>
      <input
        ref="fileInputRef"
        type="file"
        accept=".pdf,.xlsx,.csv,.json"
        style="display:none"
        @change="handleFileChange"
      />
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
        <div class="progress-bar__fill" :style="{ width: progressPct + '%' }" />
      </div>
      <p v-if="parseError" class="parse-error-msg">{{ parseError }}</p>
    </div>

    <!-- 결과 검토 -->
    <div v-if="step === 'review'" class="review-section">
      <div class="review-header">
        <div class="review-stats">
          <div class="stat"><strong>{{ parsedProblems.length }}</strong><span>파싱 완료</span></div>
          <div class="stat"><strong class="text-success">{{ okCount }}</strong><span>정상</span></div>
          <div class="stat"><strong class="text-danger">{{ errCount }}</strong><span>오류</span></div>
        </div>
        <div style="display:flex;gap:12px;align-items:center;">
          <!-- PDF일 때만 비교뷰 토글 버튼 표시 -->
          <button
            v-if="isPdf"
            :class="['btn btn-md', splitView ? 'btn-primary' : 'btn-secondary']"
            @click="splitView = !splitView"
          >
            {{ splitView ? '📄 단일 뷰' : '⬛ PDF 비교 뷰' }}
          </button>
          <button class="btn btn-secondary btn-md" @click="resetUpload">다시 업로드</button>
          <AppButton :loading="saving" :disabled="okCount === 0" @click="confirmUpload">
            업로드 확정 ({{ okCount }}개)
          </AppButton>
        </div>
      </div>

      <!-- 2분할 비교 뷰 (PDF + 파싱 결과) -->
      <div v-if="isPdf && splitView" class="split-container" ref="splitContainerRef">
        <!-- 좌측: PDF 원본 -->
        <div class="split-pane split-left" :style="{ width: leftPct + '%' }">
          <div class="split-pane-header">
            <span class="split-pane-title">📄 원본 PDF</span>
            <span class="split-pane-filename">{{ selectedFile?.name }}</span>
          </div>
          <iframe
            v-if="pdfObjectUrl"
            :src="pdfObjectUrl"
            class="pdf-iframe"
            type="application/pdf"
          />
          <div v-else class="pdf-loading">PDF 로딩 중...</div>
        </div>

        <!-- 스플리터 -->
        <div
          class="splitter"
          @mousedown="startDrag"
          title="드래그하여 비율 조절"
        >
          <div class="splitter-handle">
            <span></span><span></span><span></span>
          </div>
        </div>

        <!-- 우측: 파싱 결과 테이블 -->
        <div class="split-pane split-right" :style="{ width: (100 - leftPct - splitterPct) + '%' }">
          <div class="split-pane-header">
            <span class="split-pane-title">✏️ 파싱 결과 (수정 가능)</span>
            <span class="split-pane-sub">클릭하여 직접 수정하세요</span>
          </div>
          <div class="review-table-wrap">
            <table class="review-table">
              <thead>
                <tr>
                  <th>No</th>
                  <th>과목</th>
                  <th>레벨</th>
                  <th>학년</th>
                  <th>단원</th>
                  <th>문제 내용</th>
                  <th>유형</th>
                  <th>정답</th>
                  <th>상태</th>
                  <th>오류</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(p, i) in parsedProblems" :key="i" :class="{ 'row-error': p.status === 'error' }">
                  <td>{{ i + 1 }}</td>
                  <td><input v-model="p.subject" class="cell-input" /></td>
                  <td>
                    <select v-model="p.level" class="cell-select" @change="validateRow(p)">
                      <option value="">-</option>
                      <option value="A">A</option>
                      <option value="B">B</option>
                      <option value="C">C</option>
                    </select>
                  </td>
                  <td><input v-model="p.grade" class="cell-input short" /></td>
                  <td><input v-model="p.unitName" class="cell-input" /></td>
                  <td><input v-model="p.questionText" class="cell-input wide" /></td>
                  <td>
                    <select v-model="p.problemType" class="cell-select" @change="validateRow(p)">
                      <option value="MULTIPLE_CHOICE">객관식</option>
                      <option value="SHORT_ANSWER">주관식</option>
                    </select>
                  </td>
                  <td><input v-model="p.answer" class="cell-input short" @change="validateRow(p)" /></td>
                  <td>
                    <span :class="['status-icon', p.status]">
                      {{ p.status === 'ok' ? '✓ 정상' : '✗ 오류' }}
                    </span>
                  </td>
                  <td><span v-if="p.errorMsg" class="error-msg">{{ p.errorMsg }}</span><span v-else>-</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 단일 뷰 (기존 테이블) -->
      <div v-else class="review-table-wrap">
        <table class="review-table">
          <thead>
            <tr>
              <th>No</th>
              <th>과목</th>
              <th>레벨</th>
              <th>학년</th>
              <th>단원</th>
              <th>문제 내용</th>
              <th>유형</th>
              <th>정답</th>
              <th>상태</th>
              <th>오류</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, i) in parsedProblems" :key="i" :class="{ 'row-error': p.status === 'error' }">
              <td>{{ i + 1 }}</td>
              <td><input v-model="p.subject" class="cell-input" /></td>
              <td>
                <select v-model="p.level" class="cell-select" @change="validateRow(p)">
                  <option value="">-</option>
                  <option value="A">A</option>
                  <option value="B">B</option>
                  <option value="C">C</option>
                </select>
              </td>
              <td><input v-model="p.grade" class="cell-input short" /></td>
              <td><input v-model="p.unitName" class="cell-input" /></td>
              <td><input v-model="p.questionText" class="cell-input wide" /></td>
              <td>
                <select v-model="p.problemType" class="cell-select" @change="validateRow(p)">
                  <option value="MULTIPLE_CHOICE">객관식</option>
                  <option value="SHORT_ANSWER">주관식</option>
                </select>
              </td>
              <td><input v-model="p.answer" class="cell-input short" @change="validateRow(p)" /></td>
              <td>
                <span :class="['status-icon', p.status]">
                  {{ p.status === 'ok' ? '✓ 정상' : '✗ 오류' }}
                </span>
              </td>
              <td><span v-if="p.errorMsg" class="error-msg">{{ p.errorMsg }}</span><span v-else>-</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 업로드 결과 -->
    <div v-if="step === 'done'" class="done-section card">
      <div class="done-icon">✓</div>
      <h3>업로드 완료</h3>
      <p>{{ uploadResult.success }}개 문제가 문제은행에 저장되었습니다.</p>
      <p v-if="uploadResult.failed > 0" class="text-danger">{{ uploadResult.failed }}개 실패</p>
      <div style="display:flex;gap:12px;margin-top:24px;">
        <button class="btn btn-secondary btn-md" @click="resetUpload">추가 업로드</button>
        <AppButton @click="router.push('/admin/problems')">문제은행 확인</AppButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '@/components/common/AppButton.vue'
import { useToast } from '@/composables/useToast'
import api from '@/utils/api'

const router = useRouter()
const { success, error } = useToast()

const step = ref('upload')
const dragover = ref(false)
const selectedFile = ref(null)
const currentStep = ref(0)
const saving = ref(false)
const parseError = ref('')
const uploadResult = ref({ success: 0, failed: 0 })
const fileInputRef = ref(null)

const parsingSteps = ['파일 읽기', '형식 검증', 'AI 문제 파싱', '레벨 자동 분류', '최종 검토']

const parsedProblems = ref([])

const okCount = computed(() => parsedProblems.value.filter(p => p.status === 'ok').length)
const errCount = computed(() => parsedProblems.value.filter(p => p.status === 'error').length)
const progressPct = computed(() => Math.round((currentStep.value / (parsingSteps.length - 1)) * 100))

// ──────────────────────────────────────────
// PDF 2분할 비교 뷰 관련
// ──────────────────────────────────────────
const isPdf = computed(() => selectedFile.value?.name?.toLowerCase().endsWith('.pdf'))
const splitView = ref(false)
const pdfObjectUrl = ref(null)
const splitContainerRef = ref(null)
const leftPct = ref(50)
const splitterPct = 0.6 // 스플리터 너비 (%)

let isDragging = false
let dragStartX = 0
let dragStartLeft = 0

function startDrag(e) {
  isDragging = true
  dragStartX = e.clientX
  dragStartLeft = leftPct.value

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

function onDrag(e) {
  if (!isDragging || !splitContainerRef.value) return
  const containerWidth = splitContainerRef.value.offsetWidth
  const deltaX = e.clientX - dragStartX
  const deltaPct = (deltaX / containerWidth) * 100
  leftPct.value = Math.min(75, Math.max(25, dragStartLeft + deltaPct))
}

function stopDrag() {
  isDragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

onUnmounted(() => {
  stopDrag()
  if (pdfObjectUrl.value) {
    URL.revokeObjectURL(pdfObjectUrl.value)
  }
})

// ──────────────────────────────────────────

function triggerFileInput() {
  fileInputRef.value?.click()
}

function handleDrop(e) {
  dragover.value = false
  const file = e.dataTransfer.files[0]
  if (file) startParsing(file)
}

function handleFileChange(e) {
  const file = e.target.files[0]
  if (file) startParsing(file)
}

function resetUpload() {
  step.value = 'upload'
  parsedProblems.value = []
  selectedFile.value = null
  currentStep.value = 0
  parseError.value = ''
  splitView.value = false
  if (pdfObjectUrl.value) {
    URL.revokeObjectURL(pdfObjectUrl.value)
    pdfObjectUrl.value = null
  }
  if (fileInputRef.value) fileInputRef.value.value = ''
}

function validateRow(p) {
  if (!p.level) {
    p.status = 'error'
    p.errorMsg = '레벨 정보 없음'
  } else if (!p.answer) {
    p.status = 'error'
    p.errorMsg = '정답 정보 없음'
  } else if (!p.questionText?.trim()) {
    p.status = 'error'
    p.errorMsg = '문제 내용 없음'
  } else {
    p.status = 'ok'
    p.errorMsg = null
  }
}

// ──────────────────────────────────────────
// 파일 파싱 진입점
// ──────────────────────────────────────────
async function startParsing(file) {
  selectedFile.value = file
  step.value = 'parsing'
  currentStep.value = 0
  parseError.value = ''

  // PDF인 경우 Object URL 미리 생성 (비교 뷰용)
  if (file.name.toLowerCase().endsWith('.pdf')) {
    if (pdfObjectUrl.value) URL.revokeObjectURL(pdfObjectUrl.value)
    pdfObjectUrl.value = URL.createObjectURL(file)
  }

  try {
    const ext = file.name.split('.').pop().toLowerCase()

    setStep(0) // 파일 읽기
    const raw = await readFile(file, ext)

    setStep(1) // 형식 검증
    await delay(300)

    setStep(2) // AI 문제 파싱
    let problems = []
    if (ext === 'pdf') {
      problems = await parseWithAI(raw, file.name)
    } else if (ext === 'xlsx') {
      problems = await parseXlsx(raw)
    } else if (ext === 'csv') {
      problems = parseCSV(raw)
    } else if (ext === 'json') {
      problems = parseJSON(raw)
    } else {
      throw new Error('지원하지 않는 파일 형식입니다.')
    }

    setStep(3) // 레벨 자동 분류
    await delay(300)
    problems.forEach(p => validateRow(p))

    setStep(4) // 최종 검토
    await delay(300)

    parsedProblems.value = problems
    step.value = 'review'

    if (ext === 'pdf') {
      splitView.value = true
    }

  } catch (e) {
    parseError.value = e.message || '파싱 중 오류가 발생했습니다.'
    error(parseError.value)
    setTimeout(() => { step.value = 'upload' }, 2000)
  }
}

function setStep(n) { currentStep.value = n }
function delay(ms) { return new Promise(r => setTimeout(r, ms)) }

function normalizeGrade(g) {
  if (!g) return ''
  const map = { '중1': 'GRADE_1', '중2': 'GRADE_2', '중3': 'GRADE_3', '1': 'GRADE_1', '2': 'GRADE_2', '3': 'GRADE_3' }
  return map[g.toString().trim()] || g
}

// ──────────────────────────────────────────
// 파일 읽기
// ──────────────────────────────────────────
function readFile(file, ext) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    if (ext === 'pdf' || ext === 'xlsx') {
      reader.readAsDataURL(file)
      reader.onload = e => resolve(e.target.result.split(',')[1]) // base64
    } else {
      reader.readAsText(file, 'UTF-8')
      reader.onload = e => resolve(e.target.result)
    }
    reader.onerror = () => reject(new Error('파일을 읽을 수 없습니다.'))
  })
}

// ──────────────────────────────────────────
// PDF → 백엔드 프록시 → Claude AI 파싱
// ──────────────────────────────────────────
async function parseWithAI(base64Data, fileName) {
  const res = await api.post('/admin/parse-pdf', { base64Data }, { timeout: 120000 }) // 2분
  const result = res.data?.data || res.data
  const parsed = result?.problems || (Array.isArray(result) ? result : null)

  if (!Array.isArray(parsed)) throw new Error('AI가 올바른 형식으로 응답하지 않았습니다.')

  return parsed.map((p, i) => ({
    row: i + 1,
    subject: p.subject || '수학',
    grade: normalizeGrade(p.grade),
    unitName: p.unitName || '',
    questionText: p.questionText || '',
    problemType: p.problemType || 'SHORT_ANSWER',
    level: p.level || '',
    answer: p.answer || '',
    explanation: p.explanation || '',
    options: (p.options || []).map(opt => typeof opt === 'string' ? { optionText: opt, optionImgUrl: '' } : opt),
    questionImgUrl: p.questionImgUrl || '',
    passage: p.passage || '',
    status: 'ok',
    errorMsg: null
  }))
}

// ──────────────────────────────────────────
// XLSX 파싱
// ──────────────────────────────────────────
async function parseXlsx(base64Data) {
  if (!window.XLSX) {
    await loadScript('https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js')
  }
  const binary = atob(base64Data)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
  const wb = window.XLSX.read(bytes, { type: 'array' })
  const ws = wb.Sheets[wb.SheetNames[0]]
  const rows = window.XLSX.utils.sheet_to_json(ws, { defval: '' })

  return rows.map((r, i) => ({
    row: i + 1,
    subject: r.subject || r['과목'] || '수학',
    grade: normalizeGrade(r.grade || r['학년']),
    unitName: r.unitName || r['단원'] || '',
    questionText: r.questionText || r['문제'] || '',
    problemType: r.problemType || r['유형'] || 'SHORT_ANSWER',
    level: r.level || r['레벨'] || '',
    answer: String(r.answer ?? r['정답'] ?? ''),
    explanation: r.explanation || r['해설'] || '',
    options: [],
    questionImgUrl: '',
    passage: '',
    status: 'ok',
    errorMsg: null
  }))
}

// ──────────────────────────────────────────
// CSV 파싱
// ──────────────────────────────────────────
function parseCSV(text) {
  const lines = text.trim().split('\n')
  const headers = lines[0].split(',').map(h => h.trim().replace(/^"|"$/g, ''))
  return lines.slice(1).map((line, i) => {
    const vals = line.split(',').map(v => v.trim().replace(/^"|"$/g, ''))
    const r = {}
    headers.forEach((h, j) => { r[h] = vals[j] || '' })
    return {
      row: i + 1,
      subject: r.subject || r['과목'] || '수학',
      grade: normalizeGrade(r.grade || r['학년']),
      unitName: r.unitName || r['단원'] || '',
      questionText: r.questionText || r['문제'] || '',
      problemType: r.problemType || r['유형'] || 'SHORT_ANSWER',
      level: r.level || r['레벨'] || '',
      answer: String(r.answer ?? r['정답'] ?? ''),
      explanation: r.explanation || r['해설'] || '',
      options: [],
      questionImgUrl: '',
      passage: '',
      status: 'ok',
      errorMsg: null
    }
  })
}

// ──────────────────────────────────────────
// JSON 파싱
// ──────────────────────────────────────────
function parseJSON(text) {
  const data = JSON.parse(text)
  const arr = Array.isArray(data) ? data : (data.problems || [])
  return arr.map((p, i) => ({
    row: i + 1,
    subject: p.subject || '수학',
    grade: normalizeGrade(p.grade),
    unitName: p.unitName || '',
    questionText: p.questionText || '',
    problemType: p.problemType || 'SHORT_ANSWER',
    level: p.level || '',
    answer: String(p.answer ?? ''),
    explanation: p.explanation || '',
    options: p.options || [],
    questionImgUrl: '',
    passage: '',
    status: 'ok',
    errorMsg: null
  }))
}

// ──────────────────────────────────────────
// 스크립트 동적 로드 유틸
// ──────────────────────────────────────────
function loadScript(src) {
  return new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = src
    s.onload = resolve
    s.onerror = () => reject(new Error(`스크립트 로드 실패: ${src}`))
    document.head.appendChild(s)
  })
}

// ──────────────────────────────────────────
// 실제 배치 업로드 API 호출
// ──────────────────────────────────────────
async function confirmUpload() {
  const validProblems = parsedProblems.value.filter(p => p.status === 'ok')
  if (validProblems.length === 0) {
    error('업로드 가능한 문제가 없습니다.')
    return
  }

  saving.value = true
  try {
    const payload = validProblems.map(p => ({
      subject: p.subject,
      grade: p.grade,
      unitName: p.unitName,
      questionText: p.questionText,
      problemType: p.problemType,
      level: p.level,
      answer: p.answer,
      explanation: p.explanation,
      questionImgUrl: p.questionImgUrl || '',
      passage: p.passage || '',
      options: p.options || []
    }))

    const res = await api.post('/problems/upload/batch', payload)
    const result = res.data?.data || {}

    uploadResult.value = {
      success: result.success ?? validProblems.length,
      failed: result.failed ?? 0
    }

    step.value = 'done'

    if (uploadResult.value.failed > 0) {
      error(`${uploadResult.value.failed}개 문제 저장 실패`)
    }

  } catch (e) {
    error(e.response?.data?.message || '업로드에 실패했습니다.')
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

.parse-error-msg {
  margin-top: 16px;
  color: $danger;
  font-size: $font-size-sm;
  text-align: center;
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

// ──────────────────────────────────────────
// 2분할 비교 뷰
// ──────────────────────────────────────────
.split-container {
  display: flex;
  height: calc(100vh - 220px);
  min-height: 500px;
  border: 1px solid $border;
  border-radius: $radius-md;
  overflow: hidden;
}

.split-pane {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.split-pane-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: $bg-light;
  border-bottom: 1px solid $border;
  flex-shrink: 0;
}

.split-pane-title {
  font-weight: 700;
  font-size: $font-size-sm;
  color: $text-primary;
}

.split-pane-filename,
.split-pane-sub {
  font-size: $font-size-xs;
  color: $text-muted;
}

.split-left {
  border-right: none;

  .pdf-iframe {
    flex: 1;
    width: 100%;
    height: 100%;
    border: none;
  }

  .pdf-loading {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $text-muted;
    font-size: $font-size-sm;
  }
}

.split-right {
  .review-table-wrap {
    flex: 1;
    overflow: auto;
    border-radius: 0;
    border: none;
  }
}

.splitter {
  width: 8px;
  background: $border;
  cursor: col-resize;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;

  &:hover {
    background: $primary-light;

    .splitter-handle span {
      background: white;
    }
  }

  .splitter-handle {
    display: flex;
    flex-direction: column;
    gap: 3px;

    span {
      display: block;
      width: 2px;
      height: 8px;
      background: $text-muted;
      border-radius: 1px;
      transition: background 0.15s;
    }
  }
}

// ──────────────────────────────────────────

.review-table-wrap {
  overflow-x: auto;
  border-radius: $radius-md;
  border: 1px solid $border;
}

.review-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;

  th {
    background: $bg-light;
    padding: 10px 12px;
    text-align: left;
    font-weight: 600;
    color: $text-secondary;
    white-space: nowrap;
    border-bottom: 1px solid $border;
  }

  td {
    padding: 8px 12px;
    border-bottom: 1px solid $border;
    vertical-align: middle;
  }

  tr.row-error td { background: #FFF5F5; }
  tr:last-child td { border-bottom: none; }
}

.cell-input {
  border: 1px solid $border;
  border-radius: $radius-sm;
  padding: 4px 8px;
  font-size: $font-size-sm;
  width: 100%;
  min-width: 80px;
  background: white;

  &:focus {
    outline: none;
    border-color: $primary-light;
  }

  &.wide { min-width: 200px; }
  &.short { min-width: 60px; max-width: 80px; }
}

.cell-select {
  border: 1px solid $border;
  border-radius: $radius-sm;
  padding: 4px 6px;
  font-size: $font-size-sm;
  background: white;

  &:focus {
    outline: none;
    border-color: $primary-light;
  }
}

.status-icon {
  font-size: $font-size-sm;
  font-weight: 600;

  &.ok { color: $success; }
  &.error { color: $danger; }
}

.error-msg {
  color: $danger;
  font-size: $font-size-xs;
}

.done-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-16;
  text-align: center;

  h3 { font-size: $font-size-xl; font-weight: 700; margin: 16px 0 8px; }
  p { color: $text-secondary; }
}

.done-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: $success;
  color: white;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.text-success { color: $success !important; }
.text-danger { color: $danger !important; }

</style>
