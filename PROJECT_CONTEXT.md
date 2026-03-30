# PROJECT_CONTEXT.md
AI 기반 수학 교육 플랫폼 — 구현 현황 및 프로젝트 컨텍스트

> **Claude에게:** 이 파일 하나로 프로젝트 전체 맥락을 파악할 수 있도록 작성되었습니다.
> 새 세션 시작 시 이 파일을 첨부하면 이전 작업 내용을 바로 이어받을 수 있습니다.

---

## ⚡ 다음 세션 작업 (우선순위 순)
- [ ] PDF 업로드 화면 — 원본 PDF와 파싱 결과 2분할 비교 뷰 구현
- [ ] passage(보기) 미세 조정 — 테두리 박스 감지 AI 인식률 지속 개선
- [ ] 교사 계정으로 과제 출제 테스트
- [ ] 학생 계정으로 문제풀이 테스트 (보기 표시 확인 포함)
- [ ] 학생/교사 홈 대시보드 고도화

---

## 기술 스택 및 구조

| 항목 | 내용 |
|------|------|
| Backend | Spring Boot 3.2.3 + MyBatis + JWT / `http://localhost:7000/api` |
| Frontend | Vue 3 + Vite + Pinia / `http://localhost:7001` |
| DB | MySQL 8.0 / `211.171.152.242:3310/edu_platform` (root/root1234) |
| AWS S3 | 버킷: `ai-edu-bucket` / 리전: `ap-northeast-2` / 퍼블릭 읽기 허용됨 |
| 사용자 역할 | STUDENT / TEACHER(=SUPER_USER) / ADMIN |
| 라우팅 | STUDENT → `/student/*` / TEACHER → `/teacher/*` / ADMIN → `/admin/*` |
| Backend 레이어 | Controller → Service → Mapper(MyBatis XML) → DB |
| API 응답 | 모든 응답 `ApiResponse<T>` 래퍼 통일 |
| 예외 처리 | `BusinessException(ErrorCode)` 사용 |
| 파일 경로 | Controller: `controller/` / Service: `service/` / Mapper XML: `resources/mapper/` / DTO: `dto/{역할}/{기능}/` |
| Frontend 경로 | Pages: `pages/{student,teacher,admin,auth,common}/` / Store: `store/` / Layout: `DefaultLayout.vue`(인증후) `AuthLayout.vue`(로그인) |
| MyBatis | snake_case → camelCase 자동변환 (`map-underscore-to-camel-case: true`) / XML·Interface 쌍으로 생성 |

---

## 개발 작업 방식

### Claude CLI 기반 협업 플로우
이 프로젝트는 Claude LLM + Claude CLI를 조합한 방식으로 개발 중.

```
Claude LLM (claude.ai)
  → 명령어/코드 작성
  → 개발자가 Claude CLI 터미널에 붙여넣기
  → CLI 실행 결과를 Claude LLM에 다시 전달
  → 다음 명령어 작성
```

### 작업 환경
- **OS:** Windows (PowerShell + Git Bash 혼용)
- **IDE:** VS Code
- **터미널:** VS Code 내장 터미널 (bash / powershell 탭 분리)
- **프로젝트 경로:** `C:\claude\ai_edu-master`
- **Git Bash 경로 표기:** `/c/claude/ai_edu-master`

### 명령어 작성 규칙
- 파일 탐색·수정·git 작업 → **bash** 문법 사용
- Windows 시스템 환경변수 설정 → **PowerShell** 문법 사용
- `&&` 체이닝은 bash에서만 동작. PowerShell에서는 `;` 사용

### 환경변수 영구 등록 방법 (Windows)
서버 재시작·VS Code 재시작 후에도 유지되려면 반드시 Machine 레벨로 등록:
```powershell
# 관리자 PowerShell에서 실행
[System.Environment]::SetEnvironmentVariable("키이름", "값", "Machine")

# 확인
[System.Environment]::GetEnvironmentVariable("키이름", "Machine")
```
등록 후 **VS Code 완전 종료 → 재시작** 해야 새 환경변수가 백엔드에 반영됨.

