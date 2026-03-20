package com.edu.platform.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LearningReport {
    private Long reportId;
    private Long studentId;
    private String reportType;
    private LocalDate reportDate;
    private Integer totalProblems;
    private Integer correctCount;
    private BigDecimal scoreRate;
    private Integer levelACount;
    private Integer levelBCount;
    private Integer levelCCount;
    private Long studyTimeSec;
    private String weakUnits;
    private String aiComment;
    private LocalDateTime createdAt;
}
