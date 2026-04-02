-- [2026-04-03] 학급 초대코드 추가
-- classes 테이블에 invite_code 컬럼 추가 (8자리 영숫자, 유니크)
ALTER TABLE classes
    ADD COLUMN invite_code VARCHAR(8) NULL UNIQUE COMMENT '학급 초대코드 (8자리 영숫자)';

-- 기존 학급에 임시 코드 자동 부여 (서버 재시작 시 정상 코드로 대체됨)
UPDATE classes
SET invite_code = UPPER(SUBSTRING(MD5(CONCAT(class_id, NOW())), 1, 8))
WHERE invite_code IS NULL;

-- 이후 신규 생성 학급은 ClassService에서 SecureRandom으로 자동 생성
