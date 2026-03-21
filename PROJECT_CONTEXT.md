# PROJECT_CONTEXT.md
AI 기반 수학 교육 플랫폼 — 구현 현황 및 프로젝트 컨텍스트

> **Claude에게:** 이 파일 하나로 프로젝트 전체 맥락을 파악할 수 있도록 작성되었습니다.
> 새 세션 시작 시 이 파일을 첨부하면 이전 작업 내용을 바로 이어받을 수 있습니다.

---

## 작업 단위 원칙

> **Claude Code CLI는 한 세션에서 한 작업 단위만 처리한다.**
> 컨텍스트 오염과 토큰 낭비를 방지하기 위한 핵심 규칙.

### 작업 쪼개기 기준

| 나쁜 예 (너무 큰 단위) | 좋은 예 (적절한 단위) |
|------|------|
| "오답노트 기능 전체 만들어줘" | "WrongNoteController, Service, Mapper 백엔드만 먼저" |
| "교사 학급관리 다 구현해줘" | "classes 테이블 기반으로 ClassController, Service 생성" |
| "문제풀이 화면 고도화해줘" | "LaTeX 수식 렌더링 KaTeX 적용만 먼저" |
| "리포트 페이지 개선해줘" | "등급 변화 타임라인 꺾은선 차트 컴포넌트 추가" |

### 기능별 권장 작업 순서
새 기능 구현 시 아래 순서로 세션을 분리할 것.

```
1단계: 백엔드 — Controller + Service + Mapper + DTO
2단계: 백엔드 — API 연결 확인 및 테스트
3단계: 프론트 — Vue 페이지/컴포넌트 구현
4단계: 프론트 — 백엔드 API 연결 및 동작 확인
5단계: PROJECT_CONTEXT.md 업데이트 → /clear
```

---

## 시스템 개요

| 항목 | 내용 |
|------|------|
| 백엔드 | Spring Boot 3.2.3 + MyBatis + JWT |
| 프론트엔드 | Vue 3 + Vite + Pinia |
| DB | MySQL 8.0 (`211.171.152.242:3310/edu_platform`, root/root1234) |
| 사용자 역할 | STUDENT / TEACHER / SUPER_USER / ADMIN |
| 포트 | Backend: 7000, Frontend: 7001 |
| API Base | `http://localhost:7000/api` |
| Frontend | `http://localhost:7001` |

---

## 개발 환경 및 명령어

### Backend (Java 17 필수)
```bash
cd backend
./gradlew bootRun          # 개발 서버 실행
./gradlew build            # 빌드
./gradlew test             # 전체 테스트
./gradlew test --tests "com.edu.platform.SomeTest"  # 단일 테스트
```

### Frontend
```bash
cd frontend
npm install                # 의존성 설치
npm run dev                # 개발 서버 (port 7001)
npm run build              # 프로덕션 빌드
npm run preview            # 빌드 결과물 미리보기
```

### Database
```bash
mysql -h 211.171.152.242 -P 3310 -u root -p edu_platform < database/schema.sql
mysql -h 211.171.152.242 -P 3310 -u root -p edu_platform < database/seed.sql
# 증분 적용
mysql -h 211.171.152.242 -P 3310 -u root -p edu_platform < database/wrong_notes.sql
mysql -h 211.171.152.242 -P 3310 -u root -p edu_platform < database/single_session.sql
```

---

## 아키텍처

### 사용자 역할 및 라우팅
- `STUDENT` → `/student/*`
- `TEACHER` / `SUPER_USER` → `/teacher/*`
- `ADMIN` → `/admin/*`

라우터 가드(`frontend/src/router/index.js`)에서 역할별 자동 리다이렉트 처리.

### Backend 레이어
```
Controller → Service → Mapper (MyBatis XML) → DB
```
- **Controller:** `backend/src/main/java/com/edu/platform/controller/`
- **Service:** `backend/src/main/java/com/edu/platform/service/`
- **MyBatis Mapper XML:** `backend/src/main/resources/mapper/`
- **Domain/DTO:** `domain/`, `dto/{admin,auth,common,student,teacher}/`

모든 API 응답은 `ApiResponse<T>` 래퍼로 통일. 예외는 `BusinessException(ErrorCode)` 사용.

