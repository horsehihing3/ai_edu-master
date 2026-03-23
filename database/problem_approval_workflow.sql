-- [2026-03-23] 문제 검수/승인 워크플로우 컬럼 추가
ALTER TABLE problems
    ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN reviewed_by     INT          NULL,
    ADD COLUMN reviewed_at     DATETIME     NULL,
    ADD COLUMN reject_reason   VARCHAR(500) NULL;

-- 기존 데이터 초기화: 활성화된 문제는 APPROVED, 비활성 문제는 PENDING
UPDATE problems SET approval_status = 'APPROVED' WHERE is_active = 1;
UPDATE problems SET approval_status = 'PENDING'  WHERE is_active = 0;
