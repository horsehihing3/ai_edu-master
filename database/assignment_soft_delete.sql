-- [2026-03-23] 과제 소프트 삭제 컬럼 추가
ALTER TABLE assignments
    ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '소프트 삭제 여부 (1=삭제됨)';
