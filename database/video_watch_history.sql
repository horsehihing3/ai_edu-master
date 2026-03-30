-- 영상 시청 이력 테이블 (2026-03-30)
CREATE TABLE IF NOT EXISTS video_watch_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    video_id    BIGINT      NOT NULL,
    watched_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    watch_duration_sec INT  NULL COMMENT '시청 지속 시간(초)',
    source      VARCHAR(20) NOT NULL DEFAULT 'DIRECT' COMMENT 'DIRECT/WRONG_NOTE/PROBLEM',
    CONSTRAINT fk_vwh_user  FOREIGN KEY (user_id)  REFERENCES users(user_id)  ON DELETE CASCADE,
    CONSTRAINT fk_vwh_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    INDEX idx_user_id  (user_id),
    INDEX idx_video_id (video_id),
    INDEX idx_watched_at (watched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
