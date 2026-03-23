# PROJECT_CONTEXT.md
AI 기반 수학 교육 플랫폼 — 구현 현황 및 프로젝트 컨텍스트

> **Claude에게:** 이 파일 하나로 프로젝트 전체 맥락을 파악할 수 있도록 작성되었습니다.
> 새 세션 시작 시 이 파일을 첨부하면 이전 작업 내용을 바로 이어받을 수 있습니다.

---

## ⚡ 이번 세션 작업
<!-- 세션 시작 시 현재 작업 내용 1~3줄 기록. 세션 종료 시 삭제 -->

---

## 기술 스택 및 구조

| 항목 | 내용 |
|------|------|
| Backend | Spring Boot 3.2.3 + MyBatis + JWT / `http://localhost:7000/api` |
| Frontend | Vue 3 + Vite + Pinia / `http://localhost:7001` |
| DB | MySQL 8.0 / `211.171.152.242:3310/edu_platform` (root/root1234) |
| 사용자 역할 | STUDENT / TEACHER(=SUPER_USER) / ADMIN |
| 라우팅 | STUDENT → `/student/*` / TEACHER → `/teacher/*` / ADMIN → `/admin/*` |
| Backend 레이어 | Controller → Service → Mapper(MyBatis XML) → DB |
| API 응답 | 모든 응답 `ApiResponse<T>` 래퍼 통일 |
| 예외 처리 | `BusinessException(ErrorCode)` 사용 |
| 파일 경로 | Controller: `controller/` / Service: `service/` / Mapper XML: `resources/mapper/` / DTO: `dto/{역할}/{기능}/` |
| Frontend 경로 | Pages: `pages/{student,teacher,admin,auth,common}/` / Store: `store/` / Layout: `DefaultLayout.vue`(인증후) `AuthLayout.vue`(로그인) |
| MyBatis | snake_case → camelCase 자동변환 (`map-underscore-to-camel-case: true`) / XML·Interface 쌍으로 생성 |

### 학생 등급 체계
| 등급 | 정답률 기준 | 문제 배정 |
|------|------|------|
| A (상) | 약 80% 이상 | 심화·응용 위주 |
| B (중) | 약 50~79% | 표준 난이도 위주 |
| C (하) | 약 49% 이하 | 기본 개념 + 풀이 영상 우선 노출 |

### 도메인 용어 (혼동 주의)
| 용어 | 정의 | 주의 |
|------|------|------|
| 과제 (Assignment) | 교사가 학생에게 출제하는 문제 묶음 | ≠ 세션 |
| 세션 (Session) | 학생이 과제를 풀기 시작한 단위 | start → submit → complete |
| 등급 (Level) | 학습 수준 A/B/C | ≠ 학년(Grade: 중1/2/3) |
| 오답노트 (WrongNote) | v6 기준 오답 자동저장 기능 | 북마크를 대체. 신규 기능에 북마크 개념 사용 금지 |
| 북마크 (Bookmark) | 구버전 개념. v6부터 오답노트로 대체 | API는 유지, UI 노출 금지 |
| Super User | 학교 교사. TEACHER 역할과 동일 | ≠ 관리자(Admin). 학원강사 아님 |

### 학습 세션 흐름
```
POST /student/sessions/start
→ GET  /student/sessions/{id}/problems
→ POST /student/sessions/{id}/submit
→ POST /student/sessions/{id}/complete
```

---

## 테스트 계정 (비밀번호 공통: Test@1234)

| 역할 | 이메일 |
|------|------|
| 학생 | student_a@test.com |
| 교사 | teacher@test.com |
| 관리자 | admin@test.com |

---

## 환경변수 현황

| 변수 | 상태 | 비고 |
|------|------|------|
| `JWT_SECRET` | 기본값 사용 중 | 프로덕션 전 변경 필요 |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | 미설정 | 이메일 기능 동작 안 함 |
| `AWS_S3_BUCKET` / `AWS_REGION` | 미설정 | S3 연동 안 됨 |
| `CLOUDFRONT_DOMAIN` | 미설정 | CDN 미사용 |
| `UPLOAD_PATH` | 기본값(`uploads/`) | 로컬 업로드는 동작 |

---

## 트러블슈팅 패턴

| 증상 | 원인 | 해결 |
|------|------|------|
| 새 API 호출 시 `NoResourceFoundException` | Controller/Service 추가 후 백엔드 미재시작 | `taskkill //F //PID {PID}` 후 `./gradlew bootRun` |
| 포트 7000 이미 사용 중 | kill 후에도 포트 점유 지속 | `netstat -ano \| findstr :7000` 으로 PID 확인 후 kill |

---

## 🚨 블로킹 이슈 — 클라이언트 확정 필요

