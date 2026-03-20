package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AttemptFeedback {
    private Long feedbackId;
    private Long attemptId;
    private Long teacherId;
    private String comment;
    private String drawingUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
