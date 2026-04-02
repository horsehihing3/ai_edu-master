<template>
  <div class="landing">

    <!-- ===== 헤더 ===== -->
    <PublicHeader :transparent="true" />

    <!-- ===== S1: 풀스크린 비디오 히어로 ===== -->
    <section class="s-hero" id="intro">
      <!-- 비디오 플레이스홀더 (영상 파일 삽입 위치) -->
      <div style="position:absolute;inset:0;z-index:0;overflow:hidden;background:linear-gradient(135deg,#0f172a,#1e3a8a,#1d4ed8)">
        <video
          ref="videoRef"
          autoplay muted loop playsinline
          style="position:absolute;top:0;left:0;width:100%;height:100%;object-fit:cover"
        >
          <source :src="heroVideoSrc" type="video/mp4" />
        </video>
      </div>
      <div class="s-hero__overlay" />

      <div class="s-hero__content">
        <p class="s-hero__sub anim-fade" style="--delay:0s">AI 기반 맞춤형 학습·평가 플랫폼</p>
        <h1 class="s-hero__title anim-fade" style="--delay:0.2s">
          세상에 없던<br />
          <em class="s-hero__em">AI 기반 교육</em>
        </h1>
        <p class="s-hero__desc anim-fade" style="--delay:0.45s">
          진단 테스트로 학생 수준을 파악하고<br />
          AI가 배정한 맞춤 과제로 함께 성장합니다
        </p>
        <div class="s-hero__btns anim-fade" style="--delay:0.65s">
          <RouterLink to="/register" class="btn-hero-primary">
            10일 무료 체험
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </RouterLink>
          <RouterLink to="/login" class="btn-hero-outline">로그인</RouterLink>
        </div>

        <!-- 플로팅 뱃지 -->
        <div class="s-hero__badges anim-fade" style="--delay:0.9s">
          <div class="hero-badge hero-badge--blue">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            AI 진단 완료
          </div>
          <div class="hero-badge hero-badge--green">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            맞춤 과제 배정
          </div>
          <div class="hero-badge hero-badge--purple">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
            성적 향상
          </div>
        </div>
      </div>

      <a href="#trust" class="s-hero__scroll-hint">
        <span>SCROLL</span>
        <div class="s-hero__scroll-line" />
      </a>
    </section>

    <!-- ===== S2: 수치 신뢰 바 ===== -->
    <section class="s-trust" id="trust" ref="trustSection">
      <div class="lp-container">
        <div class="s-trust__grid">
          <div v-for="t in trustItems" :key="t.label" class="s-trust__item">
            <strong>{{ t.prefix }}{{ t.counted }}{{ t.suffix }}</strong>
            <span>{{ t.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== S3: 주요 기능 커버플로우 ===== -->
    <section class="s-features" id="features">
      <div class="section-hd lp-container">
        <span class="section-hd__tag">주요 기능</span>
        <h2>AI EDU만의 특별한 기능</h2>
        <p>학생 성장을 위해 설계된 맞춤형 학습 시스템</p>
      </div>

      <!-- [2026-04-02] 학생용/선생님용 탭 -->
      <div class="s-features__tabs lp-container">
        <button :class="['feat-tab', { active: featTab === 'student' }]" @click="switchFeatTab('student')">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          학생용
        </button>
        <button :class="['feat-tab', { active: featTab === 'teacher' }]" @click="switchFeatTab('teacher')">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          선생님용
        </button>
      </div>

      <!-- 커버플로우 -->
      <div class="s-features__stage">
        <button class="s-features__arrow s-features__arrow--prev" @click="carouselPrev">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
        </button>

        <div
          class="s-features__coverflow"
          @touchstart="onTouchStart"
          @touchend="onTouchEnd"
        >
          <div
            v-for="(feat, i) in features"
            :key="feat.title"
            class="s-features__cfcard"
            :class="cfClass(i)"
            :style="cfStyle(i)"
            @click="carouselIndex = i"
          >
            <div class="cfcard__glow" />
            <div class="cfcard__icon-wrap">
              <div class="cfcard__icon" v-html="feat.icon" />
            </div>
            <div class="cfcard__num">0{{ i + 1 }}</div>
            <h3 class="cfcard__title">{{ feat.title }}</h3>
            <p class="cfcard__desc">{{ feat.desc }}</p>
          </div>
        </div>

        <button class="s-features__arrow s-features__arrow--next" @click="carouselNext">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>

      <!-- 도트 -->
      <div class="s-features__dots">
        <button
          v-for="(_, i) in features"
          :key="i"
          :class="['s-features__dot', { active: i === carouselIndex }]"
          @click="carouselIndex = i"
        />
      </div>
    </section>

    <!-- ===== S4: 커리큘럼 ===== -->
    <section class="s-curriculum" id="curriculum">
      <div class="lp-container">
        <div class="section-hd">
          <span class="section-hd__tag">커리큘럼</span>
          <h2>3단계 레벨 맞춤 학습</h2>
          <p>진단 후 AI가 배정한 레벨에 맞는 교과목 커리큘럼으로 학습합니다</p>
        </div>
        <div class="s-curriculum__grid">
          <div v-for="lv in levels" :key="lv.name" class="s-curriculum__card">
            <div class="curriculum-svg-wrap" :style="{ '--lv-color': lv.color, background: lv.bgColor }" v-html="lv.svg" />
            <div class="s-curriculum__card-body">
              <div class="s-curriculum__card-level" :style="{ color: lv.color }">LEVEL {{ lv.name }}</div>
              <h3>{{ lv.title }}</h3>
              <p class="s-curriculum__card-target">{{ lv.target }}</p>
              <p class="s-curriculum__card-desc">{{ lv.desc }}</p>
              <ul>
                <li v-for="item in lv.items" :key="item">
                  <span class="check" :style="{ color: lv.color }">✓</span>
                  {{ item }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== S5: 선생님 소개 ===== -->
    <section class="s-teachers" id="teachers">
      <div class="lp-container">
        <div class="section-hd section-hd--light">
          <span class="section-hd__tag section-hd__tag--dark">강사진 소개</span>
          <h2>최고의 선생님들이<br />함께합니다</h2>
          <p>현직 교과 담당 선생님들이 학생 한 명 한 명을 책임집니다</p>
        </div>
        <div class="s-teachers__grid">
          <div v-for="teacher in teachers" :key="teacher.name" class="teacher-card">
            <div class="teacher-photo-wrap">
              <img :src="teacher.photo" :alt="teacher.name" class="teacher-photo" />
            </div>
            <div class="teacher-card__info">
              <strong class="teacher-card__name">{{ teacher.name }}</strong>
              <span class="teacher-card__subject">{{ teacher.subject }}</span>
              <p class="teacher-card__career">{{ teacher.career }}</p>
              <blockquote class="teacher-card__quote">"{{ teacher.quote }}"</blockquote>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== S6: 브랜드 두 섹션 ===== -->
    <section class="s-brand">
      <div class="s-brand__half s-brand__half--student">
        <!-- 배경 이미지 플레이스홀더 -->
        <div class="brand-bg-placeholder" />
        <div class="s-brand__half-overlay" />
        <div class="s-brand__half-content">
          <span class="s-brand__half-tag">학생을 위한</span>
          <h3>스스로 성장하는<br />맞춤형 학습</h3>
          <p>AI가 분석한 취약점을 집중 공략하여<br />빠른 성적 향상을 경험하세요</p>
          <RouterLink to="/register?role=STUDENT" class="btn-brand">학생 회원가입 →</RouterLink>
        </div>
      </div>
      <div class="s-brand__half s-brand__half--teacher">
        <div class="brand-bg-placeholder brand-bg-placeholder--teacher" />
        <div class="s-brand__half-overlay" />
        <div class="s-brand__half-content">
          <span class="s-brand__half-tag">교사를 위한</span>
          <h3>효율적인 학생<br />관리 시스템</h3>
          <p>과제 출제부터 성과 분석까지<br />한 화면에서 모든 것을 관리하세요</p>
          <RouterLink to="/register?role=TEACHER" class="btn-brand">교사 회원가입 →</RouterLink>
        </div>
      </div>
    </section>

    <!-- ===== S7: 수강 후기 ===== -->
    <section class="s-reviews" id="reviews">
      <div class="lp-container">
        <div class="section-hd">
          <span class="section-hd__tag">수강 후기</span>
          <h2>실제 사용자들의 이야기</h2>
          <p>AI EDU로 변화를 경험한 사용자들의 생생한 후기</p>
        </div>
        <div class="s-reviews__grid">
          <div v-for="r in reviews" :key="r.name" class="review-card">
            <div class="review-thumb-placeholder">
              <img v-if="r.photo" :src="r.photo" :alt="r.name" class="review-thumb-img" />
              <svg v-else width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <div class="review-card__body">
              <div class="review-card__stars">★★★★★</div>
              <p class="review-card__text">"{{ r.text }}"</p>
              <div class="review-card__author">
                <strong>{{ r.name }}</strong>
                <span>{{ r.role }}</span>
              </div>
              <div class="review-card__result" v-if="r.result">
                <span class="review-card__result-badge">{{ r.result }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== S8: 이용 안내 ===== -->
    <section class="s-guide" id="guide">
      <div class="lp-container">
        <div class="section-hd">
          <span class="section-hd__tag">이용 안내</span>
          <h2>어떻게 이용하나요?</h2>
          <p>학생과 교사 모두 간단한 절차로 바로 시작할 수 있습니다</p>
        </div>

        <!-- 탭 선택 -->
        <div class="guide-tabs">
          <button :class="['guide-tab', { active: guideTab === 'student' }]" @click="guideTab = 'student'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            학생 이용 가이드
          </button>
          <button :class="['guide-tab', { active: guideTab === 'teacher' }]" @click="guideTab = 'teacher'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            교사 이용 가이드
          </button>
        </div>

        <!-- 학생 가이드 -->
        <div v-if="guideTab === 'student'" class="guide-steps">
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#eff6ff,#dbeafe)">
              <!-- 회원가입 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="40" y="20" width="140" height="100" rx="10" fill="white" stroke="#bfdbfe" stroke-width="1.5"/>
                <circle cx="110" cy="52" r="18" fill="#3b82f6" opacity="0.15"/>
                <circle cx="110" cy="52" r="10" fill="#3b82f6" opacity="0.5"/>
                <rect x="70" y="78" width="80" height="8" rx="4" fill="#bfdbfe"/>
                <rect x="80" y="92" width="60" height="8" rx="4" fill="#bfdbfe"/>
                <rect x="85" y="108" width="50" height="14" rx="7" fill="#3b82f6" opacity="0.7"/>
                <text x="110" y="119" text-anchor="middle" fill="white" font-size="8" font-weight="700">회원가입</text>
              </svg>
            </div>
            <div class="guide-step__num">01</div>
            <div class="guide-step__body">
              <h4>회원가입 & 로그인</h4>
              <p>학생으로 회원가입 후 로그인합니다. 이름·이메일·학년을 입력하면 바로 가입 완료입니다.</p>
            </div>
          </div>
          <div class="guide-step__arrow">→</div>
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#f0fdf4,#dcfce7)">
              <!-- AI 진단 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="30" y="30" width="160" height="90" rx="10" fill="white" stroke="#bbf7d0" stroke-width="1.5"/>
                <rect x="45" y="48" width="50" height="6" rx="3" fill="#86efac"/>
                <rect x="45" y="60" width="70" height="6" rx="3" fill="#86efac" opacity="0.6"/>
                <rect x="45" y="72" width="40" height="6" rx="3" fill="#86efac" opacity="0.4"/>
                <circle cx="158" cy="75" r="22" fill="#10b981" opacity="0.12"/>
                <text x="158" y="70" text-anchor="middle" fill="#10b981" font-size="14" font-weight="900">AI</text>
                <text x="158" y="83" text-anchor="middle" fill="#10b981" font-size="8">진단중</text>
                <rect x="45" y="98" width="130" height="10" rx="5" fill="#d1fae5"/>
                <rect x="45" y="98" width="85" height="10" rx="5" fill="#10b981" opacity="0.6"/>
              </svg>
            </div>
            <div class="guide-step__num">02</div>
            <div class="guide-step__body">
              <h4>AI 진단 테스트</h4>
              <p>과목별 진단 테스트(20~30분)를 완료하면 AI가 현재 수준(A·B·C 등급)을 분석하고 맞춤 학습 경로를 제안합니다.</p>
            </div>
          </div>
          <div class="guide-step__arrow">→</div>
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#fdf4ff,#f3e8ff)">
              <!-- 문제풀기 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="30" y="20" width="160" height="105" rx="10" fill="white" stroke="#e9d5ff" stroke-width="1.5"/>
                <rect x="45" y="35" width="90" height="7" rx="3" fill="#c4b5fd" opacity="0.7"/>
                <rect x="45" y="50" width="130" height="5" rx="2" fill="#ede9fe"/>
                <rect x="45" y="60" width="110" height="5" rx="2" fill="#ede9fe"/>
                <circle cx="55" cy="80" r="6" fill="#a78bfa" opacity="0.3"/>
                <rect x="67" y="76" width="80" height="7" rx="3" fill="#ede9fe"/>
                <circle cx="55" cy="97" r="6" fill="#7c3aed" opacity="0.6"/>
                <rect x="67" y="93" width="65" height="7" rx="3" fill="#ede9fe"/>
                <rect x="140" y="108" width="40" height="12" rx="6" fill="#7c3aed" opacity="0.7"/>
                <text x="160" y="118" text-anchor="middle" fill="white" font-size="7" font-weight="700">제출</text>
              </svg>
            </div>
            <div class="guide-step__num">03</div>
            <div class="guide-step__body">
              <h4>문제 풀기</h4>
              <p>교사가 배정한 과제 또는 AI 추천 문제를 풀고, 모르는 문제는 동영상 풀이를 바로 확인할 수 있습니다. 오답은 자동으로 오답노트에 저장됩니다.</p>
            </div>
          </div>
          <div class="guide-step__arrow">→</div>
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#fff7ed,#ffedd5)">
              <!-- 성적 리포트 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="30" y="20" width="160" height="100" rx="10" fill="white" stroke="#fed7aa" stroke-width="1.5"/>
                <rect x="48" y="95" width="20" height="15" rx="3" fill="#fb923c" opacity="0.4"/>
                <rect x="74" y="78" width="20" height="32" rx="3" fill="#fb923c" opacity="0.6"/>
                <rect x="100" y="62" width="20" height="48" rx="3" fill="#fb923c" opacity="0.8"/>
                <rect x="126" y="50" width="20" height="60" rx="3" fill="#f97316"/>
                <rect x="152" y="40" width="20" height="70" rx="3" fill="#ea580c"/>
                <line x1="44" y1="115" x2="176" y2="115" stroke="#fed7aa" stroke-width="1.5"/>
                <rect x="45" y="28" width="60" height="7" rx="3" fill="#fed7aa"/>
                <text x="75" y="34" text-anchor="middle" fill="#c2410c" font-size="7" font-weight="700">성적 향상 추이</text>
              </svg>
            </div>
            <div class="guide-step__num">04</div>
            <div class="guide-step__body">
              <h4>성적 리포트 확인</h4>
              <p>학습 리포트에서 과목별 정답률·오답 패턴·성장 추이를 한눈에 확인하고 취약 단원을 집중 보완합니다.</p>
            </div>
          </div>
        </div>

        <!-- 교사 가이드 -->
        <div v-if="guideTab === 'teacher'" class="guide-steps">
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#eff6ff,#dbeafe)">
              <!-- 교사 계정 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="40" y="20" width="140" height="100" rx="10" fill="white" stroke="#bfdbfe" stroke-width="1.5"/>
                <circle cx="110" cy="55" r="20" fill="#3b82f6" opacity="0.12"/>
                <circle cx="110" cy="50" r="12" fill="#3b82f6" opacity="0.4"/>
                <path d="M85 80 Q110 70 135 80" stroke="#3b82f6" stroke-width="2" fill="none" opacity="0.4"/>
                <rect x="95" y="60" width="30" height="4" rx="2" fill="#93c5fd"/>
                <rect x="95" y="34" width="30" height="8" rx="4" fill="#1d4ed8" opacity="0.6"/>
                <text x="110" y="41" text-anchor="middle" fill="white" font-size="6" font-weight="700">교사</text>
                <rect x="65" y="88" width="90" height="20" rx="6" fill="#dbeafe"/>
                <text x="110" y="102" text-anchor="middle" fill="#1d4ed8" font-size="8" font-weight="600">계정 등록 완료 ✓</text>
              </svg>
            </div>
            <div class="guide-step__num">01</div>
            <div class="guide-step__body">
              <h4>교사 계정 가입</h4>
              <p>교사로 회원가입 후 소속 학교를 등록합니다. 관리자 승인 후 학생 관리 기능이 활성화됩니다.</p>
            </div>
          </div>
          <div class="guide-step__arrow">→</div>
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#f0fdf4,#dcfce7)">
              <!-- 학생 등록 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="25" y="25" width="170" height="95" rx="10" fill="white" stroke="#bbf7d0" stroke-width="1.5"/>
                <circle cx="60" cy="60" r="12" fill="#10b981" opacity="0.2"/>
                <circle cx="95" cy="60" r="12" fill="#10b981" opacity="0.3"/>
                <circle cx="130" cy="60" r="12" fill="#10b981" opacity="0.4"/>
                <circle cx="165" cy="60" r="12" fill="#10b981" opacity="0.5"/>
                <rect x="40" y="78" width="40" height="5" rx="2" fill="#a7f3d0"/>
                <rect x="75" y="78" width="40" height="5" rx="2" fill="#a7f3d0"/>
                <rect x="110" y="78" width="40" height="5" rx="2" fill="#a7f3d0"/>
                <rect x="145" y="78" width="30" height="5" rx="2" fill="#a7f3d0"/>
                <rect x="35" y="35" width="80" height="8" rx="4" fill="#d1fae5"/>
                <text x="75" y="42" text-anchor="middle" fill="#065f46" font-size="7" font-weight="600">1학년 3반 · 28명</text>
                <rect x="75" y="98" width="70" height="12" rx="6" fill="#10b981" opacity="0.7"/>
                <text x="110" y="108" text-anchor="middle" fill="white" font-size="7" font-weight="700">+ 학생 추가</text>
              </svg>
            </div>
            <div class="guide-step__num">02</div>
            <div class="guide-step__body">
              <h4>학생 등록 & 반 구성</h4>
              <p>학생 계정을 초대하거나 직접 등록하여 학급을 구성합니다. 학년·과목별로 그룹을 나눠 관리할 수 있습니다.</p>
            </div>
          </div>
          <div class="guide-step__arrow">→</div>
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#fdf4ff,#f3e8ff)">
              <!-- 과제 배정 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="30" y="18" width="100" height="110" rx="8" fill="white" stroke="#e9d5ff" stroke-width="1.5"/>
                <rect x="40" y="32" width="80" height="6" rx="3" fill="#c4b5fd" opacity="0.7"/>
                <rect x="40" y="44" width="70" height="4" rx="2" fill="#ede9fe"/>
                <rect x="40" y="53" width="75" height="4" rx="2" fill="#ede9fe"/>
                <rect x="40" y="62" width="60" height="4" rx="2" fill="#ede9fe"/>
                <rect x="40" y="75" width="80" height="6" rx="3" fill="#c4b5fd" opacity="0.5"/>
                <rect x="40" y="87" width="65" height="4" rx="2" fill="#ede9fe"/>
                <rect x="40" y="96" width="70" height="4" rx="2" fill="#ede9fe"/>
                <path d="M135 75 L155 75" stroke="#a78bfa" stroke-width="2" stroke-dasharray="4,2" marker-end="url(#arr)"/>
                <rect x="155" y="55" width="42" height="55" rx="8" fill="#7c3aed" opacity="0.1" stroke="#c4b5fd" stroke-width="1"/>
                <circle cx="167" cy="70" r="6" fill="#7c3aed" opacity="0.3"/>
                <circle cx="183" cy="70" r="6" fill="#7c3aed" opacity="0.3"/>
                <circle cx="175" cy="84" r="6" fill="#7c3aed" opacity="0.3"/>
                <text x="176" y="100" text-anchor="middle" fill="#7c3aed" font-size="6">배정완료</text>
              </svg>
            </div>
            <div class="guide-step__num">03</div>
            <div class="guide-step__body">
              <h4>기출문제로 과제 배정</h4>
              <p>50,000+ 기출문제 DB에서 단원·난이도별로 문제를 선택하여 학생 또는 학급 전체에 과제를 배정합니다.</p>
            </div>
          </div>
          <div class="guide-step__arrow">→</div>
          <div class="guide-step">
            <div class="guide-step__illust" style="background:linear-gradient(135deg,#fff7ed,#ffedd5)">
              <!-- 성취도 리포트 일러스트 -->
              <svg width="100%" viewBox="0 0 220 140" fill="none">
                <rect x="28" y="18" width="164" height="108" rx="10" fill="white" stroke="#fed7aa" stroke-width="1.5"/>
                <rect x="40" y="28" width="70" height="7" rx="3" fill="#fed7aa"/>
                <text x="75" y="35" text-anchor="middle" fill="#c2410c" font-size="7" font-weight="700">반별 성취도 현황</text>
                <rect x="40" y="95" width="18" height="25" rx="3" fill="#fb923c" opacity="0.4"/>
                <rect x="63" y="80" width="18" height="40" rx="3" fill="#fb923c" opacity="0.6"/>
                <rect x="86" y="65" width="18" height="55" rx="3" fill="#f97316" opacity="0.8"/>
                <rect x="109" y="55" width="18" height="65" rx="3" fill="#f97316"/>
                <rect x="132" y="45" width="18" height="75" rx="3" fill="#ea580c"/>
                <rect x="155" y="38" width="18" height="82" rx="3" fill="#c2410c"/>
                <line x1="38" y1="122" x2="178" y2="122" stroke="#fed7aa" stroke-width="1"/>
                <path d="M45 90 Q85 65 130 50 Q155 43 170 38" stroke="#ef4444" stroke-width="1.5" fill="none" stroke-dasharray="3,2"/>
              </svg>
            </div>
            <div class="guide-step__num">04</div>
            <div class="guide-step__body">
              <h4>성취도 리포트 분석</h4>
              <p>학생별·반별 정답률, 과제 제출 현황, 취약 단원을 실시간으로 확인합니다. 데이터 기반으로 수업 방향을 조정할 수 있습니다.</p>
            </div>
          </div>
        </div>

      </div>
    </section>

    <!-- ===== S9: FAQ ===== -->
    <section class="s-faq" id="faq">
      <div class="lp-container">
        <div class="section-hd">
          <span class="section-hd__tag">자주 묻는 질문</span>
          <h2>FAQ</h2>
        </div>
        <div class="s-faq__list">
          <div
            v-for="(q, i) in faqs"
            :key="i"
            class="faq-item"
            :class="{ open: openFaq === i }"
            @click="openFaq = openFaq === i ? null : i"
          >
            <div class="faq-item__q">
              <span class="faq-item__qmark">Q</span>
              {{ q.question }}
              <svg class="faq-item__arrow" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
            </div>
            <div v-if="openFaq === i" class="faq-item__a">
              <span class="faq-item__amark">A</span>
              {{ q.answer }}
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== S10: 가격 플랜 ===== -->
    <section class="s-pricing" id="pricing">
      <div class="lp-container">
        <div class="section-hd">
          <span class="section-hd__tag">요금 안내</span>
          <h2>합리적인 가격으로<br />시작하세요</h2>
          <p>모든 플랜은 월 단위 구독이며, 언제든 해지할 수 있습니다</p>
        </div>

        <div class="s-pricing__grid">

          <!-- 베이직 -->
          <div class="price-card">
            <div class="price-card__header">
              <p class="price-card__name">베이직</p>
              <div class="price-card__price">
                <span class="price-card__amount">9,900</span>
                <span class="price-card__unit">원 / 월</span>
              </div>
              <p class="price-card__desc">학습을 시작하는 학생에게</p>
            </div>
            <ul class="price-card__features">
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 문제 풀기 (무제한)</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> AI 진단 테스트</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 오답노트 자동 저장</li>
              <li class="off"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> 풀이 동영상</li>
              <li class="off"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> 학습 리포트</li>
              <li class="off"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> AI 힌트</li>
            </ul>
            <RouterLink to="/register" class="price-card__btn">시작하기</RouterLink>
          </div>

          <!-- 스탠다드 (추천) -->
          <div class="price-card price-card--featured">
            <div class="price-card__badge">가장 인기</div>
            <div class="price-card__header">
              <p class="price-card__name">스탠다드</p>
              <div class="price-card__price">
                <span class="price-card__amount">19,900</span>
                <span class="price-card__unit">원 / 월</span>
              </div>
              <p class="price-card__desc">성적 향상을 원하는 학생에게</p>
            </div>
            <ul class="price-card__features">
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 문제 풀기 (무제한)</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> AI 진단 테스트</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 오답노트 자동 저장</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 풀이 동영상</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 학습 리포트</li>
              <li class="off"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg> AI 힌트</li>
            </ul>
            <RouterLink to="/register" class="price-card__btn price-card__btn--featured">시작하기</RouterLink>
          </div>

          <!-- 프리미엄 -->
          <div class="price-card">
            <div class="price-card__header">
              <p class="price-card__name">프리미엄</p>
              <div class="price-card__price">
                <span class="price-card__amount">29,900</span>
                <span class="price-card__unit">원 / 월</span>
              </div>
              <p class="price-card__desc">최고의 결과를 원하는 학생에게</p>
            </div>
            <ul class="price-card__features">
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 문제 풀기 (무제한)</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> AI 진단 테스트</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 오답노트 자동 저장</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 풀이 동영상</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> 학습 리포트</li>
              <li class="on"><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg> AI 힌트</li>
            </ul>
            <RouterLink to="/register" class="price-card__btn">시작하기</RouterLink>
          </div>

        </div>

        <p class="s-pricing__note">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          모든 플랜은 회원가입 후 <strong>10일 무료 체험</strong> 가능합니다. 신용카드 없이 시작하세요.
        </p>
      </div>
    </section>

    <!-- ===== CTA ===== -->
    <section class="s-cta">
      <div class="s-cta__bg" />
      <div class="lp-container">
        <div class="s-cta__inner">
          <h2>지금 바로 AI EDU를 시작하세요</h2>
          <p>회원가입 후 10일간 모든 기능을 무료로 이용할 수 있습니다</p>
          <RouterLink to="/register" class="btn-cta">
            무료로 시작하기
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- ===== 푸터 ===== -->
    <PublicFooter />

  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import PublicHeader from '@/components/layout/PublicHeader.vue'
import PublicFooter from '@/components/layout/PublicFooter.vue'

const trustSection = ref(null)
const videoRef = ref(null)
const route = useRoute()

// 영상 파일 경로 — public/videos/video.mp4
const heroVideoSrc = '/videos/video.mp4'

onMounted(async () => {
  // hash 앵커 스크롤 (다른 페이지에서 /#section 으로 진입 시)
  if (route.hash) {
    setTimeout(() => {
      const el = document.querySelector(route.hash)
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }, 100)
  }

  // 비디오 강제 로드 및 재생
  await nextTick()
  if (videoRef.value) {
    videoRef.value.load()
    videoRef.value.play().catch(() => {})
  }

  // 수치 바 진입 시 카운트업
  const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      startCountUp()
      observer.disconnect()
    }
  }, { threshold: 0.4 })
  if (trustSection.value) observer.observe(trustSection.value)

})

const openFaq = ref(null)
const guideTab = ref('student')
const featTab = ref('student')

function switchFeatTab(tab) {
  featTab.value = tab
  carouselIndex.value = 0
}

// 커버플로우
const carouselIndex = ref(0)
let touchStartX = 0

function carouselNext() {
  carouselIndex.value = (carouselIndex.value + 1) % features.length
}
function carouselPrev() {
  carouselIndex.value = (carouselIndex.value - 1 + features.length) % features.length
}
function onTouchStart(e) {
  touchStartX = e.touches[0].clientX
}
function onTouchEnd(e) {
  const diff = touchStartX - e.changedTouches[0].clientX
  if (Math.abs(diff) > 40) {
    diff > 0 ? carouselNext() : carouselPrev()
  }
}

function cfOffset(i) {
  const n = features.length
  let off = i - carouselIndex.value
  if (off > n / 2) off -= n
  if (off < -n / 2) off += n
  return off
}

function cfStyle(i) {
  const off = cfOffset(i)
  const abs = Math.abs(off)
  const dir = abs === 0 ? 0 : off / abs
  const isMobile = window.innerWidth <= 768
  const xMap  = isMobile ? [0, 160, 280, 360] : [0, 270, 480, 620]
  const ryMap = isMobile ? [0,  30,  45,  55] : [0,  42,  60,  72]
  const scMap = isMobile ? [1, 0.75, 0.55, 0.40] : [1, 0.80, 0.62, 0.46]
  const opMap = isMobile ? [1, 0.70, 0.35,    0] : [1, 0.80, 0.52,    0]
  const zMap  = [10,   8,   5,     2]
  const idx = Math.min(abs, 3)
  return {
    transform: `translate(-50%, -50%) translateX(${dir * xMap[idx]}px) rotateY(${-dir * ryMap[idx]}deg) scale(${scMap[idx]})`,
    opacity: opMap[idx],
    zIndex: zMap[idx],
    pointerEvents: abs > 2 ? 'none' : 'auto',
  }
}

function cfClass(i) {
  const abs = Math.abs(cfOffset(i))
  return { 'is-active': abs === 0, 'is-side': abs === 1, 'is-far': abs >= 2 }
}

// ── 데이터 ──────────────────────────────────────────

const trustItems = ref([
  { target: 10000, counted: 0, prefix: '', suffix: '+', label: '누적 학생 수' },
  { target: 50000, counted: 0, prefix: '', suffix: '+', label: '문제 DB' },
  { target: 98,    counted: 0, prefix: '', suffix: '%', label: '학생 만족도' },
  { target: 120,   counted: 0, prefix: '', suffix: '억+', label: '누적 투자 유치' },
  { target: 4.9,   counted: 0, prefix: '', suffix: ' ★', label: '앱스토어 평점' },
])

function startCountUp() {
  trustItems.value.forEach(item => {
    const isDecimal = item.target % 1 !== 0
    const duration = 1600
    const steps = 60
    const interval = duration / steps
    let step = 0
    const timer = setInterval(() => {
      step++
      const progress = step / steps
      // easeOutQuart
      const eased = 1 - Math.pow(1 - progress, 4)
      const current = item.target * eased
      item.counted = isDecimal ? Math.round(current * 10) / 10 : Math.floor(current).toLocaleString()
      if (step >= steps) {
        item.counted = isDecimal ? item.target : item.target.toLocaleString()
        clearInterval(timer)
      }
    }, interval)
  })
}

// [2026-04-02] 학생용/선생님용 탭별 기능 데이터
const studentFeatures = [
  {
    title: '정밀 진단 테스트',
    desc: 'AI가 학생의 수준을 정밀 분석하여 A/B/C 레벨을 배정합니다. 취약 단원을 파악해 집중 학습 커리큘럼을 제안합니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>`
  },
  {
    title: 'AI 맞춤 문제 추천',
    desc: '레벨과 취약 유형에 맞는 문제를 AI가 선별 제공합니다. 수식 렌더링으로 수학 문제도 완벽하게 표현됩니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>`
  },
  {
    title: '동영상 풀이 강의',
    desc: '어려운 문제는 전문 강사의 동영상 해설로 완벽하게 이해합니다. 언제 어디서나 반복 시청이 가능합니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>`
  },
  {
    title: '오답노트 자동 저장',
    desc: '틀린 문제는 자동으로 오답노트에 저장됩니다. 취약 유형을 분석하고 반복 학습으로 완벽하게 극복합니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>`
  },
  {
    title: '학습 리포트',
    desc: '주별·월별 학습량, 정답률, 취약 단원을 분석한 리포트를 제공합니다. 학부모도 언제든지 확인할 수 있습니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`
  }
]

const teacherFeatures = [
  {
    title: '과제 출제 · 배정',
    desc: '50,000+ 기출문제 DB에서 단원·난이도별로 문제를 선택해 학생 개인 또는 학급 전체에 과제를 배정합니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/></svg>`
  },
  {
    title: '학생 진도 실시간 확인',
    desc: '학생별 과제 제출 현황, 정답률, 학습 시간을 실시간으로 파악합니다. 미제출 학생에게 바로 알림을 전송할 수 있습니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`
  },
  {
    title: '학급별 성취도 리포트',
    desc: '반 전체의 평균 정답률, 취약 단원, 상위/하위 학생 분포를 한눈에 파악합니다. 데이터 기반 수업 계획이 가능합니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`
  },
  {
    title: 'AI 취약점 분석',
    desc: '학생마다 취약한 단원과 유형을 AI가 자동 분석합니다. 맞춤 문제를 추천받아 개인 맞춤 과제를 빠르게 구성할 수 있습니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>`
  },
  {
    title: '1:1 문의 관리',
    desc: '학생·학부모의 문의를 한 곳에서 관리합니다. 답변 완료 여부를 추적하고 알림을 통해 빠른 소통이 가능합니다.',
    icon: `<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`
  }
]

const features = computed(() => featTab.value === 'student' ? studentFeatures : teacherFeatures)

const levels = [
  {
    name: 'A',
    title: '심화 과정',
    target: '정답률 80% 이상',
    color: '#3B82F6',
    bgColor: '#1e3a8a',
    desc: '상위권 학생을 위한 심화 과정. 고난도 문제와 심화 응용 커리큘럼을 제공합니다.',
    items: ['고난도 문제 중심', '심화·응용 학습', '최상위권 진입 전략'],
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 200" width="100%" height="200" style="display:block;vertical-align:top">
      <defs>
        <linearGradient id="bgA" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#1e3a8a"/>
          <stop offset="100%" stop-color="#3b82f6"/>
        </linearGradient>
        <linearGradient id="barA" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#93c5fd"/>
          <stop offset="100%" stop-color="#3b82f6"/>
        </linearGradient>
      </defs>
      <!-- 배경 -->
      <rect width="400" height="200" fill="url(#bgA)"/>
      <!-- 장식 원 -->
      <circle cx="340" cy="40" r="70" fill="rgba(255,255,255,0.05)"/>
      <circle cx="60" cy="170" r="50" fill="rgba(255,255,255,0.05)"/>
      <!-- 그래프 바 (상승) -->
      <rect x="60" y="130" width="28" height="40" rx="4" fill="url(#barA)" opacity="0.5"/>
      <rect x="100" y="110" width="28" height="60" rx="4" fill="url(#barA)" opacity="0.65"/>
      <rect x="140" y="85" width="28" height="85" rx="4" fill="url(#barA)" opacity="0.8"/>
      <rect x="180" y="60" width="28" height="110" rx="4" fill="url(#barA)" opacity="0.9"/>
      <rect x="220" y="38" width="28" height="132" rx="4" fill="url(#barA)"/>
      <!-- 상승 추이선 -->
      <polyline points="74,128 114,108 154,83 194,58 234,36" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.8"/>
      <!-- 꼭지 점 -->
      <circle cx="234" cy="36" r="5" fill="white"/>
      <!-- 트로피 -->
      <path d="M300 60 Q300 95 285 105 L315 105 Q300 95 300 60Z" fill="white" opacity="0.9"/>
      <rect x="292" y="105" width="16" height="6" rx="2" fill="white" opacity="0.9"/>
      <rect x="286" y="111" width="28" height="5" rx="2.5" fill="white" opacity="0.9"/>
      <path d="M285 68 Q275 68 275 78 Q275 88 285 90" fill="none" stroke="white" stroke-width="2" opacity="0.7"/>
      <path d="M315 68 Q325 68 325 78 Q325 88 315 90" fill="none" stroke="white" stroke-width="2" opacity="0.7"/>
      <!-- 레벨 뱃지 -->
      <rect x="24" y="20" width="44" height="28" rx="8" fill="white" opacity="0.15"/>
      <text x="46" y="39" text-anchor="middle" font-family="Arial" font-size="18" font-weight="900" fill="white">A</text>
      <!-- 텍스트 -->
      <text x="376" y="188" text-anchor="end" font-family="Arial" font-size="11" font-weight="700" fill="rgba(255,255,255,0.45)" letter-spacing="2">ADVANCED</text>
    </svg>`
  },
  {
    name: 'B',
    title: '표준 과정',
    target: '정답률 50~79%',
    color: '#10B981',
    bgColor: '#064e3b',
    desc: '중위권 학생을 위한 표준 과정. 핵심 개념을 다지고 응용 문제로 실력을 높입니다.',
    items: ['핵심 개념 완성', '유형별 응용 풀이', '상위권 도약 목표'],
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 200" width="100%" height="200" style="display:block;vertical-align:top">
      <defs>
        <linearGradient id="bgB" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#064e3b"/>
          <stop offset="100%" stop-color="#10b981"/>
        </linearGradient>
      </defs>
      <!-- 배경 -->
      <rect width="400" height="200" fill="url(#bgB)"/>
      <!-- 장식 원 -->
      <circle cx="350" cy="30" r="80" fill="rgba(255,255,255,0.05)"/>
      <circle cx="50" cy="180" r="55" fill="rgba(255,255,255,0.05)"/>
      <!-- 책 왼쪽 -->
      <rect x="55" y="55" width="60" height="90" rx="4" fill="white" opacity="0.12"/>
      <rect x="55" y="55" width="60" height="90" rx="4" fill="none" stroke="white" stroke-width="1.5" opacity="0.4"/>
      <rect x="63" y="70" width="40" height="4" rx="2" fill="white" opacity="0.5"/>
      <rect x="63" y="80" width="34" height="4" rx="2" fill="white" opacity="0.35"/>
      <rect x="63" y="90" width="38" height="4" rx="2" fill="white" opacity="0.35"/>
      <rect x="63" y="100" width="30" height="4" rx="2" fill="white" opacity="0.35"/>
      <rect x="63" y="110" width="36" height="4" rx="2" fill="white" opacity="0.35"/>
      <!-- 책 오른쪽 -->
      <rect x="125" y="62" width="60" height="90" rx="4" fill="white" opacity="0.18"/>
      <rect x="125" y="62" width="60" height="90" rx="4" fill="none" stroke="white" stroke-width="1.5" opacity="0.5"/>
      <rect x="133" y="77" width="42" height="4" rx="2" fill="white" opacity="0.6"/>
      <rect x="133" y="87" width="36" height="4" rx="2" fill="white" opacity="0.4"/>
      <rect x="133" y="97" width="40" height="4" rx="2" fill="white" opacity="0.4"/>
      <rect x="133" y="107" width="28" height="4" rx="2" fill="white" opacity="0.4"/>
      <rect x="133" y="117" width="38" height="4" rx="2" fill="white" opacity="0.4"/>
      <!-- 도넛 차트 -->
      <circle cx="280" cy="95" r="52" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="18"/>
      <circle cx="280" cy="95" r="52" fill="none" stroke="rgba(255,255,255,0.75)" stroke-width="18"
        stroke-dasharray="163 164" stroke-dashoffset="41" stroke-linecap="round"/>
      <circle cx="280" cy="95" r="52" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="18"
        stroke-dasharray="65 262" stroke-dashoffset="-122" stroke-linecap="round"/>
      <text x="280" y="90" text-anchor="middle" font-family="Arial" font-size="20" font-weight="900" fill="white">65%</text>
      <text x="280" y="108" text-anchor="middle" font-family="Arial" font-size="9" fill="rgba(255,255,255,0.6)">정답률</text>
      <!-- 레벨 뱃지 -->
      <rect x="24" y="20" width="44" height="28" rx="8" fill="white" opacity="0.15"/>
      <text x="46" y="39" text-anchor="middle" font-family="Arial" font-size="18" font-weight="900" fill="white">B</text>
      <text x="376" y="188" text-anchor="end" font-family="Arial" font-size="11" font-weight="700" fill="rgba(255,255,255,0.45)" letter-spacing="2">STANDARD</text>
    </svg>`
  },
  {
    name: 'C',
    title: '기초 과정',
    target: '정답률 49% 이하',
    color: '#F59E0B',
    bgColor: '#78350f',
    desc: '기초가 부족한 학생을 위한 맞춤 과정. 쉬운 문제부터 단계별로 학습합니다.',
    items: ['기초 개념 정립', '단계별 쉬운 문제', '꼼꼼한 해설 제공'],
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 200" width="100%" height="200" style="display:block;vertical-align:top">
      <defs>
        <linearGradient id="bgC" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#78350f"/>
          <stop offset="100%" stop-color="#f59e0b"/>
        </linearGradient>
        <linearGradient id="blockC" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="rgba(255,255,255,0.35)"/>
          <stop offset="100%" stop-color="rgba(255,255,255,0.12)"/>
        </linearGradient>
      </defs>
      <!-- 배경 -->
      <rect width="400" height="200" fill="url(#bgC)"/>
      <!-- 장식 원 -->
      <circle cx="340" cy="170" r="90" fill="rgba(255,255,255,0.05)"/>
      <circle cx="30" cy="30" r="55" fill="rgba(255,255,255,0.05)"/>
      <!-- 계단식 블록 (기초부터 쌓아 올리기) -->
      <!-- 1단 -->
      <rect x="55" y="148" width="52" height="22" rx="4" fill="url(#blockC)" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
      <rect x="113" y="148" width="52" height="22" rx="4" fill="url(#blockC)" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
      <rect x="171" y="148" width="52" height="22" rx="4" fill="url(#blockC)" stroke="rgba(255,255,255,0.3)" stroke-width="1"/>
      <!-- 2단 -->
      <rect x="79" y="120" width="52" height="22" rx="4" fill="url(#blockC)" stroke="rgba(255,255,255,0.4)" stroke-width="1"/>
      <rect x="137" y="120" width="52" height="22" rx="4" fill="url(#blockC)" stroke="rgba(255,255,255,0.4)" stroke-width="1"/>
      <!-- 3단 -->
      <rect x="103" y="92" width="52" height="22" rx="4" fill="rgba(255,255,255,0.45)" stroke="rgba(255,255,255,0.6)" stroke-width="1.5"/>
      <!-- 블록 텍스트 -->
      <text x="81" y="163" text-anchor="middle" font-family="Arial" font-size="8" font-weight="700" fill="rgba(255,255,255,0.7)">기초</text>
      <text x="139" y="163" text-anchor="middle" font-family="Arial" font-size="8" font-weight="700" fill="rgba(255,255,255,0.7)">개념</text>
      <text x="197" y="163" text-anchor="middle" font-family="Arial" font-size="8" font-weight="700" fill="rgba(255,255,255,0.7)">문제</text>
      <text x="105" y="135" text-anchor="middle" font-family="Arial" font-size="8" font-weight="700" fill="rgba(255,255,255,0.8)">응용</text>
      <text x="163" y="135" text-anchor="middle" font-family="Arial" font-size="8" font-weight="700" fill="rgba(255,255,255,0.8)">이해</text>
      <text x="129" y="107" text-anchor="middle" font-family="Arial" font-size="8" font-weight="800" fill="white">실력↑</text>
      <!-- 연필 아이콘 -->
      <g transform="translate(280,55) rotate(-30)">
        <rect x="-8" y="-45" width="16" height="60" rx="3" fill="white" opacity="0.85"/>
        <polygon points="-8,15 8,15 0,30" fill="rgba(255,200,50,0.9)"/>
        <rect x="-8" y="-45" width="16" height="10" rx="2" fill="rgba(255,255,255,0.4)"/>
        <line x1="-8" y1="10" x2="8" y2="10" stroke="rgba(0,0,0,0.15)" stroke-width="1"/>
      </g>
      <!-- 체크 아이콘들 -->
      <circle cx="310" cy="120" r="14" fill="rgba(255,255,255,0.15)" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
      <polyline points="304,120 309,126 318,114" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
      <circle cx="350" cy="148" r="14" fill="rgba(255,255,255,0.15)" stroke="rgba(255,255,255,0.4)" stroke-width="1.5"/>
      <polyline points="344,148 349,154 358,142" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
      <!-- 레벨 뱃지 -->
      <rect x="24" y="20" width="44" height="28" rx="8" fill="white" opacity="0.15"/>
      <text x="46" y="39" text-anchor="middle" font-family="Arial" font-size="18" font-weight="900" fill="white">C</text>
      <text x="376" y="188" text-anchor="end" font-family="Arial" font-size="11" font-weight="700" fill="rgba(255,255,255,0.45)" letter-spacing="2">BASIC</text>
    </svg>`
  }
]

const teachers = [
  {
    name: '김민준 선생님',
    subject: '수학 담당',
    career: '서울대 수학교육과 졸업 · 중학교 수학 10년 경력',
    quote: '수학은 이해가 전부입니다. 개념을 잡으면 문제는 자연히 풀립니다.',
    photo: '/images/teachers/teacher1.jpg'
  },
  {
    name: '이서연 선생님',
    subject: '영어 담당',
    career: '연세대 영어교육과 졸업 · 중학교 영어 8년 경력',
    quote: '학생 한 명 한 명의 가능성을 믿습니다. 함께라면 반드시 성장합니다.',
    photo: '/images/teachers/teacher2.jpg'
  },
  {
    name: '박현우 선생님',
    subject: '과학 담당',
    career: '고려대 과학교육과 졸업 · 중학교 과학 12년 경력',
    quote: '약점을 강점으로 바꾸는 것이 진짜 실력입니다.',
    photo: '/images/teachers/teacher3.jpg'
  },
  {
    name: '최지혜 선생님',
    subject: '국어 담당',
    career: '이화여대 국어교육과 졸업 · 중학교 국어 전문',
    quote: '기초가 탄탄해야 모든 과목이 흔들리지 않습니다.',
    photo: '/images/teachers/teacher4.jpg'
  },
  {
    name: '정성호 선생님',
    subject: '사회 담당',
    career: '성균관대 사회교육과 졸업 · 중학교 사회 9년',
    quote: '이해하고 암기하면 사회는 절대 어렵지 않습니다.',
    photo: '/images/teachers/teacher5.jpg'
  },
  {
    name: '한소희 선생님',
    subject: '수학 담당',
    career: '서울교육대 졸업 · 초중등 연계 전문 12년',
    quote: '수학을 좋아하게 만드는 것이 제 목표입니다.',
    photo: '/images/teachers/teacher6.jpg'
  }
]

const reviews = [
  {
    name: '김민준',
    role: '중학교 3학년',
    result: 'B → A 레벨 달성',
    text: '3개월 만에 B레벨에서 A레벨로 올랐어요. AI가 제 약점을 정확히 찾아주니까 효율적으로 공부할 수 있었습니다.',
    photo: '/images/reviews/review1.jpg'
  },
  {
    name: '이서연',
    role: '중학교 2학년',
    result: 'C → B 레벨 달성',
    text: '과목마다 어디가 부족한지 바로 알 수 있어서 좋았어요. 선생님이 배정해준 문제를 풀다 보니 성적이 올랐어요!',
    photo: '/images/reviews/review2.jpg'
  },
  {
    name: '박지우 선생님',
    role: '중학교 수학·과학 담당 교사',
    result: '수업 준비 시간 50% 절감',
    text: '학생별 진도와 정답률을 한눈에 볼 수 있어서 수업 준비 시간이 절반으로 줄었습니다. 과제 관리도 정말 편해요.',
    photo: '/images/reviews/review3.jpg'
  },
  {
    name: '최예린',
    role: '중학교 1학년',
    result: '전 과목 성적 향상',
    text: '오답노트 기능이 정말 좋아요. 틀린 문제를 다시 풀 수 있고 풀이 영상도 볼 수 있어서 완전히 이해하고 넘어갈 수 있어요.',
    photo: '/images/reviews/review4.jpg'
  }
]

const targets = [
  {
    title: '학생',
    icon: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
    desc: '스스로 공부하는 습관을 만들어 드립니다.',
    bg: '#3B82F6',
    items: [
      'AI 맞춤 문제 풀이',
      '진단 테스트로 레벨 파악',
      '오답노트 & 풀이 영상',
      '학습 리포트 자동 생성'
    ]
  },
  {
    title: '교사',
    icon: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    desc: '학생 관리와 과제 배정을 한 곳에서.',
    bg: '#10B981',
    items: [
      '학급별 학생 관리',
      '기출문제 과제 배정',
      '실시간 성적 리포트',
      '전 교과목 지원'
    ]
  }
]

const faqs = [
  { question: '무료 체험은 어떻게 진행되나요?', answer: '회원가입 후 10일간 모든 기능을 무료로 이용하실 수 있습니다. 신용카드 등록 없이 바로 시작할 수 있습니다.' },
  { question: '레벨 진단 테스트는 얼마나 걸리나요?', answer: '약 20~30분 정도 소요됩니다. 테스트 완료 즉시 AI가 레벨(A/B/C)을 배정하고 맞춤 커리큘럼을 제안합니다.' },
  { question: '교사 계정은 별도로 신청해야 하나요?', answer: '회원가입 시 [교사]를 선택하면 됩니다. 학생 관리, 기출문제 과제 배정, 성적 리포트 기능을 바로 사용할 수 있습니다.' },
  { question: '모바일에서도 이용할 수 있나요?', answer: '네, 반응형 웹으로 제작되어 PC, 태블릿, 스마트폰 모두 이용 가능합니다.' },
  { question: '문제는 어떤 과목을 지원하나요?', answer: '현재 수학을 중심으로 서비스를 제공하고 있으며, 순차적으로 과학, 영어 등으로 확대할 예정입니다.' }
]

</script>

<style scoped lang="scss">
.landing { font-family: 'Noto Sans KR', sans-serif; overflow: hidden; width: 100%; }

.lp-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 $spacing-6;
}

// 섹션 헤더
.section-hd {
  text-align: center;
  margin-bottom: 64px;

  &__tag {
    display: inline-block;
    background: $primary-bg;
    color: $primary;
    font-size: 16px;
    font-weight: 700;
    letter-spacing: 1px;
    padding: 8px 22px;
    border-radius: $radius-full;
    margin-bottom: $spacing-5;

    &--dark {
      background: rgba(255,255,255,0.15);
      color: white;
    }
  }

  h2 {
    font-size: clamp(32px, 4vw, 48px);
    font-weight: 900;
    color: $text-primary;
    margin-bottom: $spacing-4;
    line-height: 1.25;
  }

  p {
    font-size: clamp(15px, 1.5vw, 18px);
    color: $text-secondary;
  }

  &--light h2 { color: white; }
  &--light p { color: rgba(255,255,255,0.75); }
}

// ── 로고 (푸터용) ────────────────────────────────────
.lnb-logo {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  text-decoration: none;
  flex-shrink: 0;

  &__icon {
    background: $primary;
    color: white;
    padding: 5px 10px;
    border-radius: $radius-sm;
    font-size: $font-size-sm;
    font-weight: 900;
  }

  &__text {
    font-size: $font-size-xl;
    font-weight: 800;
    color: $primary;
    transition: color 0.3s;
    &--white { color: white; }
  }
}

// ── S1: 히어로 비디오 ────────────────────────────────
.s-hero {
  position: relative;
  height: 100vh;
  min-height: 700px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  &__video-wrap {
    position: absolute;
    inset: 0;
    z-index: 0;
  }

  &__video {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    z-index: 1;
  }

  // 폴백 그라디언트 (비디오 뒤)
  &__video-fallback {
    position: absolute;
    inset: 0;
    z-index: 0;
    background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 40%, #1d4ed8 70%, #0369a1 100%);
  }

  &__overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      to bottom,
      rgba(0,0,0,0.35) 0%,
      rgba(0,0,0,0.55) 50%,
      rgba(0,0,0,0.7) 100%
    );
    z-index: 1;
  }

  &__content {
    position: relative;
    z-index: 2;
    text-align: center;
    color: white;
    padding: 0 $spacing-6;
    max-width: 900px;
  }

  &__sub {
    font-size: $font-size-sm;
    font-weight: 600;
    letter-spacing: 3px;
    text-transform: uppercase;
    color: rgba(255,255,255,0.7);
    margin-bottom: $spacing-5;
  }

  &__title {
    font-size: clamp(40px, 7vw, 80px);
    font-weight: 900;
    line-height: 1.15;
    margin-bottom: $spacing-6;
    text-shadow: 0 2px 20px rgba(0,0,0,0.3);

    em {
      font-style: normal;
      background: linear-gradient(135deg, #60a5fa, #34d399);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  &__desc {
    font-size: clamp($font-size-base, 2vw, $font-size-xl);
    color: rgba(255,255,255,0.82);
    line-height: 1.9;
    margin-bottom: $spacing-10;
  }

  &__btns {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-4;
    flex-wrap: wrap;
  }

  &__scroll-hint {
    position: absolute;
    bottom: 40px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 2;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-2;
    color: rgba(255,255,255,0.5);
    font-size: 10px;
    letter-spacing: 2px;
    text-decoration: none;
    transition: color 0.3s;

    &:hover { color: rgba(255,255,255,0.9); }
  }

  &__scroll-line {
    width: 1px;
    height: 50px;
    background: linear-gradient(to bottom, rgba(255,255,255,0.5), transparent);
    animation: scrollPulse 2s ease-in-out infinite;
  }
}

@keyframes scrollPulse {
  0%, 100% { opacity: 0.5; transform: scaleY(1); }
  50% { opacity: 1; transform: scaleY(1.1); }
}

// ── 히어로 애니메이션 ────────────────────────────────
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(32px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-12px); }
}

@keyframes particleDrift {
  0%   { transform: translateY(0) scale(1); opacity: 0; }
  10%  { opacity: 1; }
  90%  { opacity: 0.6; }
  100% { transform: translateY(-120px) scale(0.5); opacity: 0; }
}

@keyframes gradientShift {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes badgePop {
  0%   { opacity: 0; transform: scale(0.7) translateY(10px); }
  70%  { transform: scale(1.05) translateY(-2px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}

.anim-fade {
  opacity: 0;
  animation: fadeUp 0.75s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  animation-delay: var(--delay, 0s);
}


.s-hero__em {
  display: inline-block;
  background: linear-gradient(90deg, #60a5fa, #34d399, #a78bfa, #60a5fa);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradientShift 4s ease infinite;
}

.s-hero__badges {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-3;
  justify-content: center;
  margin-top: $spacing-8;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  padding: 8px 16px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: 700;
  backdrop-filter: blur(8px);
  animation: badgePop 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;
  border: 1px solid rgba(255,255,255,0.2);

  &--blue {
    background: rgba(59, 130, 246, 0.3);
    color: #bfdbfe;
    animation-delay: 1s, 1s;
  }
  &--green {
    background: rgba(16, 185, 129, 0.3);
    color: #a7f3d0;
    animation-delay: 1.15s, 1.5s;
  }
  &--purple {
    background: rgba(139, 92, 246, 0.3);
    color: #ddd6fe;
    animation-delay: 1.3s, 2s;
  }
}

.btn-hero-primary {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  padding: 16px 36px;
  background: white;
  color: $primary;
  font-size: $font-size-base;
  font-weight: 800;
  border-radius: $radius-full;
  text-decoration: none;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 8px 32px rgba(0,0,0,0.25);

  &:hover { transform: translateY(-2px); box-shadow: 0 16px 40px rgba(0,0,0,0.35); }
}

.btn-hero-outline {
  display: inline-flex;
  align-items: center;
  padding: 16px 36px;
  border: 2px solid rgba(255,255,255,0.5);
  color: white;
  font-size: $font-size-base;
  font-weight: 600;
  border-radius: $radius-full;
  text-decoration: none;
  transition: background 0.2s, border-color 0.2s;

  &:hover { background: rgba(255,255,255,0.15); border-color: white; }
}

// ── S2: 신뢰 수치 ────────────────────────────────────
.s-trust {
  background: $text-primary;
  padding: $spacing-10 0;

  &__grid {
    display: flex;
    justify-content: space-around;
    flex-wrap: wrap;
    gap: $spacing-8;
  }

  &__item {
    text-align: center;

    strong {
      display: block;
      font-size: clamp($font-size-xl, 3vw, $font-size-3xl);
      font-weight: 800;
      color: white;
    }
    span {
      display: block;
      font-size: $font-size-xs;
      color: rgba(255,255,255,0.5);
      margin-top: 6px;
      letter-spacing: 1px;
    }
  }
}

// ── S3: 기능 3D 커버플로우 ────────────────────────────
.s-features {
  padding: 100px 0;
  position: relative;
  overflow: hidden;
  background: #0a1628 url('/images/features-bg.png') center center / cover no-repeat;

  // 어두운 오버레이 — 텍스트/카드 가독성 확보
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(
      to bottom,
      rgba(10, 22, 40, 0.72) 0%,
      rgba(10, 22, 40, 0.60) 50%,
      rgba(10, 22, 40, 0.75) 100%
    );
    pointer-events: none;
    z-index: 0;
  }

  // 섹션 헤더·탭·스테이지·도트 모두 오버레이 위로
  .section-hd,
  &__tabs,
  &__stage,
  &__dots {
    position: relative;
    z-index: 1;
  }

  // [2026-04-02] 학생용/선생님용 탭
  &__tabs {
    display: flex;
    justify-content: center;
    gap: $spacing-3;
    margin-top: $spacing-8;
  }

  // 헤더 텍스트 색상 오버라이드
  .section-hd {
    .section-hd__tag {
      background: rgba(255, 255, 255, 0.12);
      color: rgba(255, 255, 255, 0.9);
    }
    h2 { color: white; }
    p  { color: rgba(255, 255, 255, 0.65); }
  }

  // 스테이지: perspective 컨테이너 + 화살표 기준
  &__stage {
    position: relative;
    margin-top: 56px;
  }

  // 3D 씬 - 카드들이 모이는 공간
  &__coverflow {
    position: relative;
    height: 460px;
    perspective: 1400px;
    perspective-origin: 50% 50%;

    @media (max-width: $bp-tablet) {
      height: 400px;
    }
  }

  // 개별 카드 — position:absolute, transform으로 3D 배치
  &__cfcard {
    position: absolute;
    left: 50%;
    top: 50%;
    width: 300px;
    padding: 40px 28px 36px;
    border-radius: 24px;
    cursor: pointer;
    background: $bg-light;
    border: 2px solid transparent;
    transition:
      transform 0.55s cubic-bezier(0.4, 0, 0.2, 1),
      opacity   0.55s cubic-bezier(0.4, 0, 0.2, 1),
      box-shadow 0.3s;
    backface-visibility: hidden;
    text-align: center;

    @media (max-width: $bp-tablet) {
      width: 260px;
      padding: 32px 20px 28px;
    }

    &.is-active {
      background: linear-gradient(145deg, #0f172a 0%, #1e3a8a 50%, #2563eb 100%);
      border-color: rgba(99,179,250,0.25);
      box-shadow: 0 24px 64px rgba(37, 99, 235, 0.45);
    }

    &.is-side {
      background: white;
      border-color: $border;
      box-shadow: 0 8px 32px rgba(0,0,0,0.10);
    }

    &.is-far {
      pointer-events: none;
    }

    &:not(.is-active):hover {
      border-color: $primary;
    }
  }

  &__arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    z-index: 20;
    width: 46px;
    height: 46px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.25);
    box-shadow: 0 4px 16px rgba(0,0,0,0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: white;
    transition: all 0.2s;

    &:hover { background: $primary; color: white; border-color: $primary; }

    &--prev { left: 12px; }
    &--next { right: 12px; }
  }

  &__dots {
    display: flex;
    justify-content: center;
    gap: $spacing-2;
    margin-top: $spacing-6;
  }

  &__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.35);
    border: none;
    cursor: pointer;
    transition: all 0.3s;

    &.active {
      width: 28px;
      border-radius: 4px;
      background: $primary;
    }
  }
}

// 커버플로우 카드 내부 요소
.cfcard {
  &__glow {
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: radial-gradient(ellipse at 50% 0%, rgba(99,179,250,0.18) 0%, transparent 70%);
    pointer-events: none;

    .is-active & {
      background: radial-gradient(ellipse at 50% 0%, rgba(255,255,255,0.12) 0%, transparent 60%);
    }
  }

  &__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72px;
    height: 72px;
    border-radius: 50%;
    margin: 0 auto 20px;
    background: rgba(59,130,246,0.12);

    .is-active & {
      background: rgba(255,255,255,0.18);
    }
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;

    svg { display: block; }
    .is-active & svg { stroke: white; }
    :not(.is-active) & svg { stroke: $primary; }
  }

  &__num {
    font-size: 11px;
    font-weight: 800;
    letter-spacing: 3px;
    color: $text-muted;
    margin-bottom: 12px;

    .is-active & { color: rgba(255,255,255,0.35); }
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: 800;
    color: $text-primary;
    margin-bottom: 12px;
    line-height: 1.3;

    .is-active & { color: white; }
  }

  &__desc {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: 1.75;

    .is-active & { color: rgba(255,255,255,0.78); }
  }
}

// ── S4: 커리큘럼 ────────────────────────────────────
.s-curriculum {
  padding: 100px 0;
  position: relative;
  background: #1a1a0e url('/images/curriculum-bg.png') center center / cover no-repeat;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(
      to bottom,
      rgba(15, 20, 10, 0.68) 0%,
      rgba(15, 20, 10, 0.55) 50%,
      rgba(15, 20, 10, 0.70) 100%
    );
    pointer-events: none;
    z-index: 0;
  }

  .lp-container {
    position: relative;
    z-index: 1;
  }

  .section-hd {
    .section-hd__tag {
      background: rgba(255, 255, 255, 0.12);
      color: rgba(255, 255, 255, 0.9);
    }
    h2 { color: white; }
    p  { color: rgba(255, 255, 255, 0.65); }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $spacing-6;

    @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }
  }

  &__card {
    background: white;
    border-radius: $radius-xl;
    overflow: hidden;
    box-shadow: $shadow-sm;
    transition: box-shadow 0.3s, transform 0.3s;

    &:hover { box-shadow: $shadow-lg; transform: translateY(-6px); }
  }

  &__card-body {
    padding: $spacing-6;

    ul {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: $spacing-2;
      margin-top: $spacing-4;

      li {
        font-size: $font-size-base;
        color: $text-secondary;
        display: flex;
        align-items: center;
        gap: $spacing-2;

        .check { font-weight: 700; flex-shrink: 0; }
      }
    }
  }

  &__card-level {
    font-size: 13px;
    font-weight: 800;
    letter-spacing: 2px;
    text-transform: uppercase;
    margin-bottom: $spacing-2;
  }

  h3 {
    font-size: $font-size-2xl;
    font-weight: 800;
    color: $text-primary;
    margin-bottom: $spacing-2;
  }

  &__card-target {
    display: inline-block;
    background: $bg-light;
    font-size: 13px;
    font-weight: 600;
    padding: 4px 12px;
    border-radius: $radius-full;
    color: $text-muted;
    margin-bottom: $spacing-3;
  }

  &__card-desc {
    font-size: $font-size-base;
    color: $text-secondary;
    line-height: 1.7;
  }
}

.curriculum-svg-wrap {
  display: block;
  font-size: 0;
  line-height: 0;
  margin: 0;
  padding: 0;
  height: 200px;
  overflow: hidden;

  :deep(svg) {
    display: block !important;
    width: 100% !important;
    height: 200px !important;
    margin: 0 !important;
    padding: 0 !important;
    vertical-align: top !important;
  }
}

// ── S5: 선생님 소개 ──────────────────────────────────
.s-teachers {
  padding: 100px 0;
  background: linear-gradient(160deg, #0f172a 0%, #1e3a8a 100%);

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $spacing-6;

    @media (max-width: $bp-tablet) { grid-template-columns: 1fr 1fr; }
    @media (max-width: $bp-mobile) { grid-template-columns: 1fr; }
  }
}

.teacher-photo-wrap {
  height: 220px;
  overflow: hidden;
  border-radius: $radius-lg $radius-lg 0 0;
}

.teacher-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center top;
  transition: transform 0.4s ease;

  .teacher-card:hover & {
    transform: scale(1.05);
  }
}

.teacher-card {
  border-radius: $radius-lg;
  overflow: hidden;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.1);
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 20px 40px rgba(0,0,0,0.3);
  }

  &__info {
    padding: $spacing-5;
  }

  &__name {
    display: block;
    font-size: $font-size-xl;
    font-weight: 800;
    color: white;
    margin-bottom: 6px;
  }

  &__subject {
    display: block;
    font-size: $font-size-sm;
    color: #60a5fa;
    font-weight: 600;
    margin-bottom: $spacing-2;
  }

  &__career {
    font-size: $font-size-sm;
    color: rgba(255,255,255,0.5);
    margin-bottom: $spacing-3;
    line-height: 1.6;
  }

  &__quote {
    font-size: $font-size-base;
    color: rgba(255,255,255,0.75);
    line-height: 1.7;
    font-style: italic;
    border-left: 3px solid #3b82f6;
    padding-left: $spacing-3;
    margin: 0;
  }
}

// ── S6: 브랜드 두 섹션 ──────────────────────────────
.s-brand {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 560px;

  @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }

  &__half {
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: flex-end;
    min-height: 480px;

    &-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.3) 60%, transparent 100%);
      z-index: 1;
    }

    &-content {
      position: relative;
      z-index: 2;
      padding: 48px;
      color: white;

      @media (max-width: $bp-mobile) { padding: $spacing-8; }
    }
  }
}

.brand-bg-placeholder {
  position: absolute;
  inset: 0;
  background: #1e3a8a url('/images/student-bg.png') center center / cover no-repeat;

  &--teacher {
    background: #064e3b url('/images/teacher-bg.png') center center / cover no-repeat;
  }
}

.s-brand__half-tag {
  display: inline-block;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255,255,255,0.85);
  margin-bottom: $spacing-3;
}

.s-brand__half-content {
  h3 {
    font-size: clamp(24px, 3vw, 36px);
    font-weight: 800;
    line-height: 1.25;
    margin-bottom: $spacing-4;
  }

  p {
    font-size: $font-size-sm;
    color: rgba(255,255,255,0.75);
    line-height: 1.8;
    margin-bottom: $spacing-6;
  }
}

.btn-brand {
  display: inline-flex;
  align-items: center;
  padding: 12px 28px;
  background: white;
  color: $text-primary;
  font-size: $font-size-sm;
  font-weight: 700;
  border-radius: $radius-full;
  text-decoration: none;
  transition: transform 0.2s;

  &:hover { transform: translateX(4px); }
}

// ── S7: 후기 ────────────────────────────────────────
.s-reviews {
  padding: 100px 0;
  position: relative;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 45%, #1d4ed8 100%);
  overflow: hidden;

  // 배경 장식 원들
  &::before {
    content: '';
    position: absolute;
    width: 700px; height: 700px;
    border-radius: 50%;
    top: -200px; right: -200px;
    background: radial-gradient(circle, rgba(99,179,250,0.12) 0%, transparent 70%);
    pointer-events: none;
  }
  &::after {
    content: '';
    position: absolute;
    width: 500px; height: 500px;
    border-radius: 50%;
    bottom: -150px; left: -150px;
    background: radial-gradient(circle, rgba(52,211,153,0.10) 0%, transparent 70%);
    pointer-events: none;
  }

  .lp-container {
    position: relative;
    z-index: 1;
  }

  // 헤더 색상 오버라이드
  .section-hd {
    .section-hd__tag {
      background: rgba(255,255,255,0.12);
      color: rgba(255,255,255,0.9);
    }
    h2 { color: white; }
    p  { color: rgba(255,255,255,0.65); }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-6;

    @media (max-width: $bp-tablet) { grid-template-columns: 1fr; }
  }
}

.review-thumb-placeholder {
  flex-shrink: 0;
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.review-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.review-card {
  background: rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(12px);
  border-radius: $radius-xl;
  padding: $spacing-8;
  border: 1px solid rgba(255, 255, 255, 0.14);
  display: flex;
  gap: $spacing-5;
  transition: box-shadow 0.3s, transform 0.3s, background 0.3s;

  &:hover {
    background: rgba(255, 255, 255, 0.12);
    box-shadow: 0 16px 48px rgba(0,0,0,0.3);
    transform: translateY(-4px);
  }

  &__body { flex: 1; }

  &__stars { color: #FBBF24; font-size: 16px; letter-spacing: 2px; margin-bottom: $spacing-3; }

  &__text {
    font-size: $font-size-sm;
    color: rgba(255, 255, 255, 0.85);
    line-height: 1.85;
    margin-bottom: $spacing-4;
    font-style: italic;
  }

  &__author {
    strong { display: block; font-size: $font-size-sm; font-weight: 700; color: white; }
    span { display: block; font-size: $font-size-xs; color: rgba(255,255,255,0.5); margin-top: 2px; }
  }

  &__result {
    margin-top: $spacing-3;

    &-badge {
      display: inline-block;
      background: rgba(96,165,250,0.18);
      color: #93c5fd;
      font-size: 11px;
      font-weight: 700;
      padding: 4px 10px;
      border-radius: $radius-full;
    }
  }
}

// ── S8: 이용 가이드 ──────────────────────────────────
.s-guide {
  padding: 100px 0;
  position: relative;
  background: #f8fafc url('/images/guide-bg.png') center center / cover no-repeat;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: rgba(255, 255, 255, 0.38);
    pointer-events: none;
    z-index: 0;
  }

  .section-hd {
    h2 { color: $text-primary; }
    p  { color: $text-primary; font-weight: 600; }
  }

  .lp-container {
    position: relative;
    z-index: 1;
  }
}

// [2026-04-02] 기능 소개 탭 (다크 배경용)
.feat-tab {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-6;
  font-size: $font-size-base;
  font-weight: 700;
  color: rgba(255,255,255,0.5);
  border: 1.5px solid rgba(255,255,255,0.15);
  border-radius: $radius-full;
  transition: all 0.2s;

  svg { opacity: 0.6; transition: opacity 0.2s; }

  &.active {
    background: $primary;
    color: white;
    border-color: $primary;
    box-shadow: 0 4px 16px rgba(59,130,246,0.45);

    svg { opacity: 1; }
  }
  &:hover:not(.active) {
    color: rgba(255,255,255,0.8);
    border-color: rgba(255,255,255,0.3);
    svg { opacity: 0.8; }
  }
}

.guide-tabs {
  display: inline-flex;
  gap: $spacing-3;
  margin-bottom: $spacing-10;
  background: white;
  border-radius: $radius-xl;
  padding: $spacing-2;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}

.guide-tab {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3 $spacing-6;
  font-size: $font-size-lg;
  font-weight: 700;
  color: $text-muted;
  border-radius: $radius-lg;
  transition: all 0.2s;

  svg { width: 22px; height: 22px; }

  &.active {
    background: $primary;
    color: white;
    box-shadow: 0 4px 12px rgba(37,99,235,0.35);
  }
  &:hover:not(.active) { color: $text-secondary; background: $bg-light; }
}

.guide-steps {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-6;

  @media (max-width: $bp-tablet) {
    grid-template-columns: 1fr;
  }
}

.guide-step {
  flex: 1;
  background: $bg-light;
  border-radius: $radius-xl;
  overflow: hidden;
  border: 1px solid $border;
  transition: box-shadow 0.2s;

  &:hover { box-shadow: $shadow-md; }

  &__illust {
    width: 100%;
    padding: $spacing-4 $spacing-4 0;
    border-bottom: 1px solid rgba(0,0,0,0.04);

    svg {
      max-height: 220px;
    }
  }

  &__num {
    padding: $spacing-6 $spacing-6 0;
    font-size: 28px;
    font-weight: 900;
    color: $primary;
    opacity: 0.25;
    line-height: 1;
    margin-bottom: $spacing-3;
  }

  &__body {
    padding: 0 $spacing-6 $spacing-6;

    h4 {
      font-size: $font-size-xl;
      font-weight: 700;
      color: $text-primary;
      margin-bottom: $spacing-3;
    }
    p {
      font-size: $font-size-base;
      color: $text-secondary;
      line-height: 1.8;
    }
  }

  &__arrow {
    display: none;
  }
}

// ── S9: FAQ ──────────────────────────────────────────
.s-faq {
  padding: 100px 0;
  position: relative;
  background: linear-gradient(160deg, #0f172a 0%, #1e3a8a 50%, #0f2460 100%);
  overflow: hidden;

  // 배경 장식
  &::before {
    content: '';
    position: absolute;
    width: 600px; height: 600px;
    border-radius: 50%;
    top: -200px; left: -150px;
    background: radial-gradient(circle, rgba(99,179,250,0.10) 0%, transparent 70%);
    pointer-events: none;
  }
  &::after {
    content: '';
    position: absolute;
    width: 400px; height: 400px;
    border-radius: 50%;
    bottom: -100px; right: -100px;
    background: radial-gradient(circle, rgba(52,211,153,0.08) 0%, transparent 70%);
    pointer-events: none;
  }

  .lp-container { position: relative; z-index: 1; }

  // 헤더 색상
  .section-hd {
    .section-hd__tag {
      background: rgba(255,255,255,0.12);
      color: rgba(255,255,255,0.9);
    }
    h2 { color: white; }
  }

  &__list { max-width: 760px; margin: 0 auto; }
}

.faq-item {
  background: rgba(255,255,255,0.07);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.13);
  border-radius: $radius-lg;
  margin-bottom: $spacing-3;
  overflow: hidden;
  cursor: pointer;
  transition: background 0.2s, box-shadow 0.2s;

  &:hover { background: rgba(255,255,255,0.12); }
  &.open { border-color: rgba(99,179,250,0.45); }

  &__q {
    display: flex;
    align-items: center;
    gap: $spacing-4;
    padding: $spacing-5 $spacing-6;
    font-size: $font-size-base;
    font-weight: 600;
    color: white;
  }

  &__qmark {
    flex-shrink: 0;
    width: 28px; height: 28px;
    background: rgba(59,130,246,0.7);
    color: white;
    border-radius: $radius-sm;
    display: flex; align-items: center; justify-content: center;
    font-weight: 800; font-size: $font-size-sm;
  }

  &__arrow {
    margin-left: auto;
    transition: transform 0.3s;
    color: rgba(255,255,255,0.45);
    .open & { transform: rotate(180deg); }
  }

  &__a {
    display: flex;
    gap: $spacing-4;
    padding: 0 $spacing-6 $spacing-5;
    font-size: $font-size-sm;
    color: rgba(255,255,255,0.70);
    line-height: 1.8;
  }

  &__amark {
    flex-shrink: 0;
    width: 28px; height: 28px;
    background: rgba(16,185,129,0.7);
    color: white;
    border-radius: $radius-sm;
    display: flex; align-items: center; justify-content: center;
    font-weight: 800; font-size: $font-size-sm;
  }
}

// ── S10: 가격 플랜 ────────────────────────────────────
.s-pricing {
  padding: 100px 0;
  background: #f8fafc;

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 28px;
    align-items: stretch;
    margin-bottom: 36px;

    @media (max-width: 900px) {
      grid-template-columns: 1fr;
      max-width: 440px;
      margin-left: auto;
      margin-right: auto;
    }
  }

  &__note {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: $font-size-sm;
    color: #64748b;
    svg { color: #3b82f6; flex-shrink: 0; }
  }
}

.price-card {
  position: relative;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 20px;
  padding: 36px 32px;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s, transform 0.2s;

  &:hover {
    box-shadow: 0 12px 40px rgba(0,0,0,0.10);
    transform: translateY(-4px);
  }

  &--featured {
    border-color: #3b82f6;
    background: linear-gradient(160deg, #eff6ff 0%, #ffffff 50%);
    box-shadow: 0 8px 32px rgba(59,130,246,0.18);
    transform: translateY(-8px);

    &:hover { transform: translateY(-12px); }
  }

  &__badge {
    position: absolute;
    top: -14px;
    left: 50%;
    transform: translateX(-50%);
    background: linear-gradient(90deg, #3b82f6, #6366f1);
    color: white;
    font-size: 12px;
    font-weight: 700;
    padding: 4px 18px;
    border-radius: 999px;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }

  &__header {
    margin-bottom: 28px;
  }

  &__name {
    font-size: $font-size-lg;
    font-weight: 800;
    color: #1e293b;
    margin-bottom: 12px;
  }

  &__price {
    margin-bottom: 8px;
    display: flex;
    align-items: baseline;
    gap: 4px;
  }

  &__amount {
    font-size: 40px;
    font-weight: 900;
    color: #1e293b;
    line-height: 1;
  }

  &__unit {
    font-size: $font-size-sm;
    color: #64748b;
  }

  &__desc {
    font-size: $font-size-sm;
    color: #64748b;
  }

  &__features {
    list-style: none;
    padding: 0;
    margin: 0 0 32px;
    flex: 1;

    li {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: $font-size-sm;
      padding: 9px 0;
      border-bottom: 1px solid #f1f5f9;

      &:last-child { border-bottom: none; }

      &.on  { color: #1e293b; svg { color: #10b981; } }
      &.off { color: #cbd5e1; svg { color: #cbd5e1; } text-decoration: line-through; }
    }
  }

  &__btn {
    display: block;
    text-align: center;
    padding: 14px;
    border-radius: 12px;
    font-weight: 700;
    font-size: $font-size-base;
    transition: all 0.2s;
    background: #f1f5f9;
    color: #475569;
    text-decoration: none;

    &:hover { background: #e2e8f0; }

    &--featured {
      background: linear-gradient(90deg, #3b82f6, #6366f1);
      color: white;
      box-shadow: 0 4px 16px rgba(59,130,246,0.35);

      &:hover {
        box-shadow: 0 6px 20px rgba(59,130,246,0.45);
        transform: translateY(-1px);
      }
    }
  }
}

// ── CTA ──────────────────────────────────────────────
.s-cta {
  position: relative;
  padding: 120px 0;
  overflow: hidden;

  &__bg {
    position: absolute;
    inset: 0;
    background: #1e40af url('/images/cta-bg.png') center center / cover no-repeat;

    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(to bottom, rgba(0,0,0,0.55) 0%, rgba(0,0,0,0.40) 50%, rgba(0,0,0,0.60) 100%);
    }
  }

  &__inner {
    position: relative;
    z-index: 1;
    text-align: center;

    h2 { font-size: clamp(28px, 4vw, 44px); font-weight: 800; color: white; margin-bottom: $spacing-4; }
    p { font-size: $font-size-lg; color: rgba(255,255,255,0.8); margin-bottom: $spacing-10; }
  }
}

.btn-cta {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  padding: 18px 48px;
  background: white;
  color: $primary;
  font-size: $font-size-lg;
  font-weight: 800;
  border-radius: $radius-full;
  text-decoration: none;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover { transform: translateY(-3px); box-shadow: 0 16px 48px rgba(0,0,0,0.3); }
}

</style>
