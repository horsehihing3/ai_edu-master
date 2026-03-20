package com.edu.platform.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DiagnosisTest {

    public enum DiagnosisStatus {
        IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum DeterminedLevel {
        A, B, C
    }

    private Long testId;
    private Long studentId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer totalQuestions;
    private Integer correctCount;
    private BigDecimal scoreRate;
    private String determinedLevel;
    private String aiAnalysis;
    private LocalDateTime createdAt;

    public DiagnosisStatus getStatusAsEnum() {
        return status != null ? DiagnosisStatus.valueOf(status) : null;
    }

    public Double getScoreRateAsDouble() {
        return scoreRate != null ? scoreRate.doubleValue() : null;
    }

    public void setScoreRateFromDouble(Double rate) {
        this.scoreRate = rate != null ? BigDecimal.valueOf(rate) : null;
    }
}