| 이슈 | 내용 | 영향 범위 |
|------|------|------|
| 교사-학생 매칭 방식 | ① 교사 주도(초대코드) vs ② 학생 주도(검색 신청) 확정 필요 | 회원가입 플로우, 학급관리, 과제배정 전체 |
| DRM / 콘텐츠 보안 수준 | MVP: Signed URL / Phase2: 화면캡처방지 / Phase3: Widevine DRM | 동영상 재생, 문제 콘텐츠 보호 |
| 교사 리워드 법적 검토 | 김영란법 저촉 여부 법무 검토 필수 | 리워드 시스템 전체 |

---

## 구현 완료 기능

### 인증
- [x] 이메일/비밀번호 로그인·회원가입, JWT Access/Refresh Token, 로그아웃
- [x] 비밀번호 찾기 UI, OAuth2 엔드포인트 라우팅, 내 정보 조회 (`GET /auth/me`)

### 설정
- [x] 프로필 조회/수정, 비밀번호 변경
- [x] 알림 설정 저장 — `users` 테이블 noti 컬럼 3개 추가 (`database/notification_settings.sql`)

### 학생
- [x] 홈 대시보드, 진단 테스트 (20문항 → A/B/C 등급 배정)
- [x] 학습 세션 시작/진행/완료, 문항별 답 제출
- [x] 오답노트 (자동저장·목록·삭제·해결토글·재도전·영상바로보기)
- [x] KaTeX 수식 렌더링 (MathText.vue)
- [x] 학습 리포트 차트 (정답률 추이·일별 풀이량·월별 비교)
- [x] 문항별 풀이 소요시간 기록, 정답/오답 즉시 피드백 토스트
- [x] 학습 리포트·과제 피드백·동영상·AI 코멘트·학습 이력 조회
- [x] 북마크 API 유지 (UI 노출 제거, 오답노트로 대체됨)
- [x] 마감임박 카드 강조 — ProblemListPage.vue. 미완료 과제만 적용: 24h 이내 → "마감임박" 뱃지 + 빨간 테두리 / 기간 초과 → "기간 초과" 뱃지 + 진빨강 배경. 완료 과제는 항상 일반 스타일

### 교사
- [x] 홈 대시보드, 학생 목록·상세·레벨 변경
- [x] 과제 생성·목록·상세·제출 현황·피드백 작성
- [x] 학습 현황 분석, 미완료 학생 목록
- [x] 문제은행 탐색 (`/teacher/problems` — 필터·검색·모달·과제담기)
- [x] 학급 관리 (`/teacher/classes` — CRUD·학생 추가/제거)
- [x] 과제 수정 (제목·설명·마감일) — AssignmentDetailPage.vue 수정 모달
- [x] 과제 삭제 — 제출 이력 없음 시 하드 삭제 / 있음 시 소프트 삭제(is_deleted=1). 학생·교사 목록 자동 제외

### 관리자
- [x] 대시보드, 회원·학교 관리, 결제 목록(조회만), 시스템 분석
- [x] 1:1 문의, 동영상 관리, 시스템 코드 관리
- [x] 문제 DB 검색·생성·수정·삭제·배치 업로드 (`POST /problems/upload/batch`)
- [x] 문제 검수/승인 워크플로우 — 검수 대기 배너, PENDING/APPROVED/REJECTED 상태 배지, 승인/반려/재승인 버튼, 반려 사유 모달. 승인된 문제만 학생·교사에 노출 (is_active 연동)

### 공통
- [x] 공지사항 CRUD, 1:1 문의, 알림 (목록·읽음·카운트)
- [x] 문제 검색·상세, 관련 동영상 추천

---

## 미구현 기능 (TODO)

### 인증
| 기능 | 위치 | 비고 |
|------|------|------|
| 비밀번호 재설정 이메일 발송 | `AuthService.java:209` | 골격만 존재 |
| 이메일 인증 토큰 생성/발송/검증 | `AuthService.java:229` | 골격만 존재 |
| 소셜 로그인 실제 연동 (Google/Kakao) | `build.gradle:32` | OAuth2 의존성 주석처리됨 |

### 학생 — 문제풀이
| 기능 | 위치 | 비고 |
|------|------|------|
| 오답 시 관련 풀이 영상 연동 | - | 해설 하단 [영상으로 보기] 버튼 |
| 풀이 임시저장 (이어풀기) | - | 중간 이탈 시 마지막 위치 저장. ProblemListPage.vue 미완료/완료 탭은 구현됨 |
| '이해했어요 / 아직 모르겠어요' 피드백 버튼 | - | 학습 데이터 수집용 |
| 문제 데이터 품질 개선 | - | 실제 기출 데이터 입력 시 해결 예정 |