### 새 PC(노트북) 세팅 체크리스트
```
1. git clone 또는 pull
2. 관리자 PowerShell에서 환경변수 5개 Machine 레벨 등록:
   - ANTHROPIC_API_KEY
   - AWS_ACCESS_KEY
   - AWS_SECRET_KEY
   - AWS_S3_BUCKET (값: ai-edu-bucket)
   - AWS_REGION (값: ap-northeast-2)
3. VS Code 재시작
4. cd backend && ./gradlew bootRun
5. cd frontend && npm install && npm run dev
```

---

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

> ⚠️ **새 PC 세팅 시 아래 항목들을 Windows 시스템 환경변수(Machine 레벨)로 반드시 등록할 것**
> 관리자 PowerShell에서 한 번만 실행하면 영구 적용됨 (재부팅·VS Code 재시작 후에도 유지)
> ```powershell
> [System.Environment]::SetEnvironmentVariable("변수명", "값", "Machine")
> ```

| 변수 | 상태 | 비고 |
|------|------|------|
| `JWT_SECRET` | 기본값 사용 중 | 프로덕션 전 변경 필요 |
| `MAIL_FROM` | `horsehihing3@gmail.com` — Machine 레벨 영구 등록 완료 (2026-03-30) | AWS SES 발신 이메일 (Sandbox, Verified) |
| `APP_BASE_URL` | `http://localhost:7001` — Machine 레벨 영구 등록 완료 (2026-03-30) | 비밀번호 재설정 링크 base URL |
| `AWS_S3_BUCKET` | `ai-edu-bucket` — Machine 레벨 영구 등록 완료 | S3 연동 완료 |
| `AWS_REGION` | `ap-northeast-2` — Machine 레벨 영구 등록 완료 | S3 연동 완료 |
| `AWS_ACCESS_KEY` | Machine 레벨 영구 등록 완료 (2026-03-26) | S3 업로드 동작 |
| `AWS_SECRET_KEY` | Machine 레벨 영구 등록 완료 (2026-03-26) | S3 업로드 동작 |
| `ANTHROPIC_API_KEY` | Machine 레벨 영구 등록 완료 (2026-03-26) | AI 파싱 동작 |
| `CLOUDFRONT_DOMAIN` | 미설정 | CDN 미사용 |
| `UPLOAD_PATH` | 기본값(`uploads/`) | 로컬 업로드는 동작 |

---

## 트러블슈팅 패턴

| 증상 | 원인 | 해결 |
|------|------|------|
| 새 API 호출 시 `NoResourceFoundException` | Controller/Service 추가 후 백엔드 미재시작 | `taskkill //F //PID {PID}` 후 `./gradlew bootRun` |
| 포트 7000 이미 사용 중 | kill 후에도 포트 점유 지속 | `netstat -ano \| findstr :7000` 으로 PID 확인 후 kill |
| PDF 파싱 / S3 업로드 안 됨 | VS Code 재시작 후 환경변수 미반영 | Machine 레벨 등록 후 VS Code 완전 재시작 필요 |
| 문제 DB 페이지 500 에러 | DB `status` 컬럼 enum값 `PENDING_REVIEW`인데 프론트가 `PENDING` 사용 | `ProblemDBPage.vue` — `PENDING` → `PENDING_REVIEW` 수정 완료 (2026-03-26) |
| 배치 업로드 시 문제 저장 안 됨 | `duplicate_problem_id` 컬럼 DB 미존재 | `mysql ... -e "ALTER TABLE problems ADD COLUMN duplicate_problem_id BIGINT NULL"` 실행. 증분SQL: `database/problem_duplicate_check.sql` |
| SES 이메일 스팸함 분류 | Sandbox 모드 + 도메인 미인증 | 도메인 연결 후 해결. 테스트 시 스팸함 확인 |
| 비밀번호 재설정 API 경로 | `/reset-request` 아님 | `POST /auth/password/forgot` 사용 |
| answer 컬럼 저장 오류 | VARCHAR(10) 길이 초과 | `ALTER TABLE problems MODIFY COLUMN answer VARCHAR(500) NOT NULL DEFAULT ''` 완료 (2026-03-27, migration: `database/alter_answer_column.sql`) |
| 보기 테두리 박스가 이미지로 캡처 | 벡터 클러스터가 텍스트 박스 테두리를 도형으로 인식 | `extract_images.py` 텍스트 밀도 30% 초과 시 캡처 생략 (2026-03-27) |
| 이미지가 인접 문제번호로 잘못 매핑 | Vision 프롬프트가 도형 아래 문제로 매핑 | `PdfImageExtractService.java` 프롬프트에 "도형 위쪽 문제번호 우선" 규칙 추가 (2026-03-27) |
| 문제 수정 모달에 보기 표시 안 됨 | `mapToDetailDto()`에 passage 필드 누락 | `ProblemService.java:255` passage/passageImgUrl 추가 (2026-03-27) |
| `videos.problem_id` 없음 | 문제-영상 연결 구조 미구현 | `videos` 테이블에 `problem_id` 컬럼 추가 (2026-03-30, 추후 중간테이블 전환 예정). 증분SQL: `database/add_video_problem_id.sql` |
| `assignments.is_deleted` 없음 | schema SQL 미적용 | `ALTER TABLE assignments ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0` 실행 (2026-03-30) |
| `student_id` vs `user_id` 혼동 | `users.user_id` ≠ `students.student_id` | `learning_sessions`은 `students.student_id` 기준. 예: student_a = user_id:6, student_id:1 |
| YouTube embed 재생 안 됨 | `watch?v=` URL을 `<video>` 태그로 재생 시도 | embed URL + `<iframe>` 사용. `videoType === 'YOUTUBE'\|'VIMEO'`로 분기 (`ProblemSolvePage.vue`, `VideoDetailPage.vue`) |

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
- [x] 비밀번호 재설정 이메일 발송 (AWS SES 연동 완료, 2026-03-30)

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

