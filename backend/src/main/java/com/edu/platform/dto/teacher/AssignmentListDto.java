package com.edu.platform.dto.teacher;

import com.edu.platform.domain.Assignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentListDto {

    private Long assignmentId;
    private String title;
    private Assignment.TargetLevel targetLevel;
    private LocalDateTime dueDate;
    private Integer submittedCount;
    private Integer totalCount;
    private Double completionRate;
    private String status;
    private LocalDateTime createdAt;
}
