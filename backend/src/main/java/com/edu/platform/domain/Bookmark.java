package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Bookmark {
    private Long bookmarkId;
    private Long studentId;
    private Long problemId;
    private String memo;
    private Boolean isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
