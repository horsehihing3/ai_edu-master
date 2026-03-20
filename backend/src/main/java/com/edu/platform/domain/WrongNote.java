package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WrongNote {
    private Long wrongNoteId;
    private Long studentId;
    private Long problemId;
    private Long attemptId;
    private String memo;
    private Boolean isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
