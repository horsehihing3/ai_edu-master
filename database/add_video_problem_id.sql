-- [2026-03-30] videos 테이블에 problem_id 컬럼 추가 (테스트용, 추후 중간테이블로 전환 예정)
ALTER TABLE videos ADD COLUMN problem_id BIGINT NULL DEFAULT NULL COMMENT '연결 문제 ID';
ALTER TABLE videos ADD INDEX idx_videos_problem_id (problem_id);
