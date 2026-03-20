package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Teacher {
    private Long teacherId;
    private Long userId;
    private Long schoolId;
    private String subject;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
