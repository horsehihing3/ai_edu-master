-- [2026-03-27] answer 컬럼 확장 — 주관식 정답 수식 저장 위해 VARCHAR(10) → VARCHAR(500)
ALTER TABLE problems MODIFY COLUMN answer VARCHAR(500) NOT NULL DEFAULT '';