### 공통 — 레이아웃
- [x] 모바일 하단 탭바 (학생) — `BottomNavBar.vue`, 360~767px, 홈/문제풀기/동영상/오답노트/설정 5개 탭, 활성탭 강조, 사이드바 자동 닫기 (2026-03-30)

### 관리자
- [x] 대시보드, 회원·학교 관리, 결제 목록(조회만), 시스템 분석
- [x] 1:1 문의, 동영상 관리, 시스템 코드 관리
- [x] 문제 DB 검색·생성·수정·삭제·배치 업로드 (`POST /problems/upload/batch`)
- [x] 문제 검수/승인 워크플로우 — 검수 대기 배너, PENDING_REVIEW/APPROVED/REJECTED 상태 배지, 승인/반려/재승인 버튼, 반려 사유 모달. 승인된 문제만 학생·교사에 노출 (is_active 연동)
- [x] PDF 문제지 업로드 → Claude AI 파싱 (`POST /admin/parse-pdf`) — 자동 파싱
- [x] S3 이미지 자동 추출 — PyMuPDF(Python) + Claude Vision 하이브리드. bbox union 크롭 방식으로 다중 조각 정확 추출 (`PdfImageExtractService.java`, `scripts/extract_images.py`)
- [x] 문제 수정 모달에 `questionImgUrl` 필드 및 이미지 미리보기 추가 (`ProblemDBPage.vue`)
- [x] **중복 문제 감지** — 배치 업로드 시 공백 제거 후 앞 50글자 비교로 중복 감지 → `duplicate_problem_id` 저장. 목록에 🔴 중복 뱃지, 수정 모달에 "N번 문제와 중복" 경고 배너 (2026-03-26)
- [x] **PDF 파싱 프롬프트 개선** — 수식 변수 오인 방지 (`Ax → 4x` 오류 방지), 그림 포함 문제 `[그림]` 태그 표시 (2026-03-26)
- [x] **보기(passage) 필드 신규 추가** — 테두리 박스 안 참고 지문을 별도 필드로 분리 저장. "보기" 글자 유무 무관하게 직사각형 박스 감지 시 추출 (2026-03-27)
  - `Problem.java` passage/passageImgUrl 필드 추가
  - `ProblemMapper.xml` resultMap·INSERT·UPDATE 반영
  - `ProblemService.java` mapToDetailDto()에 passage 포함
  - `PdfParseController.java` 파싱 프롬프트에 passage 필드 추가 (테두리 박스 감지 규칙 포함)
  - `ProblemUploadPage.vue` 파싱 매핑·배치 payload에 passage 추가
  - `ProblemSolvePage.vue` passage 테두리 박스 UI (.passage-box) 추가
  - `ProblemDBPage.vue` 보기 텍스트 편집 필드 추가
