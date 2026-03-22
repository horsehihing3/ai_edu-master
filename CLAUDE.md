# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI 기반 수학 교육 플랫폼. Spring Boot 백엔드 + Vue 3 프론트엔드 구조.

- **Backend:** `http://localhost:7000/api` (Spring Boot 3.2.3 + MyBatis + JWT)
- **Frontend:** `http://localhost:7001` (Vue 3 + Vite + Pinia)
- **DB:** MySQL 8.0 (`211.171.152.242:3310/edu_platform`, root/root1234)

## Commands

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
```

### Database
```bash
mysql -h 211.171.152.242 -P 3310 -u root -p edu_platform < database/schema.sql
mysql -h 211.171.152.242 -P 3310 -u root -p edu_platform < database/seed.sql
# 증분 적용 (스키마 변경 시)
mysql -h 211.171.152.242 -P 3310 -u root -p edu_platform < database/{변경파일}.sql
```

## Architecture

### 사용자 역할 및 접근 구조
- `STUDENT` → `/student/*`
- `TEACHER` / `SUPER_USER` → `/teacher/*`
- `ADMIN` → `/admin/*`

라우터 가드(`frontend/src/router/index.js`)에서 역할별 자동 리다이렉트 처리.

### Backend 레이어 구조
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

## 도메인 용어 정의 (혼동 주의)

| 용어 | 정의 | 혼동 주의 |
|------|------|------|
| **과제 (Assignment)** | 교사가 학생에게 출제하는 문제 묶음 | ≠ 세션 |
| **세션 (Session)** | 학생이 과제를 풀기 시작한 단위 (start → submit → complete) | ≠ 과제 |
| **등급 (Level)** | 학습 수준 A/B/C | ≠ 학년 |
| **학년 (Grade)** | 학교 학년 중1/중2/중3 | ≠ 등급 |
| **오답노트 (WrongNote)** | v6 기준 오답 자동저장 기능. 북마크를 대체함 | 북마크 API는 유지하되 UI 노출 금지 |
| **북마크 (Bookmark)** | 구버전 개념. v6부터 오답노트로 대체됨. 코드는 존재하나 사용 안 함 | 새 기능에 북마크 개념 사용 금지 |
| **Super User** | 학교 교사. TEACHER 역할과 동일하게 취급. 학원강사 아님 | ≠ 관리자(Admin) |

## 절대 규칙

**어떤 상황에서도 반드시 지킬 것.**

- 프로덕션 DB 직접 수정 금지 — 스키마 변경은 반드시 `database/*.sql`을 통해
- `.env` 및 환경변수 파일 커밋 금지
- 승인 없이 DB 스키마 변경 금지 (테이블 추가/삭제/컬럼 변경)
- 승인 없이 기존 API 응답 구조 변경 금지 (하위 호환성 깨짐 방지)
- 백엔드 파일 추가/변경(Controller·Service·Mapper) 후 반드시 bootRun 재시작 확인
  (미재시작 시 새 API 404/500 오류 발생)

## 코딩 컨벤션

### Backend
- 모든 API 응답은 `ApiResponse<T>` 래퍼로 통일
- 예외는 반드시 `BusinessException(ErrorCode)` 사용
- Controller → Service → Mapper 레이어 순서 준수
- DTO는 `dto/{역할}/{기능}` 경로에 위치 (예: `dto/student/WrongNoteDto.java`)
- MyBatis XML mapper와 Java Mapper 인터페이스는 반드시 쌍으로 생성

### Frontend
- Vue 컴포넌트 파일명은 PascalCase (예: `WrongNote.vue`)
- API 호출은 `composables/` 또는 axios 직접 사용 (`/api` 프록시)
- 상태 관리는 Pinia store 사용
- 신규 페이지 추가 시 `router/index.js`에 역할별 가드 반드시 설정
- 코드 변경 시 날짜 주석: `// [YYYY-MM-DD] 변경 이유` (예: `// [2026-03-20] 북마크 버튼 제거 — 오답노트 자동저장으로 대체됨`)

## 신규 기능 구현 후 자기 검증 체크리스트

**작업 완료 전 아래 항목을 순서대로 직접 확인할 것.**

### 백엔드
- [ ] Controller / Service / Mapper XML / Mapper Interface 4개 파일이 모두 생성되었는가?
- [ ] DTO가 `dto/{역할}/{기능}/` 경로에 위치하는가?
- [ ] 모든 API 응답이 `ApiResponse<T>` 래퍼를 사용하는가?
- [ ] 예외 처리가 `BusinessException(ErrorCode)` 형태인가?
- [ ] DB 스키마 변경이 있다면 증분 SQL 파일(`database/*.sql`)이 생성되었는가?
- [ ] bootRun 재시작이 필요한 변경인가? (필요하다면 사용자에게 재시작 안내)

### 프론트엔드
- [ ] 신규 페이지라면 `router/index.js`에 역할 가드가 추가되었는가?
- [ ] 컴포넌트 파일명이 PascalCase인가?
- [ ] API 호출 후 에러 핸들링이 구현되었는가?
- [ ] 사이드바/탭 메뉴에 신규 페이지 링크가 추가되었는가? (필요한 경우)

## Key Configuration

환경변수로 주입되는 민감 설정 (`application.yml`):
- `JWT_SECRET` — 기본값 있음(개발용)
- `MAIL_USERNAME`, `MAIL_PASSWORD` — Gmail SMTP
- `AWS_S3_BUCKET`, `AWS_REGION`, `CLOUDFRONT_DOMAIN` — S3 업로드 시 필요
- `UPLOAD_PATH` — 로컬 파일 업로드 경로 (기본: `uploads/`)

## Hooks 설정

- **작업 완료 알림:** Stop 이벤트 → Windows 알림
- **자동 포맷:** Java 파일 수정 후 → `./gradlew spotlessApply` 자동 실행
- 훅 설정 위치: `.claude/hooks.json` / 확인: `/hooks`

## 참조 문서 (레이지 로딩)

상세 내용은 아래 파일을 필요할 때만 읽을 것.

- 구현 현황 및 TODO 전체: `PROJECT_CONTEXT.md`
- DB 스키마 상세: `database/schema.sql`

## 세션 종료 루틴 (매 세션 종료 전 필수)

1. PROJECT_CONTEXT.md 업데이트 — 완료 항목 `[x]` 처리, 신규 이슈/페이지/API 추가
2. `git add . && git commit -m "{prefix}: {한 줄 요약}" && git push origin dev`
   - prefix: `feat` 새기능 / `fix` 버그 / `refactor` 개선 / `docs` 문서 / `chore` 설정
3. `/clear`