### Frontend 구조
- **Pages:** `frontend/src/pages/{student,teacher,admin,auth,common}/`
- **Store (Pinia):** `frontend/src/store/` — `auth.js`가 JWT 토큰 및 사용자 상태 관리
- **API 호출:** `frontend/src/composables/` 또는 axios 직접 사용 (`/api` 프록시)
- **레이아웃:** `DefaultLayout.vue` (인증 후), `AuthLayout.vue` (로그인/회원가입)

### 인증 흐름
1. 로그인 → Access Token + Refresh Token 발급
2. Access Token은 `Authorization: Bearer {token}` 헤더로 전송
3. 만료 시 `/auth/refresh`로 갱신
4. `JwtAuthenticationFilter`가 모든 요청에서 토큰 검증

### 학습 세션 흐름
1. `POST /student/sessions/start` → sessionId 발급
2. `GET /student/sessions/{sessionId}/problems` → 문제 목록
3. `POST /student/sessions/{sessionId}/submit` → 문항별 답 제출
4. `POST /student/sessions/{sessionId}/complete` → 세션 종료 및 리포트 생성

### MyBatis 규칙
- XML mapper와 Java Mapper 인터페이스가 쌍으로 존재
- `application.yml`의 `map-underscore-to-camel-case: true` 설정으로 snake_case → camelCase 자동 변환
- SQL 로그는 DEBUG 레벨로 출력됨

---

## 트러블슈팅 패턴

| 증상 | 원인 | 해결 |
|------|------|------|
| 새 API 호출 시 `NoResourceFoundException: No static resource {path}` | Controller/Service 추가 후 백엔드가 재시작되지 않음 | `taskkill //F //PID {PID}` 후 `./gradlew bootRun` 재실행 |
| 포트 7000이 이미 사용 중 | kill 후에도 포트 점유 지속 | `netstat -ano | findstr :7000` 으로 PID 확인 후 `taskkill //F //PID {PID}` |

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

## 요건정의서 v6.0 핵심 정보

> 요건정의서 최종본: `AI_교육_플랫폼_요건정의서_전체_v6.xlsx` (2026-03-18 기준)

### 사용자 레벨 정의
| 레벨 | 설명 |
|------|------|
| 비회원 | 로그인 전 상태. 랜딩/로그인/회원가입만 접근 가능 |
| 학생 | 진단 테스트, 문제 풀이, 오답노트, 성적 조회, 풀이 영상 시청 |
| 학교교사 (Super User) | 학생 관리, 문제 배정, 리포트 조회, 문제 은행 탐색 |
| 운영자 (Admin) | 전체 서비스 운영·관리. 어드민 전체 접근 |

### 학생 등급 체계 (3단계)
| 등급 | 기준 | 문제 배정 |
|------|------|------|
| A (상) | 정답률 약 80% 이상 | 심화·응용 문제 위주 |
| B (중) | 정답률 약 50~79% | 표준 난이도 문제 위주 |
| C (하) | 정답률 약 49% 이하 | 기본 개념 문제 + 풀이 영상 우선 노출 |

---

## 구현 완료 기능

### 인증 (Auth)
- [x] 이메일/비밀번호 로그인 (`POST /auth/login`)
- [x] 회원가입 (`POST /auth/register`)
- [x] JWT Access/Refresh Token 발급 및 갱신
- [x] 로그아웃 (`POST /auth/logout`)
- [x] 비밀번호 찾기 페이지 UI (`/password/forgot`)
- [x] OAuth2 엔드포인트 라우팅 (`/auth/oauth2/login/{provider}`)
- [x] 내 정보 조회 (`GET /auth/me`)

### 설정 (Settings)
- [x] 프로필 조회/수정 (이름, 전화번호, 아바타)
- [x] 비밀번호 변경
- [x] 알림 설정 저장 (`PUT /settings/notifications`) — 2026-03-20
  - `users` 테이블에 `noti_assignment`, `noti_announcement`, `noti_inquiry_reply` 컬럼 추가 (`database/notification_settings.sql`)
  - `User.java` 필드 추가, `UserMapper.java` + `UserMapper.xml` 쿼리 추가

