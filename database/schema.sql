-- ============================================================
-- AI 교육 플랫폼 - MySQL Database Schema
-- Version: 2.0
-- Created: 2026-03-14
-- Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS edu_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edu_platform;

-- ============================================================
-- 기존 테이블 전체 삭제 (외래키 체크 비활성화)
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS email_verifications;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS subscription_plans;
DROP TABLE IF EXISTS inquiry_replies;
DROP TABLE IF EXISTS inquiries;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS learning_reports;
DROP TABLE IF EXISTS video_views;
DROP TABLE IF EXISTS video_requests;
DROP TABLE IF EXISTS wrong_notes;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS attempt_feedbacks;
DROP TABLE IF EXISTS assignment_feedbacks;
DROP TABLE IF EXISTS problem_attempts;
DROP TABLE IF EXISTS learning_sessions;
DROP TABLE IF EXISTS assignment_problems;
DROP TABLE IF EXISTS assignment_targets;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS diagnosis_test_items;
DROP TABLE IF EXISTS diagnosis_tests;
DROP TABLE IF EXISTS videos;
DROP TABLE IF EXISTS problem_upload_batches;
DROP TABLE IF EXISTS problem_tags;
DROP TABLE IF EXISTS problem_options;
DROP TABLE IF EXISTS problems;
DROP TABLE IF EXISTS class_members;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS teachers;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS social_accounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS schools;
DROP TABLE IF EXISTS system_codes;
DROP TABLE IF EXISTS system_configs;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 0. 시스템 코드 (계층형 공통 코드 관리)
-- ============================================================

