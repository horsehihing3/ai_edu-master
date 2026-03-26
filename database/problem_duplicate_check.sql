-- [2026-03-26] 문제 중복 체크를 위한 duplicate_problem_id 컬럼 추가
ALTER TABLE problems
ADD COLUMN duplicate_problem_id BIGINT NULL DEFAULT NULL COMMENT '중복 문제 ID';