### 학생 기능
- [x] 학생 홈 대시보드 (풀이 수, 정답률 통계)
- [x] 진단 테스트 (20문항, 레벨 B 기준, 결과로 레벨 A/B/C 배정)
- [x] 학습 세션 시작/진행/완료
- [x] 문항별 답 제출 및 결과 처리
- [x] 북마크 추가/삭제/목록 조회 ⚠️ v6에서 오답노트로 개념 전환됨 — 확장 필요
- [x] 북마크 문제로 학습 세션 시작
- [x] 오답노트 백엔드 구현 (자동저장·목록·삭제·해결토글) — 2026-03-20
- [x] 오답노트 프론트엔드 구현 (WrongNotesPage.vue, 라우터, 사이드바) — 2026-03-20
- [x] 오답노트 관련 영상 바로보기 버튼 추가 (`/student/videos?problemId=...` 라우팅) — 2026-03-20
- [x] 오답노트 재도전 버튼 (start-single API + 프론트 연동, 정답 시 is_resolved 자동처리) — 2026-03-21
- [x] KaTeX 수식 렌더링 (MathText.vue 컴포넌트, 문제풀이/오답노트 적용) — 2026-03-21
- [x] 학습 리포트 차트 고도화 (정답률 추이/일별 풀이량/월별 비교 SVG 차트) — 2026-03-21
- [x] 문항별 풀이 소요시간 기록 (problemStartTime 타이머, submit 시 timeSpentSec 전달) — 2026-03-21
- [x] 단답형 문제 입력창 미표시 버그 수정 (problemType API 미전달 → 입력 불가) — 2026-03-20
- [x] ProblemSolvePage 북마크 버튼 제거 (오답노트 자동저장으로 대체) — 2026-03-20
- [x] 정답/오답 즉시 피드백 토스트 추가 (정답: success / 오답: error + 해설 자동 노출) — 2026-03-20
- [x] 학습 리포트 조회 (기본)
- [x] 과제 피드백 조회
- [x] 동영상 목록 및 상세 조회 (조회수 카운트)
- [x] AI 코멘트 조회 (`GET /student/ai-comment`)
- [x] 학습 이력 조회

### 교사 기능
- [x] 교사 홈 대시보드 (기본)
- [x] 반 학생 목록 (페이지네이션)
- [x] 학생 상세 — 통계, 약점 단원, 이력, 진도 조회
- [x] 학생 레벨 변경
- [x] 과제 생성 (문제 선택 + 대상 학생 지정)
- [x] 과제 목록 및 상세 조회
- [x] 과제별 제출 현황 조회
- [x] 과제 피드백 작성 (전체 + 문항별)
- [x] 학습 현황 분석 (`/teacher/analytics`)
- [x] 미완료 학생 목록 조회
- [x] 교사 문제은행 탐색 페이지 (/teacher/problems, 필터+검색+모달+과제담기) — 2026-03-21
- [x] 교사 학급관리 페이지 (/teacher/classes, 학급 CRUD + 학생 추가/제거) — 2026-03-21

### 관리자 기능
- [x] 관리자 대시보드 (DAU, 레벨 분포, 통계)
- [x] 회원 목록 조회/상태 변경(활성/비활성)
- [x] 학원/학교 등록·수정·목록 조회
- [x] 결제 목록 및 통계 (조회만)
- [x] 시스템 분석 (DAU 30일, 레벨 분포, 약점 단원)
- [x] 1:1 문의 목록/상세/답변
- [x] 동영상 업로드, 목록, 수정, 토글, 삭제
- [x] 시스템 코드 관리 (계층형 코드 CRUD)
- [x] 문제 DB 검색 (레벨·학년·과목·단원 필터)
- [x] 문제 생성/수정/삭제 (`/admin/problems`, `/problems`)
- [x] 문제 배치 업로드 (`POST /problems/upload/batch`)

### 공통 기능
- [x] 공지사항 목록/상세/생성/수정/삭제
- [x] 1:1 문의 생성/조회 (학생)
- [x] 알림 목록, 읽음 처리, 미읽음 카운트
- [x] 문제 검색·상세 (`GET /problems`, `GET /problems/{id}`)
- [x] 관련 동영상 추천 (`GET /videos/{id}/related`)