- [x] **벡터 이미지 텍스트 밀도 필터** — `extract_images.py`에서 벡터 클러스터 bbox 내 텍스트 비율 > 30%이면 이미지 캡처 생략 (보기 테두리 박스 오캡처 방지) (2026-03-27)
- [x] **Vision 문제번호 매핑 개선** — 도형 위쪽 문제번호 우선 매핑, 도형 아래쪽 문제로 잘못 배정 방지 (`PdfImageExtractService.java`) (2026-03-27)
- [x] **주관식 정답 저장 오류 수정** — `answer` 컬럼 `VARCHAR(10)` → `VARCHAR(500)` 확장 (`database/alter_answer_column.sql`) (2026-03-27)
- [x] **검수 대기 문제 일괄 승인** — `POST /admin/problems/approve-all`, 검수 대기 배너에 [전체 승인] 버튼 (2026-03-30)
- [x] **오답 시 관련 풀이 영상 연동** — 오답 판정 시 자동 조회, 모달 재생. `videoType`으로 YOUTUBE/VIMEO → `<iframe>`, UPLOAD → `<video>` 분기. `videos.problem_id` 컬럼 추가 (2026-03-30)
- [x] **VideoDetailPage.vue YouTube/Vimeo iframe 렌더링 수정** — URL 패턴 검사 → `videoType` 기준으로 교체, 이미 embed URL인 경우 재변환 오류 방지 (2026-03-30)
- [x] **풀이 임시저장 / 이어풀기** — `nextProblem` 시 자동저장, 이탈 시 `onBeforeUnmount` 저장, 재시작 시 이어풀기 confirm 다이얼로그. `learning_sessions.problem_ids`에 currentIndex 저장 (2026-03-30)
- [x] **로그 파일 출력 설정** — `application.yml` `logging.file.name: logs/app.log` 추가 (2026-03-27)

### 공통
- [x] 공지사항 CRUD, 1:1 문의, 알림 (목록·읽음·카운트)
- [x] 문제 검색·상세, 관련 동영상 추천

---

## PDF 파싱 / 이미지 추출 현황

> ✅ **이미지 추출 기능 완료** — PyMuPDF + Claude Vision 하이브리드 방식

### 동작 방식
```
PDF 업로드(base64)
→ PdfParseController: Claude AI로 문제 텍스트 파싱 (max_tokens: 8192)
→ PdfImageExtractService:
   1. Python(PyMuPDF) 스크립트로 PDF 임베디드 이미지 추출 → bbox 좌표 반환
   2. PDFBox로 페이지 전체를 1000px 리사이즈 base64 렌더링 → Claude Vision에 전송
   3. Vision이 페이지 이미지 + bbox 위치 비율(x/y/w/h)로 문제번호 식별
   4. 같은 문제번호 조각들 bbox union → 5% 여백 추가
   5. PDFBox 200DPI BufferedImage에서 union bbox 직접 크롭 → S3 업로드
→ ProblemUploadPage.vue: 파싱 결과 인라인 수정 테이블
→ POST /problems/upload/batch: DB 저장 (questionImgUrl 포함)
```

### 파싱 프롬프트 주요 규칙 (2026-03-27 개선)
- 수식 변수(A, B, x, y 등) 절대 숫자로 변환 금지 (`Ax → 4x` 오류 방지)
- PDF 원문 그대로 추출 (임의 수정 금지)
- 그림 포함 문제는 questionText에 `[그림]` 태그 포함
- √, ², ³ 등 수학 기호 원문 유지
- **보기(passage)**: 직사각형 테두리 박스 내용은 "보기" 글자 유무 무관하게 passage 필드로 분리 추출

### 벡터 이미지 탐지 (extract_images.py)
- `page.get_drawings()` 클러스터링으로 벡터 도형 감지
- 텍스트 밀도 > 30%이면 이미지 캡처 생략 (보기 텍스트 박스 오감지 방지)
- 래스터 이미지와 30% 이상 겹치면 중복 skip
- 클러스터 최소 면적 4000pt², 최소 가로/세로 40pt

