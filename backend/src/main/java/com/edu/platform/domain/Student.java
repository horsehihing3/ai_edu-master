package com.edu.platform.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Student {

    public enum Grade {
        GRADE_1, GRADE_2, GRADE_3
    }

    public enum StudentLevel {
        A, B, C
    }

    private Long studentId;
    private Long userId;
    private Long schoolId;
    private String grade;
    private String studentLevel;
    private String joinCode;
    private String parentPhone1;
    private String parentPhone2;
    private LocalDateTime diagnosisAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudentLevel getStudentLevelAsEnum() {
        return studentLevel != null ? StudentLevel.valueOf(studentLevel) : null;
    }

    public Grade getGradeAsEnum() {
        return grade != null ? Grade.valueOf(grade) : null;
    }
}