---

## 미구현 기능 (TODO)

### 🚨 블로킹 이슈 — 개발 착수 전 클라이언트 확정 필요

| 이슈 | 내용 | 영향 범위 |
|------|------|------|
| 교사-학생 매칭 방식 | ① 교사 주도(초대코드) vs ② 학생 주도(검색 신청) 중 메인 방식 1개 확정 필요 | 회원가입 플로우, 학급관리, 과제배정 연동 전체 |
| DRM / 콘텐츠 보안 수준 | MVP: Signed URL / Phase2: 화면캡처방지 / Phase3: Widevine DRM — 미결정 시 보안 설계 블로킹 | 동영상 재생, 문제 콘텐츠 보호 |
| 교사 리워드 법적 검토 | 공교육 교사 대상 리워드 → 김영란법 저촉 여부 법무 검토 필수 | 리워드 시스템 전체 |

---

### 인증
| 기능 | 위치 | 비고 |
|------|------|------|
| 비밀번호 재설정 이메일 실제 발송 | `AuthService.java:209` | 로직 골격만 존재 |
| 이메일 인증 토큰 생성/발송 | `AuthService.java:229` | 로직 골격만 존재 |
| 이메일 인증 토큰 검증 처리 | `AuthService.java:235` | 로직 골격만 존재 |
| 소셜 로그인 실제 연동 (Google/Kakao) | `build.gradle:32` | OAuth2 의존성 주석처리됨 |

---

### 학생 — 오답노트 (v6 신규: 기존 북마크에서 개념 전환)
> 백엔드 + 프론트엔드 구현 완료 (2026-03-20).

**구현 완료된 API 및 UI**
- `GET /student/wrong-notes` — 페이지네이션 목록 (문제 정보 JOIN 포함)
- `DELETE /student/wrong-notes/{id}` — 단건 삭제 (본인 것만)
- `DELETE /student/wrong-notes` — 전체 삭제
- `PATCH /student/wrong-notes/{id}/resolve` — 해결 여부 토글
- `submitAnswer()` 오답 시 `wrong_notes` 자동저장 (UNIQUE KEY로 중복 방지)
- `/student/wrong-notes` 페이지: 과목/단원/레벨 클라이언트 필터 + 페이지네이션 + 해결 토글 + 삭제

**신규 파일 목록**
- `domain/WrongNote.java`, `dto/student/WrongNoteDto.java`
- `mapper/WrongNoteMapper.java`, `resources/mapper/WrongNoteMapper.xml`
- `service/WrongNoteService.java`, `controller/WrongNoteController.java`
- `frontend/src/pages/student/WrongNotesPage.vue` (재도전 버튼 추가 — 2026-03-21)
- `frontend/src/pages/student/ProblemSolvePage.vue` (single 모드 분기 추가 — 2026-03-21)
- `database/wrong_notes.sql` (증분 적용용)
- `database/single_session.sql` (session_type ENUM에 SINGLE 추가 — 2026-03-21)

**남은 TODO**

| 기능 | 비고 |
|------|------|
| ~~오답노트 재도전 버튼~~ | ✅ 완료 (2026-03-21) |
| ~~관련 풀이 영상 바로보기~~ | ✅ 완료 (2026-03-20) |

---

### 학생 — 문제풀이 고도화
| 기능 | 비고 |
|------|------|
| ~~[오답 북마크] 버튼 제거~~ | ✅ 완료 (2026-03-20) |
| 문제 데이터 품질 개선 | 그림·조건 포함 문제 누락 — 현재 시드 데이터 한계, 실제 기출 데이터 입력 시 해결 |
| ~~LaTeX 수식 렌더링~~ | ✅ 완료 (2026-03-21) — MathText.vue, KaTeX 적용 |
| ~~정답/오답 즉시 피드백 토스트~~ | ✅ 완료 (2026-03-20) — 오답 시 해설 자동 노출 포함 |
| 오답 시 관련 풀이 영상 연동 | 해설 하단 [영상으로 보기] 버튼, 영상 목록 섹션 |
| 풀이 임시저장 (이어풀기) | 중간 이탈 시 마지막 위치 자동 저장, 재접속 시 이어풀기 |
| ~~문항별 풀이 소요시간 기록~~ | ✅ 완료 (2026-03-21) |
| '이해했어요 / 아직 모르겠어요' 피드백 버튼 | 학습 데이터 수집용 |

