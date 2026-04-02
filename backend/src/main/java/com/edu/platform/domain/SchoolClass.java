package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SchoolClass {
    private Long classId;
    private Long schoolId;
    private Long teacherId;
    private String className;
    private String grade;
    private String levelFilter;
    private String inviteCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
