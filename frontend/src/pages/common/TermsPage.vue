<template>
  <div class="policy-page">
    <PublicHeader />
    <div class="policy-page__body">
      <h1>이용약관</h1>
      <p class="policy-page__notice">본 이용약관은 클라이언트 확정 후 업데이트 예정입니다.</p>
      <div class="policy-page__placeholder">
        <p>제1조 (목적)</p>
        <p>이 약관은 AI EDU(이하 "회사")가 제공하는 AI 기반 수학 교육 플랫폼 서비스(이하 "서비스")의 이용조건 및 절차, 기타 필요한 사항을 규정함을 목적으로 합니다.</p>
        <p>제2조 (약관의 효력 및 변경)</p>
        <p>① 본 약관은 서비스를 이용하고자 하는 모든 이용자에게 공시되며, 이용자가 서비스에 가입하거나 이용하는 행위는 이 약관에 동의하는 것으로 간주합니다.</p>
        <p>② 회사는 합리적인 사유가 발생한 경우 관련 법령에 위배되지 않는 범위에서 이 약관을 변경할 수 있습니다.</p>
        <br/>
        <p style="color:#9ca3af">※ 상세 약관 내용은 클라이언트 법무팀 검토 후 최종 확정 예정입니다.</p>
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
    p:first-child, p:nth-child(3) { font-weight: 700; color: $text-primary; }
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