---

### 학생 — 리포트 고도화
| 기능 | 비고 |
|------|------|
| ~~등급 변화 타임라인 차트~~ | ✅ 완료 (2026-03-21) — 정답률 추이 꺾은선 차트 |
| ~~단원별 취약 분석 바 차트~~ | ✅ 완료 (2026-03-21) — 일별 풀이량 막대 차트 |
| 월별 학습 달력 | 날짜별 학습 도트, 정답률에 따라 초록/노랑/빨강 색상 |
| ~~이번 달 vs 지난달 정답률 변화~~ | ✅ 완료 (2026-03-21) — 월별 비교 차트 (▲▼ 증감 표시) |

---

### 교사 — 문제 은행 탐색 (신규 화면)
> ✅ 완료 (2026-03-21) — ProblemBankPage.vue, `/teacher/problems`, 사이드바 메뉴 추가

| 기능 | 비고 |
|------|------|
| ~~검색 필터 패널~~ | ✅ 완료 — 난이도(A/B/C 멀티선택)/학년/단원명/키워드 |
| ~~문제 목록 뷰~~ | ✅ 완료 — 번호/단원/난이도/학년/미리보기, 페이지네이션 20개 |
| ~~문제 상세 모달~~ | ✅ 완료 — 문제 전문 + 선택지(정답 강조) + 정답 + 해설, KaTeX |
| ~~과제 바구니~~ | ✅ 완료 — 체크박스 다중선택 → 과제 생성 화면 연결 |
| ~~상단 고정 바구니 아이콘~~ | ✅ 완료 — 하단 고정 바 (선택 수 카운트 + 과제 만들기 버튼) |

---

### 교사 — 과제 기능 보완
| 기능 | 위치 | 비고 |
|------|------|------|
| 등급별 자동 분리 배정 | `TeacherService.java` | A문제→A학생, B문제→B학생 자동 분리 로직 |
| 배정 완료 시 학생 인앱 알림 | `TeacherService.java:142` | 알림 로직 미구현 |
| 과제 수정 | `TeacherController.java:170` | 서비스 미구현 |
| 과제 삭제 | `TeacherController.java:176` | 서비스 미구현 |
| 과제 복사(재사용) 버튼 | - | 신규 |
| 미완료 학생 개별 독려 알림 버튼 | - | 과제 상세 화면 |

---

### 교사 — 학급/그룹 관리
> ✅ 학급 CRUD + 학생 추가/제거 완료 (2026-03-21) — ClassManagePage.vue, ClassController/Service/Mapper

| 기능 | 비고 |
|------|------|
| ~~학급 생성/수정/삭제~~ | ✅ 완료 (2026-03-21) — 학급명/학년/난이도 설정, 소프트 삭제 |
| ~~학생 검색 후 다중 추가/개별 제거~~ | ✅ 완료 (2026-03-21) — INSERT IGNORE로 중복 방지 |
| 학급별 과제 일괄 배정 | 학급 단위로 과제 배정 |
| 학급별 리포트 조회 | 학급 단위 성적 현황 |
| 학생 초대 — 초대코드 방식 | 교사가 고유 코드 생성 → 학생이 가입 시 코드 입력 → 자동 연결 |
| 학생 초대 — 이메일 초대 | 이메일 입력 → 초대 메일 발송 → 학생 수락 후 연결 |
| 학생 초대 — CSV 일괄 추가 | 이메일 목록 CSV 업로드 |
| 매칭 해제 및 재배정 | 교사가 학생 연결 해제. 기존 학습 데이터 보관 |

---

### 교사 — 리포트 고도화
| 기능 | 비고 |
|------|------|
| CSV 내보내기 | 학급 전체 성적 테이블 CSV 다운로드 |
| 등급별 분포 차트 | A/B/C 파이차트 + 바차트 탭 전환, 이번달 vs 지난달 비교 |
| 취약 단원별 학생 분석 | 특정 단원 선택 → 정답률 하위 학생 리스트 + 보충 배정 바로가기 |