### 학생 — 리포트
| 기능 | 비고 |
|------|------|
| 월별 학습 달력 | 날짜별 도트, 정답률에 따라 색상 표시 |

### 학생 — 기타
| 기능 | 위치 | 비고 |
|------|------|------|
| 영상 시청 이력 저장 | VideoController.java:45 | 전체 조회수 카운터(incrementViewCount)만 존재. user_id+video_id 매핑 테이블 및 Controller/Service/Mapper 미구현 |
| 오답노트 → 영상 이력 연동 | WrongNotesPage.vue:180-182 | goToVideo()가 라우팅만 함. 이력 저장 API 호출 없음 |

### 교사 — 과제
| 기능 | 위치 | 비고 |
|------|------|------|
| 등급별 자동 분리 배정 | `TeacherService.java` | A문제→A학생 자동 분리 로직 |
| 배정 완료 시 학생 인앱 알림 | `TeacherService.java:142` | 알림 로직 미구현 |
| 과제 복사(재사용) 버튼 | - | 신규 |
| 미완료 학생 개별 독려 알림 | - | 과제 상세 화면 |

### 교사 — 학급 관리
| 기능 | 비고 |
|------|------|
| 학급별 과제 일괄 배정 | 학급 단위 과제 배정 |
| 학급별 리포트 조회 | 학급 단위 성적 현황 |
| 학생 초대 (초대코드 / 이메일 / CSV) | 매칭 방식 클라이언트 확정 후 착수 |
| 매칭 해제 및 재배정 | 기존 학습 데이터 보관 |

### 교사 — 리포트
| 기능 | 비고 |
|------|------|
| CSV 내보내기 | 학급 전체 성적 테이블 |
| 등급별 분포 차트 | A/B/C 파이+바차트, 월별 비교 |
| 취약 단원별 학생 분석 | 하위 학생 리스트 + 보충 배정 연결 |

### 어드민
| 기능 | 위치 | 비고 |
|------|------|------|
| 교사 DB 기여 리워드 관리 | - | 법적 검토 후 착수 |
| 비밀번호 초기화 이메일 발송 | `AdminService.java:109` | 미발송 |

### 공통 — 레이아웃
| 기능 | 위치 | 비고 |
|------|------|------|
| 모바일 하단 탭바 (학생) | DefaultLayout.vue / AppSidebar.vue | 360~767px 구간 Bottom Navigation Bar 미구현. 현재 slide-in 사이드바 방식만 존재 |

### 설정 / 기타
| 기능 | 비고 |
|------|------|
| 회원 탈퇴 | 사유 선택 + 확인 모달 |
| 구독/이용권 현황 확인 | 플랜명·결제일·만료일 표시 |
| 동영상 풀이 요청 | `VideoController.java:61` 서비스 미구현 |
| 영상 시청 이력 조회 | 조회수 카운트는 있으나 이력 미구현 |
| 외부 URL 임베드 (YouTube/Vimeo) | MVP 권장 방식 |
| 구독/결제 PG 연동 | `subscriptions` 테이블 존재, PG 미연동 |

---

## 프론트엔드 페이지 현황

| 역할 | 경로 | 구현 |
|------|------|------|
| 공통 | `/` (랜딩) | 완료 (고도화 필요) |
| 공통 | `/login`, `/register`, `/password/forgot` | 완료 |
| 공통 | `/settings`, `/announcements`, `/notices` | 완료 |
| 학생 | `/student/home` | 완료 |
| 학생 | `/student/diagnosis` | 완료 |
| 학생 | `/student/learn`, `/student/learn/:sessionId` | 완료 (고도화 필요) |
| 학생 | `/student/videos`, `/student/videos/:id` | 완료 |
| 학생 | `/student/wrong-notes` | 완료 |
| 학생 | `/student/report` | 완료 (월별 달력 미구현) |
| 학생 | `/student/assignments/:id/feedback` | 완료 |
| 교사 | `/teacher/home` | 완료 (고도화 필요) |
| 교사 | `/teacher/students`, `/teacher/students/:id` | 완료 |
| 교사 | `/teacher/assignments`, `/teacher/assignments/create`, `/teacher/assignments/:id` | 완료 |
| 교사 | `/teacher/analytics` | 완료 |
| 교사 | `/teacher/problems` | 완료 |
| 교사 | `/teacher/classes` | 완료 |
| 관리자 | `/admin/dashboard` | 완료 |
| 관리자 | `/admin/members`, `/admin/schools` | 완료 |
| 관리자 | `/admin/problems`, `/admin/problems/upload` | 완료 |
| 관리자 | `/admin/videos`, `/admin/payments` | 완료 |
| 관리자 | `/admin/analytics`, `/admin/codes`, `/admin/inquiries` | 완료 |
