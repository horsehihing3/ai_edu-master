package com.edu.platform.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Problem {

    public enum Level {
        A, B, C
    }

    public enum Grade {
        GRADE_1, GRADE_2, GRADE_3
    }

    public enum Source {
        TEXTBOOK, WORKBOOK, EXAM, CUSTOM, AI_GENERATED
    }

    public enum ProblemType {
        MULTIPLE_CHOICE, SHORT_ANSWER, ESSAY, TRUE_FALSE
    }

    private Long problemId;
    private Long schoolId;
    private Long subjectCodeId;
    private Long unitCodeId;
    private String level;
    private String grade;
    private String source;

    // JOIN 조회용 (system_codes에서 가져옴)
    private String subject;
    private String unitName;
    private String subjectPath;   // 과목명
    private String unitPath;      // "법 > 헌법" 형태의 경로
    private String sourceDetail;
    private String problemType;
    private String questionText;
    private String questionImgUrl;
    private String answer;
    private String explanation;
    private String hint;
    private String aiHintText;
    private BigDecimal aiConfidence;
    private Integer difficulty;
    private Integer estimatedTime;
    private Boolean isActive;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 검수/승인 워크플로우
    private String approvalStatus;  // PENDING / APPROVED / REJECTED
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String rejectReason;

    // [2026-03-26] 중복 문제 감지 — question_text 완전 일치 시 원본 problem_id 저장
    private Long duplicateProblemId;

    // [2026-03-27] 보기(참고 지문) — 테두리 박스 안의 참고 텍스트
    private String passage;
    private String passageImgUrl;

    public Level getLevelAsEnum() {
        return level != null ? Level.valueOf(level) : null;
    }

    public ProblemType getProblemTypeAsEnum() {
        return problemType != null ? ProblemType.valueOf(problemType) : null;
    }

    public Grade getGradeAsEnum() {
        return grade != null ? Grade.valueOf(grade) : null;
    }
}
