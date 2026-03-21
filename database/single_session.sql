-- [2026-03-21] 오답노트 단일문제 세션 타입 추가
-- session_type ENUM에 'SINGLE' 값 추가
ALTER TABLE learning_sessions
    MODIFY COLUMN session_type ENUM('ASSIGNMENT', 'SELF', 'BOOKMARK', 'SINGLE') NOT NULL DEFAULT 'SELF';
