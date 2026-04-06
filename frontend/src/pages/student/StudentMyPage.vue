<template>
  <div class="my-page">
    <div class="page-header">
      <h1>마이페이지</h1>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner" />
    </div>

    <template v-else>
      <!-- 구독/이용권 현황 -->
      <section class="card">
        <h2 class="section-title">이용권 현황</h2>

        <!-- 활성 구독 있음 -->
        <div v-if="subscription" class="subscription-box subscription-box--active">
          <div class="subscription-header">
            <div class="plan-badge" :class="'plan-' + subscription.planType?.toLowerCase()">
              {{ subscription.planName }}
            </div>
            <span class="status-badge status-active">이용 중</span>
          </div>
          <div class="subscription-details">
            <div class="detail-row">
              <span class="detail-label">시작일</span>
              <span class="detail-value">{{ subscription.startDate }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">만료일</span>
              <span class="detail-value">
                {{ subscription.endDate }}
                <span v-if="subscription.daysLeft <= 7" class="expire-soon">
                  ({{ subscription.daysLeft }}일 후 만료)
                </span>
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">자동갱신</span>
              <span class="detail-value">{{ subscription.autoRenew ? '켜짐' : '꺼짐' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">이용 요금</span>
              <span class="detail-value">{{ subscription.price?.toLocaleString() }}원 / 월</span>
            </div>
          </div>
          <div v-if="subscription.daysLeft <= 30" class="renew-notice">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            만료까지 {{ subscription.daysLeft }}일 남았습니다.
          </div>
        </div>

        <!-- 구독 없음 -->
        <div v-else class="subscription-box subscription-box--empty">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
            <line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          <p class="empty-title">현재 이용 중인 이용권이 없습니다.</p>
          <p class="empty-desc">이용권을 구매하면 모든 기능을 제한 없이 사용할 수 있습니다.</p>
          <a href="/#pricing" class="btn btn-primary btn-md">요금 안내 보기</a>
        </div>
      </section>

      <!-- 최근 결제 이력 -->
      <section class="card">
        <h2 class="section-title">결제 이력</h2>

        <div v-if="!recentPayments.length" class="empty-payments">
          결제 이력이 없습니다.
        </div>
        <table v-else class="payment-table">
          <thead>
            <tr>
              <th>이용권</th>
              <th>금액</th>
              <th>상태</th>
              <th>결제일</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in recentPayments" :key="p.paymentId">
              <td>{{ p.planName || '—' }}</td>
              <td class="fw-600">{{ p.amount?.toLocaleString() }}원</td>
              <td>
                <span :class="['pay-status', 'pay-status--' + p.status?.toLowerCase()]">
                  {{ payStatusLabel(p.status) }}
                </span>
              </td>
              <td class="text-muted">{{ (p.paidAt || p.createdAt)?.slice(0, 10) }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>
  </div>
</template>

<script setup>
// [2026-04-06] 학생 마이페이지 — 구독/이용권 현황
import { ref, onMounted } from 'vue'
import api from '@/utils/api'
import { useToast } from '@/composables/useToast'

const { error } = useToast()
const loading = ref(true)
const subscription = ref(null)
const recentPayments = ref([])

function payStatusLabel(s) {
  return { COMPLETED: '결제 완료', PENDING: '대기 중', REFUNDED: '환불', FAILED: '실패' }[s] || s
}

onMounted(async () => {
  try {
    const res = await api.get('/student/mypage')
    const data = res.data?.data || res.data || {}
    subscription.value = data.subscription || null
    recentPayments.value = data.recentPayments || []
  } catch {
    error('정보를 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: $spacing-6;
  h1 { font-size: $font-size-xl; font-weight: 700; }
}

.loading-state {
  display: flex; justify-content: center; padding: $spacing-16;
}
.spinner {
  width: 32px; height: 32px;
  border: 3px solid $border;
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.card {
  background: $bg-white;
  border: 1px solid $border;
  border-radius: $radius-xl;
  padding: $spacing-6;
  margin-bottom: $spacing-5;
}

.section-title {
  font-size: $font-size-base;
  font-weight: 700;
  margin-bottom: $spacing-5;
  padding-bottom: $spacing-3;
  border-bottom: 1px solid $border;
}

// ── 구독 박스 ──────────────────────────────────
.subscription-box {
  border-radius: $radius-lg;
  padding: $spacing-5;

  &--active {
    background: $bg-light;
    border: 1px solid $border;
  }

  &--empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-3;
    padding: $spacing-10;
    text-align: center;
    background: $bg-light;
    border: 2px dashed $border;
    border-radius: $radius-lg;
  }
}

.subscription-header {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $spacing-4;
}

.plan-badge {
  padding: 4px 12px;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  font-weight: 700;

  &.plan-basic    { background: #E5E7EB; color: #374151; }
  &.plan-standard { background: #DBEAFE; color: #1E40AF; }
  &.plan-premium  { background: #FDE68A; color: #92400E; }
}

.status-badge {
  font-size: $font-size-xs;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: $radius-full;

  &.status-active { background: #D1FAE5; color: #065F46; }
}

.subscription-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-3 $spacing-6;

  @media (max-width: $bp-mobile) {
    grid-template-columns: 1fr;
  }
}

.detail-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-label {
  font-size: $font-size-xs;
  color: $text-muted;
  font-weight: 500;
}

.detail-value {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
}

.expire-soon {
  color: $danger;
  font-size: $font-size-xs;
  margin-left: $spacing-1;
}

.renew-notice {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  margin-top: $spacing-4;
  padding: $spacing-2 $spacing-3;
  background: #FEF9C3;
  border: 1px solid #FDE68A;
  border-radius: $radius-md;
  font-size: $font-size-xs;
  color: #92400E;
}

.empty-icon { color: $text-muted; }
.empty-title { font-size: $font-size-base; font-weight: 600; color: $text-primary; }
.empty-desc { font-size: $font-size-sm; color: $text-muted; }

// ── 결제 이력 테이블 ──────────────────────────────
.payment-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;

  th {
    padding: $spacing-2 $spacing-3;
    text-align: left;
    font-size: $font-size-xs;
    font-weight: 600;
    color: $text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    background: $bg-light;
    border-bottom: 2px solid $border;
  }

  td {
    padding: $spacing-3;
    border-bottom: 1px solid $border;
    color: $text-primary;
  }

  tr:last-child td { border-bottom: none; }
}

.pay-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 600;

  &--completed { background: #D1FAE5; color: #065F46; }
  &--pending   { background: #FEF9C3; color: #92400E; }
  &--refunded  { background: #E5E7EB; color: #374151; }
  &--failed    { background: #FEE2E2; color: #991B1B; }
}

.empty-payments {
  text-align: center;
  padding: $spacing-8;
  color: $text-muted;
  font-size: $font-size-sm;
}

.fw-600 { font-weight: 600; }
.text-muted { color: $text-muted; font-size: $font-size-xs; }
</style>
