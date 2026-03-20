<template>
  <div class="payment-page">
    <div class="page-header"><h1>결제/구독 관리</h1></div>

    <div class="kpi-row">
      <div class="kpi-card" v-for="k in kpis" :key="k.label" :style="{ background: k.bg, borderTop: `4px solid ${k.color}` }">
        <div class="kpi-card__top">
          <p class="kpi-card__label">{{ k.label }}</p>
          <div class="kpi-icon" :style="{ background: k.bg, color: k.color }" v-html="k.icon" />
        </div>
        <p class="kpi-card__value">{{ k.value }}</p>
      </div>
    </div>

    <div class="filter-bar">
      <input v-model="search" class="form-control" placeholder="이름, 이메일 검색..." style="width:220px;" />
      <AppSelect v-model="filterStatus" :options="statusOptions" placeholder="전체 상태" style="width:160px; margin:0;" />
    </div>

    <AppTable :columns="columns" :data="filteredPayments" :loading="loading">
      <template #cell-status="{ value }">
        <span :class="['payment-badge', value]">{{ statusLabel(value) }}</span>
      </template>
      <template #cell-amount="{ value }">
        <strong>{{ value.toLocaleString() }}원</strong>
      </template>
      <template #cell-actions="{ row }">
        <button
          v-if="row.status === 'COMPLETED'"
          class="btn btn-secondary btn-sm"
          @click="refund(row)"
        >환불 처리</button>
        <span v-else style="color:#9CA3AF; font-size:12px;">-</span>
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
const filterStatus = ref('')
const loading = ref(false)
const page = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)

const kpiColors = ['#3B82F6', '#10B981', '#EF4444', '#8B5CF6']
const kpiBgs   = ['#DBEAFE', '#D1FAE5', '#FEE2E2', '#EDE9FE']
const kpiIcons = [
  `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
  `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
  `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 .49-3.5"/></svg>`,
  `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`
]

const kpis = ref([
  { label: '이번달 매출', value: '-', color: kpiColors[0], bg: kpiBgs[0], icon: kpiIcons[0] },
  { label: '활성 구독자', value: '-', color: kpiColors[1], bg: kpiBgs[1], icon: kpiIcons[1] },
  { label: '환불 건수',   value: '-', color: kpiColors[2], bg: kpiBgs[2], icon: kpiIcons[2] },
  { label: '평균 결제 단가', value: '-', color: kpiColors[3], bg: kpiBgs[3], icon: kpiIcons[3] }
])

const statusOptions = [
  { value: 'COMPLETED', label: '결제 완료' },
  { value: 'REFUNDED', label: '환불' },
  { value: 'PENDING', label: '미결제' },
  { value: 'FAILED', label: '실패' }
]

const columns = [
  { key: 'id', label: 'No' },
  { key: 'userName', label: '구매자' },
  { key: 'plan', label: '플랜' },
  { key: 'amount', label: '금액', sortable: true },
  { key: 'paidAt', label: '결제일', sortable: true },
  { key: 'status', label: '상태' },
  { key: 'actions', label: '' }
]

const payments = ref([])
const filteredPayments = computed(() => payments.value)

async function fetchPayments() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 15 }
    if (search.value) params.keyword = search.value
    if (filterStatus.value) params.status = filterStatus.value
    const [statsRes, listRes] = await Promise.all([
      api.get('/admin/payments/stats'),
      api.get('/admin/payments', { params })
    ])
    const s = statsRes.data || {}
    kpis.value = [
      { label: '이번달 매출',    value: `${(s.monthlyRevenue || 0).toLocaleString()}원`, color: kpiColors[0], bg: kpiBgs[0], icon: kpiIcons[0] },
      { label: '활성 구독자',    value: `${(s.activeSubscribers || 0).toLocaleString()}명`, color: kpiColors[1], bg: kpiBgs[1], icon: kpiIcons[1] },
      { label: '환불 건수',      value: `${s.refundCount || 0}건`, color: kpiColors[2], bg: kpiBgs[2], icon: kpiIcons[2] },
      { label: '평균 결제 단가', value: `${(s.avgAmount || 0).toLocaleString()}원`, color: kpiColors[3], bg: kpiBgs[3], icon: kpiIcons[3] }
    ]
    const pageData = listRes.data || {}
    payments.value = (pageData.content || []).map(p => ({
      id: p.paymentId, userName: p.userName, plan: p.planName,
      amount: p.amount, paidAt: p.paidAt?.slice(0, 10), status: p.status
    }))
    totalPages.value = pageData.totalPages || 1
    totalElements.value = pageData.totalElements || payments.value.length
  } catch {} finally { loading.value = false }
}

watch([search, filterStatus], () => { page.value = 1; fetchPayments() })
watch(page, fetchPayments)
onMounted(fetchPayments)

function statusLabel(v) {
  return { COMPLETED: '결제완료', REFUNDED: '환불', PENDING: '미결제', FAILED: '실패' }[v] || v
}

async function refund(payment) {
  const ok = await dialog.confirm(`${payment.userName}의 결제 ${payment.amount.toLocaleString()}원을 환불하시겠습니까?`, { type: 'danger', confirmText: '환불' })
  if (ok) {
    payment.status = 'refunded'
    success('환불 처리가 완료되었습니다.')
  }
}
</script>

<style scoped lang="scss">
.page-header { display: flex; align-items: flex-start; justify-content: space-between; }

.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-5;
  margin-bottom: $spacing-6;

  @media (max-width: $bp-tablet) { grid-template-columns: repeat(2, 1fr); }
}

.kpi-card {
  &__top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-3;
  }
  &__label { font-size: $font-size-xs; color: $text-muted; }
  &__value { font-size: $font-size-2xl; font-weight: 700; color: $text-primary; }
}

.kpi-icon {
  width: 36px;
  height: 36px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.filter-bar {
  display: flex;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
}

.payment-badge {
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;

  &.COMPLETED { background: #D1FAE5; color: #065F46; }
  &.REFUNDED  { background: #FEE2E2; color: #991B1B; }
  &.PENDING   { background: #FEF3C7; color: #92400E; }
  &.FAILED    { background: #F3F4F6; color: #6B7280; }
}
</style>