---

### 어드민 — 문제 관리 보완
| 기능 | 비고 |
|------|------|
| 문제 검수/승인 워크플로우 | AI 파싱 결과 검수 대기 목록, 승인/반려 처리, 재검수 상태 |
| AI 자동 파싱 결과 화면 | 파싱 완료 시 문제 초안 리스트, 인식 신뢰도(%), 오류 강조 |
| 교사 DB 기여 리워드 관리 | 승인 문제 N건당 이용권 자동 지급, 리워드 이력 (법적 검토 후) |

---

### 어드민 — 기타
| 기능 | 위치 | 비고 |
|------|------|------|
| 비밀번호 초기화 시 이메일 발송 | `AdminService.java:109` | 임시 비밀번호 이메일 미발송 |

---

### 설정
| 기능 | 위치 | 비고 |
|------|------|------|
| ~~알림 설정 저장~~ | ✅ 완료 (2026-03-20) | users 테이블 컬럼 추가 + API 구현 |
| 회원 탈퇴 | - | 탈퇴 사유 선택 + 확인 모달 + 데이터 처리 안내 |
| 구독/이용권 현황 확인 | - | 현재 플랜명, 결제일/만료일 표시 |

---

### 동영상
| 기능 | 위치 | 비고 |
|------|------|------|
| 동영상 풀이 요청 | `VideoController.java:61` | 서비스 미구현 |
| 영상 시청 이력 조회 | - | 조회수 카운트는 있으나 이력 조회 미구현 |
| 외부 URL 임베드 (YouTube/Vimeo) | - | MVP 권장 방식, 직접 업로드 대안 |

---

### DB 스키마 존재하나 서비스 미연동
| 기능 | DB 테이블 | 비고 |
|------|-----------|------|
| 구독/결제 처리 | `subscriptions`, `subscription_plans` | 결제 조회만 구현, 실제 PG 연동 없음 |
| 문제 태그 | `problem_tags` | 스키마만 존재 |
| 동영상 시청 기록 | `video_views` | 조회수 카운트는 있으나 이력 조회 미구현 |

---

## 프론트엔드 페이지 현황

| 역할 | 경로 | 구현 |
|------|------|------|
| 공통 | `/` (랜딩) | 완료 (고도화 필요) |
| 공통 | `/login`, `/register`, `/password/forgot` | 완료 |
| 공통 | `/settings` | 완료 |
| 공통 | `/announcements`, `/notices` | 완료 |
| 학생 | `/student/home` | 완료 |
| 학생 | `/student/diagnosis` | 완료 |
| 학생 | `/student/learn`, `/student/learn/:sessionId` | 완료 (고도화 필요) |
| 학생 | `/student/videos`, `/student/videos/:id` | 완료 |
| 학생 | `/student/bookmarks` | 완료 (사이드바에서 제거됨, 경로는 유지) |
| 학생 | `/student/wrong-notes` | 완료 (2026-03-20) |
| 학생 | `/student/report` | 완료 (월별 달력만 미구현) |
| 학생 | `/student/assignments/:id/feedback` | 완료 |
| 교사 | `/teacher/home` | 완료 (고도화 필요) |
| 교사 | `/teacher/students`, `/teacher/students/:id` | 완료 |
| 교사 | `/teacher/assignments`, `/teacher/assignments/create`, `/teacher/assignments/:id` | 완료 |
| 교사 | `/teacher/analytics` | 완료 |
| 교사 | `/teacher/problems` | 완료 (2026-03-21) |
| 교사 | `/teacher/classes` | 완료 (2026-03-21) |
| 관리자 | `/admin/dashboard` | 완료 |
| 관리자 | `/admin/members`, `/admin/schools` | 완료 |
| 관리자 | `/admin/problems`, `/admin/problems/upload` | 완료 |
| 관리자 | `/admin/videos` | 완료 |
| 관리자 | `/admin/payments` | 완료 (조회만) |
| 관리자 | `/admin/analytics` | 완료 |
| 관리자 | `/admin/codes` | 완료 |
| 관리자 | `/admin/inquiries` | 완료 |