CREATE TABLE system_codes (
    code_id         BIGINT          NOT NULL AUTO_INCREMENT,
    code_group      VARCHAR(50)     NOT NULL COMMENT '코드 그룹 (SUBJECT/UNIT/LEVEL/GRADE/PROBLEM_TYPE/DIFFICULTY 등)',
    code_value      VARCHAR(100)    NOT NULL COMMENT '코드 값 (영문 식별자)',
    code_name       VARCHAR(200)    NOT NULL COMMENT '코드 표시명 (한글)',
    parent_code_id  BIGINT          NULL     COMMENT '부모 코드 ID (계층 구조)',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '활성 여부',
    description     VARCHAR(500)    NULL     COMMENT '코드 설명',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (code_id),
    UNIQUE KEY uk_code_group_value (code_group, code_value),
    INDEX idx_code_group (code_group),
    INDEX idx_code_parent (parent_code_id),
    FOREIGN KEY (parent_code_id) REFERENCES system_codes(code_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='시스템 공통 코드 (계층형)';

-- ============================================================
-- 1. 학교/학원 관리
-- ============================================================

CREATE TABLE schools (
    school_id       BIGINT          NOT NULL AUTO_INCREMENT,
    school_name     VARCHAR(100)    NOT NULL COMMENT '학원/학교명',
    school_code     VARCHAR(20)     NOT NULL UNIQUE COMMENT '학원 코드 (입학 코드)',
    address         VARCHAR(255)    NULL COMMENT '주소',
    phone           VARCHAR(20)     NULL COMMENT '연락처',
    logo_url        VARCHAR(500)    NULL COMMENT '로고 이미지 URL',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '활성 여부',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (school_id)
) ENGINE=InnoDB COMMENT='학교/학원 정보';

-- ============================================================
-- 2. 사용자 (통합)
-- ============================================================

CREATE TABLE users (
    user_id         BIGINT          NOT NULL AUTO_INCREMENT,
    email           VARCHAR(100)    NOT NULL UNIQUE COMMENT '이메일 (로그인 ID)',
    password_hash   VARCHAR(255)    NULL COMMENT '비밀번호 해시 (소셜 로그인 시 NULL)',
    name            VARCHAR(50)     NOT NULL COMMENT '이름',
    role            ENUM('STUDENT', 'TEACHER', 'SUPER_USER', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    school_id       BIGINT          NULL COMMENT '소속 학원 ID',
    phone           VARCHAR(20)     NULL COMMENT '연락처',
    profile_img_url MEDIUMTEXT      NULL COMMENT '프로필 이미지 URL 또는 base64',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '계정 활성 여부',
    is_email_verified TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '이메일 인증 여부',
    last_login_at   DATETIME        NULL COMMENT '마지막 로그인 일시',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL COMMENT '소프트 삭제',
    PRIMARY KEY (user_id),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_school (school_id),
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='통합 사용자';

-- ============================================================
-- 3. 소셜 로그인 계정 연동
-- ============================================================

CREATE TABLE social_accounts (
    social_id       BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    provider        ENUM('NAVER', 'KAKAO', 'GOOGLE') NOT NULL COMMENT '소셜 로그인 제공자',
    provider_user_id VARCHAR(100)   NOT NULL COMMENT '소셜 서비스에서의 사용자 ID',
    access_token    TEXT            NULL COMMENT '액세스 토큰',
    refresh_token   TEXT            NULL COMMENT '리프레시 토큰',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (social_id),
    UNIQUE KEY uk_social_provider (provider, provider_user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='소셜 로그인 연동';

-- ============================================================
-- 4. 학생 상세 정보
-- ============================================================

CREATE TABLE students (
    student_id      BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL UNIQUE,
    school_id       BIGINT          NULL COMMENT '소속 학원 ID',
    grade           ENUM('GRADE_1', 'GRADE_2', 'GRADE_3') NULL COMMENT '학년 (중1/중2/중3)',
    student_level   ENUM('A', 'B', 'C') NULL COMMENT '학습 레벨 (A:상급, B:중급, C:하급)',
    join_code       VARCHAR(50)     NULL COMMENT '입학 코드 (학원 가입 시 사용)',
    parent_phone1   VARCHAR(20)     NULL COMMENT '부모 연락처1',
    parent_phone2   VARCHAR(20)     NULL COMMENT '부모 연락처2',
    diagnosis_at    DATETIME        NULL COMMENT '진단 테스트 완료 일시',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id),
    INDEX idx_students_school (school_id),
    INDEX idx_students_level (student_level),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='학생 상세 정보';

-- ============================================================
-- 5. 교사 상세 정보
-- ============================================================

CREATE TABLE teachers (
    teacher_id      BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL UNIQUE,
    school_id       BIGINT          NULL COMMENT '소속 학원 ID',
    subject         VARCHAR(50)     NULL COMMENT '담당 과목',
    bio             TEXT            NULL COMMENT '소개',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (teacher_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='교사 상세 정보';

-- ============================================================
-- 6. 반 / 그룹 관리
-- ============================================================

CREATE TABLE classes (
    class_id        BIGINT          NOT NULL AUTO_INCREMENT,
    school_id       BIGINT          NOT NULL COMMENT '소속 학원',
    teacher_id      BIGINT          NULL COMMENT '담임 교사',
    class_name      VARCHAR(100)    NOT NULL COMMENT '반 이름',
    grade           ENUM('GRADE_1', 'GRADE_2', 'GRADE_3') NULL,
    level_filter    ENUM('A', 'B', 'C', 'ALL') NOT NULL DEFAULT 'ALL' COMMENT '대상 레벨',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (class_id),
    INDEX idx_classes_school (school_id),
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='반/그룹';

CREATE TABLE class_members (
    class_member_id BIGINT          NOT NULL AUTO_INCREMENT,
    class_id        BIGINT          NOT NULL,
    student_id      BIGINT          NOT NULL,
    joined_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (class_member_id),
    UNIQUE KEY uk_class_student (class_id, student_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='반 학생 매핑';

-- ============================================================
-- 7. 문제 DB
-- ============================================================

CREATE TABLE problems (
    problem_id      BIGINT          NOT NULL AUTO_INCREMENT,
    school_id       BIGINT          NULL COMMENT 'NULL이면 공통 문제, 값이면 학원 전용',
    subject_code_id BIGINT          NULL COMMENT '과목 코드 ID (system_codes.code_group=SUBJECT)',
    unit_code_id    BIGINT          NULL COMMENT '단원 코드 ID (system_codes.code_group=UNIT, 계층 지원)',
    level           ENUM('A', 'B', 'C') NOT NULL COMMENT '레벨 (A/B/C)',
    grade           ENUM('GRADE_1', 'GRADE_2', 'GRADE_3') NULL COMMENT '학년',
    source          ENUM('TEXTBOOK', 'PRIVATE', 'CUSTOM') NOT NULL DEFAULT 'CUSTOM' COMMENT '출처',
    source_detail   VARCHAR(200)    NULL COMMENT '출처 상세 (교재명, 페이지 등)',
    problem_type    ENUM('MULTIPLE_CHOICE', 'SHORT_ANSWER') NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    question_text   LONGTEXT        NOT NULL COMMENT '문제 내용 (HTML/LaTeX)',
    question_img_url VARCHAR(500)   NULL COMMENT '문제 이미지 URL',
    answer          VARCHAR(10)     NOT NULL COMMENT '정답 (1/2/3/4 or 단답)',
    explanation     LONGTEXT        NULL COMMENT '해설',
    hint            TEXT            NULL COMMENT '힌트',
    ai_hint_text    TEXT            NULL COMMENT 'AI 추천 학습 포인트',
    ai_confidence   DECIMAL(5,2)    NULL COMMENT 'AI 인식 신뢰도 (%)',
    difficulty      TINYINT(1)      NOT NULL DEFAULT 3 COMMENT '난이도 (1~5)',
    estimated_time  INT             NULL COMMENT '예상 풀이 시간(초)',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      BIGINT          NULL COMMENT '등록자 user_id',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (problem_id),
    INDEX idx_problems_level (level),
    INDEX idx_problems_school (school_id),
    INDEX idx_problems_grade (grade),
    INDEX idx_problems_subject_code (subject_code_id),
    INDEX idx_problems_unit_code (unit_code_id),
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (subject_code_id) REFERENCES system_codes(code_id) ON DELETE SET NULL,
    FOREIGN KEY (unit_code_id) REFERENCES system_codes(code_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='문제 DB';

-- 문제 선택지 (4지선다)
CREATE TABLE problem_options (
    option_id       BIGINT          NOT NULL AUTO_INCREMENT,
    problem_id      BIGINT          NOT NULL,
    option_no       TINYINT         NOT NULL COMMENT '선택지 번호 (1~4)',
    option_text     TEXT            NOT NULL COMMENT '선택지 내용',
    option_img_url  VARCHAR(500)    NULL,
    PRIMARY KEY (option_id),
    INDEX idx_options_problem (problem_id),
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='문제 선택지';

-- 문제 태그
CREATE TABLE problem_tags (
    tag_id          BIGINT          NOT NULL AUTO_INCREMENT,
    problem_id      BIGINT          NOT NULL,
    tag_name        VARCHAR(50)     NOT NULL COMMENT '태그명 (단원, 개념어 등)',
    PRIMARY KEY (tag_id),
    INDEX idx_tags_problem (problem_id),
    INDEX idx_tags_name (tag_name),
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='문제 태그';

-- 문제 업로드 배치 (AI 파싱 이력)
CREATE TABLE problem_upload_batches (
    batch_id        BIGINT          NOT NULL AUTO_INCREMENT,
    uploaded_by     BIGINT          NOT NULL COMMENT '업로더 user_id',
    school_id       BIGINT          NULL,
    file_name       VARCHAR(255)    NOT NULL COMMENT '업로드 파일명',
    file_url        VARCHAR(500)    NULL COMMENT 'S3 파일 URL',
    file_type       ENUM('PDF', 'DOCX', 'TXT', 'ZIP') NOT NULL,
    status          ENUM('UPLOADING', 'PARSING', 'REVIEW', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'UPLOADING',
    total_count     INT             NULL COMMENT '파싱된 문제 총 수',
    approved_count  INT             NULL COMMENT '승인된 문제 수',
    rejected_count  INT             NULL COMMENT '반려된 문제 수',
    ai_parse_log    LONGTEXT        NULL COMMENT 'AI 파싱 로그',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (batch_id),
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id),
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='문제 업로드 배치';

-- ============================================================
-- 8. 동영상 풀이
-- ============================================================

CREATE TABLE videos (
    video_id        BIGINT          NOT NULL AUTO_INCREMENT,
    problem_id      BIGINT          NULL     COMMENT '연관 문제 (NULL이면 일반 강의)',
    title           VARCHAR(200)    NOT NULL COMMENT '동영상 제목',
    subject         VARCHAR(50)     NULL     COMMENT '과목',
    video_type      ENUM('UPLOAD', 'YOUTUBE', 'VIMEO') NOT NULL DEFAULT 'UPLOAD',
    video_url       VARCHAR(500)    NULL COMMENT 'S3 URL 또는 외부 URL',
    cdn_url         VARCHAR(500)    NULL COMMENT 'CloudFront CDN URL',
    thumbnail_url   VARCHAR(500)    NULL COMMENT '썸네일 URL',
    subtitle_url    VARCHAR(500)    NULL COMMENT '자막 파일 URL (SRT)',
    duration_sec    INT             NULL COMMENT '재생 시간(초)',
    file_size_mb    DECIMAL(10,2)   NULL COMMENT '파일 크기 (MB)',
    school_id       BIGINT          NULL COMMENT 'NULL이면 공통',
    grade           ENUM('GRADE_1', 'GRADE_2', 'GRADE_3') NULL,
    level           ENUM('A', 'B', 'C') NULL,
    unit_code_id    BIGINT          NULL COMMENT '단원 코드 ID (system_codes.code_group=UNIT)',
    unit_name       VARCHAR(100)    NULL COMMENT '단원명',
    view_count      INT             NOT NULL DEFAULT 0,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    uploaded_by     BIGINT          NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (video_id),
    INDEX idx_videos_problem (problem_id),
    INDEX idx_videos_school (school_id),
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE SET NULL,
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE SET NULL,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (unit_code_id) REFERENCES system_codes(code_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='동영상 풀이';

-- ============================================================
-- 9. 진단 테스트
-- ============================================================

CREATE TABLE diagnosis_tests (
    test_id         BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      BIGINT          NOT NULL,
    status          ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') NOT NULL DEFAULT 'IN_PROGRESS',
    started_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    DATETIME        NULL,
    total_questions INT             NOT NULL DEFAULT 0,
    correct_count   INT             NOT NULL DEFAULT 0,
    score_rate      DECIMAL(5,2)    NULL COMMENT '정답률 (%)',
    determined_level ENUM('A', 'B', 'C') NULL COMMENT 'AI 진단 레벨',
    ai_analysis     TEXT            NULL COMMENT 'AI 분석 코멘트',
    PRIMARY KEY (test_id),
    INDEX idx_diagnosis_student (student_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='진단 테스트';

CREATE TABLE diagnosis_test_items (
    item_id         BIGINT          NOT NULL AUTO_INCREMENT,
    test_id         BIGINT          NOT NULL,
    problem_id      BIGINT          NOT NULL,
    problem_level   ENUM('A', 'B', 'C') NOT NULL,
    submitted_answer VARCHAR(10)    NULL COMMENT '제출 답안',
    is_correct      TINYINT(1)      NULL COMMENT '정오 (NULL=미답)',
    time_spent_sec  INT             NULL COMMENT '풀이 소요 시간(초)',
    answered_at     DATETIME        NULL,
    PRIMARY KEY (item_id),
    INDEX idx_test_items_test (test_id),
    FOREIGN KEY (test_id) REFERENCES diagnosis_tests(test_id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
) ENGINE=InnoDB COMMENT='진단 테스트 문항';

-- ============================================================
-- 10. 과제 (교사 배정)
-- ============================================================

CREATE TABLE assignments (
    assignment_id   BIGINT          NOT NULL AUTO_INCREMENT,
    teacher_id      BIGINT          NOT NULL,
    school_id       BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL COMMENT '과제명',
    description     TEXT            NULL COMMENT '설명',
    target_type     ENUM('ALL', 'LEVEL', 'CLASS', 'INDIVIDUAL') NOT NULL DEFAULT 'CLASS',
    target_level    ENUM('A', 'B', 'C', 'ALL') NULL,
    due_date        DATETIME        NULL COMMENT '마감일',
    is_auto_assign  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '레벨별 자동 분배',
    notify_email    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '이메일 알림 발송',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (assignment_id),
    INDEX idx_assignments_teacher (teacher_id),
    INDEX idx_assignments_school (school_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE,
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='교사 과제 배정';

-- 과제 대상 (반/학생)
CREATE TABLE assignment_targets (
    target_id       BIGINT          NOT NULL AUTO_INCREMENT,
    assignment_id   BIGINT          NOT NULL,
    class_id        BIGINT          NULL,
    student_id      BIGINT          NULL,
    PRIMARY KEY (target_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE SET NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='과제 배정 대상';

-- 과제 문제 목록
CREATE TABLE assignment_problems (
    ap_id           BIGINT          NOT NULL AUTO_INCREMENT,
    assignment_id   BIGINT          NOT NULL,
    problem_id      BIGINT          NOT NULL,
    order_no        INT             NOT NULL DEFAULT 1,
    PRIMARY KEY (ap_id),
    UNIQUE KEY uk_ap (assignment_id, problem_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='과제 문제 목록';

-- ============================================================
-- 11. 문제 풀기 세션 (학습 세션)
-- ============================================================

CREATE TABLE learning_sessions (
    session_id      BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      BIGINT          NOT NULL,
    assignment_id   BIGINT          NULL COMMENT '과제 풀기면 연결',
    session_type    ENUM('ASSIGNMENT', 'SELF', 'BOOKMARK') NOT NULL DEFAULT 'SELF',
    problem_ids     TEXT            NULL COMMENT '북마크 세션용 문제 ID 목록 (콤마 구분)',
    status          ENUM('IN_PROGRESS', 'COMPLETED', 'PAUSED') NOT NULL DEFAULT 'IN_PROGRESS',
    total_problems  INT             NOT NULL DEFAULT 0,
    solved_count    INT             NOT NULL DEFAULT 0,
    correct_count   INT             NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5,2)    NULL COMMENT '완료율 (%)',
    started_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    DATETIME        NULL,
    last_saved_at   DATETIME        NULL COMMENT '임시저장 시각',
    PRIMARY KEY (session_id),
    INDEX idx_sessions_student (student_id),
    INDEX idx_sessions_assignment (assignment_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='학습 세션';

-- 문제별 풀이 기록
CREATE TABLE problem_attempts (
    attempt_id      BIGINT          NOT NULL AUTO_INCREMENT,
    session_id      BIGINT          NOT NULL,
    student_id      BIGINT          NOT NULL,
    problem_id      BIGINT          NOT NULL,
    submitted_answer VARCHAR(10)    NULL COMMENT '제출 답안',
    is_correct      TINYINT(1)      NULL,
    time_spent_sec  INT             NULL COMMENT '소요 시간(초)',
    is_bookmarked   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '오답 북마크',
    requested_video TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '동영상 풀이 요청',
    feedback_like   ENUM('EASY', 'NORMAL', 'HARD') NULL COMMENT '학생 피드백',
    attempted_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (attempt_id),
    INDEX idx_attempts_session (session_id),
    INDEX idx_attempts_student (student_id),
    INDEX idx_attempts_problem (problem_id),
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='문제 풀이 기록';

-- ============================================================
-- 11-1. 과제 피드백
-- ============================================================

-- 과제 전체 피드백 (교사 → 학생, 과제 단위)
CREATE TABLE assignment_feedbacks (
    feedback_id     BIGINT          NOT NULL AUTO_INCREMENT,
    assignment_id   BIGINT          NOT NULL,
    student_id      BIGINT          NOT NULL,
    teacher_id      BIGINT          NOT NULL,
    comment         TEXT            NULL COMMENT '텍스트 피드백',
    drawing_url     MEDIUMTEXT      NULL COMMENT '펜 드로잉 이미지 URL',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (feedback_id),
    UNIQUE KEY uq_feedback_assign_student (assignment_id, student_id),
    INDEX idx_feedback_student (student_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='과제 전체 피드백';

-- 문제별 피드백 (교사 → 학생, 틀린 문제 단위)
CREATE TABLE attempt_feedbacks (
    feedback_id     BIGINT          NOT NULL AUTO_INCREMENT,
    attempt_id      BIGINT          NOT NULL,
    teacher_id      BIGINT          NOT NULL,
    comment         TEXT            NULL COMMENT '텍스트 피드백',
    drawing_url     MEDIUMTEXT      NULL COMMENT '펜 드로잉 이미지 URL',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (feedback_id),
    UNIQUE KEY uq_feedback_attempt (attempt_id),
    INDEX idx_attempt_feedback_teacher (teacher_id),
    FOREIGN KEY (attempt_id) REFERENCES problem_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='문제별 피드백';

-- ============================================================
-- 12. 즐겨찾기 (오답 노트)
-- ============================================================

CREATE TABLE bookmarks (
    bookmark_id     BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      BIGINT          NOT NULL,
    problem_id      BIGINT          NOT NULL,
    memo            TEXT            NULL COMMENT '학생 메모',
    is_resolved     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '해결 여부',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (bookmark_id),
    UNIQUE KEY uk_bookmark (student_id, problem_id),
    INDEX idx_bookmarks_student (student_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='즐겨찾기/오답노트';

-- ============================================================
-- 13. 오답노트
-- ============================================================

CREATE TABLE wrong_notes (
    wrong_note_id   BIGINT      NOT NULL AUTO_INCREMENT,
    student_id      BIGINT      NOT NULL,
    problem_id      BIGINT      NOT NULL,
    attempt_id      BIGINT      NULL COMMENT '연결된 문제 시도 ID',
    memo            TEXT        NULL COMMENT '학생 메모',
    is_resolved     TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '해결 여부',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (wrong_note_id),
    UNIQUE KEY uk_wrong_note (student_id, problem_id),
    INDEX idx_wrong_notes_student (student_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='오답노트';

-- ============================================================
-- 14. 동영상 풀이 요청
-- ============================================================

CREATE TABLE video_requests (
    request_id      BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      BIGINT          NOT NULL,
    problem_id      BIGINT          NOT NULL,
    attempt_id      BIGINT          NULL COMMENT '연관 풀이 기록',
    status          ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    video_id        BIGINT          NULL COMMENT '연결된 동영상',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (request_id),
    INDEX idx_vr_student (student_id),
    INDEX idx_vr_problem (problem_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='동영상 풀이 요청';

-- 동영상 시청 이력
CREATE TABLE video_views (
    view_id         BIGINT          NOT NULL AUTO_INCREMENT,
    video_id        BIGINT          NOT NULL,
    student_id      BIGINT          NOT NULL,
    watch_time_sec  INT             NOT NULL DEFAULT 0 COMMENT '시청 시간(초)',
    is_completed    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '완료 여부',
    viewed_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (view_id),
    INDEX idx_views_video (video_id),
    INDEX idx_views_student (student_id),
    FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='동영상 시청 이력';

-- ============================================================
-- 14. 학습 리포트
-- ============================================================

CREATE TABLE learning_reports (
    report_id       BIGINT          NOT NULL AUTO_INCREMENT,
    student_id      BIGINT          NOT NULL,
    report_type     ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL DEFAULT 'WEEKLY',
    report_date     DATE            NOT NULL COMMENT '리포트 기준 날짜',
    total_problems  INT             NOT NULL DEFAULT 0,
    correct_count   INT             NOT NULL DEFAULT 0,
    score_rate      DECIMAL(5,2)    NULL,
    level_a_count   INT             NOT NULL DEFAULT 0,
    level_b_count   INT             NOT NULL DEFAULT 0,
    level_c_count   INT             NOT NULL DEFAULT 0,
    study_time_sec  BIGINT          NOT NULL DEFAULT 0 COMMENT '총 학습 시간(초)',
    weak_units      TEXT            NULL COMMENT '취약 단원 JSON',
    ai_comment      TEXT            NULL COMMENT 'AI 생성 코멘트',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (report_id),
    UNIQUE KEY uk_report (student_id, report_type, report_date),
    INDEX idx_reports_student (student_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='학습 리포트';

-- ============================================================
-- 15. 알림 (Notification)
-- ============================================================

CREATE TABLE notifications (
    notification_id BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL COMMENT '수신 사용자',
    sender_id       BIGINT          NULL COMMENT '발신 사용자 (시스템이면 NULL)',
    noti_type       ENUM('ASSIGNMENT', 'COMPLETION', 'LEVEL_CHANGE', 'ANNOUNCEMENT', 'INQUIRY_REPLY', 'SYSTEM') NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    content         TEXT            NULL,
    link_url        VARCHAR(500)    NULL COMMENT '클릭 시 이동 URL',
    is_read         TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    INDEX idx_noti_user (user_id),
    INDEX idx_noti_read (is_read),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='알림';

-- ============================================================
-- 16. 공지사항
-- ============================================================

CREATE TABLE announcements (
    announcement_id BIGINT          NOT NULL AUTO_INCREMENT,
    school_id       BIGINT          NULL COMMENT 'NULL이면 전체 공지',
    author_id       BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    content         LONGTEXT        NOT NULL,
    is_important    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '중요 공지 여부',
    target_role     ENUM('ALL', 'STUDENT', 'TEACHER') NOT NULL DEFAULT 'ALL',
    img_url         VARCHAR(500)    NULL,
    start_at        DATETIME        NULL COMMENT '게시 시작일',
    end_at          DATETIME        NULL COMMENT '게시 종료일',
    view_count      INT             NOT NULL DEFAULT 0,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (announcement_id),
    INDEX idx_announcements_school (school_id),
    FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id)
) ENGINE=InnoDB COMMENT='공지사항';

-- ============================================================
-- 17. 1:1 문의
-- ============================================================

CREATE TABLE inquiries (
    inquiry_id      BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL COMMENT '문의자',
    category        ENUM('LEARNING', 'PAYMENT', 'ACCOUNT', 'ETC') NOT NULL DEFAULT 'ETC',
    title           VARCHAR(200)    NOT NULL,
    content         LONGTEXT        NOT NULL,
    attachment_url  VARCHAR(500)    NULL COMMENT '첨부파일 URL',
    status          ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    is_secret       TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (inquiry_id),
    INDEX idx_inquiries_user (user_id),
    INDEX idx_inquiries_status (status),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='1:1 문의';

CREATE TABLE inquiry_replies (
    reply_id        BIGINT          NOT NULL AUTO_INCREMENT,
    inquiry_id      BIGINT          NOT NULL,
    replier_id      BIGINT          NOT NULL COMMENT '답변자 user_id',
    content         LONGTEXT        NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (reply_id),
    INDEX idx_replies_inquiry (inquiry_id),
    FOREIGN KEY (inquiry_id) REFERENCES inquiries(inquiry_id) ON DELETE CASCADE,
    FOREIGN KEY (replier_id) REFERENCES users(user_id)
) ENGINE=InnoDB COMMENT='1:1 문의 답변';

-- ============================================================
-- 18. 결제 / 구독
-- ============================================================

CREATE TABLE subscription_plans (
    plan_id         BIGINT          NOT NULL AUTO_INCREMENT,
    plan_name       VARCHAR(100)    NOT NULL COMMENT '플랜명',
    plan_type       ENUM('BASIC', 'STANDARD', 'PREMIUM') NOT NULL,
    price           INT             NOT NULL COMMENT '가격 (원)',
    duration_days   INT             NOT NULL COMMENT '이용 기간 (일)',
    features        TEXT            NULL COMMENT '제공 기능 JSON',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (plan_id)
) ENGINE=InnoDB COMMENT='구독 플랜';

CREATE TABLE subscriptions (
    subscription_id BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    school_id       BIGINT          NULL,
    plan_id         BIGINT          NOT NULL,
    status          ENUM('ACTIVE', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    auto_renew      TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (subscription_id),
    INDEX idx_subscriptions_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id)
) ENGINE=InnoDB COMMENT='구독 정보';

CREATE TABLE payments (
    payment_id      BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    subscription_id BIGINT          NULL,
    pg_provider     VARCHAR(50)     NULL COMMENT 'PG사명 (아임포트/토스페이먼츠)',
    pg_tid          VARCHAR(100)    NULL COMMENT 'PG사 거래 ID',
    merchant_uid    VARCHAR(100)    NOT NULL UNIQUE COMMENT '주문 번호',
    amount          INT             NOT NULL COMMENT '결제 금액',
    status          ENUM('PENDING', 'COMPLETED', 'REFUNDED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    paid_at         DATETIME        NULL,
    refunded_at     DATETIME        NULL,
    refund_reason   TEXT            NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (payment_id),
    INDEX idx_payments_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='결제 이력';

-- ============================================================
-- 19. 이메일 인증 토큰
-- ============================================================

CREATE TABLE email_verifications (
    verification_id BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NULL,
    email           VARCHAR(100)    NOT NULL,
    token           VARCHAR(100)    NOT NULL UNIQUE,
    purpose         ENUM('SIGNUP', 'PASSWORD_RESET') NOT NULL DEFAULT 'SIGNUP',
    expires_at      DATETIME        NOT NULL,
    used_at         DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (verification_id),
    INDEX idx_ev_token (token),
    INDEX idx_ev_email (email)
) ENGINE=InnoDB COMMENT='이메일 인증 토큰';

-- ============================================================
-- 20. 리프레시 토큰
-- ============================================================

CREATE TABLE refresh_tokens (
    token_id        BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    token           VARCHAR(500)    NOT NULL,
    expires_at      DATETIME        NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (token_id),
    INDEX idx_rt_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='JWT 리프레시 토큰';

-- ============================================================
-- 21. 시스템 설정
-- ============================================================

CREATE TABLE system_configs (
    config_key      VARCHAR(100)    NOT NULL,
    config_value    TEXT            NOT NULL,
    description     VARCHAR(255)    NULL,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (config_key)
) ENGINE=InnoDB COMMENT='시스템 설정';

-- ============================================================
-- 기초 데이터 (Initial Data)
-- ============================================================

-- ============================================================
-- 시스템 코드 초기 데이터
-- ============================================================

-- ── 레벨 코드 ──────────────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order, description) VALUES
('LEVEL', 'A', 'A 레벨 (상급)', NULL, 1, '상위권 학생 대상 심화 수준'),
('LEVEL', 'B', 'B 레벨 (중급)', NULL, 2, '중위권 학생 대상 표준 수준'),
('LEVEL', 'C', 'C 레벨 (하급)', NULL, 3, '하위권 학생 대상 기초 수준');

-- ── 학년 코드 ──────────────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('GRADE', 'GRADE_1', '중학교 1학년', NULL, 1),
('GRADE', 'GRADE_2', '중학교 2학년', NULL, 2),
('GRADE', 'GRADE_3', '중학교 3학년', NULL, 3);

-- ── 문제 유형 코드 ──────────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('PROBLEM_TYPE', 'MULTIPLE_CHOICE', '객관식', NULL, 1),
('PROBLEM_TYPE', 'SHORT_ANSWER',    '단답형', NULL, 2);

-- ── 난이도 코드 ─────────────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('DIFFICULTY', '1', '매우 쉬움', NULL, 1),
('DIFFICULTY', '2', '쉬움',     NULL, 2),
('DIFFICULTY', '3', '보통',     NULL, 3),
('DIFFICULTY', '4', '어려움',   NULL, 4),
('DIFFICULTY', '5', '매우 어려움', NULL, 5);

-- ── 과목 코드 (최상위) ─────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('SUBJECT', 'MATH',    '수학', NULL, 1),
('SUBJECT', 'ENGLISH', '영어', NULL, 2),
('SUBJECT', 'KOREAN',  '국어', NULL, 3),
('SUBJECT', 'SCIENCE', '과학', NULL, 4),
('SUBJECT', 'SOCIAL',  '사회', NULL, 5);

-- ── 단원 코드 ID 변수 (과목) ──────────────────────────────
SET @s_math    = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='MATH');
SET @s_english = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='ENGLISH');
SET @s_korean  = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='KOREAN');
SET @s_science = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='SCIENCE');
SET @s_social  = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='SOCIAL');

-- ── 단원 코드 - 수학 (SUBJECT:MATH의 자식) ────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'MATH_NUMBER',   '수와 연산',   @s_math, 1),
('UNIT', 'MATH_ALGEBRA',  '문자와 식',   @s_math, 2),
('UNIT', 'MATH_FUNCTION', '함수',        @s_math, 3),
('UNIT', 'MATH_GEOMETRY', '도형',        @s_math, 4),
('UNIT', 'MATH_STAT',     '확률과 통계', @s_math, 5);

SET @u_math_algebra  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_ALGEBRA');
SET @u_math_number   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_NUMBER');
SET @u_math_function = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_FUNCTION');
SET @u_math_geometry = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_GEOMETRY');

-- ── 단원 코드 - 수학 하위 (UNIT:MATH_ALGEBRA의 자식) ───────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'MATH_ALGEBRA_EQ1',  '일차방정식', @u_math_algebra, 1),
('UNIT', 'MATH_ALGEBRA_EQ2',  '이차방정식', @u_math_algebra, 2),
('UNIT', 'MATH_ALGEBRA_INEQ', '부등식',     @u_math_algebra, 3),
('UNIT', 'MATH_ALGEBRA_POLY', '다항식',     @u_math_algebra, 4),
('UNIT', 'MATH_ALGEBRA_EXPR', '문자와 식',  @u_math_algebra, 5),
('UNIT', 'MATH_ALGEBRA_SIM',  '연립방정식', @u_math_algebra, 6),
('UNIT', 'MATH_ALGEBRA_SEQ',  '수열',       @u_math_algebra, 7);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'MATH_NUMBER_INT',    '정수와 유리수',     @u_math_number, 1),
('UNIT', 'MATH_NUMBER_FACTOR', '소인수분해',        @u_math_number, 2),
('UNIT', 'MATH_NUMBER_ARITH',  '자연수의 혼합계산', @u_math_number, 3),
('UNIT', 'MATH_NUMBER_DIVIS',  '약수와 배수',       @u_math_number, 4),
('UNIT', 'MATH_NUMBER_RATIO',  '비와 비율',         @u_math_number, 5);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'MATH_FUNCTION_LINEAR', '일차함수',    @u_math_function, 1),
('UNIT', 'MATH_FUNCTION_QUAD',   '이차함수',    @u_math_function, 2),
('UNIT', 'MATH_FUNCTION_TRIG',   '삼각함수',    @u_math_function, 3),
('UNIT', 'MATH_FUNCTION_EXP',    '지수와 로그', @u_math_function, 4);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'MATH_GEOMETRY_BASIC', '기본 도형',   @u_math_geometry, 1),
('UNIT', 'MATH_GEOMETRY_AREA',  '도형의 넓이', @u_math_geometry, 2);

-- ── 단원 코드 - 영어 ──────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'ENG_READING', '독해', @s_english, 1),
('UNIT', 'ENG_GRAMMAR', '문법', @s_english, 2),
('UNIT', 'ENG_WRITING', '쓰기', @s_english, 3),
('UNIT', 'ENG_VOCAB',   '어휘', @s_english, 4);

SET @u_eng_reading = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_READING');
SET @u_eng_grammar = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_GRAMMAR');
SET @u_eng_writing = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_WRITING');
SET @u_eng_vocab   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_VOCAB');

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'ENG_READING_INFER', '추론', @u_eng_reading, 1),
('UNIT', 'ENG_READING_MAIN',  '요지', @u_eng_reading, 2),
('UNIT', 'ENG_READING_BLANK', '빈칸', @u_eng_reading, 3),
('UNIT', 'ENG_READING_TOPIC', '주제', @u_eng_reading, 4);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'ENG_GRAMMAR_REL',   '관계대명사', @u_eng_grammar, 1),
('UNIT', 'ENG_GRAMMAR_TENSE', '시제',       @u_eng_grammar, 2),
('UNIT', 'ENG_GRAMMAR_PREP',  '전치사',     @u_eng_grammar, 3);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'ENG_WRITING_STRUCT', '문장구조', @u_eng_writing, 1);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'ENG_VOCAB_SYN', '동의어', @u_eng_vocab, 1);

-- ── 단원 코드 - 국어 ──────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'KOR_NONLIT',  '비문학', @s_korean, 1),
('UNIT', 'KOR_LIT',     '문학',   @s_korean, 2),
('UNIT', 'KOR_GRAMMAR', '문법',   @s_korean, 3),
('UNIT', 'KOR_SPEECH',  '화법',   @s_korean, 4),
('UNIT', 'KOR_LISTEN',  '듣기',   @s_korean, 5);

SET @u_kor_nonlit  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_NONLIT');
SET @u_kor_lit     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_LIT');
SET @u_kor_grammar = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_GRAMMAR');
SET @u_kor_speech  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_SPEECH');
SET @u_kor_listen  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_LISTEN');

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'KOR_NONLIT_INFER', '추론적 독해', @u_kor_nonlit, 1),
('UNIT', 'KOR_NONLIT_FACT',  '사실적 독해', @u_kor_nonlit, 2);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'KOR_LIT_MOD_NOVEL', '현대소설', @u_kor_lit, 1),
('UNIT', 'KOR_LIT_MOD_POEM',  '현대시',   @u_kor_lit, 2),
('UNIT', 'KOR_LIT_CLS_POEM',  '고전시가', @u_kor_lit, 3);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'KOR_GRAMMAR_SENT',  '문장 성분', @u_kor_grammar, 1),
('UNIT', 'KOR_GRAMMAR_PARTS', '품사',      @u_kor_grammar, 2);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'KOR_SPEECH_DEBATE', '토론', @u_kor_speech, 1);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'KOR_LISTEN_MAIN', '중심내용', @u_kor_listen, 1);

-- ── 단원 코드 - 과학 ──────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SCI_PHYSICS', '물리',     @s_science, 1),
('UNIT', 'SCI_CHEM',    '화학',     @s_science, 2),
('UNIT', 'SCI_BIO',     '생물',     @s_science, 3),
('UNIT', 'SCI_EARTH',   '지구과학', @s_science, 4),
('UNIT', 'SCI_MATTER',  '물질',     @s_science, 5);

SET @u_sci_physics = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_PHYSICS');
SET @u_sci_chem    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_CHEM');
SET @u_sci_bio     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_BIO');
SET @u_sci_earth   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_EARTH');
SET @u_sci_matter  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_MATTER');

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SCI_PHYSICS_WAVE', '파동', @u_sci_physics, 1),
('UNIT', 'SCI_PHYSICS_MECH', '역학', @u_sci_physics, 2),
('UNIT', 'SCI_PHYSICS_ELEC', '전기', @u_sci_physics, 3);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SCI_CHEM_REACT', '반응식', @u_sci_chem, 1);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SCI_BIO_GENETICS', '유전', @u_sci_bio, 1),
('UNIT', 'SCI_BIO_CELL',     '세포', @u_sci_bio, 2);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SCI_EARTH_STRATA', '지층',   @u_sci_earth, 1),
('UNIT', 'SCI_EARTH_SOLAR',  '태양계', @u_sci_earth, 2);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SCI_MATTER_STATE', '상태변화', @u_sci_matter, 1);

-- ── 단원 코드 - 사회 ──────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_LAW',      '법',      @s_social, 1),
('UNIT', 'SOC_POLITICS', '정치',    @s_social, 2),
('UNIT', 'SOC_ECONOMY',  '경제',    @s_social, 3),
('UNIT', 'SOC_HISTORY',  '역사',    @s_social, 4),
('UNIT', 'SOC_GEO_WORLD','세계지리',@s_social, 5),
('UNIT', 'SOC_GEO',      '지리',    @s_social, 6);

SET @u_soc_law      = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_LAW');
SET @u_soc_politics = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_POLITICS');
SET @u_soc_economy  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_ECONOMY');
SET @u_soc_history  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_HISTORY');
SET @u_soc_geo_world= (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_GEO_WORLD');
SET @u_soc_geo      = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_GEO');

-- ── 단원 코드 - 사회 > 법 하위 ────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_LAW_CONSTITUTION', '헌법',   @u_soc_law, 1),
('UNIT', 'SOC_LAW_CIVIL',        '민법',   @u_soc_law, 2),
('UNIT', 'SOC_LAW_CRIMINAL',     '형법',   @u_soc_law, 3),
('UNIT', 'SOC_LAW_ADMIN',        '행정법', @u_soc_law, 4);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_POLITICS_DEMO',  '민주주의', @u_soc_politics, 1),
('UNIT', 'SOC_POLITICS_ELECT', '선거',     @u_soc_politics, 2);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_ECONOMY_TRADE',  '국제무역', @u_soc_economy, 1),
('UNIT', 'SOC_ECONOMY_MARKET', '시장',     @u_soc_economy, 2),
('UNIT', 'SOC_ECONOMY_BASIC',  '기본 개념',@u_soc_economy, 3);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_HIST_KOR', '한국사', @u_soc_history, 1);

SET @u_soc_hist_kor = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_HIST_KOR');

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_HIST_KOR_MOD', '근현대', @u_soc_hist_kor, 1);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_GEO_WORLD_CLIMATE', '기후', @u_soc_geo_world, 1);

INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('UNIT', 'SOC_GEO_KOR_TERRAIN', '한국 지형', @u_soc_geo, 1);

-- ── 문의 카테고리 코드 ─────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('INQUIRY_CATEGORY', 'LEARNING', '학습 관련', NULL, 1),
('INQUIRY_CATEGORY', 'PAYMENT',  '결제 관련', NULL, 2),
('INQUIRY_CATEGORY', 'ACCOUNT',  '계정 관련', NULL, 3),
('INQUIRY_CATEGORY', 'ETC',      '기타',      NULL, 4);

-- ── 문의 상태 코드 ─────────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('INQUIRY_STATUS', 'PENDING',     '접수 대기', NULL, 1),
('INQUIRY_STATUS', 'IN_PROGRESS', '처리 중',   NULL, 2),
('INQUIRY_STATUS', 'COMPLETED',   '처리 완료', NULL, 3);

-- ── 공지 대상 코드 ─────────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('ANNOUNCEMENT_TARGET', 'ALL',     '전체',   NULL, 1),
('ANNOUNCEMENT_TARGET', 'STUDENT', '학생',   NULL, 2),
('ANNOUNCEMENT_TARGET', 'TEACHER', '교사',   NULL, 3);

-- ── 출처 코드 ──────────────────────────────────────────────
INSERT INTO system_codes (code_group, code_value, code_name, parent_code_id, sort_order) VALUES
('PROBLEM_SOURCE', 'TEXTBOOK', '교과서',    NULL, 1),
('PROBLEM_SOURCE', 'PRIVATE',  '사설 교재', NULL, 2),
('PROBLEM_SOURCE', 'CUSTOM',   '자체 제작', NULL, 3);

-- 구독 플랜
INSERT INTO subscription_plans (plan_name, plan_type, price, duration_days, features) VALUES
('기본 플랜', 'BASIC',    9900,  30, '{"problems":true,"video":false,"report":false}'),
('스탠다드', 'STANDARD', 19900, 30, '{"problems":true,"video":true,"report":false}'),
('프리미엄', 'PREMIUM',  29900, 30, '{"problems":true,"video":true,"report":true,"ai":true}');

-- 시스템 설정
INSERT INTO system_configs (config_key, config_value, description) VALUES
('DIAGNOSIS_A_MIN_SCORE', '80', '진단 테스트 A등급 최소 정답률(%)'),
('DIAGNOSIS_B_MIN_SCORE', '50', '진단 테스트 B등급 최소 정답률(%)'),
('DIAGNOSIS_PROBLEM_COUNT', '15', '진단 테스트 문항 수'),
('MAX_ASSIGNMENT_PROBLEMS', '30', '과제 최대 문제 수'),
('VIDEO_REQUEST_DAILY_LIMIT', '5', '하루 동영상 풀이 요청 최대 횟수'),
('EMAIL_RESEND_INTERVAL_SEC', '60', '이메일 재발송 대기 시간(초)'),
('BOOKMARK_MAX_COUNT', '200', '즐겨찾기 최대 개수');

-- 샘플 학원
INSERT INTO schools (school_name, school_code, address, phone) VALUES
('AI 교육 플랫폼 샘플학원', 'SCH001', '서울시 강남구 테헤란로 123', '02-1234-5678');

-- 관리자 계정 (password: Admin@1234 → bcrypt)
INSERT INTO users (email, password_hash, name, role, is_active, is_email_verified) VALUES
('admin@edu-platform.com', '$2a$10$9BqQzbc5dgy2XqF7arqqn.r9/NhpbJIFHyyxrZZpa/VYHbZhBhrsW', 'system', 'ADMIN', 1, 1);