### 알려진 한계
- 보기가 도형+텍스트 혼합인 경우 passage/이미지 혼용 가능 — 수동 수정 필요
- AI 파싱 시 수식 인식 오류 가능 → 파싱 결과 화면에서 수동 수정 필요
- 관리자가 questionImgUrl/passage 수동 수정 가능 (문제 수정 모달)

### 환경변수 (로컬 실행 시 필요 — Windows Machine 레벨 등록 권장)
| 변수 | 용도 |
|------|------|
| `ANTHROPIC_API_KEY` | Claude AI 파싱 / Vision |
| `AWS_ACCESS_KEY` | S3 업로드 |
| `AWS_SECRET_KEY` | S3 업로드 |

### 주요 버그 수정 이력
| 버그 | 원인 | 수정 위치 |
|------|------|------|
| questionImgUrl DB 저장 안 됨 | ProblemUploadPage.vue parseWithAI()에서 questionImgUrl 누락 | `ProblemUploadPage.vue:298` — `questionImgUrl: p.questionImgUrl \|\| ''` 추가 |
| 문제 DB 페이지 500 에러 | 프론트 `PENDING` vs DB enum `PENDING_REVIEW` 불일치 | `ProblemDBPage.vue` — `PENDING` → `PENDING_REVIEW` 일괄 수정 (2026-03-26) |
| PDF 파싱/S3 업로드 안 됨 | 환경변수 세션 종료 시 초기화 | `ANTHROPIC_API_KEY`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` Windows Machine 레벨 영구 등록 (2026-03-26) |
| 배치 업로드 시 저장 안 됨 | `duplicate_problem_id` 컬럼 DB 미존재 | `database/problem_duplicate_check.sql` 실행 필요 (2026-03-26) |
| answer 컬럼 저장 오류 | VARCHAR(10) 길이 초과 | `ALTER TABLE problems MODIFY COLUMN answer VARCHAR(500)` 완료 (2026-03-26) |

---

## 중복 문제 감지 현황 (2026-03-26 신규)

### 동작 방식
```
배치 업로드(POST /problems/upload/batch)
→ ProblemService.uploadBatch()
→ 각 문제의 questionText에서 공백 제거 후 앞 50글자 비교
→ ProblemMapper.findIdByQuestionText() — DB에서 동일 문자열 검색
→ 일치하면 duplicate_problem_id에 원본 problem_id 저장
→ ProblemDBPage.vue: 목록에 🔴 중복 뱃지 표시
→ 수정 모달: "N번 문제와 중복됩니다" 경고 배너
```

### DB 컬럼
```sql
-- database/problem_duplicate_check.sql
ALTER TABLE problems
ADD COLUMN duplicate_problem_id BIGINT NULL DEFAULT NULL COMMENT '중복 문제 ID';
```

> ⚠️ **주의:** 배치 업로드 오류 발생 시 이 컬럼 존재 여부 먼저 확인
> ```bash
> mysql -h 211.171.152.242 -P 3310 -u root -proot1234 edu_platform -e "SHOW COLUMNS FROM problems LIKE 'duplicate%';"
> ```
> 없으면 위 SQL 직접 실행

### 알려진 한계
- AI 파싱 시마다 텍스트가 미세하게 달라질 수 있어 일부 중복이 감지 안 될 수 있음
- 향후 개선 후보: 유사도 기반 매칭 또는 batch_id 관리 방식

---

## 미구현 기능 (TODO)

### 관리자 — 문제 관리
| 기능 | 위치 | 비고 |
|------|------|------|
| PDF 업로드 2분할 비교 뷰 | `ProblemUploadPage.vue` | 원본 PDF(좌) + 파싱 결과(우) 나란히 비교 |

### 인증
| 기능 | 위치 | 비고 |
|------|------|------|
| 이메일 인증 토큰 생성/발송/검증 | `AuthService.java:229` | 골격만 존재 |
| 소셜 로그인 실제 연동 (Google/Kakao) | `build.gradle:32` | OAuth2 의존성 주석처리됨 |

### 학생 — 문제풀이
| 기능 | 위치 | 비고 |
|------|------|------|
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
