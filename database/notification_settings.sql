-- [2026-03-20] 알림 설정 컬럼 추가 (증분 적용용)
ALTER TABLE users
  ADD COLUMN noti_assignment    TINYINT(1) NOT NULL DEFAULT 1 COMMENT '과제 알림',
  ADD COLUMN noti_announcement  TINYINT(1) NOT NULL DEFAULT 1 COMMENT '공지사항 알림',
  ADD COLUMN noti_inquiry_reply TINYINT(1) NOT NULL DEFAULT 1 COMMENT '문의 답변 알림';
