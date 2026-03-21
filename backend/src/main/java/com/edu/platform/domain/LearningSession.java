package com.edu.platform.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LearningSession {

    public enum SessionType {
        ASSIGNMENT, SELF, BOOKMARK, SINGLE
    }

    public enum SessionStatus {
        IN_PROGRESS, COMPLETED, PAUSED
    }

    private Long sessionId;
    private Long studentId;
    private Long assignmentId;
    private String sessionType;
    private String problemIds;
    private String status;
    private Integer totalProblems;
    private Integer solvedCount;
    private Integer correctCount;
    private BigDecimal completionRate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastSavedAt;

    public SessionStatus getStatusAsEnum() {
        return status != null ? SessionStatus.valueOf(status) : null;
    }

    public Double getCompletionRateAsDouble() {
        return completionRate != null ? completionRate.doubleValue() : null;
    }

    public void setCompletionRateFromDouble(Double rate) {
        this.completionRate = rate != null ? BigDecimal.valueOf(rate) : null;
    }
}
