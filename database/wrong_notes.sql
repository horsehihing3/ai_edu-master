-- ============================================================
-- 오답노트 테이블 신규 생성 (안전한 증분 적용용)
-- ============================================================

CREATE TABLE IF NOT EXISTS wrong_notes (
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
