package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AssignmentFeedback {
    private Long feedbackId;
    private Long assignmentId;
    private Long studentId;
    private Long teacherId;
    private String comment;
    private String drawingUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
