<template>
  <div class="policy-page">
    <PublicHeader />
    <div class="policy-page__body">
      <h1>개인정보처리방침</h1>
      <p class="policy-page__notice">본 개인정보처리방침은 클라이언트 확정 후 업데이트 예정입니다.</p>
      <div class="policy-page__placeholder">
        <p>제1조 (개인정보의 처리 목적)</p>
        <p>AI EDU(이하 "회사")는 다음의 목적을 위하여 개인정보를 처리합니다. 처리하고 있는 개인정보는 다음의 목적 이외의 용도로는 이용되지 않으며, 이용 목적이 변경되는 경우에는 개인정보 보호법 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행할 예정입니다.</p>
        <p>제2조 (개인정보의 처리 및 보유 기간)</p>
        <p>① 회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.</p>
        <p>② 각각의 개인정보 처리 및 보유 기간은 다음과 같습니다: 회원 가입 및 관리 — 회원 탈퇴 시까지</p>
        <p>제3조 (개인정보의 제3자 제공)</p>
        <p>회사는 정보주체의 개인정보를 제1조(개인정보의 처리 목적)에서 명시한 범위 내에서만 처리하며, 정보주체의 동의, 법률의 특별한 규정 등 개인정보 보호법 제17조 및 제18조에 해당하는 경우에만 개인정보를 제3자에게 제공합니다.</p>
        <br/>
        <p style="color:#9ca3af">※ 상세 개인정보처리방침은 클라이언트 법무팀 검토 후 최종 확정 예정입니다.</p>
      </div>
      <RouterLink to="/" class="policy-page__back">← 메인으로 돌아가기</RouterLink>
    </div>
    <PublicFooter />
  </div>
</template>

<script setup>
import PublicHeader from '@/components/layout/PublicHeader.vue'
import PublicFooter from '@/components/layout/PublicFooter.vue'
</script>

<style scoped lang="scss">
.policy-page {
  background: #f8fafc;
  min-height: 100vh;

  &__body {
    max-width: 800px;
    margin: 0 auto;
    padding: 120px 24px 80px;

    h1 {
      font-size: $font-size-3xl;
      font-weight: 800;
      color: $text-primary;
      margin-bottom: $spacing-4;
    }
  }

  &__notice {
    color: $primary;
    font-size: $font-size-sm;
    font-weight: 600;
    background: #eff6ff;
    border: 1px solid #bfdbfe;
    border-radius: $radius-md;
    padding: $spacing-3 $spacing-4;
    margin-bottom: $spacing-8;
  }

  &__placeholder {
    background: white;
    border: 1px solid $border;
    border-radius: $radius-lg;
    padding: $spacing-8;
    color: $text-secondary;
    font-size: $font-size-sm;
    line-height: 2;

    p { margin-bottom: $spacing-3; }
    p:first-child, p:nth-child(3), p:nth-child(5), p:nth-child(7) {
      font-weight: 700;
      color: $text-primary;
    }
  }

  &__back {
    display: inline-block;
    margin-top: $spacing-8;
    color: $primary;
    font-weight: 600;
    font-size: $font-size-sm;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}
</style>
