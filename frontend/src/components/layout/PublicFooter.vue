<template>
  <footer class="pub-footer">
    <div class="pub-footer__container">
      <div class="pub-footer__top">
        <div class="pub-footer__brand">
          <RouterLink to="/" class="pub-footer__logo">
            <span class="pub-footer__logo-icon">AI</span>
            <span class="pub-footer__logo-text">EDU</span>
          </RouterLink>
          <p>AI 기반 맞춤형 학습·평가 플랫폼<br />학생 한 명 한 명의 성장을 응원합니다</p>
        </div>
        <div class="pub-footer__links">
          <div v-for="col in footerLinks" :key="col.title" class="pub-footer__col">
            <strong>{{ col.title }}</strong>
            <ul>
              <li v-for="link in col.items" :key="link.label">
                <RouterLink v-if="link.to" :to="link.to">{{ link.label }}</RouterLink>
                <a v-else :href="link.anchor || '#'">{{ link.label }}</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="pub-footer__bottom">
        <p>고객센터: 1670-0000 (평일 09:00~18:00) | contact@aiedu.com</p>
        <p class="pub-footer__biz">사업자등록번호: 000-00-00000 | 대표자: (클라이언트 제공 필요) | 주소: (클라이언트 제공 필요)</p>
        <p class="pub-footer__copy">© 2026 AI EDU. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
const footerLinks = [
  {
    title: '서비스',
    items: [
      { label: '플랫폼 소개', anchor: '/#intro' },
      { label: '주요 기능', anchor: '/#features' },
      { label: '커리큘럼', anchor: '/#curriculum' },
      { label: '강사진 소개', anchor: '/#teachers' }
    ]
  },
  {
    title: '이용 안내',
    items: [
      { label: '이용 가이드', anchor: '/#guide' },
      { label: 'FAQ', anchor: '/#faq' },
      { label: '공지사항', to: '/announcements' }
    ]
  },
  {
    title: '계정',
    items: [
      { label: '회원가입', to: '/register' },
      { label: '로그인', to: '/login' }
    ]
  },
  {
    title: '약관 및 정책',
    items: [
      { label: '이용약관', to: '/terms' },
      { label: '개인정보처리방침', to: '/privacy' }
    ]
  }
]
</script>

<style scoped lang="scss">
.pub-footer {
  background: #0f172a;
  padding: 64px 0 $spacing-8;

  &__container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-6;
  }

  &__top {
    display: grid;
    grid-template-columns: 260px 1fr;
    gap: $spacing-12;
    padding-bottom: $spacing-10;
    border-bottom: 1px solid rgba(255,255,255,0.07);
    margin-bottom: $spacing-8;

    @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }
  }

  &__brand {
    p {
      font-size: $font-size-sm;
      color: rgba(255,255,255,0.35);
      margin-top: $spacing-4;
      line-height: 1.8;
    }
  }

  &__logo {
    display: inline-flex;
    align-items: center;
    gap: $spacing-2;
    text-decoration: none;
  }

  &__logo-icon {
    background: $primary;
    color: white;
    padding: 5px 9px;
    border-radius: $radius-sm;
    font-size: $font-size-base;
    font-weight: 900;
  }

  &__logo-text {
    font-size: $font-size-xl;
    font-weight: 800;
    color: white;
  }

  &__links {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $spacing-6;

    @media (max-width: $bp-tablet) { grid-template-columns: repeat(2, 1fr); }
  }

  &__col {
    strong {
      display: block;
      font-size: $font-size-sm;
      font-weight: 700;
      color: white;
      margin-bottom: $spacing-4;
    }

    ul {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: $spacing-3;
    }

    a {
      font-size: $font-size-sm;
      color: rgba(255,255,255,0.4);
      text-decoration: none;
      transition: color 0.2s;

      &:hover { color: rgba(255,255,255,0.9); }
    }
  }

  &__bottom {
    p {
      font-size: $font-size-xs;
      color: rgba(255,255,255,0.25);
      margin-bottom: $spacing-2;
    }
  }

  &__biz {
    font-size: 10px !important;
    color: rgba(255,255,255,0.18) !important;
    margin-bottom: $spacing-1 !important;
  }

  &__copy { color: rgba(255,255,255,0.15) !important; }
}
</style>
