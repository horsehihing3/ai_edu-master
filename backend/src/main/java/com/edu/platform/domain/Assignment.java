package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Assignment {

    public enum TargetType {
        ALL, LEVEL, CLASS, INDIVIDUAL
    }

    public enum TargetLevel {
        A, B, C, ALL
    }

    private Long assignmentId;
    private Long teacherId;
    private Long schoolId;
    private String title;
    private String description;
    private String targetType;
    private String targetLevel;
    private LocalDateTime dueDate;
    private Boolean isAutoAssign;
    private Boolean notifyEmail;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TargetType getTargetTypeAsEnum() {
        return targetType != null ? TargetType.valueOf(targetType) : null;
    }

    public TargetLevel getTargetLevelAsEnum() {
        return targetLevel != null ? TargetLevel.valueOf(targetLevel) : null;
    }
}
